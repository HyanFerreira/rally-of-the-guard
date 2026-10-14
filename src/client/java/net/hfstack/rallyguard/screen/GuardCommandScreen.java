package net.hfstack.rallyguard.screen;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.hfstack.rallyguard.network.NetworkConstants;
import net.hfstack.rallyguard.network.payload.GuardActionC2SPayload;
import net.hfstack.rallyguard.network.payload.GuardListS2CPayload;
import net.hfstack.rallyguard.network.payload.ScopedGuardActionC2SPayload;
import net.hfstack.rallyguard.api.command.GuardCommandScope;
import net.hfstack.rallyguard.api.command.GuardCommandScopeKind;
import net.hfstack.rallyguard.order.GuardOrderStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class GuardCommandScreen extends Screen {
    private static final int WHITE = 0xFFFFFFFF;
    private static final int MUTED = 0xFFCCCCCC;
    private static final int EMPTY_TEXT = 0xFFAAAAAA;

    public static record Entry(int entityId, UUID guardId, String name, boolean patrolling, int status, boolean routeActive,
                               int routeWaitSeconds, List<GuardListS2CPayload.Point> routePoints,
                               Optional<Component> rank, Optional<Component> settlement,
                               Optional<UUID> squadId, boolean captain) {
    }

    private final List<Entry> all = new ArrayList<>();
    private GuardListS2CPayload snapshot;
    private GuardCommandScope scope;
    private int page = 0;

    private static final int PER_PAGE = 4;
    private static final int MAX_PANEL_W = 650;
    private static final int PANEL_H = 238;
    private static final int SCREEN_PAD = 12;

    private static final int MARGIN_L = 16;
    private static final int MARGIN_R = 16;

    private static final int HEADER_Y = 48;
    private static final int HEADER_LINE_Y = 62;
    private static final int ROW_TOP = 62;
    private static final int ROW_HEIGHT = 28;

    private static final int COL_NAME_X = MARGIN_L;
    private static final int COL_SQUAD_X = 126;
    private static final int COL_STATUS_X = 226;

    private static final int BTN_SUMMON_W = 54;
    private static final int BTN_FOLLOW_W = 48;
    private static final int BTN_WAIT_W = 58;
    private static final int BTN_PATROL_W = 62;
    private static final int BTN_ROUTE_W = 42;
    private static final int BTN_H = 18;
    private static final int BTN_GAP = 2;

    public GuardCommandScreen(GuardListS2CPayload snapshot) {
        this(snapshot, GuardCommandScope.all());
    }

    GuardCommandScreen(GuardListS2CPayload snapshot, GuardCommandScope scope) {
        super(Component.translatable("gui.rallyguard.command.title"));
        this.snapshot = snapshot;
        this.scope = scope;
        rebuildEntries();
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
        this.snapshot = payload;
        this.scope = new SquadScreenModel(payload, scope).scope();
        rebuildEntries();
        page = Math.min(page, Math.max(0, (all.size() - 1) / PER_PAGE));
        rebuildButtons();
    }

    private void rebuildEntries() {
        all.clear();
        for (GuardListS2CPayload.Entry e : new SquadScreenModel(snapshot, scope).filteredGuards()) {
            all.add(new Entry(
                    e.entityId(),
                    e.guardId(),
                    e.name(),
                    e.patrolling(),
                    e.status(),
                    e.routeActive(),
                    e.routeWaitSeconds(),
                    e.routePoints(),
                    e.rank(),
                    e.settlement(),
                    e.squadId(),
                    e.captain()
            ));
        }
    }

    @Override
    protected void init() {
        super.init();
        rebuildButtons();
    }

    private void rebuildButtons() {
        this.clearWidgets();

        int panelW = panelWidth();
        int x = panelX();
        int y = (this.height - PANEL_H) / 2;

        int start = page * PER_PAGE;
        int end = Math.min(start + PER_PAGE, all.size());

        int groupWidth = actionGroupWidth();
        int actionsRight = x + panelW - MARGIN_R;
        int actionsLeft = actionsRight - groupWidth;

        if (snapshot.squadsEnabled()) {
            this.addRenderableWidget(Button.builder(scopeLabel(), b -> cycleScope())
                    .bounds(x + 12, y + 20, 150, BTN_H).build());
        }

        for (int i = start; i < end; i++) {
            final int idx = i;
            Entry e = all.get(i);

            int rowTop = y + ROW_TOP + (i - start) * ROW_HEIGHT;
            int rowMidY = rowTop + (ROW_HEIGHT / 2);
            int btnY = rowMidY - (BTN_H / 2);

            int btnX = actionsLeft;
            Button summon = Button.builder(
                    Component.translatable("gui.rallyguard.command.summon"),
                    b -> {
                        sendAction(e.entityId(), NetworkConstants.ACTION_SUMMON);
                        setEntryStatus(idx, GuardOrderStatus.IDLE);
                    }
            ).bounds(btnX, btnY, BTN_SUMMON_W, BTN_H).build();

            btnX += BTN_SUMMON_W + BTN_GAP;
            boolean following = e.status() == GuardOrderStatus.FOLLOWING;
            Component followLabel = following
                    ? Component.translatable("gui.rallyguard.command.stop")
                    : Component.translatable("gui.rallyguard.command.follow");
            Button follow = Button.builder(
                    followLabel,
                    b -> {
                        if (following) {
                            sendAction(e.entityId(), NetworkConstants.ACTION_WAIT);
                            setEntryStatus(idx, GuardOrderStatus.WAITING);
                        } else {
                            sendAction(e.entityId(), NetworkConstants.ACTION_FOLLOW);
                            setEntryStatus(idx, GuardOrderStatus.FOLLOWING);
                        }
                    }
            ).bounds(btnX, btnY, BTN_FOLLOW_W, BTN_H).build();

            btnX += BTN_FOLLOW_W + BTN_GAP;
            Button wait = Button.builder(
                    Component.translatable("gui.rallyguard.command.wait"),
                    b -> {
                        sendAction(e.entityId(), NetworkConstants.ACTION_WAIT);
                        setEntryStatus(idx, GuardOrderStatus.WAITING);
                    }
            ).bounds(btnX, btnY, BTN_WAIT_W, BTN_H).build();

            btnX += BTN_WAIT_W + BTN_GAP;
            Component patrolLabel = e.patrolling()
                    ? Component.translatable("gui.rallyguard.command.stop")
                    : Component.translatable("gui.rallyguard.command.patrol");
            Button patrol = Button.builder(patrolLabel, b -> {
                sendAction(e.entityId(), NetworkConstants.ACTION_TOGGLE_PATROL);
                Entry curr = all.get(idx);
                int status = curr.patrolling() ? GuardOrderStatus.WAITING : GuardOrderStatus.PATROLLING;
                all.set(idx, new Entry(
                        curr.entityId(),
                        curr.guardId(),
                        curr.name(),
                        !curr.patrolling(),
                        status,
                        false,
                        curr.routeWaitSeconds(),
                        curr.routePoints(),
                        curr.rank(),
                        curr.settlement(),
                        curr.squadId(),
                        curr.captain()
                ));
                rebuildButtons();
            }).bounds(btnX, btnY, BTN_PATROL_W, BTN_H).build();

            btnX += BTN_PATROL_W + BTN_GAP;
            Button route = Button.builder(
                    Component.translatable("gui.rallyguard.command.route"),
                    b -> Minecraft.getInstance().setScreen(new GuardRouteScreen(this, idx, e))
            ).bounds(btnX, btnY, BTN_ROUTE_W, BTN_H).build();

            this.addRenderableWidget(summon);
            this.addRenderableWidget(follow);
            this.addRenderableWidget(wait);
            this.addRenderableWidget(patrol);
            this.addRenderableWidget(route);
        }

        Button prev = Button.builder(Component.literal("<"), b -> {
            if (page > 0) {
                page--;
                rebuildButtons();
            }
        }).bounds(x + 8, y + PANEL_H - 28, 22, 20).build();

        Button next = Button.builder(Component.literal(">"), b -> {
            if ((page + 1) * PER_PAGE < all.size()) {
                page++;
                rebuildButtons();
            }
        }).bounds(x + panelW - 30, y + PANEL_H - 28, 22, 20).build();

        this.addRenderableWidget(prev);
        this.addRenderableWidget(next);

        Button combat = Button.builder(
                Component.translatable("gui.rallyguard.combat.button"),
                b -> Minecraft.getInstance().setScreen(new GuardCombatScreen(this, scope))
        ).bounds(x + 38, y + PANEL_H - 28, 76, 20).build();
        this.addRenderableWidget(combat);

        if (snapshot.squadsEnabled()) {
            this.addRenderableWidget(Button.builder(
                    Component.translatable("gui.rallyguard.squad.manage"),
                    b -> Minecraft.getInstance().setScreen(new SquadManagementScreen(snapshot, scope))
            ).bounds(x + 118, y + PANEL_H - 28, 76, 20).build());

            int batchY = y + PANEL_H - 56;
            int batchX = x + 12;
            addBatchButton(batchX, batchY, 62, "gui.rallyguard.command.summon", NetworkConstants.ACTION_SUMMON);
            boolean allFollowing = !all.isEmpty() && all.stream()
                    .allMatch(entry -> entry.status() == GuardOrderStatus.FOLLOWING);
            addBatchButton(
                    batchX + 66,
                    batchY,
                    62,
                    allFollowing ? "gui.rallyguard.command.stop" : "gui.rallyguard.command.follow",
                    allFollowing ? NetworkConstants.ACTION_WAIT : NetworkConstants.ACTION_FOLLOW
            );
            addBatchButton(batchX + 132, batchY, 62, "gui.rallyguard.command.wait", NetworkConstants.ACTION_WAIT);
            addBatchButton(batchX + 198, batchY, 62, "gui.rallyguard.command.patrol", NetworkConstants.ACTION_TOGGLE_PATROL);
        }
    }

    private void addBatchButton(int x, int y, int width, String key, int action) {
        this.addRenderableWidget(Button.builder(
                Component.translatable(key),
                button -> ClientPlayNetworking.send(new ScopedGuardActionC2SPayload(action, scope))
        ).bounds(x, y, width, BTN_H).build());
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
                .findFirst()
                .map(squad -> Component.literal(squad.name()))
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
        rebuildEntries();
        rebuildButtons();
    }

    private void sendAction(int entityId, int action) {
        ClientPlayNetworking.send(new GuardActionC2SPayload(entityId, action));
    }

    private void setEntryStatus(int idx, int status) {
        Entry curr = all.get(idx);
        all.set(idx, new Entry(
                curr.entityId(),
                curr.guardId(),
                curr.name(),
                status == GuardOrderStatus.PATROLLING,
                status,
                status == GuardOrderStatus.ROUTING,
                curr.routeWaitSeconds(),
                curr.routePoints(),
                curr.rank(),
                curr.settlement(),
                curr.squadId(),
                curr.captain()
        ));
        rebuildButtons();
    }

    public void updateRouteEntry(int idx, boolean active, int waitSeconds, List<GuardListS2CPayload.Point> points) {
        Entry curr = all.get(idx);
        int status = active ? GuardOrderStatus.ROUTING : GuardOrderStatus.WAITING;
        all.set(idx, new Entry(
                curr.entityId(), curr.guardId(), curr.name(), active, status, active, waitSeconds, List.copyOf(points),
                curr.rank(), curr.settlement(), curr.squadId(), curr.captain()
        ));
    }

    public void updateRouteDraft(int idx, int waitSeconds, List<GuardListS2CPayload.Point> points) {
        Entry curr = all.get(idx);
        all.set(idx, new Entry(
                curr.entityId(),
                curr.guardId(),
                curr.name(),
                curr.patrolling(),
                curr.status(),
                curr.routeActive(),
                waitSeconds,
                List.copyOf(points),
                curr.rank(),
                curr.settlement(),
                curr.squadId(),
                curr.captain()
        ));
    }

    private static int actionGroupWidth() {
        return BTN_SUMMON_W + BTN_GAP + BTN_FOLLOW_W + BTN_GAP + BTN_WAIT_W + BTN_GAP + BTN_PATROL_W + BTN_GAP + BTN_ROUTE_W;
    }

    private int panelWidth() {
        return Math.min(MAX_PANEL_W, Math.max(320, this.width - SCREEN_PAD * 2));
    }

    private int panelX() {
        return (this.width - panelWidth()) / 2;
    }

    private String trimToWidth(String text, int maxWidth) {
        if (maxWidth <= 0) return "";
        if (this.font.width(text) <= maxWidth) return text;

        String ellipsis = "...";
        int ellipsisW = this.font.width(ellipsis);
        if (maxWidth <= ellipsisW) {
            return this.font.plainSubstrByWidth(text, maxWidth);
        }
        return this.font.plainSubstrByWidth(text, maxWidth - ellipsisW) + ellipsis;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor ctx, int mouseX, int mouseY, float delta) {
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor ctx, int mouseX, int mouseY, float delta) {
        drawPanel(ctx);
        drawRows(ctx);
        super.extractRenderState(ctx, mouseX, mouseY, delta);
    }

    private void drawPanel(GuiGraphicsExtractor ctx) {
        int panelW = panelWidth();
        int x = panelX();
        int y = (this.height - PANEL_H) / 2;

        ctx.fill(x, y, x + panelW, y + PANEL_H, 0xF0101010);

        ctx.fill(x, y, x + panelW, y + 1, WHITE);
        ctx.fill(x, y + PANEL_H - 1, x + panelW, y + PANEL_H, WHITE);
        ctx.fill(x, y, x + 1, y + PANEL_H, WHITE);
        ctx.fill(x + panelW - 1, y, x + panelW, y + PANEL_H, WHITE);

        ctx.centeredText(this.font,
                Component.translatable("gui.rallyguard.command.title"),
                this.width / 2, y + 8, WHITE);

        ctx.text(this.font,
                Component.translatable("gui.rallyguard.command.name"),
                x + COL_NAME_X, y + HEADER_Y, MUTED);

        ctx.text(this.font,
                Component.translatable("gui.rallyguard.command.squad"),
                x + COL_SQUAD_X, y + HEADER_Y, MUTED);

        ctx.text(this.font,
                Component.translatable("gui.rallyguard.command.status"),
                x + COL_STATUS_X, y + HEADER_Y, MUTED);

        int groupWidth = actionGroupWidth();
        int actionsRight = x + panelW - MARGIN_R;
        int actionsLeft = actionsRight - groupWidth;
        int actionsCenterX = actionsLeft + groupWidth / 2;

        Component actions = Component.translatable("gui.rallyguard.command.actions");
        int actionsHeaderW = this.font.width(actions);
        ctx.text(this.font, actions, actionsCenterX - (actionsHeaderW / 2), y + HEADER_Y, MUTED);

        ctx.fill(x + 6, y + HEADER_LINE_Y, x + panelW - 6, y + HEADER_LINE_Y + 1, 0x33FFFFFF);
    }

    private void drawRows(GuiGraphicsExtractor ctx) {
        int panelW = panelWidth();
        int x = panelX();
        int y = (this.height - PANEL_H) / 2;

        if (all.isEmpty()) {
            ctx.centeredText(this.font,
                    Component.translatable("gui.rallyguard.command.empty"),
                    this.width / 2, y + (PANEL_H / 2), EMPTY_TEXT);
            drawPageIndicator(ctx, x, y);
            return;
        }

        int start = page * PER_PAGE;
        int end = Math.min(start + PER_PAGE, all.size());

        int fontH = this.font.lineHeight;
        int actionsRight = x + panelW - MARGIN_R;
        int actionsLeft = actionsRight - actionGroupWidth();
        SquadScreenModel screenModel = new SquadScreenModel(snapshot, scope);

        for (int i = start; i < end; i++) {
            Entry e = all.get(i);

            int rowTop = y + ROW_TOP + (i - start) * ROW_HEIGHT;
            int rowMidY = rowTop + (ROW_HEIGHT / 2);
            int rowBot = rowTop + ROW_HEIGHT;
            Optional<Component> presentation = presentationComponent(e);
            int textY = presentation.isPresent()
                    ? rowTop + 3
                    : ScreenLayout.centeredLineY(rowTop, ROW_HEIGHT, fontH);
            int statusY = ScreenLayout.centeredLineY(rowTop, ROW_HEIGHT, fontH);

            ctx.fill(x + 6, rowBot - 1, x + panelW - 6, rowBot, 0x22FFFFFF);

            int maxNameW = (x + COL_SQUAD_X - 12) - (x + COL_NAME_X);
            String name = trimToWidth(e.name(), maxNameW);
            ctx.text(this.font, Component.literal(name), x + COL_NAME_X, textY, WHITE);
            presentation.ifPresent(label -> {
                String metadata = trimToWidth(label.getString(), maxNameW);
                ctx.text(
                        this.font,
                        Component.literal(metadata).setStyle(label.getStyle()),
                        x + COL_NAME_X,
                        rowTop + 15,
                        EMPTY_TEXT
                );
            });

            int maxSquadW = (x + COL_STATUS_X - 12) - (x + COL_SQUAD_X);
            String squad = screenModel.squadName(e.squadId())
                    .orElseGet(() -> Component.translatable("gui.rallyguard.squad.scope_unassigned").getString());
            ctx.text(this.font, Component.literal(trimToWidth(squad, maxSquadW)),
                    x + COL_SQUAD_X, statusY, MUTED);

            int maxStatusW = (actionsLeft - 12) - (x + COL_STATUS_X);
            String status = trimToWidth(statusComponent(e.status()).getString(), maxStatusW);
            ctx.text(this.font, Component.literal(status), x + COL_STATUS_X, statusY, MUTED);
        }

        drawPageIndicator(ctx, x, y);
    }

    private static Component statusComponent(int status) {
        return switch (status) {
            case GuardOrderStatus.FOLLOWING -> Component.translatable("gui.rallyguard.command.status.following");
            case GuardOrderStatus.WAITING -> Component.translatable("gui.rallyguard.command.status.waiting");
            case GuardOrderStatus.PATROLLING -> Component.translatable("gui.rallyguard.command.status.patrolling");
            case GuardOrderStatus.ROUTING -> Component.translatable("gui.rallyguard.command.status.routing");
            default -> Component.translatable("gui.rallyguard.command.status.idle");
        };
    }

    private static Optional<Component> presentationComponent(Entry entry) {
        if (entry.rank().isPresent() && entry.settlement().isPresent()) {
            return Optional.of(entry.rank().get().copy()
                    .append(Component.literal(" · "))
                    .append(entry.settlement().get()));
        }
        return entry.rank().or(() -> entry.settlement());
    }

    private void drawPageIndicator(GuiGraphicsExtractor ctx, int x, int y) {
        int totalPages = Math.max(1, (all.size() + PER_PAGE - 1) / PER_PAGE);
        String pg = (page + 1) + " / " + totalPages;
        int w = this.font.width(pg);
        ctx.text(this.font, Component.literal(pg),
                x + (panelWidth() - w) / 2, y + PANEL_H - 24, WHITE);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
