package net.hfstack.rallyguard.api.recruitment;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

import java.util.Objects;

public final class GuardRecruitmentEvents {
    private GuardRecruitmentEvents() {
    }

    /**
     * Policies run in registration order. Each allowed offer is passed to the next listener;
     * the first denial stops evaluation.
     */
    public static final Event<Before> BEFORE = EventFactory.createArrayBacked(
            Before.class,
            listeners -> (context, initialOffer) -> {
                RecruitmentOffer currentOffer = initialOffer;
                for (Before listener : listeners) {
                    RecruitmentDecision decision = Objects.requireNonNull(
                            listener.evaluate(context, currentOffer),
                            "Recruitment BEFORE listener returned null"
                    );
                    if (decision instanceof RecruitmentDecision.Deny) {
                        return decision;
                    }
                    currentOffer = ((RecruitmentDecision.Allow) decision).offer();
                }
                return RecruitmentDecision.allow(currentOffer);
            }
    );

    /**
     * Notification emitted after ownership has been assigned successfully.
     */
    public static final Event<After> AFTER = EventFactory.createArrayBacked(
            After.class,
            listeners -> (context, appliedOffer) -> {
                for (After listener : listeners) {
                    listener.onRecruited(context, appliedOffer);
                }
            }
    );

    @FunctionalInterface
    public interface Before {
        RecruitmentDecision evaluate(RecruitmentContext context, RecruitmentOffer currentOffer);
    }

    @FunctionalInterface
    public interface After {
        void onRecruited(RecruitmentContext context, RecruitmentOffer appliedOffer);
    }
}
