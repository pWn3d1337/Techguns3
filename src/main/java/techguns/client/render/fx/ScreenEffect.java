package techguns.client.render.fx;

import org.joml.Matrix4f;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import techguns.TGIds;
import techguns.client.render.TGMatrixOps;
import techguns.client.render.TGRenderTypes;

/**
 * Sprite-sheet billboard effect. Drawn through the buffer source with an emissive translucent or additive render type.
 */
public class ScreenEffect implements IScreenEffect {

    /** Full-bright packed light (15, 15). */
    private static final int FULL_BRIGHT = 0xF000F0;

    protected ResourceLocation fxTexture;
    protected int cols;
    protected int rows;
    protected int numSprites;
    protected BlendMode type;

    protected float colorR = 1.0f;
    protected float colorG = 1.0f;
    protected float colorB = 1.0f;
    protected float alpha = 1.0f;
    protected FadeType fadeType = FadeType.NONE;

    protected boolean flipY = false;
    protected boolean flipX = false;

    protected FadeType dynamicScaleType = null;

    protected float minScale = 0f;
    protected float maxScale = 0f;

    protected DrawMode3p drawMode3p = DrawMode3p.CROSS_XYZ;

    public ScreenEffect(String fxTexture, int cols, int rows, int numSprites, BlendMode type) {
        this.fxTexture = TGIds.id(fxTexture);
        this.cols = cols;
        this.rows = rows;
        this.numSprites = numSprites;
        this.type = type;
    }

    public ScreenEffect setColor(float r, float g, float b, float a) {
        this.colorR = r;
        this.colorG = g;
        this.colorB = b;
        this.alpha = a;
        return this;
    }

    public ScreenEffect setFade(FadeType type) {
        this.fadeType = type;
        return this;
    }

    public ScreenEffect setFlipAxis(boolean x, boolean y) {
        this.flipX = x;
        this.flipY = y;
        return this;
    }

    public ScreenEffect setDynamicScale(FadeType fadeType, float minScale, float maxScale) {
        this.dynamicScaleType = fadeType;
        this.minScale = minScale;
        this.maxScale = maxScale;
        return this;
    }

    public ScreenEffect setDrawMode3p(DrawMode3p mode) {
        this.drawMode3p = mode;
        return this;
    }

    enum DrawMode3p {
        CROSS_XYZ, CROSS_XY, PLANE_Z
    }

    protected RenderType renderTypeFor() {
        return switch (type) {
            case ADDITIVE -> TGRenderTypes.fxAdditive(fxTexture);
            case SCOPE -> TGRenderTypes.scope(fxTexture);
            default -> TGRenderTypes.fxAlpha(fxTexture);
        };
    }

    @Override
    public void doRender(PoseStack poseStack, MultiBufferSource buffers, float progress, float offsetX, float offsetY, float offsetZ, float scale,
                         float rotX, float rotY, float rotZ, boolean is3p, String ammoVariant) {
        int currentFrame = (int) ((float) numSprites * progress) % numSprites;
        int col = currentFrame % cols;
        int row = currentFrame / cols;

        float u = 1.f / cols;
        float v = 1.f / rows;

        float u1 = col * u;
        float v1 = row * v;
        float u2 = (col + 1) * u;
        float v2 = (row + 1) * v;

        TGMatrixOps.rotate(poseStack, rotX, 1.0f, 0, 0);
        TGMatrixOps.rotate(poseStack, rotY, 0, 1.0f, 0);
        TGMatrixOps.rotate(poseStack, rotZ, 0, 0, 1.0f);

        if (this.flipX) {
            float tmp = u1;
            u1 = u2;
            u2 = tmp;
        }
        if (this.flipY) {
            float tmp = v1;
            v1 = v2;
            v2 = tmp;
        }

        float f = scale / 2.0f;
        if (this.dynamicScaleType != null) {
            f *= this.dynamicScaleType.getValueAt(progress);
        }

        float a = this.alpha * this.fadeType.getValueAt(progress);

        VertexConsumer vertices = buffers.getBuffer(renderTypeFor());
        PoseStack.Pose pose = poseStack.last();
        Matrix4f mat = pose.pose();

        if (!is3p) {
            quad(vertices, pose, mat, a,
                    offsetX - f, offsetY + f, offsetZ, u1, v2,
                    offsetX - f, offsetY - f, offsetZ, u1, v1,
                    offsetX + f, offsetY - f, offsetZ, u2, v1,
                    offsetX + f, offsetY + f, offsetZ, u2, v2);
        } else {
            if (drawMode3p == DrawMode3p.PLANE_Z || drawMode3p == DrawMode3p.CROSS_XYZ) {
                // Z plane
                quad(vertices, pose, mat, a,
                        offsetX - f, offsetY + f, offsetZ, u1, v2,
                        offsetX - f, offsetY - f, offsetZ, u1, v1,
                        offsetX + f, offsetY - f, offsetZ, u2, v1,
                        offsetX + f, offsetY + f, offsetZ, u2, v2);
            }
            if (drawMode3p == DrawMode3p.CROSS_XY || drawMode3p == DrawMode3p.CROSS_XYZ) {
                // Y plane
                quad(vertices, pose, mat, a,
                        offsetX - f, offsetY, offsetZ + f, u1, v2,
                        offsetX - f, offsetY, offsetZ - f, u1, v1,
                        offsetX + f, offsetY, offsetZ - f, u2, v1,
                        offsetX + f, offsetY, offsetZ + f, u2, v2);
                // X plane
                quad(vertices, pose, mat, a,
                        offsetX, offsetY - f, offsetZ + f, u1, v2,
                        offsetX, offsetY - f, offsetZ - f, u1, v1,
                        offsetX, offsetY + f, offsetZ - f, u2, v1,
                        offsetX, offsetY + f, offsetZ + f, u2, v2);
            }
        }
    }

    /** Emits one quad; the FX render types don't cull, so it is visible from both sides. */
    private void quad(VertexConsumer vc, PoseStack.Pose pose, Matrix4f mat, float a,
                      float x1, float y1, float z1, float u1, float v1,
                      float x2, float y2, float z2, float u2, float v2,
                      float x3, float y3, float z3, float u3, float v3,
                      float x4, float y4, float z4, float u4, float v4) {
        vertex(vc, mat, a, x1, y1, z1, u1, v1);
        vertex(vc, mat, a, x2, y2, z2, u2, v2);
        vertex(vc, mat, a, x3, y3, z3, u3, v3);
        vertex(vc, mat, a, x4, y4, z4, u4, v4);
    }

    /** POSITION_COLOR_TEX_LIGHTMAP vertex. */
    private void vertex(VertexConsumer vc, Matrix4f mat, float a, float x, float y, float z, float u, float v) {
        vc.addVertex(mat, x, y, z).setColor(colorR, colorG, colorB, a).setUv(u, v).setLight(FULL_BRIGHT);
    }
}
