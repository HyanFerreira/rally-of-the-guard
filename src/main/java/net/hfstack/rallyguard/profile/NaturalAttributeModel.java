package net.hfstack.rallyguard.profile;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class NaturalAttributeModel {
    private NaturalAttributeModel() {
    }

    public static int quality(int firstSample, int secondSample) {
        requireRange(firstSample, 0, 10_000, "quality sample");
        requireRange(secondSample, 0, 10_000, "quality sample");
        return (firstSample + secondSample + 1) / 2;
    }

    public static int storedRoll(int quality, double firstNoise, double secondNoise) {
        requireRange(quality, 0, 10_000, "quality");
        requireNoise(firstNoise);
        requireNoise(secondNoise);
        double qualityBias = (quality - 5_000) / 5_000.0;
        double attributeNoise = (firstNoise + secondNoise) / 2.0;
        double roll = Math.max(-1.0, Math.min(1.0, 0.55 * qualityBias + 0.65 * attributeNoise));
        return roundHalfUp(roll * 10_000.0);
    }

    public static double mapSignedRoll(int storedRoll, double negativeBound, double positiveBound) {
        requireRange(storedRoll, -10_000, 10_000, "stored roll");
        if (!Double.isFinite(negativeBound)
                || !Double.isFinite(positiveBound)
                || negativeBound > 0
                || positiveBound < 0) {
            throw new IllegalArgumentException("Bounds must straddle zero");
        }
        double normalized = storedRoll / 10_000.0;
        return normalized < 0 ? -normalized * negativeBound : normalized * positiveBound;
    }

    private static int roundHalfUp(double value) {
        return BigDecimal.valueOf(value).setScale(0, RoundingMode.HALF_UP).intValueExact();
    }

    private static void requireNoise(double value) {
        if (!Double.isFinite(value) || value < -1.0 || value > 1.0) {
            throw new IllegalArgumentException("Noise must be finite and between -1 and 1");
        }
    }

    private static void requireRange(int value, int minimum, int maximum, String label) {
        if (value < minimum || value > maximum) {
            throw new IllegalArgumentException(label + " must be between " + minimum + " and " + maximum);
        }
    }
}
