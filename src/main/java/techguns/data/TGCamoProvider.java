package techguns.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import techguns.TGIds;
import techguns.camo.TGCamo;
import techguns.registry.TGArmors;
import techguns.registry.TGCamos;
import techguns.registry.TGGuns;

/**
 * Camo entries of the {@code techguns:camo} datapack registry (Techguns 2 texture lists and camo names, Fabric
 * {@code TGCamos} ids). Each item's first camo (order 0) is its default look.
 */
public final class TGCamoProvider {
    private TGCamoProvider() {
    }

    /** How a camo's texture is applied. */
    private enum Kind {
        /** {@code textures/guns/<texture>.png} */
        GUN,
        /** armor layer textures {@code textures/models/armor/<texture>_layer_1/_2.png} */
        ARMOR_LAYERS,
        /** one armor model texture {@code textures/models/armor/<texture>.png} */
        ARMOR_SINGLE,
        /** shield model texture {@code textures/armors/<texture>.png} */
        SHIELD,
        /** extra-slot gear model texture {@code textures/armors/<texture>.png} */
        SLOT
    }

    /** One camo: id, English name, texture base name (without path and extension). */
    public record Camo(String name, String english, String texture) {
    }

    /** The camos of a group of items (a gun, or all pieces of an armor set). */
    private record Group(List<? extends ItemLike> items, Kind kind, List<Camo> camos) {
    }

    private static Camo camo(String name, String english, String texture) {
        return new Camo(name, english, texture);
    }

    /** All camo definitions. */
    private static List<Group> groups() {
        List<Group> groups = new ArrayList<>();
        // --- Guns ---
        groups.add(new Group(List.of(TGGuns.M4.get()), Kind.GUN, List.of(
                camo("default", "Default", "m4texture"),
                camo("swamp", "Swamp", "m4texture_1"),
                camo("desert", "Desert", "m4texture_2"),
                camo("ice", "Ice", "m4texture_3"),
                camo("hazard", "Hazard", "m4texture_4"))));
        groups.add(new Group(List.of(TGGuns.AK47.get()), Kind.GUN, List.of(
                camo("default", "Default", "ak47texture"),
                camo("blue", "Blue", "ak47texture_1"))));
        groups.add(new Group(List.of(TGGuns.AUG.get()), Kind.GUN, List.of(
                camo("default", "Default", "augtexture"),
                camo("grey", "Grey", "augtexture_1"),
                camo("wood", "Wood", "augtexture_2"),
                camo("sand", "Sand", "augtexture_3"),
                camo("nether", "Nether", "augtexture_4"))));
        groups.add(new Group(List.of(TGGuns.BOLT_ACTION.get()), Kind.GUN, List.of(
                camo("default", "Default", "boltactionrifle"),
                camo("woodland", "Woodland", "boltactionrifle_1"),
                camo("night", "Night", "boltactionrifle_2"))));
        groups.add(new Group(List.of(TGGuns.VECTOR.get()), Kind.GUN, List.of(
                camo("default", "Default", "vector_texture"),
                camo("black", "Black", "vector_texture_1"))));
        groups.add(new Group(List.of(TGGuns.SCAR.get()), Kind.GUN, List.of(
                camo("default", "Default", "scar_texture"),
                camo("black", "Black", "scar_texture_1"))));
        groups.add(new Group(List.of(TGGuns.PULSE_RIFLE.get()), Kind.GUN, List.of(
                camo("default", "Default", "pulserifle"),
                camo("grey", "Grey", "pulserifle_1"),
                camo("dark_green", "Dark Green", "pulserifle_2"))));
        groups.add(new Group(List.of(TGGuns.PDW.get()), Kind.GUN, List.of(
                camo("default", "Default", "pdw"),
                camo("silver", "Silver", "pdw_1"),
                camo("dark_green", "Dark Green", "pdw_2"))));
        groups.add(new Group(List.of(TGGuns.ROCKET_LAUNCHER.get()), Kind.GUN, List.of(
                camo("default", "Default", "rocketlauncher"),
                camo("nuclear", "Nuclear", "rocketlauncher_1"))));
        groups.add(new Group(List.of(TGGuns.SHISHKEBAP.get()), Kind.GUN, List.of(
                camo("rusty", "Rusty", "shishkebab_texture"),
                camo("silver", "Silver", "shishkebab_texture_1"),
                camo("dark", "Dark", "shishkebab_texture_2"))));
        groups.add(new Group(List.of(TGGuns.MAGIC_RIFLE.get()), Kind.GUN, List.of(
                camo("default", "Default", "magic_rifle"),
                camo("basic", "Basic", "magic_rifle_basic"),
                camo("rusty", "Rusty", "magic_rifle_rusty"),
                camo("black", "Black", "magic_rifle_black"),
                camo("silver", "Silver", "magic_rifle_silver"),
                camo("ivory", "Ivory", "magic_rifle_ivory"))));
        // --- Armor ---
        groups.add(new Group(List.of(TGArmors.T2_COMBAT_HELMET.get(), TGArmors.T2_COMBAT_CHESTPLATE.get(), TGArmors.T2_COMBAT_LEGGINGS.get(),
                TGArmors.T2_COMBAT_BOOTS.get()), Kind.ARMOR_LAYERS, List.of(
                camo("urban", "Urban", "t2_combat"),
                camo("woodland", "Woodland", "t2_combat_wood"),
                camo("desert", "Desert", "t2_combat_desert"),
                camo("arctic", "Arctic", "t2_combat_arctic"),
                camo("swat", "SWAT", "t2_combat_swat"),
                camo("security", "Security", "t2_combat_security"))));
        groups.add(new Group(List.of(TGArmors.T1_SCOUT_HELMET.get(), TGArmors.T1_SCOUT_CHESTPLATE.get(), TGArmors.T1_SCOUT_LEGGINGS.get(), TGArmors.T1_SCOUT_BOOTS.get()), Kind.ARMOR_LAYERS, List.of(
                camo("desert", "Desert", "t1_scout"),
                camo("forest", "Forest", "t1_scout_forest"),
                camo("snow", "Snow", "t1_scout_snow"),
                camo("black", "Black", "t1_scout_black"))));
        groups.add(new Group(List.of(TGArmors.T1_MINER_HELMET.get()), Kind.ARMOR_LAYERS, List.of(
                camo("yellow", "Yellow", "t1_miner"),
                camo("lime", "Lime", "t1_miner_red"),
                camo("cyan", "Light Blue", "t1_miner_green"),
                camo("orange", "Orange", "t1_miner_black"))));
        groups.add(new Group(List.of(TGArmors.T1_MINER_CHESTPLATE.get(), TGArmors.T1_MINER_LEGGINGS.get(), TGArmors.T1_MINER_BOOTS.get()), Kind.ARMOR_LAYERS, List.of(
                camo("blue", "Blue", "t1_miner"),
                camo("red", "Red", "t1_miner_red"),
                camo("green", "Green", "t1_miner_green"),
                camo("black", "Black", "t1_miner_black"))));
        groups.add(new Group(List.of(TGArmors.HAZMAT_HELMET.get(), TGArmors.HAZMAT_CHESTPLATE.get(), TGArmors.HAZMAT_LEGGINGS.get(), TGArmors.HAZMAT_BOOTS.get()), Kind.ARMOR_LAYERS, List.of(
                camo("yellow", "Yellow", "hazmatsuit"),
                camo("grey", "Grey", "hazmatsuit_grey"),
                camo("orange", "Orange", "hazmatsuit_orange"),
                camo("blue", "Blue", "hazmatsuit_blue"))));
        groups.add(new Group(List.of(TGArmors.T3_COMBAT_HELMET.get(), TGArmors.T3_COMBAT_CHESTPLATE.get(), TGArmors.T3_COMBAT_LEGGINGS.get(), TGArmors.T3_COMBAT_BOOTS.get()), Kind.ARMOR_LAYERS, List.of(
                camo("white", "White", "t3_combat"),
                camo("silver", "Silver", "t3_combat_silver"),
                camo("dark_green", "Dark Green", "t3_combat_green"))));
        groups.add(new Group(List.of(TGArmors.T3_MINER_HELMET.get(), TGArmors.T3_MINER_CHESTPLATE.get(), TGArmors.T3_MINER_LEGGINGS.get(), TGArmors.T3_MINER_BOOTS.get()), Kind.ARMOR_LAYERS, List.of(
                camo("orange", "Orange", "hevsuit"),
                camo("silver", "Silver", "hevsuit_silver"),
                camo("black", "Black", "hevsuit_black"))));
        groups.add(new Group(List.of(TGArmors.T3_EXO_HELMET.get(), TGArmors.T3_EXO_CHESTPLATE.get(), TGArmors.T3_EXO_LEGGINGS.get(), TGArmors.T3_EXO_BOOTS.get()), Kind.ARMOR_LAYERS, List.of(
                camo("white", "White", "t3_exo"),
                camo("silver", "Silver", "t3_exo_silver"),
                camo("dark_green", "Dark Green", "t3_exo_green"))));
        groups.add(new Group(List.of(TGArmors.STEAM_HELMET.get(), TGArmors.STEAM_CHESTPLATE.get(), TGArmors.STEAM_LEGGINGS.get(), TGArmors.STEAM_BOOTS.get()), Kind.ARMOR_SINGLE, List.of(
                camo("default", "Default", "steam_armor"),
                camo("rusty", "Rusty", "steam_armor_rusty"),
                camo("iron", "Steel", "steam_armor_iron"),
                camo("hazard", "Hazard", "steam_armor_hazard"))));
        groups.add(new Group(List.of(TGArmors.T3_POWER_HELMET.get(), TGArmors.T3_POWER_CHESTPLATE.get(), TGArmors.T3_POWER_LEGGINGS.get(), TGArmors.T3_POWER_BOOTS.get()), Kind.ARMOR_SINGLE, List.of(
                camo("paladin", "Paladin", "powerarmor"),
                camo("outcast", "Outcast", "powerarmor_dark"),
                camo("outcast_worn", "Outcast 2", "powerarmor_dark2"))));
        groups.add(new Group(List.of(TGArmors.T4_POWER_HELMET.get(), TGArmors.T4_POWER_CHESTPLATE.get(), TGArmors.T4_POWER_LEGGINGS.get(), TGArmors.T4_POWER_BOOTS.get()), Kind.ARMOR_SINGLE, List.of(
                camo("darkgrey", "Dark Grey", "powerarmor_mk2_darkgrey"),
                camo("grey", "Grey", "powerarmor_mk2"),
                camo("black", "Black", "powerarmor_mk2_black"),
                camo("white", "White", "powerarmor_mk2_white"))));
        groups.add(new Group(List.of(TGArmors.T2_BERET_ITEM.get()), Kind.ARMOR_SINGLE, List.of(
                camo("red", "Red", "beret_texture"),
                camo("black", "Black", "beret_texture_black"),
                camo("green", "Green", "beret_texture_green"))));
        // --- Shields (Techguns 2 ClientProxy texture arrays, camo names from its lang) ---
        groups.add(new Group(List.of(TGArmors.RIOT_SHIELD.get()), Kind.SHIELD, List.of(
                camo("swat", "SWAT", "riot_shield"),
                camo("black", "Black", "riot_shield_black"),
                camo("dark_green", "Dark Green", "riot_shield_darkgreen"),
                camo("grey", "Grey", "riot_shield_grey"))));
        groups.add(new Group(List.of(TGArmors.BALLISTIC_SHIELD.get()), Kind.SHIELD, List.of(
                camo("swat", "SWAT", "ballistic_shield"),
                camo("black", "Black", "ballistic_shield_black"),
                camo("dark_green", "Dark Green", "ballistic_shield_green"),
                camo("grey", "Grey", "ballistic_shield_grey"))));
        groups.add(new Group(List.of(TGArmors.ADVANCED_SHIELD.get()), Kind.SHIELD, List.of(
                camo("black", "Black", "advanced_shield"),
                camo("grey", "Grey", "advanced_shield_silver"),
                camo("dark_green", "Dark Green", "advanced_shield_green"))));
        // --- Extra-slot gear (Techguns 2 ClientProxy texture lists, camo names from its lang; index 0 has no 1.12 name) ---
        groups.add(new Group(List.of(slotItem("jetpack")), Kind.SLOT, List.of(
                camo("default", "Default", "jetpack"),
                camo("black", "Black", "jetpack_1"),
                camo("bronze", "Bronze", "jetpack_2"),
                camo("dark_green", "Dark Green", "jetpack_3"))));
        groups.add(new Group(List.of(slotItem("jump_pack")), Kind.SLOT, List.of(
                camo("default", "Default", "jetpack"),
                camo("black", "Black", "jetpack_1"),
                camo("bronze", "Bronze", "jetpack_2"),
                camo("dark_green", "Dark Green", "jetpack_3"))));
        groups.add(new Group(List.of(slotItem("antigrav_pack")), Kind.SLOT, List.of(
                camo("default", "Default", "antigravpack"),
                camo("bronze", "Bronze", "antigravpack_1"),
                camo("black", "Black", "antigravpack_2"),
                camo("dark_green", "Dark Green", "antigravpack_3"),
                camo("silver", "Silver", "antigravpack_4"))));
        groups.add(new Group(List.of(slotItem("tactical_mask")), Kind.SLOT, List.of(
                camo("black", "Black", "tacticalmask"),
                camo("dark_green", "Dark Green", "tacticalmask_1"),
                camo("green", "Green", "tacticalmask_2"),
                camo("white", "White", "tacticalmask_3"))));
        return groups;
    }

    /**
     * Item icon tint per layer (Fabric {@code TGCamos} colors for its layered armor icons, see {@code TGItemModelProvider}),
     * by item and camo texture; -1 leaves a layer untinted. Pieces without an entry keep one plain icon for all camos.
     */
    private static Map<Item, Map<String, List<Integer>>> iconTints() {
        Map<Item, Map<String, List<Integer>>> tints = new HashMap<>();
        tint(tints, TGArmors.T1_MINER_HELMET, "t1_miner", 0xFFFF00);
        tint(tints, TGArmors.T1_MINER_HELMET, "t1_miner_red", 0x66FF33);
        tint(tints, TGArmors.T1_MINER_HELMET, "t1_miner_green", 0x00FFFF);
        tint(tints, TGArmors.T1_MINER_HELMET, "t1_miner_black", 0xFF9900);
        for (ItemLike item : List.of(TGArmors.T1_MINER_CHESTPLATE, TGArmors.T1_MINER_LEGGINGS)) {
            tint(tints, item, "t1_miner", 0x4C6CBF);
            tint(tints, item, "t1_miner_red", 0xE83030);
            tint(tints, item, "t1_miner_green", 0x2F8C3A);
            tint(tints, item, "t1_miner_black", 0x464646);
        }

        tint(tints, TGArmors.T1_SCOUT_HELMET, "t1_scout", 0xDFC993, 0xD6C9A9);
        tint(tints, TGArmors.T1_SCOUT_CHESTPLATE, "t1_scout", 0xD5C5A0, 0xB68647);
        tint(tints, TGArmors.T1_SCOUT_LEGGINGS, "t1_scout", 0xD5C5A0);
        tint(tints, TGArmors.T1_SCOUT_BOOTS, "t1_scout", 0xD6C9A9);
        for (ItemLike item : List.of(TGArmors.T1_SCOUT_HELMET, TGArmors.T1_SCOUT_CHESTPLATE)) {
            tint(tints, item, "t1_scout_forest", 0x007825, 0x007825);
            tint(tints, item, "t1_scout_snow", 0xFFFFFF, 0xFFFFFF);
            tint(tints, item, "t1_scout_black", 0x4F4F4F, 0x4F4F4F);
        }
        for (ItemLike item : List.of(TGArmors.T1_SCOUT_LEGGINGS, TGArmors.T1_SCOUT_BOOTS)) {
            tint(tints, item, "t1_scout_forest", 0x007825);
            tint(tints, item, "t1_scout_snow", 0xFFFFFF);
            tint(tints, item, "t1_scout_black", 0x4F4F4F);
        }

        tint(tints, TGArmors.T2_COMBAT_HELMET, "t2_combat", 0x82A780, 0x82A780);
        tint(tints, TGArmors.T2_COMBAT_HELMET, "t2_combat_desert", 0xFFFCC7, 0xC29971);
        tint(tints, TGArmors.T2_COMBAT_HELMET, "t2_combat_wood", 0x669756, 0xFFFCC7);
        tint(tints, TGArmors.T2_COMBAT_HELMET, "t2_combat_arctic", 0xFFFFFF, 0x797979);
        tint(tints, TGArmors.T2_COMBAT_HELMET, "t2_combat_swat", 0x404040, 0x404040);
        tint(tints, TGArmors.T2_COMBAT_HELMET, "t2_combat_security", 0x54545D, 0x54545D);
        for (ItemLike item : List.of(TGArmors.T2_COMBAT_CHESTPLATE, TGArmors.T2_COMBAT_LEGGINGS)) {
            tint(tints, item, "t2_combat", 0x82A780, 0x82A780, 0x82A780);
            tint(tints, item, "t2_combat_desert", 0xFFFCC7, 0xC29971, 0xD8C06C);
            tint(tints, item, "t2_combat_wood", 0x669756, 0xFFFCC7, 0x669756);
            tint(tints, item, "t2_combat_arctic", 0xFFFFFF, 0x797979, 0x797979);
            tint(tints, item, "t2_combat_swat", 0x404040, 0x404040, 0x707070);
            tint(tints, item, "t2_combat_security", 0x3C39C7, 0x3C39C7, 0x707070);
        }
        tint(tints, TGArmors.T2_COMBAT_BOOTS, "t2_combat", 0x959595);
        tint(tints, TGArmors.T2_COMBAT_BOOTS, "t2_combat_desert", 0x9E9982);
        tint(tints, TGArmors.T2_COMBAT_BOOTS, "t2_combat_wood", 0x759874);
        tint(tints, TGArmors.T2_COMBAT_BOOTS, "t2_combat_arctic", 0xD7D7D7);
        tint(tints, TGArmors.T2_COMBAT_BOOTS, "t2_combat_swat", 0x606060);
        tint(tints, TGArmors.T2_COMBAT_BOOTS, "t2_combat_security", 0x54545D);

        for (ItemLike item : List.of(TGArmors.HAZMAT_HELMET, TGArmors.HAZMAT_CHESTPLATE, TGArmors.HAZMAT_LEGGINGS)) {
            tint(tints, item, "hazmatsuit", 0xEEFF00);
            tint(tints, item, "hazmatsuit_orange", 0xFF8800);
            tint(tints, item, "hazmatsuit_grey", 0xA6A6A6);
            tint(tints, item, "hazmatsuit_blue", 0x4DCFFF);
        }

        // T3 combat and exo share the base colors; the third layer is the visor or chest light, the fourth the exo frame
        tint(tints, TGArmors.T3_COMBAT_HELMET, "t3_combat", 0x384A57, 0xE9FEFF, 0x5146E3);
        tint(tints, TGArmors.T3_COMBAT_HELMET, "t3_combat_green", 0x015409, 0x20231B, 0x585858);
        tint(tints, TGArmors.T3_COMBAT_HELMET, "t3_combat_silver", 0x4A4A4A, 0xCFDBD9, 0xD1D15D);
        tint(tints, TGArmors.T3_COMBAT_CHESTPLATE, "t3_combat", 0x384A57, 0xE9FEFF, 0xFF0000);
        tint(tints, TGArmors.T3_COMBAT_CHESTPLATE, "t3_combat_green", 0x015409, 0x20231B, 0xFFFFFF);
        tint(tints, TGArmors.T3_COMBAT_CHESTPLATE, "t3_combat_silver", 0x4A4A4A, 0xCFDBD9, 0xCFDBD9);
        for (ItemLike item : List.of(TGArmors.T3_COMBAT_LEGGINGS, TGArmors.T3_COMBAT_BOOTS)) {
            tint(tints, item, "t3_combat", 0x384A57, 0xE9FEFF);
            tint(tints, item, "t3_combat_green", 0x015409, 0x20231B);
            tint(tints, item, "t3_combat_silver", 0x4A4A4A, 0xCFDBD9);
        }
        tint(tints, TGArmors.T3_EXO_HELMET, "t3_exo", 0x384A57, 0xE9FEFF, 0x5146E3, 0xB0B0B0);
        tint(tints, TGArmors.T3_EXO_HELMET, "t3_exo_green", 0x015409, 0x20231B, 0x585858, 0x303030);
        tint(tints, TGArmors.T3_EXO_HELMET, "t3_exo_silver", 0x4A4A4A, 0xCFDBD9, 0xD1D15D, 0xB0B0B0);
        tint(tints, TGArmors.T3_EXO_CHESTPLATE, "t3_exo", 0x384A57, 0xE9FEFF, 0xFF0000, 0xB0B0B0);
        tint(tints, TGArmors.T3_EXO_CHESTPLATE, "t3_exo_green", 0x015409, 0x20231B, 0xFFFFFF, 0x303030);
        tint(tints, TGArmors.T3_EXO_CHESTPLATE, "t3_exo_silver", 0x4A4A4A, 0xCFDBD9, 0xCFDBD9, 0xB0B0B0);
        tint(tints, TGArmors.T3_EXO_LEGGINGS, "t3_exo", 0x384A57, 0xE9FEFF, 0xA5A5A5);
        tint(tints, TGArmors.T3_EXO_LEGGINGS, "t3_exo_green", 0x015409, 0x20231B, 0x303030);
        tint(tints, TGArmors.T3_EXO_LEGGINGS, "t3_exo_silver", 0x4A4A4A, 0xCFDBD9, 0xA5A5A5);
        tint(tints, TGArmors.T3_EXO_BOOTS, "t3_exo", 0x384A57, 0xE9FEFF, 0xB0B0B0);
        tint(tints, TGArmors.T3_EXO_BOOTS, "t3_exo_green", 0x015409, 0x20231B, 0x303030);
        tint(tints, TGArmors.T3_EXO_BOOTS, "t3_exo_silver", 0x4A4A4A, 0xCFDBD9, 0xB0B0B0);

        tint(tints, TGArmors.T3_MINER_HELMET, "hevsuit", 0xFFAE00, -1, 0x8BC2FF);
        tint(tints, TGArmors.T3_MINER_HELMET, "hevsuit_black", 0x424242, -1, 0xFF3C00);
        tint(tints, TGArmors.T3_MINER_HELMET, "hevsuit_silver", 0xEEEEEE, -1, 0x8BC2FF);
        for (ItemLike item : List.of(TGArmors.T3_MINER_CHESTPLATE, TGArmors.T3_MINER_LEGGINGS, TGArmors.T3_MINER_BOOTS)) {
            tint(tints, item, "hevsuit", 0xFFAE00);
            tint(tints, item, "hevsuit_black", 0x424242);
            tint(tints, item, "hevsuit_silver", 0xEEEEEE);
        }

        tint(tints, TGArmors.T2_BERET_ITEM, "beret_texture", 0xB30000);
        tint(tints, TGArmors.T2_BERET_ITEM, "beret_texture_green", 0x009000);
        tint(tints, TGArmors.T2_BERET_ITEM, "beret_texture_black", 0x444444);
        return tints;
    }

    private static void tint(Map<Item, Map<String, List<Integer>>> tints, ItemLike item, String texture, int... colors) {
        tints.computeIfAbsent(item.asItem(), key -> new HashMap<>()).put(texture, Arrays.stream(colors).boxed().toList());
    }

    /** An extra-slot item by id (registered by {@code TGSlotItems}); {@link Items#AIR} if it does not exist (yet). */
    private static ItemLike slotItem(String id) {
        return BuiltInRegistries.ITEM.getOptional(TGIds.id(id)).orElse(Items.AIR);
    }

    /** Camo name -> English display name for the language provider ({@code camo.techguns.<name>}). */
    public static Map<String, String> englishNames() {
        Map<String, String> names = new LinkedHashMap<>();
        groups().forEach(group -> group.camos().forEach(camo -> names.putIfAbsent(camo.name(), camo.english())));
        return names;
    }

    public static void bootstrap(BootstrapContext<TGCamo> context) {
        Map<Item, Map<String, List<Integer>>> iconTints = iconTints();
        for (Group group : groups()) {
            for (ItemLike itemLike : group.items()) {
                Item item = itemLike.asItem();
                if (item == Items.AIR) {
                    continue;
                }
                for (int order = 0; order < group.camos().size(); order++) {
                    Camo camo = group.camos().get(order);
                    Optional<ResourceLocation> texture = switch (group.kind()) {
                        case GUN -> Optional.of(TGIds.id("textures/guns/" + camo.texture() + ".png"));
                        case ARMOR_SINGLE -> Optional.of(TGIds.id("textures/models/armor/" + camo.texture() + ".png"));
                        case SHIELD, SLOT -> Optional.of(TGIds.id("textures/armors/" + camo.texture() + ".png"));
                        case ARMOR_LAYERS -> Optional.empty();
                    };
                    Optional<ResourceLocation> layer = group.kind() == Kind.ARMOR_LAYERS ? Optional.of(TGIds.id(camo.texture())) : Optional.empty();
                    String itemName = BuiltInRegistries.ITEM.getKey(item).getPath();
                    context.register(ResourceKey.create(TGCamos.KEY, TGIds.id(itemName + "/" + camo.name())),
                            new TGCamo(BuiltInRegistries.ITEM.wrapAsHolder(item), camo.name(), order,
                                    Component.translatable("camo.techguns." + camo.name()), texture, layer,
                                    iconTints.getOrDefault(item, Map.of()).getOrDefault(camo.texture(), List.of())));
                }
            }
        }
    }
}
