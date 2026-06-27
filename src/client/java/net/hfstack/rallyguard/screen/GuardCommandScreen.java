package net.hfstack.rallyguard.screen;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.hfstack.rallyguard.network.NetworkConstants;
import net.hfstack.rallyguard.network.payload.GuardActionC2SPayload;
import net.hfstack.rallyguard.network.payload.GuardListS2CPayload;
import net.hfstack.rallyguard.order.GuardOrderStatus;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class GuardCommandScreen extends Screen {
    private static final int WHITE = 0xFFFFFFFF;
    private static final int MUTED = 0xFFCCCCCC;
    private static final int EMPTY_TEXT = 0xFFAAAAAA;

    public static record Entry(int entityId, String name, boolean patrolling, int status) {
    }

    private final List<Entry> all = new ArrayList<>();
    private int page = 0;

    private static final int PER_PAGE = 4;
    private static final int MAX_PANEL_W = 540;
    private static final int PANEL_H = 200;
    private static final int SCREEN_PAD = 12;

    private static final int MARGIN_L = 16;
    private static final int MARGIN_R = 16;

    private static final int HEADER_Y = 24;
    private static final int HEADER_LINE_Y = 38;
    private static final int ROW_TOP = 38;
    private static final int ROW_HEIGHT = 28;

    private static final int COL_NAME_X = MARGIN_L;
    private static final int COL_STATUS_X = 120;

    private static final int BTN_SUMMON_W = 54;
    private static final int BTN_FOLLOW_W = 48;
    private static final int BTN_WAIT_W = 58;
    private static final int BTN_PATROL_W = 62;
    private static final int BTN_ROUTE_W = 42;
    private static final int BTN_H = 18;
    private static final int BTN_GAP = 2;

    public GuardCommandScreen(List<Entry> entries) {
        super(Text.translatable("gui.rallyguard.command.title"));
        this.all.addAll(entries);
    }

    public static void openFromPayload(GuardListS2CPayload payload) {
        List<Entry> list = new ArrayList<>(payload.entries().size());
        for (GuardListS2CPayload.Entry e : payload.entries()) {
            list.add(new Entry(e.entityId(), e.name(), e.patrolling(), e.status()));
        }
        MinecraftClient.getInstance().execute(() ->
                MinecraftClient.getInstance().setScreen(new GuardCommandScreen(list))
        );
    }

    @Override
    protected void init() {
        super.init();
        rebuildButtons();
    }

    private void rebuildButtons() {
        this.clearChildren();

        int panelW = panelWidth();
        int x = panelX();
        int y = (this.height - PANEL_H) / 2;

        int start = page * PER_PAGE;
        int end = Math.min(start + PER_PAGE, all.size());

        int groupWidth = actionGroupWidth();
        int actionsRight = x + panelW - MARGIN_R;
        int actionsLeft = actionsRight - groupWidth;

        for (int i = start; i < end; i++) {
            final int idx = i;
            Entry e = all.get(i);

            int rowTop = y + ROW_TOP + (i - start) * ROW_HEIGHT;
            int rowMidY = rowTop + (ROW_HEIGHT / 2);
            int btnY = rowMidY - (BTN_H / 2);

            int btnX = actionsLeft;
            ButtonWidget summon = ButtonWidget.builder(
                    Text.translatable("gui.rallyguard.command.summon"),
                    b -> {
                        sendAction(e.entityId(), NetworkConstants.ACTION_SUMMON);
                        setEntryStatus(idx, GuardOrderStatus.IDLE);
                    }
            ).dimensions(btnX, btnY, BTN_SUMMON_W, BTN_H).build();

            btnX += BTN_SUMMON_W + BTN_GAP;
            boolean following = e.status() == GuardOrderStatus.FOLLOWING;
            Text followLabel = following
                    ? Text.translatable("gui.rallyguard.command.stop")
                    : Text.translatable("gui.rallyguard.command.follow");
            ButtonWidget follow = ButtonWidget.builder(
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
            ).dimensions(btnX, btnY, BTN_FOLLOW_W, BTN_H).build();

            btnX += BTN_FOLLOW_W + BTN_GAP;
            ButtonWidget wait = ButtonWidget.builder(
                    Text.translatable("gui.rallyguard.command.wait"),
                    b -> {
                        sendAction(e.entityId(), NetworkConstants.ACTION_WAIT);
                        setEntryStatus(idx, GuardOrderStatus.WAITING);
                    }
            ).dimensions(btnX, btnY, BTN_WAIT_W, BTN_H).build();

            btnX += BTN_WAIT_W + BTN_GAP;
            Text patrolLabel = e.patrolling()
                    ? Text.translatable("gui.rallyguard.command.stop")
                    : Text.translatable("gui.rallyguard.command.patrol");
            ButtonWidget patrol = ButtonWidget.builder(patrolLabel, b -> {
                sendAction(e.entityId(), NetworkConstants.ACTION_TOGGLE_PATROL);
                Entry curr = all.get(idx);
                int status = curr.patrolling() ? GuardOrderStatus.WAITING : GuardOrderStatus.PATROLLING;
                all.set(idx, new Entry(curr.entityId(), curr.name(), !curr.patrolling(), status));
                rebuildButtons();
            }).dimensions(btnX, btnY, BTN_PATROL_W, BTN_H).build();

            btnX += BTN_PATROL_W + BTN_GAP;
            ButtonWidget route = ButtonWidget.builder(
                    Text.translatable("gui.rallyguard.command.route"),
                    b -> sendAction(e.entityId(), NetworkConstants.ACTION_ROUTE_PLACEHOLDER)
            ).dimensions(btnX, btnY, BTN_ROUTE_W, BTN_H).build();

            this.addDrawableChild(summon);
            this.addDrawableChild(follow);
            this.addDrawableChild(wait);
            this.addDrawableChild(patrol);
            this.addDrawableChild(route);
        }

        ButtonWidget prev = ButtonWidget.builder(Text.literal("<"), b -> {
            if (page > 0) {
                page--;
                rebuildButtons();
            }
        }).dimensions(x + 8, y + PANEL_H - 28, 22, 20).build();

        ButtonWidget next = ButtonWidget.builder(Text.literal(">"), b -> {
            if ((page + 1) * PER_PAGE < all.size()) {
                page++;
                rebuildButtons();
            }
        }).dimensions(x + panelW - 30, y + PANEL_H - 28, 22, 20).build();

        this.addDrawableChild(prev);
        this.addDrawableChild(next);
    }

    private void sendAction(int entityId, int action) {
        ClientPlayNetworking.send(new GuardActionC2SPayload(entityId, action));
    }

    private void setEntryStatus(int idx, int status) {
        Entry curr = all.get(idx);
        all.set(idx, new Entry(curr.entityId(), curr.name(), status == GuardOrderStatus.PATROLLING, status));
        rebuildButtons();
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
        if (this.textRenderer.getWidth(text) <= maxWidth) return text;

        String ellipsis = "...";
        int ellipsisW = this.textRenderer.getWidth(ellipsis);
        if (maxWidth <= ellipsisW) {
            return this.textRenderer.trimToWidth(text, maxWidth);
        }
        return this.textRenderer.trimToWidth(text, maxWidth - ellipsisW) + ellipsis;
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
        int panelW = panelWidth();
        int x = panelX();
        int y = (this.height - PANEL_H) / 2;

        ctx.fill(x, y, x + panelW, y + PANEL_H, 0xF0101010);

        ctx.fill(x, y, x + panelW, y + 1, WHITE);
        ctx.fill(x, y + PANEL_H - 1, x + panelW, y + PANEL_H, WHITE);
        ctx.fill(x, y, x + 1, y + PANEL_H, WHITE);
        ctx.fill(x + panelW - 1, y, x + panelW, y + PANEL_H, WHITE);

        ctx.drawCenteredTextWithShadow(this.textRenderer,
                Text.translatable("gui.rallyguard.command.title"),
                this.width / 2, y + 8, WHITE);

        ctx.drawTextWithShadow(this.textRenderer,
                Text.translatable("gui.rallyguard.command.name"),
                x + COL_NAME_X, y + HEADER_Y, MUTED);

        ctx.drawTextWithShadow(this.textRenderer,
                Text.translatable("gui.rallyguard.command.status"),
                x + COL_STATUS_X, y + HEADER_Y, MUTED);

        int groupWidth = actionGroupWidth();
        int actionsRight = x + panelW - MARGIN_R;
        int actionsLeft = actionsRight - groupWidth;
        int actionsCenterX = actionsLeft + groupWidth / 2;

        Text actions = Text.translatable("gui.rallyguard.command.actions");
        int actionsHeaderW = this.textRenderer.getWidth(actions);
        ctx.drawTextWithShadow(this.textRenderer, actions, actionsCenterX - (actionsHeaderW / 2), y + HEADER_Y, MUTED);

        ctx.fill(x + 6, y + HEADER_LINE_Y, x + panelW - 6, y + HEADER_LINE_Y + 1, 0x33FFFFFF);
    }

    private void drawRows(DrawContext ctx) {
        int panelW = panelWidth();
        int x = panelX();
        int y = (this.height - PANEL_H) / 2;

        if (all.isEmpty()) {
            ctx.drawCenteredTextWithShadow(this.textRenderer,
                    Text.translatable("gui.rallyguard.command.empty"),
                    this.width / 2, y + (PANEL_H / 2), EMPTY_TEXT);
            drawPageIndicator(ctx, x, y);
            return;
        }

        int start = page * PER_PAGE;
        int end = Math.min(start + PER_PAGE, all.size());

        int fontH = this.textRenderer.fontHeight;
        int actionsRight = x + panelW - MARGIN_R;
        int actionsLeft = actionsRight - actionGroupWidth();

        for (int i = start; i < end; i++) {
            Entry e = all.get(i);

            int rowTop = y + ROW_TOP + (i - start) * ROW_HEIGHT;
            int rowMidY = rowTop + (ROW_HEIGHT / 2);
            int rowBot = rowTop + ROW_HEIGHT;
            int textY = rowMidY - (fontH / 2);

            ctx.fill(x + 6, rowBot - 1, x + panelW - 6, rowBot, 0x22FFFFFF);

            int maxNameW = (x + COL_STATUS_X - 12) - (x + COL_NAME_X);
            String name = trimToWidth(e.name(), maxNameW);
            ctx.drawTextWithShadow(this.textRenderer, Text.literal(name), x + COL_NAME_X, textY, WHITE);

            int maxStatusW = (actionsLeft - 12) - (x + COL_STATUS_X);
            String status = trimToWidth(statusText(e.status()).getString(), maxStatusW);
            ctx.drawTextWithShadow(this.textRenderer, Text.literal(status), x + COL_STATUS_X, textY, MUTED);
        }

        drawPageIndicator(ctx, x, y);
    }

    private static Text statusText(int status) {
        return switch (status) {
            case GuardOrderStatus.FOLLOWING -> Text.translatable("gui.rallyguard.command.status.following");
            case GuardOrderStatus.WAITING -> Text.translatable("gui.rallyguard.command.status.waiting");
            case GuardOrderStatus.PATROLLING -> Text.translatable("gui.rallyguard.command.status.patrolling");
            default -> Text.translatable("gui.rallyguard.command.status.idle");
        };
    }

    private void drawPageIndicator(DrawContext ctx, int x, int y) {
        int totalPages = Math.max(1, (all.size() + PER_PAGE - 1) / PER_PAGE);
        String pg = (page + 1) + " / " + totalPages;
        int w = this.textRenderer.getWidth(pg);
        ctx.drawTextWithShadow(this.textRenderer, Text.literal(pg),
                x + (panelWidth() - w) / 2, y + PANEL_H - 24, WHITE);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
