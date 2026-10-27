package techguns.data;

import java.util.stream.Stream;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.EntityLootSubProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.EnchantedCountIncreaseFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import techguns.registry.TGAmmo;
import techguns.registry.TGItems;
import techguns.registry.TGNpcs;

/**
 * NPC loot tables, from the Techguns 2 {@code loot_tables/entities} files: one roll per pool, each pool one item
 * with a count range and (mostly) a looting bonus.
 */
public class TGEntityLootProvider extends EntityLootSubProvider {
    public TGEntityLootProvider(HolderLookup.Provider registries) {
        super(FeatureFlags.REGISTRY.allFlags(), registries);
    }

    @Override
    public void generate() {
        add(TGNpcs.ZOMBIE_SOLDIER.type().get(), LootTable.lootTable()
                .withPool(pool(TGItems.HEAVY_CLOTH.get(), 1, 2, 0)).withPool(pool(Items.GUNPOWDER, 1, 2, 2))
                .withPool(pool(Items.ROTTEN_FLESH, 1, 1, 1)).withPool(pool(TGAmmo.PISTOL_ROUNDS.get(), 1, 4, 2))
                .withPool(pool(TGAmmo.SHOTGUN_ROUNDS.get(), 1, 8, 4)));
        add(TGNpcs.ZOMBIE_FARMER.type().get(), LootTable.lootTable()
                .withPool(pool(TGItems.HEAVY_CLOTH.get(), 1, 2, 2)).withPool(pool(Items.WHEAT_SEEDS, 1, 2, 2))
                .withPool(pool(Items.GUNPOWDER, 1, 2, 2)).withPool(pool(Items.WHEAT, 1, 5, 2)).withPool(pool(Items.LEATHER, 1, 2, 2))
                .withPool(pool(Items.BREAD, 1, 3, 2)).withPool(pool(Items.APPLE, 1, 4, 2)));
        add(TGNpcs.ZOMBIE_MINER.type().get(), LootTable.lootTable()
                .withPool(pool(TGItems.HEAVY_CLOTH.get(), 1, 2, 2)).withPool(pool(Items.COAL, 1, 2, 2))
                .withPool(pool(Items.GUNPOWDER, 1, 2, 2)).withPool(pool(Items.REDSTONE, 1, 5, 2)).withPool(pool(Items.IRON_INGOT, 1, 1, 1)));
        add(TGNpcs.ZOMBIE_POLICEMAN.type().get(), LootTable.lootTable()
                .withPool(pool(TGItems.HEAVY_CLOTH.get(), 1, 2, 0)).withPool(pool(Items.GUNPOWDER, 1, 2, 2))
                .withPool(pool(Items.ROTTEN_FLESH, 1, 1, 1)).withPool(pool(TGAmmo.PISTOL_ROUNDS.get(), 1, 4, 2))
                .withPool(pool(Items.IRON_INGOT, 1, 2, 2)));
        add(TGNpcs.ZOMBIE_PIGMAN_SOLDIER.type().get(), LootTable.lootTable()
                .withPool(pool(Items.GUNPOWDER, 1, 2, 2)).withPool(pool(Items.REDSTONE, 1, 5, 2))
                .withPool(pool(TGItems.OBSIDIAN_STEEL_INGOT.get(), 1, 2, 2)).withPool(pool(Items.GOLD_INGOT, 1, 2, 2))
                .withPool(pool(TGAmmo.SHOTGUN_ROUNDS.get(), 1, 4, 2)).withPool(pool(TGAmmo.RIFLE_ROUNDS.get(), 1, 4, 2)));
        add(TGNpcs.SKELETON_SOLDIER.type().get(), LootTable.lootTable()
                .withPool(pool(Items.COAL, 1, 2, 0)).withPool(pool(Items.GUNPOWDER, 1, 2, 2))
                .withPool(pool(Items.BONE, 1, 2, 2)).withPool(pool(TGItems.HEAVY_CLOTH.get(), 1, 2, 2)));
    }

    /** One roll of {@code item}, {@code min}..{@code max}, plus 1..{@code looting} per looting level (0: none). */
    private LootPool.Builder pool(ItemLike item, int min, int max, int looting) {
        LootItem.Builder<?> entry = LootItem.lootTableItem(item).apply(SetItemCountFunction.setCount(UniformGenerator.between(min, max)));
        if (looting > 0) {
            entry.apply(EnchantedCountIncreaseFunction.lootingMultiplier(registries, UniformGenerator.between(1, looting)));
        }
        return LootPool.lootPool().setRolls(ConstantValue.exactly(1)).add(entry);
    }

    @Override
    protected Stream<EntityType<?>> getKnownEntityTypes() {
        return TGNpcs.npcs().stream().map(npc -> (EntityType<?>) npc.type().get());
    }
}
