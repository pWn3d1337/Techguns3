package techguns.debug;

import java.util.List;
import java.util.Locale;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import techguns.world.structure.DebugStructures;
import techguns.world.structure.dungeon.DungeonDebug;
import techguns.world.structure.dungeon.DungeonPreset;

/**
 * Techguns 2 {@code WorldGenTestTool}: sneak + use switches the mode, selecting an area (two corners) runs it there.
 * Techguns 2 had the military camp, its Nether dungeon test and the terrain tools; the other Techguns 3 structures
 * built over an area are here as well.
 */
public class WorldGenTestToolItem extends AreaToolItem {
    public enum Mode {
        MILITARY_CAMP, CITY, CASTLE, NETHER_FORTRESS, SMOOTH, FLATTEN, REMOVE_JUNK;

        public Component displayName() {
            return Component.translatable("techguns.debug.worldgen.mode." + name().toLowerCase(Locale.ROOT));
        }
    }

    public WorldGenTestToolItem(Properties properties) {
        super(properties);
    }

    public static Mode mode(ItemStack stack) {
        int mode = DebugToolData.mode(stack);
        return Mode.values()[Math.floorMod(mode, Mode.values().length)];
    }

    @Override
    protected boolean cyclesMode() {
        return true;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (context.getPlayer() != null && context.getPlayer().isSecondaryUseActive()) {
            return cycle(context.getLevel(), context.getPlayer(), context.getItemInHand());
        }
        return super.useOn(context);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isSecondaryUseActive()) {
            return new InteractionResultHolder<>(cycle(level, player, stack), stack);
        }
        return InteractionResultHolder.pass(stack);
    }

    private static InteractionResult cycle(Level level, Player player, ItemStack stack) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (!TGDebug.check(player)) {
            return InteractionResult.FAIL;
        }
        DebugToolData.setMode(stack, (mode(stack).ordinal() + 1) % Mode.values().length);
        player.displayClientMessage(Component.translatable("techguns.debug.worldgen.mode", mode(stack).displayName()), true);
        return InteractionResult.CONSUME;
    }

    @Override
    protected void apply(ServerLevel level, Player player, ItemStack stack, BlockPos min, BlockPos max) {
        int sizeX = max.getX() - min.getX() + 1;
        int sizeY = max.getY() - min.getY() + 1;
        int sizeZ = max.getZ() - min.getZ() + 1;
        Mode mode = mode(stack);
        switch (mode) {
            case MILITARY_CAMP -> DebugStructures.placeCamp(level, min.getX(), min.getZ(), sizeX, sizeZ, level.random);
            case CITY -> DebugStructures.placeCity(level, min.getX(), min.getZ(), sizeX, sizeZ, level.random);
            case CASTLE -> DungeonDebug.generate(level, min, sizeX, sizeY, sizeZ, DungeonPreset.CASTLE, level.random);
            case NETHER_FORTRESS -> DungeonDebug.generate(level, min, sizeX, sizeY, sizeZ, DungeonPreset.NETHER, level.random);
            case SMOOTH -> DebugStructures.smooth(level, min.getX(), min.getZ(), sizeX, sizeZ);
            case FLATTEN -> DebugStructures.flatten(level, min.getX(), min.getZ(), sizeX, sizeZ);
            case REMOVE_JUNK -> DebugStructures.removeJunk(level, min.getX(), min.getZ(), sizeX, sizeZ);
        }
        player.displayClientMessage(Component.translatable("techguns.debug.worldgen.done", mode.displayName()), false);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("techguns.debug.worldgen.mode", mode(stack).displayName()));
    }
}
