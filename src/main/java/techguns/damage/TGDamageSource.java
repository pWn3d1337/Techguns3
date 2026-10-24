package techguns.damage;

import javax.annotation.Nullable;

import net.minecraft.core.Holder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import techguns.core.damage.DeathType;

/**
 * Damage source carrying Techguns hit data. Create through {@link TGDamage}, not directly.
 */
public class TGDamageSource extends DamageSource {
    /** Armor penetration per armor piece (gun stat). */
    private float armorPenetration;
    private DeathType deathType = DeathType.DEFAULT;
    /** Chance for gore death effects when this hit kills. */
    private float goreChance = 0.5f;

    TGDamageSource(Holder<DamageType> type, @Nullable Entity directEntity, @Nullable Entity causingEntity) {
        super(type, directEntity, causingEntity);
    }

    public float getArmorPenetration() {
        return armorPenetration;
    }

    public TGDamageSource setArmorPenetration(float armorPenetration) {
        this.armorPenetration = armorPenetration;
        return this;
    }

    public DeathType getDeathType() {
        return deathType;
    }

    public TGDamageSource setDeathType(DeathType deathType) {
        this.deathType = deathType;
        return this;
    }

    public float getGoreChance() {
        return goreChance;
    }

    public TGDamageSource setGoreChance(float goreChance) {
        this.goreChance = goreChance;
        return this;
    }
}
