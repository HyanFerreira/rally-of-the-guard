package net.hfstack.rallyguard.screen;

import net.hfstack.rallyguard.RallyOfTheGuard;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.function.Consumer;

final class EditSaveIconButton extends AbstractButton {
    private static final int TEXTURE_SIZE = 20;
    private static final Identifier EDIT_TEXTURE = Identifier.fromNamespaceAndPath(
            RallyOfTheGuard.MOD_ID,
            "textures/gui/icons/route_point_edit.png"
    );
    private static final Identifier SAVE_TEXTURE = Identifier.fromNamespaceAndPath(
            RallyOfTheGuard.MOD_ID,
            "textures/gui/icons/route_point_save.png"
    );
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
        extractDefaultSprite(graphics);
        int iconX = getX() + (getWidth() - TEXTURE_SIZE) / 2;
        int iconY = getY() + (getHeight() - TEXTURE_SIZE) / 2;
        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                saving ? SAVE_TEXTURE : EDIT_TEXTURE,
                iconX,
                iconY,
                0.0F,
                0.0F,
                TEXTURE_SIZE,
                TEXTURE_SIZE,
                TEXTURE_SIZE,
                TEXTURE_SIZE
        );
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput output) {
        defaultButtonNarrationText(output);
    }
}
