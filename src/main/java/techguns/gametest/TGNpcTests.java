package techguns.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootTable;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import techguns.Techguns;
import techguns.entity.npc.TGNpc;
import techguns.entity.npc.TGNpcFaction;
import techguns.gun.GunItem;
import techguns.registry.TGGuns;
import techguns.registry.TGNpcs;

/** NPCs (Phase 6a): spawn loadouts, armor stat, gun and melee attacks, factions, loot tables. */
@GameTestHolder(Techguns.MODID)
@PrefixGameTestTemplate(false)
public class TGNpcTests {
    private static final String ARENA = "empty_8x10x8";

    private static TGNpc spawn(GameTestHelper helper, TGNpcs.Npc npc, BlockPos pos) {
        return npc.type().get().spawn(helper.getLevel(), helper.absolutePos(pos), MobSpawnType.SPAWN_EGG);
    }

    @GameTest(template = ARENA)
    public static void npcsSpawnWithLoadouts(GameTestHelper helper) {
        for (TGNpcs.Npc type : TGNpcs.npcs()) {
            TGNpc npc = spawn(helper, type, new BlockPos(3, 1, 3));
            helper.assertTrue(npc != null && !npc.getMainHandItem().isEmpty(), type.name() + " spawns with a weapon");
            helper.assertFalse(npc.canPickUpLoot(), type.name() + " doesn't pick up loot");
            npc.discard();
        }
        TGNpc policeman = spawn(helper, TGNpcs.ZOMBIE_POLICEMAN, new BlockPos(3, 1, 3));
        helper.assertTrue(policeman.isHoldingGun(), "Policemen always carry a revolver or pistol");
        helper.assertTrue(policeman.getArmorValue() == 5, "Techguns 2 armor stat 5, not the worn armor");
        TGNpc miner = spawn(helper, TGNpcs.ZOMBIE_MINER, new BlockPos(3, 1, 3));
        helper.assertTrue(!miner.getItemBySlot(EquipmentSlot.HEAD).isEmpty(), "Miners always wear the miner helmet");
        helper.assertTrue(miner.getAttributeValue(Attributes.MAX_HEALTH) == 20.0, "Zombie miner: 20 health");
        helper.assertTrue(TGNpcs.ZOMBIE_SOLDIER.type().get().is(EntityTypeTags.UNDEAD), "Zombie soldiers are undead");
        helper.assertTrue(TGNpcs.ZOMBIE_PIGMAN_SOLDIER.type().get().fireImmune(), "Pigman soldiers are fire immune");
        helper.succeed();
    }

    @GameTest(template = ARENA)
    public static void humanNpcsGear(GameTestHelper helper) {
        TGNpc outcast = spawn(helper, TGNpcs.OUTCAST, new BlockPos(3, 1, 3));
        helper.assertTrue(outcast.getItemBySlot(EquipmentSlot.CHEST).is(techguns.registry.TGArmors.T3_POWER_CHESTPLATE.get()),
                "Outcasts wear power armor");
        TGNpc commando = spawn(helper, TGNpcs.COMMANDO, new BlockPos(3, 1, 3));
        helper.assertTrue(commando.getMainHandItem().is(TGGuns.M4_INFILTRATOR.get()) && commando.getArmorValue() == 8,
                "Commandos: M4 Infiltrator, armor stat 8");
        TGNpc dave = spawn(helper, TGNpcs.DICTATOR_DAVE, new BlockPos(3, 1, 3));
        helper.assertTrue(dave.getMainHandItem().is(TGGuns.GOLDEN_REVOLVER.get()) && dave.getAttributeValue(Attributes.MAX_HEALTH) == 100.0
                && dave.getAttributeValue(Attributes.ARMOR_TOUGHNESS) == 2.0, "Dictator Dave: golden revolver, 100 health, toughness 2");
        // worn armor attributes apply on the entity's next tick
        helper.runAfterDelay(2, () -> {
            helper.assertTrue(outcast.getArmorValue() > 10, "Outcasts' worn armor protects them (gear armor), got " + outcast.getArmorValue());
            helper.succeed();
        });
    }

    @GameTest(template = ARENA, timeoutTicks = 200)
    public static void npcShootsItsTarget(GameTestHelper helper) {
        TGNpc npc = spawn(helper, TGNpcs.ZOMBIE_POLICEMAN, new BlockPos(1, 1, 3));
        npc.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(TGGuns.REVOLVER.get()));
        Husk husk = helper.spawnWithNoFreeWill(EntityType.HUSK, new BlockPos(6, 1, 3));
        husk.getAttribute(Attributes.MAX_HEALTH).setBaseValue(500.0);
        husk.setHealth(500.0f);
        npc.setTarget(husk);
        helper.runAfterDelay(150, () -> {
            helper.assertTrue(husk.getHealth() < 500.0f, "The NPC shoots its target, health " + husk.getHealth());
            helper.succeed();
        });
    }

    @GameTest(template = ARENA)
    public static void npcCombatFollowsWeapon(GameTestHelper helper) {
        TGNpc npc = spawn(helper, TGNpcs.ZOMBIE_FARMER, new BlockPos(3, 1, 3));
        npc.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(net.minecraft.world.item.Items.IRON_HOE));
        helper.assertFalse(npc.isHoldingGun(), "A hoe is a melee weapon");
        npc.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(TGGuns.HAND_CANNON.get()));
        helper.assertTrue(npc.isHoldingGun() && npc.getMainHandItem().getItem() instanceof GunItem, "A hand cannon is a gun");
        helper.succeed();
    }

    @GameTest(template = ARENA)
    public static void npcFactionsAndLoot(GameTestHelper helper) {
        helper.assertFalse(TGNpcFaction.HOSTILE.isHostileTo(TGNpcFaction.HOSTILE), "Hostile NPCs don't fight each other");
        helper.assertTrue(TGNpcFaction.HOSTILE.isHostileTo(TGNpcFaction.TURRET) && TGNpcFaction.TURRET.isHostileTo(TGNpcFaction.HOSTILE),
                "Hostiles and turrets fight");
        helper.assertFalse(TGNpcFaction.TURRET.isHostileTo(TGNpcFaction.NEUTRAL), "Turrets leave neutrals alone");
        for (TGNpcs.Npc type : TGNpcs.npcs()) {
            LootTable table = helper.getLevel().getServer().reloadableRegistries().getLootTable(type.type().get().getDefaultLootTable());
            helper.assertTrue(table != LootTable.EMPTY, type.name() + " has a loot table");
        }
        helper.succeed();
    }
}
