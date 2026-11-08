package techguns.client.render.fx;

import java.util.Map;
import java.util.Optional;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.renderer.MultiBufferSource;

/**
 * Picks the effect by the loaded ammo variant ({@code "type": "by_ammo"} in a screen effect file, e.g. the magic rifle
 * charge circle), with {@code default} for other variants; nothing is drawn without one.
 */
public record VariableScreenEffect(Map<String, IScreenEffect> ammo, Optional<IScreenEffect> fallback) implements IScreenEffect {

    static MapCodec<VariableScreenEffect> codec(Codec<IScreenEffect> effect) {
        return RecordCodecBuilder.mapCodec(i -> i.group(
                Codec.unboundedMap(Codec.STRING, effect).fieldOf("ammo").forGetter(VariableScreenEffect::ammo),
                effect.optionalFieldOf("default").forGetter(VariableScreenEffect::fallback)
        ).apply(i, VariableScreenEffect::new));
    }

    @Override
    public void doRender(PoseStack poseStack, MultiBufferSource buffers, float progress, float offsetX, float offsetY, float offsetZ, float scale,
                         float rotX, float rotY, float rotZ, boolean is3p, String ammoVariant) {
        IScreenEffect effect = ammoVariant != null ? ammo.get(ammoVariant) : null;
        if (effect == null) {
            effect = fallback.orElse(null);
        }
        if (effect != null) {
            effect.doRender(poseStack, buffers, progress, offsetX, offsetY, offsetZ, scale, rotX, rotY, rotZ, is3p, ammoVariant);
        }
    }
}
