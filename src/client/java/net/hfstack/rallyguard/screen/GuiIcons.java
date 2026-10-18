package net.hfstack.rallyguard.screen;

import net.hfstack.rallyguard.RallyOfTheGuard;
import net.minecraft.resources.Identifier;

final class GuiIcons {
    static final int TEXTURE_SIZE = 20;
    static final Identifier EDIT = icon("edit.png");
    static final Identifier EDIT_SMALL = icon("edit_small.png");
    static final Identifier CONFIRM_SMALL = icon("confirm_small.png");
    static final Identifier SAVE = icon("save.png");
    static final Identifier SAVE_SMALL = icon("save_small.png");

    private GuiIcons() {
    }

    private static Identifier icon(String fileName) {
        return Identifier.fromNamespaceAndPath(
                RallyOfTheGuard.MOD_ID,
                "textures/gui/icons/" + fileName
        );
    }
}
