package techguns.client.render.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.model.data.ModelData;
import techguns.TGIds;
import techguns.block.Door3x3Block;
import techguns.block.Door3x3BlockEntity;

/**
 * Techguns 2 {@code RenderDoor3x3Fast}: while a 3x3 door moves its master block shows only the frame, this draws the
 * moving parts. The tech doors slide their two wings 18 pixels apart; the hangar doors move their four segments up
 * (or down) by three blocks, the segments that would pass the frame disappear on the way. The parts are standalone
 * block models in the door's model space, turned like the blockstate turns the frame.
 */
// MIGRATION(26.x): BlockEntityRenderer#render gets a render state and submit collector in 1.21.9+.
public class Door3x3Renderer implements BlockEntityRenderer<Door3x3BlockEntity> {
    private static final ModelResourceLocation WING_LEFT = model("techdoor3x3_left");
    private static final ModelResourceLocation WING_RIGHT = model("techdoor3x3_right");
    private static final ModelResourceLocation WING_LEFT_NETHER = model("techdoor3x3_left_nether");
    private static final ModelResourceLocation WING_RIGHT_NETHER = model("techdoor3x3_right_nether");
    /** Hangar door segments from the bottom: the bottom edge (up) or top edge (down) and three block-high plates. */
    private static final ModelResourceLocation HANGAR_LOWER = model("hangar_door_lower");
    private static final ModelResourceLocation HANGAR_UPPER = model("hangar_door_down_upper");
    private static final ModelResourceLocation HANGAR_MID1 = model("hangar_door_mid1j");
    private static final ModelResourceLocation HANGAR_MID2 = model("hangar_door_mid2j");
    private static final ModelResourceLocation HANGAR_MID3 = model("hangar_door_mid3j");

    /** How far the tech door wings slide out (18 pixels). */
    private static final float WING_DISTANCE = 1.125f;
    /** How far the hangar door segments move. */
    private static final float HANGAR_DISTANCE = 3.0f;

    public Door3x3Renderer(BlockEntityRendererProvider.Context context) {
    }

    private static ModelResourceLocation model(String name) {
        return ModelResourceLocation.standalone(TGIds.id("block/" + name));
    }

    public static void registerAdditional(ModelEvent.RegisterAdditional event) {
        for (ModelResourceLocation model : new ModelResourceLocation[]{WING_LEFT, WING_RIGHT, WING_LEFT_NETHER, WING_RIGHT_NETHER, HANGAR_LOWER,
                HANGAR_UPPER, HANGAR_MID1, HANGAR_MID2, HANGAR_MID3}) {
            event.register(model);
        }
    }

    @Override
    public void render(Door3x3BlockEntity door, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int light, int overlay) {
        BlockState state = door.getBlockState();
        if (!(state.getBlock() instanceof Door3x3Block block) || !state.getValue(Door3x3Block.MOVING)) {
            return;
        }
        float open = door.openness(partialTick);
        Parts parts = new Parts(poseStack, buffer.getBuffer(Sheets.cutoutBlockSheet()), state, light, overlay);
        poseStack.pushPose();
        if (state.getValue(Door3x3Block.ZPLANE)) {
            // the blockstate's y = 90 turn
            poseStack.translate(0.5f, 0.0f, 0.5f);
            poseStack.mulPose(Axis.YP.rotationDegrees(-90.0f));
            poseStack.translate(-0.5f, 0.0f, -0.5f);
        }
        switch (block.kind()) {
            case METAL -> wings(parts, open, WING_LEFT, WING_RIGHT);
            case NETHER -> wings(parts, open, WING_LEFT_NETHER, WING_RIGHT_NETHER);
            case HANGAR_UP -> hangar(parts, open * HANGAR_DISTANCE, HANGAR_LOWER, HANGAR_MID1, HANGAR_MID2, HANGAR_MID3);
            case HANGAR_DOWN -> hangar(parts, -open * HANGAR_DISTANCE, HANGAR_UPPER, HANGAR_MID3, HANGAR_MID2, HANGAR_MID1);
        }
        poseStack.popPose();
    }

    private static void wings(Parts parts, float open, ModelResourceLocation left, ModelResourceLocation right) {
        parts.draw(left, -open * WING_DISTANCE, 0.0f);
        parts.draw(right, open * WING_DISTANCE, 0.0f);
    }

    /**
     * Moves the segments by {@code offset}, the edge and the leading plate always, the next plates only while they have
     * not passed the frame yet (Techguns 2: up to 2 and 1 blocks).
     */
    private static void hangar(Parts parts, float offset, ModelResourceLocation edge, ModelResourceLocation leading, ModelResourceLocation middle,
                               ModelResourceLocation trailing) {
        float distance = Math.abs(offset);
        parts.draw(edge, 0.0f, offset);
        parts.draw(leading, 0.0f, offset);
        if (distance <= 2.0f) {
            parts.draw(middle, 0.0f, offset);
        }
        if (distance <= 1.0f) {
            parts.draw(trailing, 0.0f, offset);
        }
    }

    /** Draws standalone part models moved along the door plane ({@code x}) and up ({@code y}). */
    private record Parts(PoseStack poseStack, VertexConsumer consumer, BlockState state, int light, int overlay) {
        void draw(ModelResourceLocation id, float x, float y) {
            BakedModel model = Minecraft.getInstance().getModelManager().getModel(id);
            poseStack.pushPose();
            poseStack.translate(x, y, 0.0f);
            Minecraft.getInstance().getBlockRenderer().getModelRenderer().renderModel(poseStack.last(), consumer, state, model, 1.0f, 1.0f, 1.0f,
                    light, overlay, ModelData.EMPTY, RenderType.cutout());
            poseStack.popPose();
        }
    }

    /** The door spans three blocks around the master; the hangar segments move three blocks up or down. */
    @Override
    public AABB getRenderBoundingBox(Door3x3BlockEntity door) {
        return new AABB(door.getBlockPos()).inflate(2.0, 4.0, 2.0);
    }
}
