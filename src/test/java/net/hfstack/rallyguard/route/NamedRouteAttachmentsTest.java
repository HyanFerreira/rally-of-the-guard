package net.hfstack.rallyguard.route;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class NamedRouteAttachmentsTest {
    @Test
    void playerRegistryIsPersistentAndCopiedAcrossRespawn() throws IOException {
        String source = Files.readString(Path.of(
                "src", "main", "java", "net", "hfstack", "rallyguard", "route", "NamedRouteAttachments.java"
        ));

        assertTrue(source.contains("builder.persistent(CompoundTag.CODEC).copyOnDeath()"));
    }
}
