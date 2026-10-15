package net.hfstack.rallyguard.api.profile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.hfstack.rallyguard.profile.GuardProfile;
import net.hfstack.rallyguard.profile.GuardProfileReadResult;
import net.hfstack.rallyguard.api.RallyGuardApi;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

class GuardProfileAccessTest {

    @Test
    void apiVersionSixExposesCaptainService() {
        assertEquals(6, RallyGuardApi.apiVersion());
        assertSame(RallyGuardApi.captains(), RallyGuardApi.captains());
    }

    @Test
    void availableProfileIsExposedAsReadOnlyView() {
        GuardProfile profile = GuardProfile.legacyNeutral();

        Optional<GuardProfileView> result = GuardProfileAccess.viewOf(
                new GuardProfileReadResult.Available(profile, false));

        assertTrue(result.isPresent());
        assertSame(profile, result.orElseThrow());
    }

    @Test
    void absentProfileReturnsEmpty() {
        assertTrue(GuardProfileAccess.viewOf(new GuardProfileReadResult.Absent()).isEmpty());
    }

    @Test
    void futureSchemaReturnsEmpty() {
        assertTrue(GuardProfileAccess.viewOf(new GuardProfileReadResult.FutureSchema(
                GuardProfile.CURRENT_SCHEMA_VERSION + 1
        )).isEmpty());
    }

    @Test
    void returnedViewDoesNotExposePersistenceMutation() {
        Set<String> expectedAccessors = Set.of(
                "schemaVersion",
                "generationVersion",
                "origin",
                "totalXpMilli",
                "naturalQuality",
                "healthRoll",
                "attackRoll",
                "armorRoll",
                "speedRoll",
                "kills",
                "assists",
                "captain",
                "level",
                "rank",
                "qualityStars",
                "xpIntoLevelMilli",
                "xpForNextLevelMilli");
        Method[] methods = GuardProfileView.class.getDeclaredMethods();
        Set<String> actualMethods = Arrays.stream(methods)
                .map(Method::getName)
                .collect(Collectors.toSet());

        assertEquals(expectedAccessors, actualMethods);
        assertTrue(Arrays.stream(methods).allMatch(method -> method.getParameterCount() == 0));
    }

    @Test
    void captainAccessorKeepsExistingImplementationsSourceCompatible() throws NoSuchMethodException {
        assertTrue(GuardProfileView.class.getMethod("captain").isDefault());
    }
}
