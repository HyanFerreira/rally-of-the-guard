package net.hfstack.rallyguard.gametest;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.hfstack.rallyguard.event.RallyLifecycle;
import net.hfstack.rallyguard.order.defend.DefendAreaState;
import net.hfstack.rallyguard.order.defend.DefendAreas;
import net.hfstack.rallyguard.order.move.MoveOrderSlotResolver;
import net.hfstack.rallyguard.order.move.MoveOrderState;
import net.hfstack.rallyguard.order.move.MoveOrders;
import net.hfstack.rallyguard.order.GuardOrders;
import net.hfstack.rallyguard.order.GuardRouteState;
import net.hfstack.rallyguard.order.GuardRoutes;
import net.hfstack.rallyguard.order.temporary.GuardTaskSuspensions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Blocks;

import dev.sterner.guardvillagers.common.entity.GuardEntity;

import java.util.List;
import java.util.UUID;

public final class RallyGuardGameTests {
    @GameTest
    public void rallyCleanupReleasesOnlyTaggedParticipant(GameTestHelper helper) {
        EntityType<?> guardType = BuiltInRegistries.ENTITY_TYPE.getValue(
                Identifier.fromNamespaceAndPath("guardvillagers", "guard")
        );
        GuardEntity participant = spawnGuard(helper, guardType, new BlockPos(2, 1, 2));
        GuardEntity independentFollower = spawnGuard(helper, guardType, new BlockPos(4, 1, 2));
        UUID ownerId = UUID.randomUUID();
        participant.setOwnerId(ownerId);
        independentFollower.setOwnerId(ownerId);
        participant.setFollowing(true);
        independentFollower.setFollowing(true);
        GuardOrders.setRallied(participant, true);

        RallyLifecycle.stop(helper.getLevel().getServer(), ownerId);

        helper.assertTrue(!participant.isFollowing(), "Rally participant kept following after cleanup");
        helper.assertTrue(!GuardOrders.isRallied(participant), "Rally participant kept its Rally tag");
        helper.assertTrue(independentFollower.isFollowing(), "Cleanup stopped an independent follower");
        helper.assertTrue(!GuardOrders.isRallied(independentFollower), "Independent follower acquired a Rally tag");
        helper.succeed();
    }

    @GameTest
    public void moveAndDefendOrdersUseLiveEntityAttachments(GameTestHelper helper) {
        Entity entity = helper.spawn(EntityType.ARMOR_STAND, new BlockPos(2, 1, 2));
        BlockPos moveTarget = helper.absolutePos(new BlockPos(5, 1, 5));
        BlockPos defendCenter = helper.absolutePos(new BlockPos(4, 1, 4));
        BlockPos defendPost = helper.absolutePos(new BlockPos(3, 1, 4));

        helper.assertTrue(MoveOrders.set(entity, moveTarget), "Move Here order was rejected");
        MoveOrderState move = MoveOrders.active(entity).orElseThrow(
                () -> helper.assertionException("Move Here order was not attached to the entity")
        );
        helper.assertValueEqual(move.target(), moveTarget, "Move Here target");

        helper.assertTrue(DefendAreas.set(entity, defendCenter, defendPost, 12), "Defend Area order was rejected");
        DefendAreaState defend = DefendAreas.active(entity).orElseThrow(
                () -> helper.assertionException("Defend Area order was not attached to the entity")
        );
        helper.assertValueEqual(defend.center(), defendCenter, "Defend Area center");
        helper.assertValueEqual(defend.post(), defendPost, "Defend Area post");
        helper.assertValueEqual(defend.radius(), 12, "Defend Area radius");

        MoveOrders.clear(entity);
        DefendAreas.clear(entity);
        helper.assertTrue(MoveOrders.active(entity).isEmpty(), "Move Here order was not cleared");
        helper.assertTrue(DefendAreas.active(entity).isEmpty(), "Defend Area order was not cleared");
        helper.succeed();
    }

    @GameTest
    public void moveHereSlotRequiresSolidFloorAndTwoClearBlocks(GameTestHelper helper) {
        BlockPos relativeAnchor = new BlockPos(4, 2, 4);
        BlockPos absoluteAnchor = helper.absolutePos(relativeAnchor);

        helper.setBlock(relativeAnchor.below(), Blocks.STONE);
        helper.setBlock(relativeAnchor, Blocks.AIR);
        helper.setBlock(relativeAnchor.above(), Blocks.AIR);

        BlockPos resolved = MoveOrderSlotResolver.resolve(helper.getLevel(), absoluteAnchor, 0).orElseThrow(
                () -> helper.assertionException("Safe Move Here slot was not found")
        );
        helper.assertValueEqual(resolved, absoluteAnchor, "Safe Move Here slot");

        helper.setBlock(relativeAnchor.above(), Blocks.STONE);
        helper.assertTrue(
                MoveOrderSlotResolver.resolve(helper.getLevel(), absoluteAnchor, 0)
                        .filter(absoluteAnchor::equals)
                        .isEmpty(),
                "Move Here accepted a slot without two clear blocks"
        );
        helper.succeed();
    }

    @GameTest
    public void dismissRestoresTheRoutineBeforeTemporaryOrders(GameTestHelper helper) {
        EntityType<?> guardType = BuiltInRegistries.ENTITY_TYPE.getValue(
                Identifier.fromNamespaceAndPath("guardvillagers", "guard")
        );
        Entity spawned = helper.spawn(guardType, new BlockPos(2, 1, 2));
        if (!(spawned instanceof GuardEntity guard)) {
            throw helper.assertionException("Guard Villagers entity type did not spawn a GuardEntity");
        }

        BlockPos first = helper.absolutePos(new BlockPos(3, 1, 3));
        BlockPos second = helper.absolutePos(new BlockPos(6, 1, 6));
        GuardRouteState originalRoute = new GuardRouteState(true, 0, 60, 0, List.of(first, second));
        GuardRoutes.applyToGuard(guard, originalRoute);
        guard.setPatrolPos(first);
        guard.setPatrolling(true);

        helper.assertTrue(GuardTaskSuspensions.suspendIfAbsent(guard), "Original routine was not suspended");
        helper.assertTrue(!GuardTaskSuspensions.suspendIfAbsent(guard), "Temporary order overwrote the first snapshot");
        GuardRoutes.deactivate(guard);
        guard.setPatrolling(false);
        guard.setPatrolPos(null);
        guard.setFollowing(true);

        helper.assertTrue(GuardTaskSuspensions.restore(guard), "Dismiss did not restore the suspended routine");
        helper.assertTrue(!guard.isFollowing(), "Dismiss kept the temporary Follow order");
        helper.assertTrue(guard.isPatrolling(), "Dismiss did not restore patrol state");
        helper.assertValueEqual(guard.getPatrolPos(), first, "Restored patrol position");
        helper.assertValueEqual(GuardRoutes.get(guard), originalRoute, "Restored named/individual route state");
        helper.assertTrue(!GuardOrders.isWaiting(guard), "Dismiss incorrectly replaced the route with Wait");
        helper.assertTrue(!GuardTaskSuspensions.hasSuspendedTask(guard), "Dismiss left a stale snapshot");
        helper.succeed();
    }

    private static GuardEntity spawnGuard(GameTestHelper helper, EntityType<?> guardType, BlockPos position) {
        Entity spawned = helper.spawn(guardType, position);
        if (spawned instanceof GuardEntity guard) return guard;
        throw helper.assertionException("Guard Villagers entity type did not spawn a GuardEntity");
    }
}
