package net.hfstack.rallyguard.screen;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;

public class HireGuardScreen extends HandledScreen<HireGuardScreenHandler> {

    private ButtonWidget hireButton;

    public HireGuardScreen(HireGuardScreenHandler handler, PlayerInventory inv, Text title) {
        super(handler, inv, title);
        this.backgroundWidth = 320;
        this.backgroundHeight = 110;
    }

    @Override
    protected void init() {
        super.init();

        // esconde “Inventário” e o título padrão (vamos desenhar o nosso)
        this.playerInventoryTitleX = Integer.MAX_VALUE / 2;
        this.playerInventoryTitleY = -1000;
        this.titleX = Integer.MAX_VALUE / 2;

        int y = (this.height - this.backgroundHeight) / 2;

        // Botão “Contratar” — envia o botão 0 para o SERVIDOR
        this.hireButton = ButtonWidget.builder(
                Text.translatable("gui.rallyguard.hire.button"),
                b -> {
                    if (this.client != null && this.client.interactionManager != null) {
                        this.client.interactionManager.clickButton(this.handler.syncId, 0);
                    }
                }
        ).dimensions(this.width / 2 - 50, y + 66, 100, 20).build();

        this.addDrawableChild(this.hireButton);
    }

    /**
     * NÃO chamamos renderBackground para não escurecer o mundo atrás.
     */
    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        super.render(ctx, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(ctx, mouseX, mouseY);
    }

    /**
     * Painel estilo vanilla simples (retângulo + borda)
     */
    @Override
    protected void drawBackground(DrawContext ctx, float delta, int mouseX, int mouseY) {
        int x = (this.width - this.backgroundWidth) / 2;
        int y = (this.height - this.backgroundHeight) / 2;

        // Corpo do painel
        int bg = 0xF0101010; // leve translucidez, “vanilla vibe”
        ctx.fill(x, y, x + this.backgroundWidth, y + this.backgroundHeight, bg);

        // Borda
        int border = 0xFFFFFFFF;
        // topo
        ctx.fill(x, y, x + this.backgroundWidth, y + 1, border);
        // base
        ctx.fill(x, y + this.backgroundHeight - 1, x + this.backgroundWidth, y + this.backgroundHeight, border);
        // esquerda
        ctx.fill(x, y, x + 1, y + this.backgroundHeight, border);
        // direita
        ctx.fill(x + this.backgroundWidth - 1, y, x + this.backgroundWidth, y + this.backgroundHeight, border);

        // Título central
        ctx.drawCenteredTextWithShadow(this.textRenderer,
                Text.translatable("gui.rallyguard.hire.title"),
                this.width / 2, y + 10, 0xFFFFFF);

        // Mensagem
        ctx.drawCenteredTextWithShadow(this.textRenderer,
                Text.translatable("gui.rallyguard.hire.body"),
                this.width / 2, y + 40, 0xFFFFFF);
    }

    /**
     * Não queremos título/labels padrão do HandledScreen.
     */
    @Override
    protected void drawForeground(DrawContext ctx, int mouseX, int mouseY) {
        // intencionalmente vazio
    }
}
