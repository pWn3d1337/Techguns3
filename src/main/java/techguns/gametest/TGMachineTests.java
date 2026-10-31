package techguns.gametest;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.Tags;
import techguns.machine.TGMachineBlock;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.state.BlockState;
import techguns.compat.jei.SpecialCraftingExamples;
import techguns.multiblock.OreDrillPartItem;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import techguns.core.damage.DeathType;
import techguns.item.TGMeleeWeapon;
import techguns.registry.TGAmmo;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.items.IItemHandler;
import techguns.Techguns;
import techguns.machine.MetalPressBlockEntity;
import techguns.machine.RedstoneMode;
import techguns.machine.TGMachineBlockEntity;
import techguns.registry.TGBlocks;
import techguns.registry.TGItems;

/**
 * Machines (Phase 5b1): the Metal Press presses by recipe with power, splits stacks, runs several operations at once
 * with stack upgrades, honours redstone mode and security; automation inserts by the slot rules and only extracts
 * results.
 */
@GameTestHolder(Techguns.MODID)
@PrefixGameTestTemplate(false)
public class TGMachineTests {
    private static final String ARENA = "empty_8x4x8";
    private static final BlockPos PRESS = new BlockPos(2, 2, 2);
    /** One Metal Press operation plus some slack. */
    private static final int OPERATION_TICKS = MetalPressBlockEntity.TIME + 5;

    private static MetalPressBlockEntity press(GameTestHelper helper) {
        helper.setBlock(PRESS, TGBlocks.METAL_PRESS.get());
        return (MetalPressBlockEntity) helper.getBlockEntity(PRESS);
    }

    private static Player playerAt(GameTestHelper helper, BlockPos pos) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        BlockPos abs = helper.absolutePos(pos);
        player.moveTo(abs.getX() + 0.5, abs.getY(), abs.getZ() + 1.5);
        return player;
    }

    @GameTest(template = ARENA, timeoutTicks = 200)
    public static void metalPressMakesPlatesWithPower(GameTestHelper helper) {
        MetalPressBlockEntity press = press(helper);
        press.getEnergy().setEnergy(MetalPressBlockEntity.ENERGY_CAPACITY);
        press.getInventory().setStackInSlot(MetalPressBlockEntity.SLOT_INPUT1, new ItemStack(Items.IRON_INGOT));
        press.getInventory().setStackInSlot(MetalPressBlockEntity.SLOT_INPUT2, new ItemStack(Items.IRON_INGOT));

        helper.runAfterDelay(OPERATION_TICKS, () -> {
            ItemStack out = press.getInventory().getStackInSlot(MetalPressBlockEntity.SLOT_OUTPUT);
            helper.assertTrue(out.is(TGItems.IRON_PLATE.get()) && out.getCount() == 2, "Two iron ingots should make 2 iron plates, got " + out);
            int used = MetalPressBlockEntity.ENERGY_CAPACITY - press.getEnergy().getEnergyStored();
            helper.assertTrue(used == MetalPressBlockEntity.TIME * MetalPressBlockEntity.POWER_PER_TICK,
                    "One operation should use 100 x 20 FE, used " + used);
            helper.succeed();
        });
    }

    @GameTest(template = ARENA, timeoutTicks = 200)
    public static void metalPressMakesNetheritePlates(GameTestHelper helper) {
        MetalPressBlockEntity press = press(helper);
        press.getEnergy().setEnergy(MetalPressBlockEntity.ENERGY_CAPACITY);
        press.getInventory().setStackInSlot(MetalPressBlockEntity.SLOT_INPUT1, new ItemStack(Items.NETHERITE_INGOT));
        press.getInventory().setStackInSlot(MetalPressBlockEntity.SLOT_INPUT2, new ItemStack(Items.NETHERITE_INGOT));

        helper.runAfterDelay(OPERATION_TICKS, () -> {
            ItemStack out = press.getInventory().getStackInSlot(MetalPressBlockEntity.SLOT_OUTPUT);
            helper.assertTrue(out.is(TGItems.NETHERITE_PLATE.get()) && out.getCount() == 2,
                    "Two netherite ingots should make 2 netherite plates, got " + out);
            helper.assertTrue(out.has(net.minecraft.core.component.DataComponents.FIRE_RESISTANT), "Netherite plates should not burn");
            helper.succeed();
        });
    }

    @GameTest(template = ARENA, timeoutTicks = 200)
    public static void metalPressNeedsPower(GameTestHelper helper) {
        MetalPressBlockEntity press = press(helper);
        press.getInventory().setStackInSlot(MetalPressBlockEntity.SLOT_INPUT1, new ItemStack(Items.IRON_INGOT));
        press.getInventory().setStackInSlot(MetalPressBlockEntity.SLOT_INPUT2, new ItemStack(Items.IRON_INGOT));

        helper.runAfterDelay(OPERATION_TICKS, () -> {
            helper.assertTrue(press.isWorking(), "The job should have started (inputs taken)");
            helper.assertTrue(press.getInventory().getStackInSlot(MetalPressBlockEntity.SLOT_OUTPUT).isEmpty(),
                    "Without power the press should not finish");
            helper.succeed();
        });
    }

    @GameTest(template = ARENA, timeoutTicks = 200)
    public static void metalPressAcceptsSwappedInputs(GameTestHelper helper) {
        MetalPressBlockEntity press = press(helper);
        press.getEnergy().setEnergy(MetalPressBlockEntity.ENERGY_CAPACITY);
        // recipe is iron plate + flint; the inputs are in the other order
        press.getInventory().setStackInSlot(MetalPressBlockEntity.SLOT_INPUT1, new ItemStack(Items.FLINT));
        press.getInventory().setStackInSlot(MetalPressBlockEntity.SLOT_INPUT2, new ItemStack(TGItems.IRON_PLATE.get()));

        helper.runAfterDelay(OPERATION_TICKS, () -> {
            ItemStack out = press.getInventory().getStackInSlot(MetalPressBlockEntity.SLOT_OUTPUT);
            helper.assertTrue(out.is(TGItems.MECHANICAL_PARTS_IRON.get()), "Swapped inputs should still press, got " + out);
            helper.succeed();
        });
    }

    @GameTest(template = ARENA, timeoutTicks = 200)
    public static void metalPressAutoSplitAndStackUpgrade(GameTestHelper helper) {
        MetalPressBlockEntity press = press(helper);
        Player player = playerAt(helper, PRESS);
        press.getEnergy().setEnergy(MetalPressBlockEntity.ENERGY_CAPACITY);
        helper.assertTrue(press.pressButton(player, MetalPressBlockEntity.BUTTON_AUTO_SPLIT), "Auto split should switch on");
        // 3 upgrades: up to 4 operations at once; 8 ingots split 4 / 4
        press.getInventory().setStackInSlot(MetalPressBlockEntity.SLOT_UPGRADE, new ItemStack(TGItems.MACHINE_UPGRADE_STACK.get(), 3));
        press.getInventory().setStackInSlot(MetalPressBlockEntity.SLOT_INPUT1, new ItemStack(Items.IRON_INGOT, 8));

        helper.runAfterDelay(OPERATION_TICKS, () -> {
            ItemStack out = press.getInventory().getStackInSlot(MetalPressBlockEntity.SLOT_OUTPUT);
            helper.assertTrue(out.is(TGItems.IRON_PLATE.get()) && out.getCount() == 8, "8 split ingots should make 8 plates at once, got " + out);
            int used = MetalPressBlockEntity.ENERGY_CAPACITY - press.getEnergy().getEnergyStored();
            helper.assertTrue(used == 4 * MetalPressBlockEntity.TIME * MetalPressBlockEntity.POWER_PER_TICK,
                    "Four operations at once should use four times the power, used " + used);
            helper.succeed();
        });
    }

    @GameTest(template = ARENA, timeoutTicks = 300)
    public static void metalPressWaitsForRedstone(GameTestHelper helper) {
        MetalPressBlockEntity press = press(helper);
        Player player = playerAt(helper, PRESS);
        press.getEnergy().setEnergy(MetalPressBlockEntity.ENERGY_CAPACITY);
        press.pressButton(player, TGMachineBlockEntity.BUTTON_REDSTONE);
        helper.assertTrue(press.getRedstoneMode() == RedstoneMode.HIGH, "One press should require a high signal");
        press.getInventory().setStackInSlot(MetalPressBlockEntity.SLOT_INPUT1, new ItemStack(Items.IRON_INGOT));
        press.getInventory().setStackInSlot(MetalPressBlockEntity.SLOT_INPUT2, new ItemStack(Items.IRON_INGOT));

        helper.runAfterDelay(OPERATION_TICKS, () -> {
            helper.assertTrue(press.getInventory().getStackInSlot(MetalPressBlockEntity.SLOT_OUTPUT).isEmpty(),
                    "Without a signal the press should not work");
            helper.setBlock(PRESS.relative(Direction.EAST), Blocks.REDSTONE_BLOCK);
            helper.runAfterDelay(OPERATION_TICKS, () -> {
                helper.assertTrue(press.hasSignal(), "The press should see the redstone block");
                helper.assertFalse(press.getInventory().getStackInSlot(MetalPressBlockEntity.SLOT_OUTPUT).isEmpty(),
                        "With a signal the press should finish");
                helper.succeed();
            });
        });
    }

    @GameTest(template = ARENA)
    public static void ownerOnlyLocksOthersOut(GameTestHelper helper) {
        MetalPressBlockEntity press = press(helper);
        Player owner = playerAt(helper, PRESS);
        Player other = playerAt(helper, PRESS);
        press.setOwner(owner);

        helper.assertFalse(press.pressButton(other, TGMachineBlockEntity.BUTTON_SECURITY), "Only the owner may change security");
        helper.assertTrue(press.pressButton(owner, TGMachineBlockEntity.BUTTON_SECURITY), "The owner may change security");
        helper.assertTrue(press.mayAccess(owner), "The owner keeps access");
        helper.assertFalse(press.mayAccess(other), "Others are locked out in owner-only mode");
        helper.succeed();
    }

    @GameTest(template = ARENA)
    public static void automationFollowsSlotRules(GameTestHelper helper) {
        MetalPressBlockEntity press = press(helper);
        IItemHandler items = helper.getLevel().getCapability(Capabilities.ItemHandler.BLOCK, helper.absolutePos(PRESS), null);
        IEnergyStorage energy = helper.getLevel().getCapability(Capabilities.EnergyStorage.BLOCK, helper.absolutePos(PRESS), null);
        helper.assertTrue(items != null && energy != null, "The press should expose items and energy");

        helper.assertTrue(energy.receiveEnergy(500, false) == 500 && energy.extractEnergy(100, false) == 0,
                "Power goes in, but not out");
        ItemStack dirt = items.insertItem(MetalPressBlockEntity.SLOT_INPUT1, new ItemStack(Items.DIRT), false);
        helper.assertTrue(dirt.getCount() == 1, "Dirt is no Metal Press input");
        ItemStack ingot = items.insertItem(MetalPressBlockEntity.SLOT_INPUT1, new ItemStack(Items.IRON_INGOT), false);
        helper.assertTrue(ingot.isEmpty(), "An iron ingot is a Metal Press input");
        helper.assertTrue(items.extractItem(MetalPressBlockEntity.SLOT_INPUT1, 1, false).isEmpty(), "Inputs can't be pulled out");
        press.getInventory().setStackInSlot(MetalPressBlockEntity.SLOT_OUTPUT, new ItemStack(TGItems.IRON_PLATE.get(), 2));
        helper.assertTrue(items.extractItem(MetalPressBlockEntity.SLOT_OUTPUT, 2, false).getCount() == 2, "Results can be pulled out");
        helper.succeed();
    }

    @GameTest(template = ARENA)
    public static void brokenPressDropsContents(GameTestHelper helper) {
        MetalPressBlockEntity press = press(helper);
        press.getInventory().setStackInSlot(MetalPressBlockEntity.SLOT_INPUT1, new ItemStack(Items.IRON_INGOT, 5));
        helper.destroyBlock(PRESS);
        helper.assertItemEntityCountIs(Items.IRON_INGOT, PRESS, 2.0, 5);
        helper.succeed();
    }

    /** Right-clicks the press with a crowbar like a player (the wrench event runs before the block's use). */
    private static void wrench(GameTestHelper helper, Player player, boolean sneaking) {
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(TGItems.CROWBAR.get()));
        player.setShiftKeyDown(sneaking);
        BlockPos abs = helper.absolutePos(PRESS);
        CommonHooks.onRightClickBlock(player, InteractionHand.MAIN_HAND, abs, new BlockHitResult(Vec3.atCenterOf(abs), Direction.UP, abs, false));
    }

    /** Techguns 2 wrench: use turns a machine, sneak + use dismantles it into an item that keeps its contents. */
    @GameTest(template = ARENA)
    public static void wrenchTurnsAndDismantlesMachines(GameTestHelper helper) {
        MetalPressBlockEntity press = press(helper);
        Player player = playerAt(helper, PRESS);
        helper.assertTrue(TGItems.CROWBAR.get().getDefaultInstance().is(Tags.Items.TOOLS_WRENCH), "The crowbar is a wrench");
        Direction facing = helper.getBlockState(PRESS).getValue(TGMachineBlock.FACING);
        wrench(helper, player, false);
        helper.assertTrue(helper.getBlockState(PRESS).getValue(TGMachineBlock.FACING) == facing.getClockWise(), "A wrench turns the machine");

        press.getInventory().setStackInSlot(MetalPressBlockEntity.SLOT_INPUT1, new ItemStack(Items.IRON_INGOT, 5));
        wrench(helper, player, true);
        helper.assertBlockNotPresent(TGBlocks.METAL_PRESS.get(), PRESS);
        helper.assertItemEntityCountIs(Items.IRON_INGOT, PRESS, 2.0, 0);
        List<ItemEntity> items = helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(helper.absolutePos(PRESS)).inflate(2.0),
                entity -> entity.getItem().is(TGBlocks.METAL_PRESS.get().asItem()));
        helper.assertTrue(items.size() == 1, "Sneak + wrench drops the machine as an item");
        ItemStack item = items.get(0).getItem();
        helper.assertTrue(item.has(DataComponents.BLOCK_ENTITY_DATA), "The machine item keeps the block entity data");

        // placing it again brings the contents back
        helper.setBlock(PRESS, TGBlocks.METAL_PRESS.get());
        BlockItem.updateCustomBlockEntityTag(helper.getLevel(), player, helper.absolutePos(PRESS), item);
        ItemStack input = ((MetalPressBlockEntity) helper.getBlockEntity(PRESS)).getInventory().getStackInSlot(MetalPressBlockEntity.SLOT_INPUT1);
        helper.assertTrue(input.is(Items.IRON_INGOT) && input.getCount() == 5, "The placed machine has its contents back, got " + input);

        // multiblock controllers are not wrenched (Techguns 2)
        helper.assertTrue(!TGBlocks.FABRICATOR_CONTROLLER.get().canBeWrenched() && TGBlocks.TURRET.get().canBeWrenched(),
                "Multiblocks are not wrenched, turrets are");
        helper.succeed();
    }

    /** Techguns 2 crowbar: a steel (iron level) pickaxe. */
    @GameTest(template = ARENA)
    public static void crowbarIsASteelPickaxe(GameTestHelper helper) {
        ItemStack crowbar = new ItemStack(TGItems.CROWBAR.get());
        helper.assertTrue(crowbar.isCorrectToolForDrops(Blocks.IRON_ORE.defaultBlockState()), "The crowbar mines iron ore");
        helper.assertTrue(crowbar.isCorrectToolForDrops(Blocks.DIAMOND_ORE.defaultBlockState()), "The crowbar mines diamond ore (iron level)");
        helper.assertTrue(!crowbar.isCorrectToolForDrops(Blocks.OBSIDIAN.defaultBlockState()), "The crowbar does not mine obsidian");
        helper.assertTrue(crowbar.getMaxDamage() == 1000, "1000 uses like Techguns 2");
        helper.succeed();
    }

    /** Techguns 2 combat knife: a steel sword (6.5 damage) whose kills gore a quarter of the time, like the crowbar's. */
    @GameTest(template = ARENA)
    public static void combatKnifeIsASteelSword(GameTestHelper helper) {
        ItemStack knife = new ItemStack(TGItems.COMBAT_KNIFE.get());
        helper.assertTrue(knife.is(ItemTags.SWORDS) && knife.getMaxDamage() == 1000, "A steel sword");
        double[] damage = {1.0};
        knife.getAttributeModifiers().forEach(EquipmentSlot.MAINHAND, (attribute, modifier) -> {
            if (attribute.is(Attributes.ATTACK_DAMAGE)) {
                damage[0] += modifier.amount();
            }
        });
        helper.assertTrue(Math.abs(damage[0] - 6.5) < 1.0e-4, "Techguns 2 knife damage 6.5, got " + damage[0]);
        for (ItemStack weapon : new ItemStack[] {knife, new ItemStack(TGItems.CROWBAR.get())}) {
            TGMeleeWeapon melee = (TGMeleeWeapon) weapon.getItem();
            helper.assertTrue(melee.meleeDeathType(weapon) == DeathType.GORE && melee.meleeGoreChance() == 0.25f, weapon + " gores a quarter of kills");
        }
        helper.succeed();
    }

    /** Techguns 2 {@code ItemBlockOreDrill}: where drill rods and controllers may be placed. */
    @GameTest(template = ARENA)
    public static void oreDrillPartsPlacementRules(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockState rod = TGBlocks.ORE_DRILL_ROD.get().defaultBlockState();
        BlockState controller = TGBlocks.ORE_DRILL_CONTROLLER.get().defaultBlockState();
        BlockPos cluster = new BlockPos(4, 1, 4);
        helper.setBlock(cluster, TGBlocks.ORE_CLUSTER_COAL.get());
        helper.setBlock(cluster.east(), TGBlocks.ORE_CLUSTER_COAL.get());
        helper.assertTrue("techguns.msg.error.rodplacement".equals(OreDrillPartItem.placementError(level, helper.absolutePos(new BlockPos(1, 1, 1)), rod)),
                "A rod alone is refused");
        helper.assertTrue(OreDrillPartItem.placementError(level, helper.absolutePos(cluster.above()), rod) == null, "A rod on a cluster is fine");
        helper.setBlock(cluster.above(), rod);
        helper.assertTrue(OreDrillPartItem.placementError(level, helper.absolutePos(cluster.above(2)), rod) == null, "A rod on a rod is fine");
        helper.assertTrue("techguns.msg.error.alreadyrod".equals(OreDrillPartItem.placementError(level, helper.absolutePos(cluster.east().above()), rod)),
                "A second rod at the same cluster is refused");
        BlockPos first = new BlockPos(1, 1, 6);
        helper.setBlock(first, controller);
        helper.assertTrue("techguns.msg.error.oredrill.controllernext".equals(OreDrillPartItem.placementError(level, helper.absolutePos(first.east()), controller)),
                "No controller next to a controller");
        helper.assertTrue(OreDrillPartItem.placementError(level, helper.absolutePos(first.east(2)), controller) == null, "One block apart is fine");
        helper.succeed();
    }

    /** The JEI examples of the special crafting recipes are what crafting really gives. */
    @GameTest(template = ARENA)
    public static void specialCraftingExamplesMatchTheRecipes(GameTestHelper helper) {
        var level = helper.getLevel();
        List<RecipeHolder<CraftingRecipe>> examples = SpecialCraftingExamples.all(level.registryAccess());
        helper.assertTrue(examples.stream().anyMatch(e -> e.id().getPath().startsWith("jei/ammo_change/"))
                && examples.stream().anyMatch(e -> e.id().getPath().startsWith("jei/mining_head/")), "Ammo change and mining head examples");
        for (RecipeHolder<CraftingRecipe> example : examples) {
            List<ItemStack> inputs = example.value().getIngredients().stream().map(ingredient -> ingredient.getItems()[0]).toList();
            CraftingInput input = CraftingInput.of(2, 1, inputs);
            ItemStack crafted = level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, level)
                    .map(recipe -> recipe.value().assemble(input, level.registryAccess())).orElse(ItemStack.EMPTY);
            ItemStack shown = example.value().getResultItem(level.registryAccess());
            helper.assertTrue(ItemStack.isSameItemSameComponents(crafted, shown), example.id() + " shows " + shown + " but crafting gives " + crafted);
        }
        helper.succeed();
    }

    /** The nuke rocket is crafted from a rocket and a tactical nuke warhead (Techguns 2 shapeless recipe). */
    @GameTest(template = ARENA)
    public static void warheadMakesNukeRockets(GameTestHelper helper) {
        CraftingInput input = CraftingInput.of(2, 1, List.of(new ItemStack(TGAmmo.ROCKET.get()), new ItemStack(TGItems.TACTICAL_NUKE_WARHEAD.get())));
        ItemStack result = helper.getLevel().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, helper.getLevel())
                .map(recipe -> recipe.value().assemble(input, helper.getLevel().registryAccess())).orElse(ItemStack.EMPTY);
        helper.assertTrue(result.is(TGAmmo.ROCKET_NUKE.get()), "Rocket + warhead = nuke rocket, got " + result);
        helper.succeed();
    }
}
