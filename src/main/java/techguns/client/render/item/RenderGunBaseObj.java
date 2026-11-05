package techguns.client.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import techguns.client.model.GeoGunModel;
import techguns.client.model.TGGeoBridge;
import techguns.client.model.TGModel;
import techguns.client.render.TGMatrixOps;

/**
 * Gun renderer for OBJ mesh models (Fabric {@code RenderGunBaseObj}): the base rotation of the Java model is a single
 * turn about Y; a geo model with mesh bones uses the normal base rotation.
 */
public class RenderGunBaseObj extends RenderGunBase {

    protected final float rotY;

    public RenderGunBaseObj(TGModel model, int parts, ResourceLocation texture, float rotY) {
        super(model, parts, texture);
        this.rotY = rotY;
    }

    @Override
    protected void setBaseRotation(PoseStack poseStack, ItemDisplayContext transform) {
        if (this.model instanceof GeoGunModel && TGGeoBridge.enabled()) {
            // the geo file points along -Z like every gun; its mesh bones carry the OBJ turn (TGModel#renderMesh)
            this.setGeoBaseRotation(poseStack, transform);
            return;
        }
        TGMatrixOps.rotate(poseStack, rotY, 0f, 1.0f, 0f);
    }
}
