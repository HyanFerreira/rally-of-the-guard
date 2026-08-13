package net.hfstack.rallyguard.api.recruitment;

import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class GuardRecruitmentEventsTest {
    @Test
    void beforePoliciesPassModifiedOffersInOrderAndStopAtDenial() {
        List<String> calls = new ArrayList<>();

        GuardRecruitmentEvents.BEFORE.register((context, offer) -> {
            calls.add("replace:" + offer.cost());
            return RecruitmentDecision.allow(new RecruitmentOffer(offer.paymentItemId(), 7));
        });
        GuardRecruitmentEvents.BEFORE.register((context, offer) -> {
            calls.add("deny:" + offer.cost());
            return RecruitmentDecision.deny(Text.literal("denied"));
        });
        GuardRecruitmentEvents.BEFORE.register((context, offer) -> {
            calls.add("unreachable");
            return RecruitmentDecision.allow(offer);
        });

        RecruitmentDecision decision = GuardRecruitmentEvents.BEFORE.invoker().evaluate(
                null,
                new RecruitmentOffer(Identifier.of("minecraft", "emerald"), 3)
        );

        RecruitmentDecision.Deny denied = assertInstanceOf(RecruitmentDecision.Deny.class, decision);
        assertEquals("denied", denied.reason().getString());
        assertEquals(List.of("replace:3", "deny:7"), calls);
    }
}
