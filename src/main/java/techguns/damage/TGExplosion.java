package techguns.damage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

/**
 * Techguns explosion (Techguns 2 {@code TGExplosion}): full damage within {@code primaryRadius}, falling off to
 * {@code secondaryDamage} at {@code secondaryRadius}; entities behind blocks take no damage. Blocks break along rays
 * when their blast resistance is below the remaining power times {@code blockDamageFactor} (0 = no block damage).
 */
public class TGExplosion {
    /** Ray step length (Techguns 2 value). */
    private static final double STEP = 0.30000001192092896;

    private final ServerLevel level;
    @Nullable
    private final Entity exploder;
    @Nullable
    private final Entity projectile;
    private final Vec3 position;
    private final double primaryDamage;
    private final double secondaryDamage;
    private final double primaryRadius;
    private final double secondaryRadius;
    private final double blockDamageFactor;
    /** Chance (scaled by distance beyond the primary radius) that a destroyed block drops itself. */
    private float blockDropChance = 0.25f;
    private final Explosion explosionDummy;
    /** Entity that takes no damage (the target a projectile already hit directly); may be null. */
    @Nullable
    private Entity excluded;
    /** Seconds entities hit are set on fire for; 0 = none. */
    private int entityFireSeconds;

    public TGExplosion(ServerLevel level, @Nullable Entity exploder, @Nullable Entity projectile, Vec3 position, double primaryDamage,
                       double secondaryDamage, double primaryRadius, double secondaryRadius, double blockDamageFactor) {
        this.level = level;
        this.exploder = exploder;
        this.projectile = projectile;
        this.position = position;
        this.primaryDamage = primaryDamage;
        this.secondaryDamage = secondaryDamage;
        this.primaryRadius = primaryRadius;
        this.secondaryRadius = secondaryRadius;
        this.blockDamageFactor = blockDamageFactor;
        // vanilla blocks and entities expect an Explosion for resistance and explosion-immunity checks
        this.explosionDummy = new Explosion(level, exploder, position.x, position.y, position.z,
                (float) Math.max(primaryRadius, secondaryRadius), false,
                damagesTerrain() ? Explosion.BlockInteraction.DESTROY : Explosion.BlockInteraction.KEEP);
    }

    public TGExplosion setBlockDropChance(float chance) {
        this.blockDropChance = chance;
        return this;
    }

    /** The entity takes no damage from this explosion (Fabric {@code TGExplosionIgnoreBlocks} excluded entities). */
    public TGExplosion setExcludedEntity(@Nullable Entity excluded) {
        this.excluded = excluded;
        return this;
    }

    /** Entities damaged by the blast are set on fire (Fabric {@code setIncendiary}, entity part only). */
    public TGExplosion setEntityFire(int seconds) {
        this.entityFireSeconds = seconds;
        return this;
    }

    private boolean damagesTerrain() {
        return blockDamageFactor >= 0.01;
    }

    /** Power at a distance: primary damage inside the primary radius, then linear towards the secondary radius. */
    private double powerAt(double distance) {
        if (distance <= primaryRadius) {
            return primaryDamage;
        } else if (distance <= secondaryRadius) {
            // Techguns 2 formula (interpolates from secondary at the primary radius back up to primary); kept as is
            return secondaryDamage + ((distance - primaryRadius) / (secondaryRadius - primaryRadius)) * (primaryDamage - secondaryDamage);
        }
        return 0.0;
    }

    /**
     * Breaks blocks and damages entities.
     *
     * @param source damage source for entities hit, usually the projectile's
     * @param playSound play the generic explosion sound
     */
    public void explode(TGDamageSource source, boolean playSound) {
        if (playSound) {
            level.playSound(null, position.x, position.y, position.z, SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS, 4.0f,
                    (1.0f + (level.random.nextFloat() - level.random.nextFloat()) * 0.2f) * 0.7f);
        }
        Map<BlockPos, Double> blocks = damagesTerrain() ? traceBlocks() : Map.of();
        damageEntities(source);
        breakBlocks(blocks);
    }

    private Map<BlockPos, Double> traceBlocks() {
        Map<BlockPos, Double> affected = new HashMap<>();
        int radius = (int) Math.ceil(Math.max(primaryRadius, secondaryRadius));
        int steps = (int) Math.ceil(radius / STEP);
        for (int j = -radius; j < radius; ++j) {
            for (int k = -radius; k < radius; ++k) {
                for (int l = -radius; l < radius; ++l) {
                    if (j != -radius && j != radius - 1 && k != -radius && k != radius - 1 && l != -radius && l != radius - 1) {
                        continue;
                    }
                    Vec3 dir = new Vec3((float) j / radius, (float) k / radius, (float) l / radius).normalize();
                    Vec3 offset = Vec3.ZERO;
                    double damping = 0.0;
                    double power = primaryDamage;
                    BlockPos prevPos = null;
                    for (int i = 0; i < steps && power > 0.0; i++) {
                        BlockPos pos = BlockPos.containing(position.add(offset));
                        BlockState state = level.getBlockState(pos);
                        double distance = position.distanceTo(Vec3.atCenterOf(pos));
                        power = powerAt(distance);
                        if (!state.isAir()) {
                            float resistance = state.getExplosionResistance(level, pos, explosionDummy);
                            if (power - damping > 0.0 && resistance < (power - damping) * blockDamageFactor
                                    && (exploder == null || exploder.shouldBlockExplode(explosionDummy, level, pos, state, (float) power))) {
                                affected.put(pos, distance);
                                if (!pos.equals(prevPos)) {
                                    damping += resistance;
                                }
                            } else {
                                power = 0.0;
                            }
                        }
                        offset = offset.add(dir.scale(STEP));
                        prevPos = pos;
                    }
                }
            }
        }
        return affected;
    }

    private void damageEntities(TGDamageSource source) {
        double total = Math.max(primaryRadius, secondaryRadius);
        AABB box = new AABB(position, position).inflate(total + 1.0);
        List<Entity> entities = level.getEntities(projectile, box);
        for (Entity entity : entities) {
            if (entity == excluded || entity.ignoreExplosion(explosionDummy) || !entity.isAlive() || !isTarget(entity)) {
                continue;
            }
            double distance = position.distanceTo(entity.position().add(0, entity.getEyeHeight(), 0));
            double damage = powerAt(distance);
            if (damage <= 0.0) {
                continue;
            }
            // no damage through walls
            Vec3 end = entity.position().add(0, entity.getEyeHeight() * 0.5, 0);
            if (level.clip(new ClipContext(position, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE,
                            projectile != null ? CollisionContext.of(projectile) : CollisionContext.empty()))
                    .getType() == HitResult.Type.BLOCK) {
                continue;
            }
            entity.hurt(source, (float) Math.max(0, damage));
            if (entityFireSeconds > 0 && !entity.fireImmune()) {
                entity.igniteForSeconds(entityFireSeconds);
            }
        }
    }

    /** Techguns 2 {@code BULLET_TARGETS}: living entities, respecting PvP. */
    private boolean isTarget(Entity entity) {
        if (!(entity instanceof LivingEntity)) {
            return false;
        }
        return !(entity instanceof Player target && exploder instanceof Player owner && owner != target && !owner.canHarmPlayer(target));
    }

    private void breakBlocks(Map<BlockPos, Double> blocks) {
        double falloff = secondaryRadius - primaryRadius;
        for (Map.Entry<BlockPos, Double> entry : blocks.entrySet()) {
            BlockPos pos = entry.getKey();
            BlockState state = level.getBlockState(pos);
            if (state.isAir()) {
                continue;
            }
            if (falloff > 0 && entry.getValue() > primaryRadius
                    && level.random.nextFloat() < blockDropChance * (entry.getValue() - primaryRadius) / falloff
                    && state.canDropFromExplosion(level, pos, explosionDummy)) {
                Block.dropResources(state, level, pos, level.getBlockEntity(pos), exploder, net.minecraft.world.item.ItemStack.EMPTY);
            }
            state.onBlockExploded(level, pos, explosionDummy);
        }
    }
}
