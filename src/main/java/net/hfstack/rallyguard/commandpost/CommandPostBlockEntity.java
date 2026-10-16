package net.hfstack.rallyguard.commandpost;

import net.hfstack.rallyguard.RallyOfTheGuard;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.UUID;

public final class CommandPostBlockEntity extends BlockEntity {
    private CommandPostState state;
    private boolean futureSchema;

    public CommandPostBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.COMMAND_POST, pos, blockState);
        this.state = CommandPostState.create(UUID.randomUUID(), null);
    }

    public CommandPostState state() {
        return state;
    }

    public boolean hasFutureSchema() {
        return futureSchema;
    }

    public void claim(UUID ownerId) {
        if (futureSchema || ownerId == null || state.ownerId().isPresent()) return;
        state = state.withOwner(ownerId);
        setChanged();
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        CompoundTag stored = input.read("command_post", CompoundTag.CODEC).orElse(null);
        CommandPostStateCodec.ReadResult decoded = CommandPostStateCodec.decode(stored, state.postId());
        if (decoded instanceof CommandPostStateCodec.ReadResult.FutureSchema future) {
            futureSchema = true;
            RallyOfTheGuard.LOGGER.warn("Command post at {} uses unsupported schema {}", worldPosition, future.schemaVersion());
            return;
        }
        CommandPostStateCodec.ReadResult.Available available = (CommandPostStateCodec.ReadResult.Available) decoded;
        state = available.state();
        futureSchema = false;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (!futureSchema) output.store("command_post", CompoundTag.CODEC, CommandPostStateCodec.encode(state));
    }
}
