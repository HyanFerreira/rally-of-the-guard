package net.hfstack.rallyguard.screen;

public final class ScreenLayout {
    private ScreenLayout() {
    }

    public static int panelCenterX(int panelX, int panelWidth) {
        return panelX + panelWidth / 2;
    }

    public static int centeredLineY(int rowTop, int rowHeight, int lineHeight) {
        return rowTop + (rowHeight - lineHeight) / 2;
    }

    public static int boundedPanelWidth(
            int screenWidth,
            int preferredWidth,
            int minimumWidth,
            int screenPadding
    ) {
        int availableWidth = Math.max(0, screenWidth - Math.max(0, screenPadding) * 2);
        int idealWidth = Math.max(0, Math.max(preferredWidth, minimumWidth));
        return Math.min(idealWidth, availableWidth);
    }

    public static int ledgerRosterWidth(int panelWidth) {
        return Math.max(120, Math.max(0, panelWidth) * 43 / 100);
    }

    public static HorizontalSpan ledgerRosterCardSpan(int panelX, int rosterWidth) {
        return new HorizontalSpan(panelX + 24, Math.max(0, rosterWidth - 20));
    }

    public static HorizontalSpan ledgerRosterSpan(int panelX, int rosterWidth) {
        return new HorizontalSpan(panelX + 34, Math.max(0, rosterWidth - 40));
    }

    public static VerticalSpan ledgerRosterCardVerticalSpan(int panelY, int panelHeight) {
        return new VerticalSpan(panelY + 88, Math.max(0, panelHeight - 140));
    }

    public static VerticalSpan ledgerRosterRowVerticalSpan(int panelY, int rowIndex) {
        return new VerticalSpan(panelY + 92 + Math.max(0, rowIndex) * 30, 28);
    }

    public record HorizontalSpan(int x, int width) {
    }

    public record VerticalSpan(int y, int height) {
    }
}
