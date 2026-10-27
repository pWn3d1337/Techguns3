package techguns.client.gui;

import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.extensions.common.IClientMobEffectExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import techguns.TGIds;
import techguns.core.radiation.RadiationRules;
import techguns.radiation.ItemRadiation;
import techguns.radiation.PlayerRadiation;
import techguns.radiation.RadiationEffects;
import techguns.registry.TGMobEffects;

/**
 * Techguns 2 radiation HUD ({@code TGGuiEvents}): the player's radiation in the bottom right corner, coloured by
 * poisoning stage, and the dose per second while the radiation effect is on them; the radiation level in the tooltip
 * of radioactive items.
 */
public final class RadiationOverlay {
    private static final int MARGIN = 2;

    private RadiationOverlay() {
    }

    // MIGRATION(26.x): GUI layers change with the new GUI render pipeline.
    public static void register(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.HOTBAR, TGIds.id("radiation"), RadiationOverlay::render);
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

    private static void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) {
            return;
        }
        int level = PlayerRadiation.get(mc.player);
        if (level <= 0) {
            return;
        }
        ChatFormatting color = switch (RadiationRules.stage(level)) {
            case LETHAL -> ChatFormatting.RED;
            case SEVERE -> ChatFormatting.GOLD;
            case MINOR -> ChatFormatting.YELLOW;
            case NONE -> ChatFormatting.WHITE;
        };
        int y = graphics.guiHeight() - MARGIN - mc.font.lineHeight;
        Component total = Component.translatable("techguns.hud.radiation", level).withStyle(color);
        graphics.drawString(mc.font, total, graphics.guiWidth() - MARGIN - mc.font.width(total), y, 0xFFFFFFFF);
        MobEffectInstance effect = mc.player.getEffect(TGMobEffects.RADIATION);
        if (effect != null) {
            int dose = RadiationRules.dose(effect.getAmplifier(), RadiationEffects.resistance(mc.player));
            Component rate = Component.translatable("techguns.hud.radiation_rate", dose);
            graphics.drawString(mc.font, rate, graphics.guiWidth() - MARGIN - mc.font.width(rate), y - 10, 0xFFFFFFFF);
        }
    }

    /** Techguns 2: "Radiation" with the level as numeral from II on. */
    public static void onTooltip(ItemTooltipEvent event) {
        int level = ItemRadiation.of(event.getItemStack());
        if (level > 0) {
            Component text = Component.translatable("techguns.tooltip.radiation");
            if (level > 1) {
                text = Component.translatable("potion.withAmplifier", text, Component.translatable("potion.potency." + (level - 1)));
            }
            event.getToolTip().add(text.copy().withStyle(ChatFormatting.GREEN));
        }
    }
}
