package techguns.damage;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

import net.minecraft.core.Holder;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingKnockBackEvent;
import javax.annotation.Nullable;

import techguns.TGConfig;
import techguns.core.damage.DeathEffectRules;
import techguns.core.damage.DeathType;
import techguns.core.damage.TGArmorFormula;
import techguns.core.damage.TGDamageCategory;
import techguns.entity.npc.TurretEntity;
import techguns.gun.MeleeToolItem;
import techguns.item.TGMeleeWeapon;
import techguns.network.TGNetwork;
import techguns.registry.TGAttributes;

/**
 * Hooks the Techguns damage system into NeoForge's damage pipeline (no mixins needed).
 */
public final class TGDamageEvents {
    private TGDamageEvents() {
    }

    /** Dev testing only (/tgdebug alwaysdeath): every Techguns kill plays its death effect. */
    public static boolean debugAlwaysDeathEffect = false;

    /** Death effects (gore, bio, laser) for kills by Techguns damage (Techguns 2 {@code onLivingDeathEvent}). */
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (!entity.level().isClientSide && event.getSource() instanceof TGDamageSource source
                && DeathEffectRules.playsDeathEffect(source.getDeathType(), debugAlwaysDeathEffect ? 1.0f : source.getGoreChance(),
                        entity.getRandom().nextFloat())) {
            TGNetwork.sendEntityDeath(entity, source.getDeathType());
        } else if (!entity.level().isClientSide && event.getSource().is(DamageTypes.PLAYER_ATTACK)
                && event.getSource().getEntity() instanceof Player player && player.getMainHandItem().getItem() instanceof TGMeleeWeapon tool) {
            // melee tools and weapons hit with the vanilla player attack; their death effect comes from the tool
            DeathType deathType = tool.meleeDeathType(player.getMainHandItem());
            if (DeathEffectRules.playsDeathEffect(deathType, debugAlwaysDeathEffect ? 1.0f : tool.meleeGoreChance(), entity.getRandom().nextFloat())) {
                TGNetwork.sendEntityDeath(entity, deathType);
            }
        }
    }

    /**
     * Knockback multiplier of the hit an entity is taking, for its knockback later in the same hurt (the knockback
     * event doesn't carry the damage source); only valid in the tick it was set.
     */
    private record PendingKnockback(float multiplier, long gameTime) {
    }

    private static final Map<LivingEntity, PendingKnockback> PENDING_KNOCKBACK = Collections.synchronizedMap(new WeakHashMap<>());

    /** Techguns 2 / Fabric knockback multiplier: TG damage sources and powered melee tools scale the hit's knockback. */
    public static float knockbackMultiplier(DamageSource source) {
        if (source instanceof TGDamageSource tgSource) {
            return tgSource.getKnockbackMultiplier();
        }
        if (source.is(DamageTypes.PLAYER_ATTACK) && source.getEntity() instanceof Player player
                && player.getMainHandItem().getItem() instanceof MeleeToolItem tool) {
            return tool.meleeKnockback(player.getMainHandItem());
        }
        return 1.0f;
    }

    public static void onKnockback(LivingKnockBackEvent event) {
        LivingEntity entity = event.getEntity();
        PendingKnockback pending = PENDING_KNOCKBACK.remove(entity);
        if (pending == null || pending.gameTime() != entity.level().getGameTime()) {
            return;
        }
        if (pending.multiplier() <= 0.0f) {
            event.setCanceled(true);
        } else {
            event.setStrength(event.getStrength() * pending.multiplier());
        }
    }

    public static void onLivingIncomingDamage(LivingIncomingDamageEvent event) {
        LivingEntity target = event.getEntity();
        DamageContainer container = event.getContainer();
        float knockback = knockbackMultiplier(container.getSource());
        if (knockback != 1.0f) {
            PENDING_KNOCKBACK.put(target, new PendingKnockback(knockback, target.level().getGameTime()));
        } else {
            PENDING_KNOCKBACK.remove(target);
        }

        if (container.getSource() instanceof TGDamageSource source) {
            float factor = damageFactor(source.getEntity(), target);
            if (factor <= 0.0f) {
                event.setCanceled(true);
                return;
            }
            container.setNewDamage(container.getNewDamage() * factor);
        }

        if (TGConfig.TG_ARMOR_SYSTEM.getAsBoolean()) {
            // Replaces the vanilla armor reduction. The ARMOR reduction is set for every hit, so armor-bypassing sources keep the vanilla value (0).
            event.addReductionModifier(DamageContainer.Reduction.ARMOR, (dc, vanillaReduction) -> {
                if (dc.getSource().is(DamageTypeTags.BYPASSES_ARMOR)) {
                    return vanillaReduction;
                }
                float damage = dc.getNewDamage();
                return damage - damageAfterTGArmor(target, dc, damage);
            });
        }
    }

    private static float damageAfterTGArmor(LivingEntity target, DamageContainer container, float damage) {
        TGDamageCategory category = TGDamage.categoryOf(container.getSource());
        float armor = target instanceof TGArmorProvider provider ? provider.tgArmorAgainst(category)
                : TGArmorFormula.armorAgainst(category, target.getArmorValue(), bonusArmor(target, category), target.fireImmune());
        float toughness = (float) target.getAttributeValue(Attributes.ARMOR_TOUGHNESS);
        float penetration = TGDamage.penetrationOf(container.getSource());
        if (target instanceof TGArmorProvider provider) {
            penetration = Math.max(penetration - provider.tgPenetrationResistance(category), 0.0f);
        }
        return TGArmorFormula.damageAfterArmor(damage, armor, toughness, penetration);
    }

    private static float bonusArmor(LivingEntity target, TGDamageCategory category) {
        Holder<Attribute> attribute = TGAttributes.bonusArmor(category);
        if (attribute == null) {
            return 0.0f;
        }
        AttributeInstance instance = target.getAttribute(attribute);
        return instance == null ? 0.0f : (float) instance.getValue();
    }

    /**
     * Damage multiplier for Techguns damage depending on who hits whom (config, Techguns 2 {@code DamageSystem.getDamageFactor}).
     * Damage without an attacker (radiation, acid, ownerless shots) is not scaled.
     */
    public static float damageFactor(@Nullable Entity attacker, LivingEntity target) {
        if (attacker == null) {
            return 1.0f;
        }
        if (attacker instanceof Player && target instanceof Player) {
            return target.getServer() != null && target.getServer().isPvpAllowed() ? TGConfig.DAMAGE_PVP.get().floatValue() : 0.0f;
        }
        if (attacker instanceof Player) {
            return 1.0f;
        }
        if (attacker instanceof TurretEntity && target instanceof Player) {
            return TGConfig.DAMAGE_TURRET_TO_PLAYER.get().floatValue();
        }
        return TGConfig.DAMAGE_FACTOR_NPC.get().floatValue();
    }
}
