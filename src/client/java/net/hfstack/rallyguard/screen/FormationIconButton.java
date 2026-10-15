package net.hfstack.rallyguard.screen;

import net.hfstack.rallyguard.RallyOfTheGuard;
import net.hfstack.rallyguard.formation.RallyFormation;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

final class FormationIconButton extends AbstractButton {
    private static final int ICON_SIZE = 21;
    private final Identifier texture;
    private final boolean selected;
    private final Runnable action;

    FormationIconButton(int x, int y, RallyFormation formation, boolean selected, Runnable action) {
        super(x, y, 32, 32, Component.translatable(formation.translationKey()));
        this.texture = Identifier.fromNamespaceAndPath(
                RallyOfTheGuard.MOD_ID,
                "textures/gui/formations/" + formation.id() + ".png"
        );
        this.selected = selected;
        this.action = action;
    }

    @Override
    public void onPress(InputWithModifiers input) {
        action.run();
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        int iconX = getX() + (getWidth() - ICON_SIZE) / 2;
        int iconY = getY() + (getHeight() - ICON_SIZE) / 2;
        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                texture,
                iconX,
                iconY,
                0.0F,
                0.0F,
                ICON_SIZE,
                ICON_SIZE,
                ICON_SIZE,
                ICON_SIZE
        );
        int outlineColor = FormationButtonStyle.outlineColor(selected, isHoveredOrFocused());
        if (outlineColor != 0) {
            graphics.outline(getX(), getY(), getWidth(), getHeight(), outlineColor);
        }
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput output) {
        defaultButtonNarrationText(output);
    }
}
