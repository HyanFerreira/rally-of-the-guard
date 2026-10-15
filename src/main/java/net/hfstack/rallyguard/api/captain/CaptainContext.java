package net.hfstack.rallyguard.api.captain;

import dev.sterner.guardvillagers.common.entity.GuardEntity;
import net.hfstack.rallyguard.api.profile.GuardProfileView;
import net.minecraft.server.level.ServerPlayer;

import java.util.Objects;
import java.util.Optional;

public record CaptainContext(
        ServerPlayer player,
        GuardEntity guard,
        CaptainAction action,
        GuardProfileView profile,
        Optional<CaptainOffer> offer
) {
    public CaptainContext {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(guard, "guard");
        Objects.requireNonNull(action, "action");
        Objects.requireNonNull(profile, "profile");
        offer = offer == null ? Optional.empty() : offer;
    }
}
