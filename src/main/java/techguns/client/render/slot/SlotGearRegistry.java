package techguns.client.render.slot;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import techguns.TGIds;
import techguns.Techguns;
import techguns.client.model.slot.SlotGearModel;
import techguns.client.model.slot.SlotGearModels;
import techguns.client.render.CamoTextures;

/**
 * Render setup of the extra-slot items (Techguns 2 {@code AdditionalSlotRenderRegistry} filled in {@code ClientProxy}):
 * model, default texture and per-frame state of each item, found by the item's id path (the items are registered by
 * {@code TGSlotItems}). The camo texture of a stack, if it has one, replaces the default texture.
 */
// MIGRATION(26.x): render layers / models move to render states in 1.21.2+.
public final class SlotGearRegistry {
    /**
     * @param model       the model (plain / non-slim variant)
     * @param slimModel   model for slim player arms, null = same as {@code model}
     * @param texture     default texture
     * @param slimTexture default texture for slim player arms, null = same as {@code texture}
     * @param prepare     per-frame state of the model (visible parts), may be null
     */
    public record Entry(Supplier<SlotGearModel> model, @Nullable Supplier<SlotGearModel> slimModel, ResourceLocation texture,
                        @Nullable ResourceLocation slimTexture, @Nullable BiConsumer<SlotGearModel, AbstractClientPlayer> prepare) {
        SlotGearModel model(boolean slim) {
            return slim && slimModel != null ? slimModel.get() : model.get();
        }

        ResourceLocation defaultTexture(boolean slim) {
            return slim && slimTexture != null ? slimTexture : texture;
        }
    }

    private static final Map<String, Entry> ENTRIES = new HashMap<>();

    static {
        entry("gas_mask", SlotGearModels::gasMask, "gasmask");
        entry("oxygen_mask", SlotGearModels::faceMask, "oxygenmask");
        entry("tactical_mask", SlotGearModels::faceMask, "tacticalmask");
        entry("night_vision_goggles", SlotGearModels::nightVisionGoggles, "nightvisiongoggles", SlotGearRegistry::prepareGoggles);
        entry("glider", SlotGearModels::glider, "glider", SlotGearRegistry::prepareGlider);
        entry("jetpack", SlotGearModels::jetPack, "jetpack");
        entry("jump_pack", SlotGearModels::jumpPack, "jetpack");
        entry("scuba_tanks", SlotGearModels::oxygenTanks, "oxygentanks");
        entry("antigrav_pack", SlotGearModels::antiGravPack, "antigravpack");
        ENTRIES.put("working_gloves", new Entry(() -> SlotGearModels.gloves(false), () -> SlotGearModels.gloves(true),
                TGIds.id("textures/models/armor/working_gloves.png"), TGIds.id("textures/models/armor/working_gloves_slim.png"), null));
    }

    private SlotGearRegistry() {
    }

    private static void entry(String id, Supplier<SlotGearModel> model, String texture) {
        entry(id, model, texture, null);
    }

    private static void entry(String id, Supplier<SlotGearModel> model, String texture,
                              @Nullable BiConsumer<SlotGearModel, AbstractClientPlayer> prepare) {
        ENTRIES.put(id, new Entry(model, null, TGIds.id("textures/armors/" + texture + ".png"), null, prepare));
    }

    /** Night vision goggles are flipped down while the night vision effect is active. */
    private static void prepareGoggles(SlotGearModel model, AbstractClientPlayer player) {
        boolean on = player.hasEffect(MobEffects.NIGHT_VISION);
        for (String name : new String[] {"Mid", "Mount", "Left", "Right"}) {
            model.part(name).visible = on;
            model.part(name + "_off").visible = !on;
        }
    }

    /** The wings only show while gliding. */
    private static void prepareGlider(SlotGearModel model, AbstractClientPlayer player) {
        // MIGRATION(26.x): Techguns 2 keeps an isGliding flag set by the glider tick; derived here from the glide conditions
        // (airborne, sneaking, falling) until the glider ability exists in this port.
        boolean gliding = !player.onGround() && player.isShiftKeyDown() && player.getDeltaMovement().y < -0.05;
        for (String name : SlotGearModels.GLIDER_WINGS) {
            model.part(name).visible = gliding;
        }
    }

    @Nullable
    public static Entry get(ItemStack stack) {
        ResourceLocation key = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return key.getNamespace().equals(Techguns.MODID) ? ENTRIES.get(key.getPath()) : null;
    }

    /** The stack's camo texture, else the entry's default texture. */
    public static ResourceLocation texture(Entry entry, ItemStack stack, boolean slim) {
        return CamoTextures.of(stack).orElse(entry.defaultTexture(slim));
    }
}
