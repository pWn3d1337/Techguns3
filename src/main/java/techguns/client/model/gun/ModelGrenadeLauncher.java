package techguns.client.model.gun;

import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import techguns.TGIds;
import techguns.client.model.ObjMesh;
import techguns.client.model.TGModel;
import techguns.client.render.TGMatrixOps;

/**
 * Grenade launcher (Fabric {@code ModelGrenadeLauncher}): two OBJ meshes, the frame (part 0) and the cylinder (part
 * 1), which turns by 60 degrees (one chamber) per shot. OBJ models have no GeckoLib version.
 */
public class ModelGrenadeLauncher extends TGModel {
    private static final ObjMesh FRAME = new ObjMesh(TGIds.id("models/obj/grenadelauncher.obj"), true);
    private static final ObjMesh CYLINDER = new ObjMesh(TGIds.id("models/obj/grenadelauncher_1.obj"), true);

    public ModelGrenadeLauncher() {
        super(RenderType::entitySolid);
    }

    @Override
    public void render(@Nullable LivingEntity entity, PoseStack poseStack, VertexConsumer vertices, int ammoLeft, float reloadProgress,
                       ItemDisplayContext transformType, int part, float fireProgress, float chargeProgress, int light, int overlay) {
        if (part == 0) {
            FRAME.render(poseStack, vertices, light, overlay);
        } else {
            poseStack.pushPose();
            poseStack.translate(0, -2f, 0);
            TGMatrixOps.rotate(poseStack, 60.0f * fireProgress, 1f, 0f, 0f);
            poseStack.translate(0, 2f, 0);
            CYLINDER.render(poseStack, vertices, light, overlay);
            poseStack.popPose();
        }
    }
}
