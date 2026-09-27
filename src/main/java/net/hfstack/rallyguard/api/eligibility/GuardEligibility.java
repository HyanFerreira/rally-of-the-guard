package net.hfstack.rallyguard.api.eligibility;

import net.hfstack.rallyguard.RallyOfTheGuard;
import net.minecraft.text.Text;

import java.util.Objects;

/**
 * Safe evaluator for the public eligibility policy chain.
 */
public final class GuardEligibility {
    private GuardEligibility() {
    }

    public static GuardEligibilityDecision evaluate(GuardEligibilityContext context) {
        Objects.requireNonNull(context, "context");
        try {
            return GuardEligibilityEvents.CHECK.invoker().evaluate(context);
        } catch (RuntimeException exception) {
            RallyOfTheGuard.LOGGER.error(
                    "Guard eligibility policy failed for operation {} and guard {}",
                    context.operation(),
                    context.guard().getUuid(),
                    exception
            );
            return GuardEligibilityDecision.deny(Text.translatable("gui.rallyguard.eligibility.policy_error"));
        }
    }
}
