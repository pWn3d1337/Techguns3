package techguns.radiation;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.PushReaction;
import techguns.core.radiation.RadiationRules;
import techguns.registry.TGEntities;

/**
 * Techguns 2 {@code EntityRadiation}: an invisible lingering radiation area (the fallout of a nuke). Every second it
 * irradiates everything within its radius; it keeps its strength for half its time, then fades out.
 */
public class RadiationArea extends Entity {
    /** Techguns 2 {@code RAD_INTERVAL}; each pulse lasts 2 ticks longer, so the effect doesn't lapse. */
    private static final int INTERVAL = 20;

    private int duration;
    private float radius;
    private float innerRadius;
    private int strength;
    private int outerStrength;

    public RadiationArea(EntityType<? extends RadiationArea> type, Level level) {
        super(type, level);
        noPhysics = true;
    }

    public static RadiationArea create(Level level, double x, double y, double z, int duration, float innerRadius, int strength, float radius,
                                       int outerStrength) {
        RadiationArea area = new RadiationArea(TGEntities.RADIATION_AREA.get(), level);
        area.setPos(x, y, z);
        area.duration = duration;
        area.innerRadius = innerRadius;
        area.strength = strength;
        area.radius = radius;
        area.outerStrength = outerStrength;
        return area;
    }

    /** Techguns 2 {@code RocketProjectileNuke}: a minute of radiation X inside the blast's full-damage radius, III at its edge. */
    public static RadiationArea nuke(Level level, double x, double y, double z, float innerRadius, float radius) {
        return create(level, x, y, z, 20 * 60, innerRadius, 9, radius, 2);
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            return;
        }
        if (tickCount > duration) {
            discard();
            return;
        }
        if (tickCount % INTERVAL == 0) {
            RadiationSources.irradiate(level(), position(), radius, INTERVAL + 2, RadiationRules.fade(tickCount, duration, strength), innerRadius,
                    RadiationRules.fade(tickCount, duration, outerStrength));
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        tickCount = tag.getInt("Age");
        duration = tag.getInt("Duration");
        radius = tag.getFloat("Radius");
        innerRadius = tag.getFloat("InnerRadius");
        strength = tag.getInt("Strength");
        outerStrength = tag.getInt("OuterStrength");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("Age", tickCount);
        tag.putInt("Duration", duration);
        tag.putFloat("Radius", radius);
        tag.putFloat("InnerRadius", innerRadius);
        tag.putInt("Strength", strength);
        tag.putInt("OuterStrength", outerStrength);
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public PushReaction getPistonPushReaction() {
        return PushReaction.IGNORE;
    }
}
