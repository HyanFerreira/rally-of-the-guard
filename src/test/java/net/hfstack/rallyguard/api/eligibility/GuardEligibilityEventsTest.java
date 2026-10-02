package net.hfstack.rallyguard.api.eligibility;

import net.minecraft.network.chat.Component;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class GuardEligibilityEventsTest {
    @Test
    void policiesRunInOrderAndStopAtFirstDenial() {
        List<String> calls = new ArrayList<>();

        GuardEligibilityEvents.CHECK.register(context -> {
            calls.add("allow");
            return GuardEligibilityDecision.allow();
        });
        GuardEligibilityEvents.CHECK.register(context -> {
            calls.add("deny");
            return GuardEligibilityDecision.deny(Component.literal("not eligible"));
        });
        GuardEligibilityEvents.CHECK.register(context -> {
            calls.add("unreachable");
            return GuardEligibilityDecision.allow();
        });

        GuardEligibilityDecision decision = GuardEligibilityEvents.CHECK.invoker().evaluate(null);

        GuardEligibilityDecision.Deny denied = assertInstanceOf(GuardEligibilityDecision.Deny.class, decision);
        assertEquals("not eligible", denied.reason().getString());
        assertEquals(List.of("allow", "deny"), calls);
    }
}
