package net.hfstack.rallyguard.screen;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.hfstack.rallyguard.commandpost.CommandPostState;
import net.hfstack.rallyguard.formation.RallyFormation;
import net.hfstack.rallyguard.network.payload.CommandPostSnapshotS2CPayload;
import net.hfstack.rallyguard.network.payload.CommandPostUpdateC2SPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.UUID;

public final class CommandPostScreen extends Screen {
    private static final int PANEL_HEIGHT = 330;
    private static final int ROWS = 5;
    private static final int ROW_HEIGHT = 34;
    private static final int WHITE = 0xFFFFFFFF;
    private static final int MUTED = 0xFFAAAAAA;
    private static final int GOLD = 0xFFFFC94A;

    private CommandPostSnapshotS2CPayload snapshot;
    private EditBox nameBox;
    private UUID selectedAvailable;
    private UUID selectedAssigned;
    private int availablePage;
    private int assignedPage;

    private CommandPostScreen(CommandPostSnapshotS2CPayload snapshot) {
        super(Component.translatable("gui.rallyguard.command_post.title"));
        this.snapshot = snapshot;
    }

    public static void accept(CommandPostSnapshotS2CPayload snapshot) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.screen instanceof CommandPostScreen screen && screen.snapshot.postId().equals(snapshot.postId())) {
            screen.snapshot = snapshot;
            screen.retainSelections();
            screen.rebuildControls();
        } else {
            minecraft.setScreen(new CommandPostScreen(snapshot));
        }
    }

    @Override
    protected void init() {
        super.init();
        rebuildControls();
    }

    private void rebuildControls() {
        clearWidgets();
        clampPages();
        int x = panelX();
        int y = panelY();
        int panelW = panelWidth();
        int columnW = (panelW - 72) / 2;
        int rightX = x + panelW - 14 - columnW;

        nameBox = new EditBox(font, x + 14, y + 30, panelW - 108, 20,
                Component.translatable("gui.rallyguard.command_post.name"));
        nameBox.setMaxLength(CommandPostState.MAX_NAME_CODE_POINTS);
        nameBox.setValue(snapshot.name());
        addRenderableWidget(nameBox);
        addRenderableWidget(Button.builder(Component.translatable("gui.rallyguard.command_post.save"), button -> saveSettings())
                .bounds(x + panelW - 88, y + 30, 74, 20).build());

        addRenderableWidget(Button.builder(Component.translatable(snapshot.active()
                                ? "gui.rallyguard.command_post.active" : "gui.rallyguard.command_post.inactive"),
                        button -> sendSettings(!snapshot.active(), snapshot.radius(), formation()))
                .bounds(x + 14, y + 58, 92, 20).build());
        addRenderableWidget(Button.builder(Component.literal("-4"), button -> changeRadius(-4))
                .bounds(x + 112, y + 58, 34, 20).build());
        addRenderableWidget(Button.builder(Component.literal("+4"), button -> changeRadius(4))
                .bounds(x + 150, y + 58, 34, 20).build());
        addRenderableWidget(Button.builder(Component.translatable(formation().translationKey()), button ->
                        sendSettings(snapshot.active(), snapshot.radius(), formation().next()))
                .bounds(x + 190, y + 58, Math.min(150, panelW - 204), 20).build());

        addGuardRows(available(), availablePage, x + 14, columnW, selectedAvailable, guard -> {
            selectedAvailable = guard.guardId();
            selectedAssigned = null;
            rebuildControls();
        });
        addGuardRows(assigned(), assignedPage, rightX, columnW, selectedAssigned, guard -> {
            selectedAssigned = guard.guardId();
            selectedAvailable = null;
            rebuildControls();
        });

        int transferX = x + panelW / 2 - 18;
        Button assign = Button.builder(Component.literal(">"), button -> assignSelected())
                .bounds(transferX, y + 152, 36, 20).build();
        assign.active = selectedAvailableEntry() != null;
        addRenderableWidget(assign);
        Button remove = Button.builder(Component.literal("<"), button -> removeSelected())
                .bounds(transferX, y + 180, 36, 20).build();
        remove.active = selectedAssignedEntry() != null;
        addRenderableWidget(remove);

        int bottom = y + PANEL_HEIGHT - 28;
        addRenderableWidget(Button.builder(Component.literal("<"), button -> changePage(false, -1))
                .bounds(x + 14, bottom, 24, 20).build());
        addRenderableWidget(Button.builder(Component.literal(">"), button -> changePage(false, 1))
                .bounds(x + 42, bottom, 24, 20).build());
        addRenderableWidget(Button.builder(Component.literal("<"), button -> changePage(true, -1))
                .bounds(rightX, bottom, 24, 20).build());
        addRenderableWidget(Button.builder(Component.literal(">"), button -> changePage(true, 1))
                .bounds(rightX + 28, bottom, 24, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> onClose())
                .bounds(x + panelW - 84, bottom, 70, 20).build());
    }

    private void addGuardRows(List<CommandPostSnapshotS2CPayload.GuardEntry> guards, int page, int rowX, int rowW,
                              UUID selected, java.util.function.Consumer<CommandPostSnapshotS2CPayload.GuardEntry> click) {
        int start = page * ROWS;
        int end = Math.min(start + ROWS, guards.size());
        for (int i = start; i < end; i++) {
            CommandPostSnapshotS2CPayload.GuardEntry guard = guards.get(i);
            int rowY = panelY() + 112 + (i - start) * ROW_HEIGHT;
            addRenderableWidget(Button.builder(Component.empty(), button -> click.accept(guard))
                    .bounds(rowX, rowY, rowW, ROW_HEIGHT - 2).build());
        }
    }

    private void saveSettings() {
        sendSettings(snapshot.active(), snapshot.radius(), formation());
    }

    private void changeRadius(int delta) {
        int radius = Math.max(CommandPostState.MIN_RADIUS, Math.min(CommandPostState.MAX_RADIUS, snapshot.radius() + delta));
        sendSettings(snapshot.active(), radius, formation());
    }

    private void sendSettings(boolean active, int radius, RallyFormation formation) {
        ClientPlayNetworking.send(new CommandPostUpdateC2SPayload(
                snapshot.pos(), CommandPostUpdateC2SPayload.UPDATE_SETTINGS, 0,
                nameBox == null ? snapshot.name() : nameBox.getValue(), active, radius, formation.id()
        ));
    }

    private void assignSelected() {
        CommandPostSnapshotS2CPayload.GuardEntry guard = selectedAvailableEntry();
        if (guard == null) return;
        if (guard.assignedElsewhere()) {
            Minecraft.getInstance().setScreen(new ConfirmScreen(
                    confirmed -> {
                        if (confirmed) sendGuardAction(guard, CommandPostUpdateC2SPayload.ASSIGN_GUARD);
                        Minecraft.getInstance().setScreen(this);
                    },
                    Component.translatable("gui.rallyguard.command_post.transfer_title"),
                    Component.translatable("gui.rallyguard.command_post.transfer", guard.name())
            ));
        } else {
            sendGuardAction(guard, CommandPostUpdateC2SPayload.ASSIGN_GUARD);
        }
    }

    private void removeSelected() {
        CommandPostSnapshotS2CPayload.GuardEntry guard = selectedAssignedEntry();
        if (guard != null) sendGuardAction(guard, CommandPostUpdateC2SPayload.UNASSIGN_GUARD);
    }

    private void sendGuardAction(CommandPostSnapshotS2CPayload.GuardEntry guard, int action) {
        ClientPlayNetworking.send(new CommandPostUpdateC2SPayload(
                snapshot.pos(), action, guard.entityId(), snapshot.name(), snapshot.active(), snapshot.radius(), snapshot.formationId()
        ));
    }

    private RallyFormation formation() {
        return RallyFormation.fromId(snapshot.formationId());
    }

    private List<CommandPostSnapshotS2CPayload.GuardEntry> available() {
        return snapshot.guards().stream().filter(guard -> !guard.assignedHere()).toList();
    }

    private List<CommandPostSnapshotS2CPayload.GuardEntry> assigned() {
        return snapshot.guards().stream().filter(CommandPostSnapshotS2CPayload.GuardEntry::assignedHere).toList();
    }

    private CommandPostSnapshotS2CPayload.GuardEntry selectedAvailableEntry() {
        return find(available(), selectedAvailable);
    }

    private CommandPostSnapshotS2CPayload.GuardEntry selectedAssignedEntry() {
        return find(assigned(), selectedAssigned);
    }

    private CommandPostSnapshotS2CPayload.GuardEntry find(List<CommandPostSnapshotS2CPayload.GuardEntry> guards, UUID id) {
        if (id == null) return null;
        return guards.stream().filter(guard -> guard.guardId().equals(id)).findFirst().orElse(null);
    }

    private void retainSelections() {
        if (selectedAvailableEntry() == null) selectedAvailable = null;
        if (selectedAssignedEntry() == null) selectedAssigned = null;
    }

    private void clampPages() {
        availablePage = clampPage(availablePage, available().size());
        assignedPage = clampPage(assignedPage, assigned().size());
    }

    private int clampPage(int page, int size) {
        return Math.max(0, Math.min(page, Math.max(0, (size - 1) / ROWS)));
    }

    private void changePage(boolean right, int delta) {
        int size = right ? assigned().size() : available().size();
        int pages = Math.max(1, (size + ROWS - 1) / ROWS);
        if (right) {
            assignedPage = Math.floorMod(assignedPage + delta, pages);
            selectedAssigned = null;
        } else {
            availablePage = Math.floorMod(availablePage + delta, pages);
            selectedAvailable = null;
        }
        rebuildControls();
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        int x = panelX();
        int y = panelY();
        int panelW = panelWidth();
        int columnW = (panelW - 72) / 2;
        int rightX = x + panelW - 14 - columnW;
        graphics.fill(x, y, x + panelW, y + PANEL_HEIGHT, 0xF0101010);
        graphics.outline(x, y, panelW, PANEL_HEIGHT, WHITE);
        graphics.centeredText(font, title, width / 2, y + 10, WHITE);
        graphics.text(font, Component.translatable("gui.rallyguard.command_post.radius", snapshot.radius()), x + 350, y + 64, MUTED);
        graphics.text(font, Component.translatable("gui.rallyguard.command_post.available"), x + 14, y + 94, WHITE);
        graphics.text(font, Component.translatable("gui.rallyguard.command_post.assigned"), rightX, y + 94, WHITE);
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        drawGuardRows(graphics, available(), availablePage, x + 14, columnW, selectedAvailable);
        drawGuardRows(graphics, assigned(), assignedPage, rightX, columnW, selectedAssigned);
    }

    private void drawGuardRows(GuiGraphicsExtractor graphics, List<CommandPostSnapshotS2CPayload.GuardEntry> guards,
                               int page, int rowX, int rowW, UUID selected) {
        int start = page * ROWS;
        int end = Math.min(start + ROWS, guards.size());
        for (int i = start; i < end; i++) {
            CommandPostSnapshotS2CPayload.GuardEntry guard = guards.get(i);
            int rowY = panelY() + 112 + (i - start) * ROW_HEIGHT;
            int color = guard.guardId().equals(selected) ? GOLD : WHITE;
            graphics.text(font, Component.literal(trim(guard.name(), rowW - 12)), rowX + 6, rowY + 5, color);
            String detail = guard.squadName().isBlank()
                    ? Component.translatable("gui.rallyguard.command_post.no_squad").getString()
                    : guard.squadName();
            if (guard.assignedElsewhere()) detail += " · " + Component.translatable("gui.rallyguard.command_post.other_post").getString();
            graphics.text(font, Component.literal(trim(detail, rowW - 12)), rowX + 6, rowY + 18, MUTED);
        }
    }

    private String trim(String value, int maxWidth) {
        if (font.width(value) <= maxWidth) return value;
        return font.plainSubstrByWidth(value, Math.max(0, maxWidth - font.width("..."))) + "...";
    }

    private int panelWidth() {
        return Math.min(680, Math.max(540, width - 24));
    }

    private int panelX() {
        return (width - panelWidth()) / 2;
    }

    private int panelY() {
        return (height - PANEL_HEIGHT) / 2;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
