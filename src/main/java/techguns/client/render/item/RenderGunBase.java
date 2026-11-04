package techguns.client.render.item;

import net.minecraft.world.item.ItemStack;
import java.util.LinkedHashMap;
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
                if (this.scope != null) {
                    renderScope = true;
                } else {
                    doADSrecoil = true;
                }
            }
            if (zoomProgress > 0F && display == null) {
                this.transformADS(poseStack, zoomProgress);
            }

            if (display != null) {
                // lowered offhand gun: the turn about the hand moves to the model's root bone
                this.model.setAnimInput(this.animInput(state, display, true, doADSrecoil, leftHand, sneaking && isOffhand ? -35f : 0f, 0f));
            } else {
                this.transformFirstPerson(poseStack, fireProgress, reloadProgress, chargeProgress, leftHand, sneaking && isOffhand, doADSrecoil);
            }

        } else if (context.isThirdPerson()) {
            if (display != null) {
                // crossbow hold: the turn about the hand moves to the model's root bone (mirrored there for the left hand)
                float holdYaw = reloadProgress <= 0 && ArmPose.CROSSBOW_HOLD == armPose ? -15f : 0f;
                this.model.setAnimInput(this.animInput(state, display, false, false, leftHand, 0f, holdYaw));
            } else {
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

            for (int i = 0; i < parts; i++) {
                model.render(entity, poseStack, buffers.getBuffer(model.getRenderType(this.partTexture(context, i), i, ammoVariant)), state.loadedAmmo(), reloadProgress,
                        transform, i, fireProgress, chargeProgress, light, overlay);
            }
            this.model.setAnimInput(null);
            poseStack.popPose();

            // Draw muzzle FX (in the hand pose, without the gun's own transforms)
            if (muzzleFlashProgress > 0) {
                updateJitter(state.muzzleFlashSeed());
                if (firstPerson) {
                    if (state.zooming() && this.scope == null) {
                        this.transformADS(poseStack, zoomProgress);
                    }
                    this.drawMuzzleFx(poseStack, buffers, muzzleFlashProgress, leftHand, ammoVariant, muzzleMarker(display));
                } else {
                    poseStack.pushPose();
                    this.transformThirdPersonArmPose(poseStack, entity, reloadProgress, armPose, leftHand);
                    this.drawMuzzleFx3P(poseStack, buffers, muzzleFlashProgress, leftHand, ammoVariant, muzzleMarker(display));
                    poseStack.popPose();
                }
            } else {
                if (reloadProgress <= 0) {
                    if (firstPerson) {
                        this.drawIdleFx(entity, poseStack, buffers, leftHand, ammoVariant);
                    } else {
                        poseStack.pushPose();
                        this.transformThirdPersonArmPose(poseStack, entity, reloadProgress, armPose, leftHand);
                        this.drawIdleFx3P(entity, poseStack, buffers, leftHand, ammoVariant);
                        poseStack.popPose();
                    }
                }
                if (chargeProgress > 0.0f && this.chargeEffect != null) {
                    if (firstPerson) {
                        this.drawChargeFx(poseStack, buffers, chargeProgressFull, leftHand, ammoVariant);
                    } else {
                        poseStack.pushPose();
                        this.transformThirdPersonArmPose(poseStack, entity, reloadProgress, armPose, leftHand);
                        this.drawChargeFx3P(poseStack, buffers, chargeProgressFull, leftHand, ammoVariant);
                        poseStack.popPose();
                    }
                }
            }

        } else {
            this.model.setAnimInput(null);
            poseStack.popPose();
            if (this.scopeRecoilAnim != null && fireProgress > 0f) {
                this.scopeRecoilAnim.play(poseStack, fireProgress, ItemDisplayContext.FIRST_PERSON_LEFT_HAND == transform, this.scopeRecoilParams);
            }

            this.renderScope(poseStack, buffers, fireProgress, leftHand);
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

    /** Animation input of an in-hand draw whose whole-gun motion comes from the display file. */
    protected GeoAnimInput animInput(GunRenderState state, GunDisplay display, boolean firstPerson, boolean aiming, boolean leftHand,
                                     float holdPitch, float holdYaw) {
        return new GeoAnimInput(state.fireProgress(), state.reloadProgress(), firstPerson ? state.zoomProgress() : 0f, state.chargeProgress(),
                state.loadedAmmo() <= 0, leftHand, holdPitch, holdYaw, display.recoil().map(m -> m.layer(firstPerson, aiming)).orElse(null),
                display.reload().map(m -> m.layer(firstPerson, aiming)).orElse(null),
                display.charge().map(m -> m.layer(firstPerson, aiming)).orElse(null));
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

    /** A display transform as the item model JSON holds it: rotation (degrees, XYZ), translation (pixels), scale. */
    public record DisplayTransform(Vector3f rotation, Vector3f translation, float scale) {
    }

    private static final ItemDisplayContext[] DISPLAY_CONTEXTS = {ItemDisplayContext.FIRST_PERSON_RIGHT_HAND,
            ItemDisplayContext.FIRST_PERSON_LEFT_HAND, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND,
            ItemDisplayContext.THIRD_PERSON_LEFT_HAND, ItemDisplayContext.GUI, ItemDisplayContext.GROUND, ItemDisplayContext.FIXED};

    /**
     * This renderer's code placement per view (context translation, GUI and frame turns, base scale, rotation and
     * translation) as item model display transforms of the geo model (datagen, when a gun moves to its model files).
     * Left-hand views are stored mirrored, because vanilla mirrors them when applying.
     */
    public Map<ItemDisplayContext, DisplayTransform> exportItemTransforms() {
        Map<ItemDisplayContext, DisplayTransform> result = new LinkedHashMap<>();
        for (ItemDisplayContext context : DISPLAY_CONTEXTS) {
            PoseStack pose = new PoseStack();
            this.applyTranslation(pose, context);
            if (context == ItemDisplayContext.GUI) {
                this.transformGUI(pose);
            } else if (context == ItemDisplayContext.GROUND) {
                this.transformGround(pose);
            } else if (context == ItemDisplayContext.FIXED) {
                this.transformFixed(pose);
            }
            this.setBaseScale(null, pose, context);
            // the geo model's base placement, whether or not the geo bridge runs (datagen)
            this.setGeoBaseRotation(pose, context);
            float[] t = this.geoBaseTranslation();
            pose.translate(t[0], t[1], t[2]);
            // the geo bridge's turn: the transform maps geo model space
            pose.mulPose(Axis.ZP.rotationDegrees(180f));
            Matrix4f m = new Matrix4f(pose.last().pose());
            if (context == ItemDisplayContext.FIRST_PERSON_LEFT_HAND || context == ItemDisplayContext.THIRD_PERSON_LEFT_HAND) {
                Matrix4f mirror = new Matrix4f().scaling(-1f, 1f, 1f);
                m = new Matrix4f(mirror).mul(m).mul(mirror);
            }
            float scale = m.getScale(new Vector3f()).x;
            result.put(context, new DisplayTransform(eulerXYZ(new Matrix4f(m).scale(1f / scale)), m.getTranslation(new Vector3f()).mul(16f), scale));
        }
        return result;
    }

    /**
     * Angles (degrees) of a rotation as vanilla display transforms build it, {@code rotationXYZ} = Rx Ry Rz; at y = +-90
     * degrees (gimbal lock) z is 0. JOML's Euler getters lose that case.
     */
    private static Vector3f eulerXYZ(Matrix4f r) {
        // row 0, column 2 is sin y (JOML element mCR: column C, row R)
        float y = (float) Math.asin(Mth.clamp(r.m20(), -1f, 1f));
        float x;
        float z;
        if (Math.abs(r.m20()) < 0.9999f) {
            x = (float) Math.atan2(-r.m21(), r.m22());
            z = (float) Math.atan2(-r.m10(), r.m00());
        } else {
            x = (float) Math.atan2(r.m12(), r.m11());
            z = 0f;
        }
        return new Vector3f(x, y, z).mul(Mth.RAD_TO_DEG);
    }

    /**
     * This renderer's code presets as a display file (datagen, when a gun moves to its model files): the same animations
     * from {@code common.animation.json} with the same strength. Offsets become model pixels: the presets move the hand
     * pose, which is the model scaled by {@code baseScale} and the view's scale.
     *
     * @param charges the gun charges: its first person pull-back ({@link #setChargeTranslationAmount}) becomes
     *                {@code charge_generic} on the charge progress
     */
    public GunDisplay exportDisplay(boolean charges) {
        float firstPerson = baseScale * scale_ego;
        float thirdPerson = baseScale * scale_thirdp;
        // the scope overlay has its own recoil; only aiming without one moves the model
        Optional<GunDisplay.Strength> aiming = this.scope == null && this.scopeRecoilAnim != null
                ? Optional.of(strength(scopeRecoilAnim, scopeRecoilParams, firstPerson)) : Optional.empty();
        // only first person pulls back, as transformFirstPerson does
        Optional<GunDisplay.Motion> charge = charges && this.chargeTranslation != 0f
                ? Optional.of(new GunDisplay.Motion(new GunDisplay.Strength(CHARGE_ANIMATION, this.chargeTranslation / SCALE / firstPerson, 0f),
                Optional.empty(), new GunDisplay.Strength(CHARGE_ANIMATION, 0f, 0f)))
                : Optional.empty();
        return new GunDisplay(
                Optional.of(new GunDisplay.Motion(strength(recoilAnim, recoilParams, firstPerson), aiming,
                        strength(recoilAnim3p, recoilParams3p, thirdPerson))),
                Optional.of(new GunDisplay.Motion(strength(reloadAnim, reloadParams, firstPerson), Optional.empty(),
                        strength(reloadAnim3p, reloadParams3p, thirdPerson))),
                charge,
                true);
    }

    /** {@code common.animation.json}: {@code root} moves back along +z by one unit over the charge. */
    private static final String CHARGE_ANIMATION = "charge_generic";

    private static GunDisplay.Strength strength(GunAnimation anim, float[] params, float scale) {
        return new GunDisplay.Strength(GunAnimation.nameOf(anim), params.length > 0 ? params[0] / SCALE / scale : 0f,
                params.length > 1 ? params[1] : 0f);
    }

    /** The muzzle marker to draw the flash at: the animated bone, or its rest pose if the display file says so. */
    protected static String muzzleMarker(@Nullable GunDisplay display) {
        return display != null && !display.muzzleFollowsAnimation() ? TGGeoBridge.MUZZLE + TGGeoBridge.REST : TGGeoBridge.MUZZLE;
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

    /** At the model's {@code marker} bone if it has one, else at the configured offsets. */
    protected void drawMuzzleFx(PoseStack poseStack, MultiBufferSource buffers, float progress, boolean leftHand, String ammoVariant,
                                String marker) {
        if (this.muzzleEffect != null && this.muzzleEffect.screenEffect != null) {
            float scale = this.muzzleEffect.effectScale;
            Vector3f muzzle = markerOffset(poseStack, marker);
            float offsetX = muzzle != null ? muzzle.x : leftHand ? this.muzzleEffect.offsetX_l : this.muzzleEffect.offsetX_r;
            float offsetY = muzzle != null ? muzzle.y : this.muzzleEffect.offsetY;
            float offsetZ = muzzle != null ? muzzle.z : this.muzzleEffect.offsetZ;

            if (this.muzzleEffect.jitterScale > 0.0f) scale += muzzleEffect.jitterScale * jitterScale;
            if (this.muzzleEffect.jitterX > 0.0f) offsetX += muzzleEffect.jitterX * jitterX;
            if (this.muzzleEffect.jitterY > 0.0f) offsetY += muzzleEffect.jitterY * jitterY;

            this.muzzleEffect.screenEffect.doRender(poseStack, buffers, progress, offsetX, offsetY, offsetZ, scale, false, ammoVariant);
        }
    }

    protected void drawMuzzleFx3P(PoseStack poseStack, MultiBufferSource buffers, float progress, boolean leftHand, String ammoVariant,
                                  String marker) {
        if (this.muzzleEffect != null && this.muzzleEffect.screenEffect != null) {
            Vector3f muzzle = markerOffset(poseStack, marker);
            this.muzzleEffect.screenEffect.doRender(poseStack, buffers, progress, muzzle != null ? muzzle.x : 0,
                    muzzle != null ? muzzle.y : this.muzzleEffect.offsetY_3p, muzzle != null ? muzzle.z : this.muzzleEffect.offsetZ_3p,
                    this.muzzleEffect.effectScale * this.muzzleEffect.effectScale_3p, true, ammoVariant);
        }
    }

    protected void drawChargeFx(PoseStack poseStack, MultiBufferSource buffers, float progress, boolean leftHand, String ammoVariant) {
        if (this.chargeEffect != null && this.chargeEffect.screenEffect != null) {
            float scale = this.chargeEffect.effectScale;
            float offsetX = leftHand ? this.chargeEffect.offsetX_l : this.chargeEffect.offsetX_r;
            float offsetY = this.chargeEffect.offsetY;
            float offsetZ = this.chargeEffect.offsetZ;

            if (this.chargeEffect.jitterScale > 0.0f) scale += chargeEffect.jitterScale * jitterScale;
            if (this.chargeEffect.jitterX > 0.0f) offsetX += chargeEffect.jitterX * jitterX;
            if (this.chargeEffect.jitterY > 0.0f) offsetY += chargeEffect.jitterY * jitterY;

            this.chargeEffect.screenEffect.doRender(poseStack, buffers, progress, offsetX, offsetY, offsetZ, scale, false, ammoVariant);
        }
    }

    protected void drawChargeFx3P(PoseStack poseStack, MultiBufferSource buffers, float progress, boolean leftHand, String ammoVariant) {
        if (this.chargeEffect != null && this.chargeEffect.screenEffect != null) {
            this.chargeEffect.screenEffect.doRender(poseStack, buffers, progress, 0, this.chargeEffect.offsetY_3p, this.chargeEffect.offsetZ_3p,
                    this.chargeEffect.effectScale * this.chargeEffect.effectScale_3p, true, ammoVariant);
        }
    }

    protected void renderScope(PoseStack poseStack, MultiBufferSource buffers, float fireProgress, boolean leftHand) {
        if (this.scope != null) {
            float x = leftHand ? 0.56f : -0.56f;
            this.scope.doRender(poseStack, buffers, fireProgress, x, 0.52f, 0f, scopescale, false, null);
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
