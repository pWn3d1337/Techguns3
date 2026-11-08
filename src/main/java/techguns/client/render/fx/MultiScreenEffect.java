package techguns.client.render.fx;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.renderer.MultiBufferSource;

/**
 * Several effects layered on each other, each active in its own part of the progress range ({@code "type": "layers"} in
 * a screen effect file, e.g. the TFG muzzle flash).
 */
public record MultiScreenEffect(List<Layer> layers) implements IScreenEffect {

    /** Small z step between layers so they don't z-fight. */
    public static final float z_offset = 0.01f;

    static MapCodec<MultiScreenEffect> codec(Codec<IScreenEffect> effect) {
        return Layer.codec(effect).listOf().fieldOf("layers").xmap(MultiScreenEffect::new, MultiScreenEffect::layers);
    }

    @Override
    public void doRender(PoseStack poseStack, MultiBufferSource buffers, float progress, float offsetX, float offsetY, float offsetZ, float scale,
                         float rotX, float rotY, float rotZ, boolean is3p, String ammoVariant) {
        int i = 0;
        for (Layer layer : layers) {
            if (progress > layer.start && progress < layer.end) {
                float prog = (progress - layer.start) / (layer.end - layer.start);
                layer.effect.doRender(poseStack, buffers, prog, offsetX, offsetY, offsetZ + z_offset * i, scale * layer.scale, rotX, rotY, rotZ, is3p, ammoVariant);
                i++;
            }
        }
    }

    /**
     * One layer: an effect (a name or an inline effect) played over {@code start}..{@code end} of the progress, at
     * {@code scale} times the effect scale.
     */
    public record Layer(IScreenEffect effect, float start, float end, float scale) {
        static Codec<Layer> codec(Codec<IScreenEffect> effect) {
            return RecordCodecBuilder.create(i -> i.group(
                    effect.fieldOf("effect").forGetter(Layer::effect),
                    Codec.FLOAT.optionalFieldOf("start", 0f).forGetter(Layer::start),
                    Codec.FLOAT.optionalFieldOf("end", 1f).forGetter(Layer::end),
                    Codec.FLOAT.optionalFieldOf("scale", 1f).forGetter(Layer::scale)
            ).apply(i, Layer::new));
        }
    }
}
