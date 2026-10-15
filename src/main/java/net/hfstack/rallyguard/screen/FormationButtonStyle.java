package net.hfstack.rallyguard.screen;

public final class FormationButtonStyle {
    private static final int WHITE = 0xFFFFFFFF;
    private static final int GOLD = 0xFFFFD25A;

    private FormationButtonStyle() {
    }

    public static int outlineColor(boolean selected, boolean highlighted) {
        if (highlighted) return WHITE;
        if (selected) return GOLD;
        return 0;
    }
}
