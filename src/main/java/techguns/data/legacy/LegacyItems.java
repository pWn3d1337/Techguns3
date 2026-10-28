package techguns.data.legacy;

import java.util.Map;

import javax.annotation.Nullable;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import techguns.TGIds;

/** Techguns 2 item ids (and {@code itemshared} metas) to Techguns 3 items. */
final class LegacyItems {
    /**
     * Techguns 2 {@code itemshared} metas used by converted data. Metas and names from the Techguns 2 runtime
     * (structure dump), Techguns 3 names flattened.
     */
    private static final Map<Integer, String> SHARED = Map.ofEntries(
            Map.entry(0, "stone_bullets"), Map.entry(1, "pistol_rounds"), Map.entry(2, "shotgun_rounds"), Map.entry(3, "rifle_rounds"), Map.entry(4, "sniper_rounds"),
            Map.entry(9, "smg_magazine"), Map.entry(11, "pistol_magazine"), Map.entry(13, "assault_rifle_magazine"),
            Map.entry(27, "fuel_tank"), Map.entry(28, "fuel_tank_empty"), Map.entry(55, "plastic_sheet"), Map.entry(56, "rubber_bar"),
            Map.entry(57, "mechanical_parts_iron"), Map.entry(58, "mechanical_parts_obsidian_steel"), Map.entry(60, "heavy_cloth"),
            Map.entry(65, "circuit_board"), Map.entry(69, "cybernetic_parts"), Map.entry(70, "electric_engine"),
            Map.entry(84, "obsidian_steel_ingot"), Map.entry(94, "machine_upgrade_stack"), Map.entry(99, "turret_armor_iron"),
            Map.entry(104, "heat_ray_focus"), Map.entry(137, "ore_drill_head_medium_steel"),
            Map.entry(138, "ore_drill_head_medium_obsidian_steel"), Map.entry(147, "upgrade_protection_1"),
            Map.entry(106, "shotgun_rounds_incendiary"), Map.entry(108, "sniper_rounds_incendiary"), Map.entry(109, "pistol_rounds_incendiary"),
            Map.entry(110, "rifle_rounds_incendiary"), Map.entry(112, "smg_magazine_incendiary"), Map.entry(113, "pistol_magazine_incendiary"),
            Map.entry(115, "assault_rifle_magazine_incendiary"));

    /** Techguns 2 item names that changed. */
    private static final Map<String, String> RENAMED = Map.of("boltaction", "bolt_action", "combatshotgun", "combat_shotgun", "radaway", "rad_away", "radpills", "rad_pills",
            "handcannon", "hand_cannon");

    private LegacyItems() {
    }

    /** The Techguns 3 item, or null if it is not ported (yet). */
    @Nullable
    static Item item(String legacyName, int meta) {
        int colon = legacyName.indexOf(':');
        String namespace = legacyName.substring(0, colon);
        String path = legacyName.substring(colon + 1);
        if (namespace.equals("techguns")) {
            path = path.equals("itemshared") ? SHARED.get(meta) : RENAMED.getOrDefault(path, path);
            if (path == null) {
                throw new IllegalArgumentException("No mapping for itemshared meta " + meta);
            }
        }
        Item item = BuiltInRegistries.ITEM.get(TGIds.of(namespace, path));
        return item == Items.AIR ? null : item;
    }
}
