package techguns.client.gui;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import techguns.TGIds;
import techguns.client.gun.LockOnTracker;
import techguns.gun.LockOnGunItem;

/**
 * Lock-on crosshair of lock-on guns (Techguns 2 / Fabric {@code TGGuiRender}): four corners close in while locking,
 * then a blinking target marker and the target's name.
 */
public final class LockOnOverlay {
    private static final ResourceLocation TEXTURE = TGIds.id("textures/gui/crosshairs.png");
    private static final int TEXTURE_SIZE = 256;

    private LockOnOverlay() {
    }

    // MIGRATION(26.x): GUI layers and GuiGraphics blit signatures change with the new GUI render pipeline.
    public static void register(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.CROSSHAIR, TGIds.id("lock_on"), LockOnOverlay::render);
    }

    private static void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui || !(mc.player.getMainHandItem().getItem() instanceof LockOnGunItem gun)) {
            return;
        }
        Entity target = LockOnTracker.target();
        int ticks = LockOnTracker.ticks();
        if (target == null || ticks <= 0) {
            return;
        }
        int x = graphics.guiWidth() / 2;
        int y = graphics.guiHeight() / 2;
        float progress = (float) ticks / gun.lockOnTicks();
        int offset = (int) Math.max(0.0f, (1.0f - progress) * 16.0f) + 5;

        blit(graphics, x - offset - 3, y - offset - 3, 0, 0, 7, 7);
        blit(graphics, x + offset - 3, y - offset - 3, 7, 0, 7, 7);
        blit(graphics, x - offset - 3, y + offset - 3, 14, 0, 7, 7);
        blit(graphics, x + offset - 3, y + offset - 3, 21, 0, 7, 7);

        Component text;
        int color;
        if (progress < 1.0f) {
            text = Component.translatable("techguns.lock_on.locking", target.getName());
            color = 0xFFFFFFFF;
        } else {
            blit(graphics, x - 6, y - 6, 28, 0, 13, 13);
            if (System.currentTimeMillis() / 250 % 2 == 0) {
                blit(graphics, x - 9, y - 9, 41, 0, 19, 19);
            }
            text = Component.translatable("techguns.lock_on.locked", target.getName());
            color = 0xFFFF0000;
        }
        graphics.drawString(mc.font, text, x + 2, y + 10, color);
    }

    private static void blit(GuiGraphics graphics, int x, int y, int u, int v, int w, int h) {
        graphics.blit(TEXTURE, x, y, u, v, w, h, TEXTURE_SIZE, TEXTURE_SIZE);
    }
}
