package techguns.client.render.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import techguns.TGIds;
import techguns.machine.WindTurbineBlock;
import techguns.machine.WindTurbineBlockEntity;

/**
 * Wind Turbine segment: a central shaft, three wide helical blades turning a third of a turn per block (one winding per block together, so stacked segments
 * join into one seamless helix) and a strut per blade at half height, all turning with the column's rotor angle. The
 * lowest segment adds a fixed generator housing, the highest a hub cap; blade ends are closed only at the column ends.
 * The mesh is built once in rotor space; each frame only the rotation changes.
 */
// MIGRATION(26.x): BlockEntityRenderer#render gets a render state and submit collector in 1.21.9+.
public class WindTurbineRenderer implements BlockEntityRenderer<WindTurbineBlockEntity> {
    private static final ResourceLocation TEXTURE = TGIds.id("textures/block/wind_turbine.png");
    private static final float PX = 1.0f / 16.0f;
    private static final int BLADES = 3;
    /** Height slices per block and chord steps per blade. */
    private static final int SLICES = 16;
    private static final int CHORD_STEPS = 6;
    private static final float OUTER = 6.5f * PX;
    private static final float INNER = 5.75f * PX;
    /** Half the blade chord as an angle. */
    private static final float HALF_CHORD = (float) Math.toRadians(38.0);
    /**
     * Twist per block: a third of a turn, so each blade ends where the next one starts in the segment above and the
     * three blades together make one winding per block.
     */
    private static final double TWIST_PER_BLOCK = Math.PI * 2.0 / BLADES;

    // texture regions (32x32 texture, in 0..1)
    private static final float[] UV_BLADE = {0.0f, 0.0f, 0.5f, 0.5f};
    private static final float[] UV_SHAFT = {0.5f, 0.0f, 0.75f, 0.5f};
    private static final float[] UV_STRUT = {0.75f, 0.0f, 1.0f, 0.25f};
    private static final float[] UV_HUB = {0.75f, 0.25f, 1.0f, 0.5f};
    private static final float[] UV_HOUSING = {0.0f, 0.5f, 0.5f, 1.0f};

    private static final Mesh ROTOR = rotor();
    private static final Mesh BLADE_BOTTOMS = bladeEnds(0.0f, -1.0f);
    private static final Mesh BLADE_TOPS = bladeEnds(1.0f, 1.0f);
    private static final Mesh HUB = hub();
    private static final Mesh HOUSING = housing();

    public WindTurbineRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(WindTurbineBlockEntity turbine, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int light, int overlay) {
        BlockState state = turbine.getBlockState();
        renderSegment(poseStack, buffer, light, overlay, turbine.rotorAngle(partialTick), !state.getValue(WindTurbineBlock.DOWN),
                !state.getValue(WindTurbineBlock.UP));
    }

    /** Item: one segment with housing and hub. */
    public static void renderItem(PoseStack poseStack, MultiBufferSource buffer, int light, int overlay) {
        renderSegment(poseStack, buffer, light, overlay, 0.0f, true, true);
    }

    private static void renderSegment(PoseStack poseStack, MultiBufferSource buffer, int light, int overlay, float angle,
                                      boolean bottom, boolean top) {
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
        poseStack.pushPose();
        poseStack.translate(0.5f, 0.0f, 0.5f);
        if (bottom) {
            HOUSING.emit(poseStack.last(), consumer, light, overlay);
        }
        poseStack.mulPose(Axis.YP.rotationDegrees(angle));
        PoseStack.Pose pose = poseStack.last();
        ROTOR.emit(pose, consumer, light, overlay);
        if (bottom) {
            BLADE_BOTTOMS.emit(pose, consumer, light, overlay);
        }
        if (top) {
            BLADE_TOPS.emit(pose, consumer, light, overlay);
            HUB.emit(pose, consumer, light, overlay);
        }
        poseStack.popPose();
    }

    @Override
    public AABB getRenderBoundingBox(WindTurbineBlockEntity turbine) {
        // the hub sticks out above the block
        return new AABB(turbine.getBlockPos()).expandTowards(0.0, 0.25, 0.0);
    }

    // --- mesh ---

    /** Blade angle (radians) of blade {@code blade} at height {@code y} (0..1). */
    private static double twist(int blade, double y) {
        return blade * (Math.PI * 2.0 / BLADES) + y * TWIST_PER_BLOCK;
    }

    private static Mesh rotor() {
        Mesh mesh = new Mesh();
        for (int b = 0; b < BLADES; b++) {
            for (int s = 0; s < SLICES; s++) {
                float y0 = (float) s / SLICES;
                float y1 = (float) (s + 1) / SLICES;
                double c0 = twist(b, y0);
                double c1 = twist(b, y1);
                float v0 = lerp(UV_BLADE[1], UV_BLADE[3], y0);
                float v1 = lerp(UV_BLADE[1], UV_BLADE[3], y1);
                for (int k = 0; k < CHORD_STEPS; k++) {
                    double t0 = -HALF_CHORD + 2.0 * HALF_CHORD * k / CHORD_STEPS;
                    double t1 = -HALF_CHORD + 2.0 * HALF_CHORD * (k + 1) / CHORD_STEPS;
                    float u0 = lerp(UV_BLADE[0], UV_BLADE[2], (float) k / CHORD_STEPS);
                    float u1 = lerp(UV_BLADE[0], UV_BLADE[2], (float) (k + 1) / CHORD_STEPS);
                    // outer and inner skin, normals along the radius
                    mesh.radialQuad(OUTER, c0 + t0, c0 + t1, c1 + t1, c1 + t0, y0, y1, u0, v0, u1, v1, 1.0f);
                    mesh.radialQuad(INNER, c0 + t1, c0 + t0, c1 + t0, c1 + t1, y0, y1, u1, v0, u0, v1, -1.0f);
                }
                // leading and trailing edge
                mesh.edgeQuad(c0 - HALF_CHORD, c1 - HALF_CHORD, y0, y1, -1.0f, UV_BLADE[0], v0, UV_BLADE[0] + 0.03f, v1);
                mesh.edgeQuad(c0 + HALF_CHORD, c1 + HALF_CHORD, y0, y1, 1.0f, UV_BLADE[2] - 0.03f, v0, UV_BLADE[2], v1);
            }
            // a strut from the shaft to the blade at half height
            double mid = twist(b, 0.5);
            mesh.rotatedBox(mid, 1.0f * PX, INNER + 0.2f * PX, 7.5f * PX, 8.5f * PX, 0.5f * PX, UV_STRUT);
        }
        // shaft
        mesh.box(-1.0f * PX, 0.0f, -1.0f * PX, 1.0f * PX, 1.0f, 1.0f * PX, UV_SHAFT);
        return mesh;
    }

    /** Closes the blade ends at height {@code y}; {@code ny} the normal's direction. */
    private static Mesh bladeEnds(float y, float ny) {
        Mesh mesh = new Mesh();
        for (int b = 0; b < BLADES; b++) {
            double c = twist(b, y);
            for (int k = 0; k < CHORD_STEPS; k++) {
                double t0 = c - HALF_CHORD + 2.0 * HALF_CHORD * k / CHORD_STEPS;
                double t1 = c - HALF_CHORD + 2.0 * HALF_CHORD * (k + 1) / CHORD_STEPS;
                mesh.quad(0.0f, ny, 0.0f,
                        new float[]{cos(t0) * INNER, y, sin(t0) * INNER, UV_BLADE[0], UV_BLADE[1]},
                        new float[]{cos(t0) * OUTER, y, sin(t0) * OUTER, UV_BLADE[0], UV_BLADE[1] + 0.03f},
                        new float[]{cos(t1) * OUTER, y, sin(t1) * OUTER, UV_BLADE[2], UV_BLADE[1] + 0.03f},
                        new float[]{cos(t1) * INNER, y, sin(t1) * INNER, UV_BLADE[2], UV_BLADE[1]});
            }
        }
        return mesh;
    }

    private static Mesh hub() {
        Mesh mesh = new Mesh();
        mesh.box(-2.0f * PX, 15.0f * PX, -2.0f * PX, 2.0f * PX, 17.0f * PX, 2.0f * PX, UV_HUB);
        mesh.box(-0.5f * PX, 17.0f * PX, -0.5f * PX, 0.5f * PX, 18.0f * PX, 0.5f * PX, UV_SHAFT);
        return mesh;
    }

    private static Mesh housing() {
        Mesh mesh = new Mesh();
        // a foot plate that stays inside the blades' sweep, and the generator box the shaft runs into
        mesh.box(-4.0f * PX, 0.0f, -4.0f * PX, 4.0f * PX, 1.0f * PX, 4.0f * PX, UV_HOUSING);
        mesh.box(-2.5f * PX, 1.0f * PX, -2.5f * PX, 2.5f * PX, 4.0f * PX, 2.5f * PX, UV_HUB);
        return mesh;
    }

    private static float cos(double a) {
        return (float) Math.cos(a);
    }

    private static float sin(double a) {
        return (float) Math.sin(a);
    }

    private static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    /** Quads in rotor space: per vertex x, y, z, u, v, nx, ny, nz. */
    private static final class Mesh {
        private float[] data = new float[1024];
        private int size;

        private void vertex(float x, float y, float z, float u, float v, float nx, float ny, float nz) {
            if (size + 8 > data.length) {
                data = java.util.Arrays.copyOf(data, data.length * 2);
            }
            data[size++] = x;
            data[size++] = y;
            data[size++] = z;
            data[size++] = u;
            data[size++] = v;
            data[size++] = nx;
            data[size++] = ny;
            data[size++] = nz;
        }

        /** Four vertices {x, y, z, u, v} with one normal. */
        void quad(float nx, float ny, float nz, float[] a, float[] b, float[] c, float[] d) {
            for (float[] p : new float[][]{a, b, c, d}) {
                vertex(p[0], p[1], p[2], p[3], p[4], nx, ny, nz);
            }
        }

        /** A skin quad on the cylinder of radius {@code r}: angles at (y0, y0, y1, y1), smooth radial normals. */
        void radialQuad(float r, double a0, double a1, double a2, double a3, float y0, float y1,
                        float u0, float v0, float u1, float v1, float sign) {
            vertex(cos(a0) * r, y0, sin(a0) * r, u0, v1, cos(a0) * sign, 0.0f, sin(a0) * sign);
            vertex(cos(a1) * r, y0, sin(a1) * r, u1, v1, cos(a1) * sign, 0.0f, sin(a1) * sign);
            vertex(cos(a2) * r, y1, sin(a2) * r, u1, v0, cos(a2) * sign, 0.0f, sin(a2) * sign);
            vertex(cos(a3) * r, y1, sin(a3) * r, u0, v0, cos(a3) * sign, 0.0f, sin(a3) * sign);
        }

        /** A blade edge between the inner and outer skin at angle a0 (y0) to a1 (y1); normal along the turn. */
        void edgeQuad(double a0, double a1, float y0, float y1, float sign, float u0, float v0, float u1, float v1) {
            float nx0 = -sin(a0) * sign;
            float nz0 = cos(a0) * sign;
            float nx1 = -sin(a1) * sign;
            float nz1 = cos(a1) * sign;
            vertex(cos(a0) * INNER, y0, sin(a0) * INNER, u0, v1, nx0, 0.0f, nz0);
            vertex(cos(a0) * OUTER, y0, sin(a0) * OUTER, u1, v1, nx0, 0.0f, nz0);
            vertex(cos(a1) * OUTER, y1, sin(a1) * OUTER, u1, v0, nx1, 0.0f, nz1);
            vertex(cos(a1) * INNER, y1, sin(a1) * INNER, u0, v0, nx1, 0.0f, nz1);
        }

        /** An axis-aligned box; every face gets the whole {@code uv} region. */
        void box(float x0, float y0, float z0, float x1, float y1, float z1, float[] uv) {
            float u0 = uv[0];
            float v0 = uv[1];
            float u1 = uv[2];
            float v1 = uv[3];
            quad(0, 1, 0, new float[]{x0, y1, z0, u0, v0}, new float[]{x0, y1, z1, u0, v1}, new float[]{x1, y1, z1, u1, v1}, new float[]{x1, y1, z0, u1, v0});
            quad(0, -1, 0, new float[]{x0, y0, z1, u0, v0}, new float[]{x0, y0, z0, u0, v1}, new float[]{x1, y0, z0, u1, v1}, new float[]{x1, y0, z1, u1, v0});
            quad(0, 0, -1, new float[]{x1, y1, z0, u0, v0}, new float[]{x1, y0, z0, u0, v1}, new float[]{x0, y0, z0, u1, v1}, new float[]{x0, y1, z0, u1, v0});
            quad(0, 0, 1, new float[]{x0, y1, z1, u0, v0}, new float[]{x0, y0, z1, u0, v1}, new float[]{x1, y0, z1, u1, v1}, new float[]{x1, y1, z1, u1, v0});
            quad(-1, 0, 0, new float[]{x0, y1, z0, u0, v0}, new float[]{x0, y0, z0, u0, v1}, new float[]{x0, y0, z1, u1, v1}, new float[]{x0, y1, z1, u1, v0});
            quad(1, 0, 0, new float[]{x1, y1, z1, u0, v0}, new float[]{x1, y0, z1, u0, v1}, new float[]{x1, y0, z0, u1, v1}, new float[]{x1, y1, z0, u1, v0});
        }

        /**
         * A radial bar at angle {@code a} from radius {@code r0} to {@code r1}, between heights y0 and y1, {@code half}
         * wide to each side; normals turned with it.
         */
        void rotatedBox(double a, float r0, float r1, float y0, float y1, float half, float[] uv) {
            float cx = cos(a);
            float cz = sin(a);
            // radial (cx, cz), sideways (-cz, cx)
            float[][] corners = new float[4][];
            float[] rs = {r0, r1};
            float[] ss = {-half, half};
            int i = 0;
            for (float r : rs) {
                for (float s : ss) {
                    corners[i++] = new float[]{cx * r - cz * s, cz * r + cx * s};
                }
            }
            // corners: 0 (r0,-), 1 (r0,+), 2 (r1,-), 3 (r1,+)
            float u0 = uv[0];
            float v0 = uv[1];
            float u1 = uv[2];
            float v1 = uv[3];
            float[] c0 = corners[0];
            float[] c1 = corners[1];
            float[] c2 = corners[2];
            float[] c3 = corners[3];
            quad(0, 1, 0, p(c0, y1, u0, v0), p(c1, y1, u0, v1), p(c3, y1, u1, v1), p(c2, y1, u1, v0));
            quad(0, -1, 0, p(c1, y0, u0, v0), p(c0, y0, u0, v1), p(c2, y0, u1, v1), p(c3, y0, u1, v0));
            quad(cz, 0, -cx, p(c0, y1, u0, v0), p(c0, y0, u0, v1), p(c2, y0, u1, v1), p(c2, y1, u1, v0));
            quad(-cz, 0, cx, p(c3, y1, u0, v0), p(c3, y0, u0, v1), p(c1, y0, u1, v1), p(c1, y1, u1, v0));
            quad(cx, 0, cz, p(c2, y1, u0, v0), p(c2, y0, u0, v1), p(c3, y0, u1, v1), p(c3, y1, u1, v0));
            quad(-cx, 0, -cz, p(c1, y1, u0, v0), p(c1, y0, u0, v1), p(c0, y0, u1, v1), p(c0, y1, u1, v0));
        }

        private static float[] p(float[] xz, float y, float u, float v) {
            return new float[]{xz[0], y, xz[1], u, v};
        }

        void emit(PoseStack.Pose pose, VertexConsumer consumer, int light, int overlay) {
            for (int i = 0; i < size; i += 8) {
                consumer.addVertex(pose, data[i], data[i + 1], data[i + 2]).setColor(-1).setUv(data[i + 3], data[i + 4])
                        .setOverlay(overlay).setLight(light).setNormal(pose, data[i + 5], data[i + 6], data[i + 7]);
            }
        }
    }
}
