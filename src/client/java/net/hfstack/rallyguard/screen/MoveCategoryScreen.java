package net.hfstack.rallyguard.screen;

import net.hfstack.rallyguard.api.command.GuardCommandScope;
import net.hfstack.rallyguard.api.command.GuardUnitCategory;
import net.hfstack.rallyguard.client.move.MoveTargetingClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class MoveCategoryScreen extends Screen {
    private static final int PANEL_WIDTH = 250;
    private static final int PANEL_HEIGHT = 92;
    private static final int WHITE = 0xFFFFFFFF;

    private final Screen parent;
    private final GuardCommandScope scope;

    public MoveCategoryScreen(Screen parent, GuardCommandScope scope) {
        super(Component.translatable("gui.rallyguard.move.title"));
        this.parent = parent;
        this.scope = scope;
    }

    @Override
    protected void init() {
        super.init();
        int x = (width - PANEL_WIDTH) / 2;
        int y = (height - PANEL_HEIGHT) / 2;
        addCategoryButton(x + 10, y + 40, GuardUnitCategory.ALL, "gui.rallyguard.move.category.all");
        addCategoryButton(x + 88, y + 40, GuardUnitCategory.INFANTRY, "gui.rallyguard.move.category.infantry");
        addCategoryButton(x + 166, y + 40, GuardUnitCategory.RANGED, "gui.rallyguard.move.category.ranged");
    }

    private void addCategoryButton(int x, int y, GuardUnitCategory category, String translationKey) {
        addRenderableWidget(Button.builder(Component.translatable(translationKey), button -> select(category))
                .bounds(x, y, 74, 20)
                .build());
    }

    private void select(GuardUnitCategory category) {
        Minecraft.getInstance().setScreen(null);
        MoveTargetingClient.arm(scope, category);
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(parent);
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
        graphics.centeredText(font, title, width / 2, y + 13, WHITE);
        super.extractRenderState(graphics, mouseX, mouseY, delta);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
