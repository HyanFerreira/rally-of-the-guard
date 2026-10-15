package net.hfstack.rallyguard.combat;

import dev.sterner.guardvillagers.common.entity.GuardEntity;
import net.hfstack.rallyguard.api.command.GuardUnitCategory;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

import java.util.Objects;

public final class GuardCombatRole {
    private static final Identifier BOW = Identifier.fromNamespaceAndPath("minecraft", "bow");
    private static final Identifier CROSSBOW = Identifier.fromNamespaceAndPath("minecraft", "crossbow");

    private GuardCombatRole() {
    }

    public static boolean isRanged(GuardEntity guard) {
        Objects.requireNonNull(guard, "guard");
        return isRanged(
                BuiltInRegistries.ITEM.getKey(guard.getMainHandItem().getItem()),
                BuiltInRegistries.ITEM.getKey(guard.getOffhandItem().getItem())
        );
    }

    public static boolean isRanged(Identifier mainHandItem, Identifier offHandItem) {
        return isRangedWeapon(mainHandItem) || isRangedWeapon(offHandItem);
    }

    public static boolean matches(GuardUnitCategory category, boolean ranged) {
        return switch (Objects.requireNonNull(category, "category")) {
            case ALL -> true;
            case INFANTRY -> !ranged;
            case RANGED -> ranged;
        };
    }

    public static boolean matches(GuardUnitCategory category, GuardEntity guard) {
        return matches(category, isRanged(guard));
    }

    private static boolean isRangedWeapon(Identifier itemId) {
        return BOW.equals(itemId) || CROSSBOW.equals(itemId);
    }
}
