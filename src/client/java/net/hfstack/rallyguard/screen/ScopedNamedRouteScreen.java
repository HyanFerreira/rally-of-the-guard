package net.hfstack.rallyguard.screen;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.hfstack.rallyguard.api.command.GuardCommandScope;
import net.hfstack.rallyguard.network.payload.GuardListS2CPayload;
import net.hfstack.rallyguard.network.payload.ScopedNamedRouteAssignmentC2SPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

public final class ScopedNamedRouteScreen extends Screen {
    private static final int WHITE = 0xFFFFFFFF;
    private static final int MUTED = 0xFFAAAAAA;
    private static final int PANEL_WIDTH = 360;
    private static final int ROW_HEIGHT = 24;
    private static final int MAX_ROWS = 8;

    private final GuardCommandScreen parent;
    private final GuardListS2CPayload snapshot;
    private final GuardCommandScope scope;
    private int page;

    public ScopedNamedRouteScreen(
            GuardCommandScreen parent,
            GuardListS2CPayload snapshot,
            GuardCommandScope scope
    ) {
        super(Component.translatable("gui.rallyguard.named_route.scope_title"));
        this.parent = parent;
        this.snapshot = snapshot;
        this.scope = scope;
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
        int panelWidth = panelWidth();
        List<GuardListS2CPayload.NamedRouteEntry> routes = snapshot.namedRoutes();
        int start = page * rowsPerPage();
        int end = Math.min(start + rowsPerPage(), routes.size());
        for (int i = start; i < end; i++) {
            GuardListS2CPayload.NamedRouteEntry route = routes.get(i);
            int rowY = y + 48 + (i - start) * ROW_HEIGHT;
            addRenderableWidget(Button.builder(
                    Component.literal(route.name()), button -> select(route)
            ).bounds(x + 14, rowY, panelWidth - 28, 20).build());
        }

        int bottom = y + panelHeight() - 28;
        Button previous = Button.builder(Component.literal("<"), button -> changePage(-1))
                .bounds(x + 14, bottom, 24, 20).build();
        previous.active = page > 0;
        addRenderableWidget(previous);
        Button next = Button.builder(Component.literal(">"), button -> changePage(1))
                .bounds(x + 42, bottom, 24, 20).build();
        next.active = (page + 1) * rowsPerPage() < routes.size();
        addRenderableWidget(next);
        addRenderableWidget(Button.builder(Component.translatable("gui.rallyguard.route.back"), button -> back())
                .bounds(x + panelWidth - 84, bottom, 70, 20).build());
    }

    private void select(GuardListS2CPayload.NamedRouteEntry route) {
        boolean replacesAnotherRoute = scopedGuards().stream()
                .anyMatch(guard -> guard.namedRouteId().filter(id -> !id.equals(route.id())).isPresent());
        if (replacesAnotherRoute) {
            Minecraft.getInstance().setScreen(new ConfirmScreen(
                    confirmed -> {
                        if (confirmed) send(route);
                        else Minecraft.getInstance().setScreen(this);
                    },
                    Component.translatable("gui.rallyguard.named_route.scope_confirm_title"),
                    Component.translatable("gui.rallyguard.named_route.scope_confirm", route.name())
            ));
            return;
        }
        send(route);
    }

    private List<GuardListS2CPayload.Entry> scopedGuards() {
        return new SquadScreenModel(snapshot, scope).filteredGuards();
    }

    private void send(GuardListS2CPayload.NamedRouteEntry route) {
        ClientPlayNetworking.send(new ScopedNamedRouteAssignmentC2SPayload(scope, route.id()));
        back();
    }

    private void changePage(int delta) {
        int pages = Math.max(1, (snapshot.namedRoutes().size() + rowsPerPage() - 1) / rowsPerPage());
        page = Math.max(0, Math.min(page + delta, pages - 1));
        rebuildControls();
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
        int panelWidth = panelWidth();
        graphics.fill(x, y, x + panelWidth, y + panelHeight(), 0xF0101010);
        graphics.outline(x, y, panelWidth, panelHeight(), WHITE);
        graphics.centeredText(font, title, width / 2, y + 10, WHITE);
        graphics.centeredText(font, Component.translatable("gui.rallyguard.named_route.scope_hint", scopedGuards().size()),
                width / 2, y + 26, MUTED);
        if (snapshot.namedRoutes().isEmpty()) {
            graphics.centeredText(font, Component.translatable("gui.rallyguard.named_route.no_routes"),
                    width / 2, y + 70, MUTED);
        }
        super.extractRenderState(graphics, mouseX, mouseY, delta);
    }

    private int panelHeight() {
        return 48 + rowsPerPage() * ROW_HEIGHT + 38;
    }

    private int rowsPerPage() {
        return Math.max(3, Math.min(MAX_ROWS, (Math.max(180, height - 12) - 86) / ROW_HEIGHT));
    }

    private int panelX() {
        return (width - panelWidth()) / 2;
    }

    private int panelWidth() {
        return Math.min(PANEL_WIDTH, Math.max(280, width - 12));
    }

    private int panelY() {
        return (height - panelHeight()) / 2;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
