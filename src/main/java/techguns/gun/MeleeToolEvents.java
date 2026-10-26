package techguns.gun;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

/**
 * Area mining of melee tools with a mining radius (mining drill). Techguns 2 / Fabric patched block breaking with
 * mixins; NeoForge's break speed and break events do the same: the plane is mined at the speed of its hardest block,
 * and breaking the target also breaks the rest of the plane.
 */
public final class MeleeToolEvents {
    /** Guards the extra block breaks, which fire break events themselves. */
    private static boolean breakingPlane;

    private MeleeToolEvents() {
    }

    public static void init(IEventBus gameBus) {
        gameBus.addListener(MeleeToolEvents::onBreakSpeed);
        gameBus.addListener(MeleeToolEvents::onBlockBreak);
    }

    /**
     * The other blocks of the plane mined together with {@code center}: same face axis, not block entities, not
     * unbreakable, and minable by the tool.
     */
    public static List<BlockPos> plane(BlockGetter level, Player player, ItemStack stack, BlockPos center) {
        List<BlockPos> blocks = new ArrayList<>();
        if (!(stack.getItem() instanceof MeleeToolItem tool)) {
            return blocks;
        }
        int radius = tool.miningRadius(player);
        BlockHitResult target = target(player);
        if (radius <= 0 || target == null || !target.getBlockPos().equals(center)) {
            return blocks;
        }
        Direction.Axis axis = target.getDirection().getAxis();
        for (int a = -radius; a <= radius; a++) {
            for (int b = -radius; b <= radius; b++) {
                if (a == 0 && b == 0) {
                    continue;
                }
                BlockPos pos = switch (axis) {
                    case X -> center.offset(0, a, b);
                    case Y -> center.offset(a, 0, b);
                    case Z -> center.offset(a, b, 0);
                };
                BlockState state = level.getBlockState(pos);
                if (!state.isAir() && level.getBlockEntity(pos) == null && state.getDestroySpeed(level, pos) >= 0.0f
                        && tool.isCorrectToolForDrops(stack, state)) {
                    blocks.add(pos);
                }
            }
        }
        return blocks;
    }

    /** The block the player looks at, with the face (vanilla reach). */
    @Nullable
    private static BlockHitResult target(Player player) {
        double reach = player.blockInteractionRange();
        Vec3 eye = player.getEyePosition();
        HitResult hit = player.level().clip(new ClipContext(eye, eye.add(player.getViewVector(1.0f).scale(reach)), ClipContext.Block.OUTLINE,
                ClipContext.Fluid.NONE, player));
        return hit instanceof BlockHitResult blockHit && hit.getType() == HitResult.Type.BLOCK ? blockHit : null;
    }

    /** The plane breaks at the speed of its hardest block. */
    private static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        Player player = event.getEntity();
        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof MeleeToolItem) || event.getPosition().isEmpty()) {
            return;
        }
        BlockPos center = event.getPosition().get();
        Level level = player.level();
        float hardness = event.getState().getDestroySpeed(level, center);
        float hardest = hardness;
        for (BlockPos pos : plane(level, player, stack, center)) {
            hardest = Math.max(hardest, level.getBlockState(pos).getDestroySpeed(level, pos));
        }
        if (hardest > hardness && hardness > 0.0f) {
            event.setNewSpeed(event.getNewSpeed() * hardness / hardest);
        }
    }

    private static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (breakingPlane || !(event.getPlayer() instanceof ServerPlayer player)) {
            return;
        }
        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof MeleeToolItem)) {
            return;
        }
        List<BlockPos> blocks = plane(player.level(), player, stack, event.getPos());
        breakingPlane = true;
        try {
            for (BlockPos pos : blocks) {
                if (stack.isEmpty() || !(stack.getItem() instanceof MeleeToolItem tool) || !tool.isPowered(stack)) {
                    break;
                }
                // full vanilla break path: events, drops with the tool, fuel use via mineBlock
                player.gameMode.destroyBlock(pos);
            }
        } finally {
            breakingPlane = false;
        }
    }
}
