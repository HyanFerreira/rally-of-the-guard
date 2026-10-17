package net.hfstack.rallyguard.screen;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.hfstack.rallyguard.network.payload.RaidOpportunityS2CPayload;
import net.hfstack.rallyguard.network.payload.RaidResponseC2SPayload;
import net.hfstack.rallyguard.raid.RaidResponseAction;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class RaidOpportunityScreen extends Screen {
    private static final int PANEL_WIDTH = 300;
    private static final int PANEL_HEIGHT = 112;
    private static RaidOpportunityS2CPayload pending;
    private final RaidOpportunityS2CPayload opportunity;
    private boolean answered;

    private RaidOpportunityScreen(RaidOpportunityS2CPayload opportunity) {
        super(Component.translatable("gui.rallyguard.raid.title"));
        this.opportunity = opportunity;
    }

    public static void accept(RaidOpportunityS2CPayload opportunity) {
        Minecraft client = Minecraft.getInstance();
        if (client.screen == null) client.setScreen(new RaidOpportunityScreen(opportunity));
        else pending = opportunity;
    }

    public static void clientTick(Minecraft client) {
        if (client.player == null) pending = null;
        else if (pending != null && client.screen == null) {
            RaidOpportunityS2CPayload opportunity = pending;
            pending = null;
            client.setScreen(new RaidOpportunityScreen(opportunity));
        }
    }

    @Override
    protected void init() {
        super.init();
        int panelWidth = Math.min(PANEL_WIDTH, width - 16);
        int x = (width - panelWidth) / 2;
        int y = (height - PANEL_HEIGHT) / 2;
        int gap = GuiLayout.CONTROL_GAP;
        int buttonWidth = (panelWidth - 24 - gap * 2) / 3;
        int buttonX = x + 12;
        addChoice(buttonX, y + 74, buttonWidth, "gui.rallyguard.raid.rally", RaidResponseAction.RALLY_GUARDS);
        addChoice(buttonX + buttonWidth + gap, y + 74, buttonWidth,
                "gui.rallyguard.raid.defend", RaidResponseAction.DEFEND_VILLAGE);
        addChoice(buttonX + (buttonWidth + gap) * 2, y + 74, buttonWidth,
                "gui.rallyguard.raid.ignore", RaidResponseAction.IGNORE);
    }

    private void addChoice(int x, int y, int width, String translationKey, RaidResponseAction action) {
        addRenderableWidget(Button.builder(Component.translatable(translationKey), button -> respond(action))
                .bounds(x, y, width, 20).build());
    }

    private void respond(RaidResponseAction action) {
        if (answered) return;
        answered = true;
        ClientPlayNetworking.send(new RaidResponseC2SPayload(
                opportunity.raidId(), opportunity.dimensionId(), opportunity.center(), action.wireId()
        ));
        Minecraft.getInstance().setScreen(null);
    }

    @Override
    public void onClose() {
        respond(RaidResponseAction.IGNORE);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        int panelWidth = Math.min(PANEL_WIDTH, width - 16);
        int x = (width - panelWidth) / 2;
        int y = (height - PANEL_HEIGHT) / 2;
        graphics.fill(x, y, x + panelWidth, y + PANEL_HEIGHT, 0xF0101010);
        graphics.outline(x, y, panelWidth, PANEL_HEIGHT, 0xFFFF5555);
        graphics.centeredText(font, title, width / 2, y + 12, 0xFFFF7777);
        graphics.centeredText(font, Component.translatable(
                "gui.rallyguard.raid.message", opportunity.loadedGuards()
        ), width / 2, y + 35, 0xFFFFFFFF);
        graphics.centeredText(font, Component.translatable("gui.rallyguard.raid.choose"),
                width / 2, y + 51, 0xFFCCCCCC);
        super.extractRenderState(graphics, mouseX, mouseY, delta);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
