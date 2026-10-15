package net.hfstack.rallyguard.formation;

import java.util.Objects;

public final class FormationLayouts {
    private static final double FIRST_ROW_OFFSET = 1.8;
    private static final double SHIELD_WALL_DEPTH = 2.3;
    private static final double SHIELD_WALL_SPACING_FACTOR = 0.75;

    private FormationLayouts() {
    }

    public static FormationOffset slot(
            RallyFormation formation,
            int index,
            int total,
            double columnSpacing,
            double rowSpacing
    ) {
        Objects.requireNonNull(formation, "formation");
        if (index < 0) throw new IllegalArgumentException("index must be non-negative");
        if (!Double.isFinite(columnSpacing) || columnSpacing <= 0.0) {
            throw new IllegalArgumentException("columnSpacing must be positive and finite");
        }
        if (!Double.isFinite(rowSpacing) || rowSpacing <= 0.0) {
            throw new IllegalArgumentException("rowSpacing must be positive and finite");
        }
        int groupSize = Math.max(total, index + 1);

        return switch (formation) {
            case ESCORT -> escort(index, columnSpacing, rowSpacing);
            case COLUMN -> column(index, rowSpacing);
            case LINE -> line(index, groupSize, columnSpacing, FIRST_ROW_OFFSET);
            case WEDGE -> wedge(index, columnSpacing, rowSpacing);
            case SQUARE -> square(index, groupSize, columnSpacing, rowSpacing);
            case CIRCLE -> circle(index, groupSize, columnSpacing, rowSpacing);
            case SHIELD_WALL -> line(
                    index,
                    groupSize,
                    columnSpacing * SHIELD_WALL_SPACING_FACTOR,
                    SHIELD_WALL_DEPTH
            );
        };
    }

    private static FormationOffset escort(int index, double columnSpacing, double rowSpacing) {
        int row = index / 2;
        double side = index % 2 == 0 ? -1.0 : 1.0;
        return new FormationOffset(
                side * columnSpacing,
                FIRST_ROW_OFFSET + row * rowSpacing
        );
    }

    private static FormationOffset column(int index, double rowSpacing) {
        return new FormationOffset(0.0, FIRST_ROW_OFFSET + index * rowSpacing);
    }

    private static FormationOffset line(int index, int total, double spacing, double depth) {
        return new FormationOffset((index - (total - 1) / 2.0) * spacing, depth);
    }

    private static FormationOffset wedge(int index, double columnSpacing, double rowSpacing) {
        if (index == 0) return new FormationOffset(0.0, FIRST_ROW_OFFSET);
        int row = (index + 1) / 2;
        double side = index % 2 == 1 ? -1.0 : 1.0;
        return new FormationOffset(
                side * row * columnSpacing,
                FIRST_ROW_OFFSET + row * rowSpacing
        );
    }

    private static FormationOffset square(
            int index,
            int total,
            double columnSpacing,
            double rowSpacing
    ) {
        int columns = (int) Math.ceil(Math.sqrt(total));
        int row = index / columns;
        int column = index % columns;
        int rowStart = row * columns;
        int rowSize = Math.min(columns, total - rowStart);
        return new FormationOffset(
                (column - (rowSize - 1) / 2.0) * columnSpacing,
                FIRST_ROW_OFFSET + row * rowSpacing
        );
    }

    private static FormationOffset circle(
            int index,
            int total,
            double columnSpacing,
            double rowSpacing
    ) {
        double desiredSpacing = Math.max(columnSpacing, rowSpacing);
        double radius = Math.max(FIRST_ROW_OFFSET, desiredSpacing * total / (2.0 * Math.PI));
        double angle = 2.0 * Math.PI * index / total;
        return new FormationOffset(Math.sin(angle) * radius, Math.cos(angle) * radius);
    }
}
