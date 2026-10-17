package net.hfstack.rallyguard.screen;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.hfstack.rallyguard.api.command.GuardCommandScope;
import net.hfstack.rallyguard.network.NetworkConstants;
import net.hfstack.rallyguard.network.payload.GuardListS2CPayload;
import net.hfstack.rallyguard.network.payload.GuardSquadAssignmentC2SPayload;
import net.hfstack.rallyguard.network.payload.SquadDefinitionC2SPayload;
import net.hfstack.rallyguard.network.payload.SquadOfficerC2SPayload;
import net.hfstack.rallyguard.screen.SquadScreenModel;
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
    private static final int MUTED = 0xFFAAAAAA;
    private static final int GOLD = 0xFFFFC94A;
    private static final int PANEL_HEIGHT = 320;
    private static final int MAX_PANEL_WIDTH = 700;
    private static final int ROW_HEIGHT = 32;
    private static final int SQUAD_TABS = 4;

    private GuardListS2CPayload snapshot;
    private final GuardCommandScope returnScope;
    private int selectedSquad;
    private int availablePage;
    private int memberPage;
    private UUID selectedAvailableGuard;
    private UUID selectedMemberGuard;
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
        snapshot = updated;
        selectedSquad = findSquadIndex(selectedId);
        clampSelection();
        retainValidSelections();
        clampPages();
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
        clampPages();
        int x = panelX();
        int y = panelY();
        int panelW = panelWidth();
        int columnW = columnWidth();
        int rightX = x + panelW - 14 - columnW;

        int renameW = 72;
        int newW = 52;
        int deleteW = 62;
        int nameW = Math.max(100, panelW - 28 - renameW - newW - deleteW - 12);
        nameBox = new EditBox(font, x + 14, y + 30, nameW, 20,
                Component.translatable("gui.rallyguard.squad.name"));
        nameBox.setMaxLength(48);
        selected().ifPresent(squad -> nameBox.setValue(squad.name()));
        addRenderableWidget(nameBox);

        Button rename = Button.builder(
                selected().isPresent()
                        ? Component.translatable("gui.rallyguard.squad.rename")
                        : Component.translatable("gui.rallyguard.squad.create"),
                button -> submitName()
        ).bounds(x + 18 + nameW, y + 30, renameW, 20).build();
        addRenderableWidget(rename);
        addRenderableWidget(Button.builder(Component.translatable("gui.rallyguard.squad.new"), button -> newSquad())
                .bounds(x + 22 + nameW + renameW, y + 30, newW, 20).build());
        Button delete = Button.builder(Component.translatable("gui.rallyguard.squad.delete"), button -> confirmDelete())
                .bounds(x + 26 + nameW + renameW + newW, y + 30, deleteW, 20).build();
        delete.active = selected().isPresent();
        addRenderableWidget(delete);

        Button previousSquad = Button.builder(Component.literal("<"), button -> changeSquad(-1))
                .bounds(x + 14, y + 58, 24, 20).build();
        previousSquad.active = !snapshot.squads().isEmpty() && selectedSquad > 0;
        addRenderableWidget(previousSquad);
        Button nextSquad = Button.builder(Component.literal(">"), button -> changeSquad(1))
                .bounds(x + 42, y + 58, 24, 20).build();
        nextSquad.active = selectedSquad >= 0 && selectedSquad < snapshot.squads().size() - 1;
        addRenderableWidget(nextSquad);
        int squadTabsY = y + (compactLayout() ? 54 : 58);
        previousSquad.setY(squadTabsY);
        nextSquad.setY(squadTabsY);
        addSquadTabs(x + 72, squadTabsY, panelW - 86);

        List<GuardListS2CPayload.Entry> available = availableGuards();
        List<GuardListS2CPayload.Entry> members = memberGuards();
        addGuardRows(available, availablePage, x + 14, columnW, selectedAvailableGuard,
                guard -> {
                    selectedAvailableGuard = guard.guardId();
                    selectedMemberGuard = null;
                    rebuildControls();
                });
        addGuardRows(members, memberPage, rightX, columnW, selectedMemberGuard,
                guard -> {
                    selectedMemberGuard = guard.guardId();
                    selectedAvailableGuard = null;
                    rebuildControls();
                });

        int transferX = x + panelW / 2 - 18;
        Button assign = Button.builder(Component.literal("→"), button -> assignSelected())
                .bounds(transferX, y + 139, 36, 20).build();
        assign.active = selected().isPresent() && selectedAvailable().isPresent();
        addRenderableWidget(assign);
        Button remove = Button.builder(Component.literal("←"), button -> removeSelected())
                .bounds(transferX, y + 167, 36, 20).build();
        remove.active = selected().isPresent() && selectedMember().isPresent();
        addRenderableWidget(remove);

        int bottom = y + panelHeight() - 28;
        addRenderableWidget(Button.builder(Component.literal("<"), button -> changeAvailablePage(-1))
                .bounds(x + 14, bottom, 24, 20).build());
        addRenderableWidget(Button.builder(Component.literal(">"), button -> changeAvailablePage(1))
                .bounds(x + 42, bottom, 24, 20).build());
        addRenderableWidget(Button.builder(Component.literal("<"), button -> changeMemberPage(-1))
                .bounds(rightX, bottom, 24, 20).build());
        addRenderableWidget(Button.builder(Component.literal(">"), button -> changeMemberPage(1))
                .bounds(rightX + 28, bottom, 24, 20).build());

        Button officer = Button.builder(Component.translatable(
                        selectedMember().filter(this::isOfficer).isPresent()
                                ? "gui.rallyguard.squad.clear_officer"
                                : "gui.rallyguard.squad.make_officer"),
                button -> toggleOfficer()).bounds(rightX + 58, bottom, Math.max(72, columnW - 142), 20).build();
        officer.active = selectedMember().filter(GuardListS2CPayload.Entry::captain).isPresent();
        addRenderableWidget(officer);
        addRenderableWidget(Button.builder(Component.translatable("gui.rallyguard.route.back"), button -> back())
                .bounds(x + panelW - 84, bottom, 70, 20).build());
    }

    private void addSquadTabs(int tabsX, int tabsY, int availableWidth) {
        int count = Math.min(SQUAD_TABS, snapshot.squads().size());
        if (count == 0) return;
        int selectedExisting = Math.min(selectedSquad, snapshot.squads().size() - 1);
        int start = Math.max(0, Math.min(selectedExisting - count + 1, snapshot.squads().size() - count));
        int gap = 4;
        int tabWidth = Math.max(48, (availableWidth - gap * (count - 1)) / count);
        for (int offset = 0; offset < count; offset++) {
            int index = start + offset;
            GuardListS2CPayload.SquadEntry squad = snapshot.squads().get(index);
            Button tab = Button.builder(Component.literal(squad.name()), button -> selectSquad(index))
                    .bounds(tabsX + offset * (tabWidth + gap), tabsY, tabWidth, 20).build();
            tab.active = index != selectedSquad;
            addRenderableWidget(tab);
        }
    }

    private void addGuardRows(List<GuardListS2CPayload.Entry> guards, int page, int rowX, int rowW,
                              UUID selectedGuard, java.util.function.Consumer<GuardListS2CPayload.Entry> onSelect) {
        int start = page * rowsPerPage();
        int end = Math.min(start + rowsPerPage(), guards.size());
        for (int i = start; i < end; i++) {
            GuardListS2CPayload.Entry guard = guards.get(i);
            int rowY = panelY() + listTop() + (i - start) * ROW_HEIGHT;
            Button row = Button.builder(Component.empty(), button -> onSelect.accept(guard))
                    .bounds(rowX, rowY, rowW, ROW_HEIGHT - 2).build();
            addRenderableWidget(row);
        }
    }

    private void submitName() {
        Optional<GuardListS2CPayload.SquadEntry> squad = selected();
        ClientPlayNetworking.send(new SquadDefinitionC2SPayload(
                squad.isPresent() ? NetworkConstants.SQUAD_RENAME : NetworkConstants.SQUAD_CREATE,
                squad.map(GuardListS2CPayload.SquadEntry::id), nameBox.getValue()
        ));
    }

    private void newSquad() {
        selectedSquad = snapshot.squads().size();
        selectedAvailableGuard = null;
        selectedMemberGuard = null;
        availablePage = 0;
        memberPage = 0;
        rebuildControls();
        nameBox.setValue("");
        setInitialFocus(nameBox);
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

    private void assignSelected() {
        Optional<GuardListS2CPayload.Entry> guard = selectedAvailable();
        Optional<GuardListS2CPayload.SquadEntry> squad = selected();
        if (guard.isEmpty() || squad.isEmpty()) return;
        GuardListS2CPayload.Entry entry = guard.get();
        if (entry.squadId().isPresent()) {
            String previous = squadName(entry.squadId()).orElse(Component.translatable("gui.rallyguard.squad.scope_unassigned").getString());
            Minecraft.getInstance().setScreen(new ConfirmScreen(
                    confirmed -> {
                        if (confirmed) sendAssignment(entry.entityId(), Optional.of(squad.get().id()));
                        Minecraft.getInstance().setScreen(this);
                    },
                    Component.translatable("gui.rallyguard.squad.move_confirm_title"),
                    Component.translatable("gui.rallyguard.squad.move_confirm", entry.name(), previous, squad.get().name())
            ));
            return;
        }
        sendAssignment(entry.entityId(), Optional.of(squad.get().id()));
    }

    private void removeSelected() {
        selectedMember().ifPresent(guard -> sendAssignment(guard.entityId(), Optional.empty()));
    }

    private void sendAssignment(int entityId, Optional<UUID> squadId) {
        ClientPlayNetworking.send(new GuardSquadAssignmentC2SPayload(entityId, squadId));
    }

    private void toggleOfficer() {
        Optional<GuardListS2CPayload.Entry> guard = selectedMember();
        selected().ifPresent(squad -> guard.ifPresent(entry -> ClientPlayNetworking.send(new SquadOfficerC2SPayload(
                squad.id(), isOfficer(entry) ? -1 : entry.entityId()
        ))));
    }

    private void changeSquad(int delta) {
        if (snapshot.squads().isEmpty()) return;
        selectedSquad = Math.max(0, Math.min(
                Math.min(selectedSquad, snapshot.squads().size() - 1) + delta,
                snapshot.squads().size() - 1
        ));
        resetSquadSelection();
        rebuildControls();
    }

    private void selectSquad(int index) {
        selectedSquad = Math.max(0, Math.min(index, snapshot.squads().size() - 1));
        resetSquadSelection();
        rebuildControls();
    }

    private void resetSquadSelection() {
        selectedAvailableGuard = null;
        selectedMemberGuard = null;
        availablePage = 0;
        memberPage = 0;
    }

    private void changeAvailablePage(int delta) {
        availablePage = cyclePage(availablePage, delta, availableGuards().size());
        selectedAvailableGuard = null;
        rebuildControls();
    }

    private void changeMemberPage(int delta) {
        memberPage = cyclePage(memberPage, delta, memberGuards().size());
        selectedMemberGuard = null;
        rebuildControls();
    }

    private int cyclePage(int page, int delta, int entries) {
        int pages = Math.max(1, (entries + rowsPerPage() - 1) / rowsPerPage());
        return Math.floorMod(page + delta, pages);
    }

    private List<GuardListS2CPayload.Entry> availableGuards() {
        return selected().map(squad -> new SquadScreenModel(snapshot, returnScope).outside(squad.id()))
                .orElse(snapshot.entries());
    }

    private List<GuardListS2CPayload.Entry> memberGuards() {
        return selected().map(squad -> new SquadScreenModel(snapshot, returnScope).members(squad.id()))
                .orElse(List.of());
    }

    private Optional<GuardListS2CPayload.Entry> selectedAvailable() {
        return findGuard(availableGuards(), selectedAvailableGuard);
    }

    private Optional<GuardListS2CPayload.Entry> selectedMember() {
        return findGuard(memberGuards(), selectedMemberGuard);
    }

    private Optional<GuardListS2CPayload.Entry> findGuard(List<GuardListS2CPayload.Entry> guards, UUID id) {
        if (id == null) return Optional.empty();
        return guards.stream().filter(guard -> guard.guardId().equals(id)).findFirst();
    }

    private Optional<String> squadName(Optional<UUID> squadId) {
        return new SquadScreenModel(snapshot, returnScope).squadName(squadId);
    }

    private boolean isOfficer(GuardListS2CPayload.Entry guard) {
        return selected().flatMap(GuardListS2CPayload.SquadEntry::officerGuardId)
                .filter(guard.guardId()::equals).isPresent();
    }

    private Optional<GuardListS2CPayload.SquadEntry> selected() {
        return selectedSquad >= 0 && selectedSquad < snapshot.squads().size()
                ? Optional.of(snapshot.squads().get(selectedSquad)) : Optional.empty();
    }

    private int findSquadIndex(UUID squadId) {
        if (squadId == null) return Math.min(selectedSquad, snapshot.squads().size());
        for (int i = 0; i < snapshot.squads().size(); i++) {
            if (snapshot.squads().get(i).id().equals(squadId)) return i;
        }
        return 0;
    }

    private void clampSelection() {
        selectedSquad = Math.max(0, Math.min(selectedSquad, snapshot.squads().size()));
    }

    private void clampPages() {
        availablePage = Math.min(availablePage, Math.max(0, (availableGuards().size() - 1) / rowsPerPage()));
        memberPage = Math.min(memberPage, Math.max(0, (memberGuards().size() - 1) / rowsPerPage()));
    }

    private void retainValidSelections() {
        if (selectedAvailable().isEmpty()) selectedAvailableGuard = null;
        if (selectedMember().isEmpty()) selectedMemberGuard = null;
    }

    private void back() {
        Minecraft.getInstance().setScreen(new GuardCommandScreen(snapshot, returnScope));
    }

    @Override
    public void onClose() {
        back();
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        int x = panelX();
        int y = panelY();
        int panelW = panelWidth();
        int columnW = columnWidth();
        int rightX = x + panelW - 14 - columnW;
        graphics.fill(x, y, x + panelW, y + panelHeight(), 0xF0101010);
        graphics.outline(x, y, panelW, panelHeight(), WHITE);
        graphics.centeredText(font, title, width / 2, y + 10, WHITE);

        String selectedName = selected().map(GuardListS2CPayload.SquadEntry::name)
                .orElseGet(() -> Component.translatable("gui.rallyguard.squad.new").getString());
        int headingY = y + (compactLayout() ? 80 : 91);
        graphics.text(font, Component.translatable("gui.rallyguard.squad.available"), x + 14, headingY, WHITE);
        graphics.text(font, Component.translatable("gui.rallyguard.squad.members", selectedName), rightX, headingY, WHITE);

        super.extractRenderState(graphics, mouseX, mouseY, delta);
        drawGuardRows(graphics, availableGuards(), availablePage, x + 14, columnW, selectedAvailableGuard, true);
        drawGuardRows(graphics, memberGuards(), memberPage, rightX, columnW, selectedMemberGuard, false);
    }

    private void drawGuardRows(GuiGraphicsExtractor graphics, List<GuardListS2CPayload.Entry> guards, int page,
                               int rowX, int rowW, UUID selectedGuard, boolean showSquad) {
        int start = page * rowsPerPage();
        int end = Math.min(start + rowsPerPage(), guards.size());
        for (int i = start; i < end; i++) {
            GuardListS2CPayload.Entry guard = guards.get(i);
            int rowY = panelY() + listTop() + (i - start) * ROW_HEIGHT;
            int color = guard.guardId().equals(selectedGuard) ? GOLD : WHITE;
            String captain = guard.captain()
                    ? " · " + Component.translatable("gui.rallyguard.muster.captain").getString() : "";
            graphics.text(font, Component.literal(trimToWidth(guard.name() + captain, rowW - 12)), rowX + 6, rowY + 5, color);
            String rank = guard.rank().map(Component::getString).orElse("");
            String squad = showSquad
                    ? squadName(guard.squadId()).orElse(Component.translatable("gui.rallyguard.squad.scope_unassigned").getString())
                    : Component.translatable("gui.rallyguard.squad.member").getString();
            String detail = rank.isBlank() ? squad : rank + " · " + squad;
            graphics.text(font, Component.literal(trimToWidth(detail, rowW - 12)), rowX + 6, rowY + 17, MUTED);
        }
    }

    private String trimToWidth(String text, int maxWidth) {
        return font.plainSubstrByWidth(text, Math.max(8, maxWidth));
    }

    private int panelWidth() {
        return Math.min(MAX_PANEL_WIDTH, Math.max(420, width - 24));
    }

    private int columnWidth() {
        return Math.max(150, (panelWidth() - 82) / 2);
    }

    private int panelX() {
        return (width - panelWidth()) / 2;
    }

    private int panelY() {
        return (height - panelHeight()) / 2;
    }

    private int panelHeight() {
        return Math.min(PANEL_HEIGHT, Math.max(220, height - 12));
    }

    private boolean compactLayout() {
        return panelHeight() < PANEL_HEIGHT;
    }

    private int rowsPerPage() {
        return compactLayout() ? 3 : 5;
    }

    private int listTop() {
        return compactLayout() ? 92 : 108;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
