package net.hfstack.rallyguard.screen;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.hfstack.rallyguard.network.payload.RaidBehaviorC2SPayload;
import net.hfstack.rallyguard.network.payload.RaidBehaviorS2CPayload;
import net.hfstack.rallyguard.raid.RaidBehavior;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class RaidBehaviorScreen extends Screen {
    private static final int PANEL_WIDTH = 260;
    private static final int PANEL_HEIGHT = 154;
    private final Screen parent;
    private RaidBehavior current = RaidBehavior.NORMAL;

    public RaidBehaviorScreen(Screen parent) {
        super(Component.translatable("gui.rallyguard.raid.behavior.title"));
        this.parent = parent;
    }

    public static void accept(RaidBehaviorS2CPayload payload) {
        Screen screen = Minecraft.getInstance().screen;
        if (screen instanceof RaidBehaviorScreen behaviorScreen) {
            behaviorScreen.current = RaidBehavior.fromWireId(payload.behaviorId()).orElse(RaidBehavior.NORMAL);
            behaviorScreen.rebuildWidgets();
        }
    }

    @Override
    protected void init() {
        super.init();
        int x = (width - PANEL_WIDTH) / 2;
        int y = (height - PANEL_HEIGHT) / 2;
        int row = y + 44;
        for (RaidBehavior behavior : RaidBehavior.values()) {
            Button button = Button.builder(Component.translatable(behavior.translationKey()),
                    ignored -> save(behavior)).bounds(x + 30, row, 200, 20).build();
            button.active = behavior != current;
            addRenderableWidget(button);
            row += 25;
        }
        addRenderableWidget(Button.builder(Component.translatable("gui.rallyguard.route.back"),
                ignored -> closeToParent()).bounds(x + 85, y + 124, 90, 20).build());
    }

    @Override
    public void added() {
        super.added();
        ClientPlayNetworking.send(new RaidBehaviorC2SPayload(RaidBehaviorC2SPayload.QUERY));
    }

    private void save(RaidBehavior behavior) {
        current = behavior;
        ClientPlayNetworking.send(new RaidBehaviorC2SPayload(behavior.wireId()));
        rebuildWidgets();
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
        int x = (width - PANEL_WIDTH) / 2;
        int y = (height - PANEL_HEIGHT) / 2;
        graphics.fill(x, y, x + PANEL_WIDTH, y + PANEL_HEIGHT, 0xF0101010);
        graphics.outline(x, y, PANEL_WIDTH, PANEL_HEIGHT, 0xFFFFFFFF);
        graphics.centeredText(font, title, width / 2, y + 11, 0xFFFFFFFF);
        graphics.centeredText(font, Component.translatable("gui.rallyguard.raid.behavior.hint"),
                width / 2, y + 26, 0xFFCCCCCC);
        super.extractRenderState(graphics, mouseX, mouseY, delta);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
