package techguns.client.model;

import java.util.function.Function;

import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;

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

    /** Render type for one part of this model. */
    public RenderType getRenderType(ResourceLocation texture, int part) {
        return this.renderType(texture);
    }

    /** Render type for one part of this model, for models whose texture depends on the loaded ammo variant. */
    public RenderType getRenderType(ResourceLocation texture, int part, String ammoVariant) {
        return this.getRenderType(texture, part);
    }

    protected void setRotation(TGModelPart model, float x, float y, float z) {
        model.pitch = x;
        model.yaw = y;
        model.roll = z;
    }
}
