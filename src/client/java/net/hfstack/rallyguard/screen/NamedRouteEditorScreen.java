package net.hfstack.rallyguard.screen;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.hfstack.rallyguard.config.RallyConfig;
import net.hfstack.rallyguard.network.NetworkConstants;
import net.hfstack.rallyguard.network.payload.GuardListS2CPayload;
import net.hfstack.rallyguard.network.payload.NamedRouteMutationC2SPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class NamedRouteEditorScreen extends Screen {
    private static final int WHITE = 0xFFFFFFFF;
    private static final int MUTED = 0xFFAAAAAA;
    private static final int PANEL_HEIGHT = 330;
    private static final int MAX_PANEL_WIDTH = 680;
    private static final int ROW_TOP = 76;
    private static final int ROW_HEIGHT = 30;
    private static final int EDIT_BUTTON_WIDTH = 20;

    private final NamedRouteManagementScreen parent;
    private final UUID routeId;
    private GuardListS2CPayload snapshot;
    private String routeName;
    private int waitSeconds;
    private final List<GuardListS2CPayload.Point> points = new ArrayList<>();
    private final List<EditBox> labelBoxes = new ArrayList<>();
    private EditBox nameBox;
    private int page;

    NamedRouteEditorScreen(NamedRouteManagementScreen parent, GuardListS2CPayload snapshot,
                           GuardListS2CPayload.NamedRouteEntry route) {
        super(Component.translatable("gui.rallyguard.named_route.editor_title", route.name()));
        this.parent = parent;
        this.snapshot = snapshot;
        this.routeId = route.id();
        load(route);
    }

    void acceptSnapshot(GuardListS2CPayload updated) {
        snapshot = updated;
        updated.namedRoutes().stream().filter(route -> route.id().equals(routeId)).findFirst()
                .ifPresent(this::load);
        parent.acceptSnapshot(updated);
        rebuildControls();
    }

    @Override
    protected void init() {
        super.init();
        rebuildControls();
    }

    private void rebuildControls() {
        clearWidgets();
        labelBoxes.clear();
        page = Math.max(0, Math.min(page, Math.max(0, (points.size() - 1) / rowsPerPage())));
        int x = panelX();
        int y = panelY();
        int panelW = panelWidth();

        nameBox = new EditBox(font, x + 14, y + 32, Math.max(130, panelW - 190), 20,
                Component.translatable("gui.rallyguard.named_route.name"));
        nameBox.setMaxLength(48);
        nameBox.setValue(routeName);
        addRenderableWidget(nameBox);

        addRenderableWidget(Button.builder(Component.literal("-5s"), button -> changeWait(-5))
                .bounds(x + panelW - 166, y + 32, 48, 20).build());
        addRenderableWidget(Button.builder(Component.literal("+5s"), button -> changeWait(5))
                .bounds(x + panelW - 114, y + 32, 48, 20).build());

        int start = page * rowsPerPage();
        int end = Math.min(start + rowsPerPage(), points.size());
        for (int i = start; i < end; i++) addPointRow(i, i - start);

        int bottom = y + panelHeight() - 30;
        int controlsY = bottom - 30;
        Button previous = Button.builder(Component.literal("<"), button -> changePage(-1))
                .bounds(x + 14, controlsY, 24, 20).build();
        previous.active = page > 0;
        addRenderableWidget(previous);
        Button next = Button.builder(Component.literal(">"), button -> changePage(1))
                .bounds(x + 42, controlsY, 24, 20).build();
        next.active = (page + 1) * rowsPerPage() < points.size();
        addRenderableWidget(next);

        Button add = Button.builder(Component.translatable("gui.rallyguard.route.add_current"),
                        button -> addCurrentPoint())
                .bounds(x + 70, controlsY, 108, 20).build();
        add.active = points.size() < RallyConfig.routeMaxPoints();
        addRenderableWidget(add);

        Button save = Button.builder(Component.translatable("gui.rallyguard.named_route.save_changes"),
                        button -> save())
                .bounds(x + 14, bottom, 112, 20).build();
        save.active = points.size() >= 2;
        addRenderableWidget(save);
        addRenderableWidget(Button.builder(Component.translatable("gui.rallyguard.route.back"), button -> back())
                .bounds(x + panelW - 84, bottom, 70, 20).build());
    }

    private void addPointRow(int pointIndex, int visibleIndex) {
        GuardListS2CPayload.Point point = points.get(pointIndex);
        int x = panelX();
        int y = panelY() + ROW_TOP + visibleIndex * ROW_HEIGHT;
        int panelW = panelWidth();
        int actionsWidth = 188;
        int coordinateWidth = 132;
        int labelWidth = Math.max(64, panelW - 28 - coordinateWidth - actionsWidth
                - EDIT_BUTTON_WIDTH - GuiLayout.CONTROL_GAP);

        EditBox label = new EditBox(font, x + 14, y + 4, labelWidth, 20,
                Component.translatable("gui.rallyguard.named_route.point_name", pointIndex + 1));
        label.setMaxLength(32);
        label.setValue(RoutePointNames.displayName(point.label(), pointIndex));
        label.setEditable(false);
        label.active = false;
        labelBoxes.add(label);
        addRenderableWidget(label);

        addRenderableWidget(new EditConfirmIconButton(
                x + 14 + labelWidth + GuiLayout.CONTROL_GAP, y + 4, EDIT_BUTTON_WIDTH, button -> {
            if (!button.isConfirming()) {
                label.setEditable(true);
                label.active = true;
                setInitialFocus(label);
                button.setConfirming(true);
                return;
            }
            label.setValue(RoutePointNames.displayName(label.getValue(), pointIndex));
            captureDraft();
            label.setEditable(false);
            label.active = false;
            label.setFocused(false);
            button.setConfirming(false);
        }));

        int actionsX = x + panelW - 14 - actionsWidth;
        Button up = Button.builder(Component.literal("↑"), button -> movePoint(pointIndex, -1))
                .bounds(actionsX, y + 4, 22, 20).build();
        up.active = pointIndex > 0;
        addRenderableWidget(up);
        Button down = Button.builder(Component.literal("↓"), button -> movePoint(pointIndex, 1))
                .bounds(actionsX + 26, y + 4, 22, 20).build();
        down.active = pointIndex + 1 < points.size();
        addRenderableWidget(down);
        addRenderableWidget(Button.builder(Component.translatable("gui.rallyguard.route.use_current"),
                        button -> replaceWithCurrent(pointIndex))
                .bounds(actionsX + 52, y + 4, 72, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.rallyguard.route.remove"),
                        button -> removePoint(pointIndex))
                .bounds(actionsX + 128, y + 4, 60, 20).build());
    }

    private void captureDraft() {
        if (nameBox != null) routeName = nameBox.getValue();
        int start = page * rowsPerPage();
        for (int visible = 0; visible < labelBoxes.size(); visible++) {
            int index = start + visible;
            if (index >= points.size()) break;
            GuardListS2CPayload.Point point = points.get(index);
            points.set(index, new GuardListS2CPayload.Point(
                    point.x(), point.y(), point.z(), labelBoxes.get(visible).getValue()
            ));
        }
    }

    private void addCurrentPoint() {
        captureDraft();
        BlockPos pos = currentPosition();
        points.add(new GuardListS2CPayload.Point(
                pos.getX(), pos.getY(), pos.getZ(), RoutePointNames.initialName(points.size())
        ));
        page = (points.size() - 1) / rowsPerPage();
        rebuildControls();
    }

    private void replaceWithCurrent(int index) {
        captureDraft();
        BlockPos pos = currentPosition();
        GuardListS2CPayload.Point previous = points.get(index);
        points.set(index, new GuardListS2CPayload.Point(
                pos.getX(), pos.getY(), pos.getZ(), previous.label()
        ));
        rebuildControls();
    }

    private void removePoint(int index) {
        captureDraft();
        points.remove(index);
        rebuildControls();
    }

    private void movePoint(int index, int delta) {
        captureDraft();
        int destination = index + delta;
        if (destination < 0 || destination >= points.size()) return;
        java.util.Collections.swap(points, index, destination);
        page = destination / rowsPerPage();
        rebuildControls();
    }

    private void changeWait(int delta) {
        captureDraft();
        waitSeconds = Math.max(0, Math.min(600, waitSeconds + delta));
        rebuildControls();
    }

    private void changePage(int delta) {
        captureDraft();
        page += delta;
        rebuildControls();
    }

    private void save() {
        captureDraft();
        if (points.size() < 2) return;
        List<NamedRouteMutationC2SPayload.Point> payloadPoints = points.stream()
                .map(point -> new NamedRouteMutationC2SPayload.Point(
                        point.x(), point.y(), point.z(), point.label()
                )).toList();
        ClientPlayNetworking.send(new NamedRouteMutationC2SPayload(
                NetworkConstants.NAMED_ROUTE_UPDATE, Optional.of(routeId), routeName, waitSeconds, payloadPoints,
                NamedRouteMutationC2SPayload.NO_SOURCE_GUARD
        ));
    }

    private void load(GuardListS2CPayload.NamedRouteEntry route) {
        routeName = route.name();
        waitSeconds = route.waitSeconds();
        points.clear();
        for (int i = 0; i < route.points().size(); i++) {
            GuardListS2CPayload.Point point = route.points().get(i);
            points.add(new GuardListS2CPayload.Point(
                    point.x(), point.y(), point.z(), RoutePointNames.displayName(point.label(), i)
            ));
        }
    }

    private BlockPos currentPosition() {
        return Minecraft.getInstance().player == null
                ? BlockPos.ZERO : Minecraft.getInstance().player.blockPosition();
    }

    private void back() {
        captureDraft();
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
        graphics.fill(x, y, x + panelW, y + panelHeight(), 0xF0101010);
        graphics.outline(x, y, panelW, panelHeight(), WHITE);
        graphics.centeredText(font, title, width / 2, y + 10, WHITE);
        graphics.text(font, Component.translatable("gui.rallyguard.named_route.point_names_hint"),
                x + 14, y + 58, MUTED);
        graphics.text(font, Component.translatable("gui.rallyguard.route.wait", waitSeconds),
                x + panelW - 62, y + 58, MUTED);

        int start = page * rowsPerPage();
        int end = Math.min(start + rowsPerPage(), points.size());
        for (int i = start; i < end; i++) {
            GuardListS2CPayload.Point point = points.get(i);
            int rowY = y + ROW_TOP + (i - start) * ROW_HEIGHT;
            int labelWidth = Math.max(64, panelW - 28 - 132 - 188
                    - EDIT_BUTTON_WIDTH - GuiLayout.CONTROL_GAP);
            int coordinateX = x + 14 + labelWidth + GuiLayout.CONTROL_GAP
                    + EDIT_BUTTON_WIDTH + GuiLayout.CONTROL_GAP;
            graphics.text(font, Component.literal(
                    "#" + (i + 1) + "  " + point.x() + ", " + point.y() + ", " + point.z()
            ), coordinateX, rowY + 10, MUTED);
            graphics.fill(x + 10, rowY + ROW_HEIGHT - 1, x + panelW - 10, rowY + ROW_HEIGHT, 0x22FFFFFF);
        }
        if (points.isEmpty()) {
            graphics.centeredText(font, Component.translatable("gui.rallyguard.route.empty"),
                    width / 2, y + 130, MUTED);
        }
        if (points.size() < 2) {
            graphics.text(font, Component.translatable("gui.rallyguard.route.need_points"),
                    x + 132, y + panelHeight() - 26, 0xFFFFCC66);
        }
        super.extractRenderState(graphics, mouseX, mouseY, delta);
    }

    private int panelWidth() {
        return Math.min(MAX_PANEL_WIDTH, Math.max(1, width - 24));
    }

    private int panelX() {
        return (width - panelWidth()) / 2;
    }

    private int panelY() {
        return (height - panelHeight()) / 2;
    }

    private int panelHeight() {
        return Math.min(PANEL_HEIGHT, Math.max(1, height - 24));
    }

    private int rowsPerPage() {
        return Math.max(1, Math.min(6, (panelHeight() - ROW_TOP - 68) / ROW_HEIGHT));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
