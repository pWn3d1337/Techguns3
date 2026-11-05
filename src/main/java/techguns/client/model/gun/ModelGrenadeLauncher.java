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
 * 1), which turns by 60 degrees (one chamber) per shot. Geo cubes can't express the meshes (hexagonal and pentagonal
 * end faces), so {@code grenade_launcher.geo.json} only has mesh bones ({@code mesh_frame}, {@code mesh_drum} under the
 * animated {@code drum}) and {@link #renderMesh} draws the OBJ meshes there; {@link #render} is the Java fallback.
 */
public class ModelGrenadeLauncher extends TGModel {
    private static final ObjMesh FRAME = new ObjMesh(TGIds.id("models/obj/grenadelauncher.obj"), true);
    private static final ObjMesh CYLINDER = new ObjMesh(TGIds.id("models/obj/grenadelauncher_1.obj"), true);

    public ModelGrenadeLauncher() {
        super(RenderType::entitySolid);
    }

    /** The geo frame points along -Z: the OBJ mesh space is the geo bone space turned by Ry(90). */
    @Override
    public void renderMesh(String bone, PoseStack poseStack, VertexConsumer vertices, int light, int overlay) {
        ObjMesh mesh = switch (bone) {
            case "mesh_frame" -> FRAME;
            case "mesh_drum" -> CYLINDER;
            default -> null;
        };
        if (mesh != null) {
            TGMatrixOps.rotate(poseStack, 90.0f, 0f, 1.0f, 0f);
            mesh.render(poseStack, vertices, light, overlay);
        }
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
