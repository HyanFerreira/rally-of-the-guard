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

    public static int centeredBlockY(int areaTop, int areaBottom, int blockHeight) {
        int availableHeight = Math.max(0, areaBottom - areaTop);
        return areaTop + Math.max(0, (availableHeight - Math.max(0, blockHeight)) / 2);
    }

    public static List<Integer> centeredTextLineYs(
            int areaTop,
            int areaBottom,
            int lineHeight,
            int lineGap,
            int lineCount
    ) {
        if (lineCount <= 0) return List.of();
        int safeLineHeight = Math.max(0, lineHeight);
        int safeLineGap = Math.max(0, lineGap);
        int blockHeight = safeLineHeight * lineCount + safeLineGap * (lineCount - 1);
        int firstY = centeredBlockY(areaTop, areaBottom, blockHeight);
        List<Integer> lines = new ArrayList<>(lineCount);
        for (int index = 0; index < lineCount; index++) {
            lines.add(firstY + index * (safeLineHeight + safeLineGap));
        }
        return List.copyOf(lines);
    }

    public static TextLines ledgerGuardDetailsText(
            int dividerY,
            int buttonsY,
            int lineHeight,
            int lineGap
    ) {
        int safeLineHeight = Math.max(0, lineHeight);
        int safeLineGap = Math.max(0, lineGap);
        int firstY = centeredBlockY(
                dividerY + 1,
                buttonsY,
                safeLineHeight * 2 + safeLineGap
        );
        return new TextLines(firstY, firstY + safeLineHeight + safeLineGap);
    }

    public static TopBar ledgerTopBar(
            int panelY,
            int panelHeight,
            int titleHeight,
            int buttonHeight,
            int spacing
    ) {
        int safePanelHeight = Math.max(0, panelHeight);
        int safeTitleHeight = Math.max(0, titleHeight);
        int safeButtonHeight = Math.max(0, buttonHeight);
        int safeSpacing = Math.max(0, spacing);
        int titleY = panelY + safeSpacing;
        int buttonsY = titleY + safeTitleHeight + safeSpacing;
        return new TopBar(
                titleY,
                buttonsY,
                buttonsY + safeButtonHeight + safeSpacing,
                panelY + Math.max(0, safePanelHeight - safeSpacing)
        );
    }

    public static LedgerVerticalLayout ledgerVerticalLayout(
            int panelHeight,
            int contentBottom,
            int lineHeight,
            int buttonHeight,
            int controlGap,
            int sectionGap,
            int contentPadding
    ) {
        int safeButtonHeight = Math.max(0, buttonHeight);
        int safeControlGap = Math.max(0, controlGap);
        int safeSectionGap = Math.max(0, sectionGap);
        int safeContentPadding = Math.max(0, contentPadding);
        int twoButtonRowsHeight = safeButtonHeight * 2 + safeControlGap;

        if (panelHeight >= 306) {
            int scopeTop = contentBottom - 111;
            int individualBottom = scopeTop - safeSectionGap;
            int individualButtons = individualBottom - safeContentPadding - twoButtonRowsHeight;
            int scopeButtons = ledgerSectionContentY(
                    ledgerSectionHeader(scopeTop, lineHeight, false), safeContentPadding);
            return new LedgerVerticalLayout(
                    individualBottom, scopeTop, individualButtons, scopeButtons,
                    false, false, 3
            );
        }

        if (panelHeight >= 270) {
            int headerHeight = ledgerSectionHeader(0, lineHeight, false).dividerY();
            int scopeHeight = headerHeight + safeContentPadding
                    + twoButtonRowsHeight + safeContentPadding;
            int scopeTop = contentBottom - scopeHeight;
            int individualBottom = scopeTop - safeSectionGap;
            int individualButtons = individualBottom - safeContentPadding - twoButtonRowsHeight;
            int scopeButtons = ledgerSectionContentY(
                    ledgerSectionHeader(scopeTop, lineHeight, false), safeContentPadding);
            return new LedgerVerticalLayout(
                    individualBottom, scopeTop, individualButtons, scopeButtons,
                    false, true, 2
            );
        }

        int tightPadding = Math.max(2, safeContentPadding / 5);
        int headerHeight = ledgerSectionHeader(0, lineHeight, true).dividerY();
        int scopeHeight = headerHeight + tightPadding + twoButtonRowsHeight + tightPadding;
        int scopeTop = contentBottom - scopeHeight;
        int individualBottom = scopeTop - safeControlGap;
        int individualButtons = individualBottom - tightPadding - twoButtonRowsHeight;
        int scopeButtons = ledgerSectionContentY(
                ledgerSectionHeader(scopeTop, lineHeight, true), tightPadding);
        return new LedgerVerticalLayout(
                individualBottom, scopeTop, individualButtons, scopeButtons,
                true, true, 2
        );
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

    public static HorizontalSpan ledgerRosterCardSpan(int panelX, int rosterWidth, int panelPadding) {
        return new HorizontalSpan(panelX + Math.max(0, panelPadding) + 10, Math.max(0, rosterWidth - 20));
    }

    public static HorizontalSpan ledgerRosterSpan(int panelX, int rosterWidth, int panelPadding) {
        return new HorizontalSpan(panelX + Math.max(0, panelPadding) + 20, Math.max(0, rosterWidth - 40));
    }

    public static VerticalSpan ledgerRosterCardVerticalSpan(
            int lineHeight,
            int contentTop,
            int contentBottom,
            boolean compact,
            int contentPadding,
            int buttonHeight
    ) {
        int safePadding = Math.max(0, contentPadding);
        int cardTop = ledgerSectionHeader(contentTop, lineHeight, compact).dividerY();
        int cardBottom = ledgerPagerVerticalSpan(contentBottom, buttonHeight, safePadding).y() - safePadding;
        return new VerticalSpan(cardTop, Math.max(0, cardBottom - cardTop));
    }

    public static VerticalSpan ledgerRosterRowVerticalSpan(
            int lineHeight,
            int contentTop,
            boolean compact,
            int contentPadding,
            int rowIndex
    ) {
        int cardTop = ledgerSectionHeader(contentTop, lineHeight, compact).dividerY();
        return new VerticalSpan(
                cardTop + Math.max(0, contentPadding) + Math.max(0, rowIndex) * 30,
                28
        );
    }

    public static int ledgerRosterPageSize(int cardHeight) {
        return Math.max(1, (Math.max(0, cardHeight) - 18) / 30);
    }

    public static SectionHeader ledgerSectionHeader(int cardTop, int lineHeight, boolean compact) {
        int safeLineHeight = Math.max(0, lineHeight);
        int titleInset = compact ? 2 : 8;
        int dividerGap = compact ? 1 : 5;
        int titleY = cardTop + titleInset;
        return new SectionHeader(titleY, titleY + safeLineHeight + dividerGap);
    }

    public static int ledgerSectionContentY(SectionHeader header, int contentPadding) {
        return header.dividerY() + Math.max(0, contentPadding);
    }

    public static VerticalSpan ledgerPagerVerticalSpan(int contentBottom, int buttonHeight, int contentPadding) {
        int safeButtonHeight = Math.max(0, buttonHeight);
        return new VerticalSpan(
                contentBottom - Math.max(0, contentPadding) - safeButtonHeight,
                safeButtonHeight
        );
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

    public record SectionHeader(int titleY, int dividerY) {
    }

    public record TopBar(int titleY, int buttonsY, int contentY, int contentBottomY) {
    }

    public record TextLines(int firstY, int secondY) {
    }

    public record LedgerVerticalLayout(
            int individualBottomY,
            int scopeTopY,
            int individualButtonsY,
            int scopeButtonsY,
            boolean compactHeader,
            boolean compactLabels,
            int scopeRows
    ) {
    }
}
