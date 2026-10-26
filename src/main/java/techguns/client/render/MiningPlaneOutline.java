package techguns.client.render;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RenderHighlightEvent;
import techguns.gun.MeleeToolEvents;
import techguns.gun.MeleeToolItem;

/**
 * Outlines every block of the mining drill's plane, not only the targeted one (Fabric {@code WorldRendererMixin}
 * injected into the block outline drawing; here the vanilla highlight event does it).
 */
public final class MiningPlaneOutline {
    private static final float OUTLINE_ALPHA = 0.4f;

    private MiningPlaneOutline() {
    }

    public static void init(IEventBus gameBus) {
        // MIGRATION(26.x): RenderHighlightEvent.Block and the lines render type change with the render state extraction
        gameBus.addListener(MiningPlaneOutline::onHighlightBlock);
    }

    private static void onHighlightBlock(RenderHighlightEvent.Block event) {
        LocalPlayer player = Minecraft.getInstance().player;
        ClientLevel level = Minecraft.getInstance().level;
        if (player == null || level == null) {
            return;
        }
        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof MeleeToolItem)) {
            return;
        }
        List<BlockPos> blocks = MeleeToolEvents.plane(level, player, stack, event.getTarget().getBlockPos());
        if (blocks.isEmpty()) {
            return;
        }
        PoseStack poseStack = event.getPoseStack();
        Camera camera = event.getCamera();
        Vec3 cam = camera.getPosition();
        VertexConsumer consumer = event.getMultiBufferSource().getBuffer(RenderType.lines());
        CollisionContext context = CollisionContext.of(camera.getEntity());
        for (BlockPos pos : blocks) {
            drawShape(poseStack, consumer, level.getBlockState(pos).getShape(level, pos, context), pos.getX() - cam.x, pos.getY() - cam.y,
                    pos.getZ() - cam.z);
        }
    }

    /** Draws the edges of a shape as vanilla does for the block outline ({@code LevelRenderer.renderShape} is private). */
    private static void drawShape(PoseStack poseStack, VertexConsumer consumer, VoxelShape shape, double x, double y, double z) {
        PoseStack.Pose pose = poseStack.last();
        shape.forAllEdges((x1, y1, z1, x2, y2, z2) -> {
            float dx = (float) (x2 - x1);
            float dy = (float) (y2 - y1);
            float dz = (float) (z2 - z1);
            float length = Mth.sqrt(dx * dx + dy * dy + dz * dz);
            dx /= length;
            dy /= length;
            dz /= length;
            consumer.addVertex(pose, (float) (x1 + x), (float) (y1 + y), (float) (z1 + z)).setColor(0.0f, 0.0f, 0.0f, OUTLINE_ALPHA)
                    .setNormal(pose, dx, dy, dz);
            consumer.addVertex(pose, (float) (x2 + x), (float) (y2 + y), (float) (z2 + z)).setColor(0.0f, 0.0f, 0.0f, OUTLINE_ALPHA)
                    .setNormal(pose, dx, dy, dz);
        });
    }
}
