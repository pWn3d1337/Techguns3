package techguns.core.transport;

/**
 * How a pipe segment treats one of its six sides. Towards a block (an endpoint): {@link #NEUTRAL} accepts what the block
 * pushes in and delivers to it, {@link #OUTPUT} only delivers, {@link #INPUT} pulls from the block (and accepts pushes),
 * {@link #DISABLED} does nothing. Towards another pipe only connected (any mode but disabled) or disabled matter.
 */
public enum SideMode {
    NEUTRAL, OUTPUT, INPUT, DISABLED;

    private static final SideMode[] VALUES = values();

    /** Wrench cycle on an endpoint side. */
    public SideMode next() {
        return VALUES[(ordinal() + 1) % VALUES.length];
    }

    /** The network pulls from the block on this side. */
    public boolean pulls() {
        return this == INPUT;
    }

    /** The block on this side may push into the pipe. */
    public boolean acceptsPush() {
        return this == NEUTRAL || this == INPUT;
    }

    /** The network delivers to the block on this side. */
    public boolean delivers() {
        return this == NEUTRAL || this == OUTPUT;
    }

    public boolean connects() {
        return this != DISABLED;
    }

    public static SideMode byId(int id) {
        return id >= 0 && id < VALUES.length ? VALUES[id] : NEUTRAL;
    }
}
