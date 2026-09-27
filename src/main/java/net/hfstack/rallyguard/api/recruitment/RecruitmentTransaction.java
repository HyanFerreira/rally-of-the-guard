package net.hfstack.rallyguard.api.recruitment;

import net.minecraft.text.Text;

import java.util.Optional;

/**
 * Reversible server-side reservation contributed by a recruitment policy.
 * Instances are created during BEFORE evaluation and used once on the server thread.
 */
public interface RecruitmentTransaction {
    /** An empty result reserves successfully; a message denies recruitment. */
    Optional<Text> reserve();

    /** Finalizes a successful ownership assignment. */
    void commit();

    /** Restores reserved resources and must tolerate partial failures. */
    void rollback();
}
