package techguns.client.model.shield;

import java.util.function.Supplier;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;

/**
 * Techguns 2 shield models (ModelRiotShield, ModelBallisticShield, ModelAdvancedShield) as vanilla models; the whole
 * model is drawn at once like the 1.12 render(..., 0.0625F).
 */
// MIGRATION(26.x): Model / ModelPart / renderToBuffer signatures change (render states) in 1.21.2+.
public final class TGShieldModel extends Model {
    private static final float STEP = 0.39269908169872414F;

    private final ModelPart root;

    private TGShieldModel(ModelPart root) {
        super(RenderType::entityCutoutNoCull);
        this.root = root;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        root.render(poseStack, buffer, packedLight, packedOverlay, color);
    }

    /** Bakes on first use (client models are only available after resource loading). */
    public static Supplier<TGShieldModel> lazy(Supplier<LayerDefinition> definition) {
        return new Supplier<>() {
            private TGShieldModel baked;

            @Override
            public TGShieldModel get() {
                if (baked == null) {
                    baked = new TGShieldModel(definition.get().bakeRoot());
                }
                return baked;
            }
        };
    }

    private static void box(PartDefinition root, String name, int u, int v, boolean mirror, float x, float y, float z,
                            float ox, float oy, float oz, int w, int h, int d, float rx, float ry, float rz) {
        CubeListBuilder b = CubeListBuilder.create().texOffs(u, v);
        if (mirror) {
            b = b.mirror();
        }
        root.addOrReplaceChild(name, b.addBox(ox, oy, oz, w, h, d, CubeDeformation.NONE),
                PartPose.offsetAndRotation(x, y, z, rx, ry, rz));
    }

    public static LayerDefinition riot() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition r = mesh.getRoot();
        box(r, "bottom_border", 0, 22, false, -5, 10, -1.5f, 0, 0, 0, 10, 1, 1, 0, 0, 0);
        box(r, "side_border_1", 27, 0, false, -6, -10.5f, -1.5f, 0, 0, 0, 1, 21, 1, 0, 0, 0);
        box(r, "plate", 0, 0, false, -5, -10, -2, 0, 0, 0, 10, 20, 1, 0, 0, 0);
        box(r, "handle", 16, 20, false, -1, -3, -1, 0, 0, 0, 2, 6, 6, 0, 0, 0);
        box(r, "side_border_0", 27, 0, false, 5, -10.5f, -1.5f, 0, 0, 0, 1, 21, 1, 0, 0, 0);
        box(r, "top_border", 0, 22, false, -5, -11, -1.5f, 0, 0, 0, 10, 1, 1, 0, 0, 0);
        return LayerDefinition.create(mesh, 32, 32);
    }

    public static LayerDefinition ballistic() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition r = mesh.getRoot();
        box(r, "bottom_border", 0, 15, false, -5, 10, -1.5f, 0, 0, 0, 10, 1, 1, 0, 0, 0);
        box(r, "side_border_1", 27, 0, false, -6, -4.5f, -1.5f, 0, 0, 0, 1, 15, 1, 0, 0, 0);
        box(r, "side_border_3", 23, 0, false, -5, -10.5f, -1.5f, 0, 0, 0, 1, 7, 1, 0, 0, 0);
        box(r, "side_border_0", 27, 0, false, 5, -4.5f, -1.5f, 0, 0, 0, 1, 15, 1, 0, 0, 0);
        box(r, "glass", 0, 17, false, -4, -8, -1.8f, 0, 0, 0, 8, 4, 1, 0, 0, 0);
        box(r, "handle", 16, 20, false, -1, -3, -1, 0, 0, 0, 2, 6, 6, 0, 0, 0);
        box(r, "top_border", 0, 22, false, -4, -11, -1.5f, 0, 0, 0, 8, 3, 1, 0, 0, 0);
        box(r, "side_border_2", 23, 0, false, 4, -10.5f, -1.5f, 0, 0, 0, 1, 7, 1, 0, 0, 0);
        box(r, "plate", 0, 0, false, -5, -4, -2, 0, 0, 0, 10, 14, 1, 0, 0, 0);
        return LayerDefinition.create(mesh, 32, 32);
    }

    public static LayerDefinition advanced() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition r = mesh.getRoot();
        box(r, "side_border_0", 20, 0, false, 4, -4, -2, 0, 0, 0, 3, 15, 1, 0, -STEP, 0);
        box(r, "bottom_border", 0, 16, false, -4, 10, -2, 0, 0, 0, 8, 2, 1, STEP, 0, 0);
        box(r, "glass", 26, 17, false, -4, -10.5f, -1.8f, 0, 0, 0, 8, 7, 1, 0, 0, 0);
        box(r, "side_border_3", 29, 0, false, 4, -11, -2, 0, 0, 0, 2, 7, 1, 0, -STEP, 0);
        box(r, "handle", 14, 20, false, -1, -3, -1, 0, 0, 0, 2, 6, 6, 0, 0, 0);
        box(r, "side_border_1", 20, 0, true, -4, -4, -2, -3, 0, 0, 3, 15, 1, 0, STEP, 0);
        box(r, "plate", 0, 0, false, -4, -4, -2, 0, 0, 0, 8, 14, 1, 0, 0, 0);
        box(r, "top_border", 0, 20, false, -4, -10.5f, -2.1f, 0, -1, 0, 8, 1, 1, -STEP, 0, 0);
        box(r, "side_border_2", 29, 0, true, -4, -11, -2, -2, 0, 0, 2, 7, 1, 0, STEP, 0);
        return LayerDefinition.create(mesh, 64, 32);
    }

    /** Techguns 2 {@code ModelHeavyShield} (unused in Techguns 2, no item there). */
    public static LayerDefinition heavy() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition r = mesh.getRoot();
        box(r, "bottom_border", 0, 28, false, -5, 10, -2.5f, 0, 0, 0, 10, 2, 2, 0, 0, 0);
        box(r, "side_border_0", 23, 0, false, 5, -11.5f, -2.5f, 0, 0, 0, 2, 23, 2, 0, 0, 0);
        box(r, "plate_upper_s01", 0, 21, false, 3, -7, -2, 0, 0, 0, 2, 3, 1, 0, 0, 0);
        box(r, "side_border_1", 23, 0, false, -7, -11.5f, -2.5f, 0, 0, 0, 2, 23, 2, 0, 0, 0);
        box(r, "plate_upper", 0, 16, false, -5, -10, -2, 0, 0, 0, 10, 3, 1, 0, 0, 0);
        box(r, "plate_upper_s01_1", 0, 21, false, -5, -7, -2, 0, 0, 0, 2, 3, 1, 0, 0, 0);
        box(r, "plate_armor", 32, 0, false, -5, -2, -2.3f, 0, 0, 0, 10, 2, 1, 0, 0, 0);
        box(r, "plate", 0, 0, false, -5, -4, -2, 0, 0, 0, 10, 14, 1, 0, 0, 0);
        box(r, "top_border", 0, 28, false, -5, -12, -2.5f, 0, 0, 0, 10, 2, 2, 0, 0, 0);
        box(r, "glass", 8, 21, false, -3, -7, -2, 0, 0, 0, 6, 3, 1, 0, 0, 0);
        box(r, "armor_box", 32, 4, false, 4.5f, 3, -3, 0, 0, 0, 3, 6, 3, 0, 0, 0);
        box(r, "armor_box_4", 32, 4, true, -7.5f, 3, -3, 0, 0, 0, 3, 6, 3, 0, 0, 0);
        box(r, "plate_armor_2", 32, 0, false, -5, 6, -2.3f, 0, 0, 0, 10, 2, 1, 0, 0, 0);
        box(r, "armor_box_3", 32, 4, true, -7.5f, -9, -3, 0, 0, 0, 3, 6, 3, 0, 0, 0);
        box(r, "handle", 26, 20, false, -1, -3, -1, 0, 0, 0, 2, 6, 6, 0, 0, 0);
        box(r, "plate_armor_3", 32, 0, false, -5, 2, -2.3f, 0, 0, 0, 10, 2, 1, 0, 0, 0);
        box(r, "armor_box_2", 32, 4, false, 4.5f, -9, -3, 0, 0, 0, 3, 6, 3, 0, 0, 0);
        return LayerDefinition.create(mesh, 64, 32);
    }
}
