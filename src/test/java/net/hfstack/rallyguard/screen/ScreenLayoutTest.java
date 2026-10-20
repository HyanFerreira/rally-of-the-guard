package net.hfstack.rallyguard.screen;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ScreenLayoutTest {
    @Test
    void centersContentInsideItsPanelInsteadOfUsingAColumnOrigin() {
        assertEquals(310, ScreenLayout.panelCenterX(100, 420));
    }

    @Test
    void centersSingleLineTextVerticallyInsideCommandRow() {
        assertEquals(109, ScreenLayout.centeredLineY(100, 28, 9));
    }

    @Test
    void centersOneAndTwoLineBlocksBetweenDividerAndButtons() {
        assertEquals(143, ScreenLayout.centeredBlockY(131, 164, 9));
        assertEquals(137, ScreenLayout.centeredBlockY(131, 164, 21));
    }

    @Test
    void centersEveryLineOfAMultilineEmptyStateAsOneBlock() {
        assertEquals(List.of(120, 131),
                ScreenLayout.centeredTextLineYs(100, 160, 9, 2, 2));
    }

    @Test
    void guardDetailsUseAnEvenTwoPixelGapForExactVerticalCentering() {
        ScreenLayout.TextLines details = ScreenLayout.ledgerGuardDetailsText(86, 119, 9, 2);

        assertEquals(new ScreenLayout.TextLines(93, 104), details);
        assertEquals(6, details.firstY() - 87);
        assertEquals(6, 119 - details.secondY() - 9);
    }

    @Test
    void ledgerPanelUsesTheSameInsetAboveBetweenAndBelowItsContent() {
        ScreenLayout.TopBar topBar = ScreenLayout.ledgerTopBar(50, 306, 8, 20, 12);

        assertEquals(new ScreenLayout.TopBar(62, 82, 114, 344), topBar);
        assertEquals(12, topBar.titleY() - 50);
        assertEquals(12, topBar.buttonsY() - topBar.titleY() - 8);
        assertEquals(12, topBar.contentY() - topBar.buttonsY() - 20);
        assertEquals(12, 50 + 306 - topBar.contentBottomY());
    }

    @Test
    void ledgerVerticalLayoutPreservesSpacingAcrossResponsiveSizes() {
        ScreenLayout.LedgerVerticalLayout normal = ScreenLayout.ledgerVerticalLayout(
                306, 344, 9, 20, 4, 10, 10);
        ScreenLayout.LedgerVerticalLayout medium = ScreenLayout.ledgerVerticalLayout(
                288, 326, 9, 20, 4, 10, 10);
        ScreenLayout.LedgerVerticalLayout small = ScreenLayout.ledgerVerticalLayout(
                228, 266, 9, 20, 4, 10, 10);

        assertEquals(new ScreenLayout.LedgerVerticalLayout(
                223, 233, 169, 265, false, false, 3), normal);
        assertEquals(new ScreenLayout.LedgerVerticalLayout(
                230, 240, 176, 272, false, true, 2), medium);
        assertEquals(new ScreenLayout.LedgerVerticalLayout(
                202, 206, 156, 220, true, true, 2), small);
        assertEquals(10, normal.scopeTopY() - normal.individualBottomY());
        assertEquals(10, medium.scopeTopY() - medium.individualBottomY());
        assertEquals(4, small.scopeTopY() - small.individualBottomY());
    }

    @Test
    void ledgerRosterPageSizeUsesTheAvailableCardHeight() {
        assertEquals(5, ScreenLayout.ledgerRosterPageSize(168));
        assertEquals(4, ScreenLayout.ledgerRosterPageSize(150));
        assertEquals(2, ScreenLayout.ledgerRosterPageSize(100));
    }

    @Test
    void keepsExpandedMusterPanelInsideNarrowScreens() {
        assertEquals(420, ScreenLayout.boundedPanelWidth(640, 420, 320, 12));
        assertEquals(336, ScreenLayout.boundedPanelWidth(360, 420, 320, 12));
        assertEquals(276, ScreenLayout.boundedPanelWidth(300, 420, 320, 12));
    }

    @Test
    void reservesMoreLedgerSpaceForActionsThanForTheRoster() {
        assertEquals(264, ScreenLayout.ledgerRosterWidth(616));
        assertEquals(301, ScreenLayout.ledgerRosterWidth(700));
    }

    @Test
    void ledgerRowsStayInsideTheInnerRosterCard() {
        ScreenLayout.HorizontalSpan card = ScreenLayout.ledgerRosterCardSpan(100, 264, 12);
        ScreenLayout.HorizontalSpan row = ScreenLayout.ledgerRosterSpan(100, 264, 12);
        ScreenLayout.VerticalSpan pager = ScreenLayout.ledgerPagerVerticalSpan(344, 20, 10);
        ScreenLayout.VerticalSpan cardVertical =
                ScreenLayout.ledgerRosterCardVerticalSpan(9, 114, 344, false, 10, 20);
        ScreenLayout.VerticalSpan firstRow =
                ScreenLayout.ledgerRosterRowVerticalSpan(9, 114, false, 10, 0);
        ScreenLayout.VerticalSpan fifthRow =
                ScreenLayout.ledgerRosterRowVerticalSpan(9, 114, false, 10, 4);

        assertEquals(10, row.x() - card.x());
        assertEquals(10, card.x() + card.width() - row.x() - row.width());
        assertEquals(10, firstRow.y() - cardVertical.y());
        assertEquals(10, cardVertical.y() + cardVertical.height() - fifthRow.y() - fifthRow.height());
        assertEquals(10, pager.y() - cardVertical.y() - cardVertical.height());
        assertEquals(10, 344 - pager.y() - pager.height());
    }

    @Test
    void ledgerSectionHeadersKeepTheSameTitleAndDividerInsetsForEveryCard() {
        ScreenLayout.SectionHeader roster = ScreenLayout.ledgerSectionHeader(108, 9, false);
        ScreenLayout.SectionHeader individualOrders = ScreenLayout.ledgerSectionHeader(108, 9, false);
        ScreenLayout.SectionHeader scopeOrders = ScreenLayout.ledgerSectionHeader(224, 9, false);
        ScreenLayout.SectionHeader compactRoster = ScreenLayout.ledgerSectionHeader(108, 9, true);
        ScreenLayout.SectionHeader compactScopeOrders = ScreenLayout.ledgerSectionHeader(206, 9, true);

        assertEquals(new ScreenLayout.SectionHeader(116, 130), roster);
        assertEquals(roster, individualOrders);
        assertEquals(8, scopeOrders.titleY() - 224);
        assertEquals(5, scopeOrders.dividerY() - scopeOrders.titleY() - 9);
        assertEquals(new ScreenLayout.SectionHeader(110, 120), compactRoster);
        assertEquals(2, compactScopeOrders.titleY() - 206);
        assertEquals(1, compactScopeOrders.dividerY() - compactScopeOrders.titleY() - 9);
    }

    @Test
    void ledgerSectionBodiesUseTheSameInsetBelowTheirDividers() {
        ScreenLayout.SectionHeader individualOrders = ScreenLayout.ledgerSectionHeader(114, 9, false);
        ScreenLayout.SectionHeader scopeOrders = ScreenLayout.ledgerSectionHeader(233, 9, false);

        int individualContentY = ScreenLayout.ledgerSectionContentY(individualOrders, 10);
        int scopeContentY = ScreenLayout.ledgerSectionContentY(scopeOrders, 10);

        assertEquals(10, individualContentY - individualOrders.dividerY());
        assertEquals(10, scopeContentY - scopeOrders.dividerY());
    }

    @Test
    void evenlySpacedControlsKeepEveryGapAndReachBothEdgesWhenWidthHasRemainder() {
        List<ScreenLayout.HorizontalSpan> threeControls =
                ScreenLayout.evenlySpacedHorizontalSpans(100, 588, 3, 4);
        List<ScreenLayout.HorizontalSpan> fourControls =
                ScreenLayout.evenlySpacedHorizontalSpans(200, 290, 4, 4);

        assertEquals(List.of(
                new ScreenLayout.HorizontalSpan(100, 194),
                new ScreenLayout.HorizontalSpan(298, 193),
                new ScreenLayout.HorizontalSpan(495, 193)
        ), threeControls);
        assertEquals(List.of(
                new ScreenLayout.HorizontalSpan(200, 70),
                new ScreenLayout.HorizontalSpan(274, 70),
                new ScreenLayout.HorizontalSpan(348, 69),
                new ScreenLayout.HorizontalSpan(421, 69)
        ), fourControls);
    }

    @Test
    void distributedSizesPutIndivisibleRemainderInTheFirstIntervals() {
        assertEquals(List.of(5, 5, 5, 5, 4), ScreenLayout.evenlyDistributedSizes(24, 5));
        assertEquals(List.of(8, 8, 8), ScreenLayout.evenlyDistributedSizes(24, 3));
    }

    @Test
    void twoColumnLayoutKeepsOuterPaddingAndSectionGapIndependent() {
        List<ScreenLayout.HorizontalSpan> columns =
                ScreenLayout.twoColumnSpans(12, 616, 264, 12, 10);

        assertEquals(List.of(
                new ScreenLayout.HorizontalSpan(24, 264),
                new ScreenLayout.HorizontalSpan(298, 318)
        ), columns);
        assertEquals(12, columns.getFirst().x() - 12);
        assertEquals(12, 12 + 616 - columns.getLast().x() - columns.getLast().width());
    }
}
