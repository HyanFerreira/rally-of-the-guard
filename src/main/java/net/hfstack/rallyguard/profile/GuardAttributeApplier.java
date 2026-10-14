package net.hfstack.rallyguard.profile;

import dev.sterner.guardvillagers.common.entity.GuardEntity;
import net.hfstack.rallyguard.RallyOfTheGuard;
import net.hfstack.rallyguard.config.RallyConfig;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.Objects;

public final class GuardAttributeApplier {
    public static final Identifier NATURAL_HEALTH = id("natural_health");
    public static final Identifier NATURAL_ATTACK = id("natural_attack");
    public static final Identifier NATURAL_ARMOR = id("natural_armor");
    public static final Identifier NATURAL_SPEED = id("natural_speed");
    public static final Identifier LEVEL_HEALTH = id("level_health");
    public static final Identifier LEVEL_ATTACK = id("level_attack");
    public static final Identifier LEVEL_ARMOR = id("level_armor");

    private GuardAttributeApplier() {
    }

    public static void apply(
            GuardEntity guard,
            GuardProfile profile,
            GuardAttributeApplicationCause cause
    ) {
        Objects.requireNonNull(guard, "guard");
        Objects.requireNonNull(profile, "profile");
        Objects.requireNonNull(cause, "cause");
        GuardAttributePlan plan = GuardAttributePlan.from(
                profile,
                RallyConfig.progressionEnabled(),
                RallyConfig.naturalVariationEnabled(),
                RallyConfig.statGrowthEnabled()
        );
        apply(guard, plan, cause);
    }

    static void apply(
            GuardEntity guard,
            GuardAttributePlan plan,
            GuardAttributeApplicationCause cause
    ) {
        float oldMaximum = guard.getMaxHealth();
        float oldHealth = guard.getHealth();
        removeKnownModifiers(guard);

        if (plan.naturalVariationEnabled()) {
            add(guard.getAttribute(Attributes.MAX_HEALTH), NATURAL_HEALTH,
                    plan.naturalHealth(), AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
            add(guard.getAttribute(Attributes.ATTACK_DAMAGE), NATURAL_ATTACK,
                    plan.naturalAttack(), AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
            add(guard.getAttribute(Attributes.ARMOR), NATURAL_ARMOR,
                    plan.naturalArmor(), AttributeModifier.Operation.ADD_VALUE);
            add(guard.getAttribute(Attributes.MOVEMENT_SPEED), NATURAL_SPEED,
                    plan.naturalSpeed(), AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        }
        if (plan.statGrowthEnabled()) {
            add(guard.getAttribute(Attributes.MAX_HEALTH), LEVEL_HEALTH,
                    plan.levelHealth(), AttributeModifier.Operation.ADD_VALUE);
            add(guard.getAttribute(Attributes.ATTACK_DAMAGE), LEVEL_ATTACK,
                    plan.levelAttack(), AttributeModifier.Operation.ADD_VALUE);
            add(guard.getAttribute(Attributes.ARMOR), LEVEL_ARMOR,
                    plan.levelArmor(), AttributeModifier.Operation.ADD_VALUE);
        }

        guard.setHealth(GuardHealthAdjustment.adjustedHealth(
                oldHealth,
                oldMaximum,
                guard.getMaxHealth(),
                cause
        ));
    }

    public static void remove(GuardEntity guard, GuardAttributeApplicationCause cause) {
        Objects.requireNonNull(guard, "guard");
        Objects.requireNonNull(cause, "cause");
        float oldMaximum = guard.getMaxHealth();
        float oldHealth = guard.getHealth();
        removeKnownModifiers(guard);
        guard.setHealth(GuardHealthAdjustment.adjustedHealth(
                oldHealth,
                oldMaximum,
                guard.getMaxHealth(),
                cause
        ));
    }

    private static void removeKnownModifiers(GuardEntity guard) {
        remove(guard.getAttribute(Attributes.MAX_HEALTH), NATURAL_HEALTH);
        remove(guard.getAttribute(Attributes.ATTACK_DAMAGE), NATURAL_ATTACK);
        remove(guard.getAttribute(Attributes.ARMOR), NATURAL_ARMOR);
        remove(guard.getAttribute(Attributes.MOVEMENT_SPEED), NATURAL_SPEED);
        remove(guard.getAttribute(Attributes.MAX_HEALTH), LEVEL_HEALTH);
        remove(guard.getAttribute(Attributes.ATTACK_DAMAGE), LEVEL_ATTACK);
        remove(guard.getAttribute(Attributes.ARMOR), LEVEL_ARMOR);
    }

    private static void add(
            AttributeInstance attribute,
            Identifier id,
            double amount,
            AttributeModifier.Operation operation
    ) {
        if (attribute != null && amount != 0.0) {
            attribute.addOrReplacePermanentModifier(new AttributeModifier(id, amount, operation));
        }
    }

    private static void remove(AttributeInstance attribute, Identifier id) {
        if (attribute != null) {
            attribute.removeModifier(id);
        }
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(RallyOfTheGuard.MOD_ID, path);
    }
}
