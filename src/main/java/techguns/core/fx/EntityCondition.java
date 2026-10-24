package techguns.core.fx;

import net.minecraft.world.entity.Entity;

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
            // TODO charge-up weapons: needs the player's weapon charge state (Phase 3); until then the system stops at once
            case CHARGING_WEAPON -> false;
            case ENTITY_ALIVE -> entity.isAlive();
            default -> true;
        };
    }
}
