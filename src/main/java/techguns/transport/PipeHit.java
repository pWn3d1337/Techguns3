package techguns.transport;

import java.util.Optional;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Which part of which segment a player looks at: the nearest part box their view ray hits. {@link #side} is the arm's
 * or plate's side, or for the core the face that was hit (so a disabled side can be clicked on the core).
 */
public record PipeHit(PipeSegment segment, PipeParts.Part part, Direction side) {
    @Nullable
    public static PipeHit pick(TGPipeBlockEntity pipe, Player player) {
        Vec3 from = player.getEyePosition();
        double range = player.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.BLOCK_INTERACTION_RANGE) + 1.0;
        // from yaw and pitch: the head rotation behind getViewVector is not synced to the server for players
        return pick(pipe, from, from.add(Vec3.directionFromRotation(player.getXRot(), player.getYRot()).scale(range)));
    }

    @Nullable
    public static PipeHit pick(TGPipeBlockEntity pipe, Vec3 from, Vec3 to) {
        BlockPos pos = pipe.getBlockPos();
        PipeHit best = null;
        double bestDistance = Double.MAX_VALUE;
        for (PipeSegment segment : pipe.segments()) {
            for (PipeParts.Part part : PipeParts.parts(segment.kind().lane(), segment.links(), segment.ends())) {
                AABB box = part.box().move(pos);
                Optional<Vec3> hit = box.clip(from, to);
                if (hit.isEmpty()) {
                    continue;
                }
                double distance = hit.get().distanceToSqr(from);
                if (distance < bestDistance) {
                    bestDistance = distance;
                    Direction side = part.side() != null ? part.side() : face(box, hit.get());
                    best = new PipeHit(segment, part, side);
                }
            }
        }
        return best;
    }

    /** The face of {@code box} that {@code point} (on its surface) lies on. */
    private static Direction face(AABB box, Vec3 point) {
        Direction best = Direction.UP;
        double min = Double.MAX_VALUE;
        double[][] faces = {{box.minY, point.y}, {box.maxY, point.y}, {box.minZ, point.z}, {box.maxZ, point.z}, {box.minX, point.x}, {box.maxX, point.x}};
        Direction[] dirs = {Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};
        for (int i = 0; i < 6; i++) {
            double d = Math.abs(faces[i][0] - faces[i][1]);
            if (d < min) {
                min = d;
                best = dirs[i];
            }
        }
        return best;
    }
}
