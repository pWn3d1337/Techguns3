package techguns.client.render.entity.npc;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.HumanoidArm;
import techguns.entity.npc.TGNpc;

/** Techguns 2 {@code ModelGenericNPC}: a biped that aims guns like a bow and holds other items out. */
// MIGRATION(26.x): entity models take render states in 1.21.2+.
public class TGNpcModel extends HumanoidModel<TGNpc> {
    private final boolean aimsGuns;

    public TGNpcModel(ModelPart root) {
        this(root, true);
    }

    /** @param aimsGuns false for NPCs that carry guns low (Techguns 2 {@code hasAimedBowAnim = false}, cyber demon) */
    public TGNpcModel(ModelPart root, boolean aimsGuns) {
        super(root);
        this.aimsGuns = aimsGuns;
    }

    @Override
    public void prepareMobModel(TGNpc npc, float limbSwing, float limbSwingAmount, float partialTick) {
        ArmPose pose = npc.isHoldingGun() && aimsGuns ? ArmPose.BOW_AND_ARROW : npc.getMainHandItem().isEmpty() ? ArmPose.EMPTY : ArmPose.ITEM;
        if (npc.getMainArm() == HumanoidArm.LEFT) {
            leftArmPose = pose;
            rightArmPose = ArmPose.EMPTY;
        } else {
            rightArmPose = pose;
            leftArmPose = ArmPose.EMPTY;
        }
        super.prepareMobModel(npc, limbSwing, limbSwingAmount, partialTick);
    }
}
