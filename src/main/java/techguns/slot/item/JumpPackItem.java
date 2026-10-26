package techguns.slot.item;

import java.util.Map;

import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import techguns.player.TGPlayerData;
import techguns.registry.TGAmmo;
import techguns.registry.TGAttributes;
import techguns.registry.TGSounds;
import techguns.slot.TGSlotItem;
import techguns.slot.TGSlotType;

/** Jump pack (Techguns 2 {@code ItemJumpPack}): higher jumps and softer falls for 1 charge each, while the jetpack toggle is on. */
public class JumpPackItem extends TGSlotItem {
    public JumpPackItem(Properties properties, int durability) {
        super(properties, TGSlotType.BACK, durability);
        bonus(TGAttributes.JUMP_BONUS, 0.8);
        bonus(TGAttributes.FALL_FREE_HEIGHT, 8.0);
        bonus(TGAttributes.FALL_DAMAGE_REDUCTION, 0.2);
        refill(TGAmmo.COMPRESSED_AIR_TANK, TGAmmo.COMPRESSED_AIR_TANK_EMPTY);
    }

    @Override
    public Map<Holder<Attribute>, Double> activeBonuses(Player player, ItemStack stack) {
        return TGPlayerData.of(player).get(TGPlayerData.Toggle.JETPACK) ? super.activeBonuses(player, stack) : Map.of();
    }

    @Override
    public void slotTick(Player player, ItemStack stack) {
        SlotItemSupport.recharge(this, player, stack);
    }

    @Override
    public void onBonusAction(Player player, ItemStack stack, Holder<Attribute> bonus) {
        boolean jump = bonus.value() == TGAttributes.JUMP_BONUS.value();
        if (!jump && bonus.value() != TGAttributes.FALL_DAMAGE_REDUCTION.value()) {
            return;
        }
        useCharge(player, stack, 1);
        if (jump) {
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(), TGSounds.STEAM_JUMP_2.get(),
                    SoundSource.PLAYERS, 1.0f, 1.0f);
        }
        SlotItemSupport.sync(player);
    }
}
