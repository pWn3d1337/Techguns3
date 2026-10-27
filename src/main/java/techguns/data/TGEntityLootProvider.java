package techguns.data;

import java.util.function.Supplier;
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
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceWithEnchantedBonusCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import techguns.registry.TGAmmo;
import techguns.registry.TGGuns;
import techguns.registry.TGItems;
import techguns.registry.TGNpcs;

/**
 * NPC loot tables, from the Techguns 2 {@code loot_tables/entities} files: one roll per pool, each pool one item
 * with a count range, a looting bonus (1..n per level) and a drop chance raised per looting level.
 */
public class TGEntityLootProvider extends EntityLootSubProvider {
    /** Techguns 2's usual drop chance and its looting bonus per level. */
    private static final float CHANCE = 0.2f;
    private static final float LOOTING = 0.05f;
    private static final float LOOTING_AMMO = 0.1f;

    public TGEntityLootProvider(HolderLookup.Provider registries) {
        super(FeatureFlags.REGISTRY.allFlags(), registries);
    }

    @Override
    public void generate() {
        add(TGNpcs.ZOMBIE_SOLDIER, pool(TGItems.HEAVY_CLOTH, 1, 2, 0, CHANCE, LOOTING), pool(() -> Items.GUNPOWDER, 1, 2, 2, CHANCE, LOOTING),
                pool(() -> Items.ROTTEN_FLESH, 1, 1, 1, CHANCE, LOOTING), pool(TGAmmo.PISTOL_ROUNDS, 1, 4, 2, CHANCE, LOOTING_AMMO),
                pool(TGAmmo.SHOTGUN_ROUNDS, 1, 8, 4, CHANCE, LOOTING_AMMO));
        add(TGNpcs.ZOMBIE_FARMER, pool(TGItems.HEAVY_CLOTH, 1, 2, 2, CHANCE, LOOTING), pool(() -> Items.WHEAT_SEEDS, 1, 2, 2, CHANCE, LOOTING),
                pool(() -> Items.GUNPOWDER, 1, 2, 2, CHANCE, LOOTING), pool(() -> Items.WHEAT, 1, 5, 2, CHANCE, LOOTING),
                pool(() -> Items.LEATHER, 1, 2, 2, CHANCE, LOOTING), pool(() -> Items.BREAD, 1, 3, 2, CHANCE, LOOTING),
                pool(() -> Items.APPLE, 1, 4, 2, CHANCE, LOOTING));
        add(TGNpcs.ZOMBIE_MINER, pool(TGItems.HEAVY_CLOTH, 1, 2, 2, CHANCE, LOOTING), pool(() -> Items.COAL, 1, 2, 2, CHANCE, LOOTING),
                pool(() -> Items.GUNPOWDER, 1, 2, 2, CHANCE, LOOTING), pool(() -> Items.REDSTONE, 1, 5, 2, CHANCE, LOOTING),
                pool(() -> Items.IRON_INGOT, 1, 1, 1, CHANCE, LOOTING));
        add(TGNpcs.ZOMBIE_POLICEMAN, pool(TGItems.HEAVY_CLOTH, 1, 2, 0, CHANCE, LOOTING), pool(() -> Items.GUNPOWDER, 1, 2, 2, CHANCE, LOOTING),
                pool(() -> Items.ROTTEN_FLESH, 1, 1, 1, CHANCE, LOOTING), pool(TGAmmo.PISTOL_ROUNDS, 1, 4, 2, CHANCE, LOOTING_AMMO),
                pool(() -> Items.IRON_INGOT, 1, 2, 2, CHANCE, LOOTING));
        add(TGNpcs.ZOMBIE_PIGMAN_SOLDIER, pool(() -> Items.GUNPOWDER, 1, 2, 2, CHANCE, LOOTING), pool(() -> Items.REDSTONE, 1, 5, 2, CHANCE, LOOTING),
                pool(TGItems.OBSIDIAN_STEEL_INGOT, 1, 2, 2, CHANCE, LOOTING), pool(() -> Items.GOLD_INGOT, 1, 2, 2, CHANCE, LOOTING),
                pool(TGAmmo.SHOTGUN_ROUNDS, 1, 4, 2, CHANCE, LOOTING), pool(TGAmmo.RIFLE_ROUNDS, 1, 4, 2, CHANCE, LOOTING));
        add(TGNpcs.SKELETON_SOLDIER, pool(() -> Items.COAL, 1, 2, 0, CHANCE, LOOTING), pool(() -> Items.GUNPOWDER, 1, 2, 2, CHANCE, LOOTING),
                pool(() -> Items.BONE, 1, 2, 2, CHANCE, LOOTING), pool(TGItems.HEAVY_CLOTH, 1, 2, 2, CHANCE, LOOTING));

        add(TGNpcs.ARMY_SOLDIER, pool(TGItems.HEAVY_CLOTH, 1, 2, 0, CHANCE, LOOTING), pool(() -> Items.IRON_INGOT, 1, 2, 2, CHANCE, LOOTING),
                pool(() -> Items.GUNPOWDER, 1, 2, 2, CHANCE, LOOTING), pool(TGAmmo.SHOTGUN_ROUNDS, 1, 7, 2, CHANCE, LOOTING_AMMO),
                pool(TGAmmo.RIFLE_ROUNDS, 1, 3, 2, CHANCE, LOOTING_AMMO));
        add(TGNpcs.BANDIT, pool(TGItems.HEAVY_CLOTH, 1, 2, 0, CHANCE, LOOTING), pool(() -> Items.IRON_INGOT, 1, 2, 2, CHANCE, LOOTING),
                pool(() -> Items.GUNPOWDER, 1, 2, 2, CHANCE, LOOTING), pool(TGAmmo.SHOTGUN_ROUNDS, 1, 7, 2, CHANCE, LOOTING_AMMO),
                pool(TGAmmo.PISTOL_ROUNDS, 1, 7, 2, CHANCE, LOOTING_AMMO), pool(TGAmmo.RIFLE_ROUNDS, 1, 3, 2, CHANCE, LOOTING_AMMO));
        add(TGNpcs.COMMANDO, pool(TGItems.OBSIDIAN_STEEL_INGOT, 1, 2, 0, 0.1f, LOOTING), pool(TGItems.RUBBER_BAR, 1, 2, 2, CHANCE, LOOTING),
                pool(() -> Items.GUNPOWDER, 1, 2, 2, CHANCE, LOOTING), pool(TGAmmo.RIFLE_ROUNDS, 1, 7, 2, CHANCE, LOOTING_AMMO),
                pool(TGAmmo.SHOTGUN_ROUNDS, 1, 3, 2, CHANCE, LOOTING_AMMO), pool(TGAmmo.PISTOL_ROUNDS, 1, 3, 2, CHANCE, LOOTING_AMMO));
        add(TGNpcs.OUTCAST, pool(TGItems.OBSIDIAN_STEEL_INGOT, 1, 2, 0, 0.1f, LOOTING), pool(TGItems.RUBBER_BAR, 1, 2, 2, CHANCE, LOOTING),
                pool(() -> Items.GUNPOWDER, 1, 2, 2, CHANCE, LOOTING), pool(TGAmmo.RIFLE_ROUNDS, 1, 7, 2, CHANCE, LOOTING_AMMO),
                pool(TGAmmo.PISTOL_ROUNDS, 1, 3, 2, 0.1f, LOOTING_AMMO), pool(TGAmmo.SHOTGUN_ROUNDS, 1, 3, 2, 0.1f, LOOTING_AMMO));
        add(TGNpcs.PSYCHO_STEVE, pool(TGGuns.CHAINSAW, 1, 2, 0, 0.5f, LOOTING), pool(TGAmmo.FUEL_TANK, 2, 4, 2, 0.5f, LOOTING));
        add(TGNpcs.STORMTROOPER, pool(TGItems.PLASTIC_SHEET, 1, 2, 0, CHANCE, LOOTING), pool(TGItems.OBSIDIAN_STEEL_INGOT, 1, 2, 2, CHANCE, LOOTING),
                pool(() -> Items.REDSTONE, 1, 5, 2, 0.3f, LOOTING), pool(TGItems.CIRCUIT_BOARD, 1, 2, 2, 0.1f, LOOTING),
                pool(TGAmmo.ENERGY_CELL, 1, 2, 2, 0.1f, LOOTING));
        add(TGNpcs.DICTATOR_DAVE, pool(TGGuns.GOLDEN_REVOLVER, 1, 2, 0, 0.5f, LOOTING), pool(TGAmmo.PISTOL_ROUNDS, 1, 17, 2, 0.5f, LOOTING));
    }

    private void add(TGNpcs.Npc npc, LootPool.Builder... pools) {
        LootTable.Builder table = LootTable.lootTable();
        for (LootPool.Builder pool : pools) {
            table.withPool(pool);
        }
        add(npc.type().get(), table);
    }

    /**
     * One roll of {@code item}, {@code min}..{@code max}, plus 1..{@code looting} per looting level (0: none), with
     * {@code chance} raised by {@code lootingChance} per looting level (Techguns 2 {@code random_chance_with_looting}).
     */
    private LootPool.Builder pool(Supplier<? extends ItemLike> item, int min, int max, int looting, float chance, float lootingChance) {
        LootItem.Builder<?> entry = LootItem.lootTableItem(item.get()).apply(SetItemCountFunction.setCount(UniformGenerator.between(min, max)));
        if (looting > 0) {
            entry.apply(EnchantedCountIncreaseFunction.lootingMultiplier(registries, UniformGenerator.between(1, looting)));
        }
        return LootPool.lootPool().setRolls(ConstantValue.exactly(1)).add(entry)
                .when(LootItemRandomChanceWithEnchantedBonusCondition.randomChanceAndLootingBoost(registries, chance, lootingChance));
    }

    @Override
    protected Stream<EntityType<?>> getKnownEntityTypes() {
        return TGNpcs.npcs().stream().map(npc -> (EntityType<?>) npc.type().get());
    }
}
