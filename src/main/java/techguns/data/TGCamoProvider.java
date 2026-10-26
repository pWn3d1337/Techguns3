package techguns.data;

import java.util.ArrayList;
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
        ARMOR_SINGLE
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
        return groups;
    }

    /** Camo name -> English display name for the language provider ({@code camo.techguns.<name>}). */
    public static Map<String, String> englishNames() {
        Map<String, String> names = new LinkedHashMap<>();
        groups().forEach(group -> group.camos().forEach(camo -> names.putIfAbsent(camo.name(), camo.english())));
        return names;
    }

    public static void bootstrap(BootstrapContext<TGCamo> context) {
        for (Group group : groups()) {
            for (ItemLike itemLike : group.items()) {
                Item item = itemLike.asItem();
                for (int order = 0; order < group.camos().size(); order++) {
                    Camo camo = group.camos().get(order);
                    Optional<ResourceLocation> texture = switch (group.kind()) {
                        case GUN -> Optional.of(TGIds.id("textures/guns/" + camo.texture() + ".png"));
                        case ARMOR_SINGLE -> Optional.of(TGIds.id("textures/models/armor/" + camo.texture() + ".png"));
                        case ARMOR_LAYERS -> Optional.empty();
                    };
                    Optional<ResourceLocation> layer = group.kind() == Kind.ARMOR_LAYERS ? Optional.of(TGIds.id(camo.texture())) : Optional.empty();
                    String itemName = BuiltInRegistries.ITEM.getKey(item).getPath();
                    context.register(ResourceKey.create(TGCamos.KEY, TGIds.id(itemName + "/" + camo.name())),
                            new TGCamo(BuiltInRegistries.ITEM.wrapAsHolder(item), camo.name(), order,
                                    Component.translatable("camo.techguns." + camo.name()), texture, layer));
                }
            }
        }
    }
}
