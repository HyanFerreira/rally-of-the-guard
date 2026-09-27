package net.hfstack.rallyguard.api.recruitment;

import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class RecruitmentDecisionTest {
    @Test
    void transactionalDecisionCarriesItsReversibleReservation() {
        RecruitmentTransaction transaction = new RecruitmentTransaction() {
            @Override
            public Optional<Text> reserve() {
                return Optional.empty();
            }

            @Override
            public void commit() {
            }

            @Override
            public void rollback() {
            }
        };
        RecruitmentOffer freePersonalOffer = new RecruitmentOffer(Identifier.of("minecraft", "emerald"), 0);

        RecruitmentDecision.Allow allowed = assertInstanceOf(
                RecruitmentDecision.Allow.class,
                RecruitmentDecision.transactional(freePersonalOffer, transaction)
        );

        assertEquals(freePersonalOffer, allowed.offer());
        assertEquals(java.util.List.of(transaction), allowed.transactions());
    }
}
