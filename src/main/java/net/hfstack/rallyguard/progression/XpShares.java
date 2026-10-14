package net.hfstack.rallyguard.progression;

public record XpShares(double contribution, double finalBlow) {
    public static XpShares normalize(double contribution, double finalBlow) {
        double safeContribution = finiteNonNegative(contribution);
        double safeFinalBlow = finiteNonNegative(finalBlow);
        double total = safeContribution + safeFinalBlow;
        if (!(total > 0.0) || !Double.isFinite(total)) {
            return new XpShares(0.80, 0.20);
        }
        return new XpShares(safeContribution / total, safeFinalBlow / total);
    }

    private static double finiteNonNegative(double value) {
        return Double.isFinite(value) ? Math.max(0.0, value) : 0.0;
    }
}
