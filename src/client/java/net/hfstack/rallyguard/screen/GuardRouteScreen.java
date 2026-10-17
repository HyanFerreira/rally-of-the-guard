package net.hfstack.rallyguard.screen;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.hfstack.rallyguard.config.RallyConfig;
import net.hfstack.rallyguard.network.NetworkConstants;
import net.hfstack.rallyguard.network.payload.GuardListS2CPayload;
import net.hfstack.rallyguard.network.payload.GuardRouteUpdateC2SPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class GuardRouteScreen extends Screen {
    private static final int WHITE = 0xFFFFFFFF;
    private static final int MUTED = 0xFFCCCCCC;
    private static final int WARNING = 0xFFFFCC66;

    private static final int MAX_PANEL_W = 520;
    private static final int MIN_PANEL_H = 236;
    private static final int SCREEN_PAD = 12;
    private static final int ROW_TOP = 64;
    private static final int ROW_H = 24;
    private static final int BTN_H = 18;
    private static final int EDIT_BUTTON_W = 20;
    private static final Component EDIT_ICON = Component.literal("\u270e");
    private static final Component SAVE_ICON = Component.literal("\u2713");

    private final GuardCommandScreen parent;
    private final int entryIndex;
    private final GuardCommandScreen.Entry guard;
    private final java.util.Optional<String> assignedRouteName;
    private final List<GuardListS2CPayload.Point> points = new ArrayList<>();
    private final List<EditBox> pointNameBoxes = new ArrayList<>();
    private boolean active;
    private int waitSeconds;
    private int pointPage;

    public GuardRouteScreen(GuardCommandScreen parent, int entryIndex, GuardCommandScreen.Entry guard,
                            java.util.Optional<String> assignedRouteName) {
        super(Component.translatable("gui.rallyguard.route.title"));
        this.parent = parent;
        this.entryIndex = entryIndex;
        this.guard = guard;
        this.assignedRouteName = assignedRouteName == null ? java.util.Optional.empty() : assignedRouteName;
        this.active = guard.routeActive();
        this.waitSeconds = guard.routeWaitSeconds() > 0 ? guard.routeWaitSeconds() : RallyConfig.routeDefaultWaitSeconds();
        for (int i = 0; i < guard.routePoints().size(); i++) {
            GuardListS2CPayload.Point point = guard.routePoints().get(i);
            this.points.add(new GuardListS2CPayload.Point(
                    point.x(), point.y(), point.z(), RoutePointNames.displayName(point.label(), i)
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
        pointNameBoxes.clear();

        int x = panelX();
        int y = panelY();
        int panelW = panelWidth();
        pointPage = Math.max(0, Math.min(pointPage, Math.max(0, (points.size() - 1) / rowsPerPage())));

        int start = pointPage * rowsPerPage();
        int end = Math.min(start + rowsPerPage(), points.size());
        for (int i = start; i < end; i++) {
            final int idx = i;
            int rowY = y + ROW_TOP + (i - start) * ROW_H;
            int useX = x + panelW - 156;
            int removeX = x + panelW - 72;
            int nameW = Math.max(72, panelW - 374);
            EditBox pointName = new EditBox(font, x + 16, rowY + 3, nameW, BTN_H,
                    Component.translatable("gui.rallyguard.named_route.point_name", i + 1));
            pointName.setMaxLength(32);
            pointName.setValue(RoutePointNames.displayName(points.get(i).label(), i));
            pointName.setEditable(false);
            pointNameBoxes.add(pointName);
            addRenderableWidget(pointName);

            boolean[] editing = {false};
            this.addRenderableWidget(Button.builder(EDIT_ICON, button -> {
                if (!editing[0]) {
                    editing[0] = true;
                    pointName.setEditable(true);
                    setInitialFocus(pointName);
                    button.setMessage(SAVE_ICON);
                    return;
                }
                pointName.setValue(RoutePointNames.displayName(pointName.getValue(), idx));
                capturePointNames();
                editing[0] = false;
                pointName.setEditable(false);
                button.setMessage(EDIT_ICON);
                saveOnly();
            }).bounds(x + 16 + nameW + GuiLayout.CONTROL_GAP, rowY + 2, EDIT_BUTTON_W, GuiLayout.BUTTON_HEIGHT)
                    .build());

            this.addRenderableWidget(Button.builder(
                    Component.translatable("gui.rallyguard.route.use_current"),
                    b -> {
                        capturePointNames();
                        setPoint(idx, currentPoint());
                        saveOnly();
                    }
            ).bounds(useX, rowY + 3, 78, BTN_H).build());

            this.addRenderableWidget(Button.builder(
                    Component.translatable("gui.rallyguard.route.remove"),
                    b -> {
                        capturePointNames();
                        points.remove(idx);
                        if (active && points.size() < 2) active = false;
                        sendRoute(active ? NetworkConstants.ROUTE_START : NetworkConstants.ROUTE_SAVE);
                        rebuildButtons();
                    }
            ).bounds(removeX, rowY + 3, 58, BTN_H).build());
        }

        int controlsY = y + panelHeight() - 54;
        this.addRenderableWidget(Button.builder(
                Component.literal("-5s"),
                b -> {
                    capturePointNames();
                    waitSeconds = Math.max(0, waitSeconds - 5);
                    saveOnly();
                    rebuildButtons();
                }
        ).bounds(x + 16, controlsY, 42, BTN_H).build());

        this.addRenderableWidget(Button.builder(
                Component.literal("+5s"),
                b -> {
                    capturePointNames();
                    waitSeconds = Math.min(600, waitSeconds + 5);
                    saveOnly();
                    rebuildButtons();
                }
        ).bounds(x + 62, controlsY, 42, BTN_H).build());

        this.addRenderableWidget(Button.builder(
                Component.translatable("gui.rallyguard.route.add_current"),
                b -> {
            capturePointNames();
            if (points.size() < RallyConfig.routeMaxPoints()) {
                GuardListS2CPayload.Point current = currentPoint();
                points.add(new GuardListS2CPayload.Point(
                        current.x(), current.y(), current.z(), RoutePointNames.initialName(points.size())
                ));
                pointPage = (points.size() - 1) / rowsPerPage();
                saveOnly();
                rebuildButtons();
            }
                }
        ).bounds(x + 108, controlsY, 104, BTN_H).build());

        Button previous = Button.builder(Component.literal("<"), b -> {
            capturePointNames();
            pointPage--;
            rebuildButtons();
        }).bounds(x + 216, controlsY, 24, BTN_H).build();
        previous.active = pointPage > 0;
        addRenderableWidget(previous);
        Button next = Button.builder(Component.literal(">"), b -> {
            capturePointNames();
            pointPage++;
            rebuildButtons();
        }).bounds(x + 244, controlsY, 24, BTN_H).build();
        next.active = (pointPage + 1) * rowsPerPage() < points.size();
        addRenderableWidget(next);

        int bottomY = y + panelHeight() - 28;
        this.addRenderableWidget(Button.builder(
                active ? Component.translatable("gui.rallyguard.route.pause") : Component.translatable("gui.rallyguard.route.start"),
                b -> {
                    capturePointNames();
                    if (!active && points.size() < 2) {
                        sendRoute(NetworkConstants.ROUTE_SAVE);
                        return;
                    }
                    active = !active;
                    sendRoute(active ? NetworkConstants.ROUTE_START : NetworkConstants.ROUTE_PAUSE);
                    rebuildButtons();
                }
        ).bounds(x + 16, bottomY, 78, BTN_H).build());

        this.addRenderableWidget(Button.builder(
                Component.translatable("gui.rallyguard.route.clear"),
                b -> {
                    capturePointNames();
                    points.clear();
                    active = false;
                    sendRoute(NetworkConstants.ROUTE_CLEAR);
                    rebuildButtons();
                }
        ).bounds(x + 98, bottomY, 58, BTN_H).build());

        this.addRenderableWidget(Button.builder(
                Component.translatable("gui.rallyguard.route.back"),
                b -> closeToParent()
        ).bounds(x + panelW - 74, bottomY, 58, BTN_H).build());
    }

    private void setPoint(int idx, GuardListS2CPayload.Point point) {
        GuardListS2CPayload.Point previous = points.get(idx);
        points.set(idx, new GuardListS2CPayload.Point(point.x(), point.y(), point.z(), previous.label()));
        rebuildButtons();
    }

    private void capturePointNames() {
        int start = pointPage * rowsPerPage();
        for (int visible = 0; visible < pointNameBoxes.size(); visible++) {
            int index = start + visible;
            if (index >= points.size()) break;
            GuardListS2CPayload.Point point = points.get(index);
            points.set(index, new GuardListS2CPayload.Point(
                    point.x(), point.y(), point.z(),
                    RoutePointNames.displayName(pointNameBoxes.get(visible).getValue(), index)
            ));
        }
    }

    private GuardListS2CPayload.Point currentPoint() {
        Minecraft client = Minecraft.getInstance();
        BlockPos pos = client.player != null ? client.player.blockPosition() : BlockPos.ZERO;
        return new GuardListS2CPayload.Point(pos.getX(), pos.getY(), pos.getZ());
    }

    private void saveOnly() {
        if (active && points.size() >= 2) {
            sendRoute(NetworkConstants.ROUTE_START);
            return;
        }

        if (points.size() < 2) {
            active = false;
        }
        sendRoute(NetworkConstants.ROUTE_SAVE);
    }

    private void sendRoute(int action) {
        List<GuardRouteUpdateC2SPayload.Point> payloadPoints = points.stream()
                .map(p -> new GuardRouteUpdateC2SPayload.Point(p.x(), p.y(), p.z(), p.label()))
                .toList();
        ClientPlayNetworking.send(new GuardRouteUpdateC2SPayload(guard.entityId(), action, waitSeconds, payloadPoints));

        if (action == NetworkConstants.ROUTE_SAVE) {
            parent.updateRouteDraft(entryIndex, waitSeconds, List.copyOf(points));
            return;
        }

        if (action == NetworkConstants.ROUTE_CLEAR) {
            parent.updateRouteEntry(entryIndex, false, waitSeconds, List.of());
        } else {
            parent.updateRouteEntry(entryIndex, active && points.size() >= 2, waitSeconds, List.copyOf(points));
        }
    }

    private void closeToParent() {
        Minecraft.getInstance().setScreen(parent);
    }

    @Override
    public void onClose() {
        closeToParent();
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
        int x = panelX();
        int y = panelY();
        int panelW = panelWidth();

        int panelH = panelHeight();

        ctx.fill(x, y, x + panelW, y + panelH, 0xF0101010);
        ctx.fill(x, y, x + panelW, y + 1, WHITE);
        ctx.fill(x, y + panelH - 1, x + panelW, y + panelH, WHITE);
        ctx.fill(x, y, x + 1, y + panelH, WHITE);
        ctx.fill(x + panelW - 1, y, x + panelW, y + panelH, WHITE);

        ctx.centeredText(this.font,
                Component.translatable("gui.rallyguard.route.title_named", guard.name()),
                this.width / 2, y + 8, WHITE);

        ctx.text(this.font,
                Component.translatable("gui.rallyguard.route.points"),
                x + 16, y + 30, MUTED);

        String wait = Component.translatable("gui.rallyguard.route.wait", waitSeconds).getString();
        int waitW = this.font.width(wait);
        ctx.text(this.font, Component.literal(wait), x + panelW - 16 - waitW, y + 30, MUTED);

        assignedRouteName.ifPresent(name -> ctx.text(this.font,
                Component.translatable("gui.rallyguard.named_route.individual_edit_warning", name),
                x + 16, y + 44, WARNING));

        ctx.fill(x + 6, y + 56, x + panelW - 6, y + 57, 0x33FFFFFF);
    }

    private void drawRows(GuiGraphicsExtractor ctx) {
        int x = panelX();
        int y = panelY();
        int panelW = panelWidth();

        if (points.isEmpty()) {
            ctx.centeredText(this.font,
                    Component.translatable("gui.rallyguard.route.empty"),
                    this.width / 2, y + 96, MUTED);
        }

        int start = pointPage * rowsPerPage();
        int end = Math.min(start + rowsPerPage(), points.size());
        for (int i = start; i < end; i++) {
            GuardListS2CPayload.Point point = points.get(i);
            int rowY = y + ROW_TOP + (i - start) * ROW_H;
            ctx.fill(x + 6, rowY + ROW_H - 1, x + panelW - 6, rowY + ROW_H, 0x22FFFFFF);
            String label = "#" + (i + 1) + "  X " + point.x() + "  Y " + point.y() + "  Z " + point.z();
            int nameW = Math.max(72, panelW - 374);
            int coordinateX = x + 16 + nameW + GuiLayout.CONTROL_GAP + EDIT_BUTTON_W + GuiLayout.CONTROL_GAP;
            int maxLabelW = Math.max(50, x + panelW - 162 - coordinateX);
            if (this.font.width(label) > maxLabelW) {
                label = this.font.plainSubstrByWidth(label, maxLabelW - this.font.width("...")) + "...";
            }
            ctx.text(this.font, Component.literal(label), coordinateX, rowY + 8, WHITE);
        }

        if (points.size() < 2) {
            ctx.centeredText(this.font,
                    Component.translatable("gui.rallyguard.route.need_points"),
                    this.width / 2, y + panelHeight() - 72, WARNING);
        }
    }

    private int panelWidth() {
        return Math.min(MAX_PANEL_W, Math.max(320, this.width - SCREEN_PAD * 2));
    }

    private int panelX() {
        return (this.width - panelWidth()) / 2;
    }

    private int panelY() {
        return (this.height - panelHeight()) / 2;
    }

    private int panelHeight() {
        int rowsHeight = ROW_TOP + RallyConfig.routeMaxPoints() * ROW_H + 84;
        return Math.min(this.height - SCREEN_PAD * 2, Math.max(MIN_PANEL_H, rowsHeight));
    }

    private int rowsPerPage() {
        return Math.max(1, Math.min(RallyConfig.routeMaxPoints(),
                (panelHeight() - ROW_TOP - 76) / ROW_H));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
