package techguns.gun;

import java.util.List;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import techguns.core.damage.TGDamageCategory;
import techguns.entity.projectile.GrenadeProjectile;

/**
 * Hand grenade (Techguns 2 {@code GenericGrenade}): hold the use key to wind up, release to throw. A longer wind-up
 * (up to {@link #FULL_CHARGE_TICKS}) throws flatter and further.
 */
public class GrenadeItem extends Item {
    private static final int USE_DURATION = 72000;
    private static final float FULL_CHARGE_TICKS = 30.0f;
    /** Shortest wind-up counted, so a click still throws (gravity is divided by the charge). */
    private static final float MIN_CHARGE = 0.1f;
    private static final float SPEED = 0.75f;
    private static final float SPREAD = 0.1f;
    private static final int LIFETIME = 200;

    private final GrenadeProjectile.Type type;
    /** Full damage within {@code radius} blocks, falling to {@code damageMin} at {@code radiusMin}. */
    private final float damage;
    private final float radius;
    private final float damageMin;
    private final float radiusMin;
    @Nullable
    private final Supplier<SoundEvent> startSound;

    public GrenadeItem(Properties properties, GrenadeProjectile.Type type, float damage, float radius, float damageMin, float radiusMin,
                       @Nullable Supplier<SoundEvent> startSound) {
        super(properties);
        this.type = type;
        this.damage = damage;
        this.radius = radius;
        this.damageMin = damageMin;
        this.radiusMin = radiusMin;
        this.startSound = startSound;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        player.startUsingItem(hand);
        if (!level.isClientSide && startSound != null) {
            level.playSound(null, player.getX(), player.getEyeY(), player.getZ(), startSound.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
        }
        return InteractionResultHolder.consume(player.getItemInHand(hand));
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return USE_DURATION;
    }

    /** No vanilla use pose: the grenade renderer tilts the grenade itself while winding up (as in Techguns 2). */
    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.NONE;
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        float charge = Math.min(1.0f, Math.max(MIN_CHARGE, (getUseDuration(stack, entity) - timeLeft) / FULL_CHARGE_TICKS));
        if (!level.isClientSide) {
            boolean left = (entity.getUsedItemHand() == InteractionHand.OFF_HAND) != (entity.getMainArm() == HumanoidArm.LEFT);
            ShotParams params = new ShotParams(null, level, entity, damage, damageMin, radius, radiusMin, 0.0f, SPEED, LIFETIME, SPREAD,
                    0.015 / charge, radiusMin, GunItem.blockDamage(entity), left ? FirePos.LEFT : FirePos.RIGHT, null);
            level.addFreshEntity(new GrenadeProjectile(params, type));
        }
        if (!(entity instanceof Player player) || !player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        entity.swing(entity.getUsedItemHand());
    }

    /** Techguns 2 {@code GenericGrenade.addInformation}: damage; with shift also damage type, radius and bounces. */
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        boolean detailed = GunClientEffects.get().hasShiftDown();
        if (detailed) {
            tooltip.add(GunTooltip.line("tooltip.techguns.gun.damage_type", GunTooltip.damageCategory(TGDamageCategory.EXPLOSION)));
        }
        tooltip.add(GunTooltip.line("tooltip.techguns.gun.damage", GunTooltip.oneDecimal(damage) + "-" + GunTooltip.oneDecimal(damageMin)));
        if (detailed) {
            tooltip.add(GunTooltip.line("tooltip.techguns.gun.radius", GunTooltip.oneDecimal(radius), GunTooltip.oneDecimal(radiusMin)));
            tooltip.add(GunTooltip.line("tooltip.techguns.grenade.bounces", type.bounces));
        } else {
            tooltip.add(Component.translatable("tooltip.techguns.shift_for_details").withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}
