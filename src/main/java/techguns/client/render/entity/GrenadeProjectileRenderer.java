package techguns.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import techguns.TGIds;
import techguns.client.model.ObjMesh;
import techguns.client.model.TGModel;
import techguns.client.model.gun.ModelFragGrenade;
import techguns.client.model.gun.ModelStielgranate;
import techguns.client.render.item.RenderItemBase;
import techguns.client.render.TGMatrixOps;
import techguns.entity.projectile.GrenadeProjectile;

/**
 * Spinning grenades: the 40 mm launcher grenade (OBJ mesh, Fabric {@code RenderGrenadeProjectile}) and the hand
 * grenades (box models, Techguns 2 {@code RenderGrenadeProjectile} / {@code RenderFragGrenadeProjectile}).
 */
public class GrenadeProjectileRenderer extends EntityRenderer<GrenadeProjectile> {
    private static final ResourceLocation TEXTURE_40MM = TGIds.id("textures/entity/launchergrenade.png");
    private static final ResourceLocation TEXTURE_STIELGRANATE = TGIds.id("textures/guns/stielgranate.png");
    private static final ResourceLocation TEXTURE_FRAG = TGIds.id("textures/guns/frag_grenade_texture.png");

    private static final ObjMesh MESH_40MM = new ObjMesh(TGIds.id("models/obj/grenade40mm.obj"), false);
    private final TGModel stielgranate = new ModelStielgranate();
    private final TGModel frag = new ModelFragGrenade(false);

    public GrenadeProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(GrenadeProjectile entity) {
        return switch (entity.grenadeType()) {
            case STIELGRANATE -> TEXTURE_STIELGRANATE;
            case FRAG -> TEXTURE_FRAG;
            default -> TEXTURE_40MM;
        };
    }

    @Override
    public void render(GrenadeProjectile entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffers,
                       int packedLight) {
        poseStack.pushPose();
        TGMatrixOps.rotate(poseStack, Mth.lerp(partialTick, entity.yRotO, entity.getYRot()) - 90.0f, 0f, 1f, 0f);
        TGMatrixOps.rotate(poseStack, Mth.lerp(partialTick, entity.xRotO, entity.getXRot()), 0f, 0f, 1f);
        float spin = ((entity.tickCount % 20) + partialTick) / 20.0f;
        TGMatrixOps.rotate(poseStack, -360.0f * spin, 0f, 0f, 1f);

        if (entity.grenadeType() == GrenadeProjectile.Type.LAUNCHER_40MM) {
            MESH_40MM.render(poseStack, buffers.getBuffer(RenderType.entitySolid(TEXTURE_40MM)), packedLight, OverlayTexture.NO_OVERLAY);
        } else {
            poseStack.scale(0.8f, 0.8f, 0.8f);
            TGMatrixOps.rotate(poseStack, -180.0f, 1f, 0f, 0f);
            TGMatrixOps.rotate(poseStack, 180.0f, 0f, 1f, 0f);
            poseStack.translate(-RenderItemBase.SCALE * 0.5f, 0.83f, -RenderItemBase.SCALE);
            TGModel model = entity.grenadeType() == GrenadeProjectile.Type.FRAG ? frag : stielgranate;
            model.render(null, poseStack, buffers.getBuffer(model.renderType(getTextureLocation(entity))), 0, 0f, ItemDisplayContext.NONE,
                    0, 0f, 0f, packedLight, OverlayTexture.NO_OVERLAY);
        }
        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, buffers, packedLight);
    }
}
