package net.hfstack.rallyguard.screen;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.hfstack.rallyguard.api.command.GuardCommandScope;
import net.hfstack.rallyguard.network.NetworkConstants;
import net.hfstack.rallyguard.network.payload.GuardListS2CPayload;
import net.hfstack.rallyguard.network.payload.GuardSquadAssignmentC2SPayload;
import net.hfstack.rallyguard.network.payload.SquadDefinitionC2SPayload;
import net.hfstack.rallyguard.network.payload.SquadOfficerC2SPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class SquadManagementScreen extends Screen {
    private static final int WHITE = 0xFFFFFFFF;
    private static final int MUTED = 0xFFBBBBBB;
    private static final int PANEL_H = 270;
    private static final int ROWS = 5;
    private GuardListS2CPayload snapshot;
    private final GuardCommandScope returnScope;
    private int selectedSquad;
    private int memberPage;
    private EditBox nameBox;

    public SquadManagementScreen(GuardListS2CPayload snapshot, GuardCommandScope returnScope) {
        super(Component.translatable("gui.rallyguard.squad.manage_title"));
        this.snapshot = snapshot;
        this.returnScope = returnScope;
    }

    void acceptSnapshot(GuardListS2CPayload updated) {
        if (!updated.squadsEnabled()) {
            Minecraft.getInstance().setScreen(new GuardCommandScreen(updated));
            return;
        }
        UUID selectedId = selected().map(GuardListS2CPayload.SquadEntry::id).orElse(null);
        this.snapshot = updated;
        selectedSquad = 0;
        if (selectedId != null) {
            for (int i = 0; i < updated.squads().size(); i++) {
                if (updated.squads().get(i).id().equals(selectedId)) selectedSquad = i;
            }
        }
        clampSelection();
        memberPage = 0;
        rebuildControls();
    }

    @Override
    protected void init() {
        super.init();
        rebuildControls();
    }

    private void rebuildControls() {
        clearWidgets();
        clampSelection();
        int x = panelX();
        int y = panelY();
        int panelW = panelWidth();

        nameBox = new EditBox(font, x + 14, y + 30, Math.max(80, panelW - 112), 20,
                Component.translatable("gui.rallyguard.squad.name"));
        nameBox.setMaxLength(48);
        selected().ifPresent(squad -> nameBox.setValue(squad.name()));
        addRenderableWidget(nameBox);

        addRenderableWidget(Button.builder(
                selected().isPresent()
                        ? Component.translatable("gui.rallyguard.squad.rename")
                        : Component.translatable("gui.rallyguard.squad.create"),
                button -> submitName()
        ).bounds(x + panelW - 92, y + 30, 78, 20).build());

        addRenderableWidget(Button.builder(Component.literal("<"), button -> changeSquad(-1))
                .bounds(x + 14, y + 58, 24, 20).build());
        addRenderableWidget(Button.builder(Component.literal(">"), button -> changeSquad(1))
                .bounds(x + 42, y + 58, 24, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.rallyguard.squad.new"), button -> newSquad())
                .bounds(x + 72, y + 58, 60, 20).build());
        Button delete = Button.builder(Component.translatable("gui.rallyguard.squad.delete"), button -> confirmDelete())
                .bounds(x + 136, y + 58, 66, 20).build();
        delete.active = selected().isPresent();
        addRenderableWidget(delete);

        List<GuardListS2CPayload.Entry> guards = snapshot.entries();
        int start = memberPage * ROWS;
        int end = Math.min(start + ROWS, guards.size());
        for (int i = start; i < end; i++) {
            GuardListS2CPayload.Entry guard = guards.get(i);
            int rowY = y + 96 + (i - start) * 28;
            Optional<GuardListS2CPayload.SquadEntry> squad = selected();
            boolean member = squad.isPresent() && guard.squadId().filter(squad.get().id()::equals).isPresent();
            Button assignment = Button.builder(
                    Component.translatable(member
                            ? "gui.rallyguard.squad.unassign"
                            : "gui.rallyguard.squad.assign"),
                    button -> ClientPlayNetworking.send(new GuardSquadAssignmentC2SPayload(
                            guard.entityId(), member ? Optional.empty() : squad.map(GuardListS2CPayload.SquadEntry::id)
                    ))
            ).bounds(x + panelW - 154, rowY, 68, 18).build();
            assignment.active = squad.isPresent();
            addRenderableWidget(assignment);

            Button officer = Button.builder(
                    Component.translatable(isOfficer(guard)
                            ? "gui.rallyguard.squad.clear_officer"
                            : "gui.rallyguard.squad.make_officer"),
                    button -> squad.ifPresent(value -> ClientPlayNetworking.send(new SquadOfficerC2SPayload(
                            value.id(), isOfficer(guard) ? -1 : guard.entityId()
                    )))
            ).bounds(x + panelW - 82, rowY, 68, 18).build();
            officer.active = squad.isPresent() && member && guard.captain();
            addRenderableWidget(officer);
        }

        addRenderableWidget(Button.builder(Component.literal("<"), button -> changeMemberPage(-1))
                .bounds(x + 14, y + PANEL_H - 28, 24, 20).build());
        addRenderableWidget(Button.builder(Component.literal(">"), button -> changeMemberPage(1))
                .bounds(x + 42, y + PANEL_H - 28, 24, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.rallyguard.route.back"), button -> back())
                .bounds(x + panelW - 84, y + PANEL_H - 28, 70, 20).build());
    }

    private void submitName() {
        String name = nameBox.getValue();
        Optional<GuardListS2CPayload.SquadEntry> selected = selected();
        ClientPlayNetworking.send(new SquadDefinitionC2SPayload(
                selected.isPresent() ? NetworkConstants.SQUAD_RENAME : NetworkConstants.SQUAD_CREATE,
                selected.map(GuardListS2CPayload.SquadEntry::id),
                name
        ));
    }

    private void newSquad() {
        selectedSquad = snapshot.squads().size();
        nameBox.setValue("");
        rebuildControls();
    }

    private void confirmDelete() {
        selected().ifPresent(squad -> Minecraft.getInstance().setScreen(new ConfirmScreen(
                confirmed -> {
                    if (confirmed) ClientPlayNetworking.send(new SquadDefinitionC2SPayload(
                            NetworkConstants.SQUAD_DELETE, Optional.of(squad.id()), ""
                    ));
                    Minecraft.getInstance().setScreen(this);
                },
                Component.translatable("gui.rallyguard.squad.delete_confirm_title"),
                Component.translatable("gui.rallyguard.squad.delete_confirm", squad.name())
        )));
    }

    private void changeSquad(int delta) {
        if (snapshot.squads().isEmpty()) return;
        selectedSquad = Math.floorMod(selectedSquad + delta, snapshot.squads().size());
        memberPage = 0;
        rebuildControls();
    }

    private void changeMemberPage(int delta) {
        int pages = Math.max(1, (snapshot.entries().size() + ROWS - 1) / ROWS);
        memberPage = Math.floorMod(memberPage + delta, pages);
        rebuildControls();
    }

    private boolean isOfficer(GuardListS2CPayload.Entry guard) {
        return selected().flatMap(GuardListS2CPayload.SquadEntry::officerGuardId)
                .filter(guard.guardId()::equals).isPresent();
    }

    private Optional<GuardListS2CPayload.SquadEntry> selected() {
        return selectedSquad >= 0 && selectedSquad < snapshot.squads().size()
                ? Optional.of(snapshot.squads().get(selectedSquad)) : Optional.empty();
    }

    private void clampSelection() {
        selectedSquad = Math.max(0, Math.min(selectedSquad, snapshot.squads().size()));
    }

    private void back() {
        Minecraft.getInstance().setScreen(new GuardCommandScreen(snapshot, returnScope));
    }

    @Override
    public void onClose() { back(); }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        int x = panelX();
        int y = panelY();
        int panelW = panelWidth();
        graphics.fill(x, y, x + panelW, y + PANEL_H, 0xF0101010);
        graphics.fill(x, y, x + panelW, y + 1, WHITE);
        graphics.fill(x, y + PANEL_H - 1, x + panelW, y + PANEL_H, WHITE);
        graphics.fill(x, y, x + 1, y + PANEL_H, WHITE);
        graphics.fill(x + panelW - 1, y, x + panelW, y + PANEL_H, WHITE);
        graphics.centeredText(font, title, width / 2, y + 10, WHITE);
        String selectedName = selected().map(GuardListS2CPayload.SquadEntry::name)
                .orElseGet(() -> Component.translatable("gui.rallyguard.squad.new").getString());
        graphics.text(font, Component.literal(selectedName), x + 208, y + 64, MUTED);

        List<GuardListS2CPayload.Entry> guards = snapshot.entries();
        int start = memberPage * ROWS;
        int end = Math.min(start + ROWS, guards.size());
        for (int i = start; i < end; i++) {
            GuardListS2CPayload.Entry guard = guards.get(i);
            int rowY = y + 101 + (i - start) * 28;
            String suffix = guard.captain() ? " · " + Component.translatable("gui.rallyguard.muster.captain").getString() : "";
            graphics.text(font, Component.literal(guard.name() + suffix), x + 14, rowY, WHITE);
        }
        super.extractRenderState(graphics, mouseX, mouseY, delta);
    }

    private int panelWidth() { return Math.min(540, Math.max(300, width - 24)); }
    private int panelX() { return (width - panelWidth()) / 2; }
    private int panelY() { return (height - PANEL_H) / 2; }

    @Override
    public boolean isPauseScreen() { return false; }
}
