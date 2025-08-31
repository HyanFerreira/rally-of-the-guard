package net.hfstack.rallyguard.screen;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.hfstack.rallyguard.network.NetworkConstants;
import net.hfstack.rallyguard.network.payload.GuardActionC2SPayload;
import net.hfstack.rallyguard.network.payload.GuardListS2CPayload;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class GuardCommandScreen extends Screen {

    public static record Entry(int entityId, String name, boolean patrolling) {
    }

    private final List<Entry> all = new ArrayList<>();
    private int page = 0;

    // ===== Layout =====
    private static final int PER_PAGE = 4;        // 4 guardas por página
    private static final int PANEL_W = 440;      // janela mais estreita
    private static final int PANEL_H = 200;

    private static final int MARGIN_L = 16;
    private static final int MARGIN_R = 16;

    private static final int HEADER_Y = 24;
    private static final int HEADER_LINE_Y = 38;

    // primeira linha mais próxima do cabeçalho
    private static final int ROW_TOP = 38; // topo do bloco de linhas
    private static final int ROW_HEIGHT = 28; // altura de cada “div” de linha

    // Colunas (relativas ao painel)
    private static final int COL_NAME_X = MARGIN_L;

    // Botões (constantes compartilhadas entre header/linhas)
    private static final int BTN_W1 = 80;   // “Teletransportar”
    private static final int BTN_W2 = 80;   // “Patrulhar aqui / Parar patrulha”
    private static final int BTN_H = 18;
    private static final int BTN_GAP = 2;

    public GuardCommandScreen(List<Entry> entries) {
        super(Text.translatable("gui.rallyguard.command.title"));
        this.all.addAll(entries);
    }

    public static void openFromPayload(GuardListS2CPayload payload) {
        List<Entry> list = new ArrayList<>(payload.entries().size());
        for (GuardListS2CPayload.Entry e : payload.entries()) {
            list.add(new Entry(e.entityId(), e.name(), e.patrolling()));
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

        int x = (this.width - PANEL_W) / 2;
        int y = (this.height - PANEL_H) / 2;

        int start = page * PER_PAGE;
        int end = Math.min(start + PER_PAGE, all.size());

        // Grupo de botões: ancorado na margem direita do painel
        int groupWidth = BTN_W1 + BTN_GAP + BTN_W2;
        int actionsRight = x + PANEL_W - MARGIN_R;
        int actionsLeft = actionsRight - groupWidth;

        for (int i = start; i < end; i++) {
            final int idx = i;
            Entry e = all.get(i);

            int rowTop = y + ROW_TOP + (i - start) * ROW_HEIGHT;
            int rowMidY = rowTop + (ROW_HEIGHT / 2);
            int btnY = rowMidY - (BTN_H / 2); // centraliza na “div”

            ButtonWidget summon = ButtonWidget.builder(
                    Text.translatable("gui.rallyguard.command.summon"),
                    b -> sendAction(e.entityId(), NetworkConstants.ACTION_SUMMON)
            ).dimensions(actionsLeft, btnY, BTN_W1, BTN_H).build();

            Text label = e.patrolling()
                    ? Text.translatable("gui.rallyguard.command.stop")
                    : Text.translatable("gui.rallyguard.command.patrol");

            ButtonWidget patrol = ButtonWidget.builder(label, b -> {
                sendAction(e.entityId(), NetworkConstants.ACTION_TOGGLE_PATROL);
                // atualização otimista
                Entry curr = all.get(idx);
                all.set(idx, new Entry(curr.entityId(), curr.name(), !curr.patrolling()));
                rebuildButtons();
            }).dimensions(actionsLeft + BTN_W1 + BTN_GAP, btnY, BTN_W2, BTN_H).build();

            this.addDrawableChild(summon);
            this.addDrawableChild(patrol);
        }

        // Paginação
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
        }).dimensions(x + PANEL_W - 30, y + PANEL_H - 28, 22, 20).build();

        this.addDrawableChild(prev);
        this.addDrawableChild(next);
    }

    private void sendAction(int entityId, int action) {
        ClientPlayNetworking.send(new GuardActionC2SPayload(entityId, action));
    }

    /**
     * Sem overlay escuro padrão.
     */
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
        int x = (this.width - PANEL_W) / 2;
        int y = (this.height - PANEL_H) / 2;

        // painel
        int bg = 0xF0101010;
        ctx.fill(x, y, x + PANEL_W, y + PANEL_H, bg);

        int border = 0xFFFFFFFF;
        ctx.fill(x, y, x + PANEL_W, y + 1, border);
        ctx.fill(x, y + PANEL_H - 1, x + PANEL_W, y + PANEL_H, border);
        ctx.fill(x, y, x + 1, y + PANEL_H, border);
        ctx.fill(x + PANEL_W - 1, y, x + PANEL_W, y + PANEL_H, border);

        // título
        ctx.drawCenteredTextWithShadow(this.textRenderer,
                Text.translatable("gui.rallyguard.command.title"),
                this.width / 2, y + 8, 0xFFFFFF);

        // cabeçalhos
        ctx.drawTextWithShadow(this.textRenderer,
                Text.translatable("gui.rallyguard.command.name"),
                x + COL_NAME_X, y + HEADER_Y, 0xCCCCCC);

        // “Ações” centralizado sobre a área dos botões
        int groupWidth = BTN_W1 + BTN_GAP + BTN_W2;
        int actionsRight = x + PANEL_W - MARGIN_R;
        int actionsLeft = actionsRight - groupWidth;
        int actionsCenterX = actionsLeft + groupWidth / 2;

        int actionsHeaderW = this.textRenderer.getWidth(Text.translatable("gui.rallyguard.command.actions"));
        ctx.drawTextWithShadow(this.textRenderer,
                Text.translatable("gui.rallyguard.command.actions"),
                actionsCenterX - (actionsHeaderW / 2),
                y + HEADER_Y, 0xCCCCCC);

        // linha do cabeçalho
        ctx.fill(x + 6, y + HEADER_LINE_Y, x + PANEL_W - 6, y + HEADER_LINE_Y + 1, 0x33FFFFFF);
    }

    private void drawRows(DrawContext ctx) {
        int x = (this.width - PANEL_W) / 2;
        int y = (this.height - PANEL_H) / 2;

        if (all.isEmpty()) {
            ctx.drawCenteredTextWithShadow(this.textRenderer,
                    Text.translatable("gui.rallyguard.command.empty"),
                    this.width / 2, y + (PANEL_H / 2), 0xAAAAAA);
            drawPageIndicator(ctx, x, y);
            return;
        }

        int start = page * PER_PAGE;
        int end = Math.min(start + PER_PAGE, all.size());

        int fontH = this.textRenderer.fontHeight;

        // área de ações para recorte do nome
        int groupWidth = BTN_W1 + BTN_GAP + BTN_W2;
        int actionsRight = x + PANEL_W - MARGIN_R;
        int actionsLeft = actionsRight - groupWidth;

        for (int i = start; i < end; i++) {
            Entry e = all.get(i);

            int rowTop = y + ROW_TOP + (i - start) * ROW_HEIGHT;
            int rowMidY = rowTop + (ROW_HEIGHT / 2);
            int rowBot = rowTop + ROW_HEIGHT;

            // separador ao fim da “div”
            ctx.fill(x + 6, rowBot - 1, x + PANEL_W - 6, rowBot, 0x22FFFFFF);

            // Nome centralizado verticalmente; recorte até antes da área de botões
            String name = e.name();
            int maxNameW = (actionsLeft - 12) - (x + COL_NAME_X);
            if (this.textRenderer.getWidth(name) > maxNameW) {
                name = this.textRenderer.trimToWidth(name, maxNameW - this.textRenderer.getWidth("...")) + "...";
            }
            int nameY = rowMidY - (fontH / 2);
            ctx.drawTextWithShadow(this.textRenderer, Text.literal(name), x + COL_NAME_X, nameY, 0xFFFFFF);
        }

        drawPageIndicator(ctx, x, y);
    }

    private void drawPageIndicator(DrawContext ctx, int x, int y) {
        int totalPages = Math.max(1, (all.size() + PER_PAGE - 1) / PER_PAGE);
        String pg = (page + 1) + " / " + totalPages;
        int w = this.textRenderer.getWidth(pg);
        ctx.drawTextWithShadow(this.textRenderer, Text.literal(pg),
                x + (PANEL_W - w) / 2, y + PANEL_H - 24, 0xFFFFFF);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
