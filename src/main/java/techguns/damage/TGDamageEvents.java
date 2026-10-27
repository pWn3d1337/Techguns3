package techguns.damage;

import net.minecraft.core.Holder;
import net.minecraft.tags.DamageTypeTags;
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
import techguns.TGConfig;
import techguns.core.damage.DeathEffectRules;
import techguns.core.damage.DeathType;
import techguns.core.damage.TGArmorFormula;
import techguns.core.damage.TGDamageCategory;
import techguns.gun.MeleeToolItem;
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
                && event.getSource().getEntity() instanceof Player player && player.getMainHandItem().getItem() instanceof MeleeToolItem tool) {
            // melee tools hit with the vanilla player attack; their death effect comes from the tool
            DeathType deathType = tool.meleeDeathType(player.getMainHandItem());
            if (DeathEffectRules.playsDeathEffect(deathType, debugAlwaysDeathEffect ? 1.0f : tool.meleeGoreChance(), entity.getRandom().nextFloat())) {
                TGNetwork.sendEntityDeath(entity, deathType);
            }
        }
    }

    public static void onLivingIncomingDamage(LivingIncomingDamageEvent event) {
        LivingEntity target = event.getEntity();
        DamageContainer container = event.getContainer();

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

    /** Damage multiplier for Techguns damage depending on who hits whom (config). */
    private static float damageFactor(Entity attacker, LivingEntity target) {
        if (attacker instanceof Player && target instanceof Player) {
            return target.getServer() != null && target.getServer().isPvpAllowed() ? TGConfig.DAMAGE_PVP.get().floatValue() : 0.0f;
        }
        if (attacker instanceof Player) {
            return 1.0f;
        }
        return TGConfig.DAMAGE_FACTOR_NPC.get().floatValue();
    }
}
