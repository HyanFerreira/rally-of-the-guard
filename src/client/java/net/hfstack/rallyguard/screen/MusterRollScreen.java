package net.hfstack.rallyguard.screen;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.hfstack.rallyguard.muster.GuardProfileSnapshot;
import net.hfstack.rallyguard.muster.GuardServiceStatus;
import net.hfstack.rallyguard.network.payload.CloseMusterRollC2SPayload;
import net.hfstack.rallyguard.network.payload.MusterRollSnapshotS2CPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;

import java.util.Locale;

public final class MusterRollScreen extends Screen {
    private static final int PREFERRED_PANEL_WIDTH = 420;
    private static final int MINIMUM_PANEL_WIDTH = 320;
    private static final int SCREEN_PADDING = 12;
    private static final int PANEL_HEIGHT = 250;
    private static final int WHITE = 0xFFFFFFFF;
    private static final int MUTED = 0xFFBBBBBB;
    private static final int GOLD = 0xFFFFD45A;

    private GuardProfileSnapshot snapshot;
    private boolean closedByServer;

    private MusterRollScreen(GuardProfileSnapshot snapshot) {
        super(Component.translatable("gui.rallyguard.muster.title"));
        this.snapshot = snapshot;
    }

    public static void accept(MusterRollSnapshotS2CPayload payload) {
        Minecraft client = Minecraft.getInstance();
        client.execute(() -> {
            if (payload.openScreen() && client.screen instanceof MusterRollScreen screen) {
                screen.snapshot = payload.snapshot();
            } else if (payload.openScreen()) {
                client.setScreen(new MusterRollScreen(payload.snapshot()));
            } else if (client.screen instanceof MusterRollScreen screen
                    && screen.snapshot.entityUuid().equals(payload.snapshot().entityUuid())) {
                screen.snapshot = payload.snapshot();
            }
        });
    }

    public static void closeFromServer() {
        Minecraft client = Minecraft.getInstance();
        client.execute(() -> {
            if (client.screen instanceof MusterRollScreen screen) {
                screen.closedByServer = true;
                client.setScreen(null);
            }
        });
    }

    @Override
    public void removed() {
        if (!closedByServer && ClientPlayNetworking.canSend(CloseMusterRollC2SPayload.ID)) {
            ClientPlayNetworking.send(new CloseMusterRollC2SPayload());
        }
        super.removed();
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        drawPanel(graphics);
        super.extractRenderState(graphics, mouseX, mouseY, delta);
    }

    private void drawPanel(GuiGraphicsExtractor graphics) {
        int panelWidth = ScreenLayout.boundedPanelWidth(
                width,
                PREFERRED_PANEL_WIDTH,
                MINIMUM_PANEL_WIDTH,
                SCREEN_PADDING
        );
        int x = (width - panelWidth) / 2;
        int y = (height - PANEL_HEIGHT) / 2;
        int panelCenterX = ScreenLayout.panelCenterX(x, panelWidth);
        graphics.fill(x, y, x + panelWidth, y + PANEL_HEIGHT, 0xF0101010);
        graphics.fill(x, y, x + panelWidth, y + 1, WHITE);
        graphics.fill(x, y + PANEL_HEIGHT - 1, x + panelWidth, y + PANEL_HEIGHT, WHITE);
        graphics.fill(x, y, x + 1, y + PANEL_HEIGHT, WHITE);
        graphics.fill(x + panelWidth - 1, y, x + panelWidth, y + PANEL_HEIGHT, WHITE);

        graphics.centeredText(font, Component.translatable("gui.rallyguard.muster.title"), panelCenterX, y + 10, WHITE);
        String name = snapshot.displayName().isBlank()
                ? Component.translatable("entity.guardvillagers.guard").getString()
                : snapshot.displayName();
        graphics.centeredText(font, Component.literal(name), panelCenterX, y + 30, GOLD);

        if (snapshot.captain()) {
            graphics.centeredText(
                    font,
                    Component.translatable("gui.rallyguard.muster.captain"),
                    panelCenterX,
                    y + 44,
                    GOLD
            );
        }

        int left = x + 22;
        int right = x + 205;
        int line = y + (snapshot.captain() ? 64 : 54);
        Component rank = snapshot.rankOverride()
                .<Component>map(Component::literal)
                .orElseGet(() -> Component.translatable(snapshot.rank().translationKey()));
        drawLine(graphics, left, line, "gui.rallyguard.muster.rank_level", rank, snapshot.level());
        drawLine(graphics, right, line, "gui.rallyguard.muster.stars", "★".repeat(snapshot.stars()));
        line += 20;
        String xp = snapshot.xpForNextLevelMilli() == 0
                ? Component.translatable("gui.rallyguard.muster.max_level").getString()
                : formatXp(snapshot.xpIntoLevelMilli()) + " / " + formatXp(snapshot.xpForNextLevelMilli());
        drawLine(graphics, left, line, "gui.rallyguard.muster.xp", xp);
        drawLine(graphics, right, line, "gui.rallyguard.muster.service", serviceLabel(snapshot.serviceStatus()));

        line += 28;
        graphics.text(font, Component.translatable("gui.rallyguard.muster.attributes"), left, line, WHITE);
        line += 17;
        drawLine(graphics, left, line, "gui.rallyguard.muster.health", decimal(snapshot.maxHealth()));
        drawLine(graphics, right, line, "gui.rallyguard.muster.attack", decimal(snapshot.attackDamage()));
        line += 17;
        drawLine(graphics, left, line, "gui.rallyguard.muster.armor", decimal(snapshot.armor()));
        drawLine(graphics, right, line, "gui.rallyguard.muster.speed", decimal(snapshot.movementSpeed()));

        line += 28;
        graphics.text(font, Component.translatable("gui.rallyguard.muster.record"), left, line, WHITE);
        line += 17;
        drawLine(graphics, left, line, "gui.rallyguard.muster.kills", snapshot.kills());
        drawLine(graphics, right, line, "gui.rallyguard.muster.assists", snapshot.assists());

        line += 28;
        if (snapshot.hiringOffer().isPresent()) {
            var offer = snapshot.hiringOffer().orElseThrow();
            Item item = BuiltInRegistries.ITEM.getValue(offer.paymentItemId());
            Component itemName = item == null ? Component.literal(offer.paymentItemId().toString())
                    : item.getName(item.getDefaultInstance());
            graphics.centeredText(font, Component.translatable(
                    "gui.rallyguard.muster.offer",
                    offer.cost(),
                    itemName
            ), panelCenterX, line, GOLD);
        }
        graphics.centeredText(font, Component.translatable("gui.rallyguard.muster.close_hint"), panelCenterX,
                y + PANEL_HEIGHT - 18, MUTED);
    }

    private void drawLine(GuiGraphicsExtractor graphics, int x, int y, String key, Object... values) {
        graphics.text(font, Component.translatable(key, values), x, y, MUTED);
    }

    private static Component serviceLabel(GuardServiceStatus status) {
        return Component.translatable(switch (status) {
            case UNOWNED -> "gui.rallyguard.muster.service.unowned";
            case UNDER_YOUR_COMMAND -> "gui.rallyguard.muster.service.yours";
            case ALREADY_IN_SERVICE -> "gui.rallyguard.muster.service.other";
        });
    }

    private static String formatXp(long milli) {
        return String.format(Locale.ROOT, "%.1f", milli / 1_000.0);
    }

    private static String decimal(double value) {
        return String.format(Locale.ROOT, "%.2f", value);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
