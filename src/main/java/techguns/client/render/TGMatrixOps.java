package techguns.client.render;

import org.joml.AxisAngle4f;
import org.joml.Quaternionf;

import com.mojang.blaze3d.vertex.PoseStack;

/** Pose stack helpers shared by the Techguns item renderers and models. */
public final class TGMatrixOps {
    private static final float DEG2RAD = (float) (Math.PI / 180.0);

    private TGMatrixOps() {
    }

    /** Rotates by {@code deg} degrees around the axis (x, y, z). */
    public static void rotate(PoseStack poseStack, float deg, float x, float y, float z) {
        poseStack.mulPose(new Quaternionf(new AxisAngle4f(DEG2RAD * deg, x, y, z)));
    }
}
