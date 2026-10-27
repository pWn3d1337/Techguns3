package techguns.registry;

import java.util.function.Supplier;

import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import techguns.Techguns;
import techguns.block.AcidBlock;

/**
 * Techguns fluids: creeper acid (Techguns 2 {@code TGFluids.ACID}), made in the Chem Lab and used to make yellowcake
 * and treated leather; hurts living things that touch it.
 */
public final class TGFluids {
    public static final DeferredRegister<FluidType> FLUID_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, Techguns.MODID);
    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(Registries.FLUID, Techguns.MODID);

    public static final Supplier<FluidType> ACID_TYPE = FLUID_TYPES.register("creeper_acid", () -> new FluidType(FluidType.Properties.create()
            .descriptionId("block.techguns.creeper_acid").density(100).canExtinguish(true)
            .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL).sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)));

    public static final Supplier<FlowingFluid> ACID = FLUIDS.register("creeper_acid", () -> new BaseFlowingFluid.Source(acidProperties()));
    public static final Supplier<FlowingFluid> ACID_FLOWING = FLUIDS.register("flowing_creeper_acid",
            () -> new BaseFlowingFluid.Flowing(acidProperties()));

    public static final DeferredBlock<AcidBlock> ACID_BLOCK = TGBlocks.BLOCKS.register("creeper_acid", () -> new AcidBlock(ACID.get(),
            BlockBehaviour.Properties.ofFullCopy(Blocks.WATER).mapColor(MapColor.COLOR_LIGHT_GREEN)));
    public static final DeferredItem<BucketItem> ACID_BUCKET = TGItems.ITEMS.register("creeper_acid_bucket",
            () -> new BucketItem(ACID.get(), new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)));

    private TGFluids() {
    }

    private static BaseFlowingFluid.Properties acidProperties() {
        return new BaseFlowingFluid.Properties(ACID_TYPE, ACID, ACID_FLOWING).block(ACID_BLOCK).bucket(ACID_BUCKET);
    }

    /** Loads the holders (block and bucket go into the block and item registers) and attaches the fluid registers. */
    public static void init(IEventBus modEventBus) {
        FLUID_TYPES.register(modEventBus);
        FLUIDS.register(modEventBus);
    }
}
