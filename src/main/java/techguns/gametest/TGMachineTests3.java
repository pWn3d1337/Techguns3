package techguns.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import techguns.Techguns;
import techguns.machine.ChargingStationBlockEntity;
import techguns.machine.ChemLabBlockEntity;
import techguns.registry.TGAmmo;
import techguns.registry.TGBlocks;
import techguns.registry.TGFluids;
import techguns.registry.TGItems;

/**
 * Machines (Phase 5b2b): Chem Lab item and fluid recipes with tanks and the flask slot, Charging Station recipe jobs,
 * creeper acid hurting what stands in it.
 */
@GameTestHolder(Techguns.MODID)
@PrefixGameTestTemplate(false)
public class TGMachineTests3 {
    private static final String ARENA = "empty_8x4x8";
    private static final BlockPos POS = new BlockPos(2, 2, 2);
    private static final int OPERATION_TICKS = 105;

    private static ChemLabBlockEntity chemLab(GameTestHelper helper) {
        helper.setBlock(POS, TGBlocks.CHEM_LAB.get());
        ChemLabBlockEntity lab = (ChemLabBlockEntity) helper.getBlockEntity(POS);
        lab.getEnergy().setEnergy(ChemLabBlockEntity.ENERGY_CAPACITY);
        return lab;
    }

    private static IFluidHandler fluids(GameTestHelper helper) {
        return helper.getLevel().getCapability(Capabilities.FluidHandler.BLOCK, helper.absolutePos(POS), null);
    }

    @GameTest(template = ARENA, timeoutTicks = 200)
    public static void chemLabMakesGunpowderFromWater(GameTestHelper helper) {
        ChemLabBlockEntity lab = chemLab(helper);
        IItemHandlerModifiable inv = lab.getInventory();
        helper.assertTrue(fluids(helper).fill(new FluidStack(Fluids.WATER, 1000), IFluidHandler.FluidAction.EXECUTE) == 1000,
                "Water should fill the input tank");
        // swapped inputs: the recipe lists redstone first
        inv.setStackInSlot(ChemLabBlockEntity.SLOT_INPUT1, new ItemStack(Items.COAL));
        inv.setStackInSlot(ChemLabBlockEntity.SLOT_INPUT2, new ItemStack(Items.REDSTONE));

        helper.runAfterDelay(OPERATION_TICKS, () -> {
            helper.assertTrue(inv.getStackInSlot(ChemLabBlockEntity.SLOT_OUTPUT).is(Items.GUNPOWDER), "Redstone + coal + water should make gunpowder");
            helper.assertTrue(lab.getInputTank().getFluidAmount() == 750, "The job takes 250 mB water, left " + lab.getInputTank().getFluidAmount());
            helper.succeed();
        });
    }

    @GameTest(template = ARENA, timeoutTicks = 200)
    public static void chemLabMakesAcidIntoOutputTank(GameTestHelper helper) {
        ChemLabBlockEntity lab = chemLab(helper);
        IItemHandlerModifiable inv = lab.getInventory();
        lab.getInputTank().fill(new FluidStack(Fluids.WATER, 1000), IFluidHandler.FluidAction.EXECUTE);
        inv.setStackInSlot(ChemLabBlockEntity.SLOT_INPUT1, new ItemStack(TGItems.BIOMASS.get()));
        inv.setStackInSlot(ChemLabBlockEntity.SLOT_INPUT2, new ItemStack(Items.GUNPOWDER));

        helper.runAfterDelay(OPERATION_TICKS, () -> {
            FluidStack out = lab.getOutputTank().getFluid();
            helper.assertTrue(out.is(TGFluids.ACID.get()) && out.getAmount() == 1000, "Biomass + gunpowder + water should make 1000 mB acid, got " + out);
            FluidStack drained = fluids(helper).drain(1000, IFluidHandler.FluidAction.EXECUTE);
            helper.assertTrue(drained.is(TGFluids.ACID.get()) && drained.getAmount() == 1000, "Pipes drain the output tank");
            helper.succeed();
        });
    }

    @GameTest(template = ARENA)
    public static void chemLabFlaskSlotTakesOnlyFlasks(GameTestHelper helper) {
        ChemLabBlockEntity lab = chemLab(helper);
        IItemHandlerModifiable inv = lab.getInventory();
        ItemStack tank = new ItemStack(TGAmmo.BIO_TANK_EMPTY.get());
        helper.assertTrue(inv.isItemValid(ChemLabBlockEntity.SLOT_FLASK, tank), "An empty bio tank is a flask");
        helper.assertFalse(inv.isItemValid(ChemLabBlockEntity.SLOT_INPUT1, tank), "Flasks don't go into the item slots");
        helper.assertFalse(inv.isItemValid(ChemLabBlockEntity.SLOT_FLASK, new ItemStack(Items.COAL)), "Coal is no flask");
        helper.succeed();
    }

    @GameTest(template = ARENA, timeoutTicks = 200)
    public static void chargingStationChargesEnergyCell(GameTestHelper helper) {
        helper.setBlock(POS, TGBlocks.CHARGING_STATION.get());
        ChargingStationBlockEntity station = (ChargingStationBlockEntity) helper.getBlockEntity(POS);
        station.getEnergy().setEnergy(ChargingStationBlockEntity.ENERGY_CAPACITY);
        station.getInventory().setStackInSlot(ChargingStationBlockEntity.SLOT_INPUT, new ItemStack(TGAmmo.ENERGY_CELL_EMPTY.get()));

        // 50000 FE at 800 FE per tick: 62 ticks
        helper.runAfterDelay(70, () -> {
            ItemStack out = station.getInventory().getStackInSlot(ChargingStationBlockEntity.SLOT_OUTPUT);
            helper.assertTrue(out.is(TGAmmo.ENERGY_CELL.get()), "The empty cell should be charged, got " + out);
            int used = ChargingStationBlockEntity.ENERGY_CAPACITY - station.getEnergy().getEnergyStored();
            helper.assertTrue(used == 62 * ChargingStationBlockEntity.CHARGE_RATE, "Charging takes 62 ticks at 800 FE, used " + used);
            helper.succeed();
        });
    }

    @GameTest(template = ARENA, timeoutTicks = 100)
    public static void creeperAcidHurts(GameTestHelper helper) {
        BlockPos pos = new BlockPos(3, 2, 3);
        helper.setBlock(pos.below(), net.minecraft.world.level.block.Blocks.STONE);
        helper.setBlock(pos, TGFluids.ACID_BLOCK.get());
        Husk husk = TGTestTargets.husk(helper, pos);
        float health = husk.getHealth();
        // the hurt cooldown (10 ticks) is kept, as in Techguns 2: at most 2 damage every 10 ticks, not every tick
        helper.runAfterDelay(30, () -> {
            float lost = health - husk.getHealth();
            helper.assertTrue(lost > 0.0f, "Acid should hurt the husk");
            helper.assertTrue(lost <= 8.0f, "Acid keeps the hurt cooldown, lost " + lost + " in 30 ticks");
            helper.succeed();
        });
    }
}
