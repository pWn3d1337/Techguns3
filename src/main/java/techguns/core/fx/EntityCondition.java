package techguns.core.fx;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import techguns.gun.ChargeGunItem;

/**
 * Condition under which an entity-attached particle system keeps running (Techguns 2 {@code EntityCondition}).
 * Sent over the network as a byte id.
 */
public enum EntityCondition {
    NONE(0),
    CHARGING_WEAPON(1),
    ENTITY_ALIVE(2);

    public final byte id;

    EntityCondition(int id) {
        this.id = (byte) id;
    }

    public static EntityCondition fromByte(byte id) {
        for (EntityCondition condition : values()) {
            if (condition.id == id) {
                return condition;
            }
        }
        return NONE;
    }

    public boolean evaluate(Entity entity) {
        return switch (this) {
            case CHARGING_WEAPON -> entity instanceof LivingEntity living && living.isUsingItem()
                    && living.getUseItem().getItem() instanceof ChargeGunItem;
            case ENTITY_ALIVE -> entity.isAlive();
            default -> true;
        };
    }
}
