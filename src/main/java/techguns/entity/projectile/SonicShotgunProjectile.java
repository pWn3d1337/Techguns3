package techguns.entity.projectile;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import techguns.core.damage.DeathType;
import techguns.core.damage.TGDamageCategory;
import techguns.damage.TGDamage;
import techguns.damage.TGDamageSource;
import techguns.gun.ProjectileFactory;
import techguns.gun.ShotParams;
import techguns.network.TGNetwork;
import techguns.registry.TGEntities;

/**
 * One wave segment of the sonic shotgun (Fabric {@code SonicShotgunProjectile}): invisible, passes through
 * entities and hits each one once per shot; all segments of a shot share the set of entities already hit.
 * Only the main (centre) segment has the trail and the impact FX.
 */
public class SonicShotgunProjectile extends GenericProjectile {
    /** Knockback strength of a hit (Fabric {@code setKnockback(2.0f)}). */
    private static final float KNOCKBACK = 2.0f;

    /** Shared by the segments of one shot; server only. */
    private IntSet entitiesHit = new IntOpenHashSet();
    private boolean mainProjectile;

    public SonicShotgunProjectile(EntityType<? extends SonicShotgunProjectile> type, Level level) {
        super(type, level);
    }

    protected SonicShotgunProjectile(ShotParams params) {
        super(TGEntities.SONIC_SHOTGUN_PROJECTILE.get(), params);
    }

    /** Joins a shot: segments share {@code hit}; the main segment carries the FX. */
    public SonicShotgunProjectile joinShot(IntSet hit, boolean main) {
        this.entitiesHit = hit;
        this.mainProjectile = main;
        return this;
    }

    /** Points the segment along {@code direction} at its current speed. */
    public void redirect(Vec3 direction) {
        setDeltaMovement(direction.normalize().scale(speed));
        updateRotationFromMotion();
        yRotO = getYRot();
        xRotO = getXRot();
    }

    public boolean isMainProjectile() {
        return mainProjectile;
    }

    @Override
    protected boolean canHitEntity(Entity target) {
        return super.canHitEntity(target) && !entitiesHit.contains(target.getId());
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        // passes through: no removal
        Entity target = result.getEntity();
        if (level().isClientSide || !entitiesHit.add(target.getId())) {
            return;
        }
        if (getOwner() instanceof LivingEntity owner) {
            owner.setLastHurtMob(target);
        }
        // ignores the hurt cooldown, so the other segments and following shots always hit
        target.invulnerableTime = 0;
        if (target.hurt(createDamageSource(), currentDamage()) && target instanceof LivingEntity living) {
            Vec3 push = getDeltaMovement().normalize();
            living.knockback(KNOCKBACK * 0.5f, -push.x, -push.z);
            onHitLiving(living);
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        if (mainProjectile && level() instanceof ServerLevel server) {
            TGNetwork.sendFxNear(server, position(), IMPACT_FX_RADIUS, "SonicShotgunImpact");
        }
        removeAfterHit();
    }

    @Override
    protected TGDamageSource createDamageSource() {
        return TGDamage.physical(level(), this, getOwner(), DeathType.GORE).setGoreChance(1.0f).setArmorPenetration(penetration);
    }

    @Override
    public void writeSpawnData(RegistryFriendlyByteBuf buf) {
        super.writeSpawnData(buf);
        buf.writeBoolean(mainProjectile);
    }

    @Override
    public void readSpawnData(RegistryFriendlyByteBuf buf) {
        super.readSpawnData(buf);
        mainProjectile = buf.readBoolean();
        if (mainProjectile) {
            startTrail("SonicShotgunTrail");
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("main_projectile", mainProjectile);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        mainProjectile = tag.getBoolean("main_projectile");
    }

    public record Factory() implements ProjectileFactory<SonicShotgunProjectile> {
        @Override
        public SonicShotgunProjectile create(ShotParams params) {
            return new SonicShotgunProjectile(params);
        }

        @Override
        public TGDamageCategory damageCategory() {
            return TGDamageCategory.PHYSICAL;
        }
    }
}
