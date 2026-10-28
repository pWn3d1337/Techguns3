package techguns.gametest;

import java.util.List;
import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import techguns.Techguns;
import techguns.compat.team.TGTeams;
import techguns.entity.npc.TurretEntity;
import techguns.gun.GunData;
import techguns.machine.TurretBlockEntity;
import techguns.registry.TGAmmo;
import techguns.registry.TGBlocks;
import techguns.registry.TGEntities;
import techguns.registry.TGGuns;
import techguns.registry.TGItems;

/** Turrets (Phase 5d2): the turret on its base, shooting monsters with the base's ammo, power, rebuilding, targeting rules. */
@GameTestHolder(Techguns.MODID)
@PrefixGameTestTemplate(false)
public class TGTurretTests {
    private static final String ARENA = "empty_8x10x8";
    private static final BlockPos BASE = new BlockPos(1, 1, 3);

    private static TurretBlockEntity base(GameTestHelper helper, boolean powered) {
        helper.setBlock(BASE, TGBlocks.TURRET.get());
        TurretBlockEntity base = (TurretBlockEntity) helper.getBlockEntity(BASE);
        if (powered) {
            base.getEnergy().setEnergy(TurretBlockEntity.ENERGY_CAPACITY);
        }
        return base;
    }

    private static List<TurretEntity> turrets(GameTestHelper helper) {
        BlockPos pos = helper.absolutePos(BASE);
        return helper.getLevel().getEntitiesOfClass(TurretEntity.class, new AABB(pos).inflate(2.0));
    }

    private static void armWithAk(TurretBlockEntity base) {
        ItemStack gun = new ItemStack(TGGuns.AK47.get());
        GunData.setAmmo(gun, techguns.gun.GunAmmo.EMPTY);
        base.getInventory().setStackInSlot(TurretBlockEntity.SLOT_WEAPON, gun);
        base.getInventory().setStackInSlot(TurretBlockEntity.SLOT_INPUT_FIRST, new ItemStack(TGAmmo.ASSAULT_RIFLE_MAGAZINE.get(), 2));
    }

    @GameTest(template = ARENA)
    public static void turretSpawnsOnBase(GameTestHelper helper) {
        TurretBlockEntity base = base(helper, false);
        helper.runAfterDelay(2, () -> {
            List<TurretEntity> turrets = turrets(helper);
            helper.assertTrue(turrets.size() == 1, "The base builds one turret, found " + turrets.size());
            TurretEntity turret = turrets.getFirst();
            helper.assertTrue(base.isMounted(turret) && turret.getBasePos().equals(helper.absolutePos(BASE)), "The turret belongs to the base");
            helper.assertTrue(turret.blockPosition().equals(helper.absolutePos(BASE.above())), "It stands on top of the base");
            helper.assertTrue(turret.getHealth() == (float) TurretEntity.MAX_HEALTH, "Full health");
            helper.setBlock(BASE, Blocks.AIR);
            helper.assertTrue(turrets(helper).isEmpty(), "Breaking the base removes the turret");
            helper.succeed();
        });
    }

    @GameTest(template = ARENA, timeoutTicks = 200)
    public static void turretShootsMonsters(GameTestHelper helper) {
        TurretBlockEntity base = base(helper, true);
        armWithAk(base);
        Husk husk = helper.spawnWithNoFreeWill(EntityType.HUSK, new BlockPos(6, 2, 3));
        husk.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(500.0);
        husk.setHealth(500.0f);
        helper.runAfterDelay(150, () -> {
            helper.assertTrue(husk.getHealth() < 500.0f, "The turret shoots the husk, health " + husk.getHealth());
            ItemStack gun = base.getInventory().getStackInSlot(TurretBlockEntity.SLOT_WEAPON);
            helper.assertTrue(GunData.loaded(gun) < 30, "Shots use the gun's ammo");
            helper.assertTrue(base.getInventory().getStackInSlot(TurretBlockEntity.SLOT_INPUT_FIRST).getCount() == 1,
                    "The empty gun was loaded with a magazine from the ammo slots");
            helper.assertTrue(base.getInventory().getStackInSlot(TurretBlockEntity.SLOT_OUTPUT_FIRST).is(TGAmmo.ASSAULT_RIFLE_MAGAZINE_EMPTY.get()),
                    "and put the empty magazine into the output slots");
            helper.assertTrue(base.getEnergy().getEnergyStored() < TurretBlockEntity.ENERGY_CAPACITY, "The turret uses power");
            helper.succeed();
        });
    }

    @GameTest(template = ARENA, timeoutTicks = 200)
    public static void unpoweredTurretDoesNothing(GameTestHelper helper) {
        TurretBlockEntity base = base(helper, false);
        armWithAk(base);
        Husk husk = helper.spawnWithNoFreeWill(EntityType.HUSK, new BlockPos(6, 2, 3));
        helper.runAfterDelay(100, () -> {
            helper.assertTrue(husk.getHealth() == husk.getMaxHealth(), "Without power the turret doesn't shoot");
            helper.assertTrue(turrets(helper).getFirst().isNoAi(), "and is switched off");
            helper.succeed();
        });
    }

    @GameTest(template = ARENA, timeoutTicks = 300)
    public static void deadTurretIsRebuilt(GameTestHelper helper) {
        TurretBlockEntity base = base(helper, true);
        // rebuilding needs 10000 FE, twice the buffer: keep it charged like a generator would
        helper.onEachTick(() -> base.getEnergy().setEnergy(TurretBlockEntity.ENERGY_CAPACITY));
        helper.runAfterDelay(2, () -> turrets(helper).getFirst().kill());
        helper.runAfterDelay(5, () -> helper.assertTrue(base.isDead(), "The base notices its turret's death"));
        helper.runAfterDelay(TurretBlockEntity.REPAIR_TIME + 10, () -> {
            helper.assertFalse(base.isDead(), "The base rebuilt the turret");
            List<TurretEntity> turrets = turrets(helper);
            helper.assertTrue(turrets.size() == 1 && turrets.getFirst().getHealth() <= TurretBlockEntity.REPAIRED_HEALTH + 1.0f,
                    "with 10 health");
            helper.succeed();
        });
    }

    @GameTest(template = ARENA)
    public static void turretSlotsAndArmor(GameTestHelper helper) {
        TurretBlockEntity base = base(helper, false);
        var inv = base.getInventory();
        helper.assertTrue(inv.isItemValid(TurretBlockEntity.SLOT_WEAPON, new ItemStack(TGGuns.M4.get())), "Guns go into the gun slot");
        helper.assertFalse(inv.isItemValid(TurretBlockEntity.SLOT_WEAPON, new ItemStack(TGItems.STEEL_INGOT.get())), "Other items don't");
        helper.assertFalse(inv.isItemValid(TurretBlockEntity.SLOT_ARMOR, new ItemStack(TGItems.STEEL_PLATE.get())), "Plates aren't turret armor");
        inv.setStackInSlot(TurretBlockEntity.SLOT_ARMOR, new ItemStack(TGItems.TURRET_ARMOR_OBSIDIAN_STEEL.get()));
        helper.assertTrue(base.getTier() == 3 && base.armorValue() == 15, "Obsidian steel armor: tier 3, 15 armor");
        helper.runAfterDelay(2, () -> {
            helper.assertTrue(turrets(helper).getFirst().getArmorValue() == 15, "The turret has the plate's armor");
            helper.succeed();
        });
    }

    @GameTest(template = ARENA)
    public static void turretPvpRules(GameTestHelper helper) {
        UUID owner = UUID.randomUUID();
        UUID other = UUID.randomUUID();
        helper.assertFalse(TGTeams.shouldAttack(owner, other, TGTeams.PVP_NONE), "No PvP: other players are safe");
        helper.assertTrue(TGTeams.shouldAttack(owner, other, TGTeams.PVP_EVERYONE), "Everyone: other players are attacked");
        helper.assertFalse(TGTeams.shouldAttack(owner, owner, TGTeams.PVP_EVERYONE), "never the owner");
        helper.assertTrue(TGTeams.hasTeams() || TGTeams.nextPvpSetting(TGTeams.PVP_NONE) == TGTeams.PVP_EVERYONE
                && TGTeams.nextPvpSetting(TGTeams.PVP_EVERYONE) == TGTeams.PVP_NONE, "Without teams the button toggles none / everyone");
        helper.assertTrue(TGTeams.isHostile(owner, other) && !TGTeams.isHostile(owner, owner), "Turrets fight back against others only");
        helper.assertTrue(TGEntities.TURRET.get().getDimensions().height() == 0.9f, "Turret is 0.9 high");
        helper.succeed();
    }
}
