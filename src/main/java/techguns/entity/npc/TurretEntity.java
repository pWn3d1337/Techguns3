package techguns.entity.npc;

import java.util.UUID;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import techguns.compat.team.TGTeams;
import techguns.entity.ai.GunRangedAttackGoal;
import techguns.entity.ai.GunShooter;
import techguns.gun.FirePos;
import techguns.gun.GunItem;
import techguns.gun.GunStats;
import techguns.machine.TurretBlockEntity;

/**
 * Turret (Techguns 2 {@code NPCTurret}): a gun on a turret base, fixed on top of (or below) its {@link TurretBlockEntity}.
 * It fires the base's gun with the base's ammo at monsters, players and turrets by the owner's PvP setting, animals
 * if enabled, and whoever hurts it. Without its base it falls apart.
 */
public class TurretEntity extends PathfinderMob implements GunShooter {
    private static final EntityDataAccessor<BlockPos> BASE = SynchedEntityData.defineId(TurretEntity.class, EntityDataSerializers.BLOCK_POS);
    private static final EntityDataAccessor<Boolean> HANGING = SynchedEntityData.defineId(TurretEntity.class, EntityDataSerializers.BOOLEAN);
    /** Techguns 2 turret attributes. */
    public static final double MAX_HEALTH = 30.0;
    private static final double FOLLOW_RANGE = 20.0;
    /** Techguns 2 targets in a box this many blocks above and below. */
    private static final double TARGET_HEIGHT = 20.0;

    @Nullable
    private GunRangedAttackGoal attackGoal;
    private ItemStack goalWeapon = ItemStack.EMPTY;

    public TurretEntity(EntityType<? extends TurretEntity> type, Level level) {
        super(type, level);
        setNoGravity(true);
        setPersistenceRequired();
        xpReward = 0;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes().add(Attributes.MAX_HEALTH, MAX_HEALTH).add(Attributes.MOVEMENT_SPEED, 0.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0).add(Attributes.FOLLOW_RANGE, FOLLOW_RANGE);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(BASE, BlockPos.ZERO);
        builder.define(HANGING, false);
    }

    /** Puts the turret on its base: on top, or below it for a base on the ceiling. */
    public void mount(TurretBlockEntity base) {
        entityData.set(BASE, base.getBlockPos());
        entityData.set(HANGING, base.isHanging());
        Vec3 pos = mountPosition(base.getBlockPos(), base.isHanging());
        moveTo(pos.x, pos.y, pos.z, getYRot(), getXRot());
        refreshDimensions();
    }

    private Vec3 mountPosition(BlockPos base, boolean hanging) {
        return hanging ? new Vec3(base.getX() + 0.5, base.getY() - getBbHeight(), base.getZ() + 0.5)
                : new Vec3(base.getX() + 0.5, base.getY() + 1.0, base.getZ() + 0.5);
    }

    public BlockPos getBasePos() {
        return entityData.get(BASE);
    }

    /** Hangs below a base on the ceiling (Techguns 2 facing down). */
    public boolean isHanging() {
        return entityData.get(HANGING);
    }

    @Nullable
    public TurretBlockEntity base() {
        return level().getBlockEntity(getBasePos()) instanceof TurretBlockEntity base ? base : null;
    }

    /** The base's gun (clients get it with the base's update packets). */
    public ItemStack getWeapon() {
        TurretBlockEntity base = base();
        return base != null ? base.getWeapon() : ItemStack.EMPTY;
    }

    @Override
    public ItemStack getItemBySlot(EquipmentSlot slot) {
        return slot == EquipmentSlot.MAINHAND ? getWeapon() : super.getItemBySlot(slot);
    }

    @Override
    protected EntityDimensions getDefaultDimensions(Pose pose) {
        // Techguns 2: eyes at 0.75, or 0.15 above the bottom when hanging
        EntityDimensions dimensions = super.getDefaultDimensions(pose);
        return dimensions.withEyeHeight(isHanging() ? 0.15f : 0.75f);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (HANGING.equals(key)) {
            refreshDimensions();
        }
    }

    // --- AI ---

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(2, new LookAtPlayerGoal(this, LivingEntity.class, 80.0f));
        goalSelector.addGoal(3, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this) {
            @Override
            protected boolean canAttack(@Nullable LivingEntity target, net.minecraft.world.entity.ai.targeting.TargetingConditions conditions) {
                return isRetaliationTarget(target) && super.canAttack(target, conditions);
            }
        });
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, LivingEntity.class, 0, true, false, this::isTarget) {
            @Override
            protected AABB getTargetSearchArea(double distance) {
                return mob.getBoundingBox().inflate(distance, TARGET_HEIGHT, distance);
            }
        });
    }

    /** The owner's turrets don't fight back against the owner or allies (Techguns 2 {@code EntityAIHurtByTargetTGFactions}). */
    private boolean isRetaliationTarget(@Nullable LivingEntity attacker) {
        TurretBlockEntity base = base();
        UUID owner = base != null ? base.getOwner() : null;
        if (attacker instanceof Player player && owner != null) {
            return TGTeams.isHostile(owner, player.getUUID());
        }
        if (attacker instanceof TurretEntity other && owner != null) {
            TurretBlockEntity otherBase = other.base();
            UUID otherOwner = otherBase != null ? otherBase.getOwner() : null;
            return otherOwner != null && TGTeams.isHostile(owner, otherOwner);
        }
        return true;
    }

    /** Techguns 2 {@code NPCTurret} target selector. */
    private boolean isTarget(LivingEntity entity) {
        if (entity instanceof Enemy) {
            return true;
        }
        TurretBlockEntity base = base();
        if (base == null) {
            return false;
        }
        UUID owner = base.getOwner();
        if (base.getPvpSetting() != TGTeams.PVP_NONE && owner != null) {
            if (entity instanceof TurretEntity other) {
                TurretBlockEntity otherBase = other.base();
                return otherBase != null && otherBase.getOwner() != null && TGTeams.shouldAttack(owner, otherBase.getOwner(), base.getPvpSetting());
            }
            if (entity instanceof Player player) {
                return !player.isCreative() && !player.isSpectator() && TGTeams.shouldAttack(owner, player.getUUID(), base.getPvpSetting());
            }
        }
        if (base.isAttackAnimals() && entity instanceof Animal) {
            // pets and tamed horses are spared
            return !(entity instanceof TamableAnimal tamable && tamable.isTame()) && !(entity instanceof AbstractHorse horse && horse.isTamed())
                    && !(entity instanceof OwnableEntity ownable && ownable.getOwnerUUID() != null);
        }
        return false;
    }

    /** Techguns 2 {@code setCombatTask}: the gun's ranged attack goal and range. */
    private void updateCombatGoal(ItemStack weapon) {
        if (attackGoal != null) {
            goalSelector.removeGoal(attackGoal);
            attackGoal = null;
        }
        goalWeapon = weapon.copy();
        if (weapon.getItem() instanceof GunItem gun) {
            GunStats.Ai ai = gun.stats().ai();
            attackGoal = new GunRangedAttackGoal(this, 0.0, ai);
            goalSelector.addGoal(1, attackGoal);
            getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(ai.attackRange());
        }
    }

    @Override
    public void performGunAttack(LivingEntity target, float distanceFactor) {
        TurretBlockEntity base = base();
        ItemStack weapon = getWeapon();
        if (base != null && weapon.getItem() instanceof GunItem gun && base.consumeAmmo()) {
            // projectiles leave along the view; the whole turret turns, not only its head
            lookAt(target, 360.0f, 360.0f);
            yHeadRot = getYRot();
            gun.fireFromNpc(this, weapon, 1.0f, 1.0f, FirePos.CENTER);
        }
    }

    @Override
    public void tick() {
        super.tick();
        // fixed on the base
        Vec3 pos = mountPosition(getBasePos(), isHanging());
        setPos(pos.x, pos.y, pos.z);
        setDeltaMovement(Vec3.ZERO);
        // the swivel is the head: the body follows it
        setYRot(yHeadRot);
        yBodyRot = yHeadRot;
        if (level() instanceof ServerLevel server) {
            TurretBlockEntity base = base();
            if (base == null || !base.isMounted(this)) {
                hurt(damageSources().fellOutOfWorld(), 1.0f);
                return;
            }
            if (!ItemStack.isSameItem(goalWeapon, base.getWeapon())) {
                updateCombatGoal(base.getWeapon());
            }
        }
    }

    @Override
    public int getArmorValue() {
        TurretBlockEntity base = base();
        return base != null ? base.armorValue() : 0;
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        TurretBlockEntity base = base();
        if (base != null && base.isMounted(this)) {
            base.onTurretDeath();
        }
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    protected boolean shouldDropLoot() {
        return false;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putLong("base", getBasePos().asLong());
        tag.putBoolean("hanging", isHanging());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        entityData.set(BASE, BlockPos.of(tag.getLong("base")));
        entityData.set(HANGING, tag.getBoolean("hanging"));
    }
}
