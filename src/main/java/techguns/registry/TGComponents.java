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

    /** Power stored in a powered armor piece; access through {@code TGPoweredArmorItem}. */
    public static final Supplier<DataComponentType<Integer>> ARMOR_POWER = COMPONENTS.registerComponentType("armor_power",
            builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT));

    /** Stacks stored in an Ammo Bag; access through {@code AmmoBagData}. */
    public static final Supplier<DataComponentType<AmmoBagContents>> AMMO_BAG_CONTENTS = COMPONENTS.registerComponentType("ammo_bag_contents",
            builder -> builder.persistent(AmmoBagContents.CODEC).networkSynchronized(AmmoBagContents.STREAM_CODEC));

    private TGComponents() {
    }
}
