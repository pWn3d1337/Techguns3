package techguns.armor;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.living.LivingShieldBlockEvent;
import techguns.core.damage.TGShieldFormula;
import techguns.damage.TGDamage;
import techguns.damage.TGDamageSource;

/**
 * Shields against Techguns damage (Techguns 2 {@code DamageSystem.calculateShieldDamage}): a block lets part of the
 * hit through by the shield's {@link ShieldStats}, and plays the shield material's impact sound. Vanilla damage is
 * blocked as vanilla does.
 */
public final class ShieldEvents {
    private ShieldEvents() {
    }

    public static void init(IEventBus gameBus) {
        gameBus.addListener(ShieldEvents::onShieldBlock);
    }

    private static void onShieldBlock(LivingShieldBlockEvent event) {
        if (!event.getBlocked() || !(event.getDamageSource() instanceof TGDamageSource source)) {
            return;
        }
        LivingEntity blocker = event.getEntity();
        ItemStack shield = blocker.getUseItem();
        if (shield.isEmpty()) {
            return;
        }
        ShieldStats stats = ShieldStats.of(shield.getItem());
        float damage = event.getOriginalBlockedDamage();
        float through = TGShieldFormula.damageThrough(damage, stats.rate(TGDamage.categoryOf(source)), stats.damageThreshold(),
                source.getArmorPenetration());
        event.setBlockedDamage(damage - through);
        SoundEvent sound = stats.material().sound();
        if (sound != null && !blocker.level().isClientSide) {
            blocker.level().playSound(null, blocker.getX(), blocker.getY(), blocker.getZ(), sound, SoundSource.PLAYERS, 1.0f, 1.0f);
        }
    }
}
