package net.hfstack.rallyguard.screen;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.hfstack.rallyguard.formation.RallyFormation;
import net.hfstack.rallyguard.network.payload.FormationSelectionC2SPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class FormationSelectionScreen extends Screen {
    private static final int WHITE = 0xFFFFFFFF;
    private static final int MUTED = 0xFFB8B8B8;
    private static final int PANEL_PADDING = 10;
    private static final int HEADER_HEIGHT = 31;
    private static final int FOOTER_HEIGHT = 21;
    private static final int CELL_HEIGHT = 36;

    private final int handId;
    private final RallyFormation selected;
    private FormationHubLayout.Grid grid;
    private int panelX;
    private int panelY;
    private int panelWidth;
    private int panelHeight;

    public FormationSelectionScreen(int handId, RallyFormation selected) {
        super(Component.translatable("gui.rallyguard.formation_hub.title"));
        this.handId = handId;
        this.selected = selected;
    }

    @Override
    protected void init() {
        super.init();
        RallyFormation[] formations = RallyFormation.values();
        grid = FormationHubLayout.forScreen(width, formations.length);
        panelWidth = grid.columns() * FormationHubLayout.CELL_WIDTH + PANEL_PADDING * 2;
        panelHeight = HEADER_HEIGHT + grid.rows() * CELL_HEIGHT + FOOTER_HEIGHT;
        panelX = (width - panelWidth) / 2;
        panelY = (height - panelHeight) / 2;

        for (int index = 0; index < formations.length; index++) {
            RallyFormation formation = formations[index];
            int column = index % grid.columns();
            int row = index / grid.columns();
            int x = panelX + PANEL_PADDING + column * FormationHubLayout.CELL_WIDTH + 2;
            int y = panelY + HEADER_HEIGHT + row * CELL_HEIGHT + 2;
            FormationIconButton button = new FormationIconButton(
                    x,
                    y,
                    formation,
                    formation == selected,
                    () -> select(formation)
            );
            button.setTooltip(Tooltip.create(Component.translatable(formation.translationKey())));
            addRenderableWidget(button);
        }
    }

    private void select(RallyFormation formation) {
        ClientPlayNetworking.send(new FormationSelectionC2SPayload(handId, formation.id()));
        Minecraft.getInstance().setScreen(null);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        graphics.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, 0xF0101010);
        graphics.outline(panelX, panelY, panelWidth, panelHeight, WHITE);
        graphics.centeredText(font, title, width / 2, panelY + 10, WHITE);
        graphics.centeredText(
                font,
                Component.translatable("gui.rallyguard.formation_hub.cancel_hint"),
                width / 2,
                panelY + panelHeight - 14,
                MUTED
        );
        super.extractRenderState(graphics, mouseX, mouseY, delta);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
