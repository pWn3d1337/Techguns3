package techguns.client.render.entity;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

/**
 * Renderer that does not draw in the normal entity pass: {@link #render} only queues the entity, and the queue is
 * drawn at the end of level rendering (together with the Techguns particles) by {@link #renderQueued}, so that
 * additive and alpha blended entities are blended over everything else (Techguns 2 {@code RenderLateEntityRenderer}).
 *
 * @param <T> entity type
 */
// MIGRATION(26.x): entity render states / submit nodes replace this queue.
public abstract class RenderLateEntityRenderer<T extends Entity> extends EntityRenderer<T> {
    /** Only touched on the render thread. */
    private static final List<Entity> QUEUE = new ArrayList<>();

    protected RenderLateEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    /** Dev testing only (/tgdebug fxstats): entities queued and drawn so far. */
    private static long debugQueued, debugDrawn;

    @Override
    public final void render(T entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int packedLight) {
        QUEUE.add(entity);
        debugQueued++;
    }

    public static String debugStats() {
        return "lateQueued=" + debugQueued + " lateDrawn=" + debugDrawn;
    }

    /**
     * Draws the entity in the late pass. The pose stack is translated to the entity's interpolated position
     * (relative to the camera); the buffer source is flushed after each entity.
     */
    protected abstract void renderLate(T entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int packedLight);

    /** Draws and clears the queued entities. Called once per frame from the level render stage hook. */
    @SuppressWarnings("unchecked")
    public static void renderQueued(MultiBufferSource.BufferSource buffers, Camera camera, float partialTick, PoseStack poseStack) {
        if (QUEUE.isEmpty()) {
            return;
        }
        EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        Vec3 camPos = camera.getPosition();
        for (Entity entity : QUEUE) {
            if (!(dispatcher.getRenderer(entity) instanceof RenderLateEntityRenderer<?> raw)) {
                continue;
            }
            RenderLateEntityRenderer<Entity> renderer = (RenderLateEntityRenderer<Entity>) raw;
            float yaw = Mth.lerp(partialTick, entity.yRotO, entity.getYRot());
            double x = Mth.lerp((double) partialTick, entity.xOld, entity.getX()) - camPos.x;
            double y = Mth.lerp((double) partialTick, entity.yOld, entity.getY()) - camPos.y;
            double z = Mth.lerp((double) partialTick, entity.zOld, entity.getZ()) - camPos.z;

            Vec3 offset = renderer.getRenderOffset(entity, partialTick);
            poseStack.pushPose();
            poseStack.translate(x + offset.x, y + offset.y, z + offset.z);
            renderer.renderLate(entity, yaw, partialTick, poseStack, buffers, renderer.getPackedLightCoords(entity, partialTick));
            debugDrawn++;
            poseStack.popPose();
            buffers.endLastBatch();
        }
        QUEUE.clear();
    }

    /** Drops queued entities without drawing them (when no level render pass happens). */
    public static void clearQueue() {
        QUEUE.clear();
    }
}
