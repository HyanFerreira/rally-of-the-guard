package net.hfstack.rallyguard.screen;

import net.minecraft.network.chat.Component;

final class RoutePointNames {
    private RoutePointNames() {
    }

    static String initialName(int index) {
        return Component.translatable("gui.rallyguard.route.default_point_name", alphabeticIndex(index)).getString();
    }

    static String displayName(String name, int index) {
        return name == null || name.isBlank() ? initialName(index) : name.strip();
    }

    private static String alphabeticIndex(int index) {
        StringBuilder result = new StringBuilder();
        int value = Math.max(0, index);
        do {
            result.append((char) ('A' + value % 26));
            value = value / 26 - 1;
        } while (value >= 0);
        return result.reverse().toString();
    }
}
