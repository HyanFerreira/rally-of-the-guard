package net.hfstack.rallyguard.screen;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

final class EditConfirmIconButton extends AbstractButton {
    private final Consumer<EditConfirmIconButton> action;
    private boolean confirming;

    EditConfirmIconButton(int x, int y, int size, Consumer<EditConfirmIconButton> action) {
        super(x, y, size, size, Component.translatable("gui.rallyguard.route.edit_point_name"));
        this.action = action;
    }

    boolean isConfirming() {
        return confirming;
    }

    void setConfirming(boolean confirming) {
        this.confirming = confirming;
        setMessage(Component.translatable(confirming
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
        int iconX = getX() + (getWidth() - GuiIcons.TEXTURE_SIZE) / 2;
        int iconY = getY() + (getHeight() - GuiIcons.TEXTURE_SIZE) / 2;
        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                confirming ? GuiIcons.CONFIRM_SMALL : GuiIcons.EDIT_SMALL,
                iconX,
                iconY,
                0.0F,
                0.0F,
                GuiIcons.TEXTURE_SIZE,
                GuiIcons.TEXTURE_SIZE,
                GuiIcons.TEXTURE_SIZE,
                GuiIcons.TEXTURE_SIZE
        );
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput output) {
        defaultButtonNarrationText(output);
    }
}
