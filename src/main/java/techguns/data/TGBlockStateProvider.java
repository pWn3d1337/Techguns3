package techguns.data;

import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
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
        cubeAllWithItem(TGBlocks.TIN_ORE);
        cubeAllWithItem(TGBlocks.DEEPSLATE_TIN_ORE);
        cubeAllWithItem(TGBlocks.LEAD_ORE);
        cubeAllWithItem(TGBlocks.DEEPSLATE_LEAD_ORE);
        cubeAllWithItem(TGBlocks.TITANIUM_ORE);
        cubeAllWithItem(TGBlocks.DEEPSLATE_TITANIUM_ORE);
        cubeAllWithItem(TGBlocks.URANIUM_ORE);
        cubeAllWithItem(TGBlocks.DEEPSLATE_URANIUM_ORE);
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

    /** Full cube with the same texture ({@code textures/block/<name>.png}) on all sides, plus its item model. */
    private void cubeAllWithItem(DeferredBlock<? extends Block> block) {
        simpleBlockWithItem(block.get(), cubeAll(block.get()));
    }
}
