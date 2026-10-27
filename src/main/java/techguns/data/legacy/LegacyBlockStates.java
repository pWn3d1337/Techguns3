package techguns.data.legacy;

import java.util.Map;
import java.util.function.Function;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import techguns.TGIds;

/**
 * Techguns 2 block states (1.12 names, meta enum {@code type} properties) to the flattened Techguns 3 blocks, and the
 * few vanilla 1.12 states the data fixer can't upgrade without their block entity. The
 * {@code type}-like property picks the block; every other legacy property is copied when the new block has a property
 * of that name and the value parses, the rest keep their defaults. Vanilla states are upgraded by the vanilla data
 * fixer before this runs.
 */
final class LegacyBlockStates {
    /** Legacy block name to the new block id, from the value of its variant property (null when it has none). */
    private static final Map<String, Function<String, String>> BLOCKS = Map.ofEntries(
            Map.entry("basicore", type -> type.equals("ore_copper") ? "minecraft:copper_ore" : "techguns:" + type.substring(4) + "_ore"),
            Map.entry("concrete", type -> "techguns:" + type),
            Map.entry("metalpanel", type -> "techguns:" + type),
            Map.entry("nethermetal", type -> "techguns:nethermetal_" + type),
            Map.entry("orecluster", type -> "techguns:ore_cluster_" + type),
            Map.entry("military_crate", type -> "techguns:military_crate_" + switch (type) {
                case "explosives" -> "explosive";
                case "generic" -> "generic_oak";
                default -> type;
            }),
            Map.entry("sand_hard", type -> "techguns:" + type),
            Map.entry("slimy", type -> "techguns:" + type),
            Map.entry("slimyladder", type -> "techguns:slimy_ladder"),
            Map.entry("lamp0", type -> "techguns:lamp_" + type),
            Map.entry("ladder0", type -> "techguns:ladder_" + type),
            Map.entry("camonet", type -> "techguns:camo_net_" + type),
            Map.entry("camonet_top", type -> "techguns:camo_net_top_" + type),
            Map.entry("sandbags", type -> "techguns:sandbags"),
            Map.entry("bunkerdoor", type -> "techguns:bunker_door"),
            Map.entry("door3x3", type -> "techguns:door_" + type + "_3x3"),
            Map.entry("simplemachine", type -> "techguns:" + type),
            Map.entry("stairs_metal", type -> type.equals("true") ? "techguns:steelframe_dark_stairs" : "techguns:panel_large_border_stairs"),
            Map.entry("block_creeper_acid", type -> "techguns:creeper_acid"),
            Map.entry("tg_spawner", type -> type.equals("soldier_spawn") ? "techguns:military_spawn_point" : "techguns:monster_hole"));

    /** The legacy property that selects the block, per legacy block. */
    private static final Map<String, String> VARIANT_PROPERTY = Map.of("lamp0", "lamp_type", "simplemachine", "machinetype", "stairs_metal", "type2");

    private LegacyBlockStates() {
    }

    /** True for 1.12 states this class maps: all Techguns ones and the vanilla ones the data fixer can't do alone. */
    static boolean isLegacyTechguns(CompoundTag state) {
        return state.getString("Name").startsWith("techguns:") || state.getString("Name").equals("minecraft:skull");
    }

    /**
     * 1.12 skulls took their type from the block entity; Techguns 2 placed them without one, so they were skeleton
     * skulls (the data fixer needs the block entity).
     */
    private static BlockState skull(CompoundTag properties) {
        String facing = properties.getString("facing");
        if (facing.isEmpty() || facing.equals("up") || facing.equals("down")) {
            return Blocks.SKELETON_SKULL.defaultBlockState();
        }
        return Blocks.SKELETON_WALL_SKULL.defaultBlockState().setValue(net.minecraft.world.level.block.WallSkullBlock.FACING,
                net.minecraft.core.Direction.byName(facing));
    }

    /** The new block state for a 1.12 Techguns state tag ({@code Name} + {@code Properties}). */
    static BlockState map(CompoundTag legacy) {
        String name = legacy.getString("Name");
        if (name.equals("minecraft:skull")) {
            return skull(legacy.getCompound("Properties"));
        }
        String path = name.substring(name.indexOf(':') + 1);
        Function<String, String> resolver = BLOCKS.get(path);
        if (resolver == null) {
            throw new IllegalArgumentException("No mapping for legacy block " + legacy);
        }
        CompoundTag properties = legacy.getCompound("Properties");
        String variantProperty = VARIANT_PROPERTY.getOrDefault(path, "type");
        String id = resolver.apply(properties.getString(variantProperty));
        int colon = id.indexOf(':');
        Block block = BuiltInRegistries.BLOCK.getOptional(TGIds.of(id.substring(0, colon), id.substring(colon + 1)))
                .orElseThrow(() -> new IllegalArgumentException("Legacy block " + legacy + " maps to unknown " + id));
        BlockState state = block.defaultBlockState();
        for (String key : properties.getAllKeys()) {
            if (key.equals(variantProperty)) {
                continue;
            }
            Property<?> property = block.getStateDefinition().getProperty(key);
            if (property != null) {
                state = withValue(state, property, properties.getString(key));
            }
        }
        return state == null ? Blocks.AIR.defaultBlockState() : state;
    }

    private static <T extends Comparable<T>> BlockState withValue(BlockState state, Property<T> property, String value) {
        return property.getValue(value).map(v -> state.setValue(property, v)).orElse(state);
    }
}
