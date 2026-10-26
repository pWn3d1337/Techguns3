package techguns.slot;

import java.util.Locale;

/**
 * Extra equipment slots (Techguns 2 {@code TGSlotType} face/back/hand). With Curios they map onto Curios' preset
 * slots, without it onto Techguns' own fallback slots.
 */
public enum TGSlotType {
    FACE("head"), BACK("back"), HAND("hands");

    private final String curiosId;

    TGSlotType(String curiosId) {
        this.curiosId = curiosId;
    }

    /** Curios slot identifier this slot maps to. */
    public String curiosId() {
        return curiosId;
    }

    public String id() {
        return name().toLowerCase(Locale.ROOT);
    }
}
