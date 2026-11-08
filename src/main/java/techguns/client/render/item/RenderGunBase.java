package techguns.client.render.item;

import net.minecraft.world.item.ItemStack;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.client.model.HumanoidModel.ArmPose;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import techguns.client.model.GeoAnimInput;
import techguns.client.model.GeoGunModel;
import techguns.client.model.TGGeoBridge;
import techguns.client.model.TGModel;
import techguns.client.render.CamoTextures;
import techguns.client.render.TGMatrixOps;
import techguns.client.render.fx.GunEffect;
import techguns.client.render.fx.IScreenEffect;
import techguns.gun.GunItem;
import techguns.gun.MeleeToolItem;

/**
 * Renders a gun: base transform per display context, recoil/reload animation, model parts, muzzle flash.
 */
public class RenderGunBase extends RenderItemBase {

    protected GunEffect muzzleEffect = null;
    protected GunEffect chargeEffect = null;

    protected IScreenEffect scope = null;

    protected GunAnimation recoilAnim = GunAnimation.genericRecoil;
    protected float[] recoilParams = new float[]{0.15f, 5.0f};
    protected GunAnimation reloadAnim = GunAnimation.genericReload;
    protected float[] reloadParams = new float[]{1.0f, 40.0f};

    protected GunAnimation scopeRecoilAnim = null;
    protected float[] scopeRecoilParams = null;

    protected GunAnimation recoilAnim3p = GunAnimation.genericRecoil;
    protected float[] recoilParams3p = new float[]{0f, 5.0f};
    protected GunAnimation reloadAnim3p = GunAnimation.genericReload;
    protected float[] reloadParams3p = new float[]{0f, 40.0f};

    protected float[] adsOffsets = new float[]{0f, 0f, 0f};

    protected float scopescale = 3.0f;

    protected float chargeTranslation = 0.25f;

    // local muzzle flash jitter offsets (-1.0 to 1.0), derived from the flash's seed: the renderer is shared by every
    // holder of this gun, so the jitter must not depend on what was drawn before
    private final Random jitterRandom = new Random();
    private long jitterSeed = Long.MIN_VALUE;
    private float jitterX = 0f;
    private float jitterY = 0f;
    private float jitterScale = 0f;

    public RenderGunBase(TGModel model, ResourceLocation texture) {
        super(model, texture);
        this.parts = 1;
        this.scale_ground = this.scale_thirdp;
    }

    // TODO multiple textures per part (camo comes later)
    public RenderGunBase(TGModel model, int parts, ResourceLocation texture) {
        super(model, texture);
        this.parts = parts;
        this.scale_ground = this.scale_thirdp;
    }

    /**
     * Set the basic translation applied to ALL types
     */
    @Override
    public RenderGunBase setBaseTranslation(float x, float y, float z) {
        return (RenderGunBase) super.setBaseTranslation(x, y, z);
    }

    @Override
    public RenderGunBase setGUIScale(float guiscale) {
        return (RenderGunBase) super.setGUIScale(guiscale);
    }

    public RenderGunBase setReloadAnim(GunAnimation anim, float... params) {
        this.reloadAnim = anim;
        this.reloadParams = params;
        return this;
    }

    public RenderGunBase setRecoilAnim(GunAnimation anim, float... params) {
        this.recoilAnim = anim;
        this.recoilParams = params;
        return this;
    }

    public RenderGunBase setScopeRecoilAnim(GunAnimation anim, float... params) {
        this.scopeRecoilAnim = anim;
        this.scopeRecoilParams = params;
        return this;
    }

    public RenderGunBase setReloadAnim3p(GunAnimation anim, float... params) {
        this.reloadAnim3p = anim;
        this.reloadParams3p = params;
        return this;
    }

    public RenderGunBase setRecoilAnim3p(GunAnimation anim, float... params) {
        this.recoilAnim3p = anim;
        this.recoilParams3p = params;
        return this;
    }

    public RenderGunBase setChargeTranslationAmount(float value) {
        this.chargeTranslation = value;
        return this;
    }

    /**
     * TRANSLATE FIRST PERSON x,y,z
     * TRANSLATE THIRD PERSON x,y,z
     * TRANSLATE GUI x,y,z
     * TRANSLATE GROUND x,y,z
     * TRANSLATE FIXED (frame) x,y,z
     * <p>
     * Left hand gets automatically mirrored
     *
     * @param translations must be a float[5][3]
     */
    @Override
    public RenderGunBase setTransformTranslations(float[][] translations) {
        return (RenderGunBase) super.setTransformTranslations(translations);
    }

    public RenderGunBase setAdsOffsets(float offsetX, float offsetY, float offsetZ) {
        this.adsOffsets = new float[]{offsetX, offsetY, offsetZ};
        return this;
    }

    public RenderGunBase setMuzzleFx(IScreenEffect muzzleFX, float x, float y, float z, float scale, float xLeft) {
        this.muzzleEffect = new GunEffect(muzzleFX, x, xLeft, y, z, scale);
        return this;
    }

    public RenderGunBase setMuzzleFXPos3P(float y, float z) {
        if (this.muzzleEffect != null) {
            this.muzzleEffect.offsetY_3p = y;
            this.muzzleEffect.offsetZ_3p = z;
        }
        return this;
    }

    public RenderGunBase setChargeFx(IScreenEffect muzzleFX, float x, float y, float z, float scale, float xLeft) {
        this.chargeEffect = new GunEffect(muzzleFX, x, xLeft, y, z, scale);
        return this;
    }

    public RenderGunBase setChargeFXPos3P(float y, float z) {
        if (this.chargeEffect != null) {
            this.chargeEffect.offsetY_3p = y;
            this.chargeEffect.offsetZ_3p = z;
        }
        return this;
    }

    public RenderGunBase setChargeFXPos3P(float y, float z, float scale) {
        if (this.chargeEffect != null) {
            this.chargeEffect.offsetY_3p = y;
            this.chargeEffect.offsetZ_3p = z;
            this.chargeEffect.effectScale_3p = scale;
        }
        return this;
    }

    public RenderGunBase setScope(IScreenEffect scope) {
        this.scope = scope;
        return this;
    }

    public RenderGunBase setScope(IScreenEffect scope, float scale) {
        this.scope = scope;
        this.scopescale = scale;
        return this;
    }

    public boolean hasScopeTexture() {
        return this.scope != null;
    }

    /** Third-person arm pose derived from the gun's hold type. */
    protected static ArmPose armPoseFor(GunItem gun) {
        switch (gun.stats().holdType()) {
            case CROSSBOW:
                return ArmPose.CROSSBOW_HOLD;
            case BOW:
                return ArmPose.BOW_AND_ARROW;
            default:
                return ArmPose.ITEM;
        }
    }

    @Override
    public void render(ItemRenderContext context, PoseStack poseStack, MultiBufferSource buffers, int light, int overlay) {
        GunItem gun = (GunItem) context.stack().getItem();
        GunRenderState state = GunRenderState.extract(context, gun);

        ItemDisplayContext transform = context.displayContext();
        LivingEntity entity = context.entity();
        boolean leftHand = context.isLeftSide();
        boolean sneaking = context.isSneaking();
        boolean isOffhand = context.isOffhand();
        ArmPose armPose = armPoseFor(gun);

        float fireProgress = state.fireProgress();
        float reloadProgress = state.reloadProgress();
        float muzzleFlashProgress = state.muzzleFlashProgress();
        float chargeProgress = state.chargeProgress();
        float chargeProgressFull = state.chargeProgressFull() > 0f ? state.chargeProgressFull() : chargeProgress;
        float zoomProgress = state.zoomProgress();
        String ammoVariant = state.ammoVariant();

        boolean firstPerson = context.isFirstPerson();
        boolean renderScope = false;
        // whole-gun motion from the model's animation files instead of the code presets below
        GunDisplay display = this.dataDisplay(gun);
        IScreenEffect scope = display != null ? display.scope().map(GunDisplay.Scope::effect).orElse(null) : this.scope;

        // guard: the Techguns 2 flow leaves transforms on the stack in some branches
        poseStack.pushPose();
        // a data-driven gun's item model places it (display transform, see exportItemTransforms); the effects below work in
        // the frame before that, so it is undone here and applied again for the model
        Matrix4f itemTransform = this.itemTransform(context);
        if (itemTransform != null) {
            applyDisplayTransform(poseStack, itemTransform, true);
        }

        poseStack.pushPose();

        if (display == null) {
            this.applyTranslation(poseStack, transform);
        }

        if (firstPerson) {
            boolean doADSrecoil = false;
            if (state.zooming()) {
                if (scope != null) {
                    renderScope = true;
                } else {
                    doADSrecoil = true;
                }
            }
            if (zoomProgress > 0F && display == null) {
                this.transformADS(poseStack, zoomProgress);
            }

            // lowered offhand gun: with a display file the turn about the hand moves to the model's root bone
            this.model.setAnimInput(this.animInput(state, gun, display, true, doADSrecoil, leftHand, sneaking && isOffhand ? -35f : 0f, 0f,
                    animEvents(context, state)));
            if (display == null) {
                this.transformFirstPerson(poseStack, fireProgress, reloadProgress, chargeProgress, leftHand, sneaking && isOffhand, doADSrecoil);
            }

        } else if (context.isThirdPerson()) {
            // crossbow hold: with a display file the turn about the hand moves to the model's root bone (mirrored there for
            // the left hand)
            float holdYaw = reloadProgress <= 0 && ArmPose.CROSSBOW_HOLD == armPose ? -15f : 0f;
            this.model.setAnimInput(this.animInput(state, gun, display, false, false, leftHand, 0f, holdYaw, animEvents(context, state)));
            if (display == null) {
                this.transformThirdPerson(poseStack, entity, fireProgress, reloadProgress, leftHand, armPose);
            }

        } else if (display != null) {
            // the item model's display transform places the gun

        } else if (ItemDisplayContext.GUI == transform) {
            this.transformGUI(poseStack);

        } else if (ItemDisplayContext.GROUND == transform) {
            this.transformGround(poseStack);

        } else if (ItemDisplayContext.FIXED == transform) {
            this.transformFixed(poseStack);
        }

        if (!renderScope) {
            if (display == null) {
                this.setBaseScale(entity, poseStack, transform);
                this.setBaseRotation(poseStack, transform);
                this.applyBaseTranslation(poseStack);
            } else if (itemTransform != null) {
                applyDisplayTransform(poseStack, itemTransform, false);
                // the item transform maps geo model space; the geo bridge turns back from Java model space
                poseStack.mulPose(Axis.ZP.rotationDegrees(180f));
            }

            int partCount = display != null ? display.parts().size() : parts;
            model.setBoneTypes(display != null ? this.boneTypes(context, display, ammoVariant) : Map.of());
            for (int i = 0; i < partCount; i++) {
                ResourceLocation texture = this.partTexture(context, i, display, ammoVariant);
                RenderType renderType = display != null ? display.parts().get(i).renderType().renderType(texture)
                        : model.getRenderType(texture, i, ammoVariant);
                model.setPartTexture(texture);
                model.render(entity, poseStack, buffers.getBuffer(renderType), state.loadedAmmo(), reloadProgress,
                        transform, i, fireProgress, chargeProgress, light, overlay);
            }
            this.model.setPartTexture(null);
            this.model.setBoneTypes(Map.of());
            this.model.setAnimInput(null);
            poseStack.popPose();

            if (firstPerson && !leftHand && fireProgress <= 0f && reloadProgress <= 0f && zoomProgress <= 0f && chargeProgress <= 0f
                    && GunConversion.wantsMuzzle(gun)) {
                this.placeMuzzle(poseStack);
            }

            // Draw muzzle FX (in the hand pose, without the gun's own transforms)
            GunDisplay.Effect muzzleFx = display != null ? display.muzzleFlash().orElse(null) : legacyEffect(this.muzzleEffect);
            GunDisplay.Effect chargeFx = display != null ? display.chargeEffect().orElse(null) : legacyEffect(this.chargeEffect);
            GunDisplay.Effect idleFx = display != null ? display.idleEffect().orElse(null) : null;
            if (muzzleFlashProgress > 0) {
                updateJitter(state.muzzleFlashSeed());
                if (firstPerson) {
                    if (state.zooming() && scope == null && display == null) {
                        this.transformADS(poseStack, zoomProgress);
                    }
                    this.drawEffect(poseStack, buffers, muzzleFx, this.muzzleEffect, muzzleMarker(display), muzzleFlashProgress, true, leftHand,
                            ammoVariant);
                } else {
                    poseStack.pushPose();
                    this.transformThirdPersonArmPose(poseStack, entity, reloadProgress, armPose, leftHand);
                    this.drawEffect(poseStack, buffers, muzzleFx, this.muzzleEffect, muzzleMarker(display), muzzleFlashProgress, false, leftHand,
                            ammoVariant);
                    poseStack.popPose();
                }
            } else {
                if (reloadProgress <= 0) {
                    if (firstPerson) {
                        if (idleFx != null) {
                            this.drawIdleEffect(entity, poseStack, buffers, idleFx, true, leftHand, ammoVariant);
                        } else {
                            this.drawIdleFx(entity, poseStack, buffers, leftHand, ammoVariant);
                        }
                    } else {
                        poseStack.pushPose();
                        this.transformThirdPersonArmPose(poseStack, entity, reloadProgress, armPose, leftHand);
                        if (idleFx != null) {
                            this.drawIdleEffect(entity, poseStack, buffers, idleFx, false, leftHand, ammoVariant);
                        } else {
                            this.drawIdleFx3P(entity, poseStack, buffers, leftHand, ammoVariant);
                        }
                        poseStack.popPose();
                    }
                }
                if (chargeProgress > 0.0f && chargeFx != null) {
                    if (firstPerson) {
                        this.drawEffect(poseStack, buffers, chargeFx, this.chargeEffect, GunConversion.CHARGE, chargeProgressFull, true, leftHand,
                                ammoVariant);
                    } else {
                        poseStack.pushPose();
                        this.transformThirdPersonArmPose(poseStack, entity, reloadProgress, armPose, leftHand);
                        this.drawEffect(poseStack, buffers, chargeFx, this.chargeEffect, GunConversion.CHARGE, chargeProgressFull, false, leftHand,
                                ammoVariant);
                        poseStack.popPose();
                    }
                }
            }

        } else {
            this.model.setAnimInput(null);
            poseStack.popPose();
            if (display != null) {
                GunDisplay.Scope scopeDisplay = display.scope().orElseThrow();
                scopeDisplay.recoil().ifPresent(recoil -> TGGeoBridge.get().applyRootMotion(poseStack,
                        BuiltInRegistries.ITEM.getKey(gun).getPath(), recoil.animation(), fireProgress, recoil.move(), recoil.turn(),
                        ItemDisplayContext.FIRST_PERSON_LEFT_HAND == transform));
                this.drawScope(poseStack, buffers, scopeDisplay.effect(), scopeDisplay.scale(), fireProgress, leftHand);
            } else {
                if (this.scopeRecoilAnim != null && fireProgress > 0f) {
                    this.scopeRecoilAnim.play(poseStack, fireProgress, ItemDisplayContext.FIRST_PERSON_LEFT_HAND == transform, this.scopeRecoilParams);
                }
                this.drawScope(poseStack, buffers, this.scope, this.scopescale, fireProgress, leftHand);
            }
        }

        poseStack.popPose();
    }

    /**
     * The gun's display file when its motion comes from its model files: it has one and is drawn as a geo model (not the
     * Java fallback, {@code /tgdebug geo}); null = the code presets ({@link GunAnimation}) move it.
     */
    @Nullable
    protected GunDisplay dataDisplay(GunItem gun) {
        return this.model instanceof GeoGunModel && TGGeoBridge.enabled() ? GunDisplays.get(gun) : null;
    }

    /**
     * Animation input of an in-hand draw. With a display file the whole-gun motion comes from the model files; without
     * one ({@code display} null) the code presets move the gun, so only the parts animate (no aim, mirror or hold turns).
     */
    protected GeoAnimInput animInput(GunRenderState state, GunItem gun, @Nullable GunDisplay display, boolean firstPerson, boolean aiming,
                                     boolean leftHand, float holdPitch, float holdYaw, @Nullable GeoAnimInput.Events events) {
        int loaded = state.loadedAmmo();
        int clip = gun.stats().firing().clipSize();
        float ammo = clip > 0 ? Mth.clamp(loaded / (float) clip, 0f, 1f) : loaded > 0 ? 1f : 0f;
        if (display == null) {
            return new GeoAnimInput(state.fireProgress(), state.reloadProgress(), 0f, state.chargeProgress(), ammo, loaded, loaded <= 0,
                    false, 0f, 0f, null, null, null, events, false, firstPerson, true, state.grapple(), state.locked());
        }
        return new GeoAnimInput(state.fireProgress(), state.reloadProgress(), firstPerson ? state.zoomProgress() : 0f, state.chargeProgress(),
                ammo, loaded, loaded <= 0, leftHand, holdPitch, holdYaw, display.recoil().map(m -> m.layer(firstPerson, aiming)).orElse(null),
                display.reload().map(m -> m.layer(firstPerson, aiming)).orElse(null),
                display.charge().map(m -> m.layer(firstPerson, aiming)).orElse(null), events, false, firstPerson, true, state.grapple(), state.locked());
    }

    /** Source of the animation keyframe events of an in-hand draw (holder, hand, running timers); null without a holder. */
    @Nullable
    private static GeoAnimInput.Events animEvents(ItemRenderContext context, GunRenderState state) {
        if (context.entity() == null || context.hand() == null) {
            return null;
        }
        return new GeoAnimInput.Events(context.entity().getId(), context.hand().ordinal(), context.isFirstPerson(), state.fireCycle(),
                state.reloadCycle());
    }

    /**
     * The display transform vanilla applied from the item model for this view (left hand mirrored as vanilla does), if the
     * gun is data-driven ({@link GunDisplays}); null otherwise (its item model has none).
     */
    @Nullable
    protected Matrix4f itemTransform(ItemRenderContext context) {
        if (GunDisplays.get(context.stack().getItem()) == null) {
            return null;
        }
        Minecraft mc = Minecraft.getInstance();
        PoseStack pose = new PoseStack();
        mc.getItemRenderer().getModel(context.stack(), null, null, 0).applyTransform(context.displayContext(), pose, context.isLeftSide());
        return pose.last().pose();
    }

    /**
     * Applies a display transform (translation, rotation, uniform scale) or its inverse piece by piece, so the normal
     * matrix stays unit length: {@code PoseStack#mulPose(Matrix4f)} recomputes it from the scaled pose, and GeckoLib
     * uses it without normalising, which lights the model too bright.
     */
    private static void applyDisplayTransform(PoseStack poseStack, Matrix4f m, boolean inverse) {
        Vector3f translation = m.getTranslation(new Vector3f());
        float scale = m.getScale(new Vector3f()).x;
        // JOML's getNormalizedRotation returns a non-unit quaternion for a scaled matrix: take the scale out first
        Quaternionf rotation = new Matrix4f(m).scale(1f / scale).getNormalizedRotation(new Quaternionf()).normalize();
        if (inverse) {
            poseStack.scale(1f / scale, 1f / scale, 1f / scale);
            poseStack.mulPose(rotation.conjugate());
            poseStack.translate(-translation.x, -translation.y, -translation.z);
        } else {
            poseStack.translate(translation.x, translation.y, translation.z);
            poseStack.mulPose(rotation);
            poseStack.scale(scale, scale, scale);
        }
    }

    /** The muzzle marker to draw the flash at: the animated bone, or its rest pose if the display file says so. */
    protected static String muzzleMarker(@Nullable GunDisplay display) {
        return display != null && !display.muzzleFollowsAnimation() ? TGGeoBridge.MUZZLE + TGGeoBridge.REST : TGGeoBridge.MUZZLE;
    }

    /**
     * Texture of one render part: with a display file its part texture (mining head, loaded ammo), else the camo, else the
     * file's texture; without one the code renderer's.
     */
    protected ResourceLocation partTexture(ItemRenderContext context, int part, @Nullable GunDisplay display, @Nullable String ammoVariant) {
        if (display == null) {
            return this.partTexture(context, part);
        }
        ResourceLocation texture = display.partTexture(part, headLevel(context), ammoVariant);
        return texture != null ? texture : camoTexture(context.stack()).orElse(display.texture());
    }

    /**
     * Render type of each bone with its own texture ({@link GunDisplay#boneTextures}): by mining head level or loaded ammo,
     * else the camo, else the file's texture.
     */
    protected Map<String, RenderType> boneTypes(ItemRenderContext context, GunDisplay display, @Nullable String ammoVariant) {
        if (display.boneTextures().isEmpty()) {
            return Map.of();
        }
        int headLevel = headLevel(context);
        Map<String, RenderType> types = new HashMap<>();
        display.boneTextures().forEach((bone, part) -> {
            ResourceLocation texture = part.texture(headLevel, ammoVariant);
            types.put(bone, part.renderType().renderType(texture != null ? texture : camoTexture(context.stack()).orElse(display.texture())));
        });
        return types;
    }

    /** The installed mining head level of a powered melee tool, else -1. */
    private static int headLevel(ItemRenderContext context) {
        return context.stack().getItem() instanceof MeleeToolItem tool ? tool.headLevel(context.stack()) : -1;
    }

    /** Texture of one render part; overridden where it depends on the stack (mining heads). */
    protected ResourceLocation partTexture(ItemRenderContext context, int part) {
        return camoTexture(context.stack()).orElse(this.texture);
    }

    /** Texture of the stack's camo, if its item has camos (datapack registry {@code techguns:camo}). */
    protected static Optional<ResourceLocation> camoTexture(ItemStack stack) {
        return CamoTextures.of(stack);
    }

    /** Sets the muzzle flash jitter of the flash with this seed (one roll per flash). */
    private void updateJitter(long seed) {
        if (seed != jitterSeed) {
            jitterSeed = seed;
            jitterRandom.setSeed(seed);
            jitterX = 1.0f - (jitterRandom.nextFloat() * 2.0f);
            jitterY = 1.0f - (jitterRandom.nextFloat() * 2.0f);
            jitterScale = 1.0f - (jitterRandom.nextFloat() * 2.0f);
        }
    }

    // idle / charge particle FX need systems that are not ported yet
    protected void drawIdleFx(LivingEntity entity, PoseStack poseStack, MultiBufferSource buffers, boolean leftHand, String ammoVariant) {
    }

    protected void drawIdleFx3P(LivingEntity entity, PoseStack poseStack, MultiBufferSource buffers, boolean leftHand, String ammoVariant) {
    }

    protected void transformFirstPerson(PoseStack poseStack, float fireProgress, float reloadProgress, float chargeProgress, boolean left, boolean shouldLowerWeapon, boolean ads) {
        if (chargeProgress > 0) {
            poseStack.translate(0, 0, this.chargeTranslation * chargeProgress);
        }
        if (fireProgress > 0) {
            if (ads && this.scopeRecoilAnim != null) {
                this.scopeRecoilAnim.play(poseStack, fireProgress, left, this.scopeRecoilParams);
            } else {
                this.recoilAnim.play(poseStack, fireProgress, left, this.recoilParams);
            }
        } else if (reloadProgress > 0) {
            this.reloadAnim.play(poseStack, reloadProgress, left, this.reloadParams);
        } else if (shouldLowerWeapon) {
            TGMatrixOps.rotate(poseStack, -35f, 1, 0, 0);
        }
    }

    protected void transformThirdPersonArmPose(PoseStack poseStack, LivingEntity entity, float reloadProgress, ArmPose armPose, boolean left) {
        if (reloadProgress <= 0 && ArmPose.CROSSBOW_HOLD == armPose) {
            TGMatrixOps.rotate(poseStack, left ? 15.0f : -15.0f, 0f, 1f, 0f);
        }
    }

    protected void transformThirdPerson(PoseStack poseStack, LivingEntity ent, float fireProgress, float reloadProgress, boolean left, ArmPose armPose) {
        transformThirdPersonArmPose(poseStack, ent, reloadProgress, armPose, left);

        if (fireProgress > 0) {
            this.recoilAnim3p.play(poseStack, fireProgress, left, this.recoilParams3p);
        } else if (reloadProgress > 0) {
            this.reloadAnim3p.play(poseStack, reloadProgress, left, this.reloadParams3p);
        }
    }

    protected void transformGUI(PoseStack poseStack) {
        TGMatrixOps.rotate(poseStack, 40.0f, 0, 1f, 0);
        TGMatrixOps.rotate(poseStack, 20.0f, 1f, 0, 0);
    }

    protected void transformGround(PoseStack poseStack) {
    }

    protected void transformFixed(PoseStack poseStack) {
        TGMatrixOps.rotate(poseStack, -90.0f, 0, 1.0f, 0);
    }

    /**
     * Position of the model's marker bone {@code name} (from the draw just done) relative to the current pose; null if
     * the model has no such bone.
     */
    @Nullable
    protected Vector3f markerOffset(PoseStack poseStack, String name) {
        Matrix4f marker = this.model.marker(name);
        if (marker == null) {
            return null;
        }
        return new Matrix4f(poseStack.last().pose()).invert().transformPosition(marker.getTranslation(new Vector3f()));
    }

    /**
     * Draws {@code effect} at the model's {@code marker} bone if it has one, else at {@code offsets} (the code renderer's
     * effect positions; nothing without them).
     */
    protected void drawEffect(PoseStack poseStack, MultiBufferSource buffers, @Nullable GunDisplay.Effect effect, @Nullable GunEffect offsets,
                              String marker, float progress, boolean firstPerson, boolean leftHand, String ammoVariant) {
        if (effect == null) {
            return;
        }
        Vector3f at = markerOffset(poseStack, marker);
        if (at == null) {
            if (offsets == null) {
                return;
            }
            at = firstPerson ? new Vector3f(leftHand ? offsets.offsetX_l : offsets.offsetX_r, offsets.offsetY, offsets.offsetZ)
                    : new Vector3f(0f, offsets.offsetY_3p, offsets.offsetZ_3p);
        }
        float scale = effect.scale();
        if (firstPerson) {
            if (effect.jitter().isPresent()) {
                GunDisplay.Jitter jitter = effect.jitter().get();
                scale += jitter.scale() * jitterScale;
                at.add(jitter.x() * jitterX, jitter.y() * jitterY, 0f);
            }
        } else {
            scale *= effect.thirdPersonScale();
        }
        effect.effect().doRender(poseStack, buffers, progress, at.x, at.y, at.z, scale, !firstPerson, ammoVariant);
    }

    /** Marker bone of the idle effect. */
    protected static final String IDLE_FX = "idle_fx";

    /** The idle effect at the {@code idle_fx} bone, looping every second; not under water. */
    protected void drawIdleEffect(@Nullable LivingEntity entity, PoseStack poseStack, MultiBufferSource buffers, GunDisplay.Effect effect,
                                  boolean firstPerson, boolean leftHand, String ammoVariant) {
        if (entity != null && !entity.isUnderWater() && Minecraft.getInstance().level != null) {
            this.drawEffect(poseStack, buffers, effect, null, IDLE_FX, idleProgress(), firstPerson, leftHand, ammoVariant);
        }
    }

    /** Progress of the looping idle effect: one loop per second of game time. */
    protected static float idleProgress() {
        return (float) (Minecraft.getInstance().level.getGameTime() % 20) / 20.0f;
    }

    /** The code renderer's effect as a display file effect; null without one. */
    @Nullable
    protected static GunDisplay.Effect legacyEffect(@Nullable GunEffect effect) {
        if (effect == null || effect.screenEffect == null) {
            return null;
        }
        Optional<GunDisplay.Jitter> jitter = effect.jitterX != 0f || effect.jitterY != 0f || effect.jitterScale != 0f
                ? Optional.of(new GunDisplay.Jitter(effect.jitterX, effect.jitterY, effect.jitterScale)) : Optional.empty();
        return new GunDisplay.Effect(effect.screenEffect, effect.effectScale, effect.effectScale_3p, jitter);
    }

    protected void drawScope(PoseStack poseStack, MultiBufferSource buffers, @Nullable IScreenEffect scope, float scale, float fireProgress,
                             boolean leftHand) {
        if (scope != null) {
            float x = leftHand ? 0.56f : -0.56f;
            scope.doRender(poseStack, buffers, fireProgress, x, 0.52f, 0f, scale, false, null);
        }
    }

    @Override
    public RenderGunBase setBaseScale(float baseScale) {
        return (RenderGunBase) super.setBaseScale(baseScale);
    }

    protected void transformADS(PoseStack poseStack, float zoomProgress) {
        float[] shift = this.adsShift();
        poseStack.translate(shift[0] * zoomProgress, shift[1] * zoomProgress, shift[2] * zoomProgress);
    }

    /** The first person hand shift at full zoom (blocks, hand space). */
    private float[] adsShift() {
        return new float[]{-0.5588f + this.adsOffsets[0], 0.15f + this.adsOffsets[1], 0.31f + this.adsOffsets[2]};
    }

    /** The base rotation of the geo model: the geo files of every gun point the same way (barrel along -Z). */
    protected final void setGeoBaseRotation(PoseStack poseStack, ItemDisplayContext transform) {
        super.setBaseRotation(poseStack, transform);
    }

    /** The base translation in the geo model's frame (turned for {@link RenderGunBase90}, whose geo files are turned). */
    protected float[] geoBaseTranslation() {
        return this.translateBase.clone();
    }

    /**
     * Conversion seed ({@link GunConversion}): the {@code root} bone pivot, i.e. the hand origin in geo model pixels. Whole
     * gun turns from the animation files then turn about the hand as the code presets did.
     */
    public float[] exportRootPivot() {
        float[] t = this.geoBaseTranslation();
        return new float[]{-16f * t[0], 16f * t[1], -16f * t[2]};
    }

    /**
     * Conversion seed ({@link GunConversion}): the {@code root} offset of the {@code aim} pose (full zoom) in geo model
     * pixels, from the hand shift of {@link #transformADS} through the first person scale.
     */
    public float[] exportAim() {
        float[] shift = this.adsShift();
        float s = this.baseScale * this.scale_ego;
        return new float[]{-16f * shift[0] / s, 16f * shift[1] / s, 16f * shift[2] / s};
    }

    /**
     * Conversion seed ({@link GunConversion}), at a first person rest frame: the {@code muzzle} marker bone where the old
     * first person flash offset is, in the first geo file's model space. {@code fxPose} is the flash's frame (the pose
     * {@link #drawMuzzleFx} draws in).
     */
    private void placeMuzzle(PoseStack fxPose) {
        String bone = GunConversion.requestedBone();
        GunEffect effect = GunConversion.CHARGE.equals(bone) ? this.chargeEffect : this.muzzleEffect;
        if (!(this.model instanceof techguns.client.model.GeoGunModel geo) || effect == null) {
            GunConversion.muzzleFailed("no geo model or effect");
            return;
        }
        Matrix4f origin = geo.marker(TGGeoBridge.MODEL + geo.geoIds().get(0));
        if (origin == null) {
            GunConversion.muzzleFailed("geo model not drawn");
            return;
        }
        Vector3f flash = new Vector3f(effect.offsetX_r, effect.offsetY, effect.offsetZ);
        Vector3f local = new Matrix4f(origin).invert().mul(fxPose.last().pose()).transformPosition(new Vector3f(flash));
        // an existing bone, in the same space: checks the conversion against a hand-placed bone
        Matrix4f existing = geo.marker(bone + TGGeoBridge.REST);
        Vector3f existingLocal = existing == null ? null
                : new Matrix4f(origin).invert().transformPosition(existing.getTranslation(new Vector3f()));
        Vector3f existingFlash = existing == null ? null : this.markerOffset(fxPose, bone + TGGeoBridge.REST);
        GunConversion.placeMuzzle(local, existingLocal, existingFlash, flash);
    }

    public RenderGunBase setMuzzleFlashJitter(float jX, float jY, float jAngle, float jScale) {
        if (this.muzzleEffect != null && this.muzzleEffect.screenEffect != null) {
            this.muzzleEffect.jitterX = jX;
            this.muzzleEffect.jitterY = jY;
            this.muzzleEffect.jitterScale = jScale;
            this.muzzleEffect.jitterAngle = jAngle;
        }
        return this;
    }

    @Override
    public RenderGunBase setAmbientParticleFX(String ambientParticleFX) {
        this.ambientParticleFX = ambientParticleFX;
        return this;
    }
}
