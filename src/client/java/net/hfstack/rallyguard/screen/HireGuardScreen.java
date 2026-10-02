package net.hfstack.rallyguard.screen;

import net.hfstack.rallyguard.config.RallyConfig;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.network.chat.Component;

import java.util.List;

public class HireGuardScreen extends AbstractContainerScreen<HireGuardScreenHandler> {
    private static final int WHITE = 0xFFFFFFFF;

    private Button hireButton;

    public HireGuardScreen(HireGuardScreenHandler handler, Inventory inv, Component title) {
        super(handler, inv, title, 320, 110);
    }

    @Override
    protected void init() {
        super.init();

        // esconde labels padrão
        this.inventoryLabelX = Integer.MAX_VALUE / 2;
        this.inventoryLabelY = -1000;
        this.titleLabelX = Integer.MAX_VALUE / 2;

        int y = (this.height - this.imageHeight) / 2;

        // Botão “Contratar” — envia o botão 0 para o SERVIDOR
        this.hireButton = Button.builder(
                Component.translatable("gui.rallyguard.hire.button"),
                b -> {
                    if (this.minecraft.gameMode != null) {
                        this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 0);
                    }
                }
        ).bounds(this.width / 2 - 50, y + 66, 100, 20).build();

        this.addRenderableWidget(this.hireButton);
    }

    /**
     * NÃO chamamos renderBackground para não escurecer o mundo atrás.
     */
    @Override
    public void extractBackground(GuiGraphicsExtractor ctx, int mouseX, int mouseY, float delta) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        // Corpo do painel
        int bg = 0xF0101010; // leve translucidez, “vanilla vibe”
        ctx.fill(x, y, x + this.imageWidth, y + this.imageHeight, bg);

        // Borda
        int border = 0xFFFFFFFF;
        ctx.fill(x, y, x + this.imageWidth, y + 1, border);                                       // topo
        ctx.fill(x, y + this.imageHeight - 1, x + this.imageWidth, y + this.imageHeight, border); // base
        ctx.fill(x, y, x + 1, y + this.imageHeight, border);                                      // esquerda
        ctx.fill(x + this.imageWidth - 1, y, x + this.imageWidth, y + this.imageHeight, border);  // direita

        // Título central
        ctx.centeredText(this.font,
                Component.translatable("gui.rallyguard.hire.title"),
                this.width / 2, y + 10, WHITE);

        // Mensagem com suporte a \n e wrap
        Component body = Component.translatable(
                "gui.rallyguard.hire.body",
                RallyConfig.hireCost(),
                RallyConfig.hireItem().getName(RallyConfig.hireItem().getDefaultInstance())
        );

        int maxComponentWidth = this.imageWidth - 24; // margem interna
        // wrap automático (respeita quebras explícitas \n também)
        List<FormattedCharSequence> lines = this.font.split(body, maxComponentWidth);

        int lineY = y + 36;
        for (FormattedCharSequence ot : lines) {
            int w = this.font.width(ot);
            int lineX = this.width / 2 - (w / 2); // centraliza cada linha
            ctx.text(this.font, ot, lineX, lineY, WHITE);
            lineY += this.font.lineHeight + 2;
        }
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor ctx, int mouseX, int mouseY) {
        // intencionalmente vazio
    }
}
