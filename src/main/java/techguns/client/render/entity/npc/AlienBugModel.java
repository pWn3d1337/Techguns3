package techguns.client.render.entity.npc;

import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import techguns.entity.npc.AlienBug;
import techguns.entity.npc.TGNpc;

/**
 * Techguns 2 {@code ModelAlienBug} (Tabula): segmented body, a head with two jaws and a crest, four two-part legs that
 * swing while walking; the jaws breathe when idle and snap open on a bite.
 */
// MIGRATION(26.x): entity models take render states in 1.21.2+.
public class AlienBugModel extends HierarchicalModel<TGNpc> {
    private static final float LEG_YAW = 2.792526803190927F;
    private static final float LEG_YAW_INNER = 0.3490658503988659F;
    private static final float LEG_ROLL = 0.5235987755982988F;
    private static final float JAW_UPPER = -0.18203784098300857F;
    private static final float JAW_LOWER = 0.4553564018453205F;

    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart jawUpper;
    private final ModelPart jawLower;
    /** Techguns 2 L1_A, L1_A_1, L1_A_2, L1_A_3. */
    private final ModelPart leg0;
    private final ModelPart leg1;
    private final ModelPart leg2;
    private final ModelPart leg3;

    public AlienBugModel(ModelPart root) {
        this.root = root;
        this.head = root.getChild("head");
        this.jawUpper = head.getChild("jaw_upper");
        this.jawLower = head.getChild("jaw_lower");
        this.leg0 = root.getChild("leg0");
        this.leg1 = root.getChild("leg1");
        this.leg2 = root.getChild("leg2");
        this.leg3 = root.getChild("leg3");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("b1", CubeListBuilder.create().texOffs(0, 33).addBox(0F, 0F, 0F, 5F, 5F, 9F), PartPose.offset(-2.5F, 14F, -2F));
        root.addOrReplaceChild("b2", CubeListBuilder.create().texOffs(23, 33).addBox(0F, 0F, 0F, 6F, 4F, 4F),
                PartPose.offsetAndRotation(-3F, 13F, 6F, 0.5918411493512771F, 0F, 0F));
        root.addOrReplaceChild("b3", CubeListBuilder.create().texOffs(43, 31).addBox(0F, 0F, 0F, 4F, 3F, 4F),
                PartPose.offsetAndRotation(-2F, 11F, 10F, 0.22759093446006054F, 0F, 0F));
        root.addOrReplaceChild("b4", CubeListBuilder.create().texOffs(56, 30).addBox(0F, 0F, 0F, 2F, 2F, 2F), PartPose.offset(-1F, 10.5F, 14.2F));

        PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(20, 9).addBox(-2.5F, -2.5F, -3F, 5F, 5F, 6F),
                PartPose.offsetAndRotation(0F, 11.5F, -1F, 0.136659280431156F, 0F, 0F));
        head.addOrReplaceChild("h2", CubeListBuilder.create().texOffs(36, 18).addBox(0F, 0F, -7F, 6F, 3F, 8F),
                PartPose.offsetAndRotation(-3F, -6.5F, 3.5F, 0.3490658503988659F, 0F, 0F));
        head.addOrReplaceChild("h3", CubeListBuilder.create().texOffs(22, 0).addBox(-4F, 1F, -7F, 14F, 0F, 14F),
                PartPose.offsetAndRotation(-3F, -6.5F, 3.5F, 0.3490658503988659F, 0F, 0F));
        head.addOrReplaceChild("jaw_upper", CubeListBuilder.create().texOffs(2, 15).addBox(0F, -2.5F, -10F, 4F, 4F, 10F),
                PartPose.offsetAndRotation(-2F, -1F, -2F, JAW_UPPER, 0F, 0F));
        head.addOrReplaceChild("jaw_lower", CubeListBuilder.create().texOffs(21, 20).addBox(0F, 0F, -9F, 3F, 4F, 9F),
                PartPose.offsetAndRotation(-1.5F, -1.5F, -2.5F, JAW_LOWER, 0F, 0F));

        leg(root, "leg0", -1.5F, 0F, LEG_YAW, LEG_ROLL);
        leg(root, "leg1", 1.5F, 4F, -LEG_YAW_INNER, -LEG_ROLL);
        leg(root, "leg2", -1.5F, 4F, -LEG_YAW, LEG_ROLL);
        leg(root, "leg3", 1.5F, 0F, LEG_YAW_INNER, -LEG_ROLL);
        return LayerDefinition.create(mesh, 64, 64);
    }

    private static void leg(PartDefinition root, String name, float x, float z, float yaw, float roll) {
        PartDefinition upper = root.addOrReplaceChild(name, CubeListBuilder.create().texOffs(0, 4).addBox(0F, -1.5F, -1.5F, 9F, 3F, 3F),
                PartPose.offsetAndRotation(x, 17F, z, 0F, yaw, roll));
        upper.addOrReplaceChild("lower", CubeListBuilder.create().texOffs(0, 0).addBox(0F, -1F, -1F, 11F, 2F, 2F),
                PartPose.offsetAndRotation(7.5F, 0F, 0F, 0F, 0F, 1.7453292519943295F));
    }

    @Override
    public ModelPart root() {
        return root;
    }

    @Override
    public void setupAnim(TGNpc entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        head.yRot = netHeadYaw * Mth.DEG_TO_RAD;
        head.xRot = headPitch * Mth.DEG_TO_RAD;

        float swing0 = -Mth.cos(limbSwing * 0.6662F * 2.0F) * 0.4F * limbSwingAmount;
        float swing1 = -Mth.cos(limbSwing * 0.6662F * 2.0F + Mth.PI) * 0.4F * limbSwingAmount;
        float lift0 = -Mth.cos(limbSwing * 0.6662F * 3.0F) * 0.4F * limbSwingAmount;
        float lift1 = -Mth.cos(limbSwing * 0.6662F * 4.0F + Mth.PI) * 0.4F * limbSwingAmount;
        pose(leg0, -swing0, LEG_YAW + swing0 * 1.25F, LEG_ROLL - lift0 * 0.75F);
        pose(leg3, swing0, LEG_YAW_INNER - swing0 * 1.25F, -LEG_ROLL + lift0 * 0.75F);
        pose(leg2, -swing1, -LEG_YAW + swing1 * 1.25F, LEG_ROLL - lift1 * 0.75F);
        pose(leg1, swing1, -LEG_YAW_INNER - swing1 * 1.25F, -LEG_ROLL + lift1 * 0.75F);

        jawUpper.xRot = JAW_UPPER + Mth.cos(ageInTicks * 0.1F) * 0.1F;
        jawLower.xRot = JAW_LOWER - Mth.cos(ageInTicks * 0.2F) * 0.1F;
        // Techguns 2: the jaws open over the first half of the bite
        int bite = entity instanceof AlienBug bug ? bug.attackTimer() : 0;
        if (bite > 5) {
            float open = 0.5F * (1.0F - Mth.cos((bite - 5) / 5.0F * Mth.PI));
            jawUpper.xRot += open * 0.3F;
            jawLower.xRot -= open * 0.4F;
        }
    }

    private static void pose(ModelPart leg, float pitch, float yaw, float roll) {
        leg.xRot = pitch;
        leg.yRot = yaw;
        leg.zRot = roll;
    }
}
