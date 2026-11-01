package techgunsdev;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import com.mojang.serialization.MapCodec;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.Nullable;

/**
 * Dev-only helper mod (source set {@code dev}, see build.gradle): an infinite Forge Energy source for testing the
 * Techguns machines. It is on every dev run (client, server, GameTests) but never in the Techguns jar or its
 * published dependencies.
 */
@Mod(TechgunsDev.MOD_ID)
public final class TechgunsDev {
    public static final String MOD_ID = "techguns_dev";

    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MOD_ID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MOD_ID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MOD_ID);

    public static final DeferredBlock<CreativePowerBlock> CREATIVE_POWER = BLOCKS.register("creative_power",
            () -> new CreativePowerBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_RED)
                    .strength(1.0F).noLootTable().lightLevel(state -> 7)));
    public static final DeferredHolder<Item, BlockItem> CREATIVE_POWER_ITEM = ITEMS.registerSimpleBlockItem(CREATIVE_POWER);
    @SuppressWarnings("DataFlowIssue") // the data fixer type may be null
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CreativePowerBlockEntity>> CREATIVE_POWER_BE =
            BLOCK_ENTITIES.register("creative_power",
                    () -> BlockEntityType.Builder.of(CreativePowerBlockEntity::new, CREATIVE_POWER.get()).build(null));

    public TechgunsDev(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        BLOCK_ENTITIES.register(modBus);
        modBus.addListener(TechgunsDev::registerCapabilities);
        modBus.addListener(TechgunsDev::addToCreativeTab);
    }

    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, CREATIVE_POWER_BE.get(), (be, side) -> InfiniteEnergy.INSTANCE);
    }

    private static void addToCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.OP_BLOCKS || event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.accept(CREATIVE_POWER_ITEM.get());
        }
    }

    /** Pushes as much FE as its neighbours take, every tick, on all six sides. */
    public static final class CreativePowerBlock extends BaseEntityBlock {
        private static final MapCodec<CreativePowerBlock> CODEC = simpleCodec(CreativePowerBlock::new);

        public CreativePowerBlock(Properties properties) {
            super(properties);
        }

        @Override
        protected MapCodec<? extends BaseEntityBlock> codec() {
            return CODEC;
        }

        @Override
        protected RenderShape getRenderShape(BlockState state) {
            return RenderShape.MODEL;
        }

        @Override
        public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
            return new CreativePowerBlockEntity(pos, state);
        }

        @Override
        public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
            return level.isClientSide ? null : createTickerHelper(type, CREATIVE_POWER_BE.get(), CreativePowerBlockEntity::serverTick);
        }
    }

    public static final class CreativePowerBlockEntity extends BlockEntity {
        public CreativePowerBlockEntity(BlockPos pos, BlockState state) {
            super(CREATIVE_POWER_BE.get(), pos, state);
        }

        static void serverTick(Level level, BlockPos pos, BlockState state, CreativePowerBlockEntity be) {
            for (Direction dir : Direction.values()) {
                IEnergyStorage target = level.getCapability(Capabilities.EnergyStorage.BLOCK, pos.relative(dir), dir.getOpposite());
                if (target != null && target != InfiniteEnergy.INSTANCE && target.canReceive()) {
                    target.receiveEnergy(Integer.MAX_VALUE, false);
                }
            }
        }
    }

    /** Always full: gives any amount, swallows any amount (so cables and machines can also pull from it). */
    private static final class InfiniteEnergy implements IEnergyStorage {
        static final InfiniteEnergy INSTANCE = new InfiniteEnergy();

        @Override
        public int receiveEnergy(int toReceive, boolean simulate) {
            return toReceive;
        }

        @Override
        public int extractEnergy(int toExtract, boolean simulate) {
            return toExtract;
        }

        @Override
        public int getEnergyStored() {
            return Integer.MAX_VALUE;
        }

        @Override
        public int getMaxEnergyStored() {
            return Integer.MAX_VALUE;
        }

        @Override
        public boolean canExtract() {
            return true;
        }

        @Override
        public boolean canReceive() {
            return true;
        }
    }
}
