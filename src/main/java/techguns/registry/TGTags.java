package techguns.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import techguns.TGIds;

/**
 * Tag keys. Materials use the common {@code c:} conventions, e.g. {@code c:ingots/tin}.
 */
public final class TGTags {
    private TGTags() {
    }

    public static final class Blocks {
        private Blocks() {
        }

        /** Ore Drill targets (Techguns 2 {@code TGOreClusters}); each has an {@code ore_cluster} recipe. */
        public static final TagKey<Block> ORE_CLUSTERS = TagKey.create(Registries.BLOCK, TGIds.id("ore_clusters"));

        public static TagKey<Block> ore(String material) {
            return common("ores/" + material);
        }

        private static TagKey<Block> common(String path) {
            return TagKey.create(Registries.BLOCK, TGIds.of("c", path));
        }
    }

    public static final class Fluids {
        private Fluids() {
        }

        /**
         * Techguns 2 used Thermal Expansion's liquid redstone and liquid ender and fell back to lava without it; the tags
         * hold lava and the common {@code c:} tags.
         */
        public static final TagKey<net.minecraft.world.level.material.Fluid> LIQUID_REDSTONE = TagKey.create(Registries.FLUID,
                TGIds.id("liquid_redstone"));
        public static final TagKey<net.minecraft.world.level.material.Fluid> LIQUID_ENDER = TagKey.create(Registries.FLUID, TGIds.id("liquid_ender"));
        /** Liquid fuels the Ore Drill burns (Techguns 2 {@code fluidListFuel}); lava always works. */
        public static final TagKey<net.minecraft.world.level.material.Fluid> DRILL_FUEL = TagKey.create(Registries.FLUID, TGIds.id("drill_fuel"));
        /** What the oil cluster pumps; Techguns 2 used the Forge {@code oil} fluid, here the common oil tags. */
        public static final TagKey<net.minecraft.world.level.material.Fluid> DRILL_OIL = TagKey.create(Registries.FLUID, TGIds.id("drill_oil"));
    }

    public static final class Items {
        private Items() {
        }

        /** Items that Ammo Bags accept: all ammo items, full and empty (magazines, cells, tanks, rounds, rockets). */
        public static final TagKey<Item> AMMO = TagKey.create(Registries.ITEM, TGIds.id("ammo"));
        /** Fabricator slots (Techguns 2 {@code FabricatorRecipe.items_wireslot/powderslot/plateslot}). */
        public static final TagKey<Item> FABRICATOR_WIRES = TagKey.create(Registries.ITEM, TGIds.id("fabricator_wires"));
        public static final TagKey<Item> FABRICATOR_POWDERS = TagKey.create(Registries.ITEM, TGIds.id("fabricator_powders"));
        public static final TagKey<Item> FABRICATOR_PLATES = TagKey.create(Registries.ITEM, TGIds.id("fabricator_plates"));
        /** Beam focuses for the Reaction Chamber focus slot (Techguns 2 {@code ReactionBeamFocus}). */
        public static final TagKey<Item> REACTION_CHAMBER_FOCUSES = TagKey.create(Registries.ITEM, TGIds.id("reaction_chamber_focuses"));
        /** Ammo Bench inputs (Fabric tags). */
        public static final TagKey<Item> BULLET_CORE = TagKey.create(Registries.ITEM, TGIds.id("bullet_metal_core"));
        public static final TagKey<Item> BULLET_CASING = TagKey.create(Registries.ITEM, TGIds.id("bullet_metal_casing"));
        public static final TagKey<Item> BULLET_POWDER = TagKey.create(Registries.ITEM, TGIds.id("bullet_powder"));

        public static TagKey<Item> ore(String material) {
            return common("ores/" + material);
        }

        public static TagKey<Item> rawMaterial(String material) {
            return common("raw_materials/" + material);
        }

        public static TagKey<Item> ingot(String material) {
            return common("ingots/" + material);
        }

        public static TagKey<Item> nugget(String material) {
            return common("nuggets/" + material);
        }

        public static TagKey<Item> plate(String material) {
            return common("plates/" + material);
        }

        /** Parent tag of all {@link #plate} tags. */
        public static final TagKey<Item> PLATES = common("plates");

        private static TagKey<Item> common(String path) {
            return TagKey.create(Registries.ITEM, TGIds.of("c", path));
        }
    }
}
