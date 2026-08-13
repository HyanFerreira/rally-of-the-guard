package net.hfstack.rallyguard.api.recruitment;

import net.minecraft.util.Identifier;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RecruitmentOfferTest {
    @Test
    void acceptsFreeOffersButRejectsNegativeCosts() {
        Identifier emerald = Identifier.of("minecraft", "emerald");

        assertDoesNotThrow(() -> new RecruitmentOffer(emerald, 0));
        assertThrows(IllegalArgumentException.class, () -> new RecruitmentOffer(emerald, -1));
    }
}
