package net.hfstack.rallyguard.api.recruitment;

import java.util.Objects;
import java.util.UUID;

public record RecruitmentQuote(RecruitmentOffer offer, long fingerprint) {
    private static final long FNV_OFFSET_BASIS = 0xcbf29ce484222325L;
    private static final long FNV_PRIME = 0x100000001b3L;

    public RecruitmentQuote {
        Objects.requireNonNull(offer, "offer");
    }

    public static RecruitmentQuote create(UUID guardId, long totalXpMilli, RecruitmentOffer offer) {
        Objects.requireNonNull(guardId, "guardId");
        Objects.requireNonNull(offer, "offer");
        long hash = FNV_OFFSET_BASIS;
        hash = mix(hash, guardId.getMostSignificantBits());
        hash = mix(hash, guardId.getLeastSignificantBits());
        hash = mix(hash, totalXpMilli);
        hash = mix(hash, offer.cost());
        String itemId = offer.paymentItemId().toString();
        for (int index = 0; index < itemId.length(); index++) {
            hash ^= itemId.charAt(index);
            hash *= FNV_PRIME;
        }
        return new RecruitmentQuote(offer, hash);
    }

    private static long mix(long hash, long value) {
        for (int index = 0; index < Long.BYTES; index++) {
            hash ^= value & 0xffL;
            hash *= FNV_PRIME;
            value >>>= Byte.SIZE;
        }
        return hash;
    }
}
