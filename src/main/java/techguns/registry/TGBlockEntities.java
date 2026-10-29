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
import techguns.machine.ChargingStationBlockEntity;
import techguns.machine.ChemLabBlockEntity;
import techguns.machine.GrinderBlockEntity;
import techguns.machine.CamoBenchBlockEntity;
import techguns.machine.FabricatorBlockEntity;
import techguns.machine.ReactionChamberBlockEntity;
import techguns.multiblock.MultiblockMasterBlockEntity;
import techguns.multiblock.MultiblockPartBlockEntity;
import techguns.machine.MetalPressBlockEntity;
import techguns.machine.RepairBenchBlockEntity;
import techguns.machine.UpgradeBenchBlockEntity;
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

    @SuppressWarnings("DataFlowIssue")
    public static final Supplier<BlockEntityType<ChemLabBlockEntity>> CHEM_LAB = BLOCK_ENTITY_TYPES.register("chem_lab",
            () -> BlockEntityType.Builder.of(ChemLabBlockEntity::new, TGBlocks.CHEM_LAB.get()).build(null));
    @SuppressWarnings("DataFlowIssue")
    public static final Supplier<BlockEntityType<ChargingStationBlockEntity>> CHARGING_STATION = BLOCK_ENTITY_TYPES.register("charging_station",
            () -> BlockEntityType.Builder.of(ChargingStationBlockEntity::new, TGBlocks.CHARGING_STATION.get()).build(null));

    @SuppressWarnings("DataFlowIssue")
    public static final Supplier<BlockEntityType<RepairBenchBlockEntity>> REPAIR_BENCH = BLOCK_ENTITY_TYPES.register("repair_bench",
            () -> BlockEntityType.Builder.of(RepairBenchBlockEntity::new, TGBlocks.REPAIR_BENCH.get()).build(null));
    @SuppressWarnings("DataFlowIssue")
    public static final Supplier<BlockEntityType<CamoBenchBlockEntity>> CAMO_BENCH = BLOCK_ENTITY_TYPES.register("camo_bench",
            () -> BlockEntityType.Builder.of(CamoBenchBlockEntity::new, TGBlocks.CAMO_BENCH.get()).build(null));
    @SuppressWarnings("DataFlowIssue")
    public static final Supplier<BlockEntityType<UpgradeBenchBlockEntity>> UPGRADE_BENCH = BLOCK_ENTITY_TYPES.register("upgrade_bench",
            () -> BlockEntityType.Builder.of(UpgradeBenchBlockEntity::new, TGBlocks.UPGRADE_BENCH.get()).build(null));

    @SuppressWarnings("DataFlowIssue")
    public static final Supplier<BlockEntityType<FabricatorBlockEntity>> FABRICATOR = BLOCK_ENTITY_TYPES.register("fabricator",
            () -> BlockEntityType.Builder.of(FabricatorBlockEntity::new, TGBlocks.FABRICATOR_CONTROLLER.get()).build(null));
    /** All multiblock structure blocks (housings, glass, connectors). */
    @SuppressWarnings("DataFlowIssue")
    public static final Supplier<BlockEntityType<MultiblockPartBlockEntity>> MULTIBLOCK_PART = BLOCK_ENTITY_TYPES.register("multiblock_part",
            () -> BlockEntityType.Builder.of(MultiblockPartBlockEntity::new, TGBlocks.FABRICATOR_HOUSING.get(), TGBlocks.FABRICATOR_GLASS.get(),
                    TGBlocks.REACTION_CHAMBER_HOUSING.get(), TGBlocks.REACTION_CHAMBER_GLASS.get(), TGBlocks.ORE_DRILL_FRAME.get(),
                    TGBlocks.ORE_DRILL_SCAFFOLD.get(), TGBlocks.ORE_DRILL_ROD.get(), TGBlocks.ORE_DRILL_ENGINE.get()).build(null));
    @SuppressWarnings("DataFlowIssue")
    public static final Supplier<BlockEntityType<ReactionChamberBlockEntity>> REACTION_CHAMBER = BLOCK_ENTITY_TYPES.register("reaction_chamber",
            () -> BlockEntityType.Builder.of(ReactionChamberBlockEntity::new, TGBlocks.REACTION_CHAMBER_CONTROLLER.get()).build(null));
    @SuppressWarnings("DataFlowIssue")
    public static final Supplier<BlockEntityType<techguns.machine.OreDrillBlockEntity>> ORE_DRILL = BLOCK_ENTITY_TYPES.register("ore_drill",
            () -> BlockEntityType.Builder.of(techguns.machine.OreDrillBlockEntity::new, TGBlocks.ORE_DRILL_CONTROLLER.get()).build(null));

    @SuppressWarnings("DataFlowIssue")
    public static final Supplier<BlockEntityType<techguns.machine.TurretBlockEntity>> TURRET = BLOCK_ENTITY_TYPES.register("turret",
            () -> BlockEntityType.Builder.of(techguns.machine.TurretBlockEntity::new, TGBlocks.TURRET.get()).build(null));
    @SuppressWarnings("DataFlowIssue")
    public static final Supplier<BlockEntityType<techguns.machine.ExplosiveChargeBlockEntity>> EXPLOSIVE_CHARGE = BLOCK_ENTITY_TYPES.register(
            "explosive_charge", () -> BlockEntityType.Builder.of(techguns.machine.ExplosiveChargeBlockEntity::new, TGBlocks.EXPLOSIVE_CHARGE.get(),
                    TGBlocks.EXPLOSIVE_CHARGE_ADVANCED.get()).build(null));

    @SuppressWarnings("DataFlowIssue")
    public static final Supplier<BlockEntityType<techguns.entity.spawn.TGSpawnerBlockEntity>> TG_SPAWNER = BLOCK_ENTITY_TYPES.register("tg_spawner",
            () -> BlockEntityType.Builder.of(techguns.entity.spawn.TGSpawnerBlockEntity::new, TGBlocks.MONSTER_HOLE.get(),
                    TGBlocks.MILITARY_SPAWN_POINT.get()).build(null));

    /** On the master (centre) block of the 3x3 doors. */
    @SuppressWarnings("DataFlowIssue")
    public static final Supplier<BlockEntityType<techguns.block.Door3x3BlockEntity>> DOOR_3X3 = BLOCK_ENTITY_TYPES.register("door_3x3",
            () -> BlockEntityType.Builder.of(techguns.block.Door3x3BlockEntity::new,
                    TGBlocks.DOORS_3X3.stream().map(door -> (net.minecraft.world.level.block.Block) door.get())
                            .toArray(net.minecraft.world.level.block.Block[]::new)).build(null));

    @SuppressWarnings("DataFlowIssue")
    public static final Supplier<BlockEntityType<techguns.debug.DungeonScannerBlockEntity>> DUNGEON_SCANNER = BLOCK_ENTITY_TYPES.register(
            "dungeon_scanner", () -> BlockEntityType.Builder.of(techguns.debug.DungeonScannerBlockEntity::new, TGBlocks.DUNGEON_SCANNER.get()).build(null));
    @SuppressWarnings("DataFlowIssue")
    public static final Supplier<BlockEntityType<techguns.debug.DungeonGeneratorBlockEntity>> DUNGEON_GENERATOR = BLOCK_ENTITY_TYPES.register(
            "dungeon_generator", () -> BlockEntityType.Builder.of(techguns.debug.DungeonGeneratorBlockEntity::new, TGBlocks.DUNGEON_GENERATOR.get())
                    .build(null));

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
        machine(event, CHEM_LAB.get());
        machine(event, CHARGING_STATION.get());
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, CHEM_LAB.get(), (lab, side) -> lab.getFluidHandler());
        machine(event, FABRICATOR.get());
        machine(event, REACTION_CHAMBER.get());
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, REACTION_CHAMBER.get(), (chamber, side) -> chamber.getMultiblockFluidHandler());
        machine(event, ORE_DRILL.get());
        machine(event, TURRET.get());
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, ORE_DRILL.get(), (drill, side) -> drill.getMultiblockFluidHandler());
        multiblockParts(event);
        // benches have no power; hoppers can fill the Repair Bench materials
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, REPAIR_BENCH.get(), (bench, side) -> bench.getAutomationHandler());
    }

    /** Linked parts expose their master's handlers by connector type (Techguns 2 slave capabilities). */
    private static void multiblockParts(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, MULTIBLOCK_PART.get(), (part, side) -> {
            MultiblockMasterBlockEntity master = part.master();
            return master != null && part.getConnector().items() ? master.getAutomationHandler() : null;
        });
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, MULTIBLOCK_PART.get(), (part, side) -> {
            MultiblockMasterBlockEntity master = part.master();
            return master != null && part.getConnector().energy() ? master.getEnergy() : null;
        });
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, MULTIBLOCK_PART.get(), (part, side) -> {
            MultiblockMasterBlockEntity master = part.master();
            return master != null && part.getConnector().fluids() ? master.getMultiblockFluidHandler() : null;
        });
    }

    private static <T extends TGMachineBlockEntity> void machine(RegisterCapabilitiesEvent event, BlockEntityType<T> type) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, type, (machine, side) -> machine.getAutomationHandler());
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, type, (machine, side) -> machine.getEnergy());
    }
}
