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
}
