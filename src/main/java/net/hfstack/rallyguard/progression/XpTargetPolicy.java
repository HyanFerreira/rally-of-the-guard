package net.hfstack.rallyguard.progression;

import dev.sterner.guardvillagers.common.entity.GuardEntity;
import net.hfstack.rallyguard.RallyOfTheGuard;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.Optional;

public final class XpTargetPolicy {
    public static final TagKey<EntityType<?>> XP_TARGETS = tag("xp_targets");
    public static final TagKey<EntityType<?>> NO_XP = tag("no_xp");
    public static final TagKey<EntityType<?>> COMMON = tag("xp/common");
    public static final TagKey<EntityType<?>> DANGEROUS = tag("xp/dangerous");
    public static final TagKey<EntityType<?>> RAID = tag("xp/raid");
    public static final TagKey<EntityType<?>> ELITE = tag("xp/elite");
    public static final TagKey<EntityType<?>> RAID_ELITE = tag("xp/raid_elite");
    public static final TagKey<EntityType<?>> BOSS = tag("xp/boss");

    private XpTargetPolicy() {
    }

    public static boolean isEligible(LivingEntity target) {
        return !(target instanceof Player)
                && !(target instanceof GuardEntity)
                && target.typeHolder().is(XP_TARGETS)
                && !target.typeHolder().is(NO_XP);
    }

    public static Optional<XpCategory> category(LivingEntity target) {
        if (!isEligible(target)) {
            return Optional.empty();
        }
        return Optional.of(highestCategory(
                target.typeHolder().is(COMMON),
                target.typeHolder().is(DANGEROUS),
                target.typeHolder().is(RAID),
                target.typeHolder().is(ELITE),
                target.typeHolder().is(RAID_ELITE),
                target.typeHolder().is(BOSS)
        ));
    }

    static XpCategory highestCategory(
            boolean common,
            boolean dangerous,
            boolean raid,
            boolean elite,
            boolean raidElite,
            boolean boss
    ) {
        if (boss) return XpCategory.BOSS;
        if (raidElite) return XpCategory.RAID_ELITE;
        if (elite) return XpCategory.ELITE;
        if (raid) return XpCategory.RAID;
        if (dangerous) return XpCategory.DANGEROUS;
        return XpCategory.COMMON;
    }

    private static TagKey<EntityType<?>> tag(String path) {
        return TagKey.create(
                Registries.ENTITY_TYPE,
                Identifier.fromNamespaceAndPath(RallyOfTheGuard.MOD_ID, path)
        );
    }
}
