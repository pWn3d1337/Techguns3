package techguns.client.render.block;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.AABB;
import techguns.debug.DungeonGeneratorBlockEntity;
import techguns.debug.DungeonScannerBlockEntity;
import techguns.debug.DungeonToolBlockEntity;
import techguns.world.structure.dungeon.DungeonDebug;
import techguns.world.structure.dungeon.SegmentType;

/**
 * Techguns 2 {@code RenderDungeonScanner} / {@code RenderDungeonGenerator}: the ghost of the area. The scanner shows
 * each segment's place with its floor (red), ceiling (green), walls (blue), doors (magenta) and ramp steps (cyan);
 * the generator the box the dungeon fills.
 */
// MIGRATION(26.x): BlockEntityRenderer#render gets a render state and submit collector in 1.21.9+.
public class DungeonToolRenderer implements BlockEntityRenderer<DungeonToolBlockEntity> {
    private static final float O = 0.05f;
    private static final float[] FLOOR = {1.0f, 0.5f, 0.5f};
    private static final float[] CEILING = {0.5f, 1.0f, 0.5f};
    private static final float[] WALL = {0.5f, 0.5f, 1.0f};
    private static final float[] DOOR = {1.0f, 0.5f, 1.0f};
    private static final float[] STEP = {0.5f, 1.0f, 1.0f};
    private static final float ALPHA = 0.25f;

    /** One ghost box: corner, size, colour, alpha. */
    private record Box(double x, double y, double z, double sx, double sy, double sz, float[] color, float alpha) {
    }

    public DungeonToolRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(DungeonToolBlockEntity tool, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int light, int overlay) {
        if (!tool.showGhost()) {
            return;
        }
        VertexConsumer consumer = buffer.getBuffer(RenderType.debugFilledBox());
        if (tool instanceof DungeonScannerBlockEntity scanner) {
            BlockPos base = tool.getBlockPos();
            for (SegmentType type : SegmentType.values()) {
                BlockPos at = DungeonDebug.segmentOrigin(base, type, scanner.sizeXZ()).subtract(base);
                for (Box box : segmentGhost(type, scanner.sizeXZ(), scanner.sizeY())) {
                    draw(poseStack, consumer, at, box);
                }
            }
        } else if (tool instanceof DungeonGeneratorBlockEntity generator) {
            Vec3i size = DungeonGeneratorBlockEntity.SIZE;
            draw(poseStack, consumer, generator.origin().subtract(tool.getBlockPos()), new Box(0, 0, 0, size.getX(), size.getY(), size.getZ(), WALL, 0.125f));
        }
    }

    private static void draw(PoseStack poseStack, VertexConsumer consumer, BlockPos offset, Box box) {
        double x = offset.getX() + box.x();
        double y = offset.getY() + box.y();
        double z = offset.getZ() + box.z();
        LevelRenderer.addChainedFilledBoxVertices(poseStack, consumer, x, y, z, x + box.sx(), y + box.sy(), z + box.sz(),
                box.color()[0], box.color()[1], box.color()[2], box.alpha());
    }

    /** Techguns 2 {@code drawGhost}: the hint boxes of one segment ({@code s} = sizeXZ, {@code h} = sizeY). */
    static List<Box> segmentGhost(SegmentType type, int s, int h) {
        List<Box> boxes = new ArrayList<>();
        boxes.add(new Box(O, O, O, s - 2 * O, 1 - 2 * O, s - 2 * O, FLOOR, ALPHA));
        if (type != SegmentType.RAMP && type != SegmentType.FOUNDATION && type != SegmentType.PILLARS) {
            boxes.add(new Box(O, O + h - 1, O, s - 2 * O, 1 - 2 * O, s - 2 * O, CEILING, ALPHA));
        }
        double wallH = h - 2 - 2 * O;
        Box north = new Box(O, O + 1, O, s - 2 * O, wallH, 1 - 2 * O, WALL, ALPHA);
        Box south = new Box(O, O + 1, O + s - 1, s - 2 * O, wallH, 1 - 2 * O, WALL, ALPHA);
        Box westShort = new Box(O, O + 1, O + 1, 1 - 2 * O, wallH, s - 1 - 2 * O, WALL, ALPHA);
        Box westInner = new Box(O, O + 1, O + 1, 1 - 2 * O, wallH, s - 2 - 2 * O, WALL, ALPHA);
        Box cornerSouthWest = new Box(O, O + 1, O + s - 1, 1 - 2 * O, wallH, 1 - 2 * O, WALL, ALPHA);
        Box cornerSouthEast = new Box(O + s - 1, O + 1, O + s - 1, 1 - 2 * O, wallH, 1 - 2 * O, WALL, ALPHA);
        Box cornerNorthWest = new Box(O, O + 1, O, 1 - 2 * O, wallH, 1 - 2 * O, WALL, ALPHA);
        Box cornerNorthEast = new Box(O + s - 1, O + 1, O, 1 - 2 * O, wallH, 1 - 2 * O, WALL, ALPHA);
        Box doorNorth = new Box(s * 0.5 - 0.5 - O, 1, -O, 1 + 2 * O, 2 + O, 1 + 2 * O, DOOR, ALPHA);
        Box doorWest = new Box(-O, 1, s * 0.5 - 0.5 - O, 1 + 2 * O, 2 + O, 1 + 2 * O, DOOR, ALPHA);
        switch (type) {
            case STRAIGHT -> boxes.addAll(List.of(north, south));
            case CURVE -> boxes.addAll(List.of(north, westShort, cornerSouthEast));
            case FORK -> boxes.addAll(List.of(north, cornerSouthWest, cornerSouthEast));
            case CROSS -> boxes.addAll(List.of(cornerSouthWest, cornerSouthEast, cornerNorthWest, cornerNorthEast));
            case END -> boxes.addAll(List.of(north, south, westInner));
            case RAMP -> {
                boxes.add(new Box(O, O + 1, O, s - 2 * O, h * 2 - 2 - 2 * O, 1 - 2 * O, WALL, ALPHA));
                boxes.add(new Box(O, O + 1, O + s - 1, s - 2 * O, h * 2 - 2 - 2 * O, 1 - 2 * O, WALL, ALPHA));
                boxes.add(new Box(s - O - 1, O + 1, O + 1, 1 - 2 * O, h - 1 - 2 * O, s - 2 - 2 * O, WALL, ALPHA));
                boxes.add(new Box(O, O + h - 1, O + 1, 1 - 2 * O, 1 - 2 * O, s - 2 - 2 * O, CEILING, ALPHA));
                boxes.add(new Box(s - 1 + O, O + h, O + 1, 1 - 2 * O, 1 - 2 * O, s - 2 - 2 * O, FLOOR, 0.125f));
                boxes.add(new Box(O, O + h * 2 - 1, O, s - 2 * O, 1 - 2 * O, s - 2 * O, CEILING, ALPHA));
                boxes.add(new Box(O, h + O, O + 1, 1 - 2 * O, h - 1 - 2 * O, s - 2 - 2 * O, WALL, ALPHA));
                double f = (double) s / (h + 1);
                for (int i = 1; i < h; i++) {
                    boxes.add(new Box(O + f * i, O + i, s * 0.5 - 0.5, 1 - 2 * O, 1 - 2 * O, 1 - 2 * O, STEP, ALPHA));
                }
            }
            case ENTRANCE -> boxes.addAll(List.of(north, south, westInner, doorWest));
            case ROOM_WALL -> boxes.add(north);
            case ROOM_CORNER -> boxes.addAll(List.of(north, westShort));
            case ROOM_INNER -> {
            }
            case ROOM_DOOR -> boxes.addAll(List.of(north, doorNorth));
            case ROOM_DOOR_CORNER1 -> boxes.addAll(List.of(north, westShort, doorNorth));
            case ROOM_DOOR_CORNER2 -> boxes.addAll(List.of(north, westShort, doorWest));
            case ROOM_DOOR_CORNER_DOUBLE -> boxes.addAll(List.of(north, westShort, doorNorth, doorWest));
            case FOUNDATION -> boxes.add(new Box(O, O, O, s - 2 * O, h - 2 * O, s - 2 * O, WALL, ALPHA));
            case PILLARS -> {
                for (double[] corner : new double[][] {{0, 0}, {s - 1, 0}, {s - 1, s - 1}, {0, s - 1}}) {
                    boxes.add(new Box(O + corner[0], O, O + corner[1], 1 - 2 * O, h - 2 * O, 1 - 2 * O, WALL, ALPHA));
                }
            }
        }
        return boxes;
    }

    @Override
    public boolean shouldRenderOffScreen(DungeonToolBlockEntity tool) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 256;
    }

    @Override
    public AABB getRenderBoundingBox(DungeonToolBlockEntity tool) {
        BlockPos pos = tool.getBlockPos();
        Vec3i extent = tool instanceof DungeonScannerBlockEntity scanner ? DungeonDebug.extents(scanner.sizeXZ(), scanner.sizeY())
                : DungeonGeneratorBlockEntity.SIZE.offset(1, 0, 1);
        return new AABB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + extent.getX() + 1, pos.getY() + extent.getY() + 1,
                pos.getZ() + extent.getZ() + 1);
    }
}
