package techguns.gametest;

import java.util.List;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import techguns.Techguns;
import techguns.gun.AmmoInventory;
import techguns.gun.GunAmmo;
import techguns.gun.GunData;
import techguns.gun.GunItem;
import techguns.gun.ammo.AmmoType;
import techguns.item.AmmoBagData;
import techguns.registry.TGAmmo;
import techguns.registry.TGGuns;
import techguns.registry.TGItems;

/**
 * Ammo Bag (Phase 3 batch 3g): capacity in rounds weighted by stack size, only ammo, reloads and returned ammo go
 * through bags in the inventory.
 */
@GameTestHolder(Techguns.MODID)
@PrefixGameTestTemplate(false)
public class TGAmmoBagTests {
    private static final String ARENA = "empty_8x4x8";

    @GameTest(template = ARENA)
    public static void bagHoldsAmmoUpToCapacity(GameTestHelper helper) {
        ItemStack bag = new ItemStack(TGItems.AMMO_BOX.get());
        ItemStack rounds = new ItemStack(TGAmmo.PISTOL_ROUNDS.get(), 64);
        ItemStack dirt = new ItemStack(Items.DIRT, 10);

        int inserted = 0;
        for (int i = 0; i < 5; i++) {
            rounds.setCount(64);
            inserted += AmmoBagData.insert(bag, rounds);
        }
        int dirtInserted = AmmoBagData.insert(bag, dirt);

        helper.assertTrue(inserted == 256, "The small box should hold 256 rounds, took " + inserted);
        helper.assertTrue(AmmoBagData.remainingCapacity(bag) == 0, "The box should be full");
        helper.assertTrue(dirtInserted == 0 && dirt.getCount() == 10, "A bag should not take non-ammo items");
        helper.succeed();
    }

    @GameTest(template = ARENA)
    public static void magazinesWeighByStackSize(GameTestHelper helper) {
        ItemStack bag = new ItemStack(TGItems.AMMO_BOX.get());
        ItemStack magazines = new ItemStack(TGAmmo.PISTOL_MAGAZINE.get(), 16);

        AmmoBagData.insert(bag, magazines);

        // stack size 16: each magazine weighs 4 of the 256
        helper.assertTrue(AmmoBagData.occupancy(bag) == 64, "16 magazines should occupy 64, was " + AmmoBagData.occupancy(bag));
        helper.succeed();
    }

    @GameTest(template = ARENA)
    public static void reloadUsesAmmoInBag(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack bag = new ItemStack(TGItems.AMMO_BOX.get());
        AmmoBagData.insert(bag, new ItemStack(TGAmmo.PISTOL_MAGAZINE.get(), 2));
        player.getInventory().setItem(9, bag);
        player.getInventory().add(new ItemStack(TGAmmo.PISTOL_MAGAZINE.get(), 1));
        List<ItemStack> twoMagazines = List.of(new ItemStack(TGAmmo.PISTOL_MAGAZINE.get(), 2));

        helper.assertTrue(AmmoInventory.count(player, new ItemStack(TGAmmo.PISTOL_MAGAZINE.get())) == 3, "Loose and bagged magazines should count");
        helper.assertTrue(AmmoInventory.consume(player, twoMagazines), "Two magazines should be available");
        helper.assertTrue(AmmoBagData.count(bag, new ItemStack(TGAmmo.PISTOL_MAGAZINE.get())) == 1,
                "The loose magazine should be used first, then one from the bag");
        helper.assertFalse(AmmoInventory.consume(player, twoMagazines), "Only one magazine is left");
        helper.assertTrue(AmmoBagData.count(bag, new ItemStack(TGAmmo.PISTOL_MAGAZINE.get())) == 1, "A failed consume should take nothing");
        helper.succeed();
    }

    @GameTest(template = ARENA)
    public static void gunReloadsFromBagOnly(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack bag = new ItemStack(TGItems.AMMO_BOX.get());
        AmmoBagData.insert(bag, new ItemStack(TGAmmo.PISTOL_MAGAZINE.get(), 1));
        GunItem pistol = (GunItem) TGGuns.PISTOL.get();
        ItemStack gun = new ItemStack(pistol);
        GunData.setAmmo(gun, new GunAmmo(0, AmmoType.DEFAULT));
        player.setItemInHand(InteractionHand.MAIN_HAND, gun);
        player.getInventory().setItem(9, bag);

        pistol.shoot(gun, helper.getLevel(), player, InteractionHand.MAIN_HAND, false);

        helper.assertTrue(GunData.loaded(gun) > 0, "The pistol should reload from the bag, loaded=" + GunData.loaded(gun));
        helper.assertTrue(AmmoBagData.count(bag, new ItemStack(TGAmmo.PISTOL_MAGAZINE.get())) == 0, "The bagged magazine should be used");
        helper.assertTrue(AmmoBagData.count(bag, new ItemStack(TGAmmo.PISTOL_MAGAZINE_EMPTY.get())) == 1,
                "The empty magazine should go back into the bag");
        helper.succeed();
    }
}
