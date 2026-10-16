package net.hfstack.rallyguard.screen;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.hfstack.rallyguard.api.command.GuardCommandScope;
import net.hfstack.rallyguard.combat.stance.GuardCombatStance;
import net.hfstack.rallyguard.network.SetCombatStanceC2SPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class GuardStanceScreen extends Screen {
    private static final int WHITE = 0xFFFFFFFF;
    private static final int MUTED = 0xFFB8B8B8;
    private static final int PANEL_W = 360;
    private static final int PANEL_H = 176;
    private static final int BUTTON_W = 150;
    private static final int BUTTON_H = 20;

    private final Screen parent;
    private final GuardCommandScope scope;

    public GuardStanceScreen(Screen parent, GuardCommandScope scope) {
        super(Component.translatable("gui.rallyguard.stance.title"));
        this.parent = parent;
        this.scope = scope;
    }

    @Override
    protected void init() {
        super.init();
        int x = panelX();
        int y = panelY();
        addStanceButton(GuardCombatStance.DEFENSIVE, x + 20, y + 44, "defensive");
        addStanceButton(GuardCombatStance.AGGRESSIVE, x + 190, y + 44, "aggressive");
        addStanceButton(GuardCombatStance.PASSIVE, x + 20, y + 76, "passive");
        addStanceButton(GuardCombatStance.HOLD_POSITION, x + 190, y + 76, "hold_position");
        addStanceButton(GuardCombatStance.PROTECT_COMMANDER, x + 105, y + 108, "protect_commander");

        addRenderableWidget(Button.builder(
                Component.translatable("gui.rallyguard.route.back"),
                button -> closeToParent()
        ).bounds(x + (PANEL_W - 70) / 2, y + 142, 70, BUTTON_H).build());
    }

    private void addStanceButton(GuardCombatStance stance, int x, int y, String keySuffix) {
        Button button = Button.builder(
                Component.translatable("gui.rallyguard.stance." + keySuffix),
                pressed -> select(stance)
        ).bounds(x, y, BUTTON_W, BUTTON_H).build();
        button.setTooltip(Tooltip.create(Component.translatable(
                "gui.rallyguard.stance." + keySuffix + ".description"
        )));
        addRenderableWidget(button);
    }

    private void select(GuardCombatStance stance) {
        ClientPlayNetworking.send(new SetCombatStanceC2SPayload(scope, stance.wireId()));
        closeToParent();
    }

    private void closeToParent() {
        Minecraft.getInstance().setScreen(parent);
    }

    @Override
    public void onClose() {
        closeToParent();
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        int x = panelX();
        int y = panelY();
        graphics.fill(x, y, x + PANEL_W, y + PANEL_H, 0xF0101010);
        graphics.outline(x, y, PANEL_W, PANEL_H, WHITE);
        graphics.centeredText(font, title, width / 2, y + 10, WHITE);
        graphics.centeredText(
                font,
                Component.translatable("gui.rallyguard.stance.hint"),
                width / 2,
                y + 25,
                MUTED
        );
        super.extractRenderState(graphics, mouseX, mouseY, delta);
    }

    private int panelX() {
        return (width - PANEL_W) / 2;
    }

    private int panelY() {
        return (height - PANEL_H) / 2;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
