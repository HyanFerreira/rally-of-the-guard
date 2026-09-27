package net.hfstack.rallyguard.event;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.hfstack.rallyguard.contract.GuardOwnership;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.AABB;

import java.util.*;

/**
 * Seus guardas (contratados) NÃO se voltam contra você quando
 * você bate em um guarda que NÃO é seu. Eles ficam neutros.
 */
public final class HiredGuardsNeutralityHandler {

    // ~10s ignorando o dono como alvo
    private static final int IGNORE_TICKS = 200;

    // Chave: "<dim>#<entityId>" -> (ownerUuid, ticksRestantes)
    private static final Map<String, Entry> TRACK = new HashMap<>();

    private record Entry(UUID owner, int ticks) {
    }

    private HiredGuardsNeutralityHandler() {
    }

    public static void register() {
        // Quando você acerta um guarda NÃO-contratado…
        AttackEntityCallback.EVENT.register((player, world, hand, target, hit) -> {
            if (world.isClientSide()) return net.minecraft.world.InteractionResult.PASS;
            if (!GuardOwnership.isGuard(target)) return net.minecraft.world.InteractionResult.PASS;

            // Se o alvo é um guarda seu, não mexe.
            if (GuardOwnership.isOwnedBy(target, player.getUUID())) {
                return net.minecraft.world.InteractionResult.PASS;
            }

            // Alvo é guarda, mas NÃO é seu -> neutraliza seus guardas próximos
            ServerLevel sw = (ServerLevel) world;

            var guardType = BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.fromNamespaceAndPath("guardvillagers", "guard"));
            double r = 24.0;
            AABB area = new AABB(
                    player.getX() - r, player.getY() - r, player.getZ() - r,
                    player.getX() + r, player.getY() + r, player.getZ() + r
            );

            List<? extends Entity> myGuards = sw.getEntities(
                    guardType,
                    area,
                    e -> GuardOwnership.isOwnedBy(e, player.getUUID())
            );

            for (Entity g : myGuards) {
                // Marca para ignorar o dono por IGNORE_TICKS
                track(sw, g.getId(), player.getUUID(), IGNORE_TICKS);

                // Se já estiver mirando o dono, limpa agora
                if (g instanceof Mob mob && mob.getTarget() != null
                        && mob.getTarget().getUUID().equals(player.getUUID())) {
                    mob.setTarget(null);
                }
            }

            return net.minecraft.world.InteractionResult.PASS; // não cancela seu ataque
        });

        // A cada tick, reforça a neutralidade por um tempo
        ServerTickEvents.END_LEVEL_TICK.register(sw -> {
            if (TRACK.isEmpty()) return;

            String dim = sw.dimension().identifier().toString();
            List<String> toRemove = new ArrayList<>();

            for (Map.Entry<String, Entry> en : TRACK.entrySet()) {
                String key = en.getKey();
                if (!key.startsWith(dim + "#")) continue;

                Entry val = en.getValue();
                int entityId = parseEntityId(key);
                if (entityId < 0) {
                    toRemove.add(key);
                    continue;
                }

                Entity e = sw.getEntity(entityId);
                if (e == null) {
                    toRemove.add(key);
                    continue;
                }

                if (!GuardOwnership.isGuard(e) || !GuardOwnership.isOwnedBy(e, val.owner())) {
                    toRemove.add(key);
                    continue;
                }

                if (e instanceof Mob mob && mob.getTarget() != null
                        && mob.getTarget().getUUID().equals(val.owner())) {
                    mob.setTarget(null);
                }

                int left = val.ticks() - 1;
                if (left <= 0) {
                    toRemove.add(key);
                } else {
                    TRACK.put(key, new Entry(val.owner(), left));
                }
            }

            for (String k : toRemove) TRACK.remove(k);
        });
    }

    // -- helpers --

    private static void track(ServerLevel world, int entityId, UUID owner, int ticks) {
        TRACK.put(key(world, entityId), new Entry(owner, ticks));
    }

    private static String key(ServerLevel world, int entityId) {
        return world.dimension().identifier().toString() + "#" + entityId;
    }

    private static int parseEntityId(String key) {
        int i = key.lastIndexOf('#');
        if (i < 0 || i + 1 >= key.length()) return -1;
        try {
            return Integer.parseInt(key.substring(i + 1));
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
