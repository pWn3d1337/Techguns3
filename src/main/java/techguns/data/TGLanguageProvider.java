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
import techguns.registry.TGItems;

public class TGLanguageProvider extends LanguageProvider {
    public TGLanguageProvider(PackOutput output) {
        super(output, Techguns.MODID, "en_us");
    }

    /** English names that differ from the title-cased registry name. */
    private static final Map<String, String> NAME_OVERRIDES = Map.ofEntries(
            Map.entry("biomass", "Bio Mass"),
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
            Map.entry("assault_rifle_magazine_incendiary", "Assault Rifle Magazine (Incendiary)"),
            Map.entry("minigun_drum", "Minigun Ammo Drum"),
            Map.entry("minigun_drum_empty", "Minigun Ammo Drum (Empty)"),
            Map.entry("minigun_drum_incendiary", "Minigun Ammo Drum (Incendiary)"),
            Map.entry("assault_rifle_magazine_empty", "Assault Rifle Magazine (Empty)"));

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
    }

    /** Every block and item gets an English name; title-cased from its registry name unless overridden. */
    private void addItemsAndBlocks() {
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
