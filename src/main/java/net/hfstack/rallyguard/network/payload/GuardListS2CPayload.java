package net.hfstack.rallyguard.network.payload;

import net.hfstack.rallyguard.RallyOfTheGuard;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

/**
 * S2C: servidor envia lista de guardas (id, nome, patrulhando).
 */
public record GuardListS2CPayload(List<Entry> entries) implements CustomPayload {
    public record Entry(int entityId, String name, boolean patrolling) {
    }

    public static final Id<GuardListS2CPayload> ID =
            new Id<>(Identifier.of(RallyOfTheGuard.MOD_ID, "guard_list"));

    /**
     * Codec explícito (sem lambdas) para evitar inferência errada de tipos.
     */
    public static final PacketCodec<RegistryByteBuf, GuardListS2CPayload> CODEC =
            new PacketCodec<>() {
                @Override
                public void encode(RegistryByteBuf buf, GuardListS2CPayload value) {
                    List<Entry> list = value.entries();
                    buf.writeVarInt(list.size());
                    for (Entry e : list) {
                        buf.writeVarInt(e.entityId());
                        buf.writeString(e.name());
                        buf.writeBoolean(e.patrolling());
                    }
                }

                @Override
                public GuardListS2CPayload decode(RegistryByteBuf buf) {
                    int size = buf.readVarInt();
                    List<Entry> list = new ArrayList<>(size);
                    for (int i = 0; i < size; i++) {
                        int id = buf.readVarInt();
                        String name = buf.readString();
                        boolean patrolling = buf.readBoolean();
                        list.add(new Entry(id, name, patrolling));
                    }
                    return new GuardListS2CPayload(list);
                }
            };

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
