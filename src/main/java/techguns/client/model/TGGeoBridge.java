package techguns.client.model;

import java.util.function.Consumer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;

/**
 * Draws Bedrock geo models. Techguns code only talks to this interface; the GeckoLib implementation lives in
 * {@code techguns.client.render.geo} so the 26.x jump (GeckoLib 5 render states) touches one class.
 */
// MIGRATION(26.x): keep this interface, rewrite the GeckoLib implementation for GeckoLib 5.
public interface TGGeoBridge {
    /**
     * Draws geo model {@code id} in Java model space (the gun pipeline's pose stack) into {@code buffer}, whose render
     * type the gun renderer chose for this part.
     *
     * @param animationKey identifies one animated copy (holder and hand); -1 = static (GUI, ground, frames)
     * @param bones        code-driven bone changes, applied after the keyframe animations
     * @return false if the model isn't available, so the caller draws the Java model instead
     */
    boolean render(String id, long animationKey, PoseStack poseStack, MultiBufferSource buffers, VertexConsumer buffer,
                   int light, int overlay, float reloadProgress, float fireProgress, Consumer<GeoBones> bones);

    static TGGeoBridge get() {
        return Holder.bridge;
    }

    static void install(TGGeoBridge bridge) {
        Holder.bridge = bridge;
    }

    /** Dev testing (/tgdebug geo): switches between geo and Java models at runtime. */
    static boolean enabled() {
        return Holder.bridge != null && Holder.enabled;
    }

    static void setEnabled(boolean enabled) {
        Holder.enabled = enabled;
    }

    final class Holder {
        private static TGGeoBridge bridge;
        private static boolean enabled = true;

        private Holder() {
        }
    }
}
