package net.hfstack.rallyguard.api.recruitment;

import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class RecruitmentQuoteTest {
    private static final UUID GUARD = UUID.fromString("00000000-0000-0000-0000-000000000123");

    @Test
    void fingerprintIsStableForTheSameGuardProfileAndOffer() {
        RecruitmentOffer offer = offer("emerald", 5);

        assertEquals(
                RecruitmentQuote.create(GUARD, 20_000, offer),
                RecruitmentQuote.create(GUARD, 20_000, offer)
        );
    }

    @Test
    void fingerprintChangesWithEveryIntegrityInput() {
        RecruitmentQuote baseline = RecruitmentQuote.create(GUARD, 20_000, offer("emerald", 5));

        assertNotEquals(baseline, RecruitmentQuote.create(new UUID(1, 2), 20_000, offer("emerald", 5)));
        assertNotEquals(baseline, RecruitmentQuote.create(GUARD, 20_001, offer("emerald", 5)));
        assertNotEquals(baseline, RecruitmentQuote.create(GUARD, 20_000, offer("emerald", 6)));
        assertNotEquals(baseline, RecruitmentQuote.create(GUARD, 20_000, offer("diamond", 5)));
    }

    private static RecruitmentOffer offer(String item, int cost) {
        return new RecruitmentOffer(Identifier.fromNamespaceAndPath("minecraft", item), cost);
    }
}
