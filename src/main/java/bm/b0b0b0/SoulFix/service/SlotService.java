package bm.b0b0b0.soulFix.service;

import bm.b0b0b0.soulFix.config.PluginConfig;
import bm.b0b0b0.soulFix.model.PlayerRepairProfile;
import bm.b0b0b0.soulFix.repository.PlayerProfileRepository;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.bukkit.entity.Player;

public final class SlotService {

    private final PluginConfig config;
    private final PlayerProfileRepository repository;

    public SlotService(PluginConfig config, PlayerProfileRepository repository) {
        this.config = config;
        this.repository = repository;
    }

    public int baseSlots(Player player) {
        int max = 0;
        for (Map.Entry<String, Integer> entry : config.slotTiers().entrySet()) {
            if (player.hasPermission(entry.getKey())) {
                max = Math.max(max, entry.getValue());
            }
        }
        if (max == 0 && player.hasPermission(config.permissionUse())) {
            max = 1;
        }
        return max;
    }

    public int maxPurchasableSlots(Player player) {
        int max = 0;
        for (Map.Entry<String, Integer> entry : config.purchaseLimitTiers().entrySet()) {
            if (player.hasPermission(entry.getKey())) {
                max = Math.max(max, entry.getValue());
            }
        }
        if (max == 0 && player.hasPermission(config.permissionUse())) {
            max = config.slotRowSize();
        }
        return max;
    }

    public int totalSlots(Player player, PlayerRepairProfile profile) {
        return Math.min(config.repairGridSlotCount(), baseSlots(player) + profile.purchasedSlots());
    }

    public CompletableFuture<PlayerRepairProfile> profile(UUID playerId) {
        return repository.find(playerId).thenApply(optional -> optional.orElseGet(() ->
                new PlayerRepairProfile(playerId, 0, 0L)));
    }

    public CompletableFuture<Integer> totalSlots(Player player) {
        return profile(player.getUniqueId()).thenApply(profile -> totalSlots(player, profile));
    }

    public CompletableFuture<PlayerRepairProfile> addPurchasedSlots(Player player, int amount) {
        int cap = maxPurchasableSlots(player);
        return addPurchasedSlots(player.getUniqueId(), amount, cap);
    }

    public CompletableFuture<PlayerRepairProfile> addPurchasedSlots(UUID playerId, int amount, int cap) {
        return profile(playerId).thenCompose(profile -> {
            int next = Math.min(cap, Math.max(0, profile.purchasedSlots() + amount));
            return repository.save(profile.withPurchasedSlots(next));
        });
    }

    public CompletableFuture<PlayerRepairProfile> setPurchasedSlots(UUID playerId, int amount) {
        return profile(playerId).thenCompose(profile -> repository.save(
                profile.withPurchasedSlots(Math.min(config.repairGridSlotCount(), Math.max(0, amount)))));
    }
}