package techguns.registry;

import java.util.function.Supplier;

import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.neoforge.registries.DeferredRegister;
import techguns.Techguns;
import techguns.gun.GunAmmo;
import techguns.item.AmmoBagContents;

/**
 * Techguns item data components (Codec + StreamCodec). Read and write them through typed accessors
 * such as {@code GunData}, never via scattered {@code stack.get(...)} calls.
 */
public final class TGComponents {
    public static final DeferredRegister.DataComponents COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, Techguns.MODID);

    public static final Supplier<DataComponentType<GunAmmo>> GUN_AMMO = COMPONENTS.registerComponentType("gun_ammo",
            builder -> builder.persistent(GunAmmo.CODEC).networkSynchronized(GunAmmo.STREAM_CODEC));

    /** Mining head level of melee tools (index into the tool's head list). */
    public static final Supplier<DataComponentType<Integer>> MINING_HEAD = COMPONENTS.registerComponentType("mining_head",
            builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT));

    /** Camo name of a camo-able item (see {@code CamoData}); absent = default camo. */
    public static final Supplier<DataComponentType<String>> CAMO = COMPONENTS.registerComponentType("camo",
            builder -> builder.persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8));

    /** Power stored in a powered armor piece; access through {@code TGPoweredArmorItem}. */
    public static final Supplier<DataComponentType<Integer>> ARMOR_POWER = COMPONENTS.registerComponentType("armor_power",
            builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT));

    /** Stacks stored in an Ammo Bag; access through {@code AmmoBagData}. */
    public static final Supplier<DataComponentType<AmmoBagContents>> AMMO_BAG_CONTENTS = COMPONENTS.registerComponentType("ammo_bag_contents",
            builder -> builder.persistent(AmmoBagContents.CODEC).networkSynchronized(AmmoBagContents.STREAM_CODEC));

    /** Food points of the last meal an Auto-Feeder fed that didn't fit into the food bar yet; access through {@code AutoFeederItem}. */
    public static final Supplier<DataComponentType<Integer>> AUTO_FEEDER_FOOD_LEFT = COMPONENTS.registerComponentType("auto_feeder_food_left",
            builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT));

    /** First corner a debug tool's area selection; access through {@code DebugToolData}. */
    public static final Supplier<DataComponentType<net.minecraft.core.BlockPos>> DEBUG_CORNER = COMPONENTS.registerComponentType("debug_corner",
            builder -> builder.persistent(net.minecraft.core.BlockPos.CODEC).networkSynchronized(net.minecraft.core.BlockPos.STREAM_CODEC));

    /** Mode of the world gen test tool; access through {@code DebugToolData}. */
    public static final Supplier<DataComponentType<Integer>> DEBUG_MODE = COMPONENTS.registerComponentType("debug_mode",
            builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT));

    private TGComponents() {
    }
}
