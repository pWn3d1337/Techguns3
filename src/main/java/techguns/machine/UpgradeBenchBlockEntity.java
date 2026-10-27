package techguns.machine;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import techguns.registry.TGBlockEntities;

/**
 * Upgrade Bench (Techguns 2 {@code UpgradeBenchTileEnt}, "Armor Upgrade Bench"): keeps no items, the anvil-like work
 * happens in {@link UpgradeBenchMenu}; the block entity only holds owner and security.
 */
public class UpgradeBenchBlockEntity extends TGMachineBlockEntity {
    public UpgradeBenchBlockEntity(BlockPos pos, BlockState state) {
        super(TGBlockEntities.UPGRADE_BENCH.get(), pos, state, 0, 0);
    }

    @Override
    protected boolean canPlace(int slot, ItemStack stack) {
        return false;
    }

    @Override
    protected boolean isOutputSlot(int slot) {
        return false;
    }

    @Override
    protected void checkAndStartOperation() {
    }

    @Override
    protected void finishOperation(MachineOperation operation) {
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new UpgradeBenchMenu(id, inventory, this);
    }
}
