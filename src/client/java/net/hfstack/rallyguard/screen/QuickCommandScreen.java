package net.hfstack.rallyguard.screen;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.hfstack.rallyguard.api.command.GuardCommandScope;
import net.hfstack.rallyguard.client.quick.QuickCommandAction;
import net.hfstack.rallyguard.client.quick.QuickCommandLayout;
import net.hfstack.rallyguard.network.payload.ScopedGuardActionC2SPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class QuickCommandScreen extends Screen {
    private static final int WHITE = 0xFFFFFFFF;
    private static final int MUTED = 0xFFCCCCCC;

    public QuickCommandScreen() {
        super(Component.translatable("gui.rallyguard.quick.title"));
    }

    @Override
    protected void init() {
        super.init();
        QuickCommandLayout layout = QuickCommandLayout.forScreen(width, height);
        for (QuickCommandLayout.Slot slot : layout.slots()) {
            QuickCommandAction action = slot.action();
            addRenderableWidget(Button.builder(
                    Component.translatable(action.translationKey()),
                    button -> select(action)
            ).bounds(
                    slot.x(),
                    slot.y(),
                    QuickCommandLayout.BUTTON_WIDTH,
                    QuickCommandLayout.BUTTON_HEIGHT
            ).build());
        }
    }

    private void select(QuickCommandAction action) {
        Minecraft client = Minecraft.getInstance();
        GuardCommandScope scope = GuardCommandScope.all();
        switch (action.flow()) {
            case IMMEDIATE -> {
                ClientPlayNetworking.send(new ScopedGuardActionC2SPayload(
                        action.serverAction().orElseThrow(),
                        scope
                ));
                client.setScreen(null);
            }
            case COMBAT -> client.setScreen(new GuardCombatScreen(null, scope));
            case MOVE -> client.setScreen(new MoveCategoryScreen(null, scope));
            case DEFEND -> client.setScreen(new MoveCategoryScreen(null, scope, MoveCategoryScreen.Mode.DEFEND));
        }
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        graphics.centeredText(font, title, width / 2, height / 2 - 8, WHITE);
        graphics.centeredText(
                font,
                Component.translatable("gui.rallyguard.quick.scope_hint"),
                width / 2,
                height / 2 + 5,
                MUTED
        );
        super.extractRenderState(graphics, mouseX, mouseY, delta);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
