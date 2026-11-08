package techguns.client.render.fx;

import java.util.Optional;

import org.joml.Matrix4f;
import org.joml.Vector4f;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import techguns.client.render.TGMatrixOps;
import techguns.client.render.TGRenderTypes;

/**
 * Sprite-sheet billboard effect ({@code "type": "sprite"} in a screen effect file). Drawn through the buffer source with
 * an emissive translucent or additive render type.
 *
 * @param texture      the sprite sheet
 * @param columns      sprites per row
 * @param rows         sprite rows
 * @param frames       sprites played over the effect's progress (row by row)
 * @param blend        how the effect is blended
 * @param color        tint and alpha
 * @param fade         alpha over the progress
 * @param flipX        mirrors the sprite horizontally
 * @param flipY        mirrors the sprite vertically
 * @param scaleFade    size over the progress, times the effect scale
 * @param thirdPerson  the planes drawn in third person (first person draws one plane facing the camera)
 */
public record ScreenEffect(ResourceLocation texture, int columns, int rows, int frames, BlendMode blend, Vector4f color, FadeType fade,
                           boolean flipX, boolean flipY, Optional<FadeType> scaleFade, DrawMode3p thirdPerson) implements IScreenEffect {

    /** Full-bright packed light (15, 15). */
    private static final int FULL_BRIGHT = 0xF000F0;

    public static final MapCodec<ScreenEffect> CODEC = RecordCodecBuilder.<ScreenEffect>mapCodec(i -> i.group(
            ResourceLocation.CODEC.fieldOf("texture").forGetter(ScreenEffect::texture),
            ExtraCodecs.POSITIVE_INT.optionalFieldOf("columns", 1).forGetter(ScreenEffect::columns),
            ExtraCodecs.POSITIVE_INT.optionalFieldOf("rows", 1).forGetter(ScreenEffect::rows),
            ExtraCodecs.POSITIVE_INT.optionalFieldOf("frames", 0).forGetter(e -> e.frames() == e.columns() * e.rows() ? 0 : e.frames()),
            ScreenEffects.enumCodec(BlendMode.class).optionalFieldOf("blend", BlendMode.ADDITIVE).forGetter(ScreenEffect::blend),
            ExtraCodecs.VECTOR4F.optionalFieldOf("color", new Vector4f(1, 1, 1, 1)).forGetter(ScreenEffect::color),
            ScreenEffects.enumCodec(FadeType.class).optionalFieldOf("fade", FadeType.NONE).forGetter(ScreenEffect::fade),
            Codec.BOOL.optionalFieldOf("flip_x", false).forGetter(ScreenEffect::flipX),
            Codec.BOOL.optionalFieldOf("flip_y", false).forGetter(ScreenEffect::flipY),
            ScreenEffects.enumCodec(FadeType.class).optionalFieldOf("scale_fade").forGetter(ScreenEffect::scaleFade),
            ScreenEffects.enumCodec(DrawMode3p.class).optionalFieldOf("third_person", DrawMode3p.CROSS_XYZ).forGetter(ScreenEffect::thirdPerson)
    ).apply(i, ScreenEffect::new));

    /** {@code frames} 0 (left out) = every sprite of the sheet. */
    public ScreenEffect {
        if (frames <= 0) {
            frames = columns * rows;
        }
    }

    public enum DrawMode3p {
        CROSS_XYZ, CROSS_XY, PLANE_Z
    }

    private RenderType renderTypeFor() {
        return switch (blend) {
            case ADDITIVE -> TGRenderTypes.fxAdditive(texture);
            case SCOPE -> TGRenderTypes.scope(texture);
            default -> TGRenderTypes.fxAlpha(texture);
        };
    }

    @Override
    public void doRender(PoseStack poseStack, MultiBufferSource buffers, float progress, float offsetX, float offsetY, float offsetZ, float scale,
                         float rotX, float rotY, float rotZ, boolean is3p, String ammoVariant) {
        int currentFrame = (int) ((float) frames * progress) % frames;
        int col = currentFrame % columns;
        int row = currentFrame / columns;

        float u = 1.f / columns;
        float v = 1.f / rows;

        float u1 = col * u;
        float v1 = row * v;
        float u2 = (col + 1) * u;
        float v2 = (row + 1) * v;

        TGMatrixOps.rotate(poseStack, rotX, 1.0f, 0, 0);
        TGMatrixOps.rotate(poseStack, rotY, 0, 1.0f, 0);
        TGMatrixOps.rotate(poseStack, rotZ, 0, 0, 1.0f);

        if (flipX) {
            float tmp = u1;
            u1 = u2;
            u2 = tmp;
        }
        if (flipY) {
            float tmp = v1;
            v1 = v2;
            v2 = tmp;
        }

        float f = scale / 2.0f;
        if (scaleFade.isPresent()) {
            f *= scaleFade.get().getValueAt(progress);
        }

        float a = color.w * fade.getValueAt(progress);

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
            if (thirdPerson == DrawMode3p.PLANE_Z || thirdPerson == DrawMode3p.CROSS_XYZ) {
                // Z plane
                quad(vertices, pose, mat, a,
                        offsetX - f, offsetY + f, offsetZ, u1, v2,
                        offsetX - f, offsetY - f, offsetZ, u1, v1,
                        offsetX + f, offsetY - f, offsetZ, u2, v1,
                        offsetX + f, offsetY + f, offsetZ, u2, v2);
            }
            if (thirdPerson == DrawMode3p.CROSS_XY || thirdPerson == DrawMode3p.CROSS_XYZ) {
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
        vc.addVertex(mat, x, y, z).setColor(color.x, color.y, color.z, a).setUv(u, v).setLight(FULL_BRIGHT);
    }
}
