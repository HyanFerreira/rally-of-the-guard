package net.hfstack.rallyguard.screen;

import java.util.ArrayList;
import java.util.List;

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
        return new VerticalSpan(panelY + 88, Math.max(0, panelHeight - 142));
    }

    public static VerticalSpan ledgerRosterRowVerticalSpan(int panelY, int rowIndex) {
        return new VerticalSpan(panelY + 98 + Math.max(0, rowIndex) * 30, 28);
    }

    public static VerticalSpan ledgerPagerVerticalSpan(int panelY, int panelHeight) {
        return new VerticalSpan(panelY + Math.max(0, panelHeight - 44), 20);
    }

    public static List<HorizontalSpan> evenlySpacedHorizontalSpans(int x, int width, int count, int gap) {
        if (count <= 0) return List.of();
        int safeWidth = Math.max(0, width);
        int safeGap = Math.max(0, gap);
        int availableWidth = Math.max(0, safeWidth - safeGap * (count - 1));
        List<Integer> widths = evenlyDistributedSizes(availableWidth, count);
        List<HorizontalSpan> spans = new ArrayList<>(count);
        int nextX = x;
        for (int index = 0; index < count; index++) {
            int spanWidth = widths.get(index);
            spans.add(new HorizontalSpan(nextX, spanWidth));
            nextX += spanWidth + safeGap;
        }
        return List.copyOf(spans);
    }

    public static List<Integer> evenlyDistributedSizes(int total, int count) {
        if (count <= 0) return List.of();
        int safeTotal = Math.max(0, total);
        int baseSize = safeTotal / count;
        int remainder = safeTotal % count;
        List<Integer> sizes = new ArrayList<>(count);
        for (int index = 0; index < count; index++) {
            sizes.add(baseSize + (index < remainder ? 1 : 0));
        }
        return List.copyOf(sizes);
    }

    public static List<HorizontalSpan> twoColumnSpans(
            int x,
            int width,
            int leftWidth,
            int outerPadding,
            int gap
    ) {
        int safeWidth = Math.max(0, width);
        int safePadding = Math.max(0, outerPadding);
        int safeGap = Math.max(0, gap);
        int innerX = x + safePadding;
        int innerRight = x + Math.max(safePadding, safeWidth - safePadding);
        int availableWidth = Math.max(0, innerRight - innerX);
        int safeLeftWidth = Math.min(Math.max(0, leftWidth), Math.max(0, availableWidth - safeGap));
        int rightX = innerX + safeLeftWidth + safeGap;
        return List.of(
                new HorizontalSpan(innerX, safeLeftWidth),
                new HorizontalSpan(rightX, Math.max(0, innerRight - rightX))
        );
    }

    public record HorizontalSpan(int x, int width) {
    }

    public record VerticalSpan(int y, int height) {
    }
}
