package techguns.client.model;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import javax.annotation.Nullable;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
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
    /** Prefix of the pose of a geo file's model origin (plus the geo id), reported with the markers; for conversion tools. */
    String MODEL = "#model:";
    /**
     * Bone name suffixes of the extra render passes: such a bone and its children are drawn after the rest of the model,
     * with the part's texture as {@code entityTranslucent} / {@code entityCutout} (the NDR's glow rings, the GML's holo
     * sight).
     */
    String TRANSLUCENT = "_translucent";
    String CUTOUT = "_cutout";
    /**
     * Name prefix of mesh bones: empty bones (pivot 0) at whose pose the gun model draws a mesh geo cubes can't express
     * ({@link TGModel#renderMesh}, the grenade launcher's OBJ meshes), so the mesh follows the bone's animation.
     */
    String MESH = "mesh_";

    static boolean isMarker(String bone) {
        return bone.startsWith(MUZZLE) || bone.startsWith("charge") || bone.startsWith("idle_fx") || bone.startsWith(MESH);
    }

    /**
     * Draws geo model {@code id} in Java model space (the gun pipeline's pose stack) into {@code buffer}, whose render
     * type the gun renderer chose for this part.
     *
     * @param id      geo id: {@code <path>} in the techguns namespace or {@code <namespace>:<path>}
     * @param texture the part's texture for the {@link #TRANSLUCENT} and {@link #CUTOUT} bones; null = those bones are not
     *                drawn
     * @param boneTypes bones (with their children) left out of the main draw and drawn after it with their own render type,
     *                  which carries their texture (the display file's {@code bone_textures})
     * @param anim    timer progress the keyframe animations are sampled at
     * @param bones   code-driven bone changes, applied after the keyframe animations
     * @param markers receives the pose of each marker bone's pivot ({@link #isMarker}), in the pose stack's root space, and
     *                its rest pose under the name plus {@link #REST}; also the model origin under {@link #MODEL} plus {@code id}
     * @return false if the model isn't available, so the caller draws the Java model instead
     */
    boolean render(String id, PoseStack poseStack, MultiBufferSource buffers, VertexConsumer buffer, @Nullable ResourceLocation texture,
                   Map<String, RenderType> boneTypes, int light, int overlay, GeoAnimInput anim, Consumer<GeoBones> bones, BiConsumer<String, Matrix4f> markers);

    /**
     * Moves {@code poseStack} by the {@code root} bone of animation {@code animation} (the file of geo id {@code gun},
     * {@code animations/item/<gun>.animation.json}, else {@code common.animation.json}) at {@code progress}: positions
     * times {@code move} in blocks, rotations times {@code turn}; mirrored for the left hand. Used where no geo model is
     * drawn (the scope overlay's recoil). Does nothing if the animation isn't loaded.
     */
    void applyRootMotion(PoseStack poseStack, String gun, String animation, float progress, float move, float turn, boolean mirror);

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
