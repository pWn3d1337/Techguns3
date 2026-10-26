package techguns.client.render.item;

import java.util.Random;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel.ArmPose;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import techguns.client.model.TGModel;
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

    // local muzzle flash jitter offsets, re-rolled when a new flash starts (-1.0 to 1.0)
    private final Random jitterRandom = new Random();
    private float lastFlashProgress = 0f;
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

        // guard: the Techguns 2 flow leaves transforms on the stack in some branches
        poseStack.pushPose();

        poseStack.pushPose();

        this.applyTranslation(poseStack, transform);

        if (firstPerson) {
            boolean doADSrecoil = false;
            if (state.zooming()) {
                if (this.scope != null) {
                    renderScope = true;
                } else {
                    doADSrecoil = true;
                }
            }
            if (zoomProgress > 0F) {
                this.transformADS(poseStack, zoomProgress);
            }

            this.transformFirstPerson(poseStack, fireProgress, reloadProgress, chargeProgress, leftHand, sneaking && isOffhand, doADSrecoil);

        } else if (context.isThirdPerson()) {
            this.transformThirdPerson(poseStack, entity, fireProgress, reloadProgress, leftHand, armPose);

        } else if (ItemDisplayContext.GUI == transform) {
            this.transformGUI(poseStack);

        } else if (ItemDisplayContext.GROUND == transform) {
            this.transformGround(poseStack);

        } else if (ItemDisplayContext.FIXED == transform) {
            this.transformFixed(poseStack);
        }

        if (!renderScope) {
            this.setBaseScale(entity, poseStack, transform);
            this.setBaseRotation(poseStack, transform);
            this.applyBaseTranslation(poseStack);

            for (int i = 0; i < parts; i++) {
                model.render(entity, poseStack, buffers.getBuffer(model.getRenderType(this.partTexture(context, i), i, ammoVariant)), state.loadedAmmo(), reloadProgress,
                        transform, i, fireProgress, chargeProgress, light, overlay);
            }
            poseStack.popPose();

            // Draw muzzle FX (in the hand pose, without the gun's own transforms)
            if (muzzleFlashProgress > 0) {
                updateJitter(muzzleFlashProgress);
                if (firstPerson) {
                    if (state.zooming() && this.scope == null) {
                        this.transformADS(poseStack, zoomProgress);
                    }
                    this.drawMuzzleFx(poseStack, buffers, muzzleFlashProgress, leftHand, ammoVariant);
                } else {
                    poseStack.pushPose();
                    this.transformThirdPersonArmPose(poseStack, entity, reloadProgress, armPose, leftHand);
                    this.drawMuzzleFx3P(poseStack, buffers, muzzleFlashProgress, leftHand, ammoVariant);
                    poseStack.popPose();
                }
            } else {
                lastFlashProgress = 0f;
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
            poseStack.popPose();
            if (this.scopeRecoilAnim != null && fireProgress > 0f) {
                this.scopeRecoilAnim.play(poseStack, fireProgress, ItemDisplayContext.FIRST_PERSON_LEFT_HAND == transform, this.scopeRecoilParams);
            }

            this.renderScope(poseStack, buffers, fireProgress, leftHand);
        }

        poseStack.popPose();
    }

    /** Texture of one render part; overridden where it depends on the stack (mining heads). */
    protected ResourceLocation partTexture(ItemRenderContext context, int part) {
        return this.texture;
    }

    /** Re-rolls the muzzle flash jitter whenever a new flash starts. */
    private void updateJitter(float flashProgress) {
        if (flashProgress < lastFlashProgress || lastFlashProgress == 0f) {
            jitterX = 1.0f - (jitterRandom.nextFloat() * 2.0f);
            jitterY = 1.0f - (jitterRandom.nextFloat() * 2.0f);
            jitterScale = 1.0f - (jitterRandom.nextFloat() * 2.0f);
        }
        lastFlashProgress = flashProgress;
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

    protected void drawMuzzleFx(PoseStack poseStack, MultiBufferSource buffers, float progress, boolean leftHand, String ammoVariant) {
        if (this.muzzleEffect != null && this.muzzleEffect.screenEffect != null) {
            float scale = this.muzzleEffect.effectScale;
            float offsetX = leftHand ? this.muzzleEffect.offsetX_l : this.muzzleEffect.offsetX_r;
            float offsetY = this.muzzleEffect.offsetY;
            float offsetZ = this.muzzleEffect.offsetZ;

            if (this.muzzleEffect.jitterScale > 0.0f) scale += muzzleEffect.jitterScale * jitterScale;
            if (this.muzzleEffect.jitterX > 0.0f) offsetX += muzzleEffect.jitterX * jitterX;
            if (this.muzzleEffect.jitterY > 0.0f) offsetY += muzzleEffect.jitterY * jitterY;

            this.muzzleEffect.screenEffect.doRender(poseStack, buffers, progress, offsetX, offsetY, offsetZ, scale, false, ammoVariant);
        }
    }

    protected void drawMuzzleFx3P(PoseStack poseStack, MultiBufferSource buffers, float progress, boolean leftHand, String ammoVariant) {
        if (this.muzzleEffect != null && this.muzzleEffect.screenEffect != null) {
            this.muzzleEffect.screenEffect.doRender(poseStack, buffers, progress, 0, this.muzzleEffect.offsetY_3p, this.muzzleEffect.offsetZ_3p,
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
        poseStack.translate((-0.5588f + this.adsOffsets[0]) * zoomProgress, (0.15f + this.adsOffsets[1]) * zoomProgress, (0.31f + this.adsOffsets[2]) * zoomProgress);
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
