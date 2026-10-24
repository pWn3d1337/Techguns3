package techguns.client.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import techguns.client.model.TGModel;
import techguns.client.render.TGMatrixOps;

/**
 * Renders a code-drawn {@link TGModel} as an item (magazines, rockets, ...). Base class of the gun renderers.
 */
public class RenderItemBase implements TGItemRenderer {

    public static final float SCALE = 0.0625f;

    protected TGModel model;
    protected ResourceLocation texture;

    protected float baseScale = 1.0f;

    protected float scale_thirdp = 0.35f;
    protected float scale_ground = 0.5f;
    protected float scale_ego = 0.5f;
    protected float scale_gui = 0.4f;
    protected float scale_itemframe = 0.5f;

    protected float[] translateBase = {0f, 0f, 0f};

    protected float[][] translateType = {{0f, 0f, 0f}, // TRANSLATE FIRST PERSON
            {0f, 0f, 0f}, // TRANSLATE THIRD PERSON
            {0f, 0f, 0f}, // TRANSLATE GUI
            {0f, 0f, 0f}, // TRANSLATE GROUND
            {0f, 0f, -0.05f} // TRANSLATE FIXED (frame)
    };

    protected int parts = 1;

    protected String ambientParticleFX = null;

    public RenderItemBase(TGModel model, ResourceLocation texture) {
        super();
        this.model = model;
        this.texture = texture;
    }

    public RenderItemBase setGUIScale(float guiscale) {
        this.scale_gui = guiscale;
        return this;
    }

    public RenderItemBase setFirstPersonScale(float scale) {
        this.scale_ego = scale;
        return this;
    }

    public RenderItemBase setGroundAndFrameScale(float scale) {
        this.scale_ground = scale;
        this.scale_itemframe = scale;
        return this;
    }

    /**
     * Set the basic translation applied to ALL types
     */
    public RenderItemBase setBaseTranslation(float x, float y, float z) {
        this.translateBase[0] = x;
        this.translateBase[1] = y;
        this.translateBase[2] = z;
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
    public RenderItemBase setTransformTranslations(float[][] translations) {
        this.translateType = translations;
        return this;
    }

    public RenderItemBase setBaseScale(float baseScale) {
        this.baseScale = baseScale;
        return this;
    }

    protected float getScaleFactorFromTransform(ItemDisplayContext transform) {
        switch (transform) {
            case FIRST_PERSON_LEFT_HAND:
            case FIRST_PERSON_RIGHT_HAND:
                return baseScale * scale_ego;

            case THIRD_PERSON_LEFT_HAND:
            case THIRD_PERSON_RIGHT_HAND:
                return baseScale * scale_thirdp;

            case GUI:
                return baseScale * scale_gui;

            case GROUND:
                return baseScale * scale_ground;

            case FIXED:
                return baseScale * scale_itemframe;

            default:
                return baseScale;
        }
    }

    protected void applyTranslation(PoseStack poseStack, ItemDisplayContext transform) {
        int index = -1;
        boolean flip = false;

        switch (transform) {
            case FIRST_PERSON_LEFT_HAND:
                flip = true; // fallthrough
            case FIRST_PERSON_RIGHT_HAND:
                index = 0;
                break;

            case THIRD_PERSON_LEFT_HAND:
                flip = true; // fallthrough
            case THIRD_PERSON_RIGHT_HAND:
                index = 1;
                break;
            case GUI:
                index = 2;
                break;
            case GROUND:
                index = 3;
                break;
            case FIXED:
                index = 4;
                break;
            default:
                break;
        }
        if (index >= 0) {
            float mirror = flip ? -1.0f : 1.0f;
            poseStack.translate((translateType[index][0]) * mirror, translateType[index][1], translateType[index][2]);
        }
    }

    /**
     * center the model
     */
    protected void applyBaseTranslation(PoseStack poseStack) {
        poseStack.translate(this.translateBase[0], this.translateBase[1], this.translateBase[2]);
    }

    @Override
    public void render(ItemRenderContext context, PoseStack poseStack, MultiBufferSource buffers, int light, int overlay) {
        ItemDisplayContext transform = context.displayContext();
        LivingEntity entity = context.entity();

        poseStack.pushPose();

        this.applyTranslation(poseStack, transform);

        if (ItemDisplayContext.GUI == transform) {
            TGMatrixOps.rotate(poseStack, 40.0f, 0, 1f, 0);
            TGMatrixOps.rotate(poseStack, 20.0f, 1f, 0, 0);
        } else if (ItemDisplayContext.FIXED == transform) {
            TGMatrixOps.rotate(poseStack, -90.0f, 0, 1.0f, 0);
        }

        this.setBaseScale(entity, poseStack, transform);
        this.setBaseRotation(poseStack, transform);
        this.applyBaseTranslation(poseStack);

        for (int i = 0; i < parts; i++) {
            model.render(entity, poseStack, buffers.getBuffer(model.getRenderType(texture, i)), 0, 0f, transform, i, 0f, 0f, light, overlay);
        }

        poseStack.popPose();
    }

    protected void setBaseScale(LivingEntity entity, PoseStack poseStack, ItemDisplayContext transform) {
        float scale = getScaleFactorFromTransform(transform);
        poseStack.scale(scale, scale, scale);
    }

    protected void setBaseRotation(PoseStack poseStack, ItemDisplayContext transform) {
        TGMatrixOps.rotate(poseStack, -180.0f, 1.0f, 0, 0);
        TGMatrixOps.rotate(poseStack, 180.0f, 0f, 1.0f, 0);
    }

    public String getAmbientParticleFX() {
        return ambientParticleFX;
    }

    public RenderItemBase setAmbientParticleFX(String ambientParticleFX) {
        this.ambientParticleFX = ambientParticleFX;
        return this;
    }
}
