package techguns.registry;

import java.util.function.Supplier;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import techguns.Techguns;
import techguns.machine.AmmoPressBlockEntity;
import techguns.machine.BlastFurnaceBlockEntity;
import techguns.machine.GrinderBlockEntity;
import techguns.machine.MetalPressBlockEntity;
import techguns.machine.TGMachineBlockEntity;

/** Block entity types, and the item and energy capabilities of the machines. */
public final class TGBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Techguns.MODID);

    // MIGRATION(26.x): BlockEntityType.Builder is replaced by the BlockEntityType constructor in 1.21.2+.
    @SuppressWarnings("DataFlowIssue")
    public static final Supplier<BlockEntityType<MetalPressBlockEntity>> METAL_PRESS = BLOCK_ENTITY_TYPES.register("metal_press",
            () -> BlockEntityType.Builder.of(MetalPressBlockEntity::new, TGBlocks.METAL_PRESS.get()).build(null));

    @SuppressWarnings("DataFlowIssue")
    public static final Supplier<BlockEntityType<AmmoPressBlockEntity>> AMMO_PRESS = BLOCK_ENTITY_TYPES.register("ammo_press",
            () -> BlockEntityType.Builder.of(AmmoPressBlockEntity::new, TGBlocks.AMMO_PRESS.get()).build(null));
    @SuppressWarnings("DataFlowIssue")
    public static final Supplier<BlockEntityType<BlastFurnaceBlockEntity>> BLAST_FURNACE = BLOCK_ENTITY_TYPES.register("blast_furnace",
            () -> BlockEntityType.Builder.of(BlastFurnaceBlockEntity::new, TGBlocks.BLAST_FURNACE.get()).build(null));
    @SuppressWarnings("DataFlowIssue")
    public static final Supplier<BlockEntityType<GrinderBlockEntity>> GRINDER = BLOCK_ENTITY_TYPES.register("grinder",
            () -> BlockEntityType.Builder.of(GrinderBlockEntity::new, TGBlocks.GRINDER.get()).build(null));

    private TGBlockEntities() {
    }

    public static void init(IEventBus modEventBus) {
        modEventBus.addListener(TGBlockEntities::registerCapabilities);
    }

    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        machine(event, METAL_PRESS.get());
        machine(event, AMMO_PRESS.get());
        machine(event, BLAST_FURNACE.get());
        machine(event, GRINDER.get());
    }

    private static <T extends TGMachineBlockEntity> void machine(RegisterCapabilitiesEvent event, BlockEntityType<T> type) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, type, (machine, side) -> machine.getAutomationHandler());
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, type, (machine, side) -> machine.getEnergy());
    }
}
