package net.hfstack.rallyguard.order;

import org.junit.jupiter.api.Test;

import java.util.OptionalDouble;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GuardFollowAssistPolicyTest {
    @Test
    void leavesOrdinaryWalkingToNativeGuardVillagersAi() {
        assertTrue(GuardFollowAssistPolicy.speed(true, false, false, false, false, 25.0).isEmpty());
    }

    @Test
    void assistsASeparatedGuardWhenCommanderSprints() {
        assertSpeed(0.95, GuardFollowAssistPolicy.speed(
                true, false, false, true, false, 25.0
        ));
    }

    @Test
    void usesModerateCatchUpWhenGuardFallsFarBehind() {
        assertSpeed(1.15, GuardFollowAssistPolicy.speed(
                true, false, false, true, false, 100.0
        ));
    }

    @Test
    void neverOverridesRallyCombatOrMountedTravel() {
        assertTrue(GuardFollowAssistPolicy.speed(true, true, false, true, false, 100.0).isEmpty());
        assertTrue(GuardFollowAssistPolicy.speed(true, false, true, true, false, 100.0).isEmpty());
        assertTrue(GuardFollowAssistPolicy.speed(true, false, false, true, true, 100.0).isEmpty());
    }

    @Test
    void doesNotPullGuardWhenNativeFollowRangeIsAlreadySatisfied() {
        assertTrue(GuardFollowAssistPolicy.speed(true, false, false, true, false, 9.0).isEmpty());
    }

    private static void assertSpeed(double expected, OptionalDouble speed) {
        assertTrue(speed.isPresent());
        assertEquals(expected, speed.getAsDouble());
    }
}
