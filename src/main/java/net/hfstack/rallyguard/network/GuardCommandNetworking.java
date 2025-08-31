package net.hfstack.rallyguard.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.hfstack.rallyguard.contract.GuardOwnership;
import net.hfstack.rallyguard.network.payload.GuardActionC2SPayload;
import net.hfstack.rallyguard.network.payload.GuardListS2CPayload;
import net.hfstack.rallyguard.network.payload.OpenGuardCommandC2SPayload;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public final class GuardCommandNetworking {
    private GuardCommandNetworking() {
    }

    // 🔒 Anti-duplicado (o ModInitializer roda no client também)
    private static boolean REGISTERED = false;

    public static synchronized void registerServer() {
        if (REGISTERED) return;
        REGISTERED = true;

        // C2S: abrir GUI → servidor envia lista
        ServerPlayNetworking.registerGlobalReceiver(OpenGuardCommandC2SPayload.ID, (payload, context) -> {
            ServerPlayerEntity player = context.player();
            context.server().execute(() -> sendGuardList(player));
        });

        // C2S: ação (teleportar / patrulhar)
        ServerPlayNetworking.registerGlobalReceiver(GuardActionC2SPayload.ID, (payload, context) -> {
            ServerPlayerEntity player = context.player();
            int entityId = payload.entityId();
            int action = payload.action();
            context.server().execute(() -> handleAction(player, entityId, action));
        });
    }

    private static void sendGuardList(ServerPlayerEntity player) {
        ServerWorld world = player.getServerWorld();
        Identifier guardTypeId = Identifier.of("guardvillagers", "guard");

        List<? extends Entity> guards = world.getEntitiesByType(
                Registries.ENTITY_TYPE.get(guardTypeId),
                e -> GuardOwnership.isOwnedBy(e, player.getUuid())
        );

        List<GuardListS2CPayload.Entry> list = new ArrayList<>(guards.size());
        for (Entity g : guards) {
            String name = g.getName().getString();
            boolean patrolling = readBool(g, "Patrolling");
            list.add(new GuardListS2CPayload.Entry(g.getId(), name, patrolling));
        }

        ServerPlayNetworking.send(player, new GuardListS2CPayload(list));
    }

    private static void handleAction(ServerPlayerEntity player, int entityId, int action) {
        ServerWorld world = player.getServerWorld();
        Entity e = world.getEntityById(entityId);

        if (e == null || !GuardOwnership.isGuard(e)) {
            player.sendMessage(Text.translatable("gui.rallyguard.command.not_found"), true);
            return;
        }
        if (!GuardOwnership.isOwnedBy(e, player.getUuid())) {
            player.sendMessage(Text.translatable("gui.rallyguard.command.not_owner"), true);
            return;
        }

        switch (action) {
            case NetworkConstants.ACTION_SUMMON -> {
                double ox = (player.getRandom().nextDouble() - 0.5) * 2.5;
                double oz = (player.getRandom().nextDouble() - 0.5) * 2.5;
                e.refreshPositionAndAngles(player.getX() + ox, player.getY(), player.getZ() + oz, e.getYaw(), e.getPitch());
                writeBool(e, "rallyguard:in_rally", false);
                player.sendMessage(Text.translatable("gui.rallyguard.command.summoned"), true);
            }
            case NetworkConstants.ACTION_TOGGLE_PATROL -> {
                boolean patrolling = readBool(e, "Patrolling");
                if (patrolling) {
                    // DESLIGAR patrulha
                    NbtCompound n = new NbtCompound();
                    e.writeNbt(n);
                    n.putBoolean("Patrolling", false);
                    n.putBoolean("Following", false);
                    n.putBoolean("rallyguard:in_rally", false);

                    // (opcional) limpar o ponto salvo da patrulha
                    n.remove("PatrolPosX");
                    n.remove("PatrolPosY");
                    n.remove("PatrolPosZ");
                    n.remove("PatrolPos");
                    n.remove("PatrolPosL");

                    e.readNbt(n);

                    if (e instanceof MobEntity mob) {
                        mob.setTarget(null);
                        mob.setAttacking(false);
                        mob.getNavigation().stop();
                    }
                    player.sendMessage(Text.translatable("gui.rallyguard.command.patrol_off"), true);

                } else {
                    // LIGAR patrulha: marca o ponto na POSIÇÃO ATUAL DO PLAYER e deixa o guard ir andando
                    var bp = player.getBlockPos();

                    NbtCompound n = new NbtCompound();
                    e.writeNbt(n);

                    n.putBoolean("Following", false);
                    n.putBoolean("rallyguard:in_rally", false);
                    n.putBoolean("Patrolling", true);

                    // ✅ chaves que o teu port realmente usa:
                    n.putInt("PatrolPosX", bp.getX());
                    n.putInt("PatrolPosY", bp.getY());
                    n.putInt("PatrolPosZ", bp.getZ());

                    // (compat extras — inofensivas, ajudam em outros ports)
                    n.putIntArray("PatrolPos", new int[]{bp.getX(), bp.getY(), bp.getZ()});
                    n.putLong("PatrolPosL", bp.asLong());

                    e.readNbt(n);

                    if (e instanceof MobEntity mob) {
                        // zera qualquer caminho antigo
                        mob.setTarget(null);
                        mob.setAttacking(false);
                        mob.getNavigation().stop();
                    }

                    // “debounce” no tick seguinte só pra garantir que não fique preso num path antigo
                    player.getServer().execute(() -> {
                        if (e instanceof MobEntity mob2) mob2.getNavigation().stop();
                    });

                    player.sendMessage(Text.translatable("gui.rallyguard.command.patrol_on"), true);
                }
            }
        }
    }

    // --- NBT helpers ---
    private static boolean readBool(Entity e, String key) {
        NbtCompound n = new NbtCompound();
        e.writeNbt(n);
        return n.getBoolean(key);
    }

    private static void writeBool(Entity e, String key, boolean v) {
        NbtCompound n = new NbtCompound();
        e.writeNbt(n);
        n.putBoolean(key, v);
        e.readNbt(n);
    }
}
