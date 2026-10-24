package techguns.client.particle;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.phys.Vec3;

/** A Techguns particle or particle system, ticked and rendered by the {@link TGParticleManager}. */
public interface ITGParticle {

    /**
     * Camera-aligned billboard axes (Techguns 2 / vanilla 1.12 {@code ActiveRenderInfo}): the camera's right
     * vector is (rotX, 0, rotYZ) and its up vector (rotXY, rotZ, rotXZ).
     */
    record BillboardAxes(float rotX, float rotZ, float rotYZ, float rotXY, float rotXZ) {
    }

    Vec3 getPos();

    boolean shouldRemove();

    void updateTick();

    /**
     * Draws the particle. Positions are camera-relative (the pose stack is the level's identity pose stack, the
     * camera rotation is part of the model-view matrix).
     */
    void doRender(MultiBufferSource.BufferSource buffers, Camera camera, float partialTick, BillboardAxes axes, PoseStack poseStack);

    /** Particles which are not depth sorted (streak segments depend on their draw order). */
    default boolean doNotSort() {
        return false;
    }

    double getDepth();

    void setDepth(double depth);
}
