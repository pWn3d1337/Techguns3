package techguns.client.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import techguns.client.model.TGModel;
import techguns.client.render.TGMatrixOps;

public class RenderItemBaseRocketItem extends RenderItemBase {

    public RenderItemBaseRocketItem(TGModel model, ResourceLocation texture) {
        super(model, texture);
    }

    @Override
    protected void setBaseRotation(PoseStack poseStack, ItemDisplayContext transform) {
        TGMatrixOps.rotate(poseStack, -180.0f, 1.0f, 0, 0);
        TGMatrixOps.rotate(poseStack, -90.0f, 0f, 1.0f, 0);
        TGMatrixOps.rotate(poseStack, -90.0f, 0f, 0f, 1);
    }
}
