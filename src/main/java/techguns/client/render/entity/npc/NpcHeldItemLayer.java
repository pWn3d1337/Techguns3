package techguns.client.render.entity.npc;

import java.util.function.Function;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * Techguns 2 {@code LayerHeldItemTranslateGun}: the held item of an NPC whose arms differ from the biped's (cyber
 * demon, skeleton soldier, super mutants), moved by the NPC's weapon offset ({@code INPCTechgunsShooter.getWeaponPos},
 * mirrored for the left hand) and scaled by its gun scale ({@code getGunScale}). Replaces the vanilla held item layer.
 */
// MIGRATION(26.x): render layers take render states in 1.21.2+.
public class NpcHeldItemLayer<T extends LivingEntity, M extends EntityModel<T> & ArmedModel> extends ItemInHandLayer<T, M> {
    /** Weapon offset in the hand's item space (blocks) and gun scale. */
    public record Offset(float x, float y, float z, float scale) {
    }

    private final ItemInHandRenderer itemInHandRenderer;
    private final Function<T, Offset> offset;

    public NpcHeldItemLayer(RenderLayerParent<T, M> renderer, ItemInHandRenderer itemInHandRenderer, Function<T, Offset> offset) {
        super(renderer, itemInHandRenderer);
        this.itemInHandRenderer = itemInHandRenderer;
        this.offset = offset;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void renderArmWithItem(LivingEntity entity, ItemStack stack, ItemDisplayContext displayContext, HumanoidArm arm, PoseStack poseStack,
                                     MultiBufferSource buffer, int packedLight) {
        if (stack.isEmpty()) {
            return;
        }
        Offset weapon = offset.apply((T) entity);
        poseStack.pushPose();
        getParentModel().translateToHand(arm, poseStack);
        poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        boolean left = arm == HumanoidArm.LEFT;
        poseStack.translate((left ? -1 : 1) / 16.0F, 0.125F, -0.625F);
        poseStack.translate(left ? -weapon.x() : weapon.x(), weapon.y(), weapon.z());
        poseStack.scale(weapon.scale(), weapon.scale(), weapon.scale());
        itemInHandRenderer.renderItem(entity, stack, displayContext, left, poseStack, buffer, packedLight);
        poseStack.popPose();
    }
}
