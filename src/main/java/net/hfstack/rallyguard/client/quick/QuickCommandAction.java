package net.hfstack.rallyguard.client.quick;

import java.util.Arrays;
import java.util.Optional;

public enum QuickCommandAction {
    FOLLOW(0),
    ATTACK(1),
    MOVE(2),
    WAIT(3),
    DEFEND(4),
    PATROL(5);

    private final int id;

    QuickCommandAction(int id) {
        this.id = id;
    }

    public int id() {
        return id;
    }

    public static Optional<QuickCommandAction> fromId(int id) {
        return Arrays.stream(values()).filter(action -> action.id == id).findFirst();
    }
}
