package net.hfstack.rallyguard.screen;

import org.junit.jupiter.api.Test;

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

        assertEquals(10, row.x() - card.x());
        assertEquals(10, card.x() + card.width() - row.x() - row.width());
        assertEquals(4, firstRow.y() - cardVertical.y());
        assertEquals(8, cardVertical.y() + cardVertical.height() - fifthRow.y() - fifthRow.height());
    }
}
