package techguns.client.render.item;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.fml.common.asm.enumextension.EnumProxy;
import net.neoforged.neoforge.client.IArmPoseTransformer;

/**
 * Techguns arm poses, added to {@link HumanoidModel.ArmPose} through {@code META-INF/enumextensions.json}.
 */
// MIGRATION(26.x): arm poses move to the entity render state; check the enum extension still applies.
public final class TGArmPoses {
    /**
     * Gliding with a gun (Techguns 2 left this as "TODO: Elytra Flying"): both arms stretch out ahead and hold the
     * gun along the flight direction and following where the player looks, instead of letting it hang from one
     * arm. Two-handed, so it is applied last and sets both arms.
     */
    public static final EnumProxy<HumanoidModel.ArmPose> ELYTRA_AIM = new EnumProxy<>(HumanoidModel.ArmPose.class, true,
            (IArmPoseTransformer) TGArmPoses::elytraAim);

    private TGArmPoses() {
    }

    private static void elytraAim(HumanoidModel<?> model, LivingEntity entity, HumanoidArm arm) {
        // while gliding the body lies along the flight (the head is turned up by an eighth turn to look ahead); held guns
        // lie along the arm, so arms stretched out past the head point the gun where the player flies
        float pitch = -Mth.PI + (model.head.xRot + Mth.PI / 4.0f);
        model.rightArm.xRot = pitch;
        model.leftArm.xRot = pitch;
        model.rightArm.yRot = -0.1f + model.head.yRot;
        model.leftArm.yRot = 0.1f + model.head.yRot;
        model.rightArm.zRot = 0.0f;
        model.leftArm.zRot = 0.0f;
    }

    public static HumanoidModel.ArmPose elytraAim() {
        return ELYTRA_AIM.getValue();
    }
}
