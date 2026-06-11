package bm.b0b0b0.soulFix.service;

import bm.b0b0b0.soulFix.config.PluginConfig;
import bm.b0b0b0.soulFix.model.PlayerRepairProfile;
import bm.b0b0b0.soulFix.repository.PlayerProfileRepository;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.bukkit.entity.Player;

public final class CooldownService {

    private final PluginConfig config;
    private final PlayerProfileRepository repository;
    private final SlotService slotService;

    public CooldownService(PluginConfig config, PlayerProfileRepository repository, SlotService slotService) {
        this.config = config;
        this.repository = repository;
        this.slotService = slotService;
    }

    public boolean bypasses(Player player) {
        return player.hasPermission(config.permissionBypassCooldown());
    }

    public long remainingSeconds(Player player) {
        return remainingSeconds(player.getUniqueId(), 0);
    }

    public long remainingSeconds(UUID playerId, long storedCooldownUntil) {
        long now = System.currentTimeMillis();
        if (storedCooldownUntil <= now) {
            return 0L;
        }
        return (storedCooldownUntil - now + 999L) / 1000L;
    }

    public CompletableFuture<Boolean> isOnCooldown(Player player) {
        if (bypasses(player)) {
            return CompletableFuture.completedFuture(false);
        }
        return slotService.profile(player.getUniqueId()).thenApply(profile ->
                remainingSeconds(player.getUniqueId(), profile.cooldownUntilEpochMs()) > 0L);
    }

    public CompletableFuture<Long> remainingSecondsAsync(Player player) {
        return slotService.profile(player.getUniqueId()).thenApply(profile ->
                remainingSeconds(player.getUniqueId(), profile.cooldownUntilEpochMs()));
    }

    public int durationSeconds(int itemCount) {
        return config.cooldownBaseSeconds() + Math.max(0, itemCount) * config.cooldownPerItemSeconds();
    }

    public CompletableFuture<PlayerRepairProfile> applyCooldown(UUID playerId, int itemCount) {
        long until = System.currentTimeMillis() + durationSeconds(itemCount) * 1000L;
        return slotService.profile(playerId).thenCompose(profile -> repository.save(profile.withCooldownUntil(until)));
    }

    public CompletableFuture<PlayerRepairProfile> resetCooldown(UUID playerId) {
        return slotService.profile(playerId).thenCompose(profile -> repository.save(profile.withCooldownUntil(0L)));
    }
}
