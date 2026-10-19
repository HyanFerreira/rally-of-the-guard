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
        ScreenLayout.HorizontalSpan card = ScreenLayout.ledgerRosterCardSpan(100, 264);
        ScreenLayout.HorizontalSpan row = ScreenLayout.ledgerRosterSpan(100, 264);
        ScreenLayout.VerticalSpan cardVertical = ScreenLayout.ledgerRosterCardVerticalSpan(50, 300);
        ScreenLayout.VerticalSpan firstRow = ScreenLayout.ledgerRosterRowVerticalSpan(50, 0);
        ScreenLayout.VerticalSpan fifthRow = ScreenLayout.ledgerRosterRowVerticalSpan(50, 4);
        ScreenLayout.VerticalSpan pager = ScreenLayout.ledgerPagerVerticalSpan(50, 300);

        assertEquals(10, row.x() - card.x());
        assertEquals(10, card.x() + card.width() - row.x() - row.width());
        assertEquals(10, firstRow.y() - cardVertical.y());
        assertEquals(0, cardVertical.y() + cardVertical.height() - fifthRow.y() - fifthRow.height());
        assertEquals(10, pager.y() - cardVertical.y() - cardVertical.height());
        assertEquals(10, 50 + 300 - 14 - pager.y() - pager.height());
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
                ScreenLayout.twoColumnSpans(12, 616, 264, 14, 10);

        assertEquals(List.of(
                new ScreenLayout.HorizontalSpan(26, 264),
                new ScreenLayout.HorizontalSpan(300, 314)
        ), columns);
    }
}
