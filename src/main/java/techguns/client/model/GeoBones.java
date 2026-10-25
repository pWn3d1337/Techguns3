package techguns.client.model;

/**
 * Code-driven bone changes of a geo gun model for one frame (belts, single moving parts). Bone names are the
 * {@link TGModelPart} field names of the Java model ({@link TGGeoExporter}); offsets are in Java model pixels.
 */
public interface GeoBones {
    void setHidden(String bone, boolean hidden);

    /** Moves a bone by an offset in Java model space (pixels, y down). */
    void setOffset(String bone, float x, float y, float z);

    /** Scales a bone around its pivot, after its rotation. */
    void setScale(String bone, float x, float y, float z);
}
