package techguns.world.structure;

import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import techguns.registry.TGBlocks;

/**
 * Scans parts of the world into structure templates and places them back (the debug tools). Templates are saved
 * like the vanilla structure block's, to {@code <world>/generated/<namespace>/structures/<path>.nbt}, where structures
 * and the dungeon generator find them. The structure markers follow the Techguns 2 converter: an anti-air marker is
 * left out (the place keeps its block, like structure void), air and interior markers become air.
 */
public final class StructureScans {
    private static final String AIR = "minecraft:air";

    private StructureScans() {
    }

    /** Saves the area from {@code min} of {@code size} as template {@code id}; false if the file can't be written. */
    public static boolean save(ServerLevel level, BlockPos min, Vec3i size, ResourceLocation id) {
        StructureTemplateManager manager = level.getStructureManager();
        StructureTemplate template = manager.getOrCreate(id);
        template.fillFromWorld(level, min, size, false, TGBlocks.ANTI_AIR_MARKER.get());
        CompoundTag tag = template.save(new CompoundTag());
        ListTag palette = tag.getList("palette", Tag.TAG_COMPOUND);
        for (int i = 0; i < palette.size(); i++) {
            CompoundTag state = palette.getCompound(i);
            String name = state.getString("Name");
            if (name.equals(markerName(TGBlocks.AIR_MARKER.get())) || name.equals(markerName(TGBlocks.INTERIOR_MARKER.get()))) {
                state.putString("Name", AIR);
                state.remove("Properties");
            }
        }
        template.load(level.holderLookup(Registries.BLOCK), tag);
        return manager.save(id);
    }

    private static String markerName(Block block) {
        return BuiltInRegistries.BLOCK.getKey(block).toString();
    }

    /** Places template {@code id} with its corner at {@code pos}; false if there is no such template. */
    public static boolean place(ServerLevel level, BlockPos pos, ResourceLocation id) {
        Optional<StructureTemplate> template = level.getStructureManager().get(id);
        template.ifPresent(t -> t.placeInWorld(level, pos, pos, new StructurePlaceSettings(), RandomSource.create(), Block.UPDATE_CLIENTS));
        return template.isPresent();
    }

    /** A template path from a name typed by a player: lower case, only {@code a-z0-9_-/.}. */
    public static String cleanName(String name) {
        return name.toLowerCase(java.util.Locale.ROOT).replaceAll("[^a-z0-9_./-]", "_");
    }
}
