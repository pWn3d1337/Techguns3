package techguns.world.structure;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.TemplateStructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.ChunkPos;
import techguns.registry.TGStructures;
import techguns.world.LegacyPaletteProcessor;

/**
 * One converted Techguns 2 structure (Techguns 2 {@code WorldgenStructure.setBlocks} with {@code cleanUpwards},
 * {@code placeFoundation} and {@code placeScannedStructure}): clears {@code clearAbove} blocks above the base layer,
 * fills up to {@code foundation} replaceable blocks below it with the base layer's block, then places the template
 * through the {@link LegacyPaletteProcessor}.
 */
public class LegacyTemplatePiece extends TemplateStructurePiece {
    /** Base layer (template y 0, air included) per template: local position and the block it places. */
    private static final Map<String, List<StructureTemplate.StructureBlockInfo>> BASE_LAYERS = new ConcurrentHashMap<>();

    private final Rotation rotation;
    private final BlockPos pivot;
    private final LegacyPaletteProcessor.BiomeColor biomeColor;
    private final int foundation;
    private final int clearAbove;
    /** Techguns 2 {@code placeFoundationNether}: the foundation ends after two solid blocks. */
    private final boolean foundationStopsAtSolid;

    public LegacyTemplatePiece(StructureTemplateManager templates, ResourceLocation template, BlockPos position, Rotation rotation, BlockPos pivot,
                               LegacyPaletteProcessor.BiomeColor biomeColor, int foundation, int clearAbove, boolean foundationStopsAtSolid) {
        super(TGStructures.LEGACY_TEMPLATE_PIECE.get(), 0, templates, template, template.toString(), settings(rotation, pivot, biomeColor), position);
        this.rotation = rotation;
        this.pivot = pivot;
        this.biomeColor = biomeColor;
        this.foundation = foundation;
        this.clearAbove = clearAbove;
        this.foundationStopsAtSolid = foundationStopsAtSolid;
    }

    public LegacyTemplatePiece(StructureTemplateManager templates, CompoundTag tag) {
        super(TGStructures.LEGACY_TEMPLATE_PIECE.get(), tag, templates,
                id -> settings(Rotation.valueOf(tag.getString("Rot")), NbtUtils.readBlockPos(tag, "Pivot").orElse(BlockPos.ZERO), biomeColor(tag)));
        this.rotation = Rotation.valueOf(tag.getString("Rot"));
        this.pivot = NbtUtils.readBlockPos(tag, "Pivot").orElse(BlockPos.ZERO);
        this.biomeColor = biomeColor(tag);
        this.foundation = tag.getInt("Foundation");
        this.clearAbove = tag.getInt("ClearAbove");
        this.foundationStopsAtSolid = tag.getBoolean("FoundationStopsAtSolid");
    }

    static StructurePlaceSettings settings(Rotation rotation, BlockPos pivot, LegacyPaletteProcessor.BiomeColor biomeColor) {
        return new StructurePlaceSettings().setRotation(rotation).setRotationPivot(pivot)
                .addProcessor(new LegacyPaletteProcessor(Optional.of(biomeColor))).addProcessor(BlockIgnoreProcessor.STRUCTURE_BLOCK);
    }

    private static LegacyPaletteProcessor.BiomeColor biomeColor(CompoundTag tag) {
        return LegacyPaletteProcessor.BiomeColor.CODEC.byName(tag.getString("BiomeColor"), LegacyPaletteProcessor.BiomeColor.WOODLAND);
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        super.addAdditionalSaveData(context, tag);
        tag.putString("Rot", rotation.name());
        tag.put("Pivot", NbtUtils.writeBlockPos(pivot));
        tag.putString("BiomeColor", biomeColor.getSerializedName());
        tag.putInt("Foundation", foundation);
        tag.putInt("ClearAbove", clearAbove);
        tag.putBoolean("FoundationStopsAtSolid", foundationStopsAtSolid);
    }

    @Override
    protected void handleDataMarker(String name, BlockPos pos, ServerLevelAccessor level, RandomSource random, BoundingBox box) {
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator, RandomSource random, BoundingBox box,
                            ChunkPos chunkPos, BlockPos pos) {
        for (StructureTemplate.StructureBlockInfo base : baseLayer(template, templateName)) {
            BlockPos world = StructureTemplate.calculateRelativePosition(placeSettings, base.pos()).offset(templatePosition);
            BlockState state = base.state().rotate(rotation);
            for (int up = 1; up <= clearAbove; up++) {
                setIfInside(level, box, world.above(up), Blocks.AIR.defaultBlockState());
            }
            int solid = 0;
            for (int down = 1; down <= foundation && !state.isAir(); down++) {
                BlockPos below = world.below(down);
                if (!box.isInside(below)) {
                    continue;
                }
                BlockState current = level.getBlockState(below);
                if (current.isAir() || current.canBeReplaced() || !current.getFluidState().isEmpty()) {
                    level.setBlock(below, state, Block.UPDATE_CLIENTS);
                    solid = 0;
                } else if (foundationStopsAtSolid && ++solid >= 2) {
                    break;
                }
            }
        }
        super.postProcess(level, structureManager, generator, random, box, chunkPos, pos);
    }

    private static void setIfInside(WorldGenLevel level, BoundingBox box, BlockPos pos, BlockState state) {
        if (box.isInside(pos)) {
            level.setBlock(pos, state, Block.UPDATE_CLIENTS);
        }
    }

    /** The template's base layer, from its saved form (templates don't expose their blocks otherwise). */
    static List<StructureTemplate.StructureBlockInfo> baseLayer(StructureTemplate template, String name) {
        return BASE_LAYERS.computeIfAbsent(name, id -> {
            CompoundTag saved = template.save(new CompoundTag());
            ListTag palette = saved.getList("palette", Tag.TAG_COMPOUND);
            List<BlockState> states = new ArrayList<>();
            for (int i = 0; i < palette.size(); i++) {
                states.add(NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), palette.getCompound(i)));
            }
            List<StructureTemplate.StructureBlockInfo> base = new ArrayList<>();
            for (Tag entry : saved.getList("blocks", Tag.TAG_COMPOUND)) {
                CompoundTag block = (CompoundTag) entry;
                ListTag p = block.getList("pos", Tag.TAG_INT);
                if (p.getInt(1) == 0) {
                    base.add(new StructureTemplate.StructureBlockInfo(new BlockPos(p.getInt(0), 0, p.getInt(2)), states.get(block.getInt("state")), null));
                }
            }
            return List.copyOf(base);
        });
    }
}
