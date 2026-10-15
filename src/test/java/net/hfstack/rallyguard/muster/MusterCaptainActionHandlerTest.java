package net.hfstack.rallyguard.muster;

import net.hfstack.rallyguard.api.captain.CaptainAction;
import net.hfstack.rallyguard.api.captain.CaptainMutationResult;
import net.hfstack.rallyguard.api.captain.CaptainOffer;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MusterCaptainActionHandlerTest {
    private static final CaptainOffer OFFER = new CaptainOffer(
            Identifier.fromNamespaceAndPath("minecraft", "emerald"), 4
    );

    @Test
    void missingOrInvalidSessionStatesNeverInvokeService() {
        AtomicInteger serviceCalls = new AtomicInteger();
        for (String invalidState : List.of("missing", "wrong-item", "out-of-range", "stale-entity")) {
            boolean handled = MusterCaptainActionHandler.handle(
                    CaptainAction.PROMOTE,
                    Optional::<Object>empty,
                    (guard, action) -> {
                        serviceCalls.incrementAndGet();
                        return success(action);
                    },
                    ignored -> { },
                    () -> { }
            );
            assertFalse(handled, invalidState);
        }
        assertEquals(0, serviceCalls.get());
    }

    @Test
    void noneActionIsRejectedBeforeSessionOrServiceLookup() {
        AtomicInteger lookups = new AtomicInteger();
        AtomicInteger serviceCalls = new AtomicInteger();

        boolean handled = MusterCaptainActionHandler.handle(
                CaptainAction.NONE,
                () -> {
                    lookups.incrementAndGet();
                    return Optional.of(new Object());
                },
                (guard, action) -> {
                    serviceCalls.incrementAndGet();
                    return success(action);
                },
                ignored -> { },
                () -> { }
        );

        assertFalse(handled);
        assertEquals(0, lookups.get());
        assertEquals(0, serviceCalls.get());
    }

    @Test
    void everyRequestUsesFreshServiceStateAndSuccessRefreshesImmediately() {
        AtomicBoolean affordableNow = new AtomicBoolean(false);
        AtomicInteger serviceCalls = new AtomicInteger();
        AtomicInteger refreshes = new AtomicInteger();
        List<CaptainMutationResult.Outcome> feedback = new ArrayList<>();

        MusterCaptainActionHandler.ActionExecutor<Object> service = (guard, action) -> {
            serviceCalls.incrementAndGet();
            return affordableNow.get()
                    ? success(action)
                    : CaptainMutationResult.rejected(
                            action,
                            CaptainMutationResult.Outcome.INSUFFICIENT_PAYMENT,
                            "gui.rallyguard.captain.insufficient_payment"
                    );
        };

        assertTrue(MusterCaptainActionHandler.handle(
                CaptainAction.PROMOTE,
                () -> Optional.of(new Object()),
                service,
                component -> feedback.add(CaptainMutationResult.Outcome.INSUFFICIENT_PAYMENT),
                refreshes::incrementAndGet
        ));
        affordableNow.set(true);
        assertTrue(MusterCaptainActionHandler.handle(
                CaptainAction.PROMOTE,
                () -> Optional.of(new Object()),
                service,
                component -> feedback.add(CaptainMutationResult.Outcome.SUCCESS),
                refreshes::incrementAndGet
        ));

        assertEquals(2, serviceCalls.get());
        assertEquals(2, feedback.size());
        assertEquals(1, refreshes.get());
    }

    private static CaptainMutationResult success(CaptainAction action) {
        return CaptainMutationResult.success(
                action,
                action == CaptainAction.PROMOTE ? OFFER : null,
                "gui.rallyguard.captain.changed"
        );
    }
}
