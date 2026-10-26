package techguns.armor;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import techguns.TGIds;
import techguns.registry.TGAttributes;

/**
 * Applies the armor bonus attributes the way Techguns 2 {@code TGTickHandler} / {@code TGEventHandler} did: speed as
 * a percentage (doubled while sprinting, plus the water bonus in water or lava), jump velocity, fall damage reduction
 * and free fall height, mining speed (plus the water bonus) and the cooling system.
 */
public final class ArmorBonusEvents {
    private static final ResourceLocation SPEED_MODIFIER = TGIds.id("armor_speed_bonus");

    private ArmorBonusEvents() {
    }

    public static void init(IEventBus gameBus) {
        gameBus.addListener(ArmorBonusEvents::onPlayerTick);
        gameBus.addListener(ArmorBonusEvents::onJump);
        gameBus.addListener(ArmorBonusEvents::onFall);
        gameBus.addListener(ArmorBonusEvents::onBreakSpeed);
    }

    private static double value(Player player, Holder<Attribute> attribute) {
        AttributeInstance instance = player.getAttribute(attribute);
        return instance == null ? 0.0 : instance.getValue();
    }

    private static boolean inWaterOrLava(Player player) {
        return player.isEyeInFluidType(NeoForgeMod.WATER_TYPE.value()) || player.isEyeInFluidType(NeoForgeMod.LAVA_TYPE.value());
    }

    /** Current speed multiplier bonus (Techguns 2: x2 while sprinting, water bonus only in water or lava). */
    public static double speedBonus(Player player) {
        double speed = value(player, TGAttributes.SPEED_BONUS);
        if (inWaterOrLava(player)) {
            speed += value(player, TGAttributes.WATER_SPEED_BONUS);
        }
        return player.isSprinting() ? speed * 2.0 : speed;
    }

    private static void onPlayerTick(PlayerTickEvent.Pre event) {
        Player player = event.getEntity();
        AttributeInstance movement = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (movement != null) {
            double speed = speedBonus(player);
            if (speed > 0.0) {
                movement.addOrUpdateTransientModifier(new AttributeModifier(SPEED_MODIFIER, speed, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            } else {
                movement.removeModifier(SPEED_MODIFIER);
            }
        }
        if (!player.level().isClientSide && player.isOnFire() && value(player, TGAttributes.COOLING_SYSTEM) >= 1.0) {
            player.clearFire();
        }
    }

    private static void onJump(LivingEvent.LivingJumpEvent event) {
        if (event.getEntity() instanceof Player player) {
            double jump = value(player, TGAttributes.JUMP_BONUS);
            if (jump > 0.0) {
                Vec3 motion = player.getDeltaMovement();
                player.setDeltaMovement(motion.x, motion.y + jump, motion.z);
            }
        }
    }

    private static void onFall(LivingFallEvent event) {
        if (event.getEntity() instanceof Player player) {
            float freeHeight = (float) value(player, TGAttributes.FALL_FREE_HEIGHT);
            float reduction = (float) value(player, TGAttributes.FALL_DAMAGE_REDUCTION);
            float distance = Math.max(0.0f, event.getDistance() - freeHeight);
            event.setDistance(distance * Math.max(0.0f, 1.0f - reduction));
        }
    }

    private static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        Player player = event.getEntity();
        double bonus = 1.0 + value(player, TGAttributes.MINING_SPEED_BONUS);
        if (inWaterOrLava(player)) {
            bonus *= 1.0 + value(player, TGAttributes.WATER_MINING_SPEED_BONUS);
        }
        if (bonus != 1.0) {
            event.setNewSpeed((float) (event.getNewSpeed() * bonus));
        }
    }
}
