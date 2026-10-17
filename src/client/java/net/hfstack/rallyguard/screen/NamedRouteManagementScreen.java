package net.hfstack.rallyguard.screen;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.hfstack.rallyguard.config.RallyConfig;
import net.hfstack.rallyguard.network.NetworkConstants;
import net.hfstack.rallyguard.network.payload.GuardListS2CPayload;
import net.hfstack.rallyguard.network.payload.NamedRouteAssignmentC2SPayload;
import net.hfstack.rallyguard.network.payload.NamedRouteMutationC2SPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

public final class NamedRouteManagementScreen extends Screen {
    private static final int WHITE = 0xFFFFFFFF;
    private static final int MUTED = 0xFFAAAAAA;
    private static final int GOLD = 0xFFFFC94A;
    private static final int MAX_PANEL_WIDTH = 700;
    private static final int PANEL_HEIGHT = 340;
    private static final int ROUTE_ROWS = 6;
    private static final int GUARD_ROWS = 4;
    private static final int ROUTE_ROW_HEIGHT = 34;
    private static final int GUARD_ROW_HEIGHT = 32;
    private static final int ROUTE_TOP = 72;
    private static final int ROUTE_LIST_WIDTH = 180;

    private final GuardCommandScreen parent;
    private final UUID sourceGuardId;
    private GuardListS2CPayload snapshot;
    private UUID selectedRouteId;
    private UUID selectedAvailableGuard;
    private UUID selectedAssignedGuard;
    private int routePage;
    private int availablePage;
    private int assignedPage;
    private int draftWaitSeconds;
    private List<GuardListS2CPayload.Point> draftPoints = List.of();
    private String draftName = "";
    private EditBox nameBox;

    public NamedRouteManagementScreen(GuardCommandScreen parent, GuardListS2CPayload snapshot, UUID sourceGuardId) {
        super(Component.translatable("gui.rallyguard.named_route.title"));
        this.parent = parent;
        this.snapshot = snapshot;
        this.sourceGuardId = sourceGuardId;
        if (!snapshot.namedRoutes().isEmpty()) selectRoute(snapshot.namedRoutes().getFirst());
        else useSourceDraft();
    }

    void acceptSnapshot(GuardListS2CPayload updated) {
        snapshot = updated;
        Optional<GuardListS2CPayload.NamedRouteEntry> selected = selectedRoute();
        if (selected.isPresent()) loadDraft(selected.get());
        else if (selectedRouteId != null) {
            selectedRouteId = null;
            useSourceDraft();
        }
        retainGuardSelections();
        clampPages();
        rebuildControls();
    }

    @Override
    protected void init() {
        super.init();
        rebuildControls();
    }

    private void rebuildControls() {
        clearWidgets();
        clampPages();
        int x = panelX();
        int y = panelY();
        int panelW = panelWidth();
        int detailX = x + 208;
        int detailW = x + panelW - 14 - detailX;
        int guardColumnW = guardColumnWidth();

        addRenderableWidget(Button.builder(Component.translatable("gui.rallyguard.named_route.new_from_guard"),
                        button -> newFromGuard())
                .bounds(x + 14, y + 30, ROUTE_LIST_WIDTH, 20).build());

        List<GuardListS2CPayload.NamedRouteEntry> routes = snapshot.namedRoutes();
        int routeStart = routePage * ROUTE_ROWS;
        int routeEnd = Math.min(routeStart + ROUTE_ROWS, routes.size());
        for (int i = routeStart; i < routeEnd; i++) {
            GuardListS2CPayload.NamedRouteEntry route = routes.get(i);
            int rowY = y + ROUTE_TOP + (i - routeStart) * ROUTE_ROW_HEIGHT;
            addRenderableWidget(Button.builder(Component.empty(), button -> {
                selectRoute(route);
                rebuildControls();
            }).bounds(x + 14, rowY, ROUTE_LIST_WIDTH, ROUTE_ROW_HEIGHT - 2).build());
        }

        int deleteW = 70;
        int saveW = 86;
        int nameW = Math.max(140, detailW - saveW - deleteW - 12);
        nameBox = new EditBox(font, detailX, y + 30, nameW, 20,
                Component.translatable("gui.rallyguard.named_route.name"));
        nameBox.setMaxLength(48);
        nameBox.setValue(draftName);
        addRenderableWidget(nameBox);

        Button save = Button.builder(Component.translatable(selectedRouteId == null
                                ? "gui.rallyguard.named_route.create" : "gui.rallyguard.named_route.update"),
                        button -> submitRoute())
                .bounds(detailX + nameW + 4, y + 30, saveW, 20).build();
        save.active = draftPoints.size() >= 2;
        addRenderableWidget(save);
        Button delete = Button.builder(Component.translatable("gui.rallyguard.named_route.delete"),
                        button -> confirmDelete())
                .bounds(detailX + nameW + saveW + 8, y + 30, deleteW, 20).build();
        delete.active = selectedRouteId != null;
        addRenderableWidget(delete);

        addRenderableWidget(Button.builder(Component.literal("-5s"), button -> changeWait(-5))
                .bounds(detailX, y + 58, 42, 20).build());
        addRenderableWidget(Button.builder(Component.literal("+5s"), button -> changeWait(5))
                .bounds(detailX + 48, y + 58, 42, 20).build());
        Button useGuard = Button.builder(Component.translatable("gui.rallyguard.named_route.use_guard_route"),
                        button -> copySourceRoute())
                .bounds(detailX + 98, y + 58, 130, 20).build();
        useGuard.active = sourceEntry().map(entry -> entry.routePoints().size() >= 2).orElse(false);
        addRenderableWidget(useGuard);

        List<GuardListS2CPayload.Entry> available = availableGuards();
        List<GuardListS2CPayload.Entry> assigned = assignedGuards();
        int availableX = detailX;
        int assignedX = x + panelW - 14 - guardColumnW;
        addGuardRows(available, availablePage, availableX, selectedAvailableGuard, guard -> {
            selectedAvailableGuard = guard.guardId();
            selectedAssignedGuard = null;
            rebuildControls();
        });
        addGuardRows(assigned, assignedPage, assignedX, selectedAssignedGuard, guard -> {
            selectedAssignedGuard = guard.guardId();
            selectedAvailableGuard = null;
            rebuildControls();
        });

        int arrowX = detailX + guardColumnW + 8;
        Button assign = Button.builder(Component.literal("→"), button -> assignSelected())
                .bounds(arrowX, y + 183, 36, 20).build();
        assign.active = selectedRouteId != null && selectedAvailable().isPresent() && selectedRouteInCurrentDimension();
        addRenderableWidget(assign);
        Button unassign = Button.builder(Component.literal("←"), button -> unassignSelected())
                .bounds(arrowX, y + 211, 36, 20).build();
        unassign.active = selectedAssigned().isPresent();
        addRenderableWidget(unassign);

        int bottom = y + PANEL_HEIGHT - 28;
        addRenderableWidget(Button.builder(Component.literal("<"), button -> changeRoutePage(-1))
                .bounds(x + 14, bottom, 24, 20).build());
        addRenderableWidget(Button.builder(Component.literal(">"), button -> changeRoutePage(1))
                .bounds(x + 42, bottom, 24, 20).build());
        addRenderableWidget(Button.builder(Component.literal("<"), button -> changeAvailablePage(-1))
                .bounds(availableX, bottom, 24, 20).build());
        addRenderableWidget(Button.builder(Component.literal(">"), button -> changeAvailablePage(1))
                .bounds(availableX + 28, bottom, 24, 20).build());
        addRenderableWidget(Button.builder(Component.literal("<"), button -> changeAssignedPage(-1))
                .bounds(assignedX, bottom, 24, 20).build());
        addRenderableWidget(Button.builder(Component.literal(">"), button -> changeAssignedPage(1))
                .bounds(assignedX + 28, bottom, 24, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.rallyguard.route.back"), button -> back())
                .bounds(x + panelW - 84, bottom, 70, 20).build());
    }

    private void addGuardRows(List<GuardListS2CPayload.Entry> guards, int page, int rowX, UUID selectedGuard,
                              Consumer<GuardListS2CPayload.Entry> onSelect) {
        int start = page * GUARD_ROWS;
        int end = Math.min(start + GUARD_ROWS, guards.size());
        for (int i = start; i < end; i++) {
            GuardListS2CPayload.Entry guard = guards.get(i);
            int rowY = panelY() + 154 + (i - start) * GUARD_ROW_HEIGHT;
            addRenderableWidget(Button.builder(Component.empty(), button -> onSelect.accept(guard))
                    .bounds(rowX, rowY, guardColumnWidth(), GUARD_ROW_HEIGHT - 2).build());
        }
    }

    private void submitRoute() {
        draftName = nameBox.getValue();
        int action = selectedRouteId == null
                ? NetworkConstants.NAMED_ROUTE_CREATE : NetworkConstants.NAMED_ROUTE_UPDATE;
        List<NamedRouteMutationC2SPayload.Point> points = draftPoints.stream()
                .map(point -> new NamedRouteMutationC2SPayload.Point(point.x(), point.y(), point.z())).toList();
        ClientPlayNetworking.send(new NamedRouteMutationC2SPayload(
                action, Optional.ofNullable(selectedRouteId), draftName, draftWaitSeconds, points,
                selectedRouteId == null
                        ? sourceEntry().map(GuardListS2CPayload.Entry::entityId)
                                .orElse(NamedRouteMutationC2SPayload.NO_SOURCE_GUARD)
                        : NamedRouteMutationC2SPayload.NO_SOURCE_GUARD
        ));
    }

    private void confirmDelete() {
        selectedRoute().ifPresent(route -> Minecraft.getInstance().setScreen(new ConfirmScreen(
                confirmed -> {
                    if (confirmed) ClientPlayNetworking.send(new NamedRouteMutationC2SPayload(
                            NetworkConstants.NAMED_ROUTE_DELETE, Optional.of(route.id()), "", 0, List.of(),
                            NamedRouteMutationC2SPayload.NO_SOURCE_GUARD
                    ));
                    Minecraft.getInstance().setScreen(this);
                },
                Component.translatable("gui.rallyguard.named_route.delete_confirm_title"),
                Component.translatable("gui.rallyguard.named_route.delete_confirm", route.name())
        )));
    }

    private void assignSelected() {
        Optional<GuardListS2CPayload.Entry> guard = selectedAvailable();
        Optional<GuardListS2CPayload.NamedRouteEntry> route = selectedRoute();
        if (guard.isEmpty() || route.isEmpty()) return;
        GuardListS2CPayload.Entry entry = guard.get();
        if (entry.namedRouteId().isPresent()) {
            String previous = routeName(entry.namedRouteId());
            Minecraft.getInstance().setScreen(new ConfirmScreen(
                    confirmed -> {
                        if (confirmed) sendAssignment(entry.entityId(), Optional.of(route.get().id()));
                        Minecraft.getInstance().setScreen(this);
                    },
                    Component.translatable("gui.rallyguard.named_route.move_confirm_title"),
                    Component.translatable("gui.rallyguard.named_route.move_confirm",
                            entry.name(), previous, route.get().name())
            ));
            return;
        }
        sendAssignment(entry.entityId(), Optional.of(route.get().id()));
    }

    private void unassignSelected() {
        selectedAssigned().ifPresent(guard -> sendAssignment(guard.entityId(), Optional.empty()));
    }

    private void sendAssignment(int entityId, Optional<UUID> routeId) {
        ClientPlayNetworking.send(new NamedRouteAssignmentC2SPayload(entityId, routeId));
    }

    private void newFromGuard() {
        selectedRouteId = null;
        draftName = "";
        useSourceDraft();
        selectedAvailableGuard = null;
        selectedAssignedGuard = null;
        rebuildControls();
        setInitialFocus(nameBox);
    }

    private void copySourceRoute() {
        selectedRoute().ifPresent(route -> Minecraft.getInstance().setScreen(new ConfirmScreen(
                confirmed -> {
                    if (confirmed) {
                        captureName();
                        useSourceDraft();
                        rebuildControls();
                    }
                    Minecraft.getInstance().setScreen(this);
                },
                Component.translatable("gui.rallyguard.named_route.replace_points_title"),
                Component.translatable("gui.rallyguard.named_route.replace_points", route.name())
        )));
    }

    private void useSourceDraft() {
        sourceEntry().ifPresentOrElse(entry -> {
            draftWaitSeconds = entry.routeWaitSeconds() > 0
                    ? entry.routeWaitSeconds() : RallyConfig.routeDefaultWaitSeconds();
            draftPoints = List.copyOf(entry.routePoints());
        }, () -> {
            draftWaitSeconds = RallyConfig.routeDefaultWaitSeconds();
            draftPoints = List.of();
        });
    }

    private void changeWait(int delta) {
        captureName();
        draftWaitSeconds = Math.max(0, Math.min(600, draftWaitSeconds + delta));
        rebuildControls();
    }

    private void captureName() {
        if (nameBox != null) draftName = nameBox.getValue();
    }

    private void selectRoute(GuardListS2CPayload.NamedRouteEntry route) {
        selectedRouteId = route.id();
        loadDraft(route);
        selectedAvailableGuard = null;
        selectedAssignedGuard = null;
        availablePage = 0;
        assignedPage = 0;
    }

    private void loadDraft(GuardListS2CPayload.NamedRouteEntry route) {
        draftName = route.name();
        draftWaitSeconds = route.waitSeconds();
        draftPoints = List.copyOf(route.points());
    }

    private Optional<GuardListS2CPayload.NamedRouteEntry> selectedRoute() {
        if (selectedRouteId == null) return Optional.empty();
        return snapshot.namedRoutes().stream().filter(route -> route.id().equals(selectedRouteId)).findFirst();
    }

    private Optional<GuardListS2CPayload.Entry> sourceEntry() {
        return snapshot.entries().stream().filter(entry -> entry.guardId().equals(sourceGuardId)).findFirst();
    }

    private List<GuardListS2CPayload.Entry> availableGuards() {
        if (selectedRouteId == null) return snapshot.entries();
        return snapshot.entries().stream()
                .filter(entry -> entry.namedRouteId().filter(selectedRouteId::equals).isEmpty()).toList();
    }

    private List<GuardListS2CPayload.Entry> assignedGuards() {
        if (selectedRouteId == null) return List.of();
        return snapshot.entries().stream()
                .filter(entry -> entry.namedRouteId().filter(selectedRouteId::equals).isPresent()).toList();
    }

    private Optional<GuardListS2CPayload.Entry> selectedAvailable() {
        return findGuard(availableGuards(), selectedAvailableGuard);
    }

    private Optional<GuardListS2CPayload.Entry> selectedAssigned() {
        return findGuard(assignedGuards(), selectedAssignedGuard);
    }

    private Optional<GuardListS2CPayload.Entry> findGuard(List<GuardListS2CPayload.Entry> guards, UUID id) {
        if (id == null) return Optional.empty();
        return guards.stream().filter(guard -> guard.guardId().equals(id)).findFirst();
    }

    private String routeName(Optional<UUID> routeId) {
        if (routeId.isEmpty()) return Component.translatable("gui.rallyguard.named_route.none").getString();
        return snapshot.namedRoutes().stream().filter(route -> route.id().equals(routeId.get()))
                .map(GuardListS2CPayload.NamedRouteEntry::name)
                .findFirst().orElse(Component.translatable("gui.rallyguard.named_route.missing").getString());
    }

    private String routeDescription(GuardListS2CPayload.Entry guard) {
        if (guard.namedRouteId().isPresent()) return routeName(guard.namedRouteId());
        if (!guard.routePoints().isEmpty()) {
            return Component.translatable(
                    "gui.rallyguard.named_route.individual", guard.routePoints().size()
            ).getString();
        }
        return Component.translatable("gui.rallyguard.named_route.none").getString();
    }

    private boolean selectedRouteInCurrentDimension() {
        if (Minecraft.getInstance().level == null) return false;
        String current = Minecraft.getInstance().level.dimension().identifier().toString();
        return selectedRoute().map(route -> route.dimensionId().equals(current)).orElse(false);
    }

    private void retainGuardSelections() {
        if (selectedAvailable().isEmpty()) selectedAvailableGuard = null;
        if (selectedAssigned().isEmpty()) selectedAssignedGuard = null;
    }

    private void clampPages() {
        routePage = clampPage(routePage, snapshot.namedRoutes().size(), ROUTE_ROWS);
        availablePage = clampPage(availablePage, availableGuards().size(), GUARD_ROWS);
        assignedPage = clampPage(assignedPage, assignedGuards().size(), GUARD_ROWS);
    }

    private int clampPage(int page, int size, int rows) {
        return Math.max(0, Math.min(page, Math.max(0, (size - 1) / rows)));
    }

    private int cyclePage(int page, int delta, int size, int rows) {
        int pages = Math.max(1, (size + rows - 1) / rows);
        return Math.floorMod(page + delta, pages);
    }

    private void changeRoutePage(int delta) {
        routePage = cyclePage(routePage, delta, snapshot.namedRoutes().size(), ROUTE_ROWS);
        rebuildControls();
    }

    private void changeAvailablePage(int delta) {
        availablePage = cyclePage(availablePage, delta, availableGuards().size(), GUARD_ROWS);
        selectedAvailableGuard = null;
        rebuildControls();
    }

    private void changeAssignedPage(int delta) {
        assignedPage = cyclePage(assignedPage, delta, assignedGuards().size(), GUARD_ROWS);
        selectedAssignedGuard = null;
        rebuildControls();
    }

    private void back() {
        parent.acceptSnapshot(snapshot);
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
        int detailX = x + 208;
        int assignedX = x + panelW - 14 - guardColumnWidth();
        graphics.fill(x, y, x + panelW, y + PANEL_HEIGHT, 0xF0101010);
        graphics.outline(x, y, panelW, PANEL_HEIGHT, WHITE);
        graphics.centeredText(font, title, width / 2, y + 10, WHITE);
        graphics.fill(x + 200, y + 26, x + 201, y + PANEL_HEIGHT - 10, 0x44FFFFFF);
        graphics.text(font, Component.translatable("gui.rallyguard.named_route.saved_routes"), x + 14, y + 58, MUTED);
        graphics.text(font, Component.translatable("gui.rallyguard.named_route.available"), detailX, y + 136, WHITE);
        String selectedName = selectedRoute().map(GuardListS2CPayload.NamedRouteEntry::name)
                .orElse(Component.translatable("gui.rallyguard.named_route.select").getString());
        graphics.text(font, Component.translatable("gui.rallyguard.named_route.assigned_guards", selectedName),
                assignedX, y + 136, WHITE);
        selectedRoute().ifPresent(route -> {
            String details = Component.translatable(
                    "gui.rallyguard.named_route.details", draftPoints.size(), draftWaitSeconds, route.dimensionId()
            ).getString();
            graphics.text(font, Component.literal(trim(details, panelW - 234)), detailX, y + 88, MUTED);
        });

        super.extractRenderState(graphics, mouseX, mouseY, delta);
        drawRoutes(graphics);
        drawGuards(graphics, availableGuards(), availablePage, detailX, selectedAvailableGuard);
        drawGuards(graphics, assignedGuards(), assignedPage, assignedX, selectedAssignedGuard);
    }

    private void drawRoutes(GuiGraphicsExtractor graphics) {
        int start = routePage * ROUTE_ROWS;
        int end = Math.min(start + ROUTE_ROWS, snapshot.namedRoutes().size());
        for (int i = start; i < end; i++) {
            GuardListS2CPayload.NamedRouteEntry route = snapshot.namedRoutes().get(i);
            int rowY = panelY() + ROUTE_TOP + (i - start) * ROUTE_ROW_HEIGHT;
            int color = route.id().equals(selectedRouteId) ? GOLD : WHITE;
            graphics.text(font, Component.literal(trim(route.name(), ROUTE_LIST_WIDTH - 12)),
                    panelX() + 20, rowY + 6, color);
            String detail = route.points().size() + " pts · " + route.waitSeconds() + "s";
            graphics.text(font, Component.literal(detail), panelX() + 20, rowY + 18, MUTED);
        }
        if (snapshot.namedRoutes().isEmpty()) {
            graphics.centeredText(font, Component.translatable("gui.rallyguard.named_route.no_routes"),
                    panelX() + 14 + ROUTE_LIST_WIDTH / 2, panelY() + 92, MUTED);
        }
    }

    private void drawGuards(GuiGraphicsExtractor graphics, List<GuardListS2CPayload.Entry> guards, int page,
                            int rowX, UUID selectedGuard) {
        int start = page * GUARD_ROWS;
        int end = Math.min(start + GUARD_ROWS, guards.size());
        for (int i = start; i < end; i++) {
            GuardListS2CPayload.Entry guard = guards.get(i);
            int rowY = panelY() + 154 + (i - start) * GUARD_ROW_HEIGHT;
            int color = guard.guardId().equals(selectedGuard) ? GOLD : WHITE;
            int columnW = guardColumnWidth();
            graphics.text(font, Component.literal(trim(guard.name(), columnW - 12)), rowX + 6, rowY + 5, color);
            String rank = guard.rank().map(Component::getString).orElse("");
            String route = routeDescription(guard);
            String detail = rank.isBlank() ? route : rank + " · " + route;
            graphics.text(font, Component.literal(trim(detail, columnW - 12)), rowX + 6, rowY + 17, MUTED);
        }
    }

    private String trim(String value, int maxWidth) {
        if (font.width(value) <= maxWidth) return value;
        return font.plainSubstrByWidth(value, Math.max(0, maxWidth - font.width("..."))) + "...";
    }

    private int panelX() {
        return (width - panelWidth()) / 2;
    }

    private int panelWidth() {
        return Math.min(MAX_PANEL_WIDTH, Math.max(600, width - 24));
    }

    private int guardColumnWidth() {
        int detailWidth = panelWidth() - 222;
        return Math.max(150, (detailWidth - 52) / 2);
    }

    private int panelY() {
        return (height - PANEL_HEIGHT) / 2;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
