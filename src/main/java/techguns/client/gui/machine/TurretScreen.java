package techguns.client.gui.machine;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import techguns.TGIds;
import techguns.entity.npc.TurretEntity;
import techguns.machine.TurretBlockEntity;
import techguns.machine.TurretMenu;

/**
 * Turret screen (Techguns 2 {@code TurretGui}): ammo and empty magazine slots, gun and armor plate, the repair bar
 * (turret health as tooltip), and the animal targeting and PvP buttons on the left.
 */
public class TurretScreen extends TGMachineScreen<TurretMenu> {
    private static final ResourceLocation TEXTURE = TGIds.id("textures/gui/turret_base_gui.png");

    public TurretScreen(TurretMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, TEXTURE);
        hasUpgradeSlot = false;
    }

    @Override
    protected void init() {
        super.init();
        addRenderableWidget(new IconButton(leftPos - 22, topPos + 70, 20, TurretBlockEntity.BUTTON_TARGET_ANIMALS, TEXTURE, 176,
                () -> 6 + (menu.isAttackAnimals() ? 16 : 0), 16));
        addRenderableWidget(new IconButton(leftPos - 22, topPos + 100, 20, TurretBlockEntity.BUTTON_PVP, TEXTURE, 193,
                () -> 6 + 16 * menu.pvpSetting(), 16));
    }

    @Override
    protected void renderMachineBg(GuiGraphics graphics, int x, int y) {
        // button backgrounds sticking out on the left
        graphics.blit(WIDGETS, x - 27, y + 65, 195, 0, 27, 30);
        graphics.blit(WIDGETS, x - 27, y + 95, 195, 0, 27, 30);
        if (menu.isDead()) {
            int scaled = 52 - menu.repairTime() * 52 / TurretBlockEntity.REPAIR_TIME;
            graphics.blit(TEXTURE, x + 116, y + 74, 177, 1, scaled + 1, 4);
        }
    }

    @Override
    protected void renderMachineTooltips(GuiGraphics graphics, int mx, int my, int mouseX, int mouseY) {
        if (isIn(mx, my, 115, 73, 53, 5)) {
            Component text = menu.isDead() ? Component.translatable("techguns.container.repair").append(" " + menu.repairTime() / 20 + "s")
                    : Component.translatable("techguns.turret.health").append(": " + menu.health() + "/" + TurretEntity.MAX_HEALTH);
            graphics.renderTooltip(font, text, mouseX, mouseY);
        } else if (isIn(mx, my, -22, 70, 20, 20)) {
            graphics.renderTooltip(font, Component.translatable("techguns.turret.targetAnimals").append(": ")
                    .append(menu.isAttackAnimals() ? Component.translatable("techguns.yes").withStyle(ChatFormatting.RED)
                            : Component.translatable("techguns.no").withStyle(ChatFormatting.GREEN)), mouseX, mouseY);
        } else if (isIn(mx, my, -22, 100, 20, 20)) {
            graphics.renderComponentTooltip(font, List.of(Component.translatable("techguns.turret.pvpsetting").append(": ")
                            .append(Component.translatable("techguns.turret.pvpsetting." + menu.pvpSetting())),
                    Component.translatable("techguns.container.security.tooltip.descr2")), mouseX, mouseY);
        } else {
            super.renderMachineTooltips(graphics, mx, my, mouseX, mouseY);
        }
    }
}
