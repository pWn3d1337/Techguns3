package techguns.client.render.item;

import java.util.Locale;

import javax.annotation.Nullable;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.neoforged.neoforge.client.ClientHooks;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import techguns.client.model.TGGeoBridge;
import techguns.client.model.TGModel;

/**
 * The player's arms on a gun in first person, at the gun model's hand bones ({@link TGGeoBridge#HAND_RIGHT}, the grip
 * hand; {@link TGGeoBridge#HAND_LEFT}, the support hand of two-handed guns). Drawn right after the gun model, whose draw
 * reported the bones' poses, so the arms follow every gun motion (equip, recoil, reload, aim, part keyframes).
 * <p>
 * The hand bones are authored for a right-handed hold. A left-side draw (left main arm, offhand gun) mirrors them across
 * the model's x = 0 plane in the frame of the {@code root} bone, as the gun's own left-side draw is the mirror image of
 * the right one, and draws the opposite arms.
 */
// MIGRATION(26.x): 1.21.2+ moves PlayerRenderer/PlayerModel to render states; ModelPart arms and the RenderArmEvent hook change.
public final class FirstPersonArms {
    private static final Matrix4f MIRROR_X = new Matrix4f().scaling(-1f, 1f, 1f);
    /** The hand end of the arm box (y = 10 px below the shoulder pivot, Java model space y down). */
    private static final float HAND_END = 10f / 16f;

    /** Root and grip bone poses of the last right-side first-person gun draw, for {@link #eyeReport()}. */
    @Nullable
    private static Matrix4f lastRoot;
    @Nullable
    private static Matrix4f lastGrip;

    private FirstPersonArms() {
    }

    /** Keeps the bone poses of a right-side first-person draw for {@link #eyeReport()} (dev tuning aid). */
    static void remember(TGModel model) {
        Matrix4f root = model.marker(TGGeoBridge.ROOT);
        Matrix4f grip = model.marker(TGGeoBridge.HAND_RIGHT);
        // the hand pose space is turned with the camera; the model-view matrix turns it back to view space
        Matrix4f view = new Matrix4f(RenderSystem.getModelViewMatrix());
        lastRoot = root != null ? new Matrix4f(view).mul(root) : null;
        lastGrip = grip != null ? new Matrix4f(view).mul(grip) : null;
    }

    /**
     * The first-person camera in the held gun's model space (Blockbench coordinates, pixels: geo file x negated), from the
     * last right-side draw: eye position, view and up direction. Used to set Blockbench's camera to the in-game view when
     * placing hand bones; the grip bone's pivot is listed as a check against the file. Null before any draw.
     */
    @Nullable
    public static String eyeReport() {
        if (lastRoot == null) {
            return null;
        }
        Matrix4f inv = new Matrix4f(lastRoot).invert();
        Vector3f eye = inv.transformPosition(new Vector3f()).mul(16f);
        Vector3f forward = inv.transformDirection(new Vector3f(0f, 0f, -1f)).normalize();
        Vector3f up = inv.transformDirection(new Vector3f(0f, 1f, 0f)).normalize();
        String grip = "";
        if (lastGrip != null) {
            Vector3f g = new Matrix4f(inv).mul(lastGrip).getTranslation(new Vector3f()).mul(16f);
            grip = " grip=" + vec(g);
        }
        return "eye=" + vec(eye) + " forward=" + vec(forward) + " up=" + vec(up) + " fov="
                + Minecraft.getInstance().options.fov().get() + grip;
    }

    private static String vec(Vector3f v) {
        return String.format(Locale.ROOT, "%.3f,%.3f,%.3f", v.x, v.y, v.z);
    }

    /**
     * Draws the arms of {@code player} at the hand bones of the model just drawn ({@link TGModel#marker}); arms whose bone
     * the model lacks are left out.
     *
     * @param leftSide  the gun is drawn on the left side (mirrored hold)
     * @param twoHanded also draw the support hand
     */
    static void render(TGModel model, PoseStack poseStack, MultiBufferSource buffers, int light, AbstractClientPlayer player,
                       boolean leftSide, boolean twoHanded) {
        Matrix4f grip = model.marker(TGGeoBridge.HAND_RIGHT);
        Matrix4f support = twoHanded ? model.marker(TGGeoBridge.HAND_LEFT) : null;
        Matrix4f root = model.marker(TGGeoBridge.ROOT);
        if (grip == null && support == null || leftSide && root == null) {
            return;
        }
        HumanoidArm gripArm = leftSide ? HumanoidArm.LEFT : HumanoidArm.RIGHT;
        if (grip != null) {
            drawArm(poseStack, buffers, light, player, place(grip, leftSide ? root : null), gripArm);
        }
        if (support != null) {
            drawArm(poseStack, buffers, light, player, place(support, leftSide ? root : null), gripArm.getOpposite());
        }
    }

    /**
     * The hand bone's pose; for a left-side draw mirrored across the model's x = 0 plane in the root bone's frame
     * ({@code root · Sx · root⁻¹ · hand}) and in the bone's own frame ({@code · Sx}), so the opposite arm lands where the
     * mirror image of the authored arm would be.
     */
    private static Matrix4f place(Matrix4f hand, @Nullable Matrix4f root) {
        if (root == null) {
            return hand;
        }
        return new Matrix4f(root).mul(MIRROR_X).mul(new Matrix4f(root).invert()).mul(hand).mul(MIRROR_X);
    }

    private static void drawArm(PoseStack poseStack, MultiBufferSource buffers, int light, AbstractClientPlayer player, Matrix4f hand,
                                HumanoidArm side) {
        if (!(Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(player) instanceof PlayerRenderer renderer)) {
            return;
        }
        PlayerModel<AbstractClientPlayer> playerModel = renderer.getModel();
        boolean right = side == HumanoidArm.RIGHT;
        ModelPart arm = right ? playerModel.rightArm : playerModel.leftArm;
        ModelPart sleeve = right ? playerModel.rightSleeve : playerModel.leftSleeve;
        // the arm box's centre line: classic arms are 4 px wide (x -3..1 right, -1..3 left), slim ones 3 px
        float centre = (player.getSkin().model() == PlayerSkin.Model.SLIM ? 0.5f : 1f) / 16f * (right ? 1f : -1f);

        poseStack.pushPose();
        // from the current pose to the hand bone's pivot, without the gun's display scale: the arm keeps the size of
        // vanilla's first-person arm (the hand space has scale 1), so the preview cube is the arm divided by that scale
        poseStack.mulPose(new Matrix4f(poseStack.last().pose()).invert().mul(new Matrix4f(hand).normalize3x3()));
        // arm space (y down, the shoulder at the origin) with the hand end's centre at the pivot and the arm along +y
        poseStack.mulPose(Axis.XP.rotationDegrees(180f));
        poseStack.translate(centre, -HAND_END, 0f);
        if (!ClientHooks.renderSpecificFirstPersonArm(poseStack, buffers, light, player, side)) {
            PartPose armPose = arm.storePose();
            PartPose sleevePose = sleeve.storePose();
            boolean armVisible = arm.visible;
            boolean sleeveVisible = sleeve.visible;
            arm.loadPose(PartPose.ZERO);
            sleeve.loadPose(PartPose.ZERO);
            arm.visible = true;
            sleeve.visible = player.isModelPartShown(right ? PlayerModelPart.RIGHT_SLEEVE : PlayerModelPart.LEFT_SLEEVE);
            ResourceLocation skin = player.getSkin().texture();
            arm.render(poseStack, buffers.getBuffer(RenderType.entitySolid(skin)), light, OverlayTexture.NO_OVERLAY);
            sleeve.render(poseStack, buffers.getBuffer(RenderType.entityTranslucent(skin)), light, OverlayTexture.NO_OVERLAY);
            arm.loadPose(armPose);
            sleeve.loadPose(sleevePose);
            arm.visible = armVisible;
            sleeve.visible = sleeveVisible;
        }
        poseStack.popPose();
    }
}
