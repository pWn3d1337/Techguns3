package techguns.client.render.item;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
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

    @Override
    public HumanoidModel.ArmPose getArmPose(LivingEntity entity, InteractionHand hand, ItemStack stack) {
        if (stack.getItem() instanceof GunItem gun) {
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
