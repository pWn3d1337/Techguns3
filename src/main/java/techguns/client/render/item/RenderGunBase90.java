package techguns.client.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import techguns.client.model.GeoGunModel;
import techguns.client.model.TGGeoBridge;
import techguns.client.model.TGModel;
import techguns.client.render.TGMatrixOps;

/**
 * Guns whose Java model points along x: the base rotation turns them 90 degrees more. Their geo files have that turn
 * baked in (barrel along -Z like every gun, {@code tools/geo_turn.py}), so a geo draw uses the normal base rotation and
 * the base translation turned along; {@link GeoGunModel#turned} turns the Java model's part transforms to match.
 */
public class RenderGunBase90 extends RenderGunBase {

    public RenderGunBase90(TGModel model, ResourceLocation texture) {
        super(model, texture);
        turnGeo(model);
    }

    public RenderGunBase90(TGModel model, int parts, ResourceLocation texture) {
        super(model, parts, texture);
        turnGeo(model);
    }

    private static void turnGeo(TGModel model) {
        if (model instanceof GeoGunModel geo) {
            geo.turned();
        }
    }

    /** Drawn from the turned geo files (not the Java fallback, {@code /tgdebug geo}). */
    private boolean geoTurned() {
        return this.model instanceof GeoGunModel && TGGeoBridge.enabled();
    }

    @Override
    protected void setBaseRotation(PoseStack poseStack, ItemDisplayContext transform) {
        if (this.geoTurned()) {
            super.setBaseRotation(poseStack, transform);
            return;
        }
        TGMatrixOps.rotate(poseStack, -180.0f, 1.0f, 0, 0);
        TGMatrixOps.rotate(poseStack, -90.0f, 0f, 1.0f, 0);
    }

    @Override
    protected void applyBaseTranslation(PoseStack poseStack) {
        if (this.geoTurned()) {
            // the Java turn Ry(90) moved in front of the translation: (x, y, z) -> (z, y, -x)
            poseStack.translate(this.translateBase[2], this.translateBase[1], -this.translateBase[0]);
            return;
        }
        super.applyBaseTranslation(poseStack);
    }
}
