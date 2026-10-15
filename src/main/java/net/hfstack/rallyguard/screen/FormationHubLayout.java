package net.hfstack.rallyguard.screen;

public final class FormationHubLayout {
    public static final int SCREEN_PADDING = 12;
    public static final int CELL_WIDTH = 36;

    private FormationHubLayout() {
    }

    public static Grid forScreen(int screenWidth, int itemCount) {
        int availableWidth = Math.max(0, screenWidth - SCREEN_PADDING * 2);
        int columns = Math.max(1, Math.min(Math.max(1, itemCount), availableWidth / CELL_WIDTH));
        int rows = itemCount <= 0 ? 0 : (itemCount + columns - 1) / columns;
        return new Grid(columns, rows);
    }

    public record Grid(int columns, int rows) {
    }
}
