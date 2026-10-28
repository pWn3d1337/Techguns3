package techguns.client.gun;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import techguns.client.particle.ClientFx;
import techguns.client.particle.EntityConditions;
import techguns.core.fx.EntityCondition;
import techguns.gun.ChargeGunItem;

/**
 * Charge sound and charge FX of players charging a {@link ChargeGunItem} (Techguns 2 played them from the server with
 * the {@code CHARGING_WEAPON} condition). The using-item state is synced to every client, so each client starts them
 * itself when a player starts charging; both stop when the charge ends.
 */
public final class ChargeEffects {
    /** Players charging in the previous tick, by entity id. */
    private static final Set<Integer> CHARGING = new HashSet<>();

    private ChargeEffects() {
    }

    public static void init(IEventBus gameBus) {
        gameBus.addListener(ChargeEffects::onClientTick);
    }

    private static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            CHARGING.clear();
            return;
        }
        Set<Integer> now = new HashSet<>();
        for (Player player : mc.level.players()) {
            if (player.isUsingItem() && player.getUseItem().getItem() instanceof ChargeGunItem gun) {
                now.add(player.getId());
                if (!CHARGING.contains(player.getId())) {
                    startCharge(mc, player, gun);
                }
            }
        }
        CHARGING.clear();
        CHARGING.addAll(now);
    }

    private static void startCharge(Minecraft mc, Player player, ChargeGunItem gun) {
        if (gun.chargeSound() != null) {
            mc.getSoundManager().play(new ChargeSound(player, gun.chargeSound().get()));
        }
        if (gun.chargeFx() != null) {
            float[] offset = gun.chargeFxOffset();
            float x = player.getMainArm() == HumanoidArm.RIGHT ? offset[0] : -offset[0];
            ClientFx.createFXOnEntity(gun.chargeFx(), player, x, offset[1], offset[2], true, EntityCondition.CHARGING_WEAPON);
        }
    }

    /** Follows the charging player and stops when the charge ends. */
    private static final class ChargeSound extends AbstractTickableSoundInstance {
        private final LivingEntity entity;

        ChargeSound(LivingEntity entity, SoundEvent sound) {
            super(sound, SoundSource.PLAYERS, SoundInstance.createUnseededRandom());
            this.entity = entity;
            this.attenuation = Attenuation.LINEAR;
            this.x = entity.getX();
            this.y = entity.getEyeY();
            this.z = entity.getZ();
        }

        @Override
        public void tick() {
            if (!EntityConditions.holds(EntityCondition.CHARGING_WEAPON, entity)) {
                stop();
                return;
            }
            x = entity.getX();
            y = entity.getEyeY();
            z = entity.getZ();
        }
    }
}
