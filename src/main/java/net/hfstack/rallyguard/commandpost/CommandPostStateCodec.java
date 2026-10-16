package net.hfstack.rallyguard.commandpost;

import net.hfstack.rallyguard.formation.RallyFormation;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class CommandPostStateCodec {
    private CommandPostStateCodec() {
    }

    public sealed interface ReadResult permits ReadResult.Available, ReadResult.FutureSchema {
        record Available(CommandPostState state, boolean repaired) implements ReadResult {
        }

        record FutureSchema(int schemaVersion) implements ReadResult {
        }
    }

    public static CompoundTag encode(CommandPostState state) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("schema_version", state.schemaVersion());
        tag.putString("post_id", state.postId().toString());
        state.ownerId().ifPresent(owner -> tag.putString("owner_id", owner.toString()));
        tag.putString("name", state.name());
        tag.putBoolean("active", state.active());
        tag.putInt("radius", state.radius());
        tag.putString("formation", state.formation().id());
        ListTag guards = new ListTag();
        state.assignedGuardIds().forEach(id -> guards.add(StringTag.valueOf(id.toString())));
        tag.put("assigned_guards", guards);
        return tag;
    }

    public static ReadResult decode(CompoundTag tag, UUID fallbackPostId) {
        if (tag == null) return availableDefault(fallbackPostId, true);
        int schema = strictInt(tag.get("schema_version")).orElse(0);
        if (schema > CommandPostState.SCHEMA_VERSION) return new ReadResult.FutureSchema(schema);

        Repair repair = new Repair(schema != CommandPostState.SCHEMA_VERSION);
        UUID postId = parseUuid(tag.getString("post_id").orElse(null)).orElseGet(() -> {
            repair.mark();
            return fallbackPostId;
        });
        Optional<UUID> owner = tag.getString("owner_id").flatMap(CommandPostStateCodec::parseUuid);
        if (tag.get("owner_id") != null && owner.isEmpty()) repair.mark();
        String name = tag.getString("name").orElse("Posto de Comando");
        boolean active = tag.getBoolean("active").orElse(true);
        int radius = strictInt(tag.get("radius")).orElse(CommandPostState.DEFAULT_RADIUS);
        RallyFormation formation = RallyFormation.findById(tag.getString("formation").orElse(null)).orElseGet(() -> {
            repair.mark();
            return RallyFormation.SQUARE;
        });
        List<UUID> guards = decodeGuards(tag.get("assigned_guards"), repair);

        CommandPostState state = new CommandPostState(
                CommandPostState.SCHEMA_VERSION, postId, owner, name, active, radius, formation, guards
        );
        if (!state.name().equals(name) || state.radius() != radius || state.assignedGuardIds().size() != guards.size()) repair.mark();
        return new ReadResult.Available(state, repair.value);
    }

    private static ReadResult.Available availableDefault(UUID postId, boolean repaired) {
        return new ReadResult.Available(CommandPostState.create(postId, null), repaired);
    }

    private static List<UUID> decodeGuards(Tag stored, Repair repair) {
        if (!(stored instanceof ListTag list)) {
            if (stored != null) repair.mark();
            return List.of();
        }
        List<UUID> guards = new ArrayList<>();
        for (Tag value : list) {
            if (guards.size() >= CommandPostState.MAX_ASSIGNED_GUARDS) {
                repair.mark();
                break;
            }
            if (!(value instanceof StringTag string) || parseUuid(string.value()).filter(id -> !guards.contains(id)).map(guards::add).isEmpty()) {
                repair.mark();
            }
        }
        return guards;
    }

    private static Optional<Integer> strictInt(Tag value) {
        return value instanceof NumericTag number && value.getId() == Tag.TAG_INT
                ? Optional.of(number.intValue())
                : Optional.empty();
    }

    private static Optional<UUID> parseUuid(String value) {
        if (value == null) return Optional.empty();
        try {
            return Optional.of(UUID.fromString(value));
        } catch (IllegalArgumentException exception) {
            return Optional.empty();
        }
    }

    private static final class Repair {
        private boolean value;

        private Repair(boolean value) {
            this.value = value;
        }

        private void mark() {
            value = true;
        }
    }
}
