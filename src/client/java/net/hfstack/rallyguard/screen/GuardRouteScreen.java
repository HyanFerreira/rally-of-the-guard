package net.hfstack.rallyguard.screen;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.hfstack.rallyguard.config.RallyConfig;
import net.hfstack.rallyguard.network.NetworkConstants;
import net.hfstack.rallyguard.network.payload.GuardListS2CPayload;
import net.hfstack.rallyguard.network.payload.GuardRouteUpdateC2SPayload;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class GuardRouteScreen extends Screen {
    private static final int WHITE = 0xFFFFFFFF;
    private static final int MUTED = 0xFFCCCCCC;
    private static final int WARNING = 0xFFFFCC66;

    private static final int MAX_PANEL_W = 520;
    private static final int MIN_PANEL_H = 236;
    private static final int SCREEN_PAD = 12;
    private static final int ROW_TOP = 52;
    private static final int ROW_H = 24;
    private static final int BTN_H = 18;

    private final GuardCommandScreen parent;
    private final int entryIndex;
    private final GuardCommandScreen.Entry guard;
    private final List<GuardListS2CPayload.Point> points = new ArrayList<>();
    private boolean active;
    private int waitSeconds;

    public GuardRouteScreen(GuardCommandScreen parent, int entryIndex, GuardCommandScreen.Entry guard) {
        super(Text.translatable("gui.rallyguard.route.title"));
        this.parent = parent;
        this.entryIndex = entryIndex;
        this.guard = guard;
        this.active = guard.routeActive();
        this.waitSeconds = guard.routeWaitSeconds() > 0 ? guard.routeWaitSeconds() : RallyConfig.routeDefaultWaitSeconds();
        this.points.addAll(guard.routePoints());
    }

    @Override
    protected void init() {
        super.init();
        rebuildButtons();
    }

    private void rebuildButtons() {
        this.clearChildren();

        int x = panelX();
        int y = panelY();
        int panelW = panelWidth();

        for (int i = 0; i < points.size(); i++) {
            final int idx = i;
            int rowY = y + ROW_TOP + i * ROW_H;
            int useX = x + panelW - 156;
            int removeX = x + panelW - 72;

            this.addDrawableChild(ButtonWidget.builder(
                    Text.translatable("gui.rallyguard.route.use_current"),
                    b -> {
                        setPoint(idx, currentPoint());
                        saveOnly();
                    }
            ).dimensions(useX, rowY + 3, 78, BTN_H).build());

            this.addDrawableChild(ButtonWidget.builder(
                    Text.translatable("gui.rallyguard.route.remove"),
                    b -> {
                        points.remove(idx);
                        if (active && points.size() < 2) active = false;
                        sendRoute(active ? NetworkConstants.ROUTE_START : NetworkConstants.ROUTE_SAVE);
                        rebuildButtons();
                    }
            ).dimensions(removeX, rowY + 3, 58, BTN_H).build());
        }

        int controlsY = y + panelHeight() - 54;
        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("-5s"),
                b -> {
                    waitSeconds = Math.max(0, waitSeconds - 5);
                    saveOnly();
                    rebuildButtons();
                }
        ).dimensions(x + 16, controlsY, 42, BTN_H).build());

        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("+5s"),
                b -> {
                    waitSeconds = Math.min(600, waitSeconds + 5);
                    saveOnly();
                    rebuildButtons();
                }
        ).dimensions(x + 64, controlsY, 42, BTN_H).build());

        this.addDrawableChild(ButtonWidget.builder(
                Text.translatable("gui.rallyguard.route.add_current"),
                b -> {
            if (points.size() < RallyConfig.routeMaxPoints()) {
                points.add(currentPoint());
                saveOnly();
                rebuildButtons();
            }
                }
        ).dimensions(x + 118, controlsY, 104, BTN_H).build());

        int bottomY = y + panelHeight() - 28;
        this.addDrawableChild(ButtonWidget.builder(
                active ? Text.translatable("gui.rallyguard.route.pause") : Text.translatable("gui.rallyguard.route.start"),
                b -> {
                    if (!active && points.size() < 2) {
                        sendRoute(NetworkConstants.ROUTE_SAVE);
                        return;
                    }
                    active = !active;
                    sendRoute(active ? NetworkConstants.ROUTE_START : NetworkConstants.ROUTE_PAUSE);
                    rebuildButtons();
                }
        ).dimensions(x + 16, bottomY, 78, BTN_H).build());

        this.addDrawableChild(ButtonWidget.builder(
                Text.translatable("gui.rallyguard.route.clear"),
                b -> {
                    points.clear();
                    active = false;
                    sendRoute(NetworkConstants.ROUTE_CLEAR);
                    rebuildButtons();
                }
        ).dimensions(x + 102, bottomY, 58, BTN_H).build());

        this.addDrawableChild(ButtonWidget.builder(
                Text.translatable("gui.rallyguard.route.back"),
                b -> closeToParent()
        ).dimensions(x + panelW - 74, bottomY, 58, BTN_H).build());
    }

    private void setPoint(int idx, GuardListS2CPayload.Point point) {
        points.set(idx, point);
        rebuildButtons();
    }

    private GuardListS2CPayload.Point currentPoint() {
        MinecraftClient client = MinecraftClient.getInstance();
        BlockPos pos = client.player != null ? client.player.getBlockPos() : BlockPos.ORIGIN;
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
                .map(p -> new GuardRouteUpdateC2SPayload.Point(p.x(), p.y(), p.z()))
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
        MinecraftClient.getInstance().setScreen(parent);
    }

    @Override
    public void close() {
        closeToParent();
    }

    @Override
    public void renderBackground(DrawContext ctx, int mouseX, int mouseY, float delta) {
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        drawPanel(ctx);
        drawRows(ctx);
        super.render(ctx, mouseX, mouseY, delta);
    }

    private void drawPanel(DrawContext ctx) {
        int x = panelX();
        int y = panelY();
        int panelW = panelWidth();

        int panelH = panelHeight();

        ctx.fill(x, y, x + panelW, y + panelH, 0xF0101010);
        ctx.fill(x, y, x + panelW, y + 1, WHITE);
        ctx.fill(x, y + panelH - 1, x + panelW, y + panelH, WHITE);
        ctx.fill(x, y, x + 1, y + panelH, WHITE);
        ctx.fill(x + panelW - 1, y, x + panelW, y + panelH, WHITE);

        ctx.drawCenteredTextWithShadow(this.textRenderer,
                Text.translatable("gui.rallyguard.route.title_named", guard.name()),
                this.width / 2, y + 8, WHITE);

        ctx.drawTextWithShadow(this.textRenderer,
                Text.translatable("gui.rallyguard.route.points"),
                x + 16, y + 30, MUTED);

        String wait = Text.translatable("gui.rallyguard.route.wait", waitSeconds).getString();
        int waitW = this.textRenderer.getWidth(wait);
        ctx.drawTextWithShadow(this.textRenderer, Text.literal(wait), x + panelW - 16 - waitW, y + 30, MUTED);

        ctx.fill(x + 6, y + 44, x + panelW - 6, y + 45, 0x33FFFFFF);
    }

    private void drawRows(DrawContext ctx) {
        int x = panelX();
        int y = panelY();
        int panelW = panelWidth();

        if (points.isEmpty()) {
            ctx.drawCenteredTextWithShadow(this.textRenderer,
                    Text.translatable("gui.rallyguard.route.empty"),
                    this.width / 2, y + 96, MUTED);
        }

        for (int i = 0; i < points.size(); i++) {
            GuardListS2CPayload.Point point = points.get(i);
            int rowY = y + ROW_TOP + i * ROW_H;
            ctx.fill(x + 6, rowY + ROW_H - 1, x + panelW - 6, rowY + ROW_H, 0x22FFFFFF);
            String label = (i + 1) + ".  X " + point.x() + "   Y " + point.y() + "   Z " + point.z();
            int maxLabelW = panelW - 190;
            if (this.textRenderer.getWidth(label) > maxLabelW) {
                label = this.textRenderer.trimToWidth(label, maxLabelW - this.textRenderer.getWidth("...")) + "...";
            }
            ctx.drawTextWithShadow(this.textRenderer, Text.literal(label), x + 16, rowY + 8, WHITE);
        }

        if (points.size() < 2) {
            ctx.drawTextWithShadow(this.textRenderer,
                    Text.translatable("gui.rallyguard.route.need_points"),
                    x + 230, y + panelHeight() - 50, WARNING);
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

    @Override
    public boolean shouldPause() {
        return false;
    }
}
