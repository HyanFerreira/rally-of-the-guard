package net.hfstack.rallyguard.screen;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

final class EditSaveIconButton extends AbstractButton {
    private static final int ICON_COLOR = 0xFFFFFFFF;
    private static final int ICON_SHADOW = 0xFF777777;
    private final Consumer<EditSaveIconButton> action;
    private boolean saving;

    EditSaveIconButton(int x, int y, int size, Consumer<EditSaveIconButton> action) {
        super(x, y, size, size, Component.translatable("gui.rallyguard.route.edit_point_name"));
        this.action = action;
    }

    boolean isSaving() {
        return saving;
    }

    void setSaving(boolean saving) {
        this.saving = saving;
        setMessage(Component.translatable(saving
                ? "gui.rallyguard.route.save_point_name"
                : "gui.rallyguard.route.edit_point_name"));
    }

    @Override
    public void onPress(InputWithModifiers input) {
        action.accept(this);
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        int left = getX() + (getWidth() - 12) / 2;
        int top = getY() + (getHeight() - 12) / 2;
        if (saving) {
            drawCheck(graphics, left, top);
        } else {
            drawPencil(graphics, left, top);
        }
    }

    private static void drawPencil(GuiGraphicsExtractor graphics, int x, int y) {
        for (int step = 0; step < 8; step++) {
            int px = x + 2 + step;
            int py = y + 9 - step;
            graphics.fill(px, py, px + 3, py + 3, ICON_SHADOW);
            graphics.fill(px, py, px + 2, py + 2, ICON_COLOR);
        }
        graphics.fill(x + 1, y + 9, x + 4, y + 12, ICON_COLOR);
        graphics.fill(x + 9, y, x + 12, y + 3, ICON_COLOR);
    }

    private static void drawCheck(GuiGraphicsExtractor graphics, int x, int y) {
        graphics.fill(x + 1, y + 6, x + 4, y + 9, ICON_COLOR);
        graphics.fill(x + 3, y + 8, x + 6, y + 11, ICON_COLOR);
        graphics.fill(x + 5, y + 6, x + 8, y + 9, ICON_COLOR);
        graphics.fill(x + 7, y + 4, x + 10, y + 7, ICON_COLOR);
        graphics.fill(x + 9, y + 2, x + 12, y + 5, ICON_COLOR);
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput output) {
        defaultButtonNarrationText(output);
    }
}
