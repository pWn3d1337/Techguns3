package techguns.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import techguns.TGIds;
import techguns.client.render.TGRenderTypes;
import techguns.entity.projectile.BioGunProjectile;

/** Bio gun glob: a glowing camera-facing blob sprite that grows with the ammo consumed (Fabric {@code RenderBioGunProjectile}). */
public class BioGunProjectileRenderer extends RenderLateEntityRenderer<BioGunProjectile> {
    private static final ResourceLocation TEXTURE = TGIds.id("textures/entity/bioblob.png");
    private static final float BASE_SIZE = 0.1f;
    private static final int FULL_BRIGHT = 0xF000F0;

    public BioGunProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(BioGunProjectile entity) {
        return TEXTURE;
    }

    @Override
    protected void renderLate(BioGunProjectile entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffers,
                              int packedLight) {
        float scale = switch (entity.blobLevel()) {
            case 1 -> 1.25f;
            case 2 -> 2.5f;
            case 3 -> 4.0f;
            default -> 1.0f;
        };
        ProjectileSprite.draw(poseStack, buffers.getBuffer(TGRenderTypes.fxAdditive(TEXTURE)), BASE_SIZE * scale, FULL_BRIGHT);
    }
}
