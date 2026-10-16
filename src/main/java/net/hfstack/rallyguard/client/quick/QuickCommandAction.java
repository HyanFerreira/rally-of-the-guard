package net.hfstack.rallyguard.client.quick;

import net.hfstack.rallyguard.network.NetworkConstants;

import java.util.Arrays;
import java.util.Optional;

public enum QuickCommandAction {
    FOLLOW(0, "gui.rallyguard.quick.follow", Flow.IMMEDIATE, NetworkConstants.ACTION_FOLLOW),
    ATTACK(1, "gui.rallyguard.quick.attack", Flow.COMBAT, -1),
    MOVE(2, "gui.rallyguard.quick.move", Flow.MOVE, -1),
    WAIT(3, "gui.rallyguard.quick.wait", Flow.IMMEDIATE, NetworkConstants.ACTION_WAIT),
    DEFEND(4, "gui.rallyguard.quick.defend", Flow.DEFEND, -1),
    PATROL(5, "gui.rallyguard.quick.patrol", Flow.IMMEDIATE, NetworkConstants.ACTION_TOGGLE_PATROL);

    private final int id;
    private final String translationKey;
    private final Flow flow;
    private final int serverAction;

    QuickCommandAction(int id, String translationKey, Flow flow, int serverAction) {
        this.id = id;
        this.translationKey = translationKey;
        this.flow = flow;
        this.serverAction = serverAction;
    }

    public int id() {
        return id;
    }

    public String translationKey() {
        return translationKey;
    }

    public Flow flow() {
        return flow;
    }

    public Optional<Integer> serverAction() {
        return flow == Flow.IMMEDIATE ? Optional.of(serverAction) : Optional.empty();
    }

    public static Optional<QuickCommandAction> fromId(int id) {
        return Arrays.stream(values()).filter(action -> action.id == id).findFirst();
    }

    public enum Flow {
        IMMEDIATE,
        COMBAT,
        MOVE,
        DEFEND
    }
}
