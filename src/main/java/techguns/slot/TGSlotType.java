package techguns.slot;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Extra equipment slots (Techguns 2 {@code TGSlotType} face/back/hand, and the mouth for the Auto-Feeder that replaces
 * the Techguns 2 auto-feeder slots). With Curios they map onto Curios' preset slots, without it onto Techguns' own
 * fallback slots.
 */
public enum TGSlotType {
    FACE("head", true), BACK("back", true), HAND("hands", true), MOUTH("mouth", false);

    private final String curiosId;
    private final boolean camo;

    TGSlotType(String curiosId, boolean camo) {
        this.curiosId = curiosId;
        this.camo = camo;
    }

    /** The slots of the gear the Camo Bench changes (Techguns 2: face, back and hand). */
    public static List<TGSlotType> camoSlots() {
        return Arrays.stream(values()).filter(slot -> slot.camo).toList();
    }

    /** Curios slot identifier this slot maps to. */
    public String curiosId() {
        return curiosId;
    }

    public String id() {
        return name().toLowerCase(Locale.ROOT);
    }
}
