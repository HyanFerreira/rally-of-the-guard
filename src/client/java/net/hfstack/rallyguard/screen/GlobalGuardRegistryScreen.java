package net.hfstack.rallyguard.screen;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.hfstack.rallyguard.network.payload.GlobalGuardRegistryS2CPayload;
import net.hfstack.rallyguard.network.payload.OpenGlobalGuardRegistryC2SPayload;
import net.hfstack.rallyguard.order.GuardOrderStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class GlobalGuardRegistryScreen extends Screen {
    private static final int PANEL_WIDTH = 660;
    private static final int PANEL_HEIGHT = 330;
    private static final int ROW_HEIGHT = 30;
    private static final int WHITE = 0xFFFFFFFF;
    private static final int MUTED = 0xFFAAAAAA;
    private static final int GREEN = 0xFF77DD77;

    private final GuardCommandScreen parent;
    private GlobalGuardRegistryS2CPayload snapshot;

    private GlobalGuardRegistryScreen(GuardCommandScreen parent, GlobalGuardRegistryS2CPayload snapshot) {
        super(Component.translatable("gui.rallyguard.registry.title"));
        this.parent = parent;
        this.snapshot = snapshot;
    }

    public static void open(GuardCommandScreen parent) {
        Pending.parent = parent;
        ClientPlayNetworking.send(new OpenGlobalGuardRegistryC2SPayload(0));
    }

    public static void accept(GlobalGuardRegistryS2CPayload payload) {
        Minecraft.getInstance().execute(() -> {
            Screen current = Minecraft.getInstance().screen;
            if (current instanceof GlobalGuardRegistryScreen registry) {
                registry.snapshot = payload;
                registry.rebuildControls();
            } else if (Pending.parent != null) {
                GuardCommandScreen parent = Pending.parent;
                Pending.parent = null;
                Minecraft.getInstance().setScreen(new GlobalGuardRegistryScreen(parent, payload));
            }
        });
    }

    @Override
    protected void init() {
        super.init();
        rebuildControls();
    }

    private void rebuildControls() {
        clearWidgets();
        int x = panelX();
        int y = panelY();
        int bottom = y + PANEL_HEIGHT - 28;
        Button previous = Button.builder(Component.literal("<"), button -> request(snapshot.page() - 1))
                .bounds(x + 14, bottom, 24, 20).build();
        previous.active = snapshot.page() > 0;
        addRenderableWidget(previous);
        Button next = Button.builder(Component.literal(">"), button -> request(snapshot.page() + 1))
                .bounds(x + 42, bottom, 24, 20).build();
        next.active = snapshot.page() + 1 < snapshot.totalPages();
        addRenderableWidget(next);
        addRenderableWidget(Button.builder(Component.translatable("gui.rallyguard.route.back"), button -> back())
                .bounds(x + panelWidth() - 84, bottom, 70, 20).build());
    }

    private void request(int page) {
        ClientPlayNetworking.send(new OpenGlobalGuardRegistryC2SPayload(page));
    }

    private void back() {
        Minecraft.getInstance().setScreen(parent);
    }

    @Override
    public void onClose() {
        back();
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        int x = panelX();
        int y = panelY();
        int panelW = panelWidth();
        graphics.fill(x, y, x + panelW, y + PANEL_HEIGHT, 0xF0101010);
        graphics.outline(x, y, panelW, PANEL_HEIGHT, WHITE);
        graphics.centeredText(font, title, width / 2, y + 10, WHITE);
        graphics.text(font, Component.translatable("gui.rallyguard.registry.summary", snapshot.totalGuards()), x + 14, y + 32, MUTED);
        graphics.text(font, Component.translatable("gui.rallyguard.registry.guard"), x + 20, y + 52, MUTED);
        graphics.text(font, Component.translatable("gui.rallyguard.registry.location"), x + panelW / 2, y + 52, MUTED);
        graphics.text(font, Component.translatable("gui.rallyguard.registry.availability"), x + panelW - 130, y + 52, MUTED);
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        drawRows(graphics, x, y, panelW);
        graphics.text(font, Component.literal((snapshot.page() + 1) + " / " + snapshot.totalPages()), x + 76, y + PANEL_HEIGHT - 22, MUTED);
    }

    private void drawRows(GuiGraphicsExtractor graphics, int x, int y, int panelW) {
        if (snapshot.entries().isEmpty()) {
            graphics.centeredText(font, Component.translatable("gui.rallyguard.registry.empty"),
                    width / 2, y + 150, MUTED);
            return;
        }
        for (int i = 0; i < snapshot.entries().size(); i++) {
            GlobalGuardRegistryS2CPayload.Entry entry = snapshot.entries().get(i);
            int rowY = y + 68 + i * ROW_HEIGHT;
            if (i % 2 == 1) graphics.fill(x + 10, rowY - 2, x + panelW - 10, rowY + ROW_HEIGHT - 2, 0x22111111);
            String rank = entry.rankId().isBlank() ? "" : Component.translatable("rank.rallyguard." + entry.rankId()).getString();
            String squad = entry.squadName().isBlank()
                    ? Component.translatable("gui.rallyguard.squad.scope_unassigned").getString() : entry.squadName();
            graphics.text(font, Component.literal(trim(entry.name(), panelW / 2 - 36)), x + 20, rowY, WHITE);
            graphics.text(font, Component.literal(trim(rank + " · " + squad, panelW / 2 - 36)), x + 20, rowY + 12, MUTED);

            String dimension = shortDimension(entry.dimensionId());
            String location = dimension + " · " + entry.x() + ", " + entry.y() + ", " + entry.z();
            if (entry.distance() >= 0) location += " · " + entry.distance() + "m";
            graphics.text(font, Component.literal(trim(location, panelW / 2 - 150)), x + panelW / 2, rowY, WHITE);
            graphics.text(font, status(entry.orderStatus()), x + panelW / 2, rowY + 12, MUTED);
            graphics.text(font, Component.translatable(entry.loaded()
                            ? "gui.rallyguard.registry.loaded" : "gui.rallyguard.registry.unloaded"),
                    x + panelW - 130, rowY + 6, entry.loaded() ? GREEN : MUTED);
        }
    }

    private Component status(int status) {
        return switch (status) {
            case GuardOrderStatus.FOLLOWING -> Component.translatable("gui.rallyguard.command.status.following");
            case GuardOrderStatus.WAITING -> Component.translatable("gui.rallyguard.command.status.waiting");
            case GuardOrderStatus.PATROLLING -> Component.translatable("gui.rallyguard.command.status.patrolling");
            case GuardOrderStatus.ROUTING -> Component.translatable("gui.rallyguard.command.status.routing");
            case GuardOrderStatus.MOVING -> Component.translatable("gui.rallyguard.command.status.moving");
            case GuardOrderStatus.DEFENDING -> Component.translatable("gui.rallyguard.command.status.defending");
            default -> Component.translatable("gui.rallyguard.command.status.idle");
        };
    }

    private String shortDimension(String id) {
        int separator = id.indexOf(':');
        return separator >= 0 ? id.substring(separator + 1) : id;
    }

    private String trim(String value, int maxWidth) {
        if (font.width(value) <= maxWidth) return value;
        return font.plainSubstrByWidth(value, Math.max(0, maxWidth - font.width("..."))) + "...";
    }

    private int panelWidth() {
        return Math.min(PANEL_WIDTH, Math.max(500, width - 24));
    }

    private int panelX() {
        return (width - panelWidth()) / 2;
    }

    private int panelY() {
        return (height - PANEL_HEIGHT) / 2;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private static final class Pending {
        private static GuardCommandScreen parent;
    }
}
