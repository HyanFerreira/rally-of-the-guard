package net.hfstack.rallyguard.profile;

public enum GuardProfileOrigin {
    NATURAL("natural"),
    LEGACY("legacy"),
    COMMAND("command"),
    OTHER("other");

    private final String serializedName;

    GuardProfileOrigin(String serializedName) {
        this.serializedName = serializedName;
    }

    public String serializedName() {
        return serializedName;
    }

    public static GuardProfileOrigin fromSerializedName(String serializedName) {
        if (serializedName == null) return OTHER;

        for (GuardProfileOrigin origin : values()) {
            if (origin.serializedName.equals(serializedName)) {
                return origin;
            }
        }
        return OTHER;
    }
}
