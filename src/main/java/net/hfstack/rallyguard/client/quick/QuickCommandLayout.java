package net.hfstack.rallyguard.client.quick;

import java.util.List;

public record QuickCommandLayout(List<Slot> slots) {
    public static final int PANEL_WIDTH = 312;
    public static final int PANEL_HEIGHT = 140;
    public static final int BUTTON_WIDTH = 136;
    public static final int BUTTON_HEIGHT = 20;
    public static final int PANEL_PADDING = 14;
    public static final int COLUMN_GAP = 12;
    public static final int ROW_GAP = 6;
    public static final int ACTIONS_TOP = 50;

    public QuickCommandLayout {
        slots = List.copyOf(slots);
    }

    public static QuickCommandLayout forScreen(int screenWidth, int screenHeight) {
        int panelX = (screenWidth - PANEL_WIDTH) / 2;
        int panelY = (screenHeight - PANEL_HEIGHT) / 2;
        int left = panelX + PANEL_PADDING;
        int right = left + BUTTON_WIDTH + COLUMN_GAP;
        int firstRow = panelY + ACTIONS_TOP;
        int secondRow = firstRow + BUTTON_HEIGHT + ROW_GAP;
        int thirdRow = secondRow + BUTTON_HEIGHT + ROW_GAP;
        return new QuickCommandLayout(List.of(
                new Slot(QuickCommandAction.FOLLOW, left, firstRow),
                new Slot(QuickCommandAction.WAIT, right, firstRow),
                new Slot(QuickCommandAction.PATROL, left, secondRow),
                new Slot(QuickCommandAction.ATTACK, right, secondRow),
                new Slot(QuickCommandAction.MOVE, left, thirdRow),
                new Slot(QuickCommandAction.DEFEND, right, thirdRow)
        ));
    }

    public record Slot(QuickCommandAction action, int x, int y) {
    }
}
