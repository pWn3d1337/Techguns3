package techguns.entity.npc;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import techguns.entity.ai.GunRangedAttackGoal;
import techguns.entity.ai.GunShooter;
import techguns.gun.FirePos;
import techguns.gun.GunItem;

/**
 * Techguns 2 {@code GenericNPC}: a monster that uses the gun in its hand (the gun's NPC attack stats, damage and
 * accuracy by difficulty, unlimited ammo) or attacks in melee, wanders, and targets players. Its armor against
 * Techguns damage is its own stat, not the worn armor (Techguns 2 {@code INpcTGDamageSystem}). One class for all NPCs
 * that only differ in their {@link NpcDefinition}; NPCs with own behaviour extend it.
 */
public class TGNpc extends Monster implements GunShooter, RangedAttackMob {
    private final NpcDefinition definition;
    @Nullable
    private Goal combatGoal;

    public TGNpc(EntityType<? extends TGNpc> type, Level level, NpcDefinition definition) {
        super(type, level);
        this.definition = definition;
        this.xpReward = definition.experience();
        if (!level.isClientSide) {
            updateCombatGoal();
        }
    }

    /** Techguns 2 GenericNPC defaults; each NPC type sets its own values on top. */
    public static AttributeSupplier.Builder createAttributes(double health, double speed, double attack) {
        return createAttributes(health, speed, attack, 50.0, 0.0);
    }

    public static AttributeSupplier.Builder createAttributes(double health, double speed, double attack, double followRange, double toughness) {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, health).add(Attributes.MOVEMENT_SPEED, speed)
                .add(Attributes.ATTACK_DAMAGE, attack).add(Attributes.FOLLOW_RANGE, followRange).add(Attributes.ARMOR_TOUGHNESS, toughness);
    }

    public NpcDefinition definition() {
        return definition;
    }

    public TGNpcFaction faction() {
        return definition.faction();
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(1, new FloatGoal(this));
        goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0));
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0f));
        goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this) {
            @Override
            protected boolean canAttack(@Nullable LivingEntity target, TargetingConditions conditions) {
                return isRetaliationTarget(target) && super.canAttack(target, conditions);
            }
        });
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    /** Techguns 2 {@code EntityAIHurtByTargetTGFactions}: NPCs of friendly factions don't fight back against each other. */
    private boolean isRetaliationTarget(@Nullable LivingEntity attacker) {
        if (attacker instanceof TGNpc other) {
            return faction().isHostileTo(other.faction());
        }
        if (attacker instanceof TurretEntity) {
            return faction().isHostileTo(TGNpcFaction.TURRET);
        }
        return true;
    }

    /** Techguns 2 {@code setCombatTask}: the gun's ranged attack, or melee without a gun. */
    protected void updateCombatGoal() {
        if (combatGoal != null) {
            goalSelector.removeGoal(combatGoal);
        }
        combatGoal = getMainHandItem().getItem() instanceof GunItem gun ? new GunRangedAttackGoal(this, 1.0, gun.stats().ai())
                : new MeleeAttackGoal(this, 1.2, false);
        goalSelector.addGoal(4, combatGoal);
    }

    @Override
    public void setItemSlot(EquipmentSlot slot, ItemStack stack) {
        super.setItemSlot(slot, stack);
        if (slot == EquipmentSlot.MAINHAND && !level().isClientSide) {
            updateCombatGoal();
        }
    }

    /** Techguns 2 {@code attackEntityWithRangedAttack}: the difficulty scales gun damage and accuracy. */
    @Override
    public void performGunAttack(LivingEntity target, float distanceFactor) {
        ItemStack weapon = getMainHandItem();
        if (!(weapon.getItem() instanceof GunItem gun)) {
            return;
        }
        Difficulty difficulty = level().getDifficulty();
        float accuracy = switch (difficulty) {
            case EASY -> 1.3f;
            case NORMAL -> 1.15f;
            default -> 1.0f;
        };
        float damage = switch (difficulty) {
            case EASY -> 0.6f;
            case NORMAL -> 0.8f;
            default -> 1.0f;
        };
        // projectiles leave along the view; the head turns faster than the body
        lookAt(target, 360.0f, 360.0f);
        yHeadRot = getYRot();
        yBodyRot = getYRot();
        gun.fireFromNpc(this, weapon, damage, accuracy, FirePos.RIGHT);
    }

    @Override
    public void performRangedAttack(LivingEntity target, float distanceFactor) {
        performGunAttack(target, distanceFactor);
    }

    /** Whether the NPC holds a gun (renderers aim it like a bow). */
    public boolean isHoldingGun() {
        return getMainHandItem().getItem() instanceof GunItem;
    }

    @Override
    public int getArmorValue() {
        return definition.armor() < 0 ? super.getArmorValue() : Math.round(definition.armor());
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType,
                                        @Nullable SpawnGroupData data) {
        data = super.finalizeSpawn(level, difficulty, spawnType, data);
        definition.loadout().apply(this, random, level.registryAccess());
        setCanPickUpLoot(false);
        updateCombatGoal();
        return data;
    }

    @Override
    public void aiStep() {
        // Techguns 2 GenericNPCUndead: some undead burn in daylight (not when bound to a spawner, Phase 6f)
        if (definition.burnsInDay() && isAlive() && isSunBurnTick()) {
            igniteForSeconds(8.0f);
        }
        super.aiStep();
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        updateCombatGoal();
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return definition.sounds().ambient;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return definition.sounds().hurt;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return definition.sounds().death;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        SoundEvent step = definition.sounds().step;
        if (step != null) {
            playSound(step, 0.15f, 1.0f);
        } else {
            super.playStepSound(pos, state);
        }
    }
}
