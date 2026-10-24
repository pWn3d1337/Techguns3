package techguns.damage;

import javax.annotation.Nullable;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import techguns.core.damage.DeathType;
import techguns.core.damage.TGDamageCategory;

/**
 * Single entry point for creating Techguns damage sources and classifying any damage source.
 */
public final class TGDamage {
    /** Order in which category tags are checked; first match wins. */
    private static final TGDamageCategory[] CATEGORY_LOOKUP_ORDER = {
            TGDamageCategory.UNRESISTABLE,
            TGDamageCategory.RADIATION,
            TGDamageCategory.DARK,
            TGDamageCategory.LIGHTNING,
            TGDamageCategory.ICE,
            TGDamageCategory.POISON,
            TGDamageCategory.ENERGY,
            TGDamageCategory.FIRE,
            TGDamageCategory.EXPLOSION,
            TGDamageCategory.PROJECTILE,
            TGDamageCategory.PHYSICAL,
    };

    private TGDamage() {
    }

    /**
     * Creates a Techguns damage source.
     *
     * @param directEntity  the entity dealing the damage (projectile), or the attacker for melee
     * @param causingEntity the entity responsible (shooter)
     */
    public static TGDamageSource create(Level level, ResourceKey<DamageType> type, @Nullable Entity directEntity, @Nullable Entity causingEntity) {
        return new TGDamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(type), directEntity, causingEntity);
    }

    public static TGDamageSource bullet(Level level, @Nullable Entity projectile, @Nullable Entity shooter, DeathType deathType) {
        return create(level, TGDamageTypes.BULLET, projectile, shooter).setDeathType(deathType);
    }

    public static TGDamageSource explosion(Level level, @Nullable Entity projectile, @Nullable Entity shooter, DeathType deathType) {
        return create(level, TGDamageTypes.EXPLOSION, projectile, shooter).setDeathType(deathType);
    }

    public static TGDamageSource energy(Level level, @Nullable Entity projectile, @Nullable Entity shooter, DeathType deathType) {
        return create(level, TGDamageTypes.ENERGY, projectile, shooter).setDeathType(deathType);
    }

    public static TGDamageSource fire(Level level, @Nullable Entity projectile, @Nullable Entity shooter, DeathType deathType) {
        return create(level, TGDamageTypes.FIRE, projectile, shooter).setDeathType(deathType);
    }

    public static TGDamageSource poison(Level level, @Nullable Entity projectile, @Nullable Entity shooter, DeathType deathType) {
        return create(level, TGDamageTypes.POISON, projectile, shooter).setDeathType(deathType);
    }

    public static TGDamageSource radiation(Level level, @Nullable Entity projectile, @Nullable Entity shooter, DeathType deathType) {
        return create(level, TGDamageTypes.RADIATION, projectile, shooter).setDeathType(deathType);
    }

    public static TGDamageSource radiationPoisoning(Level level, @Nullable Entity projectile, @Nullable Entity shooter, DeathType deathType) {
        return create(level, TGDamageTypes.RADIATION_POISONING, projectile, shooter).setDeathType(deathType).setGoreChance(1.0f);
    }

    public static TGDamageSource lightning(Level level, @Nullable Entity projectile, @Nullable Entity shooter, DeathType deathType) {
        return create(level, TGDamageTypes.LIGHTNING, projectile, shooter).setDeathType(deathType);
    }

    public static TGDamageSource dark(Level level, @Nullable Entity projectile, @Nullable Entity shooter, DeathType deathType) {
        return create(level, TGDamageTypes.DARK, projectile, shooter).setDeathType(deathType);
    }

    public static TGDamageSource ice(Level level, @Nullable Entity projectile, @Nullable Entity shooter, DeathType deathType) {
        return create(level, TGDamageTypes.ICE, projectile, shooter).setDeathType(deathType);
    }

    public static TGDamageSource physical(Level level, @Nullable Entity directEntity, @Nullable Entity attacker, DeathType deathType) {
        return create(level, TGDamageTypes.PHYSICAL, directEntity, attacker).setDeathType(deathType);
    }

    /**
     * Category of any damage source: explicit category tags first, then derived from vanilla tags.
     */
    public static TGDamageCategory categoryOf(DamageSource source) {
        for (TGDamageCategory category : CATEGORY_LOOKUP_ORDER) {
            if (source.is(TGDamageTypeTags.category(category))) {
                return category;
            }
        }
        if (source.is(DamageTypeTags.BYPASSES_ARMOR)) {
            return TGDamageCategory.UNRESISTABLE;
        }
        if (source.is(DamageTypeTags.IS_EXPLOSION)) {
            return TGDamageCategory.EXPLOSION;
        }
        if (source.is(DamageTypeTags.IS_LIGHTNING)) {
            return TGDamageCategory.LIGHTNING;
        }
        if (source.is(DamageTypeTags.IS_FREEZING)) {
            return TGDamageCategory.ICE;
        }
        if (source.is(DamageTypeTags.IS_FIRE)) {
            return TGDamageCategory.FIRE;
        }
        if (source.is(DamageTypeTags.IS_PROJECTILE)) {
            return TGDamageCategory.PROJECTILE;
        }
        return TGDamageCategory.PHYSICAL;
    }

    /** Armor penetration of a damage source; 0 for non-Techguns sources. */
    public static float penetrationOf(DamageSource source) {
        return source instanceof TGDamageSource tg ? tg.getArmorPenetration() : 0.0f;
    }
}
