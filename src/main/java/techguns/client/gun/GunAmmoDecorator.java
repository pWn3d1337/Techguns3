package techguns.client.gun;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.IItemDecorator;
import net.neoforged.neoforge.client.event.RegisterItemDecorationsEvent;
import techguns.gun.GunData;
import techguns.registry.TGGuns;

/**
 * Draws the loaded ammo as a small number in the top right of gun slots (Techguns 2 item overlay).
 */
// MIGRATION(26.x): GUI rendering became deferred in 1.21.6; check the item decorator API.
public final class GunAmmoDecorator implements IItemDecorator {
    private static final GunAmmoDecorator INSTANCE = new GunAmmoDecorator();
    private static final int WHITE = 0xFFFFFF;

    private GunAmmoDecorator() {
    }

    public static void register(RegisterItemDecorationsEvent event) {
        TGGuns.guns().forEach(gun -> event.register(gun.get(), INSTANCE));
    }

    @Override
    public boolean render(GuiGraphics graphics, Font font, ItemStack stack, int x, int y) {
        String text = Integer.toString(GunData.loaded(stack));
        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 200.0f);
        graphics.pose().scale(0.5f, 0.5f, 1.0f);
        graphics.drawString(font, text, 19 + 9 - 2 - font.width(text) / 2, 0, WHITE, true);
        graphics.pose().popPose();
        return true;
    }
}
