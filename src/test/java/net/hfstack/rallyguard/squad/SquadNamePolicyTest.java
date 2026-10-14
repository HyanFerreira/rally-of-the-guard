package net.hfstack.rallyguard.squad;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SquadNamePolicyTest {
    private static final UUID ID = UUID.fromString("10000000-0000-0000-0000-000000000001");

    @Test
    void normalizeTrimsAndCountsUnicodeCodePoints() {
        assertEquals(Optional.of("Royal Guard"), SquadNamePolicy.normalize("  Royal Guard  ", 24));
        assertEquals(Optional.of("😀😀"), SquadNamePolicy.normalize("😀😀", 2));
        assertTrue(SquadNamePolicy.normalize("😀😀", 1).isEmpty());
        assertTrue(SquadNamePolicy.normalize("   ", 24).isEmpty());
    }

    @Test
    void duplicateComparisonIsCaseInsensitiveAndCanExcludeRenamedSquad() {
        SquadRegistry registry = new SquadRegistry(1, List.of(
                new SquadDefinition(ID, "Alpha", Optional.empty())
        ));

        assertTrue(SquadNamePolicy.isDuplicate(registry, "alpha", Optional.empty()));
        assertFalse(SquadNamePolicy.isDuplicate(registry, "ALPHA", Optional.of(ID)));
    }
}
