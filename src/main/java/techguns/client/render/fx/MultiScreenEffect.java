package techguns.client.render.fx;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;

/**
 * Several effects layered on each other, each active in its own part of the progress range (e.g. the TFG muzzle flash).
 */
public class MultiScreenEffect implements IScreenEffect {

    /** Small z step between layers so they don't z-fight. */
    public static final float z_offset = 0.01f;

    public final List<EffectEntry> effects = new ArrayList<>();

    @Override
    public void doRender(PoseStack poseStack, MultiBufferSource buffers, float progress, float offsetX, float offsetY, float offsetZ, float scale,
                         float rotX, float rotY, float rotZ, boolean is3p, String ammoVariant) {
        int i = 0;
        for (EffectEntry entry : effects) {
            if (progress > entry.start && progress < entry.end) {
                float prog = (progress - entry.start) / (entry.end - entry.start);
                entry.effect.doRender(poseStack, buffers, prog, offsetX, offsetY, offsetZ + z_offset * i, scale * entry.scale, rotX, rotY, rotZ, is3p, ammoVariant);
                i++;
            }
        }
    }

    public MultiScreenEffect add(IScreenEffect effect, float start, float end, float scale) {
        effects.add(new EffectEntry(effect, start, end, scale));
        return this;
    }

    public MultiScreenEffect add(IScreenEffect effect) {
        effects.add(new EffectEntry(effect, 0.0f, 1.0f, 1.0f));
        return this;
    }

    public record EffectEntry(IScreenEffect effect, float start, float end, float scale) {
    }
}
