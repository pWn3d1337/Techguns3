package techguns.client.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import techguns.armor.TGArmorItem;
import techguns.client.model.armor.TGArmorModels;
import techguns.client.render.TGMatrixOps;

/**
 * Techguns 2 {@code RenderArmorItem}: an armor piece drawn as its custom armor model in the inventory, the hands, on
 * the ground and in item frames, showing only the model parts of its slot, with the piece's (camo) texture. Values are
 * the Techguns 2 ones.
 */
// MIGRATION(26.x): armor models and item rendering move to render states / special model renderers in 1.21.2+.
public class RenderArmorItem extends RenderItemBase {
    private final TGArmorItem armor;
    private final EquipmentSlot slot;
    /** Scale that makes the slot's part about one block big, and the height (blocks) of the part's centre in the model. */
    private final float fit;
    private final float centre;

    public RenderArmorItem(TGArmorItem armor) {
        super(null, null);
        this.armor = armor;
        this.slot = armor.getEquipmentSlot();
        // the Techguns 2 values relied on the 1.12 item origin; here the piece is centred on its part and scaled to fit
        this.fit = switch (slot) {
            case HEAD -> 1.3f;
            case CHEST -> 0.8f;
            case LEGS -> 0.95f;
            default -> 1.3f;
        };
        this.centre = switch (slot) {
            case HEAD -> -0.25f;
            case CHEST -> 0.375f;
            case LEGS -> 1.125f;
            default -> 1.3f;
        };
        this.scale_gui = 1.0f;
        this.scale_ego = 0.5f;
        this.scale_thirdp = 0.45f;
        this.scale_ground = 0.5f;
        this.scale_itemframe = 0.8f;
        this.translateType[0][1] = 0.1f;
        this.translateType[1][1] = 0.04f;
    }

    @Override
    public void render(ItemRenderContext context, PoseStack poseStack, MultiBufferSource buffers, int light, int overlay) {
        HumanoidModel<LivingEntity> model = TGArmorModels.get(armor.armorModel());
        Minecraft mc = Minecraft.getInstance();
        if (model == null || mc.level == null) {
            return;
        }
        // the inner layer (legs) like the worn armor
        ResourceLocation texture = armor.modelTexture(mc.level.registryAccess(), context.stack(), slot == EquipmentSlot.LEGS);
        if (texture == null) {
            return;
        }
        ItemDisplayContext transform = context.displayContext();
        poseStack.pushPose();
        applyTranslation(poseStack, transform);
        if (transform == ItemDisplayContext.GUI) {
            TGMatrixOps.rotate(poseStack, 180.0f, 0, 1.0f, 0);
        }
        float scale = fit * getScaleFactorFromTransform(transform);
        poseStack.scale(scale, scale, scale);
        setBaseRotation(poseStack, transform);
        poseStack.translate(0.0f, -centre, 0.0f);

        // the model is shared with the worn armor: a neutral pose and only this slot's parts, restored after
        model.young = false;
        model.setAllVisible(false);
        model.head.resetPose();
        model.hat.resetPose();
        model.body.resetPose();
        model.rightArm.resetPose();
        model.leftArm.resetPose();
        model.rightLeg.resetPose();
        model.leftLeg.resetPose();
        switch (slot) {
            case HEAD -> {
                model.head.visible = true;
                model.hat.visible = true;
            }
            case CHEST -> {
                model.body.visible = true;
                model.rightArm.visible = true;
                model.leftArm.visible = true;
            }
            case LEGS -> {
                model.body.visible = true;
                model.rightLeg.visible = true;
                model.leftLeg.visible = true;
            }
            case FEET -> {
                model.rightLeg.visible = true;
                model.leftLeg.visible = true;
            }
            default -> {
            }
        }
        model.renderToBuffer(poseStack, buffers.getBuffer(RenderType.armorCutoutNoCull(texture)), light, overlay, -1);
        model.setAllVisible(true);
        poseStack.popPose();
    }
}
