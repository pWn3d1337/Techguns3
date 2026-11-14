package techguns.machine;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import techguns.TGConfig;
import techguns.TGIds;
import techguns.registry.TGTags;

/**
 * Liquid fuels of the Liquid Fuel Generator and the Ore Drill: furnace burn ticks per millibucket (a lava bucket burns
 * 20000 ticks in a furnace, so lava is 20). A fluid data map, so datapacks and compat can add fuels; fluids in the tag
 * {@code techguns:fuel} without an entry use the config fallback {@code oreDrillFuelValueFuel}.
 */
public record FluidFuel(float burnPerMb) {
    public static final Codec<FluidFuel> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.floatRange(0.0f, 1.0e6f).fieldOf("burn_per_mb").forGetter(FluidFuel::burnPerMb)
    ).apply(instance, FluidFuel::new));

    /** {@code techguns:fluid_fuel}, synced for the screens and JEI. */
    public static final DataMapType<Fluid, FluidFuel> DATA_MAP = DataMapType.builder(TGIds.id("fluid_fuel"), Registries.FLUID, CODEC)
            .synced(CODEC, false).build();

    /** Burn ticks per millibucket of {@code stack}'s fluid, 0 if it is no fuel. */
    public static float burnPerMb(FluidStack stack) {
        if (stack.isEmpty()) {
            return 0.0f;
        }
        FluidFuel data = stack.getFluidHolder().getData(DATA_MAP);
        if (data != null) {
            return data.burnPerMb();
        }
        return stack.is(TGTags.Fluids.FUEL) ? TGConfig.ORE_DRILL_FUEL_VALUE_FUEL.get().floatValue() : 0.0f;
    }
}
