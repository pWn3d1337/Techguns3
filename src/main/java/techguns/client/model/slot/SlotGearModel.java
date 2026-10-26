package techguns.client.model.slot;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;

/**
 * Model of an extra-slot gear item (Techguns 2 {@code ModelAdditionalSlotBase} subclasses). Before drawing it takes
 * over the pose of the matching part of the worn player model, the way the 1.12 models recomputed the biped angles.
 */
// MIGRATION(26.x): Model / ModelPart / renderToBuffer signatures change (render states) in 1.21.2+.
public final class SlotGearModel extends Model {
    /** Which player model part the gear follows. */
    public enum Attach {
        /** Parts live under a {@code head} part that copies the player's head. */
        HEAD,
        /** Top-level parts at the body pivot; the followers copy the body's X rotation (Techguns 2 {@code copyRotateAngles}). */
        BACK,
        /** {@code right_arm} / {@code left_arm} parts copy the player's arms. */
        ARMS
    }

    private final ModelPart root;
    private final Attach attach;
    private final List<ModelPart> followers = new ArrayList<>();

    SlotGearModel(ModelPart root, Attach attach, String... followerNames) {
        super(RenderType::entityCutoutNoCull);
        this.root = root;
        this.attach = attach;
        for (String name : followerNames) {
            followers.add(root.getChild(name));
        }
    }

    /** A named part below the attach part (for toggling visibility). */
    public ModelPart part(String name) {
        return attach == Attach.HEAD ? root.getChild("head").getChild(name) : root.getChild(name);
    }

    /** Copies the pose of the player model part this gear is attached to. */
    public void follow(PlayerModel<?> player) {
        switch (attach) {
            case HEAD -> root.getChild("head").copyFrom(player.head);
            case BACK -> {
                // 1.12: whole model translated 0.2 blocks when sneaking == the body's y offset in 1.21
                root.setPos(0.0f, player.body.y, 0.0f);
                for (ModelPart follower : followers) {
                    follower.xRot = player.body.xRot;
                }
            }
            case ARMS -> {
                root.getChild("right_arm").copyFrom(player.rightArm);
                root.getChild("left_arm").copyFrom(player.leftArm);
            }
        }
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        root.render(poseStack, buffer, packedLight, packedOverlay, color);
    }
}
