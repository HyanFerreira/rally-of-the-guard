package net.hfstack.rallyguard.api.command;

public record GuardBatchCommandResult(int succeeded, int rejected, int unavailable) {
    public GuardBatchCommandResult {
        if (succeeded < 0 || rejected < 0 || unavailable < 0) {
            throw new IllegalArgumentException("Batch counts cannot be negative");
        }
    }

    public int total() {
        return succeeded + rejected + unavailable;
    }
}
