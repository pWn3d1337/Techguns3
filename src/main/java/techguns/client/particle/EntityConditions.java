package techguns.client.particle;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import techguns.core.fx.EntityCondition;
import techguns.gun.ChargeGunItem;

/** Evaluates an {@link EntityCondition} of an entity-attached particle system (kept out of {@code core}, it needs game types). */
public final class EntityConditions {
    private EntityConditions() {
    }

    public static boolean holds(EntityCondition condition, Entity entity) {
        return switch (condition) {
            case CHARGING_WEAPON -> entity instanceof LivingEntity living && living.isUsingItem()
                    && living.getUseItem().getItem() instanceof ChargeGunItem;
            case ENTITY_ALIVE -> entity.isAlive();
            case NONE -> true;
        };
    }
}
