package techguns.client.particle;

import java.awt.Color;
import java.util.List;

import org.joml.Matrix4f;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import techguns.client.particle.TGParticleSystemType.AlphaEntry;
import techguns.client.particle.TGParticleSystemType.ColorEntry;
import techguns.client.render.TGRenderTypes;

/**
 * An actual spawned particle (a camera facing quad). Standalone port of the Fabric class, which extended the
 * vanilla {@code Particle}; the vanilla particle engine is not involved, the {@link TGParticleManager} ticks and
 * renders these.
 */
public class TGParticle implements ITGParticle {
    /** Fixed lightmap value of Fabric's {@code .light(240, 255)}: full bright. */
    private static final int FULL_BRIGHT = 0x00FF00F0;

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

    protected int age;
    protected int maxAge;
    protected boolean alive = true;
    protected boolean onGround;
    protected boolean collidesWithWorld;
    protected AABB boundingBox;

    protected float red = 1.0f;
    protected float green = 1.0f;
    protected float blue = 1.0f;
    protected float alpha = 1.0f;

    int lifetime;

    float angle;
    float angleRate;
    float angleRateDamping;
    float angleRateDampingOnGround;

    float size;
    float sizePrev;
    float sizeRate;
    float sizeRateDamping;

    float animationSpeed;

    double velX;
    double velY;
    double velZ;
    float velocityDamping;
    float velocityDampingOnGround;

    float systemVelocityFactor;

    TGParticleSystem particleSystem;
    final TGParticleSystemType type;

    int variationFrame;

    protected double depth;

    float brightness;
    Vec3 worldLightColor;
    Vec3 worldLightColorPrev;

    public TGParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed,
                      TGParticleSystem particleSystem) {
        this.level = level;
        setPos(x, y, z);
        this.prevPosX = x;
        this.prevPosY = y;
        this.prevPosZ = z;
        this.collidesWithWorld = false;

        this.velocityX = xSpeed;
        this.velocityY = ySpeed;
        this.velocityZ = zSpeed;

        this.particleSystem = particleSystem;
        this.type = particleSystem.type;
        this.init();
    }

    private void init() {
        this.lifetime = FxMath.randomInt(random, type.lifetimeMin, type.lifetimeMax);
        this.maxAge = lifetime;
        this.size = FxMath.randomFloat(random, type.sizeMin, type.sizeMax) * this.particleSystem.scale;
        this.size += this.particleSystem.startSize;
        this.sizeRate = FxMath.randomFloat(random, type.sizeRateMin, type.sizeRateMax) * this.particleSystem.scale;
        this.sizeRateDamping = FxMath.randomFloat(random, type.sizeRateDampingMin, type.sizeRateDampingMax);
        this.animationSpeed = FxMath.randomFloat(random, type.animationSpeedMin, type.animationSpeedMax);
        this.velocityDamping = FxMath.randomFloat(random, type.velocityDampingMin, type.velocityDampingMax);
        this.systemVelocityFactor = FxMath.randomFloat(random, type.systemVelocityFactorMin, type.systemVelocityFactorMax);
        this.velocityDampingOnGround = FxMath.randomFloat(random, type.velocityDampingOnGroundMin, type.velocityDampingOnGroundMax);

        this.angle = FxMath.randomFloat(random, type.angleMin, type.angleMax);
        this.angleRate = FxMath.randomFloat(random, type.angleRateMin, type.angleRateMax);
        this.angleRateDamping = FxMath.randomFloat(random, type.angleRateDampingMin, type.angleRateDampingMax);
        this.angleRateDampingOnGround = FxMath.randomFloat(random, type.angleRateDampingOnGroundMin, type.angleRateDampingOnGroundMax);

        this.velocityX += systemVelocityFactor * particleSystem.motionX();
        this.velocityY += systemVelocityFactor * particleSystem.motionY();
        this.velocityZ += systemVelocityFactor * particleSystem.motionZ();

        this.velX = this.velocityX;
        this.velY = this.velocityY;
        this.velZ = this.velocityZ;

        this.variationFrame = random.nextInt(type.frames);
    }

    /** Sets the position and recenters the (0.2 x 0.2) bounding box like the vanilla particle does. */
    protected void setPos(double px, double py, double pz) {
        this.x = px;
        this.y = py;
        this.z = pz;
        this.boundingBox = new AABB(px - 0.1, py, pz - 0.1, px + 0.1, py + 0.2, pz + 0.1);
    }

    protected void markDead() {
        this.alive = false;
        this.particleSystem = null;
    }

    public void onUpdate() {
        this.prevPosX = this.x;
        this.prevPosY = this.y;
        this.prevPosZ = this.z;

        this.sizePrev = this.size;

        lifetime--;
        if (this.age++ >= this.maxAge) {
            this.markDead();
            return;
        }

        // Move with the system
        if (this.type.particlesStickToSystem) {
            if (this.particleSystem.entity != null) {
                Entity entity = this.particleSystem.entity;
                if (!entity.isAlive()) {
                    this.markDead();
                    return;
                }

                if (this.type.particlesMoveWithSystem && this.particleSystem.attachToHead && entity instanceof LivingEntity living) {
                    double p = living.getXRot() * FxMath.D2R;
                    double yaw = living.yHeadRot * FxMath.D2R;

                    double prevP = living.xRotO * FxMath.D2R;
                    double prevY = living.yHeadRotO * FxMath.D2R;

                    Vec3 offsetBase = this.particleSystem.type.offset;
                    if (this.particleSystem.entityOffset != null) {
                        offsetBase = offsetBase.add(this.particleSystem.entityOffset);
                    }

                    Vec3 offset = offsetBase.xRot((float) -p);
                    offset = offset.yRot((float) -yaw);

                    Vec3 offsetP = offsetBase.xRot((float) -prevP);
                    offsetP = offsetP.yRot((float) -prevY);

                    float eyeHeight = living.getEyeHeight(living.getPose());
                    this.prevPosX = entity.xo + offsetP.x;
                    this.prevPosY = entity.yo + eyeHeight + offsetP.y;
                    this.prevPosZ = entity.zo + offsetP.z;
                    this.x = entity.getX() + offset.x;
                    this.y = entity.getY() + eyeHeight + offset.y;
                    this.z = entity.getZ() + offset.z;
                } else {
                    this.prevPosX = entity.xo;
                    this.prevPosY = entity.yo;
                    this.prevPosZ = entity.zo;
                    this.x = entity.getX();
                    this.y = entity.getY();
                    this.z = entity.getZ();
                }
            } else {
                if (!this.particleSystem.isAlive()) {
                    this.markDead();
                    return;
                }
                this.x = this.particleSystem.posX();
                this.y = this.particleSystem.posY();
                this.z = this.particleSystem.posZ();
            }
        } else if (this.type.particlesMoveWithSystem) {
            double dP = (this.particleSystem.rotationPitch - this.particleSystem.prevRotationPitch) * FxMath.D2R;
            double dY = (this.particleSystem.rotationYaw - this.particleSystem.prevRotationYaw) * FxMath.D2R;

            Vec3 pos = new Vec3(this.x, this.y, this.z);
            Vec3 sysPos = new Vec3(this.particleSystem.posX(), this.particleSystem.posY(), this.particleSystem.posZ());

            Vec3 offset = sysPos.subtract(pos);
            offset = offset.yRot((float) -dY);
            offset = offset.xRot((float) -dP);

            Vec3 motion = new Vec3(this.velocityX, this.velocityY, this.velocityZ);
            motion = motion.yRot((float) -dY);
            motion = motion.xRot((float) -dP);

            this.x = sysPos.x + offset.x;
            this.y = sysPos.y + offset.y;
            this.z = sysPos.z + offset.z;

            this.velocityX = motion.x;
            this.velocityY = motion.y;
            this.velocityZ = motion.z;
        }

        // Motion
        if (!this.onGround) {
            this.velY -= type.gravity * 0.1D;
        }

        this.checkWorldCollision(velX, velY, velZ);

        this.velocityX = velX;
        this.velocityY = velY;
        this.velocityZ = velZ;

        this.setPos(this.x + this.velocityX, this.y + this.velocityY, this.z + this.velocityZ);

        this.velX *= velocityDamping;
        this.velY *= velocityDamping;
        this.velZ *= velocityDamping;

        if (this.onGround) {
            this.angleRate *= angleRateDampingOnGround;

            this.velX *= velocityDampingOnGround;
            this.velY *= velocityDampingOnGround;
            this.velZ *= velocityDampingOnGround;
            if (type.removeOnGround) {
                this.markDead();
            }
        }

        // Size
        size = Math.max(0.0f, size + sizeRate);
        sizeRate *= sizeRateDamping;

        // Angle
        angle = (angle + angleRate) % 360.0f;
        angleRate *= angleRateDamping;

        // Brightness
        this.worldLightColorPrev = this.worldLightColor;
        this.worldLightColor = lightColorAt(this.level, this.x, this.y, this.z);
        if (this.worldLightColorPrev == null) {
            this.worldLightColorPrev = worldLightColor;
        }
    }

    /**
     * World light at a position as an RGB multiplier. Simplification: the Fabric version sampled the lightmap
     * texture; this uses the block and (darkened) sky light levels as a grey value.
     */
    static Vec3 lightColorAt(ClientLevel level, double px, double py, double pz) {
        BlockPos pos = BlockPos.containing(px, py, pz);
        int blockLight = level.getBrightness(LightLayer.BLOCK, pos);
        int skyLight = level.getBrightness(LightLayer.SKY, pos) - level.getSkyDarken();
        double v = Mth.clamp(Math.max(blockLight, Math.max(skyLight, 0)) / 15.0, 0.05, 1.0);
        return new Vec3(v, v, v);
    }

    /** Packed lightmap coordinates at a position (for the 3D particles). */
    static int packedLightAt(ClientLevel level, double px, double py, double pz) {
        BlockPos pos = BlockPos.containing(px, py, pz);
        return net.minecraft.client.renderer.LightTexture.pack(level.getBrightness(LightLayer.BLOCK, pos), level.getBrightness(LightLayer.SKY, pos));
    }

    protected void checkWorldCollision(double dx, double dy, double dz) {
        double dy1 = dy;
        if (this.collidesWithWorld && (dx != 0.0D || dy != 0.0D || dz != 0.0D)) {
            Vec3 adjusted = Entity.collideBoundingBox(null, new Vec3(dx, dy, dz), this.boundingBox, this.level, List.of());
            dy = adjusted.y;
        }
        if (dy1 != dy && dy1 < 0.0D) {
            this.onGround = true;
        }
    }

    /** Draws the particle as a billboard quad. */
    public void renderParticle(MultiBufferSource.BufferSource buffers, float partialTick, BillboardAxes axes, Matrix4f mat, Camera camera) {
        float progress = ((float) this.age + partialTick) / (float) this.maxAge;

        preRenderStep(progress, partialTick);

        // Animation
        int currentFrame;
        if (type.hasVariations) {
            currentFrame = variationFrame;
        } else {
            currentFrame = ((int) ((float) type.frames * (progress * this.animationSpeed))) % type.frames;
        }

        float fscale = 0.1F * this.size;

        Vec3 camPos = camera.getPosition();
        float fPosX = (float) (Mth.lerp((double) partialTick, this.prevPosX, this.x) - camPos.x);
        float fPosY = (float) (Mth.lerp((double) partialTick, this.prevPosY, this.y) - camPos.y);
        float fPosZ = (float) (Mth.lerp((double) partialTick, this.prevPosZ, this.z) - camPos.z);

        int col = currentFrame % type.columns;
        int row = currentFrame / type.columns;

        float u = 1.f / type.columns;
        float v = 1.f / type.columns;
        float u1 = col * u;
        float v1 = row * v;
        float u2 = (col + 1) * u;
        float v2 = (row + 1) * v;

        RenderType layer = layerFor(this.type);
        VertexConsumer buffer = buffers.getBuffer(layer);

        double a = (angle + (partialTick * angleRate)) * FxMath.D2R;
        Vec3 p1;
        Vec3 p2;
        Vec3 p3;
        Vec3 p4;

        if (this.type.groundAligned) {
            float s = fscale;
            p1 = new Vec3(-s, 0, -s);
            p2 = new Vec3(s, 0, -s);
            p3 = new Vec3(s, 0, s);
            p4 = new Vec3(-s, 0, s);
            if (a > 0.0001f) {
                p1 = p1.yRot((float) a);
                p2 = p2.yRot((float) a);
                p3 = p3.yRot((float) a);
                p4 = p4.yRot((float) a);
            }
        } else {
            float rotX = axes.rotX();
            float rotZ = axes.rotZ();
            float rotYZ = axes.rotYZ();
            float rotXY = axes.rotXY();
            float rotXZ = axes.rotXZ();
            p1 = new Vec3(-rotX * fscale - rotXY * fscale, -rotZ * fscale, -rotYZ * fscale - rotXZ * fscale);
            p2 = new Vec3(-rotX * fscale + rotXY * fscale, +rotZ * fscale, -rotYZ * fscale + rotXZ * fscale);
            p3 = new Vec3(rotX * fscale + rotXY * fscale, +rotZ * fscale, +rotYZ * fscale + rotXZ * fscale);
            p4 = new Vec3(rotX * fscale - rotXY * fscale, -rotZ * fscale, +rotYZ * fscale - rotXZ * fscale);

            if (a > 0.0001f) {
                Vec3 axis = p1.normalize().cross(p2.normalize());
                double cosa = Math.cos(a);
                double sina = Math.sin(a);

                p1 = rotAxis(p1, axis, sina, cosa);
                p2 = rotAxis(p2, axis, sina, cosa);
                p3 = rotAxis(p3, axis, sina, cosa);
                p4 = rotAxis(p4, axis, sina, cosa);
            }
        }

        vertex(buffer, mat, p1, fPosX, fPosY, fPosZ, u2, v2);
        vertex(buffer, mat, p2, fPosX, fPosY, fPosZ, u2, v1);
        vertex(buffer, mat, p3, fPosX, fPosY, fPosZ, u1, v1);
        vertex(buffer, mat, p4, fPosX, fPosY, fPosZ, u1, v2);
    }

    private void vertex(VertexConsumer buffer, Matrix4f mat, Vec3 p, float ox, float oy, float oz, float u, float v) {
        buffer.addVertex(mat, (float) p.x + ox, (float) p.y + oy, (float) p.z + oz)
                .setColor(this.red, this.green, this.blue, this.alpha).setUv(u, v).setLight(FULL_BRIGHT);
    }

    /** Render type of a particle system type: additive FX use the additive shader layer, everything else is alpha blended. */
    static RenderType layerFor(TGParticleSystemType type) {
        return switch (type.renderType) {
            case ADDITIVE -> TGRenderTypes.fxAdditive(type.texture);
            case SCOPE -> TGRenderTypes.scope(type.texture);
            default -> TGRenderTypes.fxAlpha(type.texture);
        };
    }

    /** Interpolates colors, brightness and alpha over the particle's life. */
    protected void preRenderStep(float progress, float partialTick) {
        // brightness
        AlphaEntry b1;
        AlphaEntry b2 = null;
        if (type.brightnessEntries.isEmpty()) {
            // Backwards compatibility: If no brightness is specified, use 1.0 for additive and 0.0 for the others
            this.brightness = switch (this.type.renderType) {
                case ADDITIVE -> 1.0f;
                default -> 0.0f;
            };
        } else if (type.brightnessEntries.size() == 1) {
            b1 = type.brightnessEntries.get(0);
            this.brightness = b1.alpha;
        } else {
            b1 = type.brightnessEntries.get(0);
            for (int i = 1; i < type.brightnessEntries.size(); i++) {
                b2 = type.brightnessEntries.get(i);
                if (progress < b2.time) {
                    break;
                } else {
                    b1 = b2;
                }
            }
            if (b1.time != b2.time) {
                float p = (progress - b1.time) / (b2.time - b1.time);
                this.brightness = b1.alpha * (1f - p) + b2.alpha * p;
            } else {
                this.brightness = b1.alpha;
            }
        }

        float bR;
        float bG;
        float bB;
        if (this.brightness < 1.0f) {
            float wbR = Mth.lerp(partialTick, (float) this.worldLightColorPrev.x, (float) this.worldLightColor.x);
            float wbG = Mth.lerp(partialTick, (float) this.worldLightColorPrev.y, (float) this.worldLightColor.y);
            float wbB = Mth.lerp(partialTick, (float) this.worldLightColorPrev.z, (float) this.worldLightColor.z);
            bR = this.brightness + (1f - this.brightness) * wbR;
            bG = this.brightness + (1f - this.brightness) * wbG;
            bB = this.brightness + (1f - this.brightness) * wbB;
        } else {
            bR = 1.0f;
            bG = 1.0f;
            bB = 1.0f;
        }

        // color
        ColorEntry c1;
        ColorEntry c2 = null;
        if (type.colorEntries.isEmpty()) {
            c1 = new ColorEntry(1.0f, 1.0f, 1.0f, 0);
            c2 = c1;
        } else if (type.colorEntries.size() == 1) {
            c1 = type.colorEntries.get(0);
            c2 = c1;
        } else {
            c1 = type.colorEntries.get(0);
            for (int i = 1; i < type.colorEntries.size(); i++) {
                c2 = type.colorEntries.get(i);
                if (progress < c2.time) {
                    break;
                } else {
                    c1 = c2;
                }
            }
        }
        float p = (progress - c1.time) / (c2.time - c1.time);
        if (c1 != c2) {
            // interpolate in HSB space
            float[] hsb1 = Color.RGBtoHSB((int) (c1.r * 255), (int) (c1.g * 255), (int) (c1.b * 255), null);
            float[] hsb2 = Color.RGBtoHSB((int) (c2.r * 255), (int) (c2.g * 255), (int) (c2.b * 255), null);
            int rgb = Color.HSBtoRGB(hsb1[0] * (1f - p) + hsb2[0] * p, hsb1[1] * (1f - p) + hsb2[1] * p, hsb1[2] * (1f - p) + hsb2[2] * p);
            this.red = (((rgb >> 16) & 0xFF) / 255.0f) * bR;
            this.green = (((rgb >> 8) & 0xFF) / 255.0f) * bG;
            this.blue = ((rgb & 0xFF) / 255.0f) * bB;
        } else {
            this.red = c1.r * bR;
            this.green = c1.g * bG;
            this.blue = c1.b * bB;
        }

        // alpha
        AlphaEntry a1;
        AlphaEntry a2 = null;
        if (type.alphaEntries.isEmpty()) {
            this.alpha = 1.0f;
        } else if (type.alphaEntries.size() == 1) {
            a1 = type.alphaEntries.get(0);
            this.alpha = a1.alpha;
        } else {
            a1 = type.alphaEntries.get(0);
            for (int i = 1; i < type.alphaEntries.size(); i++) {
                a2 = type.alphaEntries.get(i);
                if (progress < a2.time) {
                    break;
                } else {
                    a1 = a2;
                }
            }
            if (a1.time != a2.time) {
                p = (progress - a1.time) / (a2.time - a1.time);
                this.alpha = a1.alpha * (1f - p) + a2.alpha * p;
            } else {
                this.alpha = a1.alpha;
            }
        }
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

    public boolean isAlive() {
        return alive;
    }

    protected Vec3 rotAxis(Vec3 p1, Vec3 axis, double sina, double cosa) {
        Vec3 v1 = axis.cross(p1);
        double d1 = axis.dot(p1);
        return p1.scale(cosa).add(v1.scale(sina)).add(axis.scale(d1 * (1.0 - cosa)));
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
    public void doRender(MultiBufferSource.BufferSource buffers, Camera camera, float partialTick, BillboardAxes axes, PoseStack poseStack) {
        this.renderParticle(buffers, partialTick, axes, new Matrix4f(poseStack.last().pose()), camera);
    }

    @Override
    public double getDepth() {
        return this.depth;
    }

    @Override
    public void setDepth(double depth) {
        this.depth = depth;
    }
}
