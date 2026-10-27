package techguns.gametest;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.minecraft.world.inventory.ContainerLevelAccess;
import techguns.TGConfig;
import techguns.machine.AmmoBenchMenu;
import techguns.machine.RepairBenchMenu;
import techguns.machine.UpgradeBenchBlockEntity;
import techguns.machine.UpgradeBenchMenu;
import techguns.Techguns;
import techguns.armor.RepairMaterial;
import techguns.camo.CamoData;
import techguns.gun.GunItem;
import techguns.machine.CamoBenchBlockEntity;
import techguns.machine.GrinderBlockEntity;
import techguns.machine.RepairBenchBlockEntity;
import techguns.recipe.AmmoBenchRecipe;
import techguns.recipe.MachineInput;
import techguns.recipe.UpgradeBenchRecipe;
import techguns.registry.TGAmmo;
import techguns.registry.TGArmors;
import techguns.registry.TGBlocks;
import techguns.registry.TGGuns;
import techguns.registry.TGItems;
import techguns.registry.TGRecipes;

/**
 * Benches (Phase 5b3): Repair Bench costs and repairs, Upgrade Bench enchanting, Camo Bench looks and buttons, Ammo
 * Bench recipe choices, Grinder armor recycling.
 */
@GameTestHolder(Techguns.MODID)
@PrefixGameTestTemplate(false)
public class TGBenchTests {
    private static final String ARENA = "empty_8x4x8";
    private static final BlockPos POS = new BlockPos(2, 2, 2);

    private static int count(List<RepairMaterial> cost, net.minecraft.world.item.Item item) {
        return cost.stream().filter(m -> m.ingredient().test(new ItemStack(item))).mapToInt(RepairMaterial::count).sum();
    }

    private static Player playerAtBench(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        BlockPos pos = helper.absolutePos(POS);
        player.moveTo(pos.getX() + 1.5, pos.getY(), pos.getZ() + 0.5);
        return player;
    }

    @GameTest(template = ARENA)
    public static void repairCostScalesWithDamage(GameTestHelper helper) {
        ItemStack chest = new ItemStack(TGArmors.T1_COMBAT_CHESTPLATE.get());
        chest.setDamageValue(chest.getMaxDamage() - 1);
        var cost = TGArmors.T1_COMBAT_CHESTPLATE.get().repairCost(chest);
        helper.assertTrue(count(cost, Items.IRON_INGOT) == 2 && count(cost, TGItems.HEAVY_CLOTH.get()) == 2,
                "A worn-out T1 chestplate needs 2 iron and 2 heavy cloth");

        chest.setDamageValue((chest.getMaxDamage() - 1) / 2);
        cost = TGArmors.T1_COMBAT_CHESTPLATE.get().repairCost(chest);
        helper.assertTrue(count(cost, Items.IRON_INGOT) == 1 && count(cost, TGItems.HEAVY_CLOTH.get()) == 1, "Half damage costs half: " + cost);

        ItemStack legs = new ItemStack(TGArmors.T1_COMBAT_LEGGINGS.get());
        legs.setDamageValue(legs.getMaxDamage() - 1);
        cost = TGArmors.T1_COMBAT_LEGGINGS.get().repairCost(legs);
        helper.assertTrue(count(cost, Items.IRON_INGOT) == 1 && count(cost, TGItems.HEAVY_CLOTH.get()) == 2, "Leggings are a third metal: " + cost);

        ItemStack scout = new ItemStack(TGArmors.T1_SCOUT_HELMET.get());
        scout.setDamageValue(scout.getMaxDamage() - 1);
        helper.assertTrue(count(TGArmors.T1_SCOUT_HELMET.get().repairCost(scout), TGItems.HEAVY_CLOTH.get()) == 2, "Scout armor is cloth only");

        ItemStack shield = new ItemStack(TGArmors.RIOT_SHIELD.get());
        helper.assertTrue(TGArmors.RIOT_SHIELD.get().repairCost(shield).isEmpty(), "An undamaged shield needs nothing");
        shield.setDamageValue(shield.getMaxDamage() - 1);
        helper.assertTrue(count(TGArmors.RIOT_SHIELD.get().repairCost(shield), TGItems.STEEL_INGOT.get()) == 3, "A worn riot shield needs 3 steel");
        helper.succeed();
    }

    @GameTest(template = ARENA)
    public static void repairBenchRepairsWithMaterials(GameTestHelper helper) {
        helper.setBlock(POS, TGBlocks.REPAIR_BENCH.get());
        RepairBenchBlockEntity bench = (RepairBenchBlockEntity) helper.getBlockEntity(POS);
        IItemHandlerModifiable inv = bench.getInventory();
        ItemStack chest = new ItemStack(TGArmors.T1_COMBAT_CHESTPLATE.get());
        chest.setDamageValue(chest.getMaxDamage() - 1);
        inv.setStackInSlot(RepairBenchBlockEntity.SLOT_REPAIR, chest);
        inv.setStackInSlot(0, new ItemStack(Items.IRON_INGOT, 5));

        Player player = playerAtBench(helper);
        bench.pressButton(player, RepairBenchBlockEntity.BUTTON_REPAIR_SLOT);
        helper.assertTrue(inv.getStackInSlot(RepairBenchBlockEntity.SLOT_REPAIR).isDamaged(), "Without heavy cloth nothing is repaired");
        helper.assertTrue(inv.getStackInSlot(0).getCount() == 5, "A failed repair takes nothing");

        inv.setStackInSlot(4, new ItemStack(TGItems.HEAVY_CLOTH.get(), 1));
        inv.setStackInSlot(8, new ItemStack(TGItems.HEAVY_CLOTH.get(), 1));
        bench.pressButton(player, RepairBenchBlockEntity.BUTTON_REPAIR_SLOT);
        helper.assertFalse(inv.getStackInSlot(RepairBenchBlockEntity.SLOT_REPAIR).isDamaged(), "Iron and cloth should fully repair it");
        helper.assertTrue(inv.getStackInSlot(0).getCount() == 3, "The repair takes 2 iron");
        helper.assertTrue(inv.getStackInSlot(4).isEmpty() && inv.getStackInSlot(8).isEmpty(), "The cloth comes from both slots");

        ItemStack boots = new ItemStack(TGArmors.T1_COMBAT_BOOTS.get());
        boots.setDamageValue(boots.getMaxDamage() - 1);
        player.setItemSlot(EquipmentSlot.FEET, boots);
        inv.setStackInSlot(4, new ItemStack(TGItems.HEAVY_CLOTH.get(), 1));
        bench.pressButton(player, RepairBenchBlockEntity.BUTTON_REPAIR_ARMOR + 3);
        helper.assertFalse(player.getItemBySlot(EquipmentSlot.FEET).isDamaged(), "The boots button repairs the worn boots");
        helper.assertFalse(inv.isItemValid(RepairBenchBlockEntity.SLOT_REPAIR, new ItemStack(Items.IRON_INGOT)),
                "Only repairable items go into the repair slot");
        helper.succeed();
    }

    @GameTest(template = ARENA)
    public static void upgradeBenchEnchantsArmor(GameTestHelper helper) {
        var level = helper.getLevel();
        Holder<Enchantment> protection = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.PROTECTION);
        ItemStack chest = new ItemStack(TGArmors.T1_COMBAT_CHESTPLATE.get());
        ItemStack upgrade = new ItemStack(TGItems.UPGRADE_PROTECTION_2.get());
        List<UpgradeBenchRecipe> recipes = level.getRecipeManager().getAllRecipesFor(TGRecipes.UPGRADE_BENCH.get()).stream()
                .map(RecipeHolder::value).toList();
        helper.assertTrue(recipes.size() == 9, "There are 9 armor upgrades, found " + recipes.size());
        MachineInput input = MachineInput.of(chest, upgrade);
        UpgradeBenchRecipe recipe = recipes.stream().filter(r -> r.matches(input, level)).findFirst().orElseThrow();
        ItemStack result = recipe.assemble(input, level.registryAccess());
        helper.assertTrue(EnchantmentHelper.getTagEnchantmentLevel(protection, result) == 2, "Protection II upgrade gives protection 2");
        helper.assertTrue(recipe.levelsGained(chest, level.registryAccess()) == 2, "Two levels gained from none");

        chest.enchant(protection, 1);
        helper.assertTrue(recipe.levelsGained(chest, level.registryAccess()) == 1, "One level gained from protection 1");
        chest.enchant(protection, 3);
        helper.assertFalse(recipe.matches(MachineInput.of(chest, upgrade), level), "No upgrade to a lower level");
        helper.assertFalse(recipe.matches(MachineInput.of(new ItemStack(Items.IRON_CHESTPLATE), upgrade), level), "Only Techguns armor");
        helper.succeed();
    }

    @GameTest(template = ARENA)
    public static void camoBenchPicksAndCyclesLooks(GameTestHelper helper) {
        var level = helper.getLevel();
        helper.setBlock(POS, TGBlocks.CAMO_BENCH.get());
        CamoBenchBlockEntity bench = (CamoBenchBlockEntity) helper.getBlockEntity(POS);
        IItemHandlerModifiable inv = bench.getInventory();
        Player player = playerAtBench(helper);

        ItemStack concrete = new ItemStack(TGBlocks.CONCRETE_BROWN.get(), 5);
        helper.assertTrue(CamoBenchBlockEntity.variants(level, concrete).size() == TGBlocks.CONCRETE.size(), "Every concrete variant is a choice");
        helper.assertFalse(inv.isItemValid(CamoBenchBlockEntity.SLOT_INPUT, new ItemStack(Items.STONE)), "Stone has no other looks");
        inv.setStackInSlot(CamoBenchBlockEntity.SLOT_INPUT, concrete);
        bench.pressButton(player, CamoBenchBlockEntity.BUTTON_VARIANT + 2);
        ItemStack picked = inv.getStackInSlot(CamoBenchBlockEntity.SLOT_INPUT);
        helper.assertTrue(picked.is(TGBlocks.CONCRETE.get(2).get().asItem()) && picked.getCount() == 5, "Picking keeps the count, got " + picked);
        bench.pressButton(player, CamoBenchBlockEntity.BUTTON_PREVIOUS);
        helper.assertTrue(inv.getStackInSlot(CamoBenchBlockEntity.SLOT_INPUT).is(TGBlocks.CONCRETE.get(1).get().asItem()), "< goes back one");

        ItemStack wool = new ItemStack(Items.WHITE_WOOL);
        helper.assertTrue(CamoBenchBlockEntity.variants(level, wool).size() == 16, "Wool has 16 colours");

        ItemStack m4 = new ItemStack(TGGuns.M4.get());
        int camos = CamoData.camos(level.registryAccess(), m4).size();
        inv.setStackInSlot(CamoBenchBlockEntity.SLOT_INPUT, m4);
        bench.pressButton(player, CamoBenchBlockEntity.BUTTON_VARIANT + camos - 1);
        ItemStack gun = inv.getStackInSlot(CamoBenchBlockEntity.SLOT_INPUT);
        helper.assertTrue(gun.getItem() instanceof GunItem && CamoBenchBlockEntity.currentVariant(level, gun) == camos - 1,
                "The gun should get its last camo");

        player.setItemSlot(EquipmentSlot.HEAD, new ItemStack(TGArmors.T1_SCOUT_HELMET.get()));
        bench.pressButton(player, CamoBenchBlockEntity.BUTTON_ARMOR);
        helper.assertTrue(CamoBenchBlockEntity.currentVariant(level, player.getItemBySlot(EquipmentSlot.HEAD)) == 1, "+ cycles the worn helmet");
        helper.succeed();
    }

    @GameTest(template = ARENA)
    public static void ammoBenchOffersRounds(GameTestHelper helper) {
        var level = helper.getLevel();
        List<AmmoBenchRecipe> recipes = level.getRecipeManager().getAllRecipesFor(TGRecipes.AMMO_BENCH.get()).stream().map(RecipeHolder::value).toList();
        MachineInput input = MachineInput.of(new ItemStack(TGItems.LEAD_INGOT.get()), new ItemStack(Items.COPPER_INGOT), new ItemStack(Items.GUNPOWDER));
        List<AmmoBenchRecipe> matching = recipes.stream().filter(r -> r.matches(input, level)).toList();
        helper.assertTrue(matching.size() == 5, "Lead, copper and gunpowder make 5 kinds of rounds, got " + matching.size());
        helper.assertTrue(matching.stream().anyMatch(r -> r.result().is(TGAmmo.PISTOL_ROUNDS.get()) && r.result().getCount() == 12),
                "12 pistol rounds");
        MachineInput wrong = MachineInput.of(new ItemStack(Items.IRON_INGOT), new ItemStack(Items.COPPER_INGOT), new ItemStack(Items.GUNPOWDER));
        helper.assertTrue(recipes.stream().noneMatch(r -> r.matches(wrong, level)), "Iron is no bullet core");
        helper.succeed();
    }

    @GameTest(template = ARENA, timeoutTicks = 200)
    public static void grinderRecyclesArmor(GameTestHelper helper) {
        helper.setBlock(POS, TGBlocks.GRINDER.get());
        GrinderBlockEntity grinder = (GrinderBlockEntity) helper.getBlockEntity(POS);
        grinder.getEnergy().setEnergy(GrinderBlockEntity.ENERGY_CAPACITY);
        IItemHandlerModifiable inv = grinder.getInventory();
        inv.setStackInSlot(GrinderBlockEntity.SLOT_INPUT, new ItemStack(TGArmors.T1_COMBAT_CHESTPLATE.get()));

        helper.runAfterDelay(GrinderBlockEntity.TIME + 5, () -> {
            int iron = 0;
            int cloth = 0;
            for (int i = GrinderBlockEntity.SLOT_OUTPUT_FIRST; i <= GrinderBlockEntity.SLOT_OUTPUT_LAST; i++) {
                ItemStack stack = inv.getStackInSlot(i);
                iron += stack.is(Items.IRON_INGOT) ? stack.getCount() : 0;
                cloth += stack.is(TGItems.HEAVY_CLOTH.get()) ? stack.getCount() : 0;
            }
            helper.assertTrue(iron == 2 && cloth == 2, "A new T1 chestplate grinds into 2 iron and 2 cloth, got " + iron + "/" + cloth);
            helper.succeed();
        });
    }

    private static int countInInventory(Player player, net.minecraft.world.item.Item item) {
        return player.getInventory().items.stream().filter(stack -> stack.is(item)).mapToInt(ItemStack::getCount).sum();
    }

    @GameTest(template = ARENA)
    public static void ammoBenchMenuCraftsPickedRounds(GameTestHelper helper) {
        Player player = playerAtBench(helper);
        AmmoBenchMenu menu = new AmmoBenchMenu(1, player.getInventory(), ContainerLevelAccess.NULL);
        menu.getSlot(0).set(new ItemStack(TGItems.LEAD_INGOT.get(), 2));
        menu.getSlot(1).set(new ItemStack(Items.COPPER_INGOT, 2));
        menu.getSlot(2).set(new ItemStack(Items.GUNPOWDER, 2));
        List<ItemStack> choices = menu.choices();
        helper.assertTrue(choices.size() == 5, "Five kinds of rounds to pick, got " + choices.size());
        helper.assertTrue(menu.getSlot(AmmoBenchMenu.SLOT_RESULT).getItem().isEmpty(), "Nothing is made before a pick");

        int pistol = 0;
        while (!choices.get(pistol).is(TGAmmo.PISTOL_ROUNDS.get())) {
            pistol++;
        }
        menu.clickMenuButton(player, pistol);
        ItemStack result = menu.getSlot(AmmoBenchMenu.SLOT_RESULT).getItem();
        helper.assertTrue(result.is(TGAmmo.PISTOL_ROUNDS.get()) && result.getCount() == 12, "The pick makes 12 pistol rounds, got " + result);

        menu.quickMoveStack(player, AmmoBenchMenu.SLOT_RESULT);
        helper.assertTrue(countInInventory(player, TGAmmo.PISTOL_ROUNDS.get()) == 12, "Shift-click moves the rounds into the inventory");
        helper.assertTrue(menu.getSlot(0).getItem().getCount() == 1 && menu.getSlot(1).getItem().getCount() == 1
                && menu.getSlot(2).getItem().getCount() == 1, "Taking the result uses one of each input");
        helper.assertTrue(menu.getSlot(AmmoBenchMenu.SLOT_RESULT).getItem().is(TGAmmo.PISTOL_ROUNDS.get()), "The pick stays for the next batch");
        helper.succeed();
    }

    @GameTest(template = ARENA)
    public static void upgradeBenchMenuChargesXp(GameTestHelper helper) {
        helper.setBlock(POS, TGBlocks.UPGRADE_BENCH.get());
        UpgradeBenchBlockEntity bench = (UpgradeBenchBlockEntity) helper.getBlockEntity(POS);
        Player player = playerAtBench(helper);
        UpgradeBenchMenu menu = new UpgradeBenchMenu(1, player.getInventory(), bench);
        menu.getSlot(UpgradeBenchMenu.SLOT_ARMOR).set(new ItemStack(TGArmors.T1_COMBAT_CHESTPLATE.get()));
        menu.getSlot(UpgradeBenchMenu.SLOT_UPGRADE).set(new ItemStack(TGItems.UPGRADE_PROTECTION_2.get(), 2));
        int cost = 2 * TGConfig.UPGRADE_XP_COST.get();
        helper.assertTrue(menu.xpCost() == cost, "Two levels cost " + cost + " XP, got " + menu.xpCost());

        player.giveExperiencePoints(cost - 1);
        menu.quickMoveStack(player, UpgradeBenchMenu.SLOT_RESULT);
        helper.assertTrue(countInInventory(player, TGArmors.T1_COMBAT_CHESTPLATE.get()) == 0, "Too little XP: the result stays");

        player.giveExperiencePoints(11);
        menu.quickMoveStack(player, UpgradeBenchMenu.SLOT_RESULT);
        helper.assertTrue(countInInventory(player, TGArmors.T1_COMBAT_CHESTPLATE.get()) == 1, "The upgraded chestplate goes to the inventory");
        helper.assertTrue(player.totalExperience == 10, "The upgrade takes its XP, left " + player.totalExperience);
        helper.assertTrue(menu.getSlot(UpgradeBenchMenu.SLOT_ARMOR).getItem().isEmpty(), "The input armor is used up");
        helper.assertTrue(menu.getSlot(UpgradeBenchMenu.SLOT_UPGRADE).getItem().getCount() == 1, "One upgrade item is used");
        helper.succeed();
    }

    @GameTest(template = ARENA)
    public static void benchSlotsShiftClickToInventory(GameTestHelper helper) {
        helper.setBlock(POS, TGBlocks.REPAIR_BENCH.get());
        RepairBenchBlockEntity bench = (RepairBenchBlockEntity) helper.getBlockEntity(POS);
        Player player = playerAtBench(helper);
        RepairBenchMenu menu = new RepairBenchMenu(1, player.getInventory(), bench);
        bench.getInventory().setStackInSlot(RepairBenchBlockEntity.SLOT_REPAIR, new ItemStack(TGArmors.T1_COMBAT_CHESTPLATE.get()));
        bench.getInventory().setStackInSlot(0, new ItemStack(Items.IRON_INGOT, 3));

        menu.quickMoveStack(player, RepairBenchBlockEntity.SLOT_REPAIR);
        menu.quickMoveStack(player, 0);
        helper.assertTrue(countInInventory(player, TGArmors.T1_COMBAT_CHESTPLATE.get()) == 1, "The repair slot shift-clicks out");
        helper.assertTrue(countInInventory(player, Items.IRON_INGOT) == 3, "Materials shift-click out");
        helper.assertTrue(player.getItemBySlot(EquipmentSlot.OFFHAND).isEmpty() && player.getItemBySlot(EquipmentSlot.CHEST).isEmpty(),
                "Nothing goes into the equipment slots");

        int ironSlot = 10;
        while (!menu.getSlot(ironSlot).getItem().is(Items.IRON_INGOT)) {
            ironSlot++;
        }
        menu.quickMoveStack(player, ironSlot);
        helper.assertTrue(bench.getInventory().getStackInSlot(0).getCount() == 3, "Materials shift-click back into the material slots");
        helper.succeed();
    }
}

