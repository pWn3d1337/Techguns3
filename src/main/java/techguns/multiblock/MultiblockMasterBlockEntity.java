package techguns.multiblock;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import techguns.machine.TGMachineBlockEntity;
import techguns.network.FxPayloads;
import techguns.network.TGNetwork;

/**
 * Controller of a multiblock machine (Techguns 2 {@code MultiBlockMachineTileEntMaster}): a machine that only works
 * while formed. Using the controller checks its {@link MultiblockSchematic}, links the parts and remembers them;
 * breaking the controller or a part unforms the structure.
 */
public abstract class MultiblockMasterBlockEntity extends TGMachineBlockEntity {
    private boolean formed;
    private Direction direction = Direction.SOUTH;
    /** Positions of the linked parts (the structure as formed; the schematic may depend on the world). */
    private final List<BlockPos> linked = new ArrayList<>();

    protected MultiblockMasterBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, int slots, int energyCapacity) {
        super(type, pos, state, slots, energyCapacity);
    }

    public abstract MultiblockSchematic schematic();

    public boolean isFormed() {
        return formed;
    }

    /** The controller side the structure was formed from; it lies behind and beside the controller. */
    public Direction getDirection() {
        return direction;
    }

    @Override
    protected boolean canWork() {
        return formed;
    }

    /** Shape of a formed part at {@code pos} (Techguns 2 {@code getBBforSlave}); a full block by default. */
    public VoxelShape partShape(BlockPos pos) {
        return Shapes.block();
    }

    /** Fluid tanks exposed by fluid connectors; none by default. */
    @Nullable
    public IFluidHandler getMultiblockFluidHandler() {
        return null;
    }

    /**
     * Techguns 2 {@code checkForm} + {@code form}: if every part is in place, links them and forms; otherwise pings
     * the wrong positions for the player and says why. Server only.
     */
    public boolean tryForm(Player player, Direction side) {
        if (level == null || formed || !schematic().canFormFromSide(side)) {
            return false;
        }
        List<MultiblockPart> parts = schematic().parts(level, worldPosition, side);
        List<BlockPos> invalid = parts.stream().filter(part -> !matches(part)).map(MultiblockPart::pos).toList();
        if (parts.isEmpty() || !invalid.isEmpty()) {
            if (player instanceof ServerPlayer serverPlayer) {
                invalid.forEach(pos -> TGNetwork.sendFxTo(serverPlayer, new FxPayloads.SpawnFx("MultiblockInvalidPing", pos.getX() + 0.5,
                        pos.getY() + 0.5, pos.getZ() + 0.5)));
            }
            player.displayClientMessage(Component.translatable("techguns.msg.error.multiblockinvalid").withStyle(ChatFormatting.RED), true);
            return false;
        }
        for (MultiblockPart part : parts) {
            if (part.isAir()) {
                continue;
            }
            BlockState state = level.getBlockState(part.pos());
            level.setBlock(part.pos(), state.setValue(MultiblockPartBlock.FORMED, true), 3);
            if (level.getBlockEntity(part.pos()) instanceof MultiblockPartBlockEntity partEntity) {
                partEntity.link(worldPosition, part.connector());
            }
            linked.add(part.pos().immutable());
        }
        formed = true;
        direction = side;
        level.setBlock(worldPosition, getBlockState().setValue(MultiblockPartBlock.FORMED, true), 3);
        contentsChanged = true;
        setChanged();
        sendUpdate();
        return true;
    }

    private boolean matches(MultiblockPart part) {
        if (part.isAir()) {
            return level.isEmptyBlock(part.pos());
        }
        BlockState state = level.getBlockState(part.pos());
        return state.is(part.block()) && state.hasProperty(MultiblockPartBlock.FORMED) && !state.getValue(MultiblockPartBlock.FORMED);
    }

    /** Techguns 2 {@code unform}: unlinks all parts that are still there. Server only. */
    public void unform() {
        if (level == null || !formed) {
            return;
        }
        formed = false;
        for (BlockPos pos : linked) {
            if (level.getBlockEntity(pos) instanceof MultiblockPartBlockEntity partEntity && worldPosition.equals(partEntity.getMasterPos())) {
                partEntity.unlink();
                BlockState state = level.getBlockState(pos);
                if (state.hasProperty(MultiblockPartBlock.FORMED)) {
                    level.setBlock(pos, state.setValue(MultiblockPartBlock.FORMED, false), 3);
                }
            }
        }
        linked.clear();
        BlockState own = level.getBlockState(worldPosition);
        if (own.hasProperty(MultiblockPartBlock.FORMED) && own.getValue(MultiblockPartBlock.FORMED)) {
            level.setBlock(worldPosition, own.setValue(MultiblockPartBlock.FORMED, false), 3);
        }
        setChanged();
        sendUpdate();
    }

    public List<BlockPos> getLinkedParts() {
        return linked;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        net.minecraft.nbt.ListTag list = new net.minecraft.nbt.ListTag();
        linked.forEach(pos -> list.add(LongTag.valueOf(pos.asLong())));
        tag.put("linked", list);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        linked.clear();
        tag.getList("linked", Tag.TAG_LONG).forEach(entry -> linked.add(BlockPos.of(((LongTag) entry).getAsLong())));
    }

    @Override
    protected void saveClientData(CompoundTag tag) {
        super.saveClientData(tag);
        tag.putBoolean("formed", formed);
        tag.putByte("direction", (byte) direction.get3DDataValue());
    }

    @Override
    protected void loadClientData(CompoundTag tag) {
        super.loadClientData(tag);
        formed = tag.getBoolean("formed");
        Direction loaded = Direction.from3DDataValue(tag.getByte("direction"));
        direction = loaded.getAxis().isHorizontal() ? loaded : Direction.SOUTH;
    }
}
