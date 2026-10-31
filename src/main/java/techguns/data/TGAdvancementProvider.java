package techguns.data;

import java.util.List;
import java.util.function.Consumer;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.ConsumeItemTrigger;
import net.minecraft.advancements.critereon.EffectsChangedTrigger;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.KilledTrigger;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.advancements.critereon.MobEffectsPredicate;
import net.minecraft.advancements.critereon.PlayerTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.neoforged.neoforge.common.data.AdvancementProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import techguns.TGIds;
import techguns.registry.TGAmmo;
import techguns.registry.TGArmors;
import techguns.registry.TGBlocks;
import techguns.registry.TGGuns;
import techguns.registry.TGItems;
import techguns.registry.TGMobEffects;
import techguns.registry.TGNpcs;
import techguns.registry.TGSlotItems;
import techguns.registry.TGStructures;
import techguns.registry.TGTags;

/**
 * Techguns advancements: a progression guide through guns, machines, armor and gear, the dangers of the world and its
 * structures (new in Techguns 3; Techguns 2 had only recipe unlocks). Titles and descriptions are
 * {@code advancements.techguns.<name>.title/.description}, written by {@link TGLanguageProvider} from {@link #ENTRIES}.
 */
public final class TGAdvancementProvider implements AdvancementProvider.AdvancementGenerator {
    /** Name, English title and description, in tree order. */
    public static final List<String[]> ENTRIES = List.of(
            new String[] {"root", "Techguns", "Get your hands on a gun or some ammunition"},
            new String[] {"handgun", "Sidearm", "Get a pistol or a revolver"},
            new String[] {"automatic", "Full Auto", "Get an automatic weapon"},
            new String[] {"heavy_ordnance", "Heavy Ordnance", "Get a rocket or grenade launcher"},
            new String[] {"tactical_nuke", "Handle With Care", "Get a tactical nuke rocket"},
            new String[] {"energy_weapon", "Future Tech", "Get an energy weapon"},
            new String[] {"superweapon", "Superweapon", "Get a nuclear death ray, a tesla gun or the TFG"},
            new String[] {"machines", "Industrialized", "Get a Metal Press or an Ammo Press"},
            new String[] {"chem_lab", "Better Living Through Chemistry", "Get a Chem Lab"},
            new String[] {"fabricator", "Assembly Line", "Get a Fabricator controller"},
            new String[] {"reaction_chamber", "Controlled Reaction", "Get a Reaction Chamber controller"},
            new String[] {"ore_drill", "Deep Core Mining", "Get an Ore Drill controller"},
            new String[] {"turret", "Sentry", "Get a turret"},
            new String[] {"armor", "Suit Up", "Get a piece of combat armor"},
            new String[] {"steam_armor", "Full Steam Ahead", "Get a steam armor chestplate"},
            new String[] {"power_armor", "Walking Tank", "Get a power armor chestplate"},
            new String[] {"jetpack", "Liftoff", "Get a jetpack, a jump pack or an antigrav pack"},
            new String[] {"gas_mask", "Breathe Easy", "Get a gas mask"},
            new String[] {"undead_soldier", "Still on Duty", "Kill an undead soldier"},
            new String[] {"cyber_demon", "Demon Slayer", "Kill a cyberdemon"},
            new String[] {"helicopter", "Chopper Down", "Shoot down an attack helicopter"},
            new String[] {"military_base", "Trespasser", "Enter a military base"},
            new String[] {"castle", "Storm the Castle", "Enter a castle"},
            new String[] {"aircraft_carrier", "Anchors Aweigh", "Board an aircraft carrier"},
            new String[] {"radiation", "Glowing", "Get irradiated"},
            new String[] {"rad_away", "Decontaminated", "Use RadAway"});

    @Override
    public void generate(HolderLookup.Provider registries, Consumer<AdvancementHolder> saver, ExistingFileHelper existingFileHelper) {
        var structures = registries.lookupOrThrow(Registries.STRUCTURE);

        List<Item> guns = TGGuns.guns().stream().map(g -> (Item) g.get()).toList();
        AdvancementHolder root = Advancement.Builder.advancement()
                .display(TGGuns.PISTOL.get(), title("root"), description("root"), TGIds.id("textures/block/concrete_grey.png"),
                        AdvancementType.TASK, false, false, false)
                .addCriterion("gun", has(guns.toArray(Item[]::new)))
                .addCriterion("ammo", InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().of(TGTags.Items.AMMO)))
                .requirements(AdvancementRequirements.Strategy.OR)
                .save(saver, id("root"));

        // weapons
        AdvancementHolder handgun = obtain(saver, root, "handgun", TGGuns.PISTOL.get(), AdvancementType.TASK,
                TGGuns.PISTOL.get(), TGGuns.REVOLVER.get(), TGGuns.GOLDEN_REVOLVER.get(), TGGuns.LASER_PISTOL.get());
        AdvancementHolder automatic = obtain(saver, handgun, "automatic", TGGuns.AK47.get(), AdvancementType.TASK,
                TGGuns.AK47.get(), TGGuns.M4.get(), TGGuns.M4_INFILTRATOR.get(), TGGuns.AUG.get(), TGGuns.SCAR.get(), TGGuns.THOMPSON.get(),
                TGGuns.MAC10.get(), TGGuns.VECTOR.get(), TGGuns.PDW.get(), TGGuns.LMG.get(), TGGuns.MINIGUN.get());
        AdvancementHolder heavy = obtain(saver, automatic, "heavy_ordnance", TGGuns.ROCKET_LAUNCHER.get(), AdvancementType.TASK,
                TGGuns.ROCKET_LAUNCHER.get(), TGGuns.GRENADE_LAUNCHER.get(), TGGuns.GUIDED_MISSILE_LAUNCHER.get());
        obtain(saver, heavy, "tactical_nuke", TGAmmo.ROCKET_NUKE.get(), AdvancementType.GOAL, TGAmmo.ROCKET_NUKE.get());
        AdvancementHolder energy = obtain(saver, automatic, "energy_weapon", TGGuns.LASERGUN.get(), AdvancementType.TASK,
                TGGuns.LASERGUN.get(), TGGuns.LASER_PISTOL.get(), TGGuns.BLASTER_RIFLE.get(), TGGuns.BLASTER_SHOTGUN.get(),
                TGGuns.PULSE_RIFLE.get(), TGGuns.GAUSS_RIFLE.get());
        obtain(saver, energy, "superweapon", TGGuns.TFG.get(), AdvancementType.CHALLENGE,
                TGGuns.NUCLEAR_DEATHRAY.get(), TGGuns.TESLA_GUN.get(), TGGuns.TFG.get());

        // machines
        AdvancementHolder machines = obtain(saver, root, "machines", TGBlocks.AMMO_PRESS.get(), AdvancementType.TASK,
                TGBlocks.METAL_PRESS.get(), TGBlocks.AMMO_PRESS.get());
        AdvancementHolder chemLab = obtain(saver, machines, "chem_lab", TGBlocks.CHEM_LAB.get(), AdvancementType.TASK, TGBlocks.CHEM_LAB.get());
        AdvancementHolder fabricator = obtain(saver, chemLab, "fabricator", TGBlocks.FABRICATOR_CONTROLLER.get(), AdvancementType.TASK,
                TGBlocks.FABRICATOR_CONTROLLER.get());
        obtain(saver, fabricator, "reaction_chamber", TGBlocks.REACTION_CHAMBER_CONTROLLER.get(), AdvancementType.GOAL,
                TGBlocks.REACTION_CHAMBER_CONTROLLER.get());
        obtain(saver, fabricator, "ore_drill", TGBlocks.ORE_DRILL_CONTROLLER.get(), AdvancementType.GOAL, TGBlocks.ORE_DRILL_CONTROLLER.get());
        obtain(saver, machines, "turret", TGBlocks.TURRET.get(), AdvancementType.TASK, TGBlocks.TURRET.get());

        // armor and gear
        AdvancementHolder armor = obtain(saver, root, "armor", TGArmors.T1_COMBAT_CHESTPLATE.get(), AdvancementType.TASK,
                TGArmors.T1_COMBAT_HELMET.get(), TGArmors.T1_COMBAT_CHESTPLATE.get(), TGArmors.T1_COMBAT_LEGGINGS.get(), TGArmors.T1_COMBAT_BOOTS.get());
        AdvancementHolder steam = obtain(saver, armor, "steam_armor", TGArmors.STEAM_CHESTPLATE.get(), AdvancementType.TASK,
                TGArmors.STEAM_CHESTPLATE.get());
        obtain(saver, steam, "power_armor", TGArmors.T3_POWER_CHESTPLATE.get(), AdvancementType.GOAL,
                TGArmors.T3_POWER_CHESTPLATE.get(), TGArmors.T3_MINER_CHESTPLATE.get(), TGArmors.T3_EXO_CHESTPLATE.get(),
                TGArmors.T4_PRAETOR_CHESTPLATE.get(), TGArmors.T4_POWER_CHESTPLATE.get(), TGArmors.T4_NANO_CHESTPLATE.get());
        obtain(saver, armor, "jetpack", TGSlotItems.JETPACK.get(), AdvancementType.TASK,
                TGSlotItems.JETPACK.get(), TGSlotItems.JUMP_PACK.get(), TGSlotItems.ANTIGRAV_PACK.get());
        obtain(saver, armor, "gas_mask", TGSlotItems.GAS_MASK.get(), AdvancementType.TASK, TGSlotItems.GAS_MASK.get());

        // combat
        AdvancementHolder undead = kill(saver, root, "undead_soldier", TGNpcs.ZOMBIE_SOLDIER.egg().get(), AdvancementType.TASK,
                TGNpcs.ZOMBIE_SOLDIER.type().get(), TGNpcs.SKELETON_SOLDIER.type().get(), TGNpcs.ZOMBIE_PIGMAN_SOLDIER.type().get());
        kill(saver, undead, "cyber_demon", TGNpcs.CYBER_DEMON.egg().get(), AdvancementType.CHALLENGE, TGNpcs.CYBER_DEMON.type().get());
        kill(saver, undead, "helicopter", TGNpcs.ATTACK_HELICOPTER.egg().get(), AdvancementType.CHALLENGE, TGNpcs.ATTACK_HELICOPTER.type().get());

        // places
        AdvancementHolder base = enter(saver, root, "military_base", TGBlocks.MILITARY_SPAWN_POINT.get(), AdvancementType.TASK,
                structures.getOrThrow(TGStructures.MILITARY_BASE_KEY));
        enter(saver, base, "castle", TGBlocks.MONSTER_HOLE.get(), AdvancementType.TASK, structures.getOrThrow(TGStructures.CASTLE_KEY));
        enter(saver, base, "aircraft_carrier", TGGuns.GUIDED_MISSILE_LAUNCHER.get(), AdvancementType.TASK,
                structures.getOrThrow(key("aircraft_carrier")));

        // radiation
        AdvancementHolder radiation = Advancement.Builder.advancement().parent(root)
                .display(TGItems.ENRICHED_URANIUM.get(), title("radiation"), description("radiation"), null, AdvancementType.TASK, true, true, false)
                .addCriterion("irradiated", EffectsChangedTrigger.TriggerInstance.hasEffects(MobEffectsPredicate.Builder.effects().and(TGMobEffects.RADIATION)))
                .save(saver, id("radiation"));
        Advancement.Builder.advancement().parent(radiation)
                .display(TGItems.RAD_AWAY.get(), title("rad_away"), description("rad_away"), null, AdvancementType.TASK, true, true, false)
                .addCriterion("rad_away", ConsumeItemTrigger.TriggerInstance.usedItem(TGItems.RAD_AWAY.get()))
                .save(saver, id("rad_away"));
    }

    private static ResourceKey<Structure> key(String name) {
        return ResourceKey.create(Registries.STRUCTURE, TGIds.id(name));
    }

    private static String id(String name) {
        return TGIds.id(name).toString();
    }

    private static Component title(String name) {
        return Component.translatable("advancements.techguns." + name + ".title");
    }

    private static Component description(String name) {
        return Component.translatable("advancements.techguns." + name + ".description");
    }

    private static Criterion<InventoryChangeTrigger.TriggerInstance> has(ItemLike... items) {
        return InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().of(items));
    }

    /** Done when the player has any of the items. */
    private static AdvancementHolder obtain(Consumer<AdvancementHolder> saver, AdvancementHolder parent, String name, ItemLike icon,
                                            AdvancementType type, ItemLike... items) {
        return Advancement.Builder.advancement().parent(parent)
                .display(icon, title(name), description(name), null, type, true, true, false)
                .addCriterion("has", has(items))
                .save(saver, id(name));
    }

    /** Done when the player kills any of the entity types. */
    private static AdvancementHolder kill(Consumer<AdvancementHolder> saver, AdvancementHolder parent, String name, ItemLike icon,
                                          AdvancementType type, EntityType<?>... types) {
        Advancement.Builder builder = Advancement.Builder.advancement().parent(parent)
                .display(icon, title(name), description(name), null, type, true, true, false)
                .requirements(AdvancementRequirements.Strategy.OR);
        for (EntityType<?> entityType : types) {
            builder.addCriterion(net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(entityType).getPath(),
                    KilledTrigger.TriggerInstance.playerKilledEntity(EntityPredicate.Builder.entity().of(entityType)));
        }
        return builder.save(saver, id(name));
    }

    /** Done when the player is inside the structure. */
    private static AdvancementHolder enter(Consumer<AdvancementHolder> saver, AdvancementHolder parent, String name, ItemLike icon,
                                           AdvancementType type, net.minecraft.core.Holder<Structure> structure) {
        return Advancement.Builder.advancement().parent(parent)
                .display(icon, title(name), description(name), null, type, true, true, false)
                .addCriterion("entered", PlayerTrigger.TriggerInstance.located(LocationPredicate.Builder.inStructure(structure)))
                .save(saver, id(name));
    }
}
