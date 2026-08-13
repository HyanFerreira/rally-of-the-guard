package net.hfstack.rallyguard.api;

import net.hfstack.rallyguard.api.command.GuardCommandService;
import net.hfstack.rallyguard.api.recruitment.GuardRecruitmentService;
import net.hfstack.rallyguard.service.DefaultGuardCommandService;
import net.hfstack.rallyguard.service.DefaultGuardRecruitmentService;

/**
 * Stable entry point for integrations with Rally of the Guard.
 */
public final class RallyGuardApi {
    public static final int API_VERSION = 1;

    private static final GuardCommandService GUARD_COMMANDS = new DefaultGuardCommandService();
    private static final GuardRecruitmentService GUARD_RECRUITMENT = new DefaultGuardRecruitmentService();

    private RallyGuardApi() {
    }

    public static int apiVersion() {
        return API_VERSION;
    }

    public static GuardCommandService guardCommands() {
        return GUARD_COMMANDS;
    }

    public static GuardRecruitmentService guardRecruitment() {
        return GUARD_RECRUITMENT;
    }
}
