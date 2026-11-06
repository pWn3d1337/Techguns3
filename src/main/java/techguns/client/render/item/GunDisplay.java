package techguns.client.render.item;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import javax.annotation.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import techguns.client.model.GeoAnimInput;
import techguns.client.render.fx.IScreenEffect;
import techguns.client.render.fx.ScreenEffects;
import techguns.gun.ammo.AmmoType;

/**
 * Display settings of a gun whose looks live in data (resource pack: {@code assets/<namespace>/techguns/gun_display/<gun>.json}).
 * Whole-gun recoil and reload motion are animations on the model's {@code root} bone (the gun's own file or
 * {@code common.animation.json}), played with a strength per view: {@code move} scales positions (model pixels),
 * {@code turn} rotations (degrees). Effects are named ({@link ScreenEffects}) and drawn at the model's marker bones.
 *
 * @param charge                 whole-gun motion while charging (charge guns), on the charge progress
 * @param muzzleFollowsAnimation the muzzle flash sits on the animated {@code muzzle} bone (true) or at its rest pose
 * @param texture                the texture of every render part (a camo or a {@link Part} texture replaces it)
 * @param parts                  the render parts, one per geo file of the gun
 * @param muzzleFlash            drawn at the {@code muzzle} bone when the gun fires
 * @param chargeEffect           drawn at the {@code charge} bone while the gun charges
 * @param idleEffect             drawn at the {@code idle_fx} bone while the gun is idle and not under water, looping every
 *                               second (the flamethrower's pilot flame)
 * @param scope                  overlay drawn instead of the gun while aiming
 */
public record GunDisplay(Optional<Motion> recoil, Optional<Motion> reload, Optional<Motion> charge, boolean muzzleFollowsAnimation,
                         ResourceLocation texture, List<Part> parts, Optional<Effect> muzzleFlash, Optional<Effect> chargeEffect,
                         Optional<Effect> idleEffect, Optional<Scope> scope) {
    public static final Codec<GunDisplay> CODEC = RecordCodecBuilder.create(i -> i.group(
            Motion.CODEC.optionalFieldOf("recoil").forGetter(GunDisplay::recoil),
            Motion.CODEC.optionalFieldOf("reload").forGetter(GunDisplay::reload),
            Motion.CODEC.optionalFieldOf("charge").forGetter(GunDisplay::charge),
            Codec.BOOL.optionalFieldOf("muzzle_follows_animation", true).forGetter(GunDisplay::muzzleFollowsAnimation),
            ResourceLocation.CODEC.fieldOf("texture").forGetter(GunDisplay::texture),
            Part.CODEC.listOf().fieldOf("parts").forGetter(GunDisplay::parts),
            Effect.CODEC.optionalFieldOf("muzzle_flash").forGetter(GunDisplay::muzzleFlash),
            Effect.CODEC.optionalFieldOf("charge_effect").forGetter(GunDisplay::chargeEffect),
            Effect.CODEC.optionalFieldOf("idle_effect").forGetter(GunDisplay::idleEffect),
            Scope.CODEC.optionalFieldOf("scope").forGetter(GunDisplay::scope)
    ).apply(i, GunDisplay::new));

    /** The texture of render part {@code part}: by mining head level or loaded ammo if the part has one, else null. */
    @Nullable
    public ResourceLocation partTexture(int part, int headLevel, @Nullable String ammoVariant) {
        if (part >= parts.size()) {
            return null;
        }
        Part p = parts.get(part);
        if (headLevel >= 0 && headLevel < p.headLevelTextures().size()) {
            return p.headLevelTextures().get(headLevel);
        }
        if (!p.ammoTextures().isEmpty()) {
            ResourceLocation byAmmo = ammoVariant != null ? p.ammoTextures().get(ammoVariant) : null;
            return byAmmo != null ? byAmmo : p.ammoTextures().get(AmmoType.DEFAULT);
        }
        return null;
    }

    /**
     * One render part (geo file).
     *
     * @param renderType        how the part is blended
     * @param headLevelTextures the part's texture per installed mining head level (powered melee tools); higher levels use
     *                          the gun's texture
     * @param ammoTextures      the part's texture per loaded ammo variant ({@code default} for others; the rocket
     *                          launcher's rocket)
     */
    public record Part(PartRenderType renderType, List<ResourceLocation> headLevelTextures, Map<String, ResourceLocation> ammoTextures) {
        public static final Codec<Part> CODEC = RecordCodecBuilder.create(i -> i.group(
                PartRenderType.CODEC.optionalFieldOf("render_type", PartRenderType.SOLID).forGetter(Part::renderType),
                ResourceLocation.CODEC.listOf().optionalFieldOf("head_level_textures", List.of()).forGetter(Part::headLevelTextures),
                Codec.unboundedMap(Codec.STRING, ResourceLocation.CODEC).optionalFieldOf("ammo_textures", Map.of()).forGetter(Part::ammoTextures)
        ).apply(i, Part::new));
    }

    public enum PartRenderType implements StringRepresentable {
        SOLID("solid", RenderType::entitySolid),
        CUTOUT("cutout", RenderType::entityCutout),
        TRANSLUCENT("translucent", RenderType::entityTranslucent);

        public static final Codec<PartRenderType> CODEC = StringRepresentable.fromEnum(PartRenderType::values);
        private final String name;
        private final Function<ResourceLocation, RenderType> factory;

        PartRenderType(String name, Function<ResourceLocation, RenderType> factory) {
            this.name = name;
            this.factory = factory;
        }

        public RenderType renderType(ResourceLocation texture) {
            return factory.apply(texture);
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }

    /**
     * A screen effect drawn at a marker bone.
     *
     * @param scale            size in first person
     * @param thirdPersonScale size in third person, relative to {@code scale}
     * @param jitter           random change per shot
     */
    public record Effect(IScreenEffect effect, float scale, float thirdPersonScale, Optional<Jitter> jitter) {
        public static final Codec<Effect> CODEC = RecordCodecBuilder.create(i -> i.group(
                ScreenEffects.CODEC.fieldOf("effect").forGetter(Effect::effect),
                Codec.FLOAT.optionalFieldOf("scale", 1f).forGetter(Effect::scale),
                Codec.FLOAT.optionalFieldOf("third_person_scale", 0.5f).forGetter(Effect::thirdPersonScale),
                Jitter.CODEC.optionalFieldOf("jitter").forGetter(Effect::jitter)
        ).apply(i, Effect::new));
    }

    /** Up to this much random offset (x, y; first person, in blocks) and size change per shot. */
    public record Jitter(float x, float y, float scale) {
        public static final Codec<Jitter> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.FLOAT.optionalFieldOf("x", 0f).forGetter(Jitter::x),
                Codec.FLOAT.optionalFieldOf("y", 0f).forGetter(Jitter::y),
                Codec.FLOAT.optionalFieldOf("scale", 0f).forGetter(Jitter::scale)
        ).apply(i, Jitter::new));
    }

    /**
     * Scope overlay.
     *
     * @param recoil the overlay's motion when the gun fires: the {@code root} bone of this animation (gun file or
     *               {@code common.animation.json}), {@code move} in blocks per keyframe unit
     */
    public record Scope(IScreenEffect effect, float scale, Optional<Strength> recoil) {
        public static final Codec<Scope> CODEC = RecordCodecBuilder.create(i -> i.group(
                ScreenEffects.CODEC.fieldOf("effect").forGetter(Scope::effect),
                Codec.FLOAT.optionalFieldOf("scale", 3f).forGetter(Scope::scale),
                Strength.CODEC.optionalFieldOf("recoil").forGetter(Scope::recoil)
        ).apply(i, Scope::new));
    }

    /**
     * One whole-gun motion per view.
     *
     * @param aiming used while aiming down sights without a scope overlay, else {@code firstPerson}
     */
    public record Motion(Strength firstPerson, Optional<Strength> aiming, Strength thirdPerson) {
        public static final Codec<Motion> CODEC = RecordCodecBuilder.create(i -> i.group(
                Strength.CODEC.fieldOf("first_person").forGetter(Motion::firstPerson),
                Strength.CODEC.optionalFieldOf("aiming").forGetter(Motion::aiming),
                Strength.CODEC.fieldOf("third_person").forGetter(Motion::thirdPerson)
        ).apply(i, Motion::new));

        public GeoAnimInput.Layer layer(boolean firstPerson, boolean aiming) {
            Strength strength = !firstPerson ? thirdPerson : aiming ? this.aiming.orElse(this.firstPerson) : this.firstPerson;
            return new GeoAnimInput.Layer(strength.animation(), strength.move(), strength.turn());
        }
    }

    /** An animation and how strongly it moves ({@code move}) and turns ({@code turn}) the gun. */
    public record Strength(String animation, float move, float turn) {
        public static final Codec<Strength> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.STRING.fieldOf("animation").forGetter(Strength::animation),
                Codec.FLOAT.optionalFieldOf("move", 0f).forGetter(Strength::move),
                Codec.FLOAT.optionalFieldOf("turn", 0f).forGetter(Strength::turn)
        ).apply(i, Strength::new));
    }
}
