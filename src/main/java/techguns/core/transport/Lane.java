package techguns.core.transport;

/**
 * Fixed place of a pipe kind inside the shared pipe block: a cube centred at {@link #center} (in 1/16 block) on each
 * axis, i.e. on the block diagonal. Parts of different lanes never overlap (each part keeps at least two coordinates
 * within {@code center +- PLATE_HALF}), so up to three kinds share one block.
 */
public enum Lane {
    A(3.0), B(8.0), C(13.0);

    /** Half sizes of the core cube, the tube and the connector plate, and the plate thickness (1/16 block). */
    public static final double CORE_HALF = 2.0;
    public static final double TUBE_HALF = 1.5;
    public static final double PLATE_HALF = 2.5;
    public static final double PLATE_DEPTH = 1.0;

    public final double center;

    Lane(double center) {
        this.center = center;
    }
}
