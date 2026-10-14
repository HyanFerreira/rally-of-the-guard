package net.hfstack.rallyguard.profile;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

class GuardProfileLifecycleTest {

    @AfterEach
    void clearWarningState() {
        GuardProfileLifecycle.clearFutureSchemaWarnings();
    }

    @Test
    void futureSchemaWarningOccursOncePerGuardAndSchemaDuringServerSession() {
        UUID guardId = UUID.randomUUID();

        assertTrue(GuardProfileLifecycle.shouldWarnFutureSchema(guardId, 2));
        assertFalse(GuardProfileLifecycle.shouldWarnFutureSchema(guardId, 2));
        assertTrue(GuardProfileLifecycle.shouldWarnFutureSchema(guardId, 3));
        assertTrue(GuardProfileLifecycle.shouldWarnFutureSchema(UUID.randomUUID(), 2));
    }

    @Test
    void stoppedServerClearsFutureSchemaWarningState() {
        UUID guardId = UUID.randomUUID();

        assertTrue(GuardProfileLifecycle.shouldWarnFutureSchema(guardId, 2));
        GuardProfileLifecycle.clearFutureSchemaWarnings();

        assertTrue(GuardProfileLifecycle.shouldWarnFutureSchema(guardId, 2));
    }
}
