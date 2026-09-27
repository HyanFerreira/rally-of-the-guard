package net.hfstack.rallyguard.api.command;

import net.minecraft.text.Text;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class GuardCommandEventsTest {
    @Test
    void policiesStopAtFirstDenialAndAfterListenersObserveSuccessfulResults() {
        List<String> calls = new ArrayList<>();

        GuardCommandEvents.BEFORE.register(context -> {
            calls.add("allow");
            return GuardCommandDecision.allow();
        });
        GuardCommandEvents.BEFORE.register(context -> {
            calls.add("deny");
            return GuardCommandDecision.deny(Text.literal("denied"));
        });
        GuardCommandEvents.BEFORE.register(context -> {
            calls.add("unreachable");
            return GuardCommandDecision.allow();
        });

        GuardCommandDecision decision = GuardCommandEvents.BEFORE.invoker().evaluate(null);

        GuardCommandDecision.Deny denied = assertInstanceOf(GuardCommandDecision.Deny.class, decision);
        assertEquals("denied", denied.reason().getString());
        assertEquals(List.of("allow", "deny"), calls);

        GuardCommandEvents.AFTER.register((context, result) -> calls.add(result.outcome().name()));
        GuardCommandEvents.AFTER.invoker().onCommanded(
                null,
                GuardCommandResult.success("gui.rallyguard.command.follow_on")
        );

        assertEquals(List.of("allow", "deny", "SUCCESS"), calls);
    }
}
