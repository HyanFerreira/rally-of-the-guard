import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class GenerateFormationIcons {
    private static final int SIZE = 21;
    private static final int OUTLINE = 0xFF24160F;
    private static final int GUARD = 0xFF8A5A3B;
    private static final int GUARD_LIGHT = 0xFFE8D7B5;
    private static final int COMMANDER = 0xFFD89B18;
    private static final int COMMANDER_LIGHT = 0xFFFFE36B;
    private static final int SHIELD = 0xFF8B969F;
    private static final int SHIELD_LIGHT = 0xFFD3DADF;

    private GenerateFormationIcons() {
    }

    public static void main(String[] args) throws IOException {
        Path output = Path.of(
                "src", "main", "resources", "assets", "rallyguard",
                "textures", "gui", "formations"
        );
        Files.createDirectories(output);

        write(output, "escort", icon(canvas -> {
            commander(canvas, 10, 3);
            guard(canvas, 6, 8);
            guard(canvas, 14, 8);
            guard(canvas, 5, 14);
            guard(canvas, 15, 14);
            guard(canvas, 6, 19);
            guard(canvas, 14, 19);
        }));
        write(output, "column", icon(canvas -> {
            commander(canvas, 10, 2);
            guard(canvas, 10, 6);
            guard(canvas, 10, 10);
            guard(canvas, 10, 14);
            guard(canvas, 10, 18);
        }));
        write(output, "line", icon(canvas -> {
            commander(canvas, 10, 5);
            guard(canvas, 2, 13);
            guard(canvas, 6, 13);
            guard(canvas, 10, 13);
            guard(canvas, 14, 13);
            guard(canvas, 18, 13);
        }));
        write(output, "wedge", icon(canvas -> {
            commander(canvas, 10, 2);
            guard(canvas, 7, 7);
            guard(canvas, 13, 7);
            guard(canvas, 4, 12);
            guard(canvas, 16, 12);
            guard(canvas, 1, 17);
            guard(canvas, 19, 17);
        }));
        write(output, "square", icon(canvas -> {
            commander(canvas, 10, 2);
            for (int y : new int[]{8, 13, 18}) {
                for (int x : new int[]{5, 10, 15}) {
                    guard(canvas, x, y);
                }
            }
        }));
        write(output, "circle", icon(canvas -> {
            commander(canvas, 10, 10);
            for (int[] point : new int[][]{
                    {10, 2}, {16, 4}, {18, 10}, {16, 16},
                    {10, 18}, {4, 16}, {2, 10}, {4, 4}
            }) {
                guard(canvas, point[0], point[1]);
            }
        }));
        write(output, "shield_wall", icon(canvas -> {
            commander(canvas, 10, 4);
            for (int x : new int[]{2, 6, 10, 14, 18}) {
                guard(canvas, x, 12);
                shield(canvas, x, 15);
            }
        }));
    }

    private static BufferedImage icon(Painter painter) {
        BufferedImage image = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_ARGB);
        painter.paint(image);
        return image;
    }

    private static void guard(BufferedImage image, int centerX, int centerY) {
        unit(image, centerX, centerY, GUARD, GUARD_LIGHT);
    }

    private static void commander(BufferedImage image, int centerX, int centerY) {
        unit(image, centerX, centerY, COMMANDER, COMMANDER_LIGHT);
    }

    private static void unit(BufferedImage image, int centerX, int centerY, int body, int light) {
        pixel(image, centerX, centerY - 1, OUTLINE);
        pixel(image, centerX - 1, centerY, OUTLINE);
        pixel(image, centerX, centerY, light);
        pixel(image, centerX + 1, centerY, OUTLINE);
        pixel(image, centerX, centerY + 1, body);
    }

    private static void shield(BufferedImage image, int centerX, int centerY) {
        pixel(image, centerX - 1, centerY - 1, OUTLINE);
        pixel(image, centerX, centerY - 1, SHIELD_LIGHT);
        pixel(image, centerX + 1, centerY - 1, OUTLINE);
        pixel(image, centerX - 1, centerY, OUTLINE);
        pixel(image, centerX, centerY, SHIELD);
        pixel(image, centerX + 1, centerY, OUTLINE);
        pixel(image, centerX, centerY + 1, OUTLINE);
    }

    private static void pixel(BufferedImage image, int x, int y, int color) {
        if (x >= 0 && x < SIZE && y >= 0 && y < SIZE) image.setRGB(x, y, color);
    }

    private static void write(Path output, String name, BufferedImage image) throws IOException {
        ImageIO.write(image, "png", output.resolve(name + ".png").toFile());
    }

    @FunctionalInterface
    private interface Painter {
        void paint(BufferedImage image);
    }
}
