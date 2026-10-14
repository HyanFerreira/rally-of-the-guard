package net.hfstack.rallyguard.api.progression;

public sealed interface XpAwardDecision permits XpAwardDecision.Allow, XpAwardDecision.Deny {
    static XpAwardDecision allow(long xpMilli) {
        return new Allow(Math.max(0, xpMilli));
    }

    static XpAwardDecision deny() {
        return Deny.INSTANCE;
    }

    record Allow(long xpMilli) implements XpAwardDecision {
        public Allow {
            xpMilli = Math.max(0, xpMilli);
        }
    }

    enum Deny implements XpAwardDecision {
        INSTANCE
    }
}
