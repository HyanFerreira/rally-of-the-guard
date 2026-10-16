package net.hfstack.rallyguard.client.quick;

import java.util.List;

public record QuickCommandLayout(List<Slot> slots) {
    public static final int BUTTON_WIDTH = 96;
    public static final int BUTTON_HEIGHT = 20;

    public QuickCommandLayout {
        slots = List.copyOf(slots);
    }

    public static QuickCommandLayout forScreen(int screenWidth, int screenHeight) {
        int centerX = (screenWidth - BUTTON_WIDTH) / 2;
        int centerY = (screenHeight - BUTTON_HEIGHT) / 2;
        int radiusX = Math.min(132, Math.max(0, (screenWidth - BUTTON_WIDTH) / 2 - 4));
        int radiusY = Math.min(64, Math.max(0, (screenHeight - BUTTON_HEIGHT) / 2 - 4));
        return new QuickCommandLayout(List.of(
                new Slot(QuickCommandAction.FOLLOW, centerX, centerY - radiusY),
                new Slot(QuickCommandAction.ATTACK, centerX + radiusX, centerY - radiusY / 2),
                new Slot(QuickCommandAction.MOVE, centerX + radiusX, centerY + radiusY / 2),
                new Slot(QuickCommandAction.WAIT, centerX, centerY + radiusY),
                new Slot(QuickCommandAction.DEFEND, centerX - radiusX, centerY + radiusY / 2),
                new Slot(QuickCommandAction.PATROL, centerX - radiusX, centerY - radiusY / 2)
        ));
    }

    public record Slot(QuickCommandAction action, int x, int y) {
    }
}
