package techguns.data.legacy;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.image.BufferedImage;

import org.junit.jupiter.api.Test;

class CtmSheetsTest {
    /** A size x size image filled with one colour. */
    private static BufferedImage solid(int size, int color) {
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                image.setRGB(x, y, color);
            }
        }
        return image;
    }

    /** A 2 size x 2 size image with a colour per quadrant: top left, top right, bottom left, bottom right. */
    private static BufferedImage quadrants(int size, int... colors) {
        BufferedImage image = new BufferedImage(size * 2, size * 2, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < size * 2; y++) {
            for (int x = 0; x < size * 2; x++) {
                image.setRGB(x, y, colors[(y / size) * 2 + x / size]);
            }
        }
        return image;
    }

    @Test
    void piecedPutsTheBaseFirstAndTheQuadrantsInReadingOrder() {
        BufferedImage sheet = CtmSheets.pieced(solid(16, 0xFF000000), quadrants(16, 0xFF000001, 0xFF000002, 0xFF000003, 0xFF000004));
        assertEquals(80, sheet.getWidth());
        assertEquals(16, sheet.getHeight());
        for (int tile = 0; tile < 5; tile++) {
            assertEquals(0xFF000000 + tile, sheet.getRGB(tile * 16 + 7, 7), "tile " + tile);
        }
    }

    @Test
    void verticalStacksTheQuadrants() {
        BufferedImage sheet = CtmSheets.vertical(quadrants(32, 0xFF000001, 0xFF000002, 0xFF000003, 0xFF000004));
        assertEquals(32, sheet.getWidth());
        assertEquals(128, sheet.getHeight());
        for (int tile = 0; tile < 4; tile++) {
            assertEquals(0xFF000001 + tile, sheet.getRGB(15, tile * 32 + 15), "tile " + tile);
        }
    }
}
