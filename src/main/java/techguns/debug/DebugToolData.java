package techguns.debug;

import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import techguns.registry.TGComponents;

/** Typed access to the debug tools' item data: the first corner of an area selection and the world gen tool mode. */
public final class DebugToolData {
    private DebugToolData() {
    }

    public static Optional<BlockPos> corner(ItemStack stack) {
        return Optional.ofNullable(stack.get(TGComponents.DEBUG_CORNER.get()));
    }

    public static void setCorner(ItemStack stack, BlockPos pos) {
        stack.set(TGComponents.DEBUG_CORNER.get(), pos.immutable());
    }

    public static void clearCorner(ItemStack stack) {
        stack.remove(TGComponents.DEBUG_CORNER.get());
    }

    public static int mode(ItemStack stack) {
        return stack.getOrDefault(TGComponents.DEBUG_MODE.get(), 0);
    }

    public static void setMode(ItemStack stack, int mode) {
        stack.set(TGComponents.DEBUG_MODE.get(), mode);
    }
}
