package techguns.client.gui;

import java.util.List;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import techguns.item.AmmoBagTooltip;

/** Draws the stacks stored in an Ammo Bag as a grid of slots below the item name. */
// MIGRATION(26.x): ClientTooltipComponent / GuiGraphics item drawing changed in 1.21.2+.
public class ClientAmmoBagTooltip implements ClientTooltipComponent {
    private static final int COLUMNS = 8;
    private static final int SLOT = 18;

    private final List<ItemStack> items;

    public ClientAmmoBagTooltip(AmmoBagTooltip tooltip) {
        this.items = tooltip.items();
    }

    public static void register(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(AmmoBagTooltip.class, ClientAmmoBagTooltip::new);
    }

    private int columns() {
        return Math.max(1, Math.min(COLUMNS, items.size()));
    }

    private int rows() {
        return (items.size() + COLUMNS - 1) / COLUMNS;
    }

    @Override
    public int getHeight() {
        return rows() * SLOT + (items.isEmpty() ? 0 : 4);
    }

    @Override
    public int getWidth(Font font) {
        return columns() * SLOT;
    }

    @Override
    public void renderImage(Font font, int x, int y, GuiGraphics graphics) {
        for (int i = 0; i < items.size(); i++) {
            int slotX = x + (i % COLUMNS) * SLOT;
            int slotY = y + (i / COLUMNS) * SLOT;
            graphics.fill(slotX, slotY, slotX + SLOT - 1, slotY + SLOT - 1, 0x66000000);
            ItemStack stack = items.get(i);
            graphics.renderItem(stack, slotX + 1, slotY + 1, i);
            graphics.renderItemDecorations(font, stack, slotX + 1, slotY + 1);
        }
    }
}
