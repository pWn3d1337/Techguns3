package techguns.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import techguns.TGConfig;
import techguns.Techguns;
import techguns.damage.TGDamageEvents;
import techguns.entity.npc.TurretEntity;
import techguns.gun.GunItem;
import techguns.player.TGPlayerData;
import techguns.registry.TGEntities;
import techguns.registry.TGSlotItems;
import techguns.slot.item.JetpackItem;

/**
 * Techguns 2 rules found missing in the code review: who damages blocks (safe mode), the damage factors by attacker and
 * the jetpack toggle.
 */
@GameTestHolder(Techguns.MODID)
@PrefixGameTestTemplate(false)
public class TGDamageRulesTests {
    private static final String ARENA = "empty_8x4x8";

    @GameTest(template = ARENA)
    public static void onlyPlayersWithoutSafeModeDamageBlocks(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        TGPlayerData data = TGPlayerData.of(player);
        helper.assertTrue(TGConfig.ALLOW_UNSAFE_MODE.get(), "Unsafe mode should be allowed by default");
        helper.assertTrue(!data.get(TGPlayerData.Toggle.SAFE_MODE) && GunItem.blockDamage(player),
                "A player's guns damage blocks by default (safe mode off, Techguns 2)");
        data.set(TGPlayerData.Toggle.SAFE_MODE, true);
        helper.assertTrue(!GunItem.blockDamage(player), "Safe mode should stop block damage");
        data.set(TGPlayerData.Toggle.SAFE_MODE, false);

        Zombie npc = TGTestTargets.husk(helper, new BlockPos(4, 1, 4));
        helper.assertTrue(!GunItem.blockDamage(npc), "NPC shots never damage blocks");
        TurretEntity turret = TGEntities.TURRET.get().create(helper.getLevel());
        helper.assertTrue(turret != null && !GunItem.blockDamage(turret), "Turret shots never damage blocks");
        helper.succeed();
    }

    @GameTest(template = ARENA)
    public static void damageFactorsFollowTheAttacker(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        Zombie npc = TGTestTargets.husk(helper, new BlockPos(4, 1, 4));
        TurretEntity turret = TGEntities.TURRET.get().create(helper.getLevel());
        float npcFactor = TGConfig.DAMAGE_FACTOR_NPC.get().floatValue();

        helper.assertTrue(TGDamageEvents.damageFactor(null, player) == 1.0f, "Damage without an attacker (radiation, acid) is not scaled");
        helper.assertTrue(TGDamageEvents.damageFactor(player, npc) == 1.0f, "Players hit NPCs at full damage");
        helper.assertTrue(TGDamageEvents.damageFactor(npc, player) == npcFactor, "NPCs use the NPC factor");
        helper.assertTrue(TGDamageEvents.damageFactor(turret, player) == TGConfig.DAMAGE_TURRET_TO_PLAYER.get().floatValue(),
                "Turrets hitting players use the turret factor");
        helper.assertTrue(TGDamageEvents.damageFactor(turret, npc) == npcFactor, "Turrets hitting mobs use the NPC factor");
        // the defaults, not the loaded values: an existing config file keeps what it has
        helper.assertTrue(TGConfig.DAMAGE_PVP.getDefault() == 0.5 && TGConfig.DAMAGE_TURRET_TO_PLAYER.getDefault() == 0.5, "Techguns 2 defaults");
        helper.succeed();
    }

    @GameTest(template = ARENA)
    public static void jetpackBoostNeedsTheToggle(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack jetpack = new ItemStack(TGSlotItems.JETPACK.get());
        JetpackItem item = (JetpackItem) jetpack.getItem();
        TGPlayerData data = TGPlayerData.of(player);
        data.setJetpackBoost(true);

        data.set(TGPlayerData.Toggle.JETPACK, false);
        player.setDeltaMovement(Vec3.ZERO);
        item.slotTick(player, jetpack);
        helper.assertTrue(player.getDeltaMovement().y <= 0.0, "No thrust with the jetpack switched off");

        data.set(TGPlayerData.Toggle.JETPACK, true);
        player.setDeltaMovement(Vec3.ZERO);
        item.slotTick(player, jetpack);
        helper.assertTrue(player.getDeltaMovement().y > 0.0, "Thrust with the jetpack on and the boost held");
        data.setJetpackBoost(false);
        helper.succeed();
    }
}
