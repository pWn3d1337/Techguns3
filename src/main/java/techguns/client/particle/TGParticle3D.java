package techguns.client.particle;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Camera;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import techguns.client.render.TGMatrixOps;

/** A particle drawn as a 3D model part (debris, gibs). Only spawned by systems that have models set. */
public class TGParticle3D extends TGParticle {
    protected static final float MODEL_SCALE = 0.0625f;

    public ModelPart model;
    /** Stored per particle, since the same particle system type is reused for different entities. */
    public ResourceLocation texture;

    float modelMinX = Float.POSITIVE_INFINITY;
    float modelMinY = Float.POSITIVE_INFINITY;
    float modelMinZ = Float.POSITIVE_INFINITY;
    float modelMaxX = Float.NEGATIVE_INFINITY;
    float modelMaxY = Float.NEGATIVE_INFINITY;
    float modelMaxZ = Float.NEGATIVE_INFINITY;

    float startSinkTime = 0.75f;
    float sinkDistance = 0.5f;

    int modelLight = 0;

    float pitch;
    float yaw;

    float anglePitch;
    float anglePitchRate;
    float anglePitchRateDamping;

    float angleRoll;
    float angleRollRate;
    float angleRollRateDamping;

    Vec3 prevDir;
    float prevPitch;
    float prevYaw;
    float prevRoll;

    public TGParticle3D(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed,
                        TGParticleSystem particleSystem, ModelPart model, ResourceLocation texture) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed, particleSystem);
        this.model = model;
        this.texture = texture;
        this.calcModelBounds(model);

        this.anglePitch = FxMath.randomFloat(random, type.anglePitchMin, type.anglePitchMax);
        this.anglePitchRate = FxMath.randomFloat(random, type.anglePitchRateMin, type.anglePitchRateMax);
        this.anglePitchRateDamping = FxMath.randomFloat(random, type.anglePitchRateDampingMin, type.anglePitchRateDampingMax);

        this.angleRoll = FxMath.randomFloat(random, type.angleRollMin, type.angleRollMax);
        this.angleRollRate = FxMath.randomFloat(random, type.angleRollRateMin, type.angleRollRateMax);
        this.angleRollRateDamping = FxMath.randomFloat(random, type.angleRollRateDampingMin, type.angleRollRateDampingMax);

        this.collidesWithWorld = true;

        Vec3 dir = new Vec3(xSpeed, ySpeed, zSpeed).normalize();
        this.prevDir = dir;
        this.yaw = (float) (FxMath.R2D * Math.atan2(dir.x, dir.z));
        this.pitch = (float) (FxMath.R2D * Math.asin(dir.y));
    }

    protected void calcModelBounds(ModelPart part) {
        part.visit(new PoseStack(), (pose, path, index, cube) -> {
            modelMinX = Math.min(modelMinX, cube.minX);
            modelMinY = Math.min(modelMinY, cube.minY);
            modelMinZ = Math.min(modelMinZ, cube.minZ);
            modelMaxX = Math.max(modelMaxX, cube.maxX);
            modelMaxY = Math.max(modelMaxY, cube.maxY);
            modelMaxZ = Math.max(modelMaxZ, cube.maxZ);
        });
    }

    @Override
    public void onUpdate() {
        this.prevPitch = this.pitch;
        this.prevYaw = this.yaw;
        this.prevRoll = this.angleRoll;

        super.onUpdate();

        if (!this.onGround) {
            this.modelLight = packedLightAt(this.level, this.x, this.y, this.z);
        } else {
            this.anglePitchRate *= this.angleRateDampingOnGround;
            this.angleRollRate *= this.angleRateDampingOnGround;
        }

        anglePitch = (anglePitch + anglePitchRate) % 360.0f;
        anglePitchRate *= anglePitchRateDamping;

        angleRoll = (angleRoll + angleRollRate) % 360.0f;
        angleRollRate *= angleRollRateDamping;

        yaw = angle;
        pitch = anglePitch;
    }

    @Override
    public void doRender(MultiBufferSource.BufferSource buffers, Camera camera, float partialTick, BillboardAxes axes, PoseStack poseStack) {
        float progress = ((float) this.age + partialTick) / (float) this.maxAge;
        float sinkProgress = 0;
        if (startSinkTime > 0.0f && progress > startSinkTime) {
            sinkProgress = (progress - startSinkTime) / (1f - startSinkTime);
        }

        RenderType renderType = RenderType.entityCutoutNoCull(this.texture);
        VertexConsumer vertexConsumer = buffers.getBuffer(renderType);

        poseStack.pushPose();

        Vec3 camPos = camera.getPosition();
        float fPosX = (float) (Mth.lerp((double) partialTick, this.prevPosX, this.x) - camPos.x);
        float fPosY = (float) (Mth.lerp((double) partialTick, this.prevPosY, this.y) - camPos.y);
        float fPosZ = (float) (Mth.lerp((double) partialTick, this.prevPosZ, this.z) - camPos.z);

        poseStack.translate(fPosX, fPosY - sinkProgress * sinkDistance, fPosZ);

        float offsetX = (-(this.modelMinX + (this.modelMaxX - this.modelMinX) * 0.5f)) * MODEL_SCALE;
        float offsetY = (-(this.modelMinY + (this.modelMaxY - this.modelMinY) * 0.5f)) * MODEL_SCALE;
        float offsetZ = (-(this.modelMinZ + (this.modelMaxZ - this.modelMinZ) * 0.5f)) * MODEL_SCALE;

        float renderYaw = Mth.lerp(partialTick, this.prevYaw, this.yaw);
        float renderPitch = Mth.lerp(partialTick, this.prevPitch, this.pitch);
        float renderRoll = Mth.lerp(partialTick, this.prevRoll, this.angleRoll);

        TGMatrixOps.rotate(poseStack, renderPitch, 1f, 0f, 0f);
        TGMatrixOps.rotate(poseStack, renderYaw, 0f, 1f, 0f);
        TGMatrixOps.rotate(poseStack, renderRoll, 0f, 0f, 1f);

        poseStack.translate(offsetX, offsetY, offsetZ);

        model.render(poseStack, vertexConsumer, this.modelLight, OverlayTexture.NO_OVERLAY);

        poseStack.popPose();
    }
}
