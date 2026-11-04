package techguns.client.model;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import org.joml.Matrix4f;

/**
 * Draws Bedrock geo models. Techguns code only talks to this interface; the GeckoLib implementation lives in
 * {@code techguns.client.render.geo} so the 26.x jump (GeckoLib 5 render states) touches one class.
 */
// MIGRATION(26.x): keep this interface, rewrite the GeckoLib implementation for GeckoLib 5.
public interface TGGeoBridge {
    /** Marker bones: empty bones whose position the gun renderer reads (muzzle flash, charge and idle effects). */
    String MUZZLE = "muzzle";
    /** Suffix of a marker's rest pose: the whole-gun and part motion left out, only aiming applied. */
    String REST = "#rest";

    static boolean isMarker(String bone) {
        return bone.startsWith(MUZZLE) || bone.startsWith("charge") || bone.startsWith("idle_fx");
    }

    /**
     * Draws geo model {@code id} in Java model space (the gun pipeline's pose stack) into {@code buffer}, whose render
     * type the gun renderer chose for this part.
     *
     * @param anim    timer progress the keyframe animations are sampled at
     * @param bones   code-driven bone changes, applied after the keyframe animations
     * @param markers receives the pose of each marker bone's pivot ({@link #isMarker}), in the pose stack's root space, and
     *                its rest pose under the name plus {@link #REST}
     * @return false if the model isn't available, so the caller draws the Java model instead
     */
    boolean render(String id, PoseStack poseStack, MultiBufferSource buffers, VertexConsumer buffer, int light, int overlay,
                   GeoAnimInput anim, Consumer<GeoBones> bones, BiConsumer<String, Matrix4f> markers);

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
