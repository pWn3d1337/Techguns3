package techguns.client.render.entity;

import java.util.EnumMap;
import java.util.Map;
import java.util.Random;

import org.joml.Matrix4f;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import techguns.TGIds;
import techguns.client.render.TGMatrixOps;
import techguns.client.render.TGRenderTypes;
import techguns.entity.projectile.GenericBeamProjectile;
import techguns.entity.projectile.GenericBeamProjectile.BeamType;

/**
 * Renders hit-scan beams (laser, NDR, tesla): crossed additive quads from the muzzle to the hit point
 * (Techguns 2 beam renderer). The entity sits at the muzzle and its rotation is the beam direction.
 */
public class BeamProjectileRenderer extends EntityRenderer<GenericBeamProjectile> {
    private static final int FULL_BRIGHT = 0xF000F0;

    private static final Map<BeamType, BeamParams> PARAMS = new EnumMap<>(BeamType.class);

    static {
        PARAMS.put(BeamType.NDR, new BeamParams("textures/fx/nukebeam.png", 17, 0.5f, 2.0f, true, false));
        PARAMS.put(BeamType.LASER, new BeamParams("textures/fx/laser3_combined.png", 1, 1.0f, 2.0f, false, true));
        PARAMS.put(BeamType.TESLA, new BeamParams("textures/fx/laser_blue.png", 1, 1.0f, 2.0f, false, false));
    }

    public BeamProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(GenericBeamProjectile entity) {
        return PARAMS.get(entity.beamType()).texture;
    }

    private RenderType renderType(GenericBeamProjectile entity) {
        return TGRenderTypes.fxAdditive(getTextureLocation(entity));
    }

    @Override
    public boolean shouldRender(GenericBeamProjectile entity, Frustum frustum, double camX, double camY, double camZ) {
        // the beam starts at the muzzle and may be long; do not cull it by the (stale) bounding box
        return entity.length() > 0;
    }

    @Override
    public void render(GenericBeamProjectile entity, float entityYaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffers, int packedLight) {
        float distance = (float) entity.length();
        if (distance <= 0f) {
            return;
        }
        BeamParams params = PARAMS.get(entity.beamType());
        Random rand = new Random(entity.getId());
        float prog = entity.age(partialTick);

        float yaw = Mth.lerp(partialTick, entity.yRotO, entity.getYRot());
        float pitch = Mth.lerp(partialTick, entity.xRotO, entity.getXRot());

        poseStack.pushPose();
        TGMatrixOps.rotate(poseStack, -(yaw - 90.0f), 0f, 1f, 0f);
        TGMatrixOps.rotate(poseStack, pitch, 0f, 0f, 1f);
        if (params.spinning) {
            TGMatrixOps.rotate(poseStack, 360.0f * prog, 1f, 0f, 0f);
        }

        float[] uv = params.uv(distance, entity.tickCount, partialTick);
        float u1 = uv[0], v1 = uv[1], u2 = uv[2], v2 = uv[3];
        VertexConsumer consumer = buffers.getBuffer(renderType(entity));

        switch (entity.beamType()) {
            case NDR -> {
                renderBeam(params, consumer, prog, 0.05f, distance, u1, u2, v1, v2, poseStack);
                renderSpiral(consumer, rand, prog, distance, v1, v2, poseStack);
            }
            case TESLA -> renderLightning(consumer, rand, prog, -distance, poseStack);
            default -> renderBeam(params, consumer, prog, 0.05f, distance, u1, u2, v1, v2, poseStack);
        }
        poseStack.popPose();
    }

    private static void vertex(VertexConsumer c, Matrix4f mat, float x, float y, float z, float alpha, float u, float v) {
        c.addVertex(mat, x, y, z).setColor(1.0f, 1.0f, 1.0f, alpha).setUv(u, v).setLight(FULL_BRIGHT);
    }

    /** POSITION_COLOR_TEX_LIGHTMAP quad; the render type does not cull. */
    private static void quad(VertexConsumer c, Matrix4f mat, float[] x, float[] y, float[] z, float[] a, float[] u, float[] v) {
        for (int i = 0; i < 4; i++) {
            vertex(c, mat, x[i], y[i], z[i], a[i], u[i], v[i]);
        }
    }

    private void renderBeam(BeamParams params, VertexConsumer consumer, float prog, float maxWidth, float distance,
                            float u1, float u2, float v1, float v2, PoseStack poseStack) {
        float intensity = (float) Math.max(0, Math.min(1, Math.sin(Math.sqrt(prog) * Math.PI)));
        float width = maxWidth * intensity;

        float startLength = 0.0f;
        if (params.beamStart) {
            startLength = maxWidth * params.uscale * 4.0f;
        }
        boolean drawBeam = distance > startLength;
        startLength = Math.min(startLength, distance);

        poseStack.pushPose();
        for (int i = 0; i < 4; i++) {
            TGMatrixOps.rotate(poseStack, 90f, 1f, 0f, 0f);
            Matrix4f mat = poseStack.last().pose();
            float a = intensity;
            if (drawBeam) {
                quad(consumer, mat,
                        new float[] {-startLength, -distance, -distance, -startLength},
                        new float[] {-width, -width, width, width},
                        new float[4], new float[] {a, a, a, a},
                        new float[] {u1, u2, u2, u1}, new float[] {v1, v1, v2, v2});
            }
            // start piece next to the muzzle
            quad(consumer, mat,
                    new float[] {0f, -startLength, -startLength, 0f},
                    new float[] {-width, -width, width, width},
                    new float[4], new float[] {a, a, a, a},
                    new float[] {0f, 1f, 1f, 0f}, new float[] {v1 + 0.5f, v1 + 0.5f, v2 + 0.5f, v2 + 0.5f});
        }
        poseStack.popPose();
    }

    private void renderSpiral(VertexConsumer consumer, Random rand, float prog, float distance, float v1, float v2, PoseStack poseStack) {
        float intensity = (float) Math.max(0, Math.min(1, (1.0 - Math.cos(Math.sqrt(prog) * 2.0 * Math.PI)) * 0.5));

        float maxDistance = 15.0f;
        distance = Math.min(distance, maxDistance);
        int segments = (int) (distance * 4.0);
        float w = 0.15f;
        float angle = (float) (Math.PI / 8.0);
        float prevX = 0f, prevY = 0f, prevZ = 0f, prevU = 0f;
        float cos2 = (float) (Math.cos((Math.PI - angle) * 0.5) * 2.0);
        float angleOffset = (float) (rand.nextDouble() * Math.PI * 2.0);
        float uvScale = 8.0f;

        poseStack.pushPose();
        poseStack.translate(-0.30, 0, 0);
        Matrix4f mat = poseStack.last().pose();

        for (int i = 0; i < segments; i++) {
            float iprog = i / (maxDistance * 4.0f);
            float x = (float) -Math.pow(iprog * maxDistance, 2.0);
            float radius = 0.20f * (float) Math.max(0, Math.min(1, (1.0 - Math.cos(Math.sqrt(iprog) * 2.0 * Math.PI)) * 0.5));
            float a = angleOffset - angle * i;
            float y = radius * Mth.cos(a);
            float z = radius * Mth.sin(a);
            float u = prevU + (cos2 * radius / uvScale);

            quad(consumer, mat,
                    new float[] {prevX - w, x - w, x + w, prevX + w},
                    new float[] {prevY, y, y, prevY},
                    new float[] {prevZ, z, z, prevZ},
                    new float[] {intensity, intensity, intensity, intensity},
                    new float[] {prevU, u, u, prevU}, new float[] {v2, v2, v1, v1});

            prevX = x;
            prevY = y;
            prevZ = z;
            prevU = u;
        }
        poseStack.popPose();
    }

    private void renderLightning(VertexConsumer consumer, Random rand, float prog, float distance, PoseStack poseStack) {
        final int sinCount = 5;
        final float widthMax = 0.5f;
        final float sinDistance = 10.0f;

        float offset = 0.20f;
        double[] dY = new double[sinCount];
        double[] dZ = new double[sinCount];
        for (int i = 0; i < sinCount; i++) {
            dY[i] = 0.5 - rand.nextDouble();
            dZ[i] = 0.5 - rand.nextDouble();
        }

        int count = Math.max(1, Math.round(Math.abs(distance) / offset));
        offset = distance / count;
        int xreps = Math.max(1, Math.round(Math.abs(distance) / sinDistance));
        float xprev = 0f, yprev = 0f, zprev = 0f, widthPrev = 1.0f, alphaPrev = 1.0f;

        poseStack.pushPose();
        for (int i = 0; i <= count; i++) {
            float d = (float) i / count;
            float x = i * offset;
            float y = 0;
            float z = 0;
            if (i > 1) {
                for (int j = 1; j <= sinCount; j++) {
                    double yfactor = ((rand.nextDouble() - 0.5) + (prog * dY[j - 1])) * (2.0 / j);
                    double zfactor = ((rand.nextDouble() - 0.5) + (prog * dZ[j - 1])) * (2.0 / j);
                    y += (float) (Math.sin(d * Math.PI * (j * xreps)) * yfactor);
                    z += (float) (Math.sin(d * Math.PI * (j * xreps)) * zfactor);
                }
            }
            float pulse = (float) (1.0f - Math.sqrt(Math.abs(prog - d) * 2.0f));
            float width = Math.max(0.0f, widthMax * pulse);
            if (i >= 1) {
                drawSegment(poseStack, consumer, xprev, yprev, zprev, x, y, z, widthPrev, width, alphaPrev);
            }
            widthPrev = width;
            alphaPrev = pulse;
            xprev = x;
            yprev = y;
            zprev = z;
        }
        poseStack.popPose();
    }

    private void drawSegment(PoseStack poseStack, VertexConsumer c, float x1, float y1, float z1, float x2, float y2, float z2,
                             float width1, float width2, float alpha) {
        float scale = 0.5f;
        y1 *= scale;
        y2 *= scale;
        z1 *= scale;
        z2 *= scale;
        width1 *= scale;
        width2 *= scale;
        float a = Mth.clamp(alpha, 0f, 1f);

        poseStack.pushPose();
        TGMatrixOps.rotate(poseStack, 45.0f, 1.0f, 0.0f, 0.0f);
        Matrix4f mat = poseStack.last().pose();
        float[] alphas = {a, a, a, a};
        float[] us = {0, 0, 1, 1};
        float[] vs = {0, 1, 1, 0};
        quad(c, mat, new float[] {x1, x1, x2, x2}, new float[] {y1 - width1, y1 + width1, y2 + width2, y2 - width2},
                new float[] {z1, z1, z2, z2}, alphas, us, vs);
        quad(c, mat, new float[] {x1, x1, x2, x2}, new float[] {y1, y1, y2, y2},
                new float[] {z1 - width1, z1 + width1, z2 + width2, z2 - width2}, alphas, us, vs);
        poseStack.popPose();
    }

    private static final class BeamParams {
        final ResourceLocation texture;
        final int numFrames;
        final float frameTime;
        final float uscale;
        final boolean spinning;
        final boolean beamStart;

        BeamParams(String texture, int numFrames, float frameTime, float uscale, boolean spinning, boolean beamStart) {
            this.texture = TGIds.id(texture);
            this.numFrames = numFrames;
            this.frameTime = frameTime;
            this.uscale = uscale;
            this.spinning = spinning;
            this.beamStart = beamStart;
        }

        /** Returns {u1, v1, u2, v2}. */
        float[] uv(float distance, int age, float partialTick) {
            float u2 = distance / uscale;
            float v1;
            float v2;
            if (beamStart) {
                v1 = 0f;
                v2 = 0.5f;
            } else {
                int frame = (int) (((age + partialTick) * frameTime) % numFrames);
                v1 = (1.0f / numFrames) * frame;
                v2 = (1.0f / numFrames) * (frame + 1);
            }
            return new float[] {0f, v1, u2, v2};
        }
    }
}
