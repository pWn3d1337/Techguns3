package techguns.compat.decor;

import java.util.Optional;
import java.util.function.Supplier;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import techguns.TGConfig;

/**
 * Optional decorative mod blocks for structures ({@link DecorRole}): a role's block when its mod is installed and
 * {@code decorModBlocks} is on, else the given fallback. Lookups go by registry id, so the mods are never needed to
 * build or run Techguns, and a renamed block just falls back.
 */
public final class DecorBlocks {
    private DecorBlocks() {
    }

    /** The installed block of a role, if any. */
    public static Optional<BlockState> state(DecorRole role) {
        if (!TGConfig.DECOR_MOD_BLOCKS.get()) {
            return Optional.empty();
        }
        for (ResourceLocation id : role.candidates()) {
            Optional<Block> block = BuiltInRegistries.BLOCK.getOptional(id);
            if (block.isPresent()) {
                return Optional.of(block.get().defaultBlockState());
            }
        }
        return Optional.empty();
    }

    public static BlockState stateOr(DecorRole role, BlockState fallback) {
        return state(role).orElse(fallback);
    }

    /** For block lists resolved at placement time. */
    public static Supplier<Block> blockOr(DecorRole role, Supplier<? extends Block> fallback) {
        return () -> state(role).map(BlockState::getBlock).orElseGet(fallback);
    }
}
