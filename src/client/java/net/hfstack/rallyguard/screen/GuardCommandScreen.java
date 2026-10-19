package net.hfstack.rallyguard.screen;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.hfstack.rallyguard.api.command.GuardCommandScope;
import net.hfstack.rallyguard.api.command.GuardCommandScopeKind;
import net.hfstack.rallyguard.api.command.GuardUnitCategory;
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
    private static final int SECTION = 0xFF777777;
    private static final int DIVIDER = 0x88777777;
    private static final int MAX_PANEL_WIDTH = 700;
    private static final int PANEL_HEIGHT = 300;
    private static final int SCREEN_PAD = 12;
    private static final int PANEL_PADDING = GuiLayout.PANEL_PADDING;
    private static final int CONTENT_PADDING = GuiLayout.CONTENT_PADDING;
    private static final int SECTION_GAP = GuiLayout.SECTION_GAP;
    private static final int BUTTON_HEIGHT = GuiLayout.BUTTON_HEIGHT;
    private static final int GAP = GuiLayout.CONTROL_GAP;

    public record Entry(int entityId, UUID guardId, String name, boolean patrolling, int status, boolean routeActive,
                        int routeWaitSeconds, List<GuardListS2CPayload.Point> routePoints,
                        Optional<Component> rank, Optional<Component> settlement,
                        Optional<UUID> squadId, boolean captain, int categoryId, boolean dismissible) {
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
            } else if (current instanceof NamedRouteManagementScreen management) {
                management.acceptSnapshot(payload);
            } else if (current instanceof NamedRouteEditorScreen editor) {
                editor.acceptSnapshot(payload);
            } else {
                Minecraft.getInstance().setScreen(new GuardCommandScreen(payload));
            }
        });
    }

    void acceptSnapshot(GuardListS2CPayload payload) {
        snapshot = payload;
        scope = new SquadScreenModel(payload, scope).scope();
        rebuildEntries();
        selectFirstIfNeeded();
        page = Math.min(page, Math.max(0, (all.size() - 1) / perPage()));
        ensureSelectedPage();
        rebuildButtons();
    }

    private void rebuildEntries() {
        all.clear();
        for (GuardListS2CPayload.Entry entry : new SquadScreenModel(snapshot, scope).filteredGuards()) {
            all.add(new Entry(
                    entry.entityId(), entry.guardId(), entry.name(), entry.patrolling(), entry.status(),
                    entry.routeActive(), entry.routeWaitSeconds(), entry.routePoints(), entry.rank(),
                    entry.settlement(), entry.squadId(), entry.captain(), entry.categoryId(), entry.dismissible()
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
        List<ScreenLayout.HorizontalSpan> columns = ScreenLayout.twoColumnSpans(
                x, panelWidth(), leftW, PANEL_PADDING, SECTION_GAP);
        ScreenLayout.HorizontalSpan rightColumn = columns.get(1);
        int rightX = rightColumn.x();
        int rightW = rightColumn.width();
        int actionX = rightX + CONTENT_PADDING;
        int actionW = rightW - CONTENT_PADDING * 2;
        ScreenLayout.HorizontalSpan rosterSpan = ScreenLayout.ledgerRosterSpan(x, leftW);

        if (snapshot.squadsEnabled()) {
            List<ScreenLayout.HorizontalSpan> topSpans = ScreenLayout.evenlySpacedHorizontalSpans(
                    x + PANEL_PADDING, panelWidth() - PANEL_PADDING * 2, 3, GAP);
            addRenderableWidget(Button.builder(scopeLabel(), button -> cycleScope(shiftDown() ? -1 : 1))
                    .bounds(topSpans.get(0).x(), y + 30, topSpans.get(0).width(), BUTTON_HEIGHT).build());
            addRenderableWidget(Button.builder(Component.translatable("gui.rallyguard.registry.button"),
                            button -> GlobalGuardRegistryScreen.open(this))
                    .bounds(topSpans.get(1).x(), y + 30, topSpans.get(1).width(), BUTTON_HEIGHT).build());
            addRenderableWidget(Button.builder(Component.translatable("gui.rallyguard.squad.manage"),
                            button -> Minecraft.getInstance().setScreen(new SquadManagementScreen(snapshot, scope)))
                    .bounds(topSpans.get(2).x(), y + 30, topSpans.get(2).width(), BUTTON_HEIGHT).build());
        } else {
            addRenderableWidget(Button.builder(Component.translatable("gui.rallyguard.registry.button"),
                            button -> GlobalGuardRegistryScreen.open(this))
                    .bounds(x + panelWidth() - PANEL_PADDING - 110, y + 30, 110, BUTTON_HEIGHT).build());
        }

        int start = page * perPage();
        int end = Math.min(start + perPage(), all.size());
        for (int i = start; i < end; i++) {
            Entry entry = all.get(i);
            ScreenLayout.VerticalSpan rowSpan = ScreenLayout.ledgerRosterRowVerticalSpan(y, i - start);
            addRenderableWidget(Button.builder(Component.empty(), button -> {
                selectedGuard = entry.guardId();
                rebuildButtons();
            }).bounds(rosterSpan.x(), rowSpan.y(), rosterSpan.width(), rowSpan.height()).build());
        }

        ScreenLayout.VerticalSpan pagerSpan = ScreenLayout.ledgerPagerVerticalSpan(y, panelHeight());
        int pagerX = x + PANEL_PADDING + CONTENT_PADDING;
        addRenderableWidget(Button.builder(Component.literal("<"), button -> changePage(-1))
                .bounds(pagerX, pagerSpan.y(), GuiLayout.COMPACT_BUTTON_WIDTH, pagerSpan.height()).build());
        addRenderableWidget(Button.builder(Component.literal(">"), button -> changePage(1))
                .bounds(pagerX + GuiLayout.COMPACT_BUTTON_WIDTH + GAP, pagerSpan.y(),
                        GuiLayout.COMPACT_BUTTON_WIDTH, pagerSpan.height()).build());

        Optional<Entry> selected = selected();
        List<ScreenLayout.HorizontalSpan> threeSpans =
                ScreenLayout.evenlySpacedHorizontalSpans(actionX, actionW, 3, GAP);
        List<ScreenLayout.HorizontalSpan> twoSpans =
                ScreenLayout.evenlySpacedHorizontalSpans(actionX, actionW, 2, GAP);
        boolean compact = panelHeight() < PANEL_HEIGHT;
        int contentBottom = y + panelHeight() - PANEL_PADDING;
        int individualBottom = y + (compact ? 156 : 164);
        int individualY = compact
                ? y + 108
                : individualBottom - CONTENT_PADDING - BUTTON_HEIGHT * 2 - GAP;
        addIndividualButton(threeSpans.get(0).x(), individualY, threeSpans.get(0).width(),
                "gui.rallyguard.command.summon", NetworkConstants.ACTION_SUMMON);
        addIndividualButton(threeSpans.get(1).x(), individualY, threeSpans.get(1).width(),
                selected.filter(Entry::dismissible).isPresent()
                        ? "gui.rallyguard.command.dismiss"
                        : selected.filter(entry -> entry.status() == GuardOrderStatus.FOLLOWING).isPresent()
                                ? "gui.rallyguard.command.stop" : "gui.rallyguard.command.follow",
                selected.filter(Entry::dismissible).isPresent()
                        ? NetworkConstants.ACTION_DISMISS
                        : selected.filter(entry -> entry.status() == GuardOrderStatus.FOLLOWING).isPresent()
                                ? NetworkConstants.ACTION_WAIT : NetworkConstants.ACTION_FOLLOW);
        addIndividualButton(threeSpans.get(2).x(), individualY, threeSpans.get(2).width(),
                "gui.rallyguard.command.wait", NetworkConstants.ACTION_WAIT);

        Button patrol = Button.builder(Component.translatable(
                        selected.filter(Entry::patrolling).isPresent()
                                ? "gui.rallyguard.command.stop" : "gui.rallyguard.command.patrol"),
                button -> toggleSelectedPatrol()).bounds(
                        twoSpans.get(0).x(), individualY + BUTTON_HEIGHT + GAP,
                        twoSpans.get(0).width(), BUTTON_HEIGHT).build();
        patrol.active = selected.isPresent();
        addRenderableWidget(patrol);
        Button route = Button.builder(Component.translatable("gui.rallyguard.command.route"), button -> openSelectedRoute())
                .bounds(twoSpans.get(1).x(), individualY + BUTTON_HEIGHT + GAP,
                        twoSpans.get(1).width(), BUTTON_HEIGHT).build();
        route.active = selected.isPresent();
        addRenderableWidget(route);

        int scopeY = compact
                ? y + 170
                : contentBottom - CONTENT_PADDING - BUTTON_HEIGHT * 3 - GAP * 2;
        List<ScreenLayout.HorizontalSpan> fourSpans =
                ScreenLayout.evenlySpacedHorizontalSpans(actionX, actionW, 4, GAP);
        addScopeButton(fourSpans.get(0).x(), scopeY, fourSpans.get(0).width(),
                compact ? "gui.rallyguard.command.summon_compact" : "gui.rallyguard.command.summon",
                NetworkConstants.ACTION_SUMMON);
        addScopeButton(fourSpans.get(1).x(), scopeY, fourSpans.get(1).width(),
                compact ? "gui.rallyguard.command.follow_compact" : "gui.rallyguard.command.follow",
                NetworkConstants.ACTION_FOLLOW);
        addScopeButton(fourSpans.get(2).x(), scopeY, fourSpans.get(2).width(),
                compact ? "gui.rallyguard.command.wait_compact" : "gui.rallyguard.command.wait",
                NetworkConstants.ACTION_WAIT);
        addScopeButton(fourSpans.get(3).x(), scopeY, fourSpans.get(3).width(),
                compact ? "gui.rallyguard.command.dismiss_compact" : "gui.rallyguard.command.dismiss",
                NetworkConstants.ACTION_DISMISS);

        int tacticalY = scopeY + BUTTON_HEIGHT + GAP;
        if (compact) {
            addRenderableWidget(Button.builder(Component.translatable("gui.rallyguard.combat.button_compact"),
                            button -> Minecraft.getInstance().setScreen(new GuardCombatScreen(this, scope)))
                    .bounds(fourSpans.get(0).x(), tacticalY, fourSpans.get(0).width(), BUTTON_HEIGHT).build());
            addRenderableWidget(Button.builder(Component.translatable("gui.rallyguard.move.button_compact"),
                            button -> Minecraft.getInstance().setScreen(new MoveCategoryScreen(this, scope)))
                    .bounds(fourSpans.get(1).x(), tacticalY, fourSpans.get(1).width(), BUTTON_HEIGHT).build());
            addRenderableWidget(Button.builder(Component.translatable("gui.rallyguard.defend.button_compact"),
                            button -> Minecraft.getInstance().setScreen(
                                    new MoveCategoryScreen(this, scope, MoveCategoryScreen.Mode.DEFEND)))
                    .bounds(fourSpans.get(2).x(), tacticalY, fourSpans.get(2).width(), BUTTON_HEIGHT).build());
            addRenderableWidget(Button.builder(Component.translatable("gui.rallyguard.command.routes_compact"),
                            button -> Minecraft.getInstance().setScreen(
                                    new ScopedNamedRouteScreen(this, snapshot, scope)))
                    .bounds(fourSpans.get(3).x(), tacticalY, fourSpans.get(3).width(), BUTTON_HEIGHT).build());
            return;
        }
        addRenderableWidget(Button.builder(Component.translatable("gui.rallyguard.combat.button"),
                        button -> Minecraft.getInstance().setScreen(new GuardCombatScreen(this, scope)))
                .bounds(twoSpans.get(0).x(), tacticalY, twoSpans.get(0).width(), BUTTON_HEIGHT).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.rallyguard.move.button"),
                        button -> Minecraft.getInstance().setScreen(new MoveCategoryScreen(this, scope)))
                .bounds(twoSpans.get(1).x(), tacticalY, twoSpans.get(1).width(), BUTTON_HEIGHT).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.rallyguard.defend.button"),
                        button -> Minecraft.getInstance().setScreen(
                                new MoveCategoryScreen(this, scope, MoveCategoryScreen.Mode.DEFEND)))
                .bounds(twoSpans.get(0).x(), tacticalY + BUTTON_HEIGHT + GAP,
                        twoSpans.get(0).width(), BUTTON_HEIGHT).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.rallyguard.command.routes"),
                        button -> Minecraft.getInstance().setScreen(
                                new ScopedNamedRouteScreen(this, snapshot, scope)))
                .bounds(twoSpans.get(1).x(), tacticalY + BUTTON_HEIGHT + GAP,
                        twoSpans.get(1).width(), BUTTON_HEIGHT).build());
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
        button.active = action == NetworkConstants.ACTION_DISMISS
                ? all.stream().anyMatch(Entry::dismissible)
                : !all.isEmpty();
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
        if (index >= 0) Minecraft.getInstance().setScreen(
                new GuardRouteHubScreen(this, snapshot, index, all.get(index))
        );
    }

    private void changePage(int delta) {
        int pages = Math.max(1, (all.size() + perPage() - 1) / perPage());
        page = Math.floorMod(page + delta, pages);
        int start = page * perPage();
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

    private void cycleScope(int delta) {
        List<GuardCommandScope> options = new ArrayList<>();
        options.add(GuardCommandScope.all());
        options.add(GuardCommandScope.unassigned());
        snapshot.squads().forEach(squad -> options.add(GuardCommandScope.squad(squad.id())));
        int current = options.indexOf(scope);
        scope = options.get(Math.floorMod(current + delta, options.size()));
        page = 0;
        selectedGuard = null;
        rebuildEntries();
        selectFirstIfNeeded();
        rebuildButtons();
    }

    private static boolean shiftDown() {
        var window = Minecraft.getInstance().getWindow();
        return InputConstants.isKeyDown(window, InputConstants.KEY_LSHIFT)
                || InputConstants.isKeyDown(window, InputConstants.KEY_RSHIFT);
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
        if (index >= 0) page = index / perPage();
    }

    private void sendAction(int entityId, int action) {
        ClientPlayNetworking.send(new GuardActionC2SPayload(entityId, action));
    }

    private static int statusForAction(int action) {
        return switch (action) {
            case NetworkConstants.ACTION_SUMMON -> GuardOrderStatus.IDLE;
            case NetworkConstants.ACTION_FOLLOW -> GuardOrderStatus.FOLLOWING;
            case NetworkConstants.ACTION_DISMISS -> GuardOrderStatus.IDLE;
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
                current.squadId(), current.captain(), current.categoryId(),
                actionKeepsDismissible(status) && current.dismissible()
        );
    }

    private static boolean actionKeepsDismissible(int status) {
        return status != GuardOrderStatus.WAITING && status != GuardOrderStatus.PATROLLING
                && status != GuardOrderStatus.IDLE;
    }

    public void updateRouteEntry(int index, boolean active, int waitSeconds, List<GuardListS2CPayload.Point> points) {
        Entry current = all.get(index);
        int status = active ? GuardOrderStatus.ROUTING : GuardOrderStatus.WAITING;
        all.set(index, new Entry(
                current.entityId(), current.guardId(), current.name(), active, status, active, waitSeconds,
                List.copyOf(points), current.rank(), current.settlement(), current.squadId(), current.captain(),
                current.categoryId(), false
        ));
    }

    public void updateRouteDraft(int index, int waitSeconds, List<GuardListS2CPayload.Point> points) {
        Entry current = all.get(index);
        all.set(index, new Entry(
                current.entityId(), current.guardId(), current.name(), current.patrolling(), current.status(),
                current.routeActive(), waitSeconds, List.copyOf(points), current.rank(), current.settlement(),
                current.squadId(), current.captain(), current.categoryId(), false
        ));
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        drawPanel(graphics);
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        drawRoster(graphics);
        drawContext(graphics);
    }

    private void drawPanel(GuiGraphicsExtractor graphics) {
        int x = panelX();
        int y = panelY();
        int panelH = panelHeight();
        int leftW = leftWidth();
        List<ScreenLayout.HorizontalSpan> columns = ScreenLayout.twoColumnSpans(
                x, panelWidth(), leftW, PANEL_PADDING, SECTION_GAP);
        ScreenLayout.HorizontalSpan rightColumn = columns.get(1);
        int rightX = rightColumn.x();
        int rightW = rightColumn.width();
        boolean compact = panelH < PANEL_HEIGHT;
        graphics.fill(x, y, x + panelWidth(), y + panelH, 0xF0101010);
        graphics.outline(x, y, panelWidth(), panelHeight(), WHITE);
        graphics.centeredText(font, title, width / 2,
                ScreenLayout.centeredLineY(y, 30, font.lineHeight), WHITE);
        int contentTop = y + 58;
        int contentBottom = y + panelH - PANEL_PADDING;
        int rightBottom = contentBottom;
        int individualBottom = y + (compact ? 156 : 164);
        int scopeTop = compact ? individualBottom : individualBottom + SECTION_GAP;
        int individualY = compact
                ? y + 108
                : individualBottom - CONTENT_PADDING - BUTTON_HEIGHT * 2 - GAP;
        int scopeY = compact
                ? y + 170
                : rightBottom - CONTENT_PADDING - BUTTON_HEIGHT * 3 - GAP * 2;
        int individualDividerY;
        int scopeDividerY;
        if (compact) {
            individualDividerY = y + 80;
            scopeDividerY = y + 168;
        } else {
            List<Integer> individualGaps = ScreenLayout.evenlyDistributedSizes(
                    individualY - contentTop - font.lineHeight * 3 - 1, 5);
            int individualHeaderY = contentTop + individualGaps.get(0);
            individualDividerY = individualHeaderY + font.lineHeight + individualGaps.get(1);
            List<Integer> scopeGaps = ScreenLayout.evenlyDistributedSizes(
                    scopeY - scopeTop - font.lineHeight - 1, 3);
            scopeDividerY = scopeTop + scopeGaps.get(0) + font.lineHeight + scopeGaps.get(1);
        }
        ScreenLayout.HorizontalSpan rosterCard = ScreenLayout.ledgerRosterCardSpan(x, leftW);
        ScreenLayout.VerticalSpan rosterCardVertical = ScreenLayout.ledgerRosterCardVerticalSpan(y, panelH);
        graphics.outline(x + PANEL_PADDING, contentTop, leftW, contentBottom - contentTop, SECTION);
        graphics.outline(rosterCard.x(), rosterCardVertical.y(), rosterCard.width(), rosterCardVertical.height(), SECTION);
        graphics.outline(rightX, contentTop, rightW, individualBottom - contentTop, SECTION);
        graphics.outline(rightX, scopeTop, rightW, rightBottom - scopeTop, SECTION);
        graphics.fill(rightX + CONTENT_PADDING, individualDividerY,
                rightX + rightW - CONTENT_PADDING, individualDividerY + 1, DIVIDER);
        graphics.fill(rightX + CONTENT_PADDING, scopeDividerY,
                rightX + rightW - CONTENT_PADDING, scopeDividerY + 1, DIVIDER);
    }

    private void drawRoster(GuiGraphicsExtractor graphics) {
        int x = panelX();
        int y = panelY();
        ScreenLayout.HorizontalSpan rosterSpan = ScreenLayout.ledgerRosterSpan(x, leftWidth());
        graphics.text(font, Component.translatable("gui.rallyguard.command.roster"),
                x + PANEL_PADDING + CONTENT_PADDING, y + 68, WHITE);
        if (all.isEmpty()) {
            graphics.centeredText(font, Component.translatable("gui.rallyguard.command.empty"),
                    rosterSpan.x() + rosterSpan.width() / 2, y + 148, MUTED);
        }
        int start = page * perPage();
        int end = Math.min(start + perPage(), all.size());
        SquadScreenModel model = new SquadScreenModel(snapshot, scope);
        for (int i = start; i < end; i++) {
            Entry entry = all.get(i);
            int rowY = ScreenLayout.ledgerRosterRowVerticalSpan(y, i - start).y();
            int color = entry.guardId().equals(selectedGuard) ? GOLD : WHITE;
            String captain = entry.captain()
                    ? " · " + Component.translatable("gui.rallyguard.muster.captain").getString() : "";
            graphics.text(font, Component.literal(trimToWidth(
                            entry.name() + captain, rosterSpan.width() - CONTENT_PADDING * 2)),
                    rosterSpan.x() + CONTENT_PADDING, rowY + 4, color);
            String squad = model.squadName(entry.squadId())
                    .orElseGet(() -> Component.translatable("gui.rallyguard.squad.scope_unassigned").getString());
            String detail = presentationComponent(entry).map(Component::getString).orElse("");
            detail = detail.isBlank() ? squad : detail + " · " + squad;
            graphics.text(font, Component.literal(trimToWidth(
                            detail, rosterSpan.width() - CONTENT_PADDING * 2)),
                    rosterSpan.x() + CONTENT_PADDING, rowY + 16, MUTED);
        }
        int pages = Math.max(1, (all.size() + perPage() - 1) / perPage());
        ScreenLayout.VerticalSpan pagerSpan = ScreenLayout.ledgerPagerVerticalSpan(y, panelHeight());
        int pagerX = x + PANEL_PADDING + CONTENT_PADDING;
        graphics.text(font, Component.literal((page + 1) + " / " + pages),
                pagerX + (GuiLayout.COMPACT_BUTTON_WIDTH + GAP) * 2 + CONTENT_PADDING,
                ScreenLayout.centeredLineY(pagerSpan.y(), pagerSpan.height(), font.lineHeight), MUTED);
    }

    private void drawContext(GuiGraphicsExtractor graphics) {
        int x = panelX();
        int y = panelY();
        ScreenLayout.HorizontalSpan rightColumn = ScreenLayout.twoColumnSpans(
                x, panelWidth(), leftWidth(), PANEL_PADDING, SECTION_GAP).get(1);
        int rightX = rightColumn.x();
        int rightW = rightColumn.width();
        Optional<Entry> selected = selected();
        boolean compact = panelHeight() < PANEL_HEIGHT;
        int contentTop = y + 58;
        int individualBottom = y + (compact ? 156 : 164);
        int individualY = compact
                ? y + 108
                : individualBottom - CONTENT_PADDING - BUTTON_HEIGHT * 2 - GAP;
        int scopeTop = compact ? individualBottom : individualBottom + SECTION_GAP;
        int individualHeaderY;
        int individualNameY;
        int individualStatusY;
        if (compact) {
            individualHeaderY = y + 66;
            individualNameY = y + 84;
            individualStatusY = individualNameY + 14;
        } else {
            List<Integer> individualGaps = ScreenLayout.evenlyDistributedSizes(
                    individualY - contentTop - font.lineHeight * 3 - 1, 5);
            individualHeaderY = contentTop + individualGaps.get(0);
            int individualDividerY = individualHeaderY + font.lineHeight + individualGaps.get(1);
            individualNameY = individualDividerY + 1 + individualGaps.get(2);
            individualStatusY = individualNameY + font.lineHeight + individualGaps.get(3);
        }
        graphics.text(font, Component.translatable("gui.rallyguard.command.individual_orders"),
                rightX + CONTENT_PADDING, individualHeaderY, WHITE);
        if (selected.isEmpty()) {
            graphics.centeredText(font, Component.translatable("gui.rallyguard.command.select_guard"),
                    rightX + rightW / 2, y + (compact ? 94 : 96), MUTED);
        } else {
            Entry entry = selected.get();
            graphics.text(font, Component.literal(trimToWidth(entry.name(), rightW - CONTENT_PADDING * 2)),
                    rightX + CONTENT_PADDING, individualNameY, WHITE);
            String status = statusComponent(entry.status()).getString();
            String squad = new SquadScreenModel(snapshot, scope).squadName(entry.squadId())
                    .orElseGet(() -> Component.translatable("gui.rallyguard.squad.scope_unassigned").getString());
            String category = categoryComponent(entry.categoryId()).getString();
            graphics.text(font, Component.literal(trimToWidth(status + " · " + category + " · " + squad,
                            rightW - CONTENT_PADDING * 2)),
                    rightX + CONTENT_PADDING, individualStatusY, MUTED);
        }
        graphics.text(font, Component.translatable("gui.rallyguard.command.scope_orders", scopeLabel()),
                rightX + CONTENT_PADDING, compact ? y + 158 : scopeTop + 8, WHITE);
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

    private static Component categoryComponent(int categoryId) {
        return GuardUnitCategory.fromWireId(categoryId).orElse(GuardUnitCategory.INFANTRY)
                == GuardUnitCategory.RANGED
                ? Component.translatable("gui.rallyguard.move.category.ranged")
                : Component.translatable("gui.rallyguard.move.category.infantry");
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
        return Math.min(MAX_PANEL_WIDTH, Math.max(280, width - SCREEN_PAD * 2));
    }

    private int leftWidth() {
        return ScreenLayout.ledgerRosterWidth(panelWidth());
    }

    private int panelX() {
        return (width - panelWidth()) / 2;
    }

    private int panelY() {
        return (height - panelHeight()) / 2;
    }

    private int panelHeight() {
        return Math.min(PANEL_HEIGHT, Math.max(220, height - SCREEN_PAD));
    }

    private int perPage() {
        if (panelHeight() < 250) return 2;
        return panelHeight() < PANEL_HEIGHT ? 3 : 5;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
