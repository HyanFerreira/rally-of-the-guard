package net.hfstack.rallyguard.screen;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.hfstack.rallyguard.network.NetworkConstants;
import net.hfstack.rallyguard.network.payload.GuardAttackTargetC2SPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.world.entity.Entity;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class GuardCombatScreen extends Screen {
    private static final int WHITE = 0xFFFFFFFF;
    private static final int MUTED = 0xFFCCCCCC;
    private static final int WARNING = 0xFFFFCC66;

    private static final int PANEL_W = 340;
    private static final int PANEL_H = 150;
    private static final int BTN_H = 20;

    private final Screen parent;
    private final Entity target;

    public GuardCombatScreen(Screen parent) {
        super(Component.translatable("gui.rallyguard.combat.title"));
        this.parent = parent;
        this.target = currentTarget();
    }

    @Override
    protected void init() {
        super.init();

        int x = panelX();
        int y = panelY();

        Button all = Button.builder(
                Component.translatable("gui.rallyguard.combat.all"),
                b -> sendAttack(NetworkConstants.ATTACK_ALL)
        ).bounds(x + 18, y + 78, 92, BTN_H).build();

        Button infantry = Button.builder(
                Component.translatable("gui.rallyguard.combat.infantry"),
                b -> sendAttack(NetworkConstants.ATTACK_INFANTRY)
        ).bounds(x + 124, y + 78, 92, BTN_H).build();

        Button ranged = Button.builder(
                Component.translatable("gui.rallyguard.combat.ranged"),
                b -> sendAttack(NetworkConstants.ATTACK_RANGED)
        ).bounds(x + 230, y + 78, 92, BTN_H).build();

        this.addRenderableWidget(all);
        this.addRenderableWidget(infantry);
        this.addRenderableWidget(ranged);

        this.addRenderableWidget(Button.builder(
                Component.translatable("gui.rallyguard.route.back"),
                b -> closeToParent()
        ).bounds(x + (PANEL_W - 70) / 2, y + 114, 70, BTN_H).build());
    }

    private static Entity currentTarget() {
        Minecraft client = Minecraft.getInstance();
        HitResult hit = client.hitResult;
        if (hit instanceof EntityHitResult entityHit) {
            return entityHit.getEntity();
        }
        return null;
    }

    private void sendAttack(int mode) {
        int targetId = target != null ? target.getId() : -1;
        ClientPlayNetworking.send(new GuardAttackTargetC2SPayload(targetId, mode));
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
    public void extractBackground(GuiGraphicsExtractor ctx, int mouseX, int mouseY, float delta) {
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor ctx, int mouseX, int mouseY, float delta) {
        drawPanel(ctx);
        super.extractRenderState(ctx, mouseX, mouseY, delta);
    }

    private void drawPanel(GuiGraphicsExtractor ctx) {
        int x = panelX();
        int y = panelY();

        ctx.fill(x, y, x + PANEL_W, y + PANEL_H, 0xF0101010);
        ctx.fill(x, y, x + PANEL_W, y + 1, WHITE);
        ctx.fill(x, y + PANEL_H - 1, x + PANEL_W, y + PANEL_H, WHITE);
        ctx.fill(x, y, x + 1, y + PANEL_H, WHITE);
        ctx.fill(x + PANEL_W - 1, y, x + PANEL_W, y + PANEL_H, WHITE);

        ctx.centeredText(this.font,
                Component.translatable("gui.rallyguard.combat.title"),
                this.width / 2, y + 10, WHITE);

        Component targetComponent = target == null
                ? Component.translatable("gui.rallyguard.combat.long_range_target")
                : Component.translatable("gui.rallyguard.combat.target", target.getName().getString());

        int color = target == null ? WARNING : MUTED;
        ctx.centeredText(this.font, targetComponent, this.width / 2, y + 40, color);
        ctx.fill(x + 10, y + 66, x + PANEL_W - 10, y + 67, 0x33FFFFFF);
    }

    private int panelX() {
        return (this.width - PANEL_W) / 2;
    }

    private int panelY() {
        return (this.height - PANEL_H) / 2;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
