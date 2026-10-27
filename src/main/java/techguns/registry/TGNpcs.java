package techguns.registry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import techguns.entity.npc.NpcDefinition;
import techguns.entity.npc.NpcLoadout;
import techguns.entity.npc.SuperMutant;
import techguns.entity.npc.TGNpc;
import techguns.entity.npc.TGNpcFaction;

/**
 * Techguns NPCs (Phase 6): entity types, attributes and spawn eggs. Stats, loadouts and egg colours are from the
 * Techguns 2 NPC classes and {@code TGEntities}.
 */
public final class TGNpcs {
    /** A registered NPC: its type, attributes and spawn egg. */
    public record Npc(String name, Supplier<EntityType<TGNpc>> type, Supplier<AttributeSupplier.Builder> attributes, DeferredItem<Item> egg) {
    }

    private static final List<Npc> NPCS = new ArrayList<>();
    private static final double ALWAYS = 1.0;
    private static final double HALF = 0.5;

    // --- Undead (Phase 6a) ---
    public static final Npc ZOMBIE_SOLDIER = npc("zombie_soldier", 0.6f, 1.95f, false, 0x757468, 0x38B038,
            () -> TGNpc.createAttributes(25, 0.25, 4),
            new NpcDefinition(5.0f, NpcDefinition.Sounds.ZOMBIE, TGNpcFaction.HOSTILE, false, NpcLoadout.builder()
                    .armor(EquipmentSlot.HEAD, TGArmors.T1_COMBAT_HELMET, HALF).armor(EquipmentSlot.CHEST, TGArmors.T1_COMBAT_CHESTPLATE, HALF)
                    .armor(EquipmentSlot.LEGS, TGArmors.T1_COMBAT_LEGGINGS, HALF).armor(EquipmentSlot.FEET, TGArmors.T1_COMBAT_BOOTS, HALF)
                    .weapon(TGGuns.REVOLVER, 1).weapon(TGGuns.THOMPSON, 1).weapon(() -> Items.IRON_SHOVEL, 1).weapon(() -> Items.STONE_SHOVEL, 1)
                    .build()));
    public static final Npc ZOMBIE_FARMER = npc("zombie_farmer", 0.6f, 1.95f, false, 0x757468, 0x38B038,
            () -> TGNpc.createAttributes(18, 0.20, 3),
            new NpcDefinition(0.0f, NpcDefinition.Sounds.ZOMBIE, TGNpcFaction.HOSTILE, true, NpcLoadout.builder().camo(NpcLoadout.RANDOM_CAMO)
                    .armor(EquipmentSlot.CHEST, TGArmors.T1_MINER_CHESTPLATE, ALWAYS).armor(EquipmentSlot.LEGS, TGArmors.T1_MINER_LEGGINGS, HALF)
                    .armor(EquipmentSlot.FEET, TGArmors.T1_MINER_BOOTS, HALF)
                    .weapon(() -> Items.WOODEN_HOE, 1).weapon(() -> Items.IRON_HOE, 1).weapon(() -> Items.STONE_HOE, 1).weapon(TGGuns.HAND_CANNON, 1)
                    .build()));
    public static final Npc ZOMBIE_MINER = npc("zombie_miner", 0.6f, 1.95f, false, 0x757468, 0x38B038,
            () -> TGNpc.createAttributes(20, 0.20, 4),
            new NpcDefinition(0.0f, NpcDefinition.Sounds.ZOMBIE, TGNpcFaction.HOSTILE, true, NpcLoadout.builder().camo(NpcLoadout.RANDOM_CAMO)
                    .armor(EquipmentSlot.HEAD, TGArmors.T1_MINER_HELMET, ALWAYS).armor(EquipmentSlot.CHEST, TGArmors.T1_MINER_CHESTPLATE, HALF)
                    .armor(EquipmentSlot.LEGS, TGArmors.T1_MINER_LEGGINGS, HALF).armor(EquipmentSlot.FEET, TGArmors.T1_MINER_BOOTS, HALF)
                    .weapon(() -> Items.STONE_PICKAXE, 1).weapon(() -> Items.IRON_PICKAXE, 1).weapon(TGGuns.HAND_CANNON, 1)
                    .build()));
    public static final Npc ZOMBIE_POLICEMAN = npc("zombie_policeman", 0.6f, 1.95f, false, 0x303030, 0x0000FF,
            () -> TGNpc.createAttributes(25, 0.25, 4),
            new NpcDefinition(5.0f, NpcDefinition.Sounds.ZOMBIE, TGNpcFaction.HOSTILE, true, NpcLoadout.builder().camo(5)
                    .armor(EquipmentSlot.HEAD, TGArmors.T2_COMBAT_HELMET, HALF).armor(EquipmentSlot.CHEST, TGArmors.T2_COMBAT_CHESTPLATE, HALF)
                    .armor(EquipmentSlot.LEGS, TGArmors.T2_COMBAT_LEGGINGS, HALF).armor(EquipmentSlot.FEET, TGArmors.T2_COMBAT_BOOTS, HALF)
                    .weapon(TGGuns.REVOLVER, 1).weapon(TGGuns.PISTOL, 1)
                    .build()));
    /** Techguns 2: the helmet's chance check guarded nothing, so the helmet is always worn. */
    public static final Npc ZOMBIE_PIGMAN_SOLDIER = npc("zombie_pigman_soldier", 0.6f, 1.95f, true, 0xFF1111, 0x770000,
            () -> TGNpc.createAttributes(25, 0.30, 5),
            new NpcDefinition(10.0f, NpcDefinition.Sounds.ZOMBIFIED_PIGLIN, TGNpcFaction.HOSTILE, false, NpcLoadout.builder().camo(3)
                    .armor(EquipmentSlot.HEAD, TGArmors.T2_COMBAT_HELMET, ALWAYS).armor(EquipmentSlot.CHEST, TGArmors.T2_COMBAT_CHESTPLATE, HALF)
                    .armor(EquipmentSlot.LEGS, TGArmors.T2_COMBAT_LEGGINGS, HALF).armor(EquipmentSlot.FEET, TGArmors.T2_COMBAT_BOOTS, HALF)
                    .weapon(TGGuns.THOMPSON, 3).weapon(TGGuns.REVOLVER, 2).weapon(TGGuns.AK47, 2).weapon(TGGuns.PISTOL, 2)
                    .build()));
    public static final Npc SKELETON_SOLDIER = npc("skeleton_soldier", 0.6f, 1.95f, false, 0x404040, 0xF0F0F0,
            () -> TGNpc.createAttributes(25, 0.25, 4),
            new NpcDefinition(0.0f, NpcDefinition.Sounds.SKELETON, TGNpcFaction.HOSTILE, true, NpcLoadout.builder()
                    .armorChoice(EquipmentSlot.HEAD, TGArmors.T1_SCOUT_HELMET, TGArmors.T1_COMBAT_HELMET, HALF)
                    .armorChoice(EquipmentSlot.FEET, TGArmors.T1_SCOUT_BOOTS, TGArmors.T1_COMBAT_BOOTS, HALF)
                    .weapon(TGGuns.REVOLVER, 1).weapon(TGGuns.THOMPSON, 1).weapon(TGGuns.HAND_CANNON, 1)
                    .build()));

    // --- Humans (Phase 6b) ---
    public static final Npc ARMY_SOLDIER = npc("army_soldier", 0.6f, 1.95f, false, 0x74806e, 0x191512,
            () -> TGNpc.createAttributes(25, 0.30, 4, 75, 0),
            new NpcDefinition(8.0f, NpcDefinition.Sounds.SOLDIER, TGNpcFaction.HOSTILE, false, NpcLoadout.builder()
                    .armorChoice(EquipmentSlot.HEAD, TGArmors.T2_COMBAT_HELMET, TGArmors.T2_BERET_ITEM, HALF)
                    .armor(EquipmentSlot.CHEST, TGArmors.T2_COMBAT_CHESTPLATE, HALF).armor(EquipmentSlot.LEGS, TGArmors.T2_COMBAT_LEGGINGS, HALF)
                    .armor(EquipmentSlot.FEET, TGArmors.T2_COMBAT_BOOTS, HALF)
                    .weapon(TGGuns.M4, 1).weapon(TGGuns.COMBAT_SHOTGUN, 1).weapon(TGGuns.BOLT_ACTION, 1)
                    .build()));
    public static final Npc BANDIT = npc("bandit", 0.6f, 1.95f, false, 0x8f9d59, 0x2c3117,
            () -> TGNpc.createAttributes(20, 0.30, 5, 40, 0),
            new NpcDefinition(5.0f, NpcDefinition.Sounds.VILLAGER, TGNpcFaction.HOSTILE, false, NpcLoadout.builder()
                    .armor(EquipmentSlot.CHEST, TGArmors.T1_SCOUT_CHESTPLATE, ALWAYS).armor(EquipmentSlot.LEGS, TGArmors.T1_SCOUT_LEGGINGS, ALWAYS)
                    .armor(EquipmentSlot.FEET, TGArmors.T1_SCOUT_BOOTS, ALWAYS).armor(EquipmentSlot.HEAD, TGArmors.T1_SCOUT_HELMET, HALF)
                    .weapon(TGGuns.PISTOL, 1).weapon(TGGuns.AK47, 1).weapon(TGGuns.SAWED_OFF, 1).weapon(TGGuns.THOMPSON, 1)
                    .weapon(TGGuns.REVOLVER, 1).weapon(TGGuns.BOLT_ACTION, 1)
                    .build()));
    public static final Npc COMMANDO = npc("commando", 0.6f, 1.95f, false, 0x191512, 0x74806e,
            () -> TGNpc.createAttributes(30, 0.35, 5, 75, 0),
            new NpcDefinition(8.0f, NpcDefinition.Sounds.SOLDIER, TGNpcFaction.HOSTILE, false, NpcLoadout.builder()
                    .armor(EquipmentSlot.HEAD, TGArmors.T2_COMMANDO_HELMET, ALWAYS).armor(EquipmentSlot.CHEST, TGArmors.T2_COMMANDO_CHESTPLATE, ALWAYS)
                    .armor(EquipmentSlot.LEGS, TGArmors.T2_COMMANDO_LEGGINGS, ALWAYS).armor(EquipmentSlot.FEET, TGArmors.T2_COMMANDO_BOOTS, ALWAYS)
                    .weapon(TGGuns.M4_INFILTRATOR, 1)
                    .build()));
    /** Techguns 2 GenericNPCGearSpecificStats: the power armor protects it; each piece has camo 1 or 2. */
    public static final Npc OUTCAST = npc("outcast", 0.6f, 1.95f, false, 0x101010, 0xFF0000,
            () -> TGNpc.createAttributes(30, 0.2, 8, 60, 0),
            new NpcDefinition(NpcDefinition.GEAR_ARMOR, NpcDefinition.Sounds.VILLAGER, TGNpcFaction.HOSTILE, false, NpcLoadout.builder()
                    .camoPerPiece(1, 2)
                    .armor(EquipmentSlot.HEAD, TGArmors.T3_POWER_HELMET, ALWAYS).armor(EquipmentSlot.CHEST, TGArmors.T3_POWER_CHESTPLATE, ALWAYS)
                    .armor(EquipmentSlot.LEGS, TGArmors.T3_POWER_LEGGINGS, ALWAYS).armor(EquipmentSlot.FEET, TGArmors.T3_POWER_BOOTS, ALWAYS)
                    .weapon(TGGuns.LASERGUN, 1).weapon(TGGuns.GRIM_REAPER, 1).weapon(TGGuns.MINIGUN, 1).weapon(TGGuns.BIOGUN, 1)
                    .weapon(TGGuns.FLAMETHROWER, 1).weapon(TGGuns.TESLA_GUN, 1).weapon(TGGuns.LMG, 1)
                    .build()));
    public static final Npc PSYCHO_STEVE = npc("psycho_steve", 0.6f, 1.95f, false, 0x757468, 0xF0F0F0,
            () -> TGNpc.createAttributes(75, 0.60, 7, 60, 1),
            new NpcDefinition(5.0f, NpcDefinition.Sounds.HEAVY, TGNpcFaction.HOSTILE, false, NpcLoadout.builder().camo(NpcLoadout.RANDOM_CAMO)
                    .armor(EquipmentSlot.HEAD, TGArmors.T1_MINER_HELMET, ALWAYS).armor(EquipmentSlot.CHEST, TGArmors.T1_MINER_CHESTPLATE, ALWAYS)
                    .armor(EquipmentSlot.LEGS, TGArmors.T1_MINER_LEGGINGS, ALWAYS).armor(EquipmentSlot.FEET, TGArmors.T1_MINER_BOOTS, ALWAYS)
                    .weapon(TGGuns.CHAINSAW, 1)
                    .build(), 25));
    /** Techguns 2 GenericNPCGearSpecificStats: the worn armor protects it. */
    public static final Npc STORMTROOPER = npc("stormtrooper", 0.6f, 1.95f, false, 0xF0F0F0, 0x404040,
            () -> TGNpc.createAttributes(35, 0.30, 4, 75, 0),
            new NpcDefinition(NpcDefinition.GEAR_ARMOR, NpcDefinition.Sounds.HEAVY, TGNpcFaction.HOSTILE, false, NpcLoadout.builder().camo(1)
                    .armor(EquipmentSlot.HEAD, TGArmors.T3_MINER_HELMET, ALWAYS).armor(EquipmentSlot.CHEST, TGArmors.T3_MINER_CHESTPLATE, ALWAYS)
                    .armor(EquipmentSlot.LEGS, TGArmors.T3_MINER_LEGGINGS, ALWAYS).armor(EquipmentSlot.FEET, TGArmors.T3_MINER_BOOTS, ALWAYS)
                    .weapon(TGGuns.BLASTER_RIFLE, 1).weapon(TGGuns.LASERGUN, 1)
                    .build()));
    public static final Npc DICTATOR_DAVE = npc("dictator_dave", 0.6f, 1.95f, false, 0x8f9d59, 0xA0A000,
            () -> TGNpc.createAttributes(100, 0.40, 7, 60, 2),
            new NpcDefinition(15.0f, NpcDefinition.Sounds.SOLDIER, TGNpcFaction.HOSTILE, false, NpcLoadout.builder()
                    .armor(EquipmentSlot.HEAD, TGArmors.T2_BERET_ITEM, ALWAYS).armor(EquipmentSlot.CHEST, TGArmors.T1_COMBAT_CHESTPLATE, ALWAYS)
                    .armor(EquipmentSlot.LEGS, TGArmors.T1_SCOUT_LEGGINGS, ALWAYS).armor(EquipmentSlot.FEET, TGArmors.T1_COMBAT_BOOTS, ALWAYS)
                    .weapon(TGGuns.GOLDEN_REVOLVER, 1)
                    .build(), 35));

    // --- Super mutants (Phase 6c; Techguns 2 sizes: width, 2 x model scale) ---
    private static final NpcLoadout MUTANT_LOADOUT = NpcLoadout.builder()
            .weapon(TGGuns.ROCKET_LAUNCHER, 1).weapon(TGGuns.AK47, 1).weapon(TGGuns.COMBAT_SHOTGUN, 1).weapon(TGGuns.LASERGUN, 2).build();
    public static final Npc SUPER_MUTANT_BASIC = mutant("super_mutant_basic", SuperMutant.Kind.BASIC, 1.0f, 0x71552e,
            () -> TGNpc.createAttributes(35, 0.30, 7, 50, 1));
    public static final Npc SUPER_MUTANT_HEAVY = mutant("super_mutant_heavy", SuperMutant.Kind.HEAVY, 1.2f, 0x51350e,
            () -> TGNpc.createAttributes(45, 0.25, 9, 50, 2));
    public static final Npc SUPER_MUTANT_ELITE = mutant("super_mutant_elite", SuperMutant.Kind.ELITE, 1.4f, 0x61451e,
            () -> TGNpc.createAttributes(60, 0.20, 11, 50, 2.5));

    private TGNpcs() {
    }

    /** Loads the holders (types into the entity register, eggs into the item register). */
    public static void init() {
    }

    private static Npc mutant(String name, SuperMutant.Kind kind, float width, int eggSpots, Supplier<AttributeSupplier.Builder> attributes) {
        NpcDefinition definition = new NpcDefinition(0.0f, NpcDefinition.Sounds.VILLAGER, TGNpcFaction.HOSTILE, false, MUTANT_LOADOUT);
        return npc(name, width, 2.0f * kind.scale, false, 0xc6a96b, eggSpots, attributes, (t, level) -> new SuperMutant(t, level, definition, kind));
    }

    private static Npc npc(String name, float width, float height, boolean fireImmune, int eggBackground, int eggSpots,
                           Supplier<AttributeSupplier.Builder> attributes, NpcDefinition definition) {
        return npc(name, width, height, fireImmune, eggBackground, eggSpots, attributes, (t, level) -> new TGNpc(t, level, definition));
    }

    private static Npc npc(String name, float width, float height, boolean fireImmune, int eggBackground, int eggSpots,
                           Supplier<AttributeSupplier.Builder> attributes, EntityType.EntityFactory<TGNpc> factory) {
        Supplier<EntityType<TGNpc>> type = TGEntities.ENTITY_TYPES.register(name, () -> {
            EntityType.Builder<TGNpc> builder = EntityType.Builder.<TGNpc>of(factory, MobCategory.MONSTER)
                    .sized(width, height).eyeHeight(height * 0.85f).clientTrackingRange(8);
            if (fireImmune) {
                builder.fireImmune();
            }
            return builder.build(name);
        });
        DeferredItem<Item> egg = TGItems.ITEMS.register(name + "_spawn_egg",
                () -> new DeferredSpawnEggItem(type, eggBackground, eggSpots, new Item.Properties()));
        Npc npc = new Npc(name, type, attributes, egg);
        NPCS.add(npc);
        return npc;
    }

    /** Techguns 2 {@code GenericNPCUndead} subclasses. */
    public static List<Npc> undead() {
        return List.of(ZOMBIE_SOLDIER, ZOMBIE_FARMER, ZOMBIE_MINER, ZOMBIE_POLICEMAN, ZOMBIE_PIGMAN_SOLDIER, SKELETON_SOLDIER);
    }

    public static List<Npc> npcs() {
        return Collections.unmodifiableList(NPCS);
    }

    public static void registerAttributes(EntityAttributeCreationEvent event) {
        NPCS.forEach(npc -> event.put(npc.type().get(), npc.attributes().get().build()));
    }
}
