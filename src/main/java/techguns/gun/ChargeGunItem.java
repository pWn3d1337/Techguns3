package techguns.gun;

import java.util.function.Supplier;

import javax.annotation.Nullable;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import techguns.network.TGNetwork;

/**
 * Charge gun (Techguns 2 / Fabric {@code GenericGunCharge}): left click fires a normal shot; holding the use key
 * charges, and releasing fires a charged shot that scales with the charge and uses up to
 * {@link #ammoPerFullCharge} rounds. The charge sound and FX run on the client while the holder uses the gun
 * (see {@code ChargeEffects}).
 */
public class ChargeGunItem extends GunItem {
    private static final int MAX_USE_TICKS = 288000;

    /** Ticks for a full charge. */
    private final float fullChargeTicks;
    /** Rounds a full charge uses. */
    private final int ammoPerFullCharge;
    @Nullable
    private Supplier<SoundEvent> chargeSound;
    @Nullable
    private String chargeFx;
    private float chargeFxX;
    private float chargeFxY;
    private float chargeFxZ;

    public ChargeGunItem(Properties properties, GunStats stats, GunSounds sounds, ProjectileSelector projectiles, float fullChargeTicks,
                         int ammoPerFullCharge) {
        super(properties, stats, sounds, projectiles);
        this.fullChargeTicks = fullChargeTicks;
        this.ammoPerFullCharge = ammoPerFullCharge;
    }

    public ChargeGunItem withChargeSound(Supplier<SoundEvent> sound) {
        this.chargeSound = sound;
        return this;
    }

    /** Particle system on the holder while charging; the x offset is mirrored for the left hand. */
    public ChargeGunItem withChargeFx(String name, float x, float y, float z) {
        this.chargeFx = name;
        this.chargeFxX = x;
        this.chargeFxY = y;
        this.chargeFxZ = z;
        return this;
    }

    @Nullable
    public Supplier<SoundEvent> chargeSound() {
        return chargeSound;
    }

    @Nullable
    public String chargeFx() {
        return chargeFx;
    }

    public float[] chargeFxOffset() {
        return new float[] {chargeFxX, chargeFxY, chargeFxZ};
    }

    public float fullChargeTicks() {
        return fullChargeTicks;
    }

    /** Charge 0..1 of an entity using this gun. */
    public float charge(LivingEntity entity) {
        return Math.min(1.0f, chargeFull(entity));
    }

    /** Charge of an entity using this gun, continuing past 1.0 (charge FX pulse while the charge is held). */
    public float chargeFull(LivingEntity entity) {
        int used = getUseDuration(entity.getUseItem(), entity) - entity.getUseItemRemainingTicks();
        return used / fullChargeTicks;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        ShooterState state = ShooterState.of(player);
        if (GunData.loaded(stack) <= 0) {
            startReload(stack, level, player, hand, false);
            return InteractionResultHolder.pass(stack);
        }
        if (state.fireDelay(hand) > 0) {
            return InteractionResultHolder.pass(stack);
        }
        state.setFireDelay(hand, stats().firing().fireDelay());
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    /** A normal shot is not possible while charging (Techguns 2 {@code canFireWhileCharging} is false for all charge guns). */
    @Override
    public void shoot(ItemStack stack, Level level, Player player, InteractionHand hand, boolean zooming, @Nullable Entity target) {
        if (player.isUsingItem() && player.getUseItem() == stack) {
            return;
        }
        super.shoot(stack, level, player, hand, zooming, target);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return MAX_USE_TICKS;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.NONE;
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (!(entity instanceof Player player)) {
            return;
        }
        float charge = Math.min(1.0f, (getUseDuration(stack, entity) - timeLeft) / fullChargeTicks);
        int wanted = Math.max(1, (int) Math.ceil(charge * ammoPerFullCharge));
        int ammo = player.getAbilities().instabuild ? wanted : GunData.consume(stack, wanted);
        if (ammo <= 0) {
            return;
        }
        if (ammo < wanted) {
            // not enough rounds left for the charge: the shot is as strong as the rounds allow
            charge = (float) ammo / ammoPerFullCharge;
        }
        InteractionHand hand = player.getUsedItemHand();
        GunStats.Firing firing = stats().firing();
        if (!level.isClientSide) {
            // charged shots always come from the main arm side (Techguns 2)
            FirePos firePos = firePos(player, InteractionHand.MAIN_HAND);
            GunStats.Projectile projectile = stats().projectile();
            spawnCharged(level, player, stack, projectile.spread(), projectile.forwardOffset(), charge, ammo, firePos);
            for (int i = 0; i < projectile.pellets(); i++) {
                spawnCharged(level, player, stack, projectile.pelletSpread(), projectile.forwardOffset(), charge, ammo, firePos);
            }
            TGNetwork.sendGunFired(player, hand, firing.recoilTicks(), firing.muzzleFlashTicks());
        } else {
            GunClientEffects.get().onFired(player, hand, firing.recoilTicks(), firing.muzzleFlashTicks(), charge);
        }
        playSound(level, player, sounds().fire().get(), SOUND_VOLUME);
    }

    private void spawnCharged(Level level, LivingEntity shooter, ItemStack stack, float spread, float forwardOffset, float charge, int ammo,
                              FirePos firePos) {
        ShotParams params = shotParams(level, shooter, stack, spread, 1.0f, firePos, null);
        Projectile entity = factory(stack) instanceof ChargedProjectileFactory<?> charged
                ? charged.createCharged(params, charge, ammo)
                : factory(stack).create(params);
        if (forwardOffset > 0.0f) {
            entity.setPos(entity.position().add(entity.getDeltaMovement().scale(forwardOffset / params.speed())));
        }
        level.addFreshEntity(entity);
    }
}
