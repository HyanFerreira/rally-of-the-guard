package net.hfstack.rallyguard.screen;

import net.hfstack.rallyguard.network.payload.RaidReportS2CPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class RaidReportScreen extends Screen {
    private static final int PANEL_WIDTH = 280;
    private static final int PANEL_HEIGHT = 160;
    private static RaidReportS2CPayload pending;
    private final RaidReportS2CPayload report;

    private RaidReportScreen(RaidReportS2CPayload report) {
        super(Component.translatable("gui.rallyguard.raid.report.title"));
        this.report = report;
    }

    public static void accept(RaidReportS2CPayload report) {
        Minecraft client = Minecraft.getInstance();
        if (client.screen == null) client.setScreen(new RaidReportScreen(report));
        else pending = report;
    }

    public static void clientTick(Minecraft client) {
        if (client.player == null) pending = null;
        else if (pending != null && client.screen == null) {
            RaidReportS2CPayload report = pending;
            pending = null;
            client.setScreen(new RaidReportScreen(report));
        }
    }

    @Override
    protected void init() {
        super.init();
        int x = (width - PANEL_WIDTH) / 2;
        int y = (height - PANEL_HEIGHT) / 2;
        addRenderableWidget(Button.builder(Component.translatable("gui.done"), ignored -> onClose())
                .bounds(x + 100, y + 130, 80, 20).build());
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(null);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        int x = (width - PANEL_WIDTH) / 2;
        int y = (height - PANEL_HEIGHT) / 2;
        graphics.fill(x, y, x + PANEL_WIDTH, y + PANEL_HEIGHT, 0xF0101010);
        graphics.outline(x, y, PANEL_WIDTH, PANEL_HEIGHT, 0xFFFFCC55);
        graphics.centeredText(font, title, width / 2, y + 11, 0xFFFFCC55);
        graphics.text(font, Component.translatable("gui.rallyguard.raid.report.deployed", report.guardsDeployed()),
                x + 18, y + 36, 0xFFFFFFFF, false);
        graphics.text(font, Component.translatable("gui.rallyguard.raid.report.defeated", report.enemiesDefeated()),
                x + 18, y + 52, 0xFFFFFFFF, false);
        graphics.text(font, Component.translatable("gui.rallyguard.raid.report.lost", report.guardsLost()),
                x + 18, y + 68, 0xFFFFFFFF, false);
        Component mvp = report.mvpName().isPresent()
                ? Component.translatable("gui.rallyguard.raid.report.mvp", report.mvpName().orElseThrow(), report.mvpKills())
                : Component.translatable("gui.rallyguard.raid.report.no_mvp");
        graphics.text(font, mvp, x + 18, y + 91, 0xFFFFFFFF, false);
        if (report.incomplete()) {
            graphics.text(font, Component.translatable("gui.rallyguard.raid.report.incomplete"),
                    x + 18, y + 109, 0xFFFFAA55, false);
        }
        super.extractRenderState(graphics, mouseX, mouseY, delta);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
