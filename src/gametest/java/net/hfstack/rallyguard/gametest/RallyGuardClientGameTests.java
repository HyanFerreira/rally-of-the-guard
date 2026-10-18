package net.hfstack.rallyguard.gametest;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.hfstack.rallyguard.client.quick.QuickCommandAction;
import net.hfstack.rallyguard.api.command.GuardCommandScope;
import net.hfstack.rallyguard.api.command.GuardUnitCategory;
import net.hfstack.rallyguard.formation.RallyFormation;
import net.hfstack.rallyguard.network.payload.GuardListS2CPayload;
import net.hfstack.rallyguard.screen.FormationSelectionScreen;
import net.hfstack.rallyguard.screen.GuardCommandScreen;
import net.hfstack.rallyguard.screen.QuickCommandScreen;
import net.hfstack.rallyguard.screen.ScopedNamedRouteScreen;
import net.hfstack.rallyguard.screen.SquadManagementScreen;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@SuppressWarnings("UnstableApiUsage")
public final class RallyGuardClientGameTests implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        formationHubFitsNormalAndSmallWindows(context);
        quickCommandExposesAllActions(context);
        ledgerAndSquadScreensExposeTheNewWorkflows(context);
    }

    private static void formationHubFitsNormalAndSmallWindows(ClientGameTestContext context) {
        context.getInput().resizeWindow(854, 480);
        context.setScreen(() -> new FormationSelectionScreen(0, RallyFormation.WEDGE));
        context.waitForScreen(FormationSelectionScreen.class);
        assertScreen(context, FormationSelectionScreen.class, 7, false);
        assertWidgetsInsideScreen(context);
        context.takeScreenshot("formation-hub-normal");

        context.getInput().resizeWindow(320, 240);
        context.waitTicks(2);
        assertScreen(context, FormationSelectionScreen.class, 7, false);
        assertWidgetsInsideScreen(context);
        context.takeScreenshot("formation-hub-small");

        context.runOnClient(client -> client.screen.onClose());
        context.waitFor(client -> !(client.screen instanceof FormationSelectionScreen));
    }

    private static void quickCommandExposesAllActions(ClientGameTestContext context) {
        context.getInput().resizeWindow(854, 480);
        context.waitTicks(2);
        context.setScreen(QuickCommandScreen::new);
        context.waitForScreen(QuickCommandScreen.class);
        assertScreen(context, QuickCommandScreen.class, 7, false);
        assertWidgetsInsideScreen(context);

        List<String> labels = context.computeOnClient(client -> client.screen.children().stream()
                .filter(AbstractWidget.class::isInstance)
                .map(AbstractWidget.class::cast)
                .map(widget -> widget.getMessage().getString())
                .toList());
        for (QuickCommandAction action : QuickCommandAction.values()) {
            assertContains(
                    labels,
                    Component.translatable(action.translationKey()).getString(),
                    "Quick Command " + action.name() + " action"
            );
        }
        context.takeScreenshot("quick-command-normal");

        context.runOnClient(client -> client.screen.onClose());
        context.waitFor(client -> !(client.screen instanceof QuickCommandScreen));
    }

    private static void ledgerAndSquadScreensExposeTheNewWorkflows(ClientGameTestContext context) {
        GuardListS2CPayload snapshot = commandSnapshot();
        context.getInput().resizeWindow(854, 480);
        context.waitTicks(2);
        context.setScreen(() -> new GuardCommandScreen(snapshot));
        context.waitForScreen(GuardCommandScreen.class);
        assertScreen(context, GuardCommandScreen.class, 20, false);
        assertWidgetsInsideScreen(context);
        assertWidgetLabelsFit(context);
        context.takeScreenshot("command-ledger-categories-dismiss-routes");

        context.setScreen(() -> new SquadManagementScreen(snapshot, GuardCommandScope.all()));
        context.waitForScreen(SquadManagementScreen.class);
        assertScreen(context, SquadManagementScreen.class, 18, false);
        assertWidgetsInsideScreen(context);
        assertWidgetLabelsFit(context);
        context.takeScreenshot("squad-direct-selection");

        context.setScreen(() -> new ScopedNamedRouteScreen(
                new GuardCommandScreen(snapshot), snapshot, GuardCommandScope.all()
        ));
        context.waitForScreen(ScopedNamedRouteScreen.class);
        assertScreen(context, ScopedNamedRouteScreen.class, 5, false);
        assertWidgetsInsideScreen(context);
        assertWidgetLabelsFit(context);
        context.takeScreenshot("scoped-named-route-selection");

        context.runOnClient(client -> client.screen.onClose());
        context.waitForScreen(GuardCommandScreen.class);
        context.runOnClient(client -> client.screen.onClose());
        context.waitFor(client -> !(client.screen instanceof GuardCommandScreen));
    }

    private static GuardListS2CPayload commandSnapshot() {
        UUID alphaId = UUID.fromString("5eb9059a-54f0-469d-9f09-a6dc22014586");
        UUID bravoId = UUID.fromString("c6132130-f485-4c04-9d44-d662a47da31e");
        UUID routeOne = UUID.fromString("46a96cf0-f676-45b3-b067-c7442923ecf4");
        UUID routeTwo = UUID.fromString("2598ec5b-cbf4-4d60-a1ee-7f8cc56d4f4e");
        List<GuardListS2CPayload.SquadEntry> squads = List.of(
                new GuardListS2CPayload.SquadEntry(alphaId, "Alpha", Optional.empty()),
                new GuardListS2CPayload.SquadEntry(bravoId, "Bravo", Optional.empty())
        );
        List<GuardListS2CPayload.NamedRouteEntry> routes = List.of(
                new GuardListS2CPayload.NamedRouteEntry(
                        routeOne, "Muralha", "minecraft:overworld", 5,
                        List.of(new GuardListS2CPayload.Point(1, 64, 1), new GuardListS2CPayload.Point(8, 64, 8))
                ),
                new GuardListS2CPayload.NamedRouteEntry(
                        routeTwo, "Portões", "minecraft:overworld", 10,
                        List.of(new GuardListS2CPayload.Point(3, 64, 3), new GuardListS2CPayload.Point(12, 64, 3))
                )
        );
        List<GuardListS2CPayload.Entry> guards = List.of(
                new GuardListS2CPayload.Entry(
                        1, UUID.fromString("f8426126-fd0f-4e79-aee9-c9cb51c33e62"), "Afonso", false, 1,
                        false, 5, List.of(), Optional.of(Component.literal("Veterano")), Optional.empty(),
                        Optional.of(alphaId), Optional.empty(), true, GuardUnitCategory.INFANTRY.wireId(), true
                ),
                new GuardListS2CPayload.Entry(
                        2, UUID.fromString("075c907d-3150-4609-a2c2-45db8fa775e6"), "Beatriz", false, 0,
                        false, 5, List.of(), Optional.of(Component.literal("Soldado")), Optional.empty(),
                        Optional.of(bravoId), Optional.empty(), false, GuardUnitCategory.RANGED.wireId(), false
                )
        );
        return new GuardListS2CPayload(true, squads, routes, guards);
    }

    private static void assertScreen(
            ClientGameTestContext context,
            Class<? extends Screen> expectedType,
            int expectedWidgets,
            boolean expectedPause
    ) {
        context.runOnClient(client -> {
            Screen screen = client.screen;
            if (!expectedType.isInstance(screen)) {
                throw new AssertionError("Expected " + expectedType.getSimpleName() + ", got " + screen);
            }
            if (screen.children().size() != expectedWidgets) {
                throw new AssertionError("Expected " + expectedWidgets + " widgets, got " + screen.children().size());
            }
            if (screen.isPauseScreen() != expectedPause) {
                throw new AssertionError("Unexpected pause behavior for " + expectedType.getSimpleName());
            }
        });
    }

    private static void assertWidgetsInsideScreen(ClientGameTestContext context) {
        context.runOnClient(client -> {
            Screen screen = client.screen;
            for (var child : screen.children()) {
                if (!(child instanceof AbstractWidget widget)) continue;
                if (widget.getX() < 0 || widget.getY() < 0
                        || widget.getX() + widget.getWidth() > screen.width
                        || widget.getY() + widget.getHeight() > screen.height) {
                    throw new AssertionError("Widget outside screen bounds: " + widget.getMessage().getString());
                }
            }
        });
    }

    private static void assertWidgetLabelsFit(ClientGameTestContext context) {
        context.runOnClient(client -> {
            Screen screen = client.screen;
            for (var child : screen.children()) {
                if (!(child instanceof AbstractWidget widget)) continue;
                int availableWidth = Math.max(0, widget.getWidth() - 8);
                if (client.font.width(widget.getMessage()) > availableWidth) {
                    throw new AssertionError("Widget label is clipped: " + widget.getMessage().getString());
                }
            }
        });
    }

    private static void assertContains(List<String> labels, String expected, String description) {
        if (!labels.contains(expected)) {
            throw new AssertionError(description + " is missing. Labels: " + labels);
        }
    }
}
