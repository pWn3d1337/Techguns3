package techguns.client.gui.machine;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import techguns.TGIds;
import techguns.armor.RepairMaterial;
import techguns.machine.RepairBenchBlockEntity;
import techguns.machine.RepairBenchMenu;

/**
 * Repair Bench screen (Techguns 2 {@code RepairBenchGui}): a repair button under each worn armor slot, the offhand and
 * the repair slot; hovering one lists the materials, missing ones in red.
 */
public class RepairBenchScreen extends TGMachineScreen<RepairBenchMenu> {
    private static final ResourceLocation TEXTURE = TGIds.id("textures/gui/repair_bench_gui.png");
    private static final int BUTTON_Y = 39;
    private static final int OFFHAND_BUTTON_X = 65;
    private static final int SLOT_BUTTON_X = 9;

    public RepairBenchScreen(RepairBenchMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, TEXTURE);
        this.powered = false;
        this.hasUpgradeSlot = false;
        this.securityButtonY = 10;
        this.showInventoryTitle = false;
    }

    @Override
    protected void init() {
        super.init();
        for (int i = 0; i < RepairBenchBlockEntity.ARMOR_SLOTS.size(); i++) {
            repairButton(armorButtonX(i), RepairBenchBlockEntity.BUTTON_REPAIR_ARMOR + i);
        }
        repairButton(OFFHAND_BUTTON_X, RepairBenchBlockEntity.BUTTON_REPAIR_OFFHAND);
        repairButton(SLOT_BUTTON_X, RepairBenchBlockEntity.BUTTON_REPAIR_SLOT);
    }

    private static int armorButtonX(int i) {
        return 90 + i * 20;
    }

    private void repairButton(int x, int id) {
        addRenderableWidget(new IconButton(leftPos + x, topPos + BUTTON_Y, 14, id, TEXTURE, 176, () -> 0, 12));
    }

    @Override
    protected void renderMachineTooltips(GuiGraphics graphics, int mx, int my, int mouseX, int mouseY) {
        super.renderMachineTooltips(graphics, mx, my, mouseX, mouseY);
        int button = -1;
        for (int i = 0; i < RepairBenchBlockEntity.ARMOR_SLOTS.size(); i++) {
            if (isIn(mx, my, armorButtonX(i), BUTTON_Y, 14, 14)) {
                button = RepairBenchBlockEntity.BUTTON_REPAIR_ARMOR + i;
            }
        }
        if (isIn(mx, my, OFFHAND_BUTTON_X, BUTTON_Y, 14, 14)) {
            button = RepairBenchBlockEntity.BUTTON_REPAIR_OFFHAND;
        } else if (isIn(mx, my, SLOT_BUTTON_X, BUTTON_Y, 14, 14)) {
            button = RepairBenchBlockEntity.BUTTON_REPAIR_SLOT;
        }
        if (button >= 0 && minecraft != null && minecraft.player != null) {
            graphics.renderComponentTooltip(font, repairTooltip(menu.machine().target(minecraft.player, button)), mouseX, mouseY);
        }
    }

    /** Techguns 2 {@code drawTooltipRepairMats}. */
    private List<Component> repairTooltip(ItemStack stack) {
        if (stack.isEmpty()) {
            return List.of(Component.translatable("techguns.repairbench.noarmor"));
        }
        if (!(stack.getItem() instanceof RepairMaterial.Repairable repairable) || !repairable.canRepairOnBench()) {
            return List.of(Component.translatable("techguns.repairbench.cantrepair"));
        }
        if (!stack.isDamaged()) {
            return List.of(Component.translatable("techguns.repairbench.fullcondition"));
        }
        List<Component> lines = new ArrayList<>();
        lines.add(Component.translatable("techguns.repairbench.requiredmats"));
        for (RepairMaterial material : repairable.repairCost(stack)) {
            lines.add(Component.literal(material.count() + "x ").append(material.display().getHoverName())
                    .withStyle(menu.machine().hasMaterial(material) ? ChatFormatting.WHITE : ChatFormatting.RED));
        }
        return lines;
    }
}
