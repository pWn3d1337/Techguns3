package techguns.client.gui.machine;

import java.util.List;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import techguns.TGIds;
import techguns.machine.AmmoPressBlockEntity;
import techguns.machine.AmmoPressMenu;
import techguns.recipe.AmmoPressRecipe;

/** Ammo Press screen (Techguns 2 {@code AmmoPressGui}): progress arrow and the build plan selector. */
public class AmmoPressScreen extends TGMachineScreen<AmmoPressMenu> {
    private static final net.minecraft.resources.ResourceLocation TEXTURE = TGIds.id("textures/gui/ammo_press_gui.png");

    public AmmoPressScreen(AmmoPressMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, TEXTURE);
    }

    @Override
    protected void init() {
        super.init();
        addRenderableWidget(Button.builder(Component.literal("<"), button -> pressButton(AmmoPressBlockEntity.BUTTON_PREVIOUS))
                .bounds(leftPos + 20, topPos + 50, 20, 20).build());
        addRenderableWidget(Button.builder(Component.literal(">"), button -> pressButton(AmmoPressBlockEntity.BUTTON_NEXT))
                .bounds(leftPos + 40, topPos + 50, 20, 20).build());
    }

    @Override
    protected void renderMachineBg(GuiGraphics graphics, int x, int y) {
        if (menu.totalTime() > 0) {
            graphics.blit(TEXTURE, x + 119, y + 36, 176, 0, 19, progressScaled(21) + 1);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderLabels(graphics, mouseX, mouseY);
        drawText(graphics, Component.translatable("techguns.container.ammopress.build"), 20, 30);
        List<AmmoPressRecipe> plans = menu.machine().plans();
        if (!plans.isEmpty()) {
            drawText(graphics, plans.get(Math.floorMod(menu.plan(), plans.size())).result().getHoverName(), 20, 40);
        }
    }
}
