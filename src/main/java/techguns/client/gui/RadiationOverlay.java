package techguns.client.gui;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.neoforged.neoforge.client.extensions.common.IClientMobEffectExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import techguns.core.radiation.RadiationRules;
import techguns.radiation.ItemRadiation;
import techguns.radiation.PlayerRadiation;
import techguns.radiation.RadiationEffects;
import techguns.registry.TGMobEffects;

/**
 * Techguns 2 radiation HUD ({@code TGGuiEvents}): the player's radiation, coloured by poisoning stage, and the dose
 * per second while the radiation effect is on them, at the top of the {@link TGHud} stack; the radiation level in the
 * tooltip of radioactive items.
 */
public final class RadiationOverlay {
    private RadiationOverlay() {
    }

    /**
     * Techguns 2 drew the radiation icon itself at full opacity: the inventory radiation lasts only seconds, so the
     * vanilla icon would always be fading out.
     */
    public static void registerEffectIcon(RegisterClientExtensionsEvent event) {
        event.registerMobEffect(new IClientMobEffectExtensions() {
            @Override
            public boolean renderGuiIcon(MobEffectInstance instance, Gui gui, GuiGraphics graphics, int x, int y, float z, float alpha) {
                TextureAtlasSprite sprite = Minecraft.getInstance().getMobEffectTextures().get(instance.getEffect());
                graphics.blit(x + 3, y + 3, 0, 18, 18, sprite);
                return true;
            }
        }, TGMobEffects.RADIATION.get());
    }

    /**
     * Draws the radiation and the dose per second right-aligned at {@code right}, the level at {@code y} and the dose
     * one line above (part of {@link TGHud}); returns the next free line above.
     */
    static int render(GuiGraphics graphics, Minecraft mc, int right, int y) {
        int level = PlayerRadiation.get(mc.player);
        if (level <= 0) {
            return y;
        }
        ChatFormatting color = switch (RadiationRules.stage(level)) {
            case LETHAL -> ChatFormatting.RED;
            case SEVERE -> ChatFormatting.GOLD;
            case MINOR -> ChatFormatting.YELLOW;
            case NONE -> ChatFormatting.WHITE;
        };
        Component total = Component.translatable("techguns.hud.radiation", level).withStyle(color);
        graphics.drawString(mc.font, total, right - mc.font.width(total), y, 0xFFFFFFFF);
        y -= TGHud.LINE;
        MobEffectInstance effect = mc.player.getEffect(TGMobEffects.RADIATION);
        if (effect != null) {
            int dose = RadiationRules.dose(effect.getAmplifier(), RadiationEffects.resistance(mc.player));
            Component rate = Component.translatable("techguns.hud.radiation_rate", dose);
            graphics.drawString(mc.font, rate, right - mc.font.width(rate), y, 0xFFFFFFFF);
            y -= TGHud.LINE;
        }
        return y;
    }

    /** Techguns 2: "Radiation" with the level as numeral from II on. */
    public static void onTooltip(ItemTooltipEvent event) {
        int level = ItemRadiation.of(event.getItemStack());
        if (level > 0) {
            var text = Component.translatable("techguns.tooltip.radiation");
            if (level > 1) {
                text.append(" " + techguns.radiation.RadMedicineItem.roman(level));
            }
            event.getToolTip().add(text.withStyle(ChatFormatting.GREEN));
        }
    }
}
