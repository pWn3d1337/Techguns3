package techguns.data.legacy;

import java.awt.image.BufferedImage;

/**
 * Converts Techguns 2 CTM textures (CTM mod format: the unconnected texture plus a {@code -ctm} sheet of twice its size)
 * into Fusion connecting texture sheets.
 */
final class CtmSheets {
    private CtmSheets() {
    }

    /**
     * CTM type {@code ctm} to Fusion layout {@code pieced} (5 tiles in a row: unconnected, fully connected, connected
     * up and down, connected left and right, connected on all sides but no corners). The CTM sheet's quadrants are, from
     * the top left: no borders, left and right borders, top and bottom borders, inner corners only.
     */
    static BufferedImage pieced(BufferedImage base, BufferedImage ctm) {
        int size = base.getWidth();
        BufferedImage out = new BufferedImage(size * 5, size, BufferedImage.TYPE_INT_ARGB);
        copy(base, 0, 0, out, 0, 0, size);
        copy(ctm, 0, 0, out, size, 0, size);
        copy(ctm, size, 0, out, size * 2, 0, size);
        copy(ctm, 0, size, out, size * 3, 0, size);
        copy(ctm, size, size, out, size * 4, 0, size);
        return out;
    }

    /**
     * CTM type {@code pillar} to Fusion layout {@code vertical} (4 tiles in a column: no connection, connected below,
     * connected both ways, connected above). The CTM sheet's quadrants: top left the short column, top right the top end
     * (connected below), bottom left the middle, bottom right the bottom end (connected above).
     */
    static BufferedImage vertical(BufferedImage ctm) {
        int size = ctm.getWidth() / 2;
        BufferedImage out = new BufferedImage(size, size * 4, BufferedImage.TYPE_INT_ARGB);
        copy(ctm, 0, 0, out, 0, 0, size);
        copy(ctm, size, 0, out, 0, size, size);
        copy(ctm, 0, size, out, 0, size * 2, size);
        copy(ctm, size, size, out, 0, size * 3, size);
        return out;
    }

    private static void copy(BufferedImage from, int fromX, int fromY, BufferedImage to, int toX, int toY, int size) {
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                to.setRGB(toX + x, toY + y, from.getRGB(fromX + x, fromY + y));
            }
        }
    }
}
