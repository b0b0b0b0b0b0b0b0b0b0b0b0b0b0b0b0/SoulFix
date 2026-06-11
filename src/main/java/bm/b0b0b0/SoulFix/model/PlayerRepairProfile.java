package bm.b0b0b0.soulFix.model;

import java.util.UUID;

public final class PlayerRepairProfile {

    private final UUID playerId;
    private final int purchasedSlots;
    private final long cooldownUntilEpochMs;

    public PlayerRepairProfile(UUID playerId, int purchasedSlots, long cooldownUntilEpochMs) {
        this.playerId = playerId;
        this.purchasedSlots = purchasedSlots;
        this.cooldownUntilEpochMs = cooldownUntilEpochMs;
    }

    public UUID playerId() {
        return playerId;
    }

    public int purchasedSlots() {
        return purchasedSlots;
    }

    public long cooldownUntilEpochMs() {
        return cooldownUntilEpochMs;
    }

    public PlayerRepairProfile withPurchasedSlots(int value) {
        return new PlayerRepairProfile(playerId, value, cooldownUntilEpochMs);
    }

    public PlayerRepairProfile withCooldownUntil(long value) {
        return new PlayerRepairProfile(playerId, purchasedSlots, value);
    }
}
