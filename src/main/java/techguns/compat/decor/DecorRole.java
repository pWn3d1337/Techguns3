package techguns.compat.decor;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import net.minecraft.resources.ResourceLocation;
import techguns.TGIds;

/**
 * Structure blocks that Techguns 2 took from Chisel when Chisel was installed (its {@code MBlockRegister} fields set in
 * {@code TGChiselBlocks.postInit}), with the blocks of current mods that look the same: Factory Blocks (Chisel's
 * factory and technical set), Chisel Reborn (stone, brick and iron variants) and Laboratory Blocks. Most targets have
 * the same texture as the Techguns 2 Chisel variant. Roles whose Chisel block has no counterpart (netherrack, the worn
 * column, the transparent scaffold, the modern iron fence) are not listed and keep their fallback.
 */
public enum DecorRole {
    // Chisel factory -> Factory Blocks
    FACTORY_PLATE("factory_blocks:rust"),
    FACTORY_PLATE_DOTTED("factory_blocks:factory"),
    FACTORY_PLATE_DOTTED_GREY("factory_blocks:srust"),
    FACTORY_PLATE_VERY_RUSTY("factory_blocks:vrust"),
    FACTORY_SHINY_METAL("factory_blocks:grinder"),
    FACTORY_WIREFRAME("factory_blocks:wireframe"),
    FACTORY_CRATE("factory_blocks:metalbox"),
    HELIPAD_HAZARDBLOCK("factory_blocks:hazard"),
    HELIPAD_WIREFRAME("factory_blocks:wireframe"),
    TANKS_WALL("factory_blocks:rust"),
    TANKS_BORDER("factory_blocks:hazard"),
    SURVIVOR_HIDEOUT_IRON_TOP("factory_blocks:metalbox"),
    // Chisel technical -> Factory Blocks
    TANKS_BASE("factory_blocks:rusty_scaffold"),
    TECHNICAL_BLOCK_SCAFFOLD("factory_blocks:rusty_scaffold"),
    TECHNICAL_CONCRETE("factory_blocks:rust_bplates"),
    TECHNICAL_PIPES("factory_blocks:small_pipes"),
    TECHNICAL_GRATE("factory_blocks:grate"),
    TECHNICAL2_FAN("factory_blocks:fan_on"),
    SURVIVOR_HIDEOUT_IRON_BOTTOM("factory_blocks:rust_bplates"),
    // Chisel laboratory -> Laboratory Blocks
    LABORATORY_WALL("laboratoryblocks:laboratory_block"),
    LABORATORY_FLOOR("laboratoryblocks:mixed_laboratory_tiles"),
    LABORATORY_BORDER("laboratoryblocks:laboratory_vent"),
    LABORATORY_DECORATION("laboratoryblocks:text_laboratory_screen"),
    GAS_STATION_CONSOLE("laboratoryblocks:text_laboratory_screen"),
    // Chisel stone, brick, iron and plank variants -> Chisel Reborn
    COBBLESTONE_1("chisel:soft_bricks/cobblestone"),
    COBBLESTONE_2("chisel:cracked_bricks/cobblestone"),
    COBBLESTONE_3("chisel:triple_bricks/cobblestone"),
    COBBLESTONE_7("chisel:large_tile/cobblestone"),
    COBBLESTONE_13("chisel:french/cobblestone"),
    COBBLESTONE_15("chisel:layer/cobblestone"),
    COBBLESTONE_FLOOR("chisel:large_tile/cobblestone"),
    ANDESITE_3("chisel:triple_bricks/andesite"),
    ANDESITE_4("chisel:encased_bricks/andesite"),
    ANDESITE_11("chisel:dent/andesite"),
    ANDESITE1_0("chisel:mosaic/andesite"),
    STONE_BRICKS_PANEL("chisel:panel/stone"),
    STONE_BRICKS_SMALL("chisel:tiles_small/stone"),
    STONE_BRICKS_SUNKEN("chisel:sunken/stone"),
    BRICKS_WIDE("chisel:triple_bricks/bricks"),
    BRICKS_WEATHERED("chisel:soft_bricks/bricks"),
    BLUE_CONCRETE_SMALL_BRICKS("chisel:small_bricks/blue_concrete"),
    IRON_BLOCK_VENTS("chisel:vents/iron_block"),
    IRON_BLOCK_SMALL_INGOTS("chisel:smallingot/iron_block"),
    // Chisel limestone has no Chisel Reborn version; its sandstone matches the sandstone fallback
    LIME_WALL("chisel:soft_bricks/sandstone"),
    LIME_FLOOR("chisel:large_tile/sandstone"),
    LIME_BORDER("chisel:dent/sandstone"),
    LIME_DECORATION("chisel:ornate/sandstone"),
    // Chisel oak planks: no identical textures, closest Chisel Reborn patterns
    OAK_PLANKS_1("chisel:stacked/oak_planks"),
    BARRACKS_WOOD_FLOOR("chisel:braid/oak_planks"),
    BARRACKS_WOOD_SCAFFOLD("chisel:shipping_crate/oak_planks");

    /** Blocks to use, first installed one wins. */
    private final List<ResourceLocation> candidates;

    DecorRole(String... candidates) {
        this.candidates = Arrays.stream(candidates).map(TGIds::parse).toList();
    }

    /** Lowercase id stored in the structure templates. */
    public String id() {
        return name().toLowerCase(Locale.ROOT);
    }

    /** The Techguns 2 {@code MBlockRegister} field name (as in {@code src/legacy/role_dump.json}; Techguns 2 spelled "SURIVIVOR"). */
    public String legacyName() {
        return name().startsWith("SURVIVOR_") ? "SURIVIVOR_" + name().substring("SURVIVOR_".length()) : name();
    }

    public List<ResourceLocation> candidates() {
        return candidates;
    }

    public static Optional<DecorRole> byId(String id) {
        return Arrays.stream(values()).filter(role -> role.id().equals(id)).findFirst();
    }

    public static Optional<DecorRole> byLegacyName(String name) {
        return Arrays.stream(values()).filter(role -> role.legacyName().equals(name)).findFirst();
    }
}
