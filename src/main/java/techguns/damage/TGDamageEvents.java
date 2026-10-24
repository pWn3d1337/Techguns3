package techguns.damage;

import net.minecraft.core.Holder;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import techguns.TGConfig;
import techguns.core.damage.TGArmorFormula;
import techguns.core.damage.TGDamageCategory;
import techguns.registry.TGAttributes;

/**
 * Hooks the Techguns damage system into NeoForge's damage pipeline (no mixins needed).
 */
public final class TGDamageEvents {
    private TGDamageEvents() {
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
        float armor = TGArmorFormula.armorAgainst(category, target.getArmorValue(), bonusArmor(target, category), target.fireImmune());
        float toughness = (float) target.getAttributeValue(Attributes.ARMOR_TOUGHNESS);
        return TGArmorFormula.damageAfterArmor(damage, armor, toughness, TGDamage.penetrationOf(container.getSource()));
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
