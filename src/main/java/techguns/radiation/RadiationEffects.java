package techguns.radiation;

import java.util.Set;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.EffectCure;
import techguns.TGConfig;
import techguns.TGIds;
import techguns.core.damage.DeathType;
import techguns.core.radiation.RadiationRules;
import techguns.damage.TGDamage;
import techguns.registry.TGAttributes;
import techguns.registry.TGSounds;

/** The Techguns 2 radiation effects ({@code RadiationPotion}, {@code RadRegenerationPotion}, {@code RadResistancePotion}). */
public final class RadiationEffects {
    private RadiationEffects() {
    }

    /** Server: the entity's radiation resistance (0 for mobs, which don't have the attribute). */
    public static double resistance(LivingEntity entity) {
        AttributeInstance attribute = entity.getAttribute(TGAttributes.RADIATION_RESISTANCE);
        return attribute == null ? 0.0 : attribute.getValue();
    }

    /**
     * Every second its dose (level minus resistance): players collect it (not in creative or spectator mode) and hear
     * the geiger counter, mobs take half of it as radiation damage. Milk doesn't cure it.
     */
    public static final class Radiation extends MobEffect {
        public Radiation() {
            super(MobEffectCategory.HARMFUL, 0xF8FF00);
        }

        @Override
        public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
            return duration % 20 == 0;
        }

        @Override
        public boolean applyEffectTick(LivingEntity entity, int amplifier) {
            if (entity.level().isClientSide) {
                return true;
            }
            int dose = RadiationRules.dose(amplifier, resistance(entity));
            if (entity instanceof Player player) {
                if (!player.isCreative() && !player.isSpectator()) {
                    PlayerRadiation.add(player, dose);
                    if (dose > 0 && TGConfig.RADIATION_ENABLED.get() && player instanceof ServerPlayer serverPlayer) {
                        serverPlayer.playNotifySound(amplifier >= 2 ? TGSounds.GEIGER_HIGH.get() : TGSounds.GEIGER_LOW.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
                    }
                }
            } else if (entity instanceof Mob) {
                float damage = dose * RadiationRules.MOB_DAMAGE_PER_DOSE;
                if (damage >= 1.0f) {
                    entity.hurt(TGDamage.radiation(entity.level(), null, null, DeathType.BIO), damage);
                }
            }
            return true;
        }

        @Override
        public void fillEffectCures(Set<EffectCure> cures, MobEffectInstance effectInstance) {
            // Techguns 2: no curative items
        }
    }

    /** Takes away level + 1 radiation per second. */
    public static final class RadRegeneration extends MobEffect {
        public RadRegeneration() {
            super(MobEffectCategory.BENEFICIAL, 0xFA2000);
        }

        @Override
        public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
            return duration % 20 == 0;
        }

        @Override
        public boolean applyEffectTick(LivingEntity entity, int amplifier) {
            if (!entity.level().isClientSide && entity instanceof Player player) {
                PlayerRadiation.add(player, -(amplifier + 1));
            }
            return true;
        }
    }

    /** Radiation resistance + 1 per level. */
    public static final class RadResistance extends MobEffect {
        public RadResistance() {
            super(MobEffectCategory.BENEFICIAL, 0x99AC2D);
            addAttributeModifier(TGAttributes.RADIATION_RESISTANCE, TGIds.id("effect.rad_resistance"), 1.0, AttributeModifier.Operation.ADD_VALUE);
        }
    }
}
