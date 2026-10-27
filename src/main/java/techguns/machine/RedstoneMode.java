package techguns.machine;

/** Techguns 2 machine redstone behaviour: when the machine is allowed to work. */
public enum RedstoneMode {
    IGNORE,
    HIGH,
    LOW;

    public boolean allows(boolean signal) {
        return this == IGNORE || (this == HIGH) == signal;
    }

    public RedstoneMode next() {
        return values()[(ordinal() + 1) % values().length];
    }

    /** Lang key of the mode name (Techguns 2 {@code techguns.redstonemode.*}). */
    public String translationKey() {
        return "techguns.redstonemode." + name().toLowerCase(java.util.Locale.ROOT);
    }

    public static RedstoneMode byId(int id) {
        return id >= 0 && id < values().length ? values()[id] : IGNORE;
    }
}
