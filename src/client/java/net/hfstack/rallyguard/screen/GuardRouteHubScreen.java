package net.hfstack.rallyguard.screen;

import net.hfstack.rallyguard.network.payload.GuardListS2CPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class GuardRouteHubScreen extends Screen {
    private static final int PANEL_WIDTH = 360;
    private static final int PANEL_HEIGHT = 142;
    private static final int WHITE = 0xFFFFFFFF;
    private final GuardCommandScreen parent;
    private final GuardListS2CPayload snapshot;
    private final int entryIndex;
    private final GuardCommandScreen.Entry guard;

    public GuardRouteHubScreen(GuardCommandScreen parent, GuardListS2CPayload snapshot,
                               int entryIndex, GuardCommandScreen.Entry guard) {
        super(Component.translatable("gui.rallyguard.route.title_named", guard.name()));
        this.parent = parent;
        this.snapshot = snapshot;
        this.entryIndex = entryIndex;
        this.guard = guard;
    }

    @Override
    protected void init() {
        super.init();
        int x = (width - PANEL_WIDTH) / 2;
        int y = (height - PANEL_HEIGHT) / 2;
        addRenderableWidget(Button.builder(Component.translatable("gui.rallyguard.named_route.personal"),
                        button -> Minecraft.getInstance().setScreen(new GuardRouteScreen(parent, entryIndex, guard)))
                .bounds(x + 24, y + 42, PANEL_WIDTH - 48, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.rallyguard.named_route.manage"),
                        button -> Minecraft.getInstance().setScreen(
                                new NamedRouteManagementScreen(parent, snapshot, guard.guardId())))
                .bounds(x + 24, y + 68, PANEL_WIDTH - 48, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.rallyguard.route.back"),
                        button -> closeToParent())
                .bounds(x + PANEL_WIDTH - 94, y + PANEL_HEIGHT - 28, 70, 20).build());
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
        graphics.outline(x, y, PANEL_WIDTH, PANEL_HEIGHT, WHITE);
        graphics.centeredText(font, title, width / 2, y + 12, WHITE);
        super.extractRenderState(graphics, mouseX, mouseY, delta);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
