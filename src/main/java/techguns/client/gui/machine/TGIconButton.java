package techguns.client.gui.machine;

import java.util.function.IntSupplier;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * Square vanilla button with a Techguns 2 state icon ({@code iconSize} pixels at {@code u, v()} of {@code texture},
 * centred), e.g. the redstone and security states.
 */
// MIGRATION(26.x): GuiGraphics#blit changes with deferred GUI rendering (1.21.6).
public class TGIconButton extends Button {
    private final ResourceLocation iconTexture;
    private final int iconU;
    private final IntSupplier iconV;
    private final int iconSize;

    public TGIconButton(int x, int y, int size, OnPress onPress, ResourceLocation iconTexture, int iconU, IntSupplier iconV, int iconSize) {
        super(x, y, size, size, Component.empty(), onPress, DEFAULT_NARRATION);
        this.iconTexture = iconTexture;
        this.iconU = iconU;
        this.iconV = iconV;
        this.iconSize = iconSize;
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.renderWidget(graphics, mouseX, mouseY, partialTick);
        int offset = (getWidth() - iconSize) / 2;
        graphics.blit(iconTexture, getX() + offset, getY() + offset, iconU, iconV.getAsInt(), iconSize, iconSize);
    }
}
