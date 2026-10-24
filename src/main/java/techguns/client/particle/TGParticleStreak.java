package techguns.client.particle;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import techguns.client.render.TGRenderTypes;

/**
 * One segment of a streak (a ribbon between consecutive particles of one system, e.g. missile trails). Each
 * segment draws the quad between itself and its predecessor.
 */
public class TGParticleStreak extends TGParticle {
    /** Sky light only (the Fabric version used light(0, 240)). */
    private static final int STREAK_LIGHT = 0x00F00000;

    protected TGParticleStreak prev;
    protected TGParticleStreak next;

    /** This streak segment's vertices. */
    protected Vec3 pos1;
    protected Vec3 pos2;
    protected float particleScale = 1.0f;

    public TGParticleStreak(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed,
                            TGParticleSystem particleSystem) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed, particleSystem);
    }

    @Override
    public void renderParticle(MultiBufferSource.BufferSource buffers, float partialTick, BillboardAxes axes, Matrix4f mat, Camera camera) {
        float progress = ((float) this.age + partialTick) / (float) this.maxAge;

        preRenderStep(progress, partialTick);

        if (this.next == null) {
            this.alpha = 0.0f;
        }

        int currentFrame;
        if (type.hasVariations) {
            currentFrame = variationFrame;
        } else {
            currentFrame = ((int) ((float) type.frames * (progress * this.animationSpeed))) % type.frames;
        }

        this.particleScale = sizePrev + (size - sizePrev) * partialTick;
        float fscale = 0.1F * this.particleScale;

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

        if (prev == null) {
            this.pos1 = null;
            this.pos2 = null;
            return;
        }

        Vector3f look = camera.getLookVector();
        Vec3 viewDir = new Vec3(look.x(), look.y(), look.z());

        float prevPosX = (float) (Mth.lerp((double) partialTick, this.prev.prevPosX, this.prev.x) - camPos.x);
        float prevPosY = (float) (Mth.lerp((double) partialTick, this.prev.prevPosY, this.prev.y) - camPos.y);
        float prevPosZ = (float) (Mth.lerp((double) partialTick, this.prev.prevPosZ, this.prev.z) - camPos.z);
        Vec3 vPrev = new Vec3(prevPosX, prevPosY, prevPosZ);

        Vec3 vDir = vPrev.subtract(fPosX, fPosY, fPosZ).normalize();
        Vec3 vCross = viewDir.cross(vDir).normalize();

        Vec3 p1 = new Vec3(vCross.x * fscale + fPosX, vCross.y * fscale + fPosY, vCross.z * fscale + fPosZ);
        Vec3 p2 = new Vec3(vCross.x * -fscale + fPosX, vCross.y * -fscale + fPosY, vCross.z * -fscale + fPosZ);

        this.pos1 = p1;
        this.pos2 = p2;

        // streaks are blended like the other additive FX (the Fabric version used a separate "lightning" layer)
        RenderType layer = TGRenderTypes.fxAdditive(type.texture);
        VertexConsumer buffer = buffers.getBuffer(layer);

        float fscaleP = prev.particleScale * 0.1f;

        Vec3 p3;
        Vec3 p4;
        if (prev.pos1 != null && prev.pos2 != null) {
            p3 = prev.pos2;
            p4 = prev.pos1;
        } else {
            p4 = new Vec3(vCross.x * fscaleP + vPrev.x, vCross.y * fscaleP + vPrev.y, vCross.z * fscaleP + vPrev.z);
            p3 = new Vec3(vCross.x * -fscaleP + vPrev.x, vCross.y * -fscaleP + vPrev.y, vCross.z * -fscaleP + vPrev.z);

            prev.pos1 = p4;
            prev.pos2 = p3;
        }

        buffer.addVertex(mat, (float) p1.x, (float) p1.y, (float) p1.z).setColor(this.red, this.green, this.blue, this.alpha).setUv(u2, v2).setLight(STREAK_LIGHT);
        buffer.addVertex(mat, (float) p2.x, (float) p2.y, (float) p2.z).setColor(this.red, this.green, this.blue, this.alpha).setUv(u2, v1).setLight(STREAK_LIGHT);
        buffer.addVertex(mat, (float) p3.x, (float) p3.y, (float) p3.z).setColor(prev.red, prev.green, prev.blue, prev.alpha).setUv(u1, v1).setLight(STREAK_LIGHT);
        buffer.addVertex(mat, (float) p4.x, (float) p4.y, (float) p4.z).setColor(prev.red, prev.green, prev.blue, prev.alpha).setUv(u1, v2).setLight(STREAK_LIGHT);
    }

    public TGParticleStreak getPrev() {
        return prev;
    }

    public void setPrev(TGParticleStreak prev) {
        this.prev = prev;
    }

    public TGParticleStreak getNext() {
        return next;
    }

    public void setNext(TGParticleStreak next) {
        this.next = next;
    }

    @Override
    public boolean doNotSort() {
        return true;
    }
}
