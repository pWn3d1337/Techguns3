package techguns.client.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import techguns.gun.ChargeGunItem;
import techguns.gun.GrenadeItem;
import techguns.gun.GunItem;
import techguns.gun.HoldType;

/**
 * Hooks code-rendered items into vanilla rendering and sets third-person arm poses for guns.
 */
public final class TGClientItemExtensions implements IClientItemExtensions {
    private static final TGClientItemExtensions INSTANCE = new TGClientItemExtensions();

    private TGClientItemExtensions() {
    }

    public static void register(RegisterClientExtensionsEvent event) {
        TGItemRendererSetup.registerAll();
        for (Item item : TGItemRenderers.items()) {
            event.registerItem(INSTANCE, item);
        }
    }

    @Override
    public BlockEntityWithoutLevelRenderer getCustomRenderer() {
        return TGItemRendererAdapter.INSTANCE;
    }

    /**
     * Hand grenades and charge guns: vanilla drops the hand like a re-equip when a use starts ({@code ItemInHandRenderer.itemUsed}); while
     * winding up or charging, draw the item fully raised instead (vanilla {@code applyItemArmTransform} with no equip offset).
     */
    @Override
    public boolean applyForgeHandTransform(PoseStack poseStack, LocalPlayer player, HumanoidArm arm, ItemStack stack, float partialTick,
                                           float equipProcess, float swingProcess) {
        if (!(stack.getItem() instanceof GrenadeItem || stack.getItem() instanceof ChargeGunItem) || !player.isUsingItem()
                || !ItemStack.isSameItem(player.getUseItem(), stack)) {
            return false;
        }
        int side = arm == HumanoidArm.RIGHT ? 1 : -1;
        poseStack.translate(side * 0.56f, -0.52f, -0.72f);
        return true;
    }

    @Override
    public HumanoidModel.ArmPose getArmPose(LivingEntity entity, InteractionHand hand, ItemStack stack) {
        if (stack.getItem() instanceof GunItem gun) {
            if (entity.isFallFlying()) {
                return TGArmPoses.elytraAim();
            }
            boolean akimbo = entity.getItemInHand(hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND)
                    .getItem() instanceof GunItem;
            HoldType hold = gun.stats().holdType();
            if (hold == HoldType.BOW || (hold == HoldType.CROSSBOW && akimbo)) {
                return HumanoidModel.ArmPose.BOW_AND_ARROW;
            }
            if (hold == HoldType.CROSSBOW) {
                return HumanoidModel.ArmPose.CROSSBOW_HOLD;
            }
        }
        return HumanoidModel.ArmPose.ITEM;
    }
}
