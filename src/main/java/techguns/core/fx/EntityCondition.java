package techguns.core.fx;

/**
 * Condition under which an entity-attached particle system keeps running (Techguns 2 {@code EntityCondition}).
 * Sent over the network as a byte id; evaluated by {@code techguns.client.particle.EntityConditions}.
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
}
