package techguns.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import techguns.Techguns;
import techguns.machine.AmmoPressBlockEntity;
import techguns.machine.BlastFurnaceBlockEntity;
import techguns.machine.GrinderBlockEntity;
import techguns.registry.TGAmmo;
import techguns.registry.TGBlocks;
import techguns.registry.TGGuns;
import techguns.registry.TGItems;

/**
 * Machines (Phase 5b2a): Ammo Press build plans, Blast Furnace counted inputs and per-recipe power, Grinder gun
 * recycling and rolled round outputs with the stack upgrade.
 */
@GameTestHolder(Techguns.MODID)
@PrefixGameTestTemplate(false)
public class TGMachineTests2 {
    private static final String ARENA = "empty_8x4x8";
    private static final BlockPos POS = new BlockPos(2, 2, 2);
    private static final int OPERATION_TICKS = 105;

    private static Player playerAt(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        BlockPos abs = helper.absolutePos(POS);
        player.moveTo(abs.getX() + 0.5, abs.getY(), abs.getZ() + 1.5);
        return player;
    }

    /** Total count of {@code item} in the slots {@code from..to}. */
    private static int count(IItemHandlerModifiable inventory, int from, int to, Item item) {
        int total = 0;
        for (int i = from; i <= to; i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (stack.is(item)) {
                total += stack.getCount();
            }
        }
        return total;
    }

    @GameTest(template = ARENA, timeoutTicks = 300)
    public static void ammoPressFollowsBuildPlan(GameTestHelper helper) {
        helper.setBlock(POS, TGBlocks.AMMO_PRESS.get());
        AmmoPressBlockEntity press = (AmmoPressBlockEntity) helper.getBlockEntity(POS);
        press.getEnergy().setEnergy(AmmoPressBlockEntity.ENERGY_CAPACITY);
        IItemHandlerModifiable inv = press.getInventory();
        inv.setStackInSlot(AmmoPressBlockEntity.SLOT_BULLET_METAL, new ItemStack(TGItems.LEAD_INGOT.get()));
        inv.setStackInSlot(AmmoPressBlockEntity.SLOT_CASING_METAL, new ItemStack(Items.COPPER_INGOT, 3));
        inv.setStackInSlot(AmmoPressBlockEntity.SLOT_POWDER, new ItemStack(Items.GUNPOWDER));

        helper.runAfterDelay(OPERATION_TICKS, () -> {
            ItemStack out = inv.getStackInSlot(AmmoPressBlockEntity.SLOT_OUTPUT);
            helper.assertTrue(out.is(TGAmmo.PISTOL_ROUNDS.get()) && out.getCount() == 12, "Plan 1 should make 12 pistol rounds, got " + out);
            helper.assertTrue(inv.getStackInSlot(AmmoPressBlockEntity.SLOT_CASING_METAL).getCount() == 1, "One operation takes two casing metal");
            press.pressButton(playerAt(helper), AmmoPressBlockEntity.BUTTON_NEXT);
            inv.setStackInSlot(AmmoPressBlockEntity.SLOT_OUTPUT, ItemStack.EMPTY);
            inv.setStackInSlot(AmmoPressBlockEntity.SLOT_BULLET_METAL, new ItemStack(TGItems.LEAD_INGOT.get()));
            inv.setStackInSlot(AmmoPressBlockEntity.SLOT_CASING_METAL, new ItemStack(Items.COPPER_INGOT, 2));
            inv.setStackInSlot(AmmoPressBlockEntity.SLOT_POWDER, new ItemStack(Items.GUNPOWDER));
            helper.runAfterDelay(OPERATION_TICKS, () -> {
                ItemStack shotgun = inv.getStackInSlot(AmmoPressBlockEntity.SLOT_OUTPUT);
                helper.assertTrue(shotgun.is(TGAmmo.SHOTGUN_ROUNDS.get()) && shotgun.getCount() == 16, "Plan 2 should make 16 shotgun rounds, got " + shotgun);
                helper.succeed();
            });
        });
    }

    @GameTest(template = ARENA, timeoutTicks = 200)
    public static void ammoPressNeedsTwoCasingMetal(GameTestHelper helper) {
        helper.setBlock(POS, TGBlocks.AMMO_PRESS.get());
        AmmoPressBlockEntity press = (AmmoPressBlockEntity) helper.getBlockEntity(POS);
        press.getEnergy().setEnergy(AmmoPressBlockEntity.ENERGY_CAPACITY);
        IItemHandlerModifiable inv = press.getInventory();
        inv.setStackInSlot(AmmoPressBlockEntity.SLOT_BULLET_METAL, new ItemStack(TGItems.STEEL_INGOT.get()));
        inv.setStackInSlot(AmmoPressBlockEntity.SLOT_CASING_METAL, new ItemStack(Items.IRON_INGOT));
        inv.setStackInSlot(AmmoPressBlockEntity.SLOT_POWDER, new ItemStack(Items.GUNPOWDER));

        helper.runAfterDelay(OPERATION_TICKS, () -> {
            helper.assertFalse(press.isWorking(), "One casing metal is not enough");
            helper.assertTrue(inv.getStackInSlot(AmmoPressBlockEntity.SLOT_OUTPUT).isEmpty(), "Nothing should be made");
            helper.assertFalse(inv.isItemValid(AmmoPressBlockEntity.SLOT_BULLET_METAL, new ItemStack(Items.COPPER_INGOT)),
                    "Copper is no bullet metal");
            helper.succeed();
        });
    }

    @GameTest(template = ARENA, timeoutTicks = 200)
    public static void blastFurnaceMakesBronzeWithRecipePower(GameTestHelper helper) {
        helper.setBlock(POS, TGBlocks.BLAST_FURNACE.get());
        BlastFurnaceBlockEntity furnace = (BlastFurnaceBlockEntity) helper.getBlockEntity(POS);
        furnace.getEnergy().setEnergy(BlastFurnaceBlockEntity.ENERGY_CAPACITY);
        IItemHandlerModifiable inv = furnace.getInventory();
        helper.assertFalse(inv.isItemValid(BlastFurnaceBlockEntity.SLOT_INPUT1, new ItemStack(Items.COAL)), "Coal belongs in the second slot");
        inv.setStackInSlot(BlastFurnaceBlockEntity.SLOT_INPUT1, new ItemStack(Items.COPPER_INGOT, 3));
        inv.setStackInSlot(BlastFurnaceBlockEntity.SLOT_INPUT2, new ItemStack(TGItems.TIN_INGOT.get()));

        helper.runAfterDelay(OPERATION_TICKS, () -> {
            ItemStack out = inv.getStackInSlot(BlastFurnaceBlockEntity.SLOT_OUTPUT);
            helper.assertTrue(out.is(TGItems.BRONZE_INGOT.get()) && out.getCount() == 4, "3 copper + 1 tin should make 4 bronze, got " + out);
            int used = BlastFurnaceBlockEntity.ENERGY_CAPACITY - furnace.getEnergy().getEnergyStored();
            helper.assertTrue(used == 100 * 10, "Bronze takes 100 ticks at 10 FE, used " + used);
            helper.succeed();
        });
    }

    @GameTest(template = ARENA, timeoutTicks = 200)
    public static void grinderRecyclesGun(GameTestHelper helper) {
        helper.setBlock(POS, TGBlocks.GRINDER.get());
        GrinderBlockEntity grinder = (GrinderBlockEntity) helper.getBlockEntity(POS);
        grinder.getEnergy().setEnergy(GrinderBlockEntity.ENERGY_CAPACITY);
        IItemHandlerModifiable inv = grinder.getInventory();
        inv.setStackInSlot(GrinderBlockEntity.SLOT_INPUT, new ItemStack(TGGuns.M4.get()));

        helper.runAfterDelay(OPERATION_TICKS, () -> {
            int first = GrinderBlockEntity.SLOT_OUTPUT_FIRST;
            int last = GrinderBlockEntity.SLOT_OUTPUT_LAST;
            helper.assertTrue(count(inv, first, last, TGItems.OBSIDIAN_STEEL_INGOT.get()) == 3, "The M4 barrel gives 3 obsidian steel");
            helper.assertTrue(count(inv, first, last, TGItems.STEEL_INGOT.get()) == 2, "The M4 gives 2 steel");
            helper.assertTrue(count(inv, first, last, TGItems.PLASTIC_SHEET.get()) == 2, "The M4 gives 2 plastic");
            helper.succeed();
        });
    }

    @GameTest(template = ARENA, timeoutTicks = 200)
    public static void grinderRollsRoundsWithStackUpgrade(GameTestHelper helper) {
        helper.setBlock(POS, TGBlocks.GRINDER.get());
        GrinderBlockEntity grinder = (GrinderBlockEntity) helper.getBlockEntity(POS);
        grinder.getEnergy().setEnergy(GrinderBlockEntity.ENERGY_CAPACITY);
        IItemHandlerModifiable inv = grinder.getInventory();
        inv.setStackInSlot(GrinderBlockEntity.SLOT_UPGRADE, new ItemStack(TGItems.MACHINE_UPGRADE_STACK.get(), 3));
        inv.setStackInSlot(GrinderBlockEntity.SLOT_INPUT, new ItemStack(TGAmmo.RIFLE_ROUNDS.get(), 8));

        helper.runAfterDelay(OPERATION_TICKS, () -> {
            int first = GrinderBlockEntity.SLOT_OUTPUT_FIRST;
            int last = GrinderBlockEntity.SLOT_OUTPUT_LAST;
            helper.assertTrue(count(inv, first, last, TGItems.LEAD_NUGGET.get()) == 4, "Four rounds at once with three upgrades: 4 lead nuggets");
            helper.assertTrue(count(inv, first, last, TGItems.COPPER_NUGGET.get()) == 8, "2 copper nuggets per rifle round");
            helper.assertTrue(count(inv, first, last, Items.GUNPOWDER) <= 1, "Powder is a 1/8 chance per round");
            helper.succeed();
        });
    }
}
