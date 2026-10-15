package net.hfstack.rallyguard.muster;

import net.hfstack.rallyguard.api.captain.CaptainAction;
import net.hfstack.rallyguard.api.captain.CaptainActionView;
import net.hfstack.rallyguard.api.captain.CaptainMutationResult;
import net.hfstack.rallyguard.api.captain.CaptainOffer;
import net.hfstack.rallyguard.api.profile.GuardRank;
import net.hfstack.rallyguard.api.recruitment.RecruitmentOffer;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GuardProfileSnapshotTest {
    @Test
    void boundsEveryExternallySuppliedDisplayAndNumericField() {
        GuardProfileSnapshot snapshot = new GuardProfileSnapshot(
                7,
                UUID.randomUUID(),
                "x".repeat(300),
                99,
                GuardRank.ELITE,
                Optional.of("r".repeat(200)),
                true,
                99,
                -4,
                -8,
                Double.NaN,
                -1,
                Double.POSITIVE_INFINITY,
                0.25,
                -1,
                -2,
                GuardServiceStatus.UNOWNED,
                Optional.of(new RecruitmentOffer(
                        Identifier.fromNamespaceAndPath("minecraft", "emerald"),
                        13
                )),
                Optional.of(new CaptainActionView(
                        false,
                        CaptainAction.PROMOTE,
                        false,
                        Optional.of(CaptainMutationResult.Outcome.LEVEL_TOO_LOW),
                        99,
                        Optional.of(new CaptainOffer(
                                Identifier.fromNamespaceAndPath("minecraft", "emerald"),
                                5_000
                        ))
                ))
        );

        assertEquals(GuardProfileSnapshot.MAX_DISPLAY_NAME_LENGTH, snapshot.displayName().length());
        assertEquals(GuardProfileSnapshot.MAX_RANK_OVERRIDE_LENGTH,
                snapshot.rankOverride().orElseThrow().length());
        assertEquals(10, snapshot.level());
        assertTrue(snapshot.captain());
        assertEquals(5, snapshot.stars());
        assertEquals(0, snapshot.xpIntoLevelMilli());
        assertEquals(0, snapshot.xpForNextLevelMilli());
        assertEquals(0.0, snapshot.maxHealth());
        assertEquals(0.0, snapshot.attackDamage());
        assertEquals(0.0, snapshot.armor());
        assertEquals(0, snapshot.kills());
        assertEquals(0, snapshot.assists());
        assertTrue(snapshot.hiringOffer().isPresent());
        CaptainActionView action = snapshot.captainAction().orElseThrow();
        assertEquals(10, action.minimumLevel());
        assertEquals(999, action.offer().orElseThrow().cost());
    }

    @Test
    void wireSnapshotDoesNotExposeMutableOrGenerationInternals() {
        Set<String> components = Arrays.stream(GuardProfileSnapshot.class.getRecordComponents())
                .map(component -> component.getName())
                .collect(Collectors.toSet());

        assertFalse(components.contains("schemaVersion"));
        assertFalse(components.contains("generationVersion"));
        assertFalse(components.contains("origin"));
        assertFalse(components.contains("naturalQuality"));
        assertFalse(components.contains("healthRoll"));
        assertFalse(components.contains("attackRoll"));
        assertFalse(components.contains("armorRoll"));
        assertFalse(components.contains("speedRoll"));
        assertTrue(components.contains("captain"));
        assertTrue(components.contains("captainAction"));
    }

    @Test
    void captainActionIsDerivedOnlyForGuardsUnderViewerCommand() {
        AtomicInteger inspections = new AtomicInteger();
        CaptainActionView expected = new CaptainActionView(
                false, CaptainAction.PROMOTE, true, Optional.empty(), 7, Optional.empty()
        );

        assertEquals(Optional.of(expected), GuardProfileSnapshotFactory.captainAction(
                GuardServiceStatus.UNDER_YOUR_COMMAND,
                () -> {
                    inspections.incrementAndGet();
                    return expected;
                }
        ));
        assertTrue(GuardProfileSnapshotFactory.captainAction(
                GuardServiceStatus.UNOWNED,
                () -> {
                    inspections.incrementAndGet();
                    return expected;
                }
        ).isEmpty());
        assertTrue(GuardProfileSnapshotFactory.captainAction(
                GuardServiceStatus.ALREADY_IN_SERVICE,
                () -> {
                    inspections.incrementAndGet();
                    return expected;
                }
        ).isEmpty());
        assertEquals(1, inspections.get());
    }
}
