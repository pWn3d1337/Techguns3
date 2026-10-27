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

        public static TagKey<Block> ore(String material) {
            return common("ores/" + material);
        }

        private static TagKey<Block> common(String path) {
            return TagKey.create(Registries.BLOCK, TGIds.of("c", path));
        }
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
