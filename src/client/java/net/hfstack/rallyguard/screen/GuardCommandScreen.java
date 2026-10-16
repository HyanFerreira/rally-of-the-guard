package net.hfstack.rallyguard.screen;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.hfstack.rallyguard.api.command.GuardCommandScope;
import net.hfstack.rallyguard.api.command.GuardCommandScopeKind;
import net.hfstack.rallyguard.network.NetworkConstants;
import net.hfstack.rallyguard.network.payload.GuardActionC2SPayload;
import net.hfstack.rallyguard.network.payload.GuardListS2CPayload;
import net.hfstack.rallyguard.network.payload.ScopedGuardActionC2SPayload;
import net.hfstack.rallyguard.order.GuardOrderStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class GuardCommandScreen extends Screen {
    private static final int WHITE = 0xFFFFFFFF;
    private static final int MUTED = 0xFFAAAAAA;
    private static final int GOLD = 0xFFFFC94A;
    private static final int MAX_PANEL_WIDTH = 700;
    private static final int PANEL_HEIGHT = 300;
    private static final int SCREEN_PAD = 12;
    private static final int PER_PAGE = 6;
    private static final int ROW_TOP = 64;
    private static final int ROW_HEIGHT = 32;
    private static final int BUTTON_HEIGHT = 20;
    private static final int GAP = 4;

    public record Entry(int entityId, UUID guardId, String name, boolean patrolling, int status, boolean routeActive,
                        int routeWaitSeconds, List<GuardListS2CPayload.Point> routePoints,
                        Optional<Component> rank, Optional<Component> settlement,
                        Optional<UUID> squadId, boolean captain) {
    }

    private final List<Entry> all = new ArrayList<>();
    private GuardListS2CPayload snapshot;
    private GuardCommandScope scope;
    private int page;
    private UUID selectedGuard;

    public GuardCommandScreen(GuardListS2CPayload snapshot) {
        this(snapshot, GuardCommandScope.all());
    }

    GuardCommandScreen(GuardListS2CPayload snapshot, GuardCommandScope scope) {
        super(Component.translatable("gui.rallyguard.command.title"));
        this.snapshot = snapshot;
        this.scope = scope;
        rebuildEntries();
        selectFirstIfNeeded();
    }

    public static void openFromPayload(GuardListS2CPayload payload) {
        Minecraft.getInstance().execute(() -> {
            Screen current = Minecraft.getInstance().screen;
            if (current instanceof GuardCommandScreen command) {
                command.acceptSnapshot(payload);
            } else if (current instanceof SquadManagementScreen management) {
                management.acceptSnapshot(payload);
            } else {
                Minecraft.getInstance().setScreen(new GuardCommandScreen(payload));
            }
        });
    }

    private void acceptSnapshot(GuardListS2CPayload payload) {
        snapshot = payload;
        scope = new SquadScreenModel(payload, scope).scope();
        rebuildEntries();
        selectFirstIfNeeded();
        page = Math.min(page, Math.max(0, (all.size() - 1) / PER_PAGE));
        ensureSelectedPage();
        rebuildButtons();
    }

    private void rebuildEntries() {
        all.clear();
        for (GuardListS2CPayload.Entry entry : new SquadScreenModel(snapshot, scope).filteredGuards()) {
            all.add(new Entry(
                    entry.entityId(), entry.guardId(), entry.name(), entry.patrolling(), entry.status(),
                    entry.routeActive(), entry.routeWaitSeconds(), entry.routePoints(), entry.rank(),
                    entry.settlement(), entry.squadId(), entry.captain()
            ));
        }
    }

    private void selectFirstIfNeeded() {
        if (selected().isEmpty()) selectedGuard = all.isEmpty() ? null : all.getFirst().guardId();
    }

    @Override
    protected void init() {
        super.init();
        rebuildButtons();
    }

    private void rebuildButtons() {
        clearWidgets();
        int x = panelX();
        int y = panelY();
        int leftW = leftWidth();
        int rightX = x + leftW + 28;
        int rightW = x + panelWidth() - 14 - rightX;

        if (snapshot.squadsEnabled()) {
            addRenderableWidget(Button.builder(scopeLabel(), button -> cycleScope())
                    .bounds(x + 14, y + 30, Math.min(170, leftW - 14), BUTTON_HEIGHT).build());
            addRenderableWidget(Button.builder(Component.translatable("gui.rallyguard.squad.manage"),
                            button -> Minecraft.getInstance().setScreen(new SquadManagementScreen(snapshot, scope)))
                    .bounds(x + panelWidth() - 104, y + 30, 90, BUTTON_HEIGHT).build());
        }

        int start = page * PER_PAGE;
        int end = Math.min(start + PER_PAGE, all.size());
        for (int i = start; i < end; i++) {
            Entry entry = all.get(i);
            int rowY = y + ROW_TOP + (i - start) * ROW_HEIGHT;
            addRenderableWidget(Button.builder(Component.empty(), button -> {
                selectedGuard = entry.guardId();
                rebuildButtons();
            }).bounds(x + 14, rowY, leftW - 14, ROW_HEIGHT - 2).build());
        }

        addRenderableWidget(Button.builder(Component.literal("<"), button -> changePage(-1))
                .bounds(x + 14, y + PANEL_HEIGHT - 28, 24, BUTTON_HEIGHT).build());
        addRenderableWidget(Button.builder(Component.literal(">"), button -> changePage(1))
                .bounds(x + 42, y + PANEL_HEIGHT - 28, 24, BUTTON_HEIGHT).build());

        Optional<Entry> selected = selected();
        int threeW = (rightW - GAP * 2) / 3;
        int twoW = (rightW - GAP) / 2;
        int individualY = y + 112;
        addIndividualButton(rightX, individualY, threeW, "gui.rallyguard.command.summon", NetworkConstants.ACTION_SUMMON);
        addIndividualButton(rightX + threeW + GAP, individualY, threeW,
                selected.filter(entry -> entry.status() == GuardOrderStatus.FOLLOWING).isPresent()
                        ? "gui.rallyguard.command.stop" : "gui.rallyguard.command.follow",
                selected.filter(entry -> entry.status() == GuardOrderStatus.FOLLOWING).isPresent()
                        ? NetworkConstants.ACTION_WAIT : NetworkConstants.ACTION_FOLLOW);
        addIndividualButton(rightX + (threeW + GAP) * 2, individualY, threeW,
                "gui.rallyguard.command.wait", NetworkConstants.ACTION_WAIT);

        Button patrol = Button.builder(Component.translatable(
                        selected.filter(Entry::patrolling).isPresent()
                                ? "gui.rallyguard.command.stop" : "gui.rallyguard.command.patrol"),
                button -> toggleSelectedPatrol()).bounds(rightX, individualY + 24, twoW, BUTTON_HEIGHT).build();
        patrol.active = selected.isPresent();
        addRenderableWidget(patrol);
        Button route = Button.builder(Component.translatable("gui.rallyguard.command.route"), button -> openSelectedRoute())
                .bounds(rightX + twoW + GAP, individualY + 24, twoW, BUTTON_HEIGHT).build();
        route.active = selected.isPresent();
        addRenderableWidget(route);

        int scopeY = y + 202;
        int fourW = (rightW - GAP * 3) / 4;
        addScopeButton(rightX, scopeY, fourW, "gui.rallyguard.command.summon", NetworkConstants.ACTION_SUMMON);
        addScopeButton(rightX + fourW + GAP, scopeY, fourW, "gui.rallyguard.command.follow", NetworkConstants.ACTION_FOLLOW);
        addScopeButton(rightX + (fourW + GAP) * 2, scopeY, fourW, "gui.rallyguard.command.wait", NetworkConstants.ACTION_WAIT);
        addScopeButton(rightX + (fourW + GAP) * 3, scopeY, fourW, "gui.rallyguard.command.patrol", NetworkConstants.ACTION_TOGGLE_PATROL);

        int tacticalY = scopeY + 24;
        addRenderableWidget(Button.builder(Component.translatable("gui.rallyguard.combat.button"),
                        button -> Minecraft.getInstance().setScreen(new GuardCombatScreen(this, scope)))
                .bounds(rightX, tacticalY, threeW, BUTTON_HEIGHT).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.rallyguard.move.button"),
                        button -> Minecraft.getInstance().setScreen(new MoveCategoryScreen(this, scope)))
                .bounds(rightX + threeW + GAP, tacticalY, threeW, BUTTON_HEIGHT).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.rallyguard.defend.button"),
                        button -> Minecraft.getInstance().setScreen(
                                new MoveCategoryScreen(this, scope, MoveCategoryScreen.Mode.DEFEND)))
                .bounds(rightX + (threeW + GAP) * 2, tacticalY, threeW, BUTTON_HEIGHT).build());
    }

    private void addIndividualButton(int x, int y, int width, String key, int action) {
        Button button = Button.builder(Component.translatable(key), ignored -> selected().ifPresent(entry -> {
            sendAction(entry.entityId(), action);
            setEntryStatus(selectedIndex(), statusForAction(action));
        })).bounds(x, y, width, BUTTON_HEIGHT).build();
        button.active = selected().isPresent();
        addRenderableWidget(button);
    }

    private void addScopeButton(int x, int y, int width, String key, int action) {
        Button button = Button.builder(Component.translatable(key), ignored ->
                ClientPlayNetworking.send(new ScopedGuardActionC2SPayload(action, scope)))
                .bounds(x, y, width, BUTTON_HEIGHT).build();
        button.active = !all.isEmpty();
        addRenderableWidget(button);
    }

    private void toggleSelectedPatrol() {
        int index = selectedIndex();
        if (index < 0) return;
        Entry current = all.get(index);
        sendAction(current.entityId(), NetworkConstants.ACTION_TOGGLE_PATROL);
        int status = current.patrolling() ? GuardOrderStatus.WAITING : GuardOrderStatus.PATROLLING;
        all.set(index, copyWithState(current, !current.patrolling(), status, false));
        rebuildButtons();
    }

    private void openSelectedRoute() {
        int index = selectedIndex();
        if (index >= 0) Minecraft.getInstance().setScreen(new GuardRouteScreen(this, index, all.get(index)));
    }

    private void changePage(int delta) {
        int pages = Math.max(1, (all.size() + PER_PAGE - 1) / PER_PAGE);
        page = Math.floorMod(page + delta, pages);
        int start = page * PER_PAGE;
        if (start < all.size()) selectedGuard = all.get(start).guardId();
        rebuildButtons();
    }

    private Component scopeLabel() {
        if (scope.kind() == GuardCommandScopeKind.ALL) {
            return Component.translatable("gui.rallyguard.squad.scope_all");
        }
        if (scope.kind() == GuardCommandScopeKind.UNASSIGNED) {
            return Component.translatable("gui.rallyguard.squad.scope_unassigned");
        }
        return snapshot.squads().stream()
                .filter(squad -> squad.id().equals(scope.squadId().orElseThrow()))
                .findFirst().map(squad -> Component.literal(squad.name()))
                .orElseGet(() -> Component.translatable("gui.rallyguard.squad.scope_all"));
    }

    private void cycleScope() {
        List<GuardCommandScope> options = new ArrayList<>();
        options.add(GuardCommandScope.all());
        options.add(GuardCommandScope.unassigned());
        snapshot.squads().forEach(squad -> options.add(GuardCommandScope.squad(squad.id())));
        int current = options.indexOf(scope);
        scope = options.get((current + 1) % options.size());
        page = 0;
        selectedGuard = null;
        rebuildEntries();
        selectFirstIfNeeded();
        rebuildButtons();
    }

    private Optional<Entry> selected() {
        int index = selectedIndex();
        return index < 0 ? Optional.empty() : Optional.of(all.get(index));
    }

    private int selectedIndex() {
        if (selectedGuard == null) return -1;
        for (int i = 0; i < all.size(); i++) {
            if (all.get(i).guardId().equals(selectedGuard)) return i;
        }
        return -1;
    }

    private void ensureSelectedPage() {
        int index = selectedIndex();
        if (index >= 0) page = index / PER_PAGE;
    }

    private void sendAction(int entityId, int action) {
        ClientPlayNetworking.send(new GuardActionC2SPayload(entityId, action));
    }

    private static int statusForAction(int action) {
        return switch (action) {
            case NetworkConstants.ACTION_SUMMON -> GuardOrderStatus.IDLE;
            case NetworkConstants.ACTION_FOLLOW -> GuardOrderStatus.FOLLOWING;
            case NetworkConstants.ACTION_TOGGLE_PATROL -> GuardOrderStatus.PATROLLING;
            default -> GuardOrderStatus.WAITING;
        };
    }

    private void setEntryStatus(int index, int status) {
        if (index < 0 || index >= all.size()) return;
        Entry current = all.get(index);
        all.set(index, copyWithState(current, status == GuardOrderStatus.PATROLLING, status,
                status == GuardOrderStatus.ROUTING));
        rebuildButtons();
    }

    private Entry copyWithState(Entry current, boolean patrolling, int status, boolean routeActive) {
        return new Entry(
                current.entityId(), current.guardId(), current.name(), patrolling, status, routeActive,
                current.routeWaitSeconds(), current.routePoints(), current.rank(), current.settlement(),
                current.squadId(), current.captain()
        );
    }

    public void updateRouteEntry(int index, boolean active, int waitSeconds, List<GuardListS2CPayload.Point> points) {
        Entry current = all.get(index);
        int status = active ? GuardOrderStatus.ROUTING : GuardOrderStatus.WAITING;
        all.set(index, new Entry(
                current.entityId(), current.guardId(), current.name(), active, status, active, waitSeconds,
                List.copyOf(points), current.rank(), current.settlement(), current.squadId(), current.captain()
        ));
    }

    public void updateRouteDraft(int index, int waitSeconds, List<GuardListS2CPayload.Point> points) {
        Entry current = all.get(index);
        all.set(index, new Entry(
                current.entityId(), current.guardId(), current.name(), current.patrolling(), current.status(),
                current.routeActive(), waitSeconds, List.copyOf(points), current.rank(), current.settlement(),
                current.squadId(), current.captain()
        ));
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        drawPanel(graphics);
        drawRoster(graphics);
        drawContext(graphics);
        super.extractRenderState(graphics, mouseX, mouseY, delta);
    }

    private void drawPanel(GuiGraphicsExtractor graphics) {
        int x = panelX();
        int y = panelY();
        graphics.fill(x, y, x + panelWidth(), y + PANEL_HEIGHT, 0xF0101010);
        graphics.outline(x, y, panelWidth(), PANEL_HEIGHT, WHITE);
        graphics.centeredText(font, title, width / 2, y + 9, WHITE);
        graphics.fill(x + leftWidth() + 14, y + 58, x + leftWidth() + 15, y + PANEL_HEIGHT - 10, 0x44FFFFFF);
    }

    private void drawRoster(GuiGraphicsExtractor graphics) {
        int x = panelX();
        int y = panelY();
        graphics.text(font, Component.translatable("gui.rallyguard.command.roster"), x + 14, y + 52, MUTED);
        if (all.isEmpty()) {
            graphics.centeredText(font, Component.translatable("gui.rallyguard.command.empty"),
                    x + 14 + (leftWidth() - 14) / 2, y + 148, MUTED);
        }
        int start = page * PER_PAGE;
        int end = Math.min(start + PER_PAGE, all.size());
        SquadScreenModel model = new SquadScreenModel(snapshot, scope);
        for (int i = start; i < end; i++) {
            Entry entry = all.get(i);
            int rowY = y + ROW_TOP + (i - start) * ROW_HEIGHT;
            int color = entry.guardId().equals(selectedGuard) ? GOLD : WHITE;
            String captain = entry.captain()
                    ? " · " + Component.translatable("gui.rallyguard.muster.captain").getString() : "";
            graphics.text(font, Component.literal(trimToWidth(entry.name() + captain, leftWidth() - 26)),
                    x + 20, rowY + 5, color);
            String squad = model.squadName(entry.squadId())
                    .orElseGet(() -> Component.translatable("gui.rallyguard.squad.scope_unassigned").getString());
            String detail = presentationComponent(entry).map(Component::getString).orElse("");
            detail = detail.isBlank() ? squad : detail + " · " + squad;
            graphics.text(font, Component.literal(trimToWidth(detail, leftWidth() - 26)),
                    x + 20, rowY + 17, MUTED);
        }
        int pages = Math.max(1, (all.size() + PER_PAGE - 1) / PER_PAGE);
        graphics.text(font, Component.literal((page + 1) + " / " + pages), x + 74, y + PANEL_HEIGHT - 22, MUTED);
    }

    private void drawContext(GuiGraphicsExtractor graphics) {
        int x = panelX();
        int y = panelY();
        int rightX = x + leftWidth() + 28;
        int rightW = x + panelWidth() - 14 - rightX;
        Optional<Entry> selected = selected();
        graphics.text(font, Component.translatable("gui.rallyguard.command.individual_orders"), rightX, y + 52, MUTED);
        if (selected.isEmpty()) {
            graphics.centeredText(font, Component.translatable("gui.rallyguard.command.select_guard"),
                    rightX + rightW / 2, y + 84, MUTED);
        } else {
            Entry entry = selected.get();
            graphics.text(font, Component.literal(trimToWidth(entry.name(), rightW)), rightX, y + 68, WHITE);
            String status = statusComponent(entry.status()).getString();
            String squad = new SquadScreenModel(snapshot, scope).squadName(entry.squadId())
                    .orElseGet(() -> Component.translatable("gui.rallyguard.squad.scope_unassigned").getString());
            graphics.text(font, Component.literal(trimToWidth(status + " · " + squad, rightW)), rightX, y + 82, MUTED);
        }
        graphics.text(font, Component.translatable("gui.rallyguard.command.scope_orders", scopeLabel()),
                rightX, y + 186, MUTED);
    }

    private static Component statusComponent(int status) {
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

    private static Optional<Component> presentationComponent(Entry entry) {
        if (entry.rank().isPresent() && entry.settlement().isPresent()) {
            return Optional.of(entry.rank().get().copy().append(Component.literal(" · ")).append(entry.settlement().get()));
        }
        return entry.rank().or(() -> entry.settlement());
    }

    private String trimToWidth(String text, int maxWidth) {
        if (font.width(text) <= maxWidth) return text;
        String ellipsis = "...";
        return font.plainSubstrByWidth(text, Math.max(0, maxWidth - font.width(ellipsis))) + ellipsis;
    }

    private int panelWidth() {
        return Math.min(MAX_PANEL_WIDTH, Math.max(500, width - SCREEN_PAD * 2));
    }

    private int leftWidth() {
        return Math.max(220, (panelWidth() * 43) / 100);
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
}
