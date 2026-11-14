package techguns.client.gui.machine;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import techguns.TGIds;
import techguns.machine.GeneratorMenu;
import techguns.machine.LiquidGeneratorBlockEntity;

/**
 * Screens of the generators (Techguns 3): the power bar, a flame that burns down with the current fuel, and the fuel
 * slot or the tank with its container slots. Sprites at u 176 of the generated GUI textures
 * ({@code tools/tex_transport.py}): flame 14x14 at v 0, tank scale 16x48 at v 31.
 */
public final class GeneratorScreens {
    static final int FLAME_U = 176;
    static final int FLAME_V = 0;
    static final int FLAME_SIZE = 14;
    static final int SCALE_U = 176;
    static final int SCALE_V = 31;

    private GeneratorScreens() {
    }

    /** Draws the lit part of the flame at {@code x, y}: full when a fuel was just taken, gone when it is used up. */
    static void drawFlame(GuiGraphics graphics, ResourceLocation texture, int x, int y, int remaining, int total) {
        if (remaining <= 0 || total <= 0) {
            return;
        }
        int h = Math.max(1, (int) Math.ceil(remaining * (double) FLAME_SIZE / total));
        h = Math.min(FLAME_SIZE, h);
        graphics.blit(texture, x, y + FLAME_SIZE - h, FLAME_U, FLAME_V + FLAME_SIZE - h, FLAME_SIZE, h);
    }

    public static class Combustion extends TGMachineScreen<GeneratorMenu.Combustion> {
        private static final ResourceLocation TEXTURE = TGIds.id("textures/gui/combustion_generator_gui.png");

        public Combustion(GeneratorMenu.Combustion menu, Inventory inventory, Component title) {
            super(menu, inventory, title, TEXTURE);
            hasUpgradeSlot = false;
        }

        @Override
        protected void renderMachineBg(GuiGraphics graphics, int x, int y) {
            drawFlame(graphics, TEXTURE, x + GeneratorMenu.Combustion.FUEL_X + 1, y + GeneratorMenu.Combustion.FUEL_Y - 18,
                    menu.burnRemaining(), menu.burnTotal());
        }
    }

    public static class Liquid extends TGMachineScreen<GeneratorMenu.Liquid> {
        private static final ResourceLocation TEXTURE = TGIds.id("textures/gui/liquid_generator_gui.png");
        /** Tank frame (18x50) top left; the fluid fills the 16x48 inside. */
        public static final int TANK_X = 44;
        public static final int TANK_Y = 17;

        public Liquid(GeneratorMenu.Liquid menu, Inventory inventory, Component title) {
            super(menu, inventory, title, TEXTURE);
            hasUpgradeSlot = false;
        }

        @Override
        protected void renderMachineBg(GuiGraphics graphics, int x, int y) {
            drawFluid(graphics, menu.fluid(), LiquidGeneratorBlockEntity.TANK_CAPACITY, x + TANK_X + 1, y + TANK_Y + 1, 16, 48);
            graphics.blit(TEXTURE, x + TANK_X + 1, y + TANK_Y + 1, SCALE_U, SCALE_V, 16, 48);
            drawFlame(graphics, TEXTURE, x + 81, y + 36, menu.burnRemaining(), menu.burnTotal());
        }

        @Override
        protected void renderMachineTooltips(GuiGraphics graphics, int mx, int my, int mouseX, int mouseY) {
            if (isIn(mx, my, TANK_X, TANK_Y, 17, 49)) {
                graphics.renderComponentTooltip(font, tankTooltip(menu.fluid(), LiquidGeneratorBlockEntity.TANK_CAPACITY), mouseX, mouseY);
            } else {
                super.renderMachineTooltips(graphics, mx, my, mouseX, mouseY);
            }
        }
    }
}
