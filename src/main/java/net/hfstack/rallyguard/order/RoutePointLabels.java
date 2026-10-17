package net.hfstack.rallyguard.order;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public final class RoutePointLabels {
    public static final int MAX_CODE_POINTS = 32;

    private RoutePointLabels() {
    }

    public static List<String> normalize(List<String> labels, int pointCount) {
        List<String> normalized = new ArrayList<>(pointCount);
        for (int i = 0; i < pointCount; i++) {
            String raw = labels != null && i < labels.size() && labels.get(i) != null ? labels.get(i) : "";
            String value = raw.trim();
            if (value.codePointCount(0, value.length()) > MAX_CODE_POINTS) {
                value = value.substring(0, value.offsetByCodePoints(0, MAX_CODE_POINTS));
            }
            normalized.add(value);
        }
        return List.copyOf(normalized);
    }

    public static String encode(List<String> labels, int pointCount) {
        Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();
        return String.join(".", normalize(labels, pointCount).stream()
                .map(label -> encoder.encodeToString(label.getBytes(StandardCharsets.UTF_8)))
                .toList());
    }

    public static List<String> decode(String encoded, int pointCount) {
        if (encoded == null) return normalize(List.of(), pointCount);
        String[] values = encoded.split("\\.", -1);
        List<String> labels = new ArrayList<>(pointCount);
        Base64.Decoder decoder = Base64.getUrlDecoder();
        for (int i = 0; i < pointCount; i++) {
            if (i >= values.length || values[i].isEmpty()) {
                labels.add("");
                continue;
            }
            labels.add(new String(decoder.decode(values[i]), StandardCharsets.UTF_8));
        }
        return normalize(labels, pointCount);
    }
}
