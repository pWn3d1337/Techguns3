package techguns.data;

import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.BlockModelBuilder;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredBlock;
import techguns.Techguns;
import techguns.block.BioBlobBlock;
import techguns.registry.TGBlocks;

public class TGBlockStateProvider extends BlockStateProvider {
    public TGBlockStateProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, Techguns.MODID, existingFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        // Techguns 2 debug tools
        cubeAllWithItem(TGBlocks.AIR_MARKER);
        cubeAllWithItem(TGBlocks.ANTI_AIR_MARKER);
        cubeAllWithItem(TGBlocks.DUNGEON_SCANNER);
        cubeAllWithItem(TGBlocks.DUNGEON_GENERATOR);
        var interior = models().cube("block/interior_marker", modLoc("block/interior_marker_side"), modLoc("block/interior_marker_side"),
                modLoc("block/interior_marker_front"), modLoc("block/interior_marker_side"), modLoc("block/interior_marker_side"),
                modLoc("block/interior_marker_side")).texture("particle", modLoc("block/interior_marker_side"));
        horizontalBlock(TGBlocks.INTERIOR_MARKER.get(), interior);
        simpleBlockItem(TGBlocks.INTERIOR_MARKER.get(), interior);
        cubeAllWithItem(TGBlocks.TIN_ORE);
        cubeAllWithItem(TGBlocks.DEEPSLATE_TIN_ORE);
        cubeAllWithItem(TGBlocks.LEAD_ORE);
        cubeAllWithItem(TGBlocks.DEEPSLATE_LEAD_ORE);
        cubeAllWithItem(TGBlocks.TITANIUM_ORE);
        cubeAllWithItem(TGBlocks.DEEPSLATE_TITANIUM_ORE);
        cubeAllWithItem(TGBlocks.URANIUM_ORE);
        cubeAllWithItem(TGBlocks.DEEPSLATE_URANIUM_ORE);
        TGBlocks.cubeBlocks().stream().filter(block -> block != TGBlocks.NETHERMETAL_BORDER_LAVA).forEach(this::cubeAllWithItem);
        borderLava();
        stairsWithItem(TGBlocks.PANEL_LARGE_BORDER_STAIRS, TGBlocks.PANEL_LARGE_BORDER);
        stairsWithItem(TGBlocks.STEELFRAME_DARK_STAIRS, TGBlocks.STEELFRAME_DARK);
        stairsWithItem(TGBlocks.CONCRETE_GREY_DARK_STAIRS, TGBlocks.CONCRETE_GREY_DARK);
        stairsWithItem(TGBlocks.CONCRETE_BROWN_LIGHT_STAIRS, TGBlocks.CONCRETE_BROWN_LIGHT);
        bioBlob();
    }

    /** Bio blob (Fabric models): a flat box per size, rotated onto the face it sits on. */
    private void bioBlob() {
        // {inset, height, top/bottom uv end, side uv start} per size; the box spans inset..16-inset horizontally
        int[][] boxes = {{5, 4, 10, 11}, {3, 4, 12, 12}, {1, 5, 15, 10}};
        BlockModelBuilder[] models = new BlockModelBuilder[3];
        for (int size = 0; size < 3; size++) {
            int inset = boxes[size][0];
            int height = boxes[size][1];
            int uvEnd = boxes[size][2];
            int uvSideStart = boxes[size][3];
            BlockModelBuilder model = models().getBuilder("block/bioblob" + size)
                    .parent(new ModelFile.UncheckedModelFile(mcLoc("block/thin_block")))
                    .texture("particle", modLoc("block/bioblob"));
            models[size] = model;
            var element = model.element().from(inset, 0, inset).to(16 - inset, height, 16 - inset);
            for (Direction face : Direction.values()) {
                var faceBuilder = element.face(face).texture("#particle");
                if (face.getAxis().isHorizontal()) {
                    faceBuilder.uvs(1, uvSideStart, uvEnd, 16);
                } else {
                    faceBuilder.uvs(1, 1, uvEnd, uvEnd);
                }
                if (face == Direction.DOWN) {
                    faceBuilder.cullface(Direction.DOWN);
                }
            }
            element.end();
            model.ao(false);
        }
        getVariantBuilder(TGBlocks.BIOBLOB.get()).forAllStates(state -> {
            int[] rotation = switch (state.getValue(BioBlobBlock.FACING)) {
                case UP -> new int[]{180, 0};
                case NORTH -> new int[]{90, 180};
                case SOUTH -> new int[]{90, 0};
                case WEST -> new int[]{90, 90};
                case EAST -> new int[]{90, 270};
                default -> new int[]{0, 0};
            };
            return ConfiguredModel.builder().modelFile(models[state.getValue(BioBlobBlock.SIZE)])
                    .rotationX(rotation[0]).rotationY(rotation[1]).build();
        });
        itemModels().getBuilder("bioblob").parent(models[0]);
    }

    /** Nether metal lava pillar (Techguns 2 cube_glow_frame): a lava cube behind a cutout frame texture. */
    private void borderLava() {
        // block/block: the item's GUI and hand rotations
        BlockModelBuilder model = models().withExistingParent("block/nethermetal_border_lava", mcLoc("block/block"))
                .texture("particle", "minecraft:block/lava_still")
                .texture("glow", "minecraft:block/lava_still")
                .texture("frame", modLoc("block/nethermetal_border_alpha"))
                .renderType("cutout");
        model.ao(false);
        var lava = model.element().from(0, 0, 0).to(16, 16, 16);
        for (Direction face : Direction.values()) {
            lava.face(face).texture("#glow").cullface(face);
        }
        lava.end();
        // each frame face sits a tenth of a pixel in front of the lava: in one plane the two z-fought (Fusion splits
        // the connected frame into smaller quads; 1.12 relied on draw order). The lava stays a closed cube in the block
        // planes, so inner corners between pillars keep no gap behind the connected (see-through) frame.
        for (Direction face : Direction.values()) {
            float[] from = {0, 0, 0};
            float[] to = {16, 16, 16};
            int axis = face.getAxis().ordinal();
            float plane = face.getAxisDirection() == Direction.AxisDirection.POSITIVE ? 16.1f : -0.1f;
            from[axis] = plane;
            to[axis] = plane;
            model.element().from(from[0], from[1], from[2]).to(to[0], to[1], to[2])
                    .face(face).texture("#frame").cullface(face).end().end();
        }
        simpleBlockWithItem(TGBlocks.NETHERMETAL_BORDER_LAVA.get(), model);
    }

    /** Vanilla stairs shapes with the base block's texture on all sides, plus the item model. */
    private void stairsWithItem(DeferredBlock<StairBlock> stairs, DeferredBlock<Block> base) {
        ResourceLocation texture = modLoc("block/" + base.getId().getPath());
        stairsBlock(stairs.get(), texture);
        itemModels().withExistingParent(stairs.getId().getPath(), modLoc("block/" + stairs.getId().getPath()));
    }

    /** Full cube with the same texture ({@code textures/block/<name>.png}) on all sides, plus its item model. */
    private void cubeAllWithItem(DeferredBlock<? extends Block> block) {
        simpleBlockWithItem(block.get(), cubeAll(block.get()));
    }
}
