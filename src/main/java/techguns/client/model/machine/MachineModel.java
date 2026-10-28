package techguns.client.model.machine;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

/** Techguns 2 {@code ModelMachine}: a machine model animated by the job progress (0..1). */
public interface MachineModel {
    void render(PoseStack poseStack, VertexConsumer consumer, int light, int overlay, float progress);

    /** Translucent parts (glass), drawn after {@link #render} with a translucent render type. */
    default void renderTranslucent(PoseStack poseStack, VertexConsumer consumer, int light, int overlay, float progress) {
    }

    /**
     * Whether {@link #renderTranslucent} draws anything. Asking for the translucent buffer ends the shared solid batch,
     * so models without glass skip it and nearby machines share one draw call.
     */
    default boolean hasTranslucent() {
        return false;
    }
}
