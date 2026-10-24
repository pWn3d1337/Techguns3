package techguns.client.render.item;

import javax.annotation.Nullable;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * Everything a Techguns item renderer may know about the current draw, collected before rendering.
 * Renderers must not query the game state themselves (extract-state-then-render, design doc §4.7).
 *
 * @param entity  entity holding the item, if rendered in a hand; null in GUI, on the ground, in frames
 * @param hand    hand holding the item, if {@code entity} is set
 * @param partialTick frame partial tick
 */
// MIGRATION(26.x): built by the BlockEntityWithoutLevelRenderer adapter now; later from item model special renderers / render states.
public record ItemRenderContext(ItemStack stack, ItemDisplayContext displayContext, @Nullable LivingEntity entity,
                                @Nullable InteractionHand hand, float partialTick) {

    public boolean isFirstPerson() {
        return displayContext == ItemDisplayContext.FIRST_PERSON_LEFT_HAND || displayContext == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND;
    }

    public boolean isThirdPerson() {
        return displayContext == ItemDisplayContext.THIRD_PERSON_LEFT_HAND || displayContext == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
    }

    public boolean isInHand() {
        return isFirstPerson() || isThirdPerson();
    }

    /** Rendered on the left side of the body (mirrored). */
    public boolean isLeftSide() {
        return displayContext == ItemDisplayContext.FIRST_PERSON_LEFT_HAND || displayContext == ItemDisplayContext.THIRD_PERSON_LEFT_HAND;
    }

    public boolean isOffhand() {
        return hand == InteractionHand.OFF_HAND;
    }

    public boolean isSneaking() {
        return entity != null && entity.isCrouching();
    }

    /** Hand derived from the display side and the entity's main arm. */
    @Nullable
    static InteractionHand handFor(@Nullable LivingEntity entity, ItemDisplayContext context) {
        if (entity == null) {
            return null;
        }
        boolean left = context == ItemDisplayContext.FIRST_PERSON_LEFT_HAND || context == ItemDisplayContext.THIRD_PERSON_LEFT_HAND;
        boolean leftIsMain = entity.getMainArm() == HumanoidArm.LEFT;
        return left == leftIsMain ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
    }
}
