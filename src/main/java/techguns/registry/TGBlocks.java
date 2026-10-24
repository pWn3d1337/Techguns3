package techguns.registry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import techguns.Techguns;

/**
 * Techguns blocks. Every block registered with {@link #withItem} also gets a block item.
 */
public final class TGBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Techguns.MODID);

    private static final List<DeferredBlock<? extends Block>> BLOCKS_WITH_ITEM = new ArrayList<>();

    // --- Ores ---
    public static final DeferredBlock<DropExperienceBlock> TIN_ORE = withItem("tin_ore",
            () -> new DropExperienceBlock(UniformInt.of(0, 0), stoneOre(3.0f)));
    public static final DeferredBlock<DropExperienceBlock> DEEPSLATE_TIN_ORE = withItem("deepslate_tin_ore",
            () -> new DropExperienceBlock(UniformInt.of(0, 0), deepslateOre(4.5f)));

    public static final DeferredBlock<DropExperienceBlock> LEAD_ORE = withItem("lead_ore",
            () -> new DropExperienceBlock(UniformInt.of(0, 0), stoneOre(4.0f)));
    public static final DeferredBlock<DropExperienceBlock> DEEPSLATE_LEAD_ORE = withItem("deepslate_lead_ore",
            () -> new DropExperienceBlock(UniformInt.of(0, 0), deepslateOre(6.0f)));
    public static final DeferredBlock<DropExperienceBlock> TITANIUM_ORE = withItem("titanium_ore",
            () -> new DropExperienceBlock(UniformInt.of(0, 0), stoneOre(5.0f)));
    public static final DeferredBlock<DropExperienceBlock> DEEPSLATE_TITANIUM_ORE = withItem("deepslate_titanium_ore",
            () -> new DropExperienceBlock(UniformInt.of(0, 0), deepslateOre(7.5f)));
    public static final DeferredBlock<DropExperienceBlock> URANIUM_ORE = withItem("uranium_ore",
            () -> new DropExperienceBlock(UniformInt.of(0, 0), stoneOre(5.0f).lightLevel(state -> 4)));
    public static final DeferredBlock<DropExperienceBlock> DEEPSLATE_URANIUM_ORE = withItem("deepslate_uranium_ore",
            () -> new DropExperienceBlock(UniformInt.of(0, 0), deepslateOre(7.5f).lightLevel(state -> 4)));

    private TGBlocks() {
    }

    private static BlockBehaviour.Properties stoneOre(float hardness) {
        return BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM)
                .requiresCorrectToolForDrops().strength(hardness, 3.0f);
    }

    private static BlockBehaviour.Properties deepslateOre(float hardness) {
        return BlockBehaviour.Properties.of().mapColor(MapColor.DEEPSLATE).instrument(NoteBlockInstrument.BASEDRUM)
                .requiresCorrectToolForDrops().strength(hardness, 3.0f).sound(SoundType.DEEPSLATE);
    }

    private static <B extends Block> DeferredBlock<B> withItem(String name, java.util.function.Supplier<B> factory) {
        DeferredBlock<B> block = BLOCKS.register(name, factory);
        TGItems.ITEMS.registerSimpleBlockItem(block);
        BLOCKS_WITH_ITEM.add(block);
        return block;
    }

    /** All blocks that have a block item, in registration order. */
    public static List<DeferredBlock<? extends Block>> blocksWithItem() {
        return Collections.unmodifiableList(BLOCKS_WITH_ITEM);
    }
}
