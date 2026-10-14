package net.hfstack.rallyguard.api.squad;

import java.util.Optional;

public record GuardSquadView(Optional<SquadView> squad, boolean officer) {
    public GuardSquadView {
        squad = squad == null ? Optional.empty() : squad;
    }

    public static GuardSquadView unassigned() {
        return new GuardSquadView(Optional.empty(), false);
    }
}
