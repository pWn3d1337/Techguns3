package techguns.client.model;

import java.util.Map;
import java.util.function.Function;

import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import org.joml.Matrix4f;

/**
 * Base class of Techguns 2 style multi-part item models (guns, magazines, rockets).
 */
// MIGRATION(26.x): vanilla Model/RenderType factories change shape; the vanilla Model base is only used for its render type factory.
public abstract class TGModel extends Model {

    protected int textureWidth = 64;
    protected int textureHeight = 64;

    protected static final double scale = 0.0625;

    /** Packed lightmap coordinates (15, 15) for glowing parts. */
    protected static final int bright_light = 15728880;

    public TGModel(Function<ResourceLocation, RenderType> layerFactory) {
        super(layerFactory);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int light, int overlay, int color) {
        render(null, poseStack, buffer, 0, 0f, ItemDisplayContext.NONE, 0, 0f, 0f, light, overlay);
    }

    public abstract void render(@Nullable LivingEntity entity, PoseStack poseStack, VertexConsumer vertices, int ammoLeft,
                                float reloadProgress, ItemDisplayContext context, int part, float fireProgress, float chargeProgress, int light, int overlay);

    /**
     * Code-driven movement of a whole render part (bolts, slides, spinning barrels), applied to the pose stack before
     * the part is drawn. The Java render and the geo render ({@link GeoGunModel}) both use it.
     */
    public void transformPart(PoseStack poseStack, int part, int ammoLeft, float reloadProgress, float fireProgress) {
    }

    /**
     * Like {@link #transformPart(PoseStack, int, int, float, float)}, for parts that depend on the holder or the display
     * context (static copies in the GUI, per-player state). Both renders call this one.
     */
    public void transformPart(PoseStack poseStack, @Nullable LivingEntity entity, ItemDisplayContext context, int part, int ammoLeft,
                              float reloadProgress, float fireProgress) {
        this.transformPart(poseStack, part, ammoLeft, reloadProgress, fireProgress);
    }

    /**
     * Draws the mesh of a geo model's mesh bone ({@link TGGeoBridge#MESH}); {@code poseStack} is at that bone's space
     * (GeckoLib space, y up, blocks). Models whose shape geo cubes can't express (OBJ meshes) draw it here.
     */
    public void renderMesh(String bone, PoseStack poseStack, VertexConsumer vertices, int light, int overlay) {
    }

    /** Whether a render part is drawn for this holder (e.g. the grappling hook while it is launched). */
    public boolean isPartVisible(@Nullable LivingEntity entity, int part) {
        return true;
    }

    /**
     * Code-driven bone changes for the geo version of this model. The static part scales of the Java models live in the
     * geo models' {@code hold} animations.
     */
    public void animateGeoBones(GeoBones bones, int part, int ammoLeft, float reloadProgress, float fireProgress,
                                ItemDisplayContext context) {
    }

    /**
     * Same as the variant without charge progress, for charge guns whose geo bones follow the charge
     * (the charge progress is only known to the renderer, not to the plain hook).
     */
    public void animateGeoBones(GeoBones bones, int part, int ammoLeft, float reloadProgress, float fireProgress, float chargeProgress,
                                ItemDisplayContext context) {
        this.animateGeoBones(bones, part, ammoLeft, reloadProgress, fireProgress, context);
    }

    /**
     * The animation input for the next in-hand draws, set by the gun renderer around its parts; null = derive it from the
     * render arguments. Only geo models animate.
     */
    public void setAnimInput(@Nullable GeoAnimInput input) {
    }

    /**
     * Texture of the next part draws, set by the gun renderer around each part: a geo model draws its extra render
     * passes ({@link TGGeoBridge#TRANSLUCENT}, {@link TGGeoBridge#CUTOUT}) with it. Only geo models use it.
     */
    public void setPartTexture(@Nullable ResourceLocation texture) {
    }

    /**
     * Pose of marker bone {@code name} ({@link TGGeoBridge#isMarker}) in the last draw, in the pose stack's root space;
     * null if the model has no such bone (Java models have none).
     */
    @Nullable
    public Matrix4f marker(String name) {
        return null;
    }

    /** Java model of render part {@code part} (> 0) when it is a separate nested model; this model otherwise. */
    public TGModel partModel(int part) {
        return this;
    }

    /** Render type for one part of this model. */
    public RenderType getRenderType(ResourceLocation texture, int part) {
        return this.renderType(texture);
    }

    /** Render type for one part of this model, for models whose texture depends on the loaded ammo variant. */
    /** Texture of render part {@code part} per loaded ammo variant, if it depends on the ammo (datagen); none by default. */
    public Map<String, ResourceLocation> ammoTextures(int part) {
        return Map.of();
    }

    public RenderType getRenderType(ResourceLocation texture, int part, String ammoVariant) {
        return this.getRenderType(texture, part);
    }

    protected void setRotation(TGModelPart model, float x, float y, float z) {
        model.pitch = x;
        model.yaw = y;
        model.roll = z;
    }
}
