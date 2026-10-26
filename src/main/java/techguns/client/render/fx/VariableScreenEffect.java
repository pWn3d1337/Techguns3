package techguns.client.render.fx;

import java.util.HashMap;
import java.util.Map;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;

/**
 * Picks the effect by the loaded ammo variant (e.g. the magic rifle charge circle), with a default for other variants.
 */
public class VariableScreenEffect implements IScreenEffect {

    public final Map<String, IScreenEffect> effects = new HashMap<>();
    public IScreenEffect defaultEffect = null;

    @SafeVarargs
    public VariableScreenEffect(Map.Entry<String, IScreenEffect>... entries) {
        for (Map.Entry<String, IScreenEffect> entry : entries) {
            effects.put(entry.getKey(), entry.getValue());
        }
    }

    public VariableScreenEffect setDefaultEffect(IScreenEffect effect) {
        this.defaultEffect = effect;
        return this;
    }

    @Override
    public void doRender(PoseStack poseStack, MultiBufferSource buffers, float progress, float offsetX, float offsetY, float offsetZ, float scale,
                         float rotX, float rotY, float rotZ, boolean is3p, String ammoVariant) {
        IScreenEffect effect = ammoVariant != null ? effects.get(ammoVariant) : null;
        if (effect == null) {
            effect = defaultEffect;
        }
        if (effect != null) {
            effect.doRender(poseStack, buffers, progress, offsetX, offsetY, offsetZ, scale, rotX, rotY, rotZ, is3p, ammoVariant);
        }
    }
}
