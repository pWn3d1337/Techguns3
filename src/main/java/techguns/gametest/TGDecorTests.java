package techguns.gametest;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import techguns.TGIds;
import techguns.Techguns;
import techguns.compat.decor.DecorBlocks;
import techguns.compat.decor.DecorRole;
import techguns.world.LegacyPaletteProcessor;

/**
 * Optional decorative mod blocks in structures (Techguns 2's Chisel blocks: Factory Blocks, Chisel Reborn, Laboratory
 * Blocks) and the Rechiseled groups. The tests pass with and without those mods: run {@code runGameTestServer
 * -Pdecor_mods=true} to check the mod side too.
 */
@GameTestHolder(Techguns.MODID)
@PrefixGameTestTemplate(false)
public class TGDecorTests {
    private static final String ARENA = "empty_8x4x8";

    private static CompoundTag resourceNbt(GameTestHelper helper, ResourceLocation path) throws IOException {
        try (InputStream in = helper.getLevel().getServer().getResourceManager().getResourceOrThrow(path).open()) {
            return NbtIo.readCompressed(in, NbtAccounter.unlimitedHeap());
        }
    }

    @GameTest(template = ARENA)
    public static void rolesResolveToTheInstalledMod(GameTestHelper helper) {
        for (DecorRole role : DecorRole.values()) {
            ResourceLocation target = role.candidates().getFirst();
            Optional<BlockState> state = DecorBlocks.state(role);
            if (ModList.get().isLoaded(target.getNamespace())) {
                helper.assertTrue(state.isPresent() && BuiltInRegistries.BLOCK.getKey(state.get().getBlock()).equals(target),
                        role + " should use " + target + " (renamed in the mod?)");
            } else {
                helper.assertTrue(state.isEmpty(), role + " should fall back without " + target.getNamespace());
            }
        }
        helper.succeed();
    }

    @GameTest(template = ARENA)
    public static void templatesMarkTheTechguns2ChiselBlocks(GameTestHelper helper) throws IOException {
        CompoundTag template = resourceNbt(helper, TGIds.id("structure/factory_house_small.nbt"));
        Set<String> roles = new HashSet<>();
        ListTag blocks = template.getList("blocks", Tag.TAG_COMPOUND);
        for (int i = 0; i < blocks.size(); i++) {
            CompoundTag nbt = blocks.getCompound(i).getCompound("nbt");
            if (nbt.contains(LegacyPaletteProcessor.KEY)) {
                String decor = nbt.getCompound(LegacyPaletteProcessor.KEY).getString(LegacyPaletteProcessor.DECOR);
                if (!decor.isEmpty()) {
                    roles.add(decor);
                }
            }
        }
        // Techguns 2 FactoryHouseSmall: scaffold, factory plate, technical concrete, crate, dotted plate
        for (DecorRole role : new DecorRole[] {DecorRole.TECHNICAL_BLOCK_SCAFFOLD, DecorRole.FACTORY_PLATE, DecorRole.TECHNICAL_CONCRETE,
                DecorRole.FACTORY_CRATE, DecorRole.FACTORY_PLATE_DOTTED}) {
            helper.assertTrue(roles.contains(role.id()), "factory_house_small should mark " + role.id() + ", has " + roles);
        }
        helper.succeed();
    }

    @GameTest(template = ARENA)
    public static void processorPlacesTheModBlockOrTheFallback(GameTestHelper helper) {
        CompoundTag option = new CompoundTag();
        option.putString(LegacyPaletteProcessor.DECOR, DecorRole.FACTORY_PLATE.id());
        option.put(LegacyPaletteProcessor.STATE, NbtUtils.writeBlockState(Blocks.BRICKS.defaultBlockState()));
        CompoundTag nbt = new CompoundTag();
        nbt.put(LegacyPaletteProcessor.KEY, option);
        StructureTemplate.StructureBlockInfo info = new StructureTemplate.StructureBlockInfo(BlockPos.ZERO, Blocks.BRICKS.defaultBlockState(), nbt);

        StructureTemplate.StructureBlockInfo placed = LegacyPaletteProcessor.INSTANCE.processBlock(helper.getLevel(), BlockPos.ZERO, BlockPos.ZERO,
                info, info, new StructurePlaceSettings());
        BlockState expected = DecorBlocks.stateOr(DecorRole.FACTORY_PLATE, Blocks.BRICKS.defaultBlockState());
        helper.assertTrue(placed != null && placed.state().equals(expected), "Expected " + expected + ", got " + (placed == null ? null : placed.state()));
        helper.succeed();
    }

    @GameTest(template = ARENA)
    public static void rechiseledGroupsAreShipped(GameTestHelper helper) {
        for (String group : new String[] {"camo_net", "camo_net_top", "metal_panel", "neon_lights", "concrete", "metal_ladder"}) {
            helper.assertTrue(helper.getLevel().getServer().getResourceManager().getResource(TGIds.id("chiseling_recipes/" + group + ".json")).isPresent(),
                    "Missing Rechiseled group " + group);
        }
        helper.succeed();
    }
}
