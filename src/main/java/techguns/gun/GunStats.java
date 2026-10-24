package techguns.gun;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * Numeric gun statistics. Immutable and Codec-backed so they can later be loaded from datapacks
 * (design doc Q4). Behaviour that is not a number (projectiles, sounds, special logic) lives on the gun item.
 */
public record GunStats(Firing firing, Damage damage, Projectile projectile, Optional<Zoom> zoom, GunHandType handType, HoldType holdType) {

    public static final Codec<GunStats> CODEC = RecordCodecBuilder.create(i -> i.group(
            Firing.CODEC.fieldOf("firing").forGetter(GunStats::firing),
            Damage.CODEC.fieldOf("damage").forGetter(GunStats::damage),
            Projectile.CODEC.fieldOf("projectile").forGetter(GunStats::projectile),
            Zoom.CODEC.optionalFieldOf("zoom").forGetter(GunStats::zoom),
            GunHandType.CODEC.optionalFieldOf("hand_type", GunHandType.TWO_HANDED).forGetter(GunStats::handType),
            HoldType.CODEC.optionalFieldOf("hold_type", HoldType.CROSSBOW).forGetter(GunStats::holdType)
    ).apply(i, GunStats::new));

    /**
     * @param semiAuto         one shot per click
     * @param fireDelay        minimum ticks between shots
     * @param clipSize         shots per reload
     * @param reloadTicks      reload duration
     * @param ammoPerReload    ammo items loaded per reload (shotguns load several shells)
     * @param recoilTicks      duration of the recoil animation
     * @param muzzleFlashTicks duration of the muzzle flash
     * @param silenced         no muzzle light, quieter
     */
    public record Firing(boolean semiAuto, int fireDelay, int clipSize, int reloadTicks, int ammoPerReload,
                         int recoilTicks, int muzzleFlashTicks, boolean silenced) {
        public static final Codec<Firing> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.BOOL.optionalFieldOf("semi_auto", false).forGetter(Firing::semiAuto),
                Codec.INT.fieldOf("fire_delay").forGetter(Firing::fireDelay),
                Codec.INT.fieldOf("clip_size").forGetter(Firing::clipSize),
                Codec.INT.fieldOf("reload_ticks").forGetter(Firing::reloadTicks),
                Codec.INT.optionalFieldOf("ammo_per_reload", 1).forGetter(Firing::ammoPerReload),
                Codec.INT.optionalFieldOf("recoil_ticks", 5).forGetter(Firing::recoilTicks),
                Codec.INT.optionalFieldOf("muzzle_flash_ticks", 5).forGetter(Firing::muzzleFlashTicks),
                Codec.BOOL.optionalFieldOf("silenced", false).forGetter(Firing::silenced)
        ).apply(i, Firing::new));
    }

    /**
     * @param damage      damage at short range
     * @param damageMin   damage beyond {@code dropEnd}
     * @param dropStart   distance where damage starts to drop
     * @param dropEnd     distance where damage reaches {@code damageMin}
     * @param penetration armor penetration per armor piece
     * @param radius      explosion/area radius, if the projectile has one
     */
    public record Damage(float damage, float damageMin, float dropStart, float dropEnd, float penetration, float radius) {
        public static final Codec<Damage> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.FLOAT.fieldOf("damage").forGetter(Damage::damage),
                Codec.FLOAT.fieldOf("damage_min").forGetter(Damage::damageMin),
                Codec.FLOAT.fieldOf("drop_start").forGetter(Damage::dropStart),
                Codec.FLOAT.fieldOf("drop_end").forGetter(Damage::dropEnd),
                Codec.FLOAT.optionalFieldOf("penetration", 0.0f).forGetter(Damage::penetration),
                Codec.FLOAT.optionalFieldOf("radius", 1.0f).forGetter(Damage::radius)
        ).apply(i, Damage::new));
    }

    /**
     * @param speed         blocks per tick
     * @param range         maximum travel distance in blocks
     * @param spread        inaccuracy of a single shot
     * @param gravity       downward acceleration per tick
     * @param forwardOffset spawn the projectile this far ahead of the muzzle
     * @param pellets       extra projectiles per shot (shotguns), 0 = single projectile
     * @param pelletSpread  spread of the extra projectiles
     * @param burst         extra projectiles are fired in a line instead of a cone
     */
    public record Projectile(float speed, float range, float spread, double gravity, float forwardOffset,
                             int pellets, float pelletSpread, boolean burst) {
        public static final Codec<Projectile> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.FLOAT.fieldOf("speed").forGetter(Projectile::speed),
                Codec.FLOAT.fieldOf("range").forGetter(Projectile::range),
                Codec.FLOAT.fieldOf("spread").forGetter(Projectile::spread),
                Codec.DOUBLE.optionalFieldOf("gravity", 0.0).forGetter(Projectile::gravity),
                Codec.FLOAT.optionalFieldOf("forward_offset", 0.0f).forGetter(Projectile::forwardOffset),
                Codec.INT.optionalFieldOf("pellets", 0).forGetter(Projectile::pellets),
                Codec.FLOAT.optionalFieldOf("pellet_spread", 0.0f).forGetter(Projectile::pelletSpread),
                Codec.BOOL.optionalFieldOf("burst", false).forGetter(Projectile::burst)
        ).apply(i, Projectile::new));

        /** Projectile lifetime in ticks. */
        public int lifetimeTicks() {
            return techguns.core.gun.GunMath.lifetimeTicks(range, speed);
        }
    }

    /**
     * @param fovMultiplier    field of view multiplier while zoomed
     * @param toggle           right click toggles zoom (instead of hold)
     * @param spreadMultiplier spread multiplier while zoomed
     * @param fireCentered     shoot from the eye centre while zoomed
     */
    public record Zoom(float fovMultiplier, boolean toggle, float spreadMultiplier, boolean fireCentered) {
        public static final Codec<Zoom> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.FLOAT.fieldOf("fov_multiplier").forGetter(Zoom::fovMultiplier),
                Codec.BOOL.optionalFieldOf("toggle", true).forGetter(Zoom::toggle),
                Codec.FLOAT.optionalFieldOf("spread_multiplier", 1.0f).forGetter(Zoom::spreadMultiplier),
                Codec.BOOL.optionalFieldOf("fire_centered", false).forGetter(Zoom::fireCentered)
        ).apply(i, Zoom::new));
    }

    /**
     * Starts a builder with the required base stats, using the Techguns 2 constructor order.
     *
     * @param range maximum range in blocks; damage drop defaults to none
     */
    public static Builder builder(boolean semiAuto, int fireDelay, int clipSize, int reloadTicks, float damage, float range, float spread) {
        return new Builder(semiAuto, fireDelay, clipSize, reloadTicks, damage, range, spread);
    }

    public static final class Builder {
        private final boolean semiAuto;
        private final int fireDelay;
        private final int clipSize;
        private final int reloadTicks;
        private int ammoPerReload = 1;
        private int recoilTicks = 5;
        private int muzzleFlashTicks = 5;
        private boolean silenced;

        private final float damage;
        private float damageMin;
        private float dropStart;
        private float dropEnd;
        private float penetration;
        private float radius = 1.0f;

        private float speed = 2.0f;
        private final float range;
        private final float spread;
        private double gravity;
        private float forwardOffset;
        private int pellets;
        private float pelletSpread;
        private boolean burst;

        private Zoom zoom;
        private GunHandType handType = GunHandType.TWO_HANDED;
        private HoldType holdType = HoldType.CROSSBOW;

        private Builder(boolean semiAuto, int fireDelay, int clipSize, int reloadTicks, float damage, float range, float spread) {
            this.semiAuto = semiAuto;
            this.fireDelay = fireDelay;
            this.clipSize = clipSize;
            this.reloadTicks = reloadTicks;
            this.damage = damage;
            this.damageMin = damage;
            this.range = range;
            this.dropStart = range;
            this.dropEnd = range;
            this.spread = spread;
        }

        public Builder speed(float speed) {
            this.speed = speed;
            return this;
        }

        public Builder damageDrop(float start, float end, float damageMin) {
            this.dropStart = start;
            this.dropEnd = end;
            this.damageMin = damageMin;
            return this;
        }

        public Builder penetration(float penetration) {
            this.penetration = penetration;
            return this;
        }

        public Builder radius(float radius) {
            this.radius = radius;
            return this;
        }

        public Builder gravity(double gravity) {
            this.gravity = gravity;
            return this;
        }

        public Builder forwardOffset(float offset) {
            this.forwardOffset = offset;
            return this;
        }

        public Builder shotgun(int pellets, float pelletSpread, boolean burst) {
            this.pellets = pellets;
            this.pelletSpread = pelletSpread;
            this.burst = burst;
            return this;
        }

        public Builder ammoPerReload(int count) {
            this.ammoPerReload = count;
            return this;
        }

        public Builder recoilTicks(int ticks) {
            this.recoilTicks = ticks;
            return this;
        }

        public Builder muzzleFlashTicks(int ticks) {
            this.muzzleFlashTicks = ticks;
            return this;
        }

        public Builder silenced() {
            this.silenced = true;
            return this;
        }

        public Builder zoom(float fovMultiplier, boolean toggle, float spreadMultiplier, boolean fireCentered) {
            this.zoom = new Zoom(fovMultiplier, toggle, spreadMultiplier, fireCentered);
            return this;
        }

        public Builder handType(GunHandType handType) {
            this.handType = handType;
            return this;
        }

        public Builder holdType(HoldType holdType) {
            this.holdType = holdType;
            return this;
        }

        public GunStats build() {
            return new GunStats(
                    new Firing(semiAuto, fireDelay, clipSize, reloadTicks, ammoPerReload, recoilTicks, muzzleFlashTicks, silenced),
                    new Damage(damage, damageMin, dropStart, dropEnd, penetration, radius),
                    new Projectile(speed, range, spread, gravity, forwardOffset, pellets, pelletSpread, burst),
                    Optional.ofNullable(zoom), handType, holdType);
        }
    }
}
