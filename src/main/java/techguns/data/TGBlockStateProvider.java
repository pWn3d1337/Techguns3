package techguns.data;

import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredBlock;
import techguns.Techguns;
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
    }

    /** Full cube with the same texture ({@code textures/block/<name>.png}) on all sides, plus its item model. */
    private void cubeAllWithItem(DeferredBlock<? extends Block> block) {
        simpleBlockWithItem(block.get(), cubeAll(block.get()));
    }
}
