package net.hfstack.rallyguard.api.captain;

import net.minecraft.network.chat.Component;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class CaptainEventsTest {
    @Test
    void beforePoliciesRunInOrderAndStopAtFirstDenial() {
        List<String> calls = new ArrayList<>();

        CaptainEvents.BEFORE.register(context -> {
            calls.add("allow");
            return CaptainDecision.allow();
        });
        CaptainEvents.BEFORE.register(context -> {
            calls.add("deny");
            return CaptainDecision.deny(Component.literal("denied"));
        });
        CaptainEvents.BEFORE.register(context -> {
            calls.add("unreachable");
            return CaptainDecision.allow();
        });

        CaptainDecision decision = CaptainEvents.BEFORE.invoker().evaluate(null);

        CaptainDecision.Deny denied = assertInstanceOf(CaptainDecision.Deny.class, decision);
        assertEquals("denied", denied.reason().getString());
        assertEquals(List.of("allow", "deny"), calls);
    }

    @Test
    void afterListenersKeepTheirOrderAndIsolateFailures() {
        List<String> calls = new ArrayList<>();

        CaptainEvents.AFTER.register((context, result) -> {
            calls.add("first");
            throw new IllegalStateException("listener failed");
        });
        CaptainEvents.AFTER.register((context, result) -> calls.add("second"));

        CaptainEvents.AFTER.invoker().onChanged(null, null);

        assertEquals(List.of("first", "second"), calls);
    }
}
