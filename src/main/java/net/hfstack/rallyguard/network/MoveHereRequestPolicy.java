package net.hfstack.rallyguard.network;

import net.hfstack.rallyguard.api.command.GuardUnitCategory;
import net.minecraft.core.Direction;

import java.util.Optional;

public final class MoveHereRequestPolicy {
    public sealed interface Result permits Valid, Rejected {
    }

    public record Valid(GuardUnitCategory category, Direction face) implements Result {
    }

    public record Rejected() implements Result {
    }

    private MoveHereRequestPolicy() {
    }

    public static Result validate(
            int categoryId,
            int faceId,
            boolean scopeValid,
            boolean withinRange,
            boolean chunkLoaded,
            boolean withinBorder,
            boolean anchorSafe
    ) {
        Optional<GuardUnitCategory> category = GuardUnitCategory.fromWireId(categoryId);
        if (category.isEmpty() || faceId < 0 || faceId >= Direction.values().length
                || !scopeValid || !withinRange || !chunkLoaded || !withinBorder || !anchorSafe) {
            return new Rejected();
        }
        Direction face = Direction.from3DDataValue(faceId);
        if (face.get3DDataValue() != faceId) return new Rejected();
        return new Valid(category.get(), face);
    }
}
