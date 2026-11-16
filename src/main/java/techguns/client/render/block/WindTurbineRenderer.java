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
import techguns.TGIds;
import techguns.machine.WindTurbineBlock;
import techguns.machine.WindTurbineBlockEntity;

/**
 * Wind Turbine segment: a central shaft and three wide helical blades turning a third of a turn per block (one winding
 * per block together, so stacked segments join into one seamless helix), all turning with the column's rotor angle.
 * Every segment has one strut per blade at half height. Four variants by the segment's place in the column: the
 * bottom segment stands on a fixed generator housing and its blades start above it; the top segment is open, its
 * blades stop just below the block top and the round shaft ends at three quarters height in a
 * rounded tip; a single segment has both ends. Blade corners are rounded at both column ends.
 * The meshes are built once in rotor space; each frame only the rotation changes.
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
    /** Slices where the blades start in the bottom segment (above the housing) and end in the top segment. */
    private static final int BOTTOM_SLICE = 4;
    private static final int TOP_SLICE = 15;
    /** Radius in pixels of the rounded blade corners at the column ends, and slices per pixel there. */
    private static final float TIP_RADIUS = 3.0f;
    private static final int TIP_STEPS_PER_PIXEL = 4;
    /** Where the shaft's dome tip starts in the top segment; the top stays open, without a hub. */
    private static final float SHAFT_TOP = 12.0f * PX;
    /** The round shaft: radius and sides. */
    private static final float SHAFT_RADIUS = 1.1f * PX;
    private static final int SHAFT_SIDES = 8;
    /** One texel of the blade's plain body colour, for blade edges and ends. */
    private static final float RIM_U = 4.5f / 32.0f;
    private static final float RIM_V = 8.5f / 32.0f;

    // texture regions (32x32 texture, in 0..1)
    private static final float[] UV_BLADE = {0.0f, 0.0f, 0.5f, 0.5f};
    private static final float[] UV_SHAFT = {0.5f, 0.0f, 0.75f, 0.5f};
    private static final float[] UV_STRUT = {0.75f, 0.0f, 1.0f, 0.25f};
    private static final float[] UV_HUB = {0.75f, 0.25f, 1.0f, 0.5f};
    private static final float[] UV_HOUSING = {0.0f, 0.5f, 0.5f, 1.0f};

    /** Rotor meshes by variant: bit 0 bottom segment, bit 1 top segment (0 middle, 3 single). */
    private static final Mesh[] ROTORS = {rotor(false, false), rotor(true, false), rotor(false, true), rotor(true, true)};
    private static final Mesh HOUSING = housing();

    public WindTurbineRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(WindTurbineBlockEntity turbine, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int light, int overlay) {
        BlockState state = turbine.getBlockState();
        renderSegment(poseStack, buffer, light, overlay, turbine.rotorAngle(partialTick), !state.getValue(WindTurbineBlock.DOWN),
                !state.getValue(WindTurbineBlock.UP));
    }

    /** Item: a single segment (housing and hub cap). */
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
        ROTORS[(bottom ? 1 : 0) | (top ? 2 : 0)].emit(poseStack.last(), consumer, light, overlay);
        poseStack.popPose();
    }

    // --- mesh ---

    /** Blade angle (radians) of blade {@code blade} at height {@code y} (0..1). */
    private static double twist(int blade, double y) {
        return blade * (Math.PI * 2.0 / BLADES) + y * TWIST_PER_BLOCK;
    }

    private static Mesh rotor(boolean bottom, boolean top) {
        Mesh mesh = new Mesh();
        int from = bottom ? BOTTOM_SLICE : 0;
        int to = top ? TOP_SLICE : SLICES;
        float low = (float) from / SLICES;
        float high = (float) to / SLICES;
        float[] heights = sliceHeights(from, to, bottom, top);
        for (int b = 0; b < BLADES; b++) {
            for (int s = 0; s + 1 < heights.length; s++) {
                float y0 = heights[s];
                float y1 = heights[s + 1];
                double c0 = twist(b, y0);
                double c1 = twist(b, y1);
                double h0 = halfChord(y0, bottom, low, top, high);
                double h1 = halfChord(y1, bottom, low, top, high);
                float v0 = lerp(UV_BLADE[1], UV_BLADE[3], y0);
                float v1 = lerp(UV_BLADE[1], UV_BLADE[3], y1);
                for (int k = 0; k < CHORD_STEPS; k++) {
                    double f0 = -1.0 + 2.0 * k / CHORD_STEPS;
                    double f1 = -1.0 + 2.0 * (k + 1) / CHORD_STEPS;
                    float u0 = lerp(UV_BLADE[0], UV_BLADE[2], (float) k / CHORD_STEPS);
                    float u1 = lerp(UV_BLADE[0], UV_BLADE[2], (float) (k + 1) / CHORD_STEPS);
                    // outer and inner skin, normals along the radius
                    mesh.radialQuad(OUTER, c0 + f0 * h0, c0 + f1 * h0, c1 + f1 * h1, c1 + f0 * h1, y0, y1, u0, v0, u1, v1, 1.0f);
                    mesh.radialQuad(INNER, c0 + f1 * h0, c0 + f0 * h0, c1 + f0 * h1, c1 + f1 * h1, y0, y1, u1, v0, u0, v1, -1.0f);
                }
                // leading and trailing edge
                // leading and trailing edge, in the rim colour (they become the rounded tip's top on the top segment)
                mesh.edgeQuad(c0 - h0, c1 - h1, y0, y1, -1.0f, RIM_U, RIM_V, RIM_U, RIM_V);
                mesh.edgeQuad(c0 + h0, c1 + h1, y0, y1, 1.0f, RIM_U, RIM_V, RIM_U, RIM_V);
            }
            // one strut from the shaft to the blade at half height, in every variant
            mesh.rotatedBox(twist(b, 0.5), 1.0f * PX, INNER + 0.2f * PX, 7.5f * PX, 8.5f * PX, 0.5f * PX, UV_STRUT);
        }
        bladeEnds(mesh, bottom, low, top, high);
        // round shaft; on the top segment it ends at three quarters of the block in a rounded tip
        mesh.prism(SHAFT_RADIUS, 0.0f, top ? SHAFT_TOP : 1.0f, SHAFT_SIDES, UV_SHAFT);
        if (top) {
            mesh.dome(SHAFT_RADIUS, SHAFT_TOP, SHAFT_RADIUS, SHAFT_SIDES, 3, UV_SHAFT);
        }
        if (bottom) {
            // a turning disc on the generator, where the shaft leaves it
            mesh.box(-2.0f * PX, low - 1.5f * PX, -2.0f * PX, 2.0f * PX, low - 1.0f * PX, 2.0f * PX, UV_HUB);
        }
        return mesh;
    }

    /** Closes the blade ends at the column's ends. */
    private static void bladeEnds(Mesh mesh, boolean bottom, float low, boolean top, float high) {
        for (int b = 0; b < BLADES; b++) {
            if (bottom) {
                bladeEnd(mesh, twist(b, low), halfChord(low, bottom, low, top, high), low, -1.0f);
            }
            if (top) {
                bladeEnd(mesh, twist(b, high), halfChord(high, bottom, low, top, high), high, 1.0f);
            }
        }
    }

    /** Slice heights from slice {@code from} to {@code to}; finer where the blade corners are rounded at either end. */
    private static float[] sliceHeights(int from, int to, boolean bottom, boolean top) {
        java.util.List<Float> heights = new java.util.ArrayList<>();
        for (int s = from; s < to; s++) {
            heights.add((float) s / SLICES);
            boolean rounding = (top && s + 1 > to - TIP_RADIUS) || (bottom && s < from + TIP_RADIUS);
            if (rounding) {
                for (int k = 1; k < TIP_STEPS_PER_PIXEL; k++) {
                    heights.add((s + (float) k / TIP_STEPS_PER_PIXEL) / SLICES);
                }
            }
        }
        heights.add((float) to / SLICES);
        float[] out = new float[heights.size()];
        for (int i = 0; i < out.length; i++) {
            out[i] = heights.get(i);
        }
        return out;
    }

    /**
     * Half the blade chord (radians) at height {@code y}: the full chord, but at the column's ends (the bottom segment's
     * lower blade end, the top segment's upper one) the blade's two corners are rounded with a radius of
     * {@link #TIP_RADIUS} pixels.
     */
    private static double halfChord(float y, boolean bottom, float low, boolean top, float high) {
        double radius = TIP_RADIUS * PX;
        double into = 0.0;
        if (top) {
            into = Math.max(into, y - (high - radius));
        }
        if (bottom) {
            into = Math.max(into, (low + radius) - y);
        }
        if (into <= 0.0) {
            return HALF_CHORD;
        }
        double t = Math.min(1.0, into / radius);
        // the corner arc, as an angle on the blade's outer skin
        return HALF_CHORD - radius / OUTER * (1.0 - Math.sqrt(1.0 - t * t));
    }

    private static void bladeEnd(Mesh mesh, double c, double half, float y, float ny) {
        for (int k = 0; k < CHORD_STEPS; k++) {
            double t0 = c - half + 2.0 * half * k / CHORD_STEPS;
            double t1 = c - half + 2.0 * half * (k + 1) / CHORD_STEPS;
            // one plain rim texel: a texture strip stretched over the chord shows as stripes
            mesh.quad(0.0f, ny, 0.0f,
                    new float[]{cos(t0) * INNER, y, sin(t0) * INNER, RIM_U, RIM_V},
                    new float[]{cos(t0) * OUTER, y, sin(t0) * OUTER, RIM_U, RIM_V},
                    new float[]{cos(t1) * OUTER, y, sin(t1) * OUTER, RIM_U, RIM_V},
                    new float[]{cos(t1) * INNER, y, sin(t1) * INNER, RIM_U, RIM_V});
        }
    }

    /** The fixed generator under the bottom segment: a wide foot plate and the generator box. */
    private static Mesh housing() {
        Mesh mesh = new Mesh();
        mesh.box(-6.0f * PX, 0.0f, -6.0f * PX, 6.0f * PX, 1.0f * PX, 6.0f * PX, UV_HOUSING);
        mesh.box(-3.5f * PX, 1.0f * PX, -3.5f * PX, 3.5f * PX, (BOTTOM_SLICE - 1.5f) * PX, 3.5f * PX, UV_HUB);
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

        /** A vertical prism of {@code sides} sides around the axis; u around it, v along its height. */
        void prism(float r, float y0, float y1, int sides, float[] uv) {
            for (int i = 0; i < sides; i++) {
                double a0 = Math.PI * 2.0 * i / sides;
                double a1 = Math.PI * 2.0 * (i + 1) / sides;
                float u0 = lerp(uv[0], uv[2], (float) i / sides);
                float u1 = lerp(uv[0], uv[2], (float) (i + 1) / sides);
                float v0 = lerp(uv[1], uv[3], 1.0f - y1);
                float v1 = lerp(uv[1], uv[3], 1.0f - y0);
                radialQuad(r, a0, a1, a1, a0, y0, y1, u0, v0, u1, v1, 1.0f);
            }
        }

        /** A rounded cap on a prism: {@code rings} rings up to a point {@code height} above {@code y}. */
        void dome(float r, float y, float height, int sides, int rings, float[] uv) {
            for (int k = 0; k < rings; k++) {
                double p0 = Math.PI / 2.0 * k / rings;
                double p1 = Math.PI / 2.0 * (k + 1) / rings;
                float r0 = (float) (r * Math.cos(p0));
                float r1 = (float) (r * Math.cos(p1));
                float y0 = y + (float) (height * Math.sin(p0));
                float y1 = y + (float) (height * Math.sin(p1));
                float ny = (float) Math.sin((p0 + p1) / 2.0);
                float nr = (float) Math.cos((p0 + p1) / 2.0);
                for (int i = 0; i < sides; i++) {
                    double a0 = Math.PI * 2.0 * i / sides;
                    double a1 = Math.PI * 2.0 * (i + 1) / sides;
                    float u0 = lerp(uv[0], uv[2], (float) i / sides);
                    float u1 = lerp(uv[0], uv[2], (float) (i + 1) / sides);
                    float am = (float) ((a0 + a1) / 2.0);
                    quad(cos(am) * nr, ny, sin(am) * nr,
                            new float[]{cos(a0) * r0, y0, sin(a0) * r0, u0, uv[3]},
                            new float[]{cos(a1) * r0, y0, sin(a1) * r0, u1, uv[3]},
                            new float[]{cos(a1) * r1, y1, sin(a1) * r1, u1, uv[1]},
                            new float[]{cos(a0) * r1, y1, sin(a0) * r1, u0, uv[1]});
                }
            }
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
