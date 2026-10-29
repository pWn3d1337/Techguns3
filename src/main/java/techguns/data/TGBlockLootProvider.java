package techguns.data;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.NestedLootTable;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import techguns.TGIds;
import techguns.block.Door3x3Block;
import techguns.block.MilitaryCrateBlock;
import techguns.registry.TGAmmo;
import techguns.registry.TGArmors;
import techguns.registry.TGBlocks;
import techguns.registry.TGGuns;
import techguns.registry.TGItems;

public class TGBlockLootProvider extends BlockLootSubProvider {
    public TGBlockLootProvider(HolderLookup.Provider registries) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
    }

    @Override
    protected void generate() {
        for (var block : java.util.List.of(TGBlocks.AIR_MARKER, TGBlocks.ANTI_AIR_MARKER, TGBlocks.INTERIOR_MARKER, TGBlocks.DUNGEON_SCANNER,
                TGBlocks.DUNGEON_GENERATOR)) {
            dropSelf(block.get());
        }
        add(TGBlocks.BIOBLOB.get(), noDrop());
        add(TGBlocks.MONSTER_HOLE.get(), noDrop());
        add(TGBlocks.MILITARY_SPAWN_POINT.get(), noDrop());
        TGBlocks.cubeBlocks().forEach(block -> dropSelf(block.get()));
        TGBlocks.stairBlocks().forEach(block -> dropSelf(block.get()));
        add(TGBlocks.TIN_ORE.get(), block -> createOreDrop(block, TGItems.RAW_TIN.get()));
        add(TGBlocks.DEEPSLATE_TIN_ORE.get(), block -> createOreDrop(block, TGItems.RAW_TIN.get()));
        add(TGBlocks.LEAD_ORE.get(), block -> createOreDrop(block, TGItems.RAW_LEAD.get()));
        add(TGBlocks.DEEPSLATE_LEAD_ORE.get(), block -> createOreDrop(block, TGItems.RAW_LEAD.get()));
        add(TGBlocks.TITANIUM_ORE.get(), block -> createOreDrop(block, TGItems.RAW_TITANIUM.get()));
        add(TGBlocks.DEEPSLATE_TITANIUM_ORE.get(), block -> createOreDrop(block, TGItems.RAW_TITANIUM.get()));
        add(TGBlocks.URANIUM_ORE.get(), block -> createOreDrop(block, TGItems.RAW_URANIUM.get()));
        add(TGBlocks.DEEPSLATE_URANIUM_ORE.get(), block -> createOreDrop(block, TGItems.RAW_URANIUM.get()));

        // Phase 5a2: shaped decorative blocks
        TGBlocks.LAMPS.forEach(block -> dropSelf(block.get()));
        TGBlocks.LANTERNS.forEach(block -> dropSelf(block.get()));
        TGBlocks.LADDERS.forEach(block -> dropSelf(block.get()));
        dropSelf(TGBlocks.SLIMY_LADDER.get());
        TGBlocks.CAMO_NETS.forEach(block -> dropSelf(block.get()));
        TGBlocks.CAMO_NETS_TOP.forEach(block -> dropSelf(block.get()));
        dropSelf(TGBlocks.SANDBAGS.get());
        dropSelf(TGBlocks.METAL_PRESS.get());
        dropSelf(TGBlocks.AMMO_PRESS.get());
        dropSelf(TGBlocks.BLAST_FURNACE.get());
        dropSelf(TGBlocks.GRINDER.get());
        dropSelf(TGBlocks.CHEM_LAB.get());
        dropSelf(TGBlocks.CHARGING_STATION.get());
        dropSelf(TGBlocks.REPAIR_BENCH.get());
        dropSelf(TGBlocks.CAMO_BENCH.get());
        dropSelf(TGBlocks.UPGRADE_BENCH.get());
        dropSelf(TGBlocks.AMMO_BENCH.get());
        dropSelf(TGBlocks.FABRICATOR_HOUSING.get());
        dropSelf(TGBlocks.FABRICATOR_GLASS.get());
        dropSelf(TGBlocks.FABRICATOR_CONTROLLER.get());
        dropSelf(TGBlocks.REACTION_CHAMBER_HOUSING.get());
        dropSelf(TGBlocks.REACTION_CHAMBER_GLASS.get());
        dropSelf(TGBlocks.REACTION_CHAMBER_CONTROLLER.get());
        dropSelf(TGBlocks.ORE_DRILL_FRAME.get());
        dropSelf(TGBlocks.ORE_DRILL_SCAFFOLD.get());
        dropSelf(TGBlocks.ORE_DRILL_ROD.get());
        dropSelf(TGBlocks.ORE_DRILL_ENGINE.get());
        dropSelf(TGBlocks.ORE_DRILL_CONTROLLER.get());
        dropSelf(TGBlocks.TURRET.get());
        dropSelf(TGBlocks.EXPLOSIVE_CHARGE.get());
        dropSelf(TGBlocks.EXPLOSIVE_CHARGE_ADVANCED.get());
        add(TGBlocks.BUNKER_DOOR.get(), this::createDoorTable);
        // a 3x3 door drops one item, from its centre block (breaking another part drops it through the centre, see Door3x3Block)
        TGBlocks.DOORS_3X3.forEach(door -> add(door.get(), this::doorMasterDrop));
        crateLoot();
    }

    private LootTable.Builder doorMasterDrop(Block door) {
        return LootTable.lootTable().withPool(applyExplosionCondition(door, LootPool.lootPool().setRolls(ConstantValue.exactly(1))
                .add(LootItem.lootTableItem(door))
                .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(door).setProperties(StatePropertiesPredicate.Builder.properties()
                        .hasProperty(Door3x3Block.COL, 1).hasProperty(Door3x3Block.ROW, 1)))));
    }

    // --- military crates (Techguns 2 loot_tables/blocks/military_crate_*.json) ---

    private void crateLoot() {
        crateContents().keySet().forEach(crate -> add(crate.get(), crateTable(crate)));
    }

    /** The crate contents table, also used by structure chests (Techguns 2 nested the crate tables there). */
    public static ResourceKey<LootTable> crateContentsTable(DeferredBlock<? extends Block> crate) {
        return ResourceKey.create(Registries.LOOT_TABLE, TGIds.id("crates/" + crate.getId().getPath()));
    }

    /** Techguns 2 crate loot, one pool per crate; generated as {@code techguns:crates/*} by {@link Crates}. */
    static Map<DeferredBlock<MilitaryCrateBlock>, LootPool.Builder> crateContents() {
        LootPool.Builder ammo = pool(
                entry(TGAmmo.PISTOL_ROUNDS.get(), 2, 1, 4), entry(TGAmmo.SHOTGUN_ROUNDS.get(), 2, 4, 8), entry(TGAmmo.RIFLE_ROUNDS.get(), 2, 1, 2),
                entry(TGAmmo.SNIPER_ROUNDS.get(), 1, 1, 2), entry(TGAmmo.SMG_MAGAZINE.get(), 2, 1, 2), entry(TGAmmo.PISTOL_MAGAZINE.get(), 1, 1, 2),
                entry(TGAmmo.ASSAULT_RIFLE_MAGAZINE.get(), 2, 1, 3), entry(TGAmmo.PISTOL_ROUNDS_INCENDIARY.get(), 1, 1, 2),
                entry(TGAmmo.SHOTGUN_ROUNDS_INCENDIARY.get(), 1, 3, 6), entry(TGAmmo.RIFLE_ROUNDS_INCENDIARY.get(), 1, 1, 2),
                entry(TGAmmo.SNIPER_ROUNDS_INCENDIARY.get(), 1, 1, 1), entry(TGAmmo.SMG_MAGAZINE_INCENDIARY.get(), 1, 1, 2),
                entry(TGAmmo.PISTOL_MAGAZINE_INCENDIARY.get(), 1, 1, 2), entry(TGAmmo.ASSAULT_RIFLE_MAGAZINE_INCENDIARY.get(), 1, 1, 2))
                .setBonusRolls(UniformGenerator.between(0, 1));
        LootPool.Builder guns = pool(
                entry(TGGuns.REVOLVER.get(), 1), entry(TGGuns.THOMPSON.get(), 1), entry(TGGuns.AK47.get(), 1), entry(TGGuns.BOLT_ACTION.get(), 1),
                entry(TGGuns.M4.get(), 1), entry(TGGuns.PISTOL.get(), 1), entry(TGGuns.COMBAT_SHOTGUN.get(), 1), entry(TGGuns.MAC10.get(), 1),
                entry(TGGuns.AUG.get(), 1), entry(TGGuns.FLAMETHROWER.get(), 1));
        LootPool.Builder armor = pool(
                entry(TGArmors.T1_COMBAT_HELMET.get(), 1), entry(TGArmors.T1_COMBAT_CHESTPLATE.get(), 1),
                entry(TGArmors.T1_COMBAT_LEGGINGS.get(), 1), entry(TGArmors.T1_COMBAT_BOOTS.get(), 1),
                entry(TGArmors.T2_COMBAT_HELMET.get(), 1), entry(TGArmors.T2_COMBAT_CHESTPLATE.get(), 1),
                entry(TGArmors.T2_COMBAT_LEGGINGS.get(), 1), entry(TGArmors.T2_COMBAT_BOOTS.get(), 1),
                entry(TGArmors.T2_COMMANDO_HELMET.get(), 1), entry(TGArmors.T2_COMMANDO_CHESTPLATE.get(), 1),
                entry(TGArmors.T2_COMMANDO_LEGGINGS.get(), 1), entry(TGArmors.T2_COMMANDO_BOOTS.get(), 1));
        LootPool.Builder medical = pool(
                entry(Items.APPLE, 1, 1, 3), entry(Items.BREAD, 1, 1, 3), entry(Items.COOKED_BEEF, 1, 1, 2),
                entry(TGItems.RAD_AWAY.get(), 2, 1, 4), entry(TGItems.RAD_PILLS.get(), 2, 1, 4));
        LootPool.Builder explosives = pool(
                entry(TGAmmo.ROCKET.get(), 2, 1, 3), entry(TGAmmo.GRENADE_40MM.get(), 2, 1, 4), entry(TGGuns.STIELGRANATE.get(), 2, 1, 3),
                entry(TGGuns.FRAG_GRENADE.get(), 2, 1, 2), entry(TGGuns.ROCKET_LAUNCHER.get(), 1), entry(TGGuns.GRENADE_LAUNCHER.get(), 1));

        Map<DeferredBlock<MilitaryCrateBlock>, LootPool.Builder> contents = new LinkedHashMap<>();
        contents.put(TGBlocks.MILITARY_CRATE_AMMO, ammo);
        contents.put(TGBlocks.MILITARY_CRATE_GUN, guns);
        contents.put(TGBlocks.MILITARY_CRATE_ARMOR, armor);
        contents.put(TGBlocks.MILITARY_CRATE_MEDICAL, medical);
        contents.put(TGBlocks.MILITARY_CRATE_EXPLOSIVE, explosives);
        // the four supply crates share one pool
        for (int type = 5; type < 9; type++) {
            contents.put(TGBlocks.MILITARY_CRATES.get(type), genericPool());
        }
        return contents;
    }

    private static LootPool.Builder genericPool() {
        return pool(
                entry(Items.IRON_INGOT, 10, 1, 1), entry(Items.REDSTONE, 10, 1, 2), entry(Items.COAL, 10, 1, 2), entry(Items.GUNPOWDER, 10, 1, 2),
                entry(Items.GOLD_INGOT, 5, 1, 1), entry(Items.DIAMOND, 1), entry(Items.ENDER_PEARL, 1),
                entry(TGItems.HEAVY_CLOTH.get(), 10, 1, 3), entry(TGItems.MECHANICAL_PARTS_IRON.get(), 10, 1, 2),
                entry(TGItems.MECHANICAL_PARTS_OBSIDIAN_STEEL.get(), 5, 1, 1), entry(TGItems.PLASTIC_SHEET.get(), 5, 1, 1),
                entry(TGItems.RUBBER_BAR.get(), 5, 1, 1), entry(TGItems.OBSIDIAN_STEEL_INGOT.get(), 5, 1, 1));
    }

    /** The crate itself with Silk Touch, otherwise its contents table (Techguns 2 dropped only the loot). */
    private LootTable.Builder crateTable(DeferredBlock<? extends Block> crate) {
        return LootTable.lootTable()
                .withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1)).add(LootItem.lootTableItem(crate.get())).when(hasSilkTouch()))
                .withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1)).add(NestedLootTable.lootTableReference(crateContentsTable(crate)))
                        .when(hasSilkTouch().invert()));
    }

    /** The crate contents tables. */
    public static class Crates implements LootTableSubProvider {
        @SuppressWarnings("unused")
        public Crates(HolderLookup.Provider registries) {
        }

        @Override
        public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> output) {
            crateContents().forEach((crate, pool) -> output.accept(crateContentsTable(crate), LootTable.lootTable().withPool(pool)));
        }
    }

    private static LootPool.Builder pool(LootPoolSingletonContainer.Builder<?>... entries) {
        LootPool.Builder pool = LootPool.lootPool().setRolls(ConstantValue.exactly(1));
        for (LootPoolSingletonContainer.Builder<?> entry : entries) {
            pool.add(entry);
        }
        return pool;
    }

    private static LootPoolSingletonContainer.Builder<?> entry(ItemLike item, int weight) {
        return LootItem.lootTableItem(item).setWeight(weight);
    }

    private static LootPoolSingletonContainer.Builder<?> entry(ItemLike item, int weight, int min, int max) {
        return LootItem.lootTableItem(item).setWeight(weight).apply(SetItemCountFunction.setCount(UniformGenerator.between(min, max)));
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return TGBlocks.BLOCKS.getEntries().stream().map(DeferredHolder::value).map(Block.class::cast)::iterator;
    }
}
