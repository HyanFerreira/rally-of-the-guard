package net.hfstack.rallyguard.screen;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.hfstack.rallyguard.network.NetworkConstants;
import net.hfstack.rallyguard.network.payload.GuardAttackTargetC2SPayload;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

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
        super(Text.translatable("gui.rallyguard.combat.title"));
        this.parent = parent;
        this.target = currentTarget();
    }

    @Override
    protected void init() {
        super.init();

        int x = panelX();
        int y = panelY();

        ButtonWidget all = ButtonWidget.builder(
                Text.translatable("gui.rallyguard.combat.all"),
                b -> sendAttack(NetworkConstants.ATTACK_ALL)
        ).dimensions(x + 18, y + 78, 92, BTN_H).build();

        ButtonWidget infantry = ButtonWidget.builder(
                Text.translatable("gui.rallyguard.combat.infantry"),
                b -> sendAttack(NetworkConstants.ATTACK_INFANTRY)
        ).dimensions(x + 124, y + 78, 92, BTN_H).build();

        ButtonWidget ranged = ButtonWidget.builder(
                Text.translatable("gui.rallyguard.combat.ranged"),
                b -> sendAttack(NetworkConstants.ATTACK_RANGED)
        ).dimensions(x + 230, y + 78, 92, BTN_H).build();

        this.addDrawableChild(all);
        this.addDrawableChild(infantry);
        this.addDrawableChild(ranged);

        this.addDrawableChild(ButtonWidget.builder(
                Text.translatable("gui.rallyguard.route.back"),
                b -> closeToParent()
        ).dimensions(x + (PANEL_W - 70) / 2, y + 114, 70, BTN_H).build());
    }

    private static Entity currentTarget() {
        MinecraftClient client = MinecraftClient.getInstance();
        HitResult hit = client.crosshairTarget;
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
        MinecraftClient.getInstance().setScreen(parent);
    }

    @Override
    public void close() {
        closeToParent();
    }

    @Override
    public void renderBackground(DrawContext ctx, int mouseX, int mouseY, float delta) {
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        drawPanel(ctx);
        super.render(ctx, mouseX, mouseY, delta);
    }

    private void drawPanel(DrawContext ctx) {
        int x = panelX();
        int y = panelY();

        ctx.fill(x, y, x + PANEL_W, y + PANEL_H, 0xF0101010);
        ctx.fill(x, y, x + PANEL_W, y + 1, WHITE);
        ctx.fill(x, y + PANEL_H - 1, x + PANEL_W, y + PANEL_H, WHITE);
        ctx.fill(x, y, x + 1, y + PANEL_H, WHITE);
        ctx.fill(x + PANEL_W - 1, y, x + PANEL_W, y + PANEL_H, WHITE);

        ctx.drawCenteredTextWithShadow(this.textRenderer,
                Text.translatable("gui.rallyguard.combat.title"),
                this.width / 2, y + 10, WHITE);

        Text targetText = target == null
                ? Text.translatable("gui.rallyguard.combat.long_range_target")
                : Text.translatable("gui.rallyguard.combat.target", target.getName().getString());

        int color = target == null ? WARNING : MUTED;
        ctx.drawCenteredTextWithShadow(this.textRenderer, targetText, this.width / 2, y + 40, color);
        ctx.fill(x + 10, y + 66, x + PANEL_W - 10, y + 67, 0x33FFFFFF);
    }

    private int panelX() {
        return (this.width - PANEL_W) / 2;
    }

    private int panelY() {
        return (this.height - PANEL_H) / 2;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
