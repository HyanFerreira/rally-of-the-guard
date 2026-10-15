package net.hfstack.rallyguard.api;

import net.hfstack.rallyguard.api.command.GuardCommandService;
import net.hfstack.rallyguard.api.captain.CaptainService;
import net.hfstack.rallyguard.api.profile.GuardProfileAccess;
import net.hfstack.rallyguard.api.progression.GuardProgressionService;
import net.hfstack.rallyguard.api.recruitment.GuardRecruitmentService;
import net.hfstack.rallyguard.api.recruitment.GuardHiringValueService;
import net.hfstack.rallyguard.api.squad.SquadService;
import net.hfstack.rallyguard.squad.DefaultSquadService;
import net.hfstack.rallyguard.captain.DefaultCaptainService;
import net.hfstack.rallyguard.service.DefaultGuardHiringValueService;
import net.hfstack.rallyguard.service.DefaultGuardCommandService;
import net.hfstack.rallyguard.service.DefaultGuardRecruitmentService;

/**
 * Stable entry point for integrations with Rally of the Guard.
 */
public final class RallyGuardApi {
    public static final int API_VERSION = 7;

    private static final GuardHiringValueService HIRING_VALUES = new DefaultGuardHiringValueService();
    private static final GuardCommandService GUARD_COMMANDS = new DefaultGuardCommandService();
    private static final GuardRecruitmentService GUARD_RECRUITMENT = new DefaultGuardRecruitmentService();
    private static final SquadService SQUADS = new DefaultSquadService();
    private static final CaptainService CAPTAINS = new DefaultCaptainService();

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

    public static GuardHiringValueService hiringValues() {
        return HIRING_VALUES;
    }

    public static GuardProfileAccess profiles() {
        return GuardProfileAccess.instance();
    }

    public static GuardProgressionService progression() {
        return net.hfstack.rallyguard.progression.GuardProgressionService.instance();
    }

    public static SquadService squads() {
        return SQUADS;
    }

    public static CaptainService captains() {
        return CAPTAINS;
    }
}
