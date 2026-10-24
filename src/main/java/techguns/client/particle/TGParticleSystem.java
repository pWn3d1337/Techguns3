package techguns.client.particle;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Camera;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import techguns.client.particle.TGParticleSystemType.DirResult;
import techguns.core.fx.EntityCondition;

/**
 * A particle system which spawns particles. Does not render anything itself; it is ticked by the
 * {@link TGParticleManager} like the particles. Standalone port of the Fabric class (which extended the vanilla
 * {@code Particle}).
 */
public class TGParticleSystem implements ITGParticle {
    public TGParticleSystemType type;

    public EntityCondition condition = EntityCondition.NONE;

    /** Models for 3D particle systems (set by the creator of the system). */
    public ArrayList<ModelPart> models;
    public ResourceLocation modelsTexture;

    protected final ClientLevel level;
    protected final RandomSource random = RandomSource.create();

    protected double x;
    protected double y;
    protected double z;
    protected double prevPosX;
    protected double prevPosY;
    protected double prevPosZ;
    protected double velocityX;
    protected double velocityY;
    protected double velocityZ;
    protected boolean alive = true;

    int systemLifetime;
    int spawnDelay = 0;

    /** Global scale. */
    public float scale = 1.0f;
    public float rotationYaw;
    public float rotationPitch;

    public float prevRotationYaw;
    public float prevRotationPitch;

    public int initialDelay;
    public int ticksExisted = 0;

    public float startSizeRate;
    public float startSizeRateDamping;
    public float startSize = 0.0f;

    protected TGParticleStreak prevParticle = null;

    /** The type of the system attached to each spawned particle. */
    @Nullable
    public TGFXType attachedFXType = null;

    /** Parent entity (if attached to an entity). */
    @Nullable
    Entity entity;
    public boolean attachToHead = false;
    @Nullable
    public Vec3 entityOffset = null;
    /** Parent particle (if attached to a particle). */
    @Nullable
    TGParticle parent;

    public TGParticleSystem(ClientLevel level, TGParticleSystemType type, double x, double y, double z,
                            double xSpeed, double ySpeed, double zSpeed) {
        this.level = level;
        this.x = x + type.offset.x;
        this.y = y + type.offset.y;
        this.z = z + type.offset.z;
        this.prevPosX = this.x;
        this.prevPosY = this.y;
        this.prevPosZ = this.z;
        this.velocityX = xSpeed;
        this.velocityY = ySpeed;
        this.velocityZ = zSpeed;
        this.type = type;
        this.init();
    }

    public TGParticleSystem(Entity entity, TGParticleSystemType type) {
        this((ClientLevel) entity.level(), type, entity.getX(), entity.getY(), entity.getZ(), 0, 0, 0);
        this.entity = entity;
    }

    public TGParticleSystem(ClientLevel level, TGParticle part, TGParticleSystemType type) {
        this(level, type, part.posX(), part.posY(), part.posZ(), 0, 0, 0);
        this.parent = part;
    }

    protected void init() {
        this.systemLifetime = FxMath.randomInt(this.random, type.systemLifetimeMin, type.systemLifetimeMax);
        this.initialDelay = FxMath.randomInt(this.random, type.initialDelayMin, type.initialDelayMax);
        this.startSizeRate = FxMath.randomFloat(this.random, type.startSizeRateMin, type.startSizeRateMax) * this.scale;
        this.startSizeRateDamping = FxMath.randomFloat(this.random, type.startSizeRateDampingMin, type.startSizeRateDampingMax);
        if (this.type.attachedSystem != null && !this.type.attachedSystem.isEmpty()) {
            this.attachedFXType = TGFX.FX_LIST.get(this.type.attachedSystem.toLowerCase(java.util.Locale.ROOT));
        }
    }

    public void onUpdate() {
        this.prevRotationPitch = this.rotationPitch;
        this.prevRotationYaw = this.rotationYaw;

        this.prevPosX = this.x;
        this.prevPosY = this.y;
        this.prevPosZ = this.z;

        if (type == null) {
            this.markDead();
            return;
        }

        if (this.entity != null) {
            if (!condition.evaluate(entity)) {
                this.markDead();
                return;
            }

            if (this.attachToHead && entity instanceof LivingEntity living) {
                this.rotationPitch = living.getXRot();
                this.rotationYaw = living.yHeadRot;

                Vec3 offset = type.offset;
                if (this.entityOffset != null) {
                    offset = offset.add(this.entityOffset);
                }

                offset = offset.xRot((float) (-living.getXRot() * FxMath.D2R));
                offset = offset.yRot((float) ((-living.yHeadRot) * FxMath.D2R));

                this.x = living.xo + offset.x;
                this.y = living.yo + living.getEyeHeight(living.getPose()) + offset.y;
                this.z = living.zo + offset.z;
            } else {
                this.rotationPitch = entity.getXRot();
                this.rotationYaw = entity.getYRot();

                Vec3 offset = type.offset;
                if (this.entityOffset != null) {
                    offset = offset.add(this.entityOffset);
                }

                offset = offset.xRot((float) (-entity.getXRot() * FxMath.D2R));
                offset = offset.yRot((float) ((-entity.getYRot()) * FxMath.D2R));

                this.x = entity.xo + offset.x;
                this.y = entity.yo + offset.y;
                this.z = entity.zo + offset.z;
            }
            Vec3 motion = entity.getDeltaMovement();
            this.velocityX = motion.x;
            this.velocityY = motion.y;
            this.velocityZ = motion.z;

        } else if (this.parent != null) {
            this.x = parent.posX() + type.offset.x;
            this.y = parent.posY() + type.offset.y;
            this.z = parent.posZ() + type.offset.z;
        } else {
            this.x += velocityX;
            this.y += velocityY;
            this.z += velocityZ;
        }

        if (initialDelay-- > 0) {
            return;
        }

        this.startSize = this.startSize + this.startSizeRate;
        this.startSizeRate = this.startSizeRate * this.startSizeRateDamping;

        if (spawnDelay-- <= 0) {
            int count = FxMath.randomInt(random, type.particleCountMin, type.particleCountMax);

            for (int i = 0; i < count; i++) {
                // Get position and motion data
                DirResult dir = this.type.new DirResult();
                Vec3 position = type.volumeType.getPosition(this, dir, i, count);
                Vec3 motion = type.velocityType.getVelocity(this, dir.values);

                motion = motion.scale(this.scale);
                position = position.scale(this.scale);

                // apply the ParticleSystem's entity rotation
                motion = motion.xRot((float) (-this.rotationPitch * FxMath.D2R));
                motion = motion.yRot((float) ((-this.rotationYaw) * FxMath.D2R));
                if (type.volumeType != TGParticleSystemType.VOL_TRAIL) {
                    position = position.xRot((float) (-this.rotationPitch * FxMath.D2R));
                    position = position.yRot((float) ((-this.rotationYaw) * FxMath.D2R));
                }

                // Spawn particle
                double mf = 0.05D; // per second instead of per tick
                TGParticle particle = null;
                if (this.type.is3d) {
                    if (this.models != null && this.modelsTexture != null && !this.models.isEmpty()) {
                        ModelPart model = this.models.get(i % this.models.size());
                        TGParticle3D particle3d = new TGParticle3D(this.level, this.x + position.x, this.y + position.y, this.z + position.z,
                                motion.x * mf, motion.y * mf, motion.z * mf, this, model, this.modelsTexture);
                        addEffect(particle3d);
                        particle = particle3d;
                    }
                    // TODO: 3D and Streak combination
                } else if (this.type.streak) {
                    TGParticleStreak particleStreak = new TGParticleStreak(this.level, this.x + position.x, this.y + position.y, this.z + position.z,
                            motion.x * mf, motion.y * mf, motion.z * mf, this);
                    if (prevParticle != null) {
                        particleStreak.setPrev(prevParticle);
                        prevParticle.setNext(particleStreak);
                    }
                    addEffect(particleStreak);
                    prevParticle = particleStreak;
                    particle = particleStreak;
                } else {
                    particle = new TGParticle(this.level, this.x + position.x, this.y + position.y, this.z + position.z,
                            motion.x * mf, motion.y * mf, motion.z * mf, this);
                    addEffect(particle);
                }
                if (particle != null && this.attachedFXType != null) {
                    List<TGParticleSystem> systems = this.attachedFXType.createParticleSystemsOnParticle(this.level, particle);
                    if (systems != null) {
                        for (TGParticleSystem s : systems) {
                            s.scale = this.scale;
                            addEffect(s);
                        }
                    }
                }
            }
            spawnDelay = FxMath.randomInt(this.random, type.spawnDelayMin, type.spawnDelayMax);
        }

        if (this.entity != null) {
            if (!this.entity.isAlive()) {
                this.markDead();
            } else if (this.systemLifetime > 0 && ticksExisted >= this.systemLifetime) {
                this.markDead();
            }
        } else if (this.parent != null) {
            if (!this.parent.isAlive()) {
                this.markDead();
            }
        } else {
            if (ticksExisted >= this.systemLifetime) {
                this.markDead();
            }
        }
        ticksExisted++;
    }

    protected void addEffect(ITGParticle particle) {
        TGParticleManager.get().addEffect(particle);
    }

    protected void markDead() {
        this.alive = false;
        this.entity = null;
    }

    public boolean isAlive() {
        return alive;
    }

    public double posX() {
        return x;
    }

    public double posY() {
        return y;
    }

    public double posZ() {
        return z;
    }

    public double motionX() {
        return velocityX;
    }

    public double motionY() {
        return velocityY;
    }

    public double motionZ() {
        return velocityZ;
    }

    @Override
    public Vec3 getPos() {
        return new Vec3(this.x, this.y, this.z);
    }

    @Override
    public boolean shouldRemove() {
        return !this.alive;
    }

    @Override
    public void updateTick() {
        this.onUpdate();
    }

    @Override
    public double getDepth() {
        return 0;
    }

    @Override
    public void setDepth(double depth) {
    }

    @Override
    public void doRender(MultiBufferSource.BufferSource buffers, Camera camera, float partialTick, BillboardAxes axes, PoseStack poseStack) {
        // a system does not render anything itself
    }
}
