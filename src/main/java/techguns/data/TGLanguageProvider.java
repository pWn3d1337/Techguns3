package techguns.data;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.LanguageProvider;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import techguns.Techguns;
import techguns.client.TGKeyMappings;
import techguns.core.damage.TGDamageCategory;
import techguns.damage.TGDamageTypes;
import techguns.registry.TGBlocks;
import techguns.registry.TGCreativeTabs;
import techguns.registry.TGMobEffects;
import techguns.registry.TGItems;

public class TGLanguageProvider extends LanguageProvider {
    public TGLanguageProvider(PackOutput output) {
        super(output, Techguns.MODID, "en_us");
    }

    /** English names that differ from the title-cased registry name. */
    private static final Map<String, String> NAME_OVERRIDES = Map.ofEntries(
            Map.entry("biomass", "Bio Mass"),
            // Phase 5c3 (Techguns 2 lang: tile.techguns.orecluster.*, item.techguns.oredrill*)
            Map.entry("explosive_charge", "Mining Charge"),
            Map.entry("cyber_demon", "Cyberdemon"),
            Map.entry("cyber_demon_spawn_egg", "Cyberdemon Spawn Egg"),
            Map.entry("super_mutant_basic", "Super Mutant"),
            Map.entry("super_mutant_heavy", "Heavy Super Mutant"),
            Map.entry("super_mutant_elite", "Elite Super Mutant"),
            Map.entry("super_mutant_basic_spawn_egg", "Super Mutant Spawn Egg"),
            Map.entry("super_mutant_heavy_spawn_egg", "Heavy Super Mutant Spawn Egg"),
            Map.entry("super_mutant_elite_spawn_egg", "Elite Super Mutant Spawn Egg"),
            Map.entry("turret_armor_iron", "Turret Armor (Iron)"),
            Map.entry("turret_armor_steel", "Turret Armor (Steel)"),
            Map.entry("turret_armor_obsidian_steel", "Turret Armor (Obsidian Steel)"),
            Map.entry("turret_armor_carbon", "Turret Armor (Carbon)"),
            Map.entry("explosive_charge_advanced", "Advanced Mining Charge"),
            Map.entry("ore_cluster_coal", "Ore Cluster (Coal)"),
            Map.entry("ore_cluster_common_metal", "Ore Cluster (Common Metal)"),
            Map.entry("ore_cluster_rare_metal", "Ore Cluster (Rare Metal)"),
            Map.entry("ore_cluster_shiny_metal", "Ore Cluster (Shiny Metal)"),
            Map.entry("ore_cluster_uranium", "Ore Cluster (Uranium)"),
            Map.entry("ore_cluster_common_gem", "Ore Cluster (Common Gem)"),
            Map.entry("ore_cluster_shiny_gem", "Ore Cluster (Shiny Gem)"),
            Map.entry("ore_cluster_nether_crystal", "Ore Cluster (Nether Crystal)"),
            Map.entry("ore_cluster_oil", "Ore Cluster (Oil)"),
            Map.entry("ore_drill_head_small_steel", "Small Ore Drill (Steel)"),
            Map.entry("ore_drill_head_small_obsidian_steel", "Small Ore Drill (Obsidian Steel)"),
            Map.entry("ore_drill_head_small_carbon", "Small Ore Drill (Carbon)"),
            Map.entry("ore_drill_head_medium_steel", "Medium Ore Drill (Steel)"),
            Map.entry("ore_drill_head_medium_obsidian_steel", "Medium Ore Drill (Obsidian Steel)"),
            Map.entry("ore_drill_head_medium_carbon", "Medium Ore Drill (Carbon)"),
            Map.entry("ore_drill_head_large_steel", "Large Ore Drill (Steel)"),
            Map.entry("ore_drill_head_large_obsidian_steel", "Large Ore Drill (Obsidian Steel)"),
            Map.entry("ore_drill_head_large_carbon", "Large Ore Drill (Carbon)"),
            // Phase 5a2 (Techguns 2 lang: tile.techguns.*)
            Map.entry("lamp_yellow", "Lamp (Yellow)"),
            Map.entry("lamp_white", "Lamp (White)"),
            Map.entry("lamp_yellow_lantern", "Lantern (Yellow)"),
            Map.entry("lamp_white_lantern", "Lantern (White)"),
            Map.entry("ladder_metal", "Ladder (Metal)"),
            Map.entry("ladder_shiny", "Ladder (Shiny)"),
            Map.entry("ladder_rusty", "Ladder (Rusty)"),
            Map.entry("ladder_carbon", "Ladder (Carbon)"),
            Map.entry("slimy_ladder", "Slimy Trail"),
            Map.entry("camo_net_wood", "Camo Netting (Woodland)"),
            Map.entry("camo_net_desert", "Camo Netting (Desert)"),
            Map.entry("camo_net_snow", "Camo Netting (Snow)"),
            Map.entry("camo_net_top_wood", "Camo Netting Top (Woodland)"),
            Map.entry("camo_net_top_desert", "Camo Netting Top (Desert)"),
            Map.entry("camo_net_top_snow", "Camo Netting Top (Snow)"),
            Map.entry("military_crate_ammo", "Ammo Crate"),
            Map.entry("military_crate_gun", "Weapon Crate"),
            Map.entry("military_crate_armor", "Armor Crate"),
            Map.entry("military_crate_medical", "Medical Crate"),
            Map.entry("military_crate_explosive", "Explosives Crate"),
            Map.entry("military_crate_generic_oak", "Supply Crate"),
            Map.entry("military_crate_generic_jungle", "Supply Crate"),
            Map.entry("military_crate_generic_birch", "Supply Crate"),
            Map.entry("military_crate_generic_spruce", "Supply Crate"),
            Map.entry("door_metal_3x3", "Techdoor"),
            Map.entry("door_hangar_up_3x3", "Hangar Door (Up)"),
            Map.entry("door_hangar_down_3x3", "Hangar Door (Down)"),
            Map.entry("door_nether_3x3", "Techdoor (Nether)"),
            Map.entry("tgx", "TGX"),
            Map.entry("heat_ray_focus", "Reaction Chamber Heat Ray Focus"),
            Map.entry("uv_emitter", "Reaction Chamber UV Emitter"),
            Map.entry("obsidian_steel_receiver", "Hardened Receiver"),
            Map.entry("obsidian_steel_barrel", "Hardened Barrel"),
            Map.entry("gauss_barrel", "Gauss Rifle Barrel"),
            Map.entry("wood_stock", "Wooden Gun Stock"),
            Map.entry("plastic_stock", "Plastic Gun Stock"),
            Map.entry("carbon_stock", "Carbon Gun Stock"),
            Map.entry("steam_armor_plate", "Steam Armor Plating"),
            Map.entry("mechanical_parts_iron", "Mechanical Parts (Iron)"),
            Map.entry("mechanical_parts_obsidian_steel", "Mechanical Parts (Hardened)"),
            Map.entry("mechanical_parts_carbon", "Mechanical Parts (Carbon)"),
            Map.entry("circuit_board_elite", "Elite Circuit Board"),
            Map.entry("circuit_board_advanced", "Advanced Circuit Board"),
            Map.entry("power_plating", "Power Armor Plating"),
            Map.entry("power_plating_mk2", "Power Armor Plating Mk2"),
            Map.entry("antigrav_core", "Anti Gravity Core"),
            Map.entry("night_vision_goggles", "Nightvision Goggles"),
            Map.entry("jump_pack", "Jumppack"),
            Map.entry("scuba_tanks", "Oxygen Tanks"),
            Map.entry("antigrav_pack", "Anti Gravity Device"),
            Map.entry("m4", "M4"),
            Map.entry("ak47", "AKM"),
            Map.entry("m4_infiltrator", "The Infiltrator"),
            Map.entry("aug", "AUG"),
            Map.entry("scar", "SCAR"),
            Map.entry("thompson", "Thompson SMG"),
            Map.entry("mac10", "MAC-10"),
            Map.entry("lmg", "Light Machine Gun"),
            Map.entry("bolt_action", "Bolt Action Rifle"),
            Map.entry("smg_magazine", "SMG Magazine"),
            Map.entry("smg_magazine_empty", "SMG Magazine (Empty)"),
            Map.entry("smg_magazine_incendiary", "SMG Magazine (Incendiary)"),
            Map.entry("lmg_magazine", "LMG Magazine"),
            Map.entry("lmg_magazine_empty", "LMG Magazine (Empty)"),
            Map.entry("lmg_magazine_incendiary", "LMG Magazine (Incendiary)"),
            Map.entry("pistol_magazine_empty", "Pistol Magazine (Empty)"),
            Map.entry("pistol_magazine_incendiary", "Pistol Magazine (Incendiary)"),
            Map.entry("pistol_rounds_incendiary", "Pistol Rounds (Incendiary)"),
            Map.entry("rifle_rounds_incendiary", "Rifle Rounds (Incendiary)"),
            Map.entry("ammo_box", "Ammo Box (Small)"),
            Map.entry("concrete_brown", "Reinforced Concrete"),
            Map.entry("concrete_brown_light", "Reinforced Concrete (Light)"),
            Map.entry("concrete_grey", "Reinforced Concrete (Grey)"),
            Map.entry("concrete_grey_dark", "Reinforced Concrete (Dark)"),
            Map.entry("concrete_brown_pipes", "Reinforced Concrete (Pipes)"),
            Map.entry("concrete_brown_light_scaff", "Reinforced Concrete (Steel Bars)"),
            Map.entry("machine_upgrade_stack", "Machine Upgrade (Stack Operation)"),
            Map.entry("upgrade_protection_1", "Armor Upgrade: Protection I"),
            Map.entry("upgrade_blast_protection_1", "Armor Upgrade: Blast Protection I"),
            Map.entry("upgrade_projectile_protection_1", "Armor Upgrade: Projectile Protection I"),
            Map.entry("upgrade_protection_2", "Armor Upgrade: Protection II"),
            Map.entry("upgrade_blast_protection_2", "Armor Upgrade: Blast Protection II"),
            Map.entry("upgrade_projectile_protection_2", "Armor Upgrade: Projectile Protection II"),
            Map.entry("upgrade_protection_3", "Armor Upgrade: Protection III"),
            Map.entry("upgrade_blast_protection_3", "Armor Upgrade: Blast Protection III"),
            Map.entry("upgrade_projectile_protection_3", "Armor Upgrade: Projectile Protection III"),
            Map.entry("container_red", "Red Container"),
            Map.entry("container_green", "Green Container"),
            Map.entry("container_blue", "Blue Container"),
            Map.entry("container_orange", "Yellow Container"),
            Map.entry("panel_large_border", "Steel Panel"),
            Map.entry("steelframe_blue", "Silver Steel Panel"),
            Map.entry("steelframe_dark", "Carbon Panel"),
            Map.entry("steelframe_scaffold", "Dark Scaffold"),
            Map.entry("nethermetal_panel", "Nether Metal (Plate)"),
            Map.entry("nethermetal_grate1", "Nether Metal (Grate)"),
            Map.entry("nethermetal_grate2", "Nether Metal (Small Grate)"),
            Map.entry("nethermetal_grey_dark", "Nether Metal (Bloody Grey)"),
            Map.entry("nethermetal_grey", "Nether Metal (Grey)"),
            Map.entry("nethermetal_grey_tiles", "Nether Metal (Grey Tiles)"),
            Map.entry("nethermetal_border_red", "Nether Metal (Red Pillar)"),
            Map.entry("nethermetal_plate_black", "Nether Metal (Dark Pillar)"),
            Map.entry("nethermetal_plate_red", "Nether Metal (Red Panel)"),
            Map.entry("nethermetal_border_lava", "Nether Metal (Lava Pillar)"),
            Map.entry("neontubes2", "Double Neon Lights"),
            Map.entry("neontubes2_rotated", "Double Neon Lights (Rotated)"),
            Map.entry("neontubes4", "Quad Neon Lights"),
            Map.entry("neontubes4_rotated", "Quad Neon Lights (Rotated)"),
            Map.entry("neonsquare_white", "Square Neon Light"),
            Map.entry("bugnest_sand", "Hardened Sand"),
            Map.entry("bugnest_eggs", "Strange Glowing Eggs"),
            Map.entry("panel_large_border_stairs", "Steel Panel Stairs"),
            Map.entry("steelframe_dark_stairs", "Carbon Panel Stairs"),
            Map.entry("concrete_grey_dark_stairs", "Reinforced Concrete Stairs (Dark)"),
            Map.entry("concrete_brown_light_stairs", "Reinforced Concrete Stairs (Light)"),
            Map.entry("t1_combat_helmet", "Soldier's Helmet"),
            Map.entry("t1_combat_chestplate", "Soldier's Vest"),
            Map.entry("t1_combat_leggings", "Soldier's Pants"),
            Map.entry("t1_combat_boots", "Soldier's Boots"),
            Map.entry("t1_scout_helmet", "Bandit Mask"),
            Map.entry("t1_scout_chestplate", "Bandit Vest"),
            Map.entry("t1_scout_leggings", "Bandit Pants"),
            Map.entry("t1_scout_boots", "Bandit Boots"),
            Map.entry("t1_miner_helmet", "Miner's Helmet"),
            Map.entry("t1_miner_chestplate", "Miner's Jacket"),
            Map.entry("t1_miner_leggings", "Miner's Pants"),
            Map.entry("t1_miner_boots", "Miner's Boots"),
            Map.entry("steam_helmet", "Steam Helmet"),
            Map.entry("steam_chestplate", "Steam Chestplate"),
            Map.entry("steam_leggings", "Steam Legplates"),
            Map.entry("steam_boots", "Steam Boots"),
            Map.entry("t3_power_helmet", "Power Armor Helmet"),
            Map.entry("t3_power_chestplate", "Power Armor Chestplate"),
            Map.entry("t3_power_leggings", "Power Armor Leggings"),
            Map.entry("t3_power_boots", "Power Armor Boots"),
            Map.entry("t3_miner_helmet", "HEV Suit Helmet"),
            Map.entry("t3_miner_chestplate", "HEV Suit Chestplate"),
            Map.entry("t3_miner_leggings", "HEV Suit Leggings"),
            Map.entry("t3_miner_boots", "HEV Suit Boots"),
            Map.entry("t3_exo_helmet", "Exo Suit Helmet"),
            Map.entry("t3_exo_chestplate", "Exo Suit Chestplate"),
            Map.entry("t3_exo_leggings", "Exo Suit Leggings"),
            Map.entry("t3_exo_boots", "Exo Suit Boots"),
            Map.entry("t4_praetor_helmet", "Nether Combat Helmet"),
            Map.entry("t4_praetor_chestplate", "Nether Combat Chestplate"),
            Map.entry("t4_praetor_leggings", "Nether Combat Leggings"),
            Map.entry("t4_praetor_boots", "Nether Combat Boots"),
            Map.entry("t4_power_helmet", "Power Armor Mk2 Helmet"),
            Map.entry("t4_power_chestplate", "Power Armor Mk2 Chestplate"),
            Map.entry("t4_power_leggings", "Power Armor Mk2 Leggings"),
            Map.entry("t4_power_boots", "Power Armor Mk2 Boots"),
            Map.entry("hazmat_helmet", "Hazmat Suit Helmet"),
            Map.entry("hazmat_chestplate", "Hazmat Suit Chestplate"),
            Map.entry("hazmat_leggings", "Hazmat Suit Leggings"),
            Map.entry("hazmat_boots", "Hazmat Suit Boots"),
            Map.entry("t2_combat_helmet", "Combat Helmet"),
            Map.entry("t2_combat_chestplate", "Combat Vest"),
            Map.entry("t2_combat_leggings", "Combat Pants"),
            Map.entry("t2_combat_boots", "Combat Boots"),
            Map.entry("t2_commando_helmet", "Commando Headgear"),
            Map.entry("t2_commando_chestplate", "Commando Vest"),
            Map.entry("t2_commando_leggings", "Commando Pants"),
            Map.entry("t2_commando_boots", "Commando Boots"),
            Map.entry("t2_riot_helmet", "Ranger Veteran Helmet"),
            Map.entry("t2_riot_chestplate", "Ranger Veteran Jacket"),
            Map.entry("t2_riot_leggings", "Ranger Veteran Pants"),
            Map.entry("t2_riot_boots", "Ranger Veteran Boots"),
            Map.entry("t3_combat_helmet", "Advanced Combat Helmet"),
            Map.entry("t3_combat_chestplate", "Advanced Combat Chestplate"),
            Map.entry("t3_combat_leggings", "Advanced Combat Leggings"),
            Map.entry("t3_combat_boots", "Advanced Combat Boots"),
            Map.entry("t2_beret", "Beret"),
            Map.entry("ammo_box_large", "Ammo Box (Large)"),
            Map.entry("rifle_rounds_stack_incendiary", "Rifle Rounds Stack (Incendiary)"),
            Map.entry("assault_rifle_magazine_incendiary", "Assault Rifle Magazine (Incendiary)"),
            Map.entry("minigun_drum", "Minigun Ammo Drum"),
            Map.entry("minigun_drum_empty", "Minigun Ammo Drum (Empty)"),
            Map.entry("minigun_drum_incendiary", "Minigun Ammo Drum (Incendiary)"),
            Map.entry("assault_rifle_magazine_empty", "Assault Rifle Magazine (Empty)"),
            Map.entry("sawed_off", "Double Barrel Shotgun"),
            Map.entry("combat_shotgun", "Combat Shotgun"),
            Map.entry("as50", "AS50 Sniper Rifle"),
            Map.entry("pdw", "PDW"),
            Map.entry("shotgun_rounds_incendiary", "Shotgun Rounds (Incendiary)"),
            Map.entry("sniper_rounds_incendiary", "Sniper Rounds (Incendiary)"),
            Map.entry("sniper_rounds_explosive", "Sniper Rounds (Explosive)"),
            Map.entry("as50_magazine", "AS50 Magazine"),
            Map.entry("as50_magazine_empty", "AS50 Magazine (Empty)"),
            Map.entry("as50_magazine_incendiary", "AS50 Magazine (Incendiary)"),
            Map.entry("as50_magazine_explosive", "AS50 Magazine (Explosive)"),
            Map.entry("advanced_magazine_empty", "Advanced Magazine (Empty)"),
            Map.entry("nuclear_deathray", "Nuclear Death Ray"),
            Map.entry("sonic_shotgun", "Sonic Rifle"),
            Map.entry("mib_gun", "De-Atomizer Pistol"),
            Map.entry("redstone_battery_empty", "Redstone Battery (Empty)"),
            Map.entry("nuclear_power_cell_empty", "Nuclear Power Cell (Depleted)"),
            Map.entry("rocket_nuke", "Rocket (Tactical Nuke)"),
            Map.entry("rocket_high_velocity", "Rocket (High Velocity)"),
            Map.entry("stielgranate", "Stick Grenade"),
            Map.entry("guided_missile_launcher", "Guided Missile Launcher"),
            Map.entry("grenade_40mm", "40mm Grenade"),
            Map.entry("grim_reaper", "Grim Reaper"),
            Map.entry("frag_grenade", "Frag Grenade"),
            Map.entry("tfg", "TFG 10K"),
            Map.entry("bioblob", "Bio Blob"),
            Map.entry("fuel_tank_empty", "Fuel Tank (Empty)"),
            Map.entry("bio_tank_empty", "Bio Tank (Empty)"),
            Map.entry("magic_bullet_fire", "Magic Bullet (Fire)"),
            Map.entry("magic_bullet_lightning", "Magic Bullet (Lightning)"),
            Map.entry("compressed_air_tank_empty", "Compressed Air Tank (Empty)"),
            Map.entry("chainsaw_blades_steel", "Chainsaw Blades (Steel)"),
            Map.entry("chainsaw_blades_obsidian", "Chainsaw Blades (Obsidian)"),
            Map.entry("chainsaw_blades_carbon", "Chainsaw Blades (Carbon)"),
            Map.entry("mining_drill_head_obsidian", "Mining Drill Head (Obsidian)"),
            Map.entry("mining_drill_head_carbon", "Mining Drill Head (Carbon)"),
            Map.entry("power_hammer_head_steel", "Power Hammer Head (Steel)"),
            Map.entry("power_hammer_head_obsidian", "Power Hammer Head (Obsidian)"),
            Map.entry("power_hammer_head_carbon", "Power Hammer Head (Carbon)"));

    @Override
    protected void addTranslations() {
        addConfig();
        addDamage();
        addItemsAndBlocks();
        add(TGCreativeTabs.MAIN_TITLE, "Techguns");
        add(TGKeyMappings.CATEGORY, "Techguns");
        add(TGKeyMappings.FORCE_RELOAD.getName(), "Force Reload");
        add(TGKeyMappings.TOGGLE_SAFE_MODE.getName(), "Toggle Safe Mode");
        add(TGKeyMappings.TOGGLE_STEP_ASSIST.getName(), "Toggle Step Assist");
        addGunTooltips();
        addMachines();
        addRadiation();
    }

    /** Techguns 2 radiation effects, potions, HUD and tooltip. */
    private void addRadiation() {
        add(TGMobEffects.RADIATION.get(), "Radiation");
        add(TGMobEffects.RAD_REGENERATION.get(), "Rad Regeneration");
        add(TGMobEffects.RAD_RESISTANCE.get(), "Rad Resistance");
        String[][] potions = {{"techguns.radiation", "Radiation"}, {"techguns.rad_regeneration", "Rad Regeneration"},
                {"techguns.rad_resistance", "Rad Resistance"}};
        for (String[] potion : potions) {
            add("item.minecraft.potion.effect." + potion[0], "Potion of " + potion[1]);
            add("item.minecraft.splash_potion.effect." + potion[0], "Splash Potion of " + potion[1]);
            add("item.minecraft.lingering_potion.effect." + potion[0], "Lingering Potion of " + potion[1]);
            add("item.minecraft.tipped_arrow.effect." + potion[0], "Arrow of " + potion[1]);
        }
        add("techguns.hud.radiation", "%s RAD");
        add("techguns.hud.radiation_rate", "+[%s RAD/s]");
        add("techguns.tooltip.radiation", "Radiation");
    }

    /** Techguns 2 machine screen texts. */
    private void addMachines() {
        add("techguns.container.power", "Power");
        add("techguns.container.power.amount", "Power: %s/%s FE");
        add("techguns.container.redstone", "Redstone Info");
        add("techguns.container.redstone.mode", "Required: %s");
        add("techguns.container.redstone.signal", "Signal: %s");
        add("techguns.redstonemode.ignore", "ignored");
        add("techguns.redstonemode.high", "high");
        add("techguns.redstonemode.low", "low");
        add("techguns.redstonemode.signal.high", "high");
        add("techguns.redstonemode.signal.low", "low");
        add("techguns.container.security.owner", "Owned by: %s");
        add("techguns.container.security.tooltip", "Security: %s");
        add("techguns.container.security.tooltip.0", "Public");
        add("techguns.container.security.tooltip.3", "Owner");
        add("techguns.container.security.tooltip.description.0", "Anyone can access");
        add("techguns.container.security.tooltip.description.3", "Only owner can access");
        add("techguns.container.security.tooltip.descr2", "Only the owner can change this setting");
        add("techguns.container.security.tooltip.unowned", "Unowned (Replace Block to set ownership)");
        add("techguns.container.security.denied", "[DENIED] You have no permission to use %s");
        add("techguns.container.metalpress.autosplit", "Stack split:");
        // Phase 5b3 benches (Techguns 2 / Fabric lang)
        add("techguns.repairbench.noarmor", "No armor");
        add("techguns.repairbench.cantrepair", "Can't repair this armor");
        add("techguns.repairbench.fullcondition", "Armor is fully repaired");
        add("techguns.repairbench.requiredmats", "Required materials for repair:");
        add("techguns.item.invalidcamo", "No Camo");
        add("techguns.gui.xp", "XP: %s");
        add("techguns.gui.xpcost", "XP Cost: %s");
        add("techguns.container.ammobench", "Ammo Bench");
        // Phase 5c multiblocks
        add("techguns.container.fabricator", "Fabricator");
        // recipe viewer categories (Techguns 2 container names)
        add("techguns.container.metalpress", "Metal Press");
        add("techguns.container.ammopress", "Ammo Press");
        add("techguns.container.blastfurnace", "Blast Furnace");
        add("techguns.container.grinder", "Grinder");
        add("techguns.container.chargingstation", "Charging Station");
        add("techguns.jei.chance", "Chance: %s%%");
        add("techguns.container.chemlab", "Chem Lab");
        add("techguns.container.upgradebench", "Armor Upgrade Bench");
        add("techguns.container.camobench", "Camo Bench");
        add("techguns.jei.oredrill.mininglevel", "Mining level: %s");
        add("techguns.jei.oredrill.oremult", "Ore multiplier: %s");
        add("techguns.jei.oredrill.powermult", "Power multiplier: %s");
        add("techguns.jei.reactionchamber.ticks", "Ticks: %s");
        add("techguns.jei.reactionchamber.intensity", "Intensity: %s (±%s)");
        add("techguns.jei.reactionchamber.completion", "Completion: %s in %s ticks");
        add("techguns.jei.reactionchamber.liquid", "Liquid level: %s, uses %s per success");
        add("techguns.jei.reactionchamber.instability", "Instability: %s%%");
        add("techguns.jei.reactionchamber.risk", "Risk: %s");
        add("techguns.container.reactionchamber", "Reaction Chamber");
        add("techguns.reactionchamber.risk.break_item", "Break Item");
        add("techguns.reactionchamber.risk.explosion_low", "Small Explosion");
        add("techguns.reactionchamber.risk.explosion_medium", "Medium Explosion");
        add("techguns.msg.error.multiblockinvalid", "Can't form multiblock: Invalid block placement");
        add("entity.techguns.turret", "Turret");
        add("entity.techguns.npc_spawn_marker", "Techguns NPC Spawn");
        add("techguns.container.turretbase", "Turret");
        add("techguns.turret.health", "Turret Health");
        add("techguns.turret.targetAnimals", "Target Animals");
        add("techguns.turret.pvpsetting", "PvP Setting");
        add("techguns.turret.pvpsetting.0", "§aNo PvP");
        add("techguns.turret.pvpsetting.1", "Attack §eEnemies");
        add("techguns.turret.pvpsetting.2", "Attack §6Neutrals");
        add("techguns.turret.pvpsetting.3", "Attack §cAllies of Team");
        add("techguns.turret.pvpsetting.4", "Attack §cEveryone except Owner");
        add("techguns.explosivecharge.radius", "Radius: %s");
        add("techguns.explosivecharge.length", "Length: %s");
        add("techguns.explosivecharge.delay", "Delay: %ss");
        add("techguns.explosivecharge.detonate", "Detonate");
        add("techguns.configuration.explosiveChargeMaxHardness", "Explosive Charge Max Hardness");
        add("techguns.configuration.explosiveChargeAdvancedMaxHardness", "Advanced Explosive Charge Max Hardness");
        add("techguns.container.oredrill", "Ore Drill");
        add("techguns.container.oredrill.broken", "Multiblock broken");
        add("techguns.container.oredrill.drillrequired.small", "Requires Small Drill");
        add("techguns.container.oredrill.drillrequired.medium", "Requires Medium Drill");
        add("techguns.container.oredrill.drillrequired.large", "Requires Large Drill");
        add("techguns.msg.error.oredrill.controlleralone", "Can't form multiblock: No connected ore drill engines/rods");
        add("techguns.msg.error.oredrill.nocluster", "Can't form multiblock: The rods must end at an ore cluster");
        add("techguns.msg.error.oredrill.rodsize", "Rod must be at least as long as drill diameter and engine size. "
                + "Engine height must be at least as high as drill radius.");
        add("techguns.gui.oredrill.rftick", "FE/t");
        add("techguns.gui.oredrill.orehour", "Ores/h");
        add("techguns.oredrill.nofuel", "No Fuel");
        add("techguns.oredrill.currentfuel", "Fuel");
        add("techguns.oredrill.remaining", "Remaining time");
        add("techguns.oredrill.ticks", "ticks.");
        add("techguns.oredrill.drillsize", "Drill Size");
        add("techguns.oredrill.drillradius", "Drill Radius");
        add("techguns.oredrill.drillsize.small", "Small Drill");
        add("techguns.oredrill.drillsize.medium", "Medium Drill");
        add("techguns.oredrill.drillsize.large", "Large Drill");
        add("techguns.oredrill.mininglevel", "Ore Drill Mining Level: %s");
        add("techguns.configuration.ore_drills", "Ore Drills");
        add("techguns.configuration.oreDrillMultiplierOre", "Ore Drill Ore Multiplier");
        add("techguns.configuration.oreDrillMultiplierPower", "Ore Drill Power Multiplier");
        add("techguns.configuration.oreDrillFuelMultiplier", "Ore Drill Fuel Multiplier");
        add("techguns.configuration.oreDrillFuelValueFuel", "Ore Drill Liquid Fuel Value");
        add("techguns.container.ammopress.build", "Building:");
        add("techguns.gui.empty", "Empty");
        add("techguns.chemlab.dumpinput", "Dump Input Tank");
        add("techguns.chemlab.dumpoutput", "Dump Output Tank");
        add("techguns.chemlab.dump.tooltip", "Content will be lost");
        add("techguns.chemlab.toggledrain", "Toggle drained Tank");
        add("techguns.chemlab.toggledrain.tooltip.0", "Fluids are drained from Output Tank");
        add("techguns.chemlab.toggledrain.tooltip.1", "Fluids are drained from Input Tank");
        add("block.techguns.creeper_acid", "Creeper Acid");
        add("techguns.container.info.on", "On");
        add("techguns.container.info.off", "Off");
        add("techguns.button.switch", "Switch");
        add("techguns.configuration.machines", "Machines");
        add("techguns.configuration.machinesNeedNoPower", "Machines Need No Power");
    }

    private void addGunTooltips() {
        add("tooltip.techguns.gun.ammo", "Ammo: %s/%s");
        add("tooltip.techguns.gun.ammo_item", "Loads: %s");
        add("tooltip.techguns.gun.damage", "Damage: %s");
        add("tooltip.techguns.gun.damage_type", "Damage type: %s");
        add("tooltip.techguns.gun.range", "Range: %s");
        add("tooltip.techguns.gun.reload_time", "Reload time: %ss");
        add("tooltip.techguns.gun.penetration", "Armor penetration: %s");
        add("tooltip.techguns.shift_for_details", "Hold Shift for details");
        add("tooltip.techguns.grenade.radius", "Radius: %s");
        add("tooltip.techguns.grenade.bounces", "Bounces: %s");
        add("tooltip.techguns.armor.durability", "Durability: %s/%s");
        add("tooltip.techguns.armor.toughness", "Toughness: %s");
        // armor bonus attributes (Techguns 2 armor tooltip names)
        add("attribute.name.techguns.speed_bonus", "Move Speed");
        add("attribute.name.techguns.water_speed_bonus", "Swim Speed");
        add("attribute.name.techguns.jump_bonus", "Jump Height");
        add("attribute.name.techguns.fall_damage_reduction", "Fall Damage Reduction");
        add("attribute.name.techguns.fall_free_height", "Safe Fall Height");
        add("attribute.name.techguns.mining_speed_bonus", "Mining Speed");
        add("attribute.name.techguns.water_mining_speed_bonus", "Underwater Mining Speed");
        add("attribute.name.techguns.gun_accuracy", "Gun Accuracy");
        add("attribute.name.techguns.cooling_system", "Cooling System");
        add("attribute.name.techguns.radiation_resistance", "Radiation Resistance");
        add("attribute.name.techguns.oxygen_gear", "Oxygen Gear");
        add("attribute.name.techguns.step_assist", "Step Assist");
        add("tooltip.techguns.armor.power", "Power: %s/%s");
        add("tooltip.techguns.armor.power_type", "Power type: %s");
        add("tooltip.techguns.armor.power_type_battery", "Power type: %s (%s)");
        add("tooltip.techguns.armor.power_type.steam", "Steam");
        add("tooltip.techguns.armor.power_type.rf", "Electric");
        add("message.techguns.toggle.step_assist", "Step Assist: %s");
        add("message.techguns.toggle.night_vision", "Night Vision: %s");
        add("message.techguns.toggle.jetpack", "Jetpack: %s");
        add(TGKeyMappings.TOGGLE_NIGHT_VISION.getName(), "Toggle Night Vision");
        add(TGKeyMappings.TOGGLE_JETPACK.getName(), "Toggle Jetpack");
        add("tooltip.techguns.slot", "Slot: %s");
        add("gui.techguns.extra_slots", "Techguns Gear");
        add("gui.techguns.extra_slots.button", "TG");
        add("gui.techguns.inventory", "<");
        add("gui.techguns.inventory.tooltip", "Back to inventory");
        add("tooltip.techguns.slot.face", "Face");
        add("tooltip.techguns.slot.back", "Back");
        add("tooltip.techguns.slot.hand", "Hand");
        add("tooltip.techguns.slot.charge", "Charge: %s/%s");
        add("tooltip.techguns.slot.refill", "Recharges with: %s");
        add("attribute.name.techguns.night_vision", "Night Vision");
        add("attribute.name.techguns.fly_speed_bonus", "Flying Speed");
        add("message.techguns.on", "On");
        add("message.techguns.camo_switch", "Switched camo to %s");
        add("tooltip.techguns.camo", "Camo: %s");
        add("tooltip.techguns.shield.blocks", "Blocks (Techguns damage):");
        TGCamoProvider.englishNames().forEach((name, english) -> add("camo.techguns." + name, english));
        add("message.techguns.off", "Off");
        add("techguns.lock_on.locking", "Locking... : %s");
        add("techguns.lock_on.locked", "Locked On: %s");
    }

    /** Every block and item gets an English name; title-cased from its registry name unless overridden. */
    private void addItemsAndBlocks() {
        techguns.registry.TGNpcs.npcs().forEach(npc -> add(npc.type().get(), englishName(npc.name())));
        Set<String> named = new HashSet<>();
        for (DeferredBlock<? extends Block> block : TGBlocks.blocksWithItem()) {
            String path = block.getId().getPath();
            add(block.get(), englishName(path));
            named.add(path);
        }
        for (DeferredHolder<Item, ? extends Item> item : TGItems.ITEMS.getEntries()) {
            String path = item.getId().getPath();
            if (!named.contains(path)) {
                add(item.get(), englishName(path));
            }
        }
    }

    private static String englishName(String path) {
        String override = NAME_OVERRIDES.get(path);
        if (override != null) {
            return override;
        }
        StringBuilder name = new StringBuilder();
        for (String word : path.split("_")) {
            if (!name.isEmpty()) {
                name.append(' ');
            }
            name.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return name.toString();
    }

    private void addConfig() {
        add("techguns.configuration.title", "Techguns 3 Configs");
        add("techguns.configuration.section.techguns.common.toml", "Techguns 3 Common Config");
        add("techguns.configuration.section.techguns.common.toml.title", "Techguns 3 Common Config");
        add("techguns.configuration.debugLogging", "Debug Logging");
        add("techguns.configuration.damage", "Damage");
        add("techguns.configuration.tgArmorSystem", "Techguns Armor System");
        add("techguns.configuration.damagePvP", "PvP Damage Multiplier");
        add("techguns.configuration.damageFactorNPC", "NPC Damage Multiplier");
    }

    private void addDamage() {
        for (TGDamageCategory category : TGDamageCategory.values()) {
            String name = displayName(category);
            add("techguns.damage_category." + category.id(), name);
            if (category.isResistable()) {
                add("attribute.name.techguns.armor." + category.id(), name + " Armor");
            }
        }

        deathMessage(TGDamageTypes.BULLET, "%1$s was shot by %2$s");
        deathMessage(TGDamageTypes.EXPLOSION, "%1$s was blown up by %2$s");
        deathMessage(TGDamageTypes.FIRE, "%1$s was burned to a crisp by %2$s");
        deathMessage(TGDamageTypes.ENERGY, "%1$s was vaporized by %2$s");
        deathMessage(TGDamageTypes.POISON, "%1$s was poisoned by %2$s");
        deathMessage(TGDamageTypes.ICE, "%1$s was frozen by %2$s");
        deathMessage(TGDamageTypes.LIGHTNING, "%1$s was electrocuted by %2$s");
        deathMessage(TGDamageTypes.RADIATION, "%1$s was irradiated by %2$s");
        deathMessage(TGDamageTypes.DARK, "%1$s was consumed by darkness from %2$s");
        deathMessage(TGDamageTypes.PHYSICAL, "%1$s was slain by %2$s");
        add("death.attack." + TGDamageTypes.msgId(TGDamageTypes.RADIATION_POISONING), "%1$s died of radiation poisoning");
        add("death.attack." + TGDamageTypes.msgId(TGDamageTypes.RADIATION_POISONING) + ".player", "%1$s died of radiation poisoning");
    }

    /** Death message for a damage type, with and without an attacker. */
    private void deathMessage(ResourceKey<DamageType> type, String withAttacker) {
        String key = "death.attack." + TGDamageTypes.msgId(type);
        add(key, withAttacker.replace(" by %2$s", "").replace(" from %2$s", ""));
        add(key + ".player", withAttacker);
    }

    private static String displayName(TGDamageCategory category) {
        String id = category.id();
        return Character.toUpperCase(id.charAt(0)) + id.substring(1);
    }
}
