package bm.b0b0b0.SoulFix.service;

import bm.b0b0b0.SoulFix.integration.SlotEconomyManager;
import bm.b0b0b0.SoulFix.message.MessageService;
import bm.b0b0b0.SoulFix.model.PlayerRepairProfile;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class SlotPurchaseService {

    private final JavaPlugin plugin;
    private final SlotService slotService;
    private final SlotEconomyManager economyManager;
    private final MessageService messageService;
    private final Set<UUID> purchasing = ConcurrentHashMap.newKeySet();

    public SlotPurchaseService(
            JavaPlugin plugin,
            SlotService slotService,
            SlotEconomyManager economyManager,
            MessageService messageService
    ) {
        this.plugin = plugin;
        this.slotService = slotService;
        this.economyManager = economyManager;
        this.messageService = messageService;
    }

    public boolean isShopAvailable() {
        return economyManager.isShopAvailable();
    }

    public CompletableFuture<Boolean> buyOne(Player player) {
        return purchase(player, 1);
    }

    public CompletableFuture<Boolean> buyFive(Player player) {
        return purchase(player, 5);
    }

    public CompletableFuture<Boolean> buyTen(Player player) {
        return purchase(player, 10);
    }

    public CompletableFuture<Boolean> buyMax(Player player) {
        return slotService.profile(player.getUniqueId()).thenCompose(profile -> {
            int remaining = remainingPurchasable(player, profile.purchasedSlots());
            if (remaining <= 0) {
                notifyLimit(player, profile.purchasedSlots());
                return CompletableFuture.completedFuture(false);
            }
            return purchase(player, remaining);
        });
    }

    private CompletableFuture<Boolean> purchase(Player player, int amount) {
        if (amount <= 0 || !player.isOnline()) {
            return CompletableFuture.completedFuture(false);
        }
        if (!isShopAvailable()) {
            notify(player, "slots.shop-unavailable", "provider", economyManager.activeProviderId());
            return CompletableFuture.completedFuture(false);
        }
        UUID playerId = player.getUniqueId();
        if (!purchasing.add(playerId)) {
            return CompletableFuture.completedFuture(false);
        }
        return slotService.profile(playerId).thenCompose(profile -> {
            int purchased = profile.purchasedSlots();
            int maxCanBuy = remainingPurchasable(player, purchased);
            int actual = Math.min(amount, maxCanBuy);
            if (actual <= 0) {
                notifyLimit(player, purchased);
                return CompletableFuture.completedFuture(false);
            }
            double cost = economyManager.totalCost(purchased, actual);
            if (!economyManager.hasFunds(playerId, purchased, actual)) {
                notify(
                        player,
                        "slots.not-enough-points",
                        "cost",
                        economyManager.formatCost(cost),
                        "currency",
                        economyManager.currencyLabel()
                );
                return CompletableFuture.completedFuture(false);
            }
            CompletableFuture<Boolean> withdrawFuture = new CompletableFuture<>();
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (!player.isOnline()) {
                    withdrawFuture.complete(false);
                    return;
                }
                withdrawFuture.complete(economyManager.withdraw(playerId, purchased, actual));
            });
            return withdrawFuture.thenCompose(withdrawn -> {
                if (!withdrawn) {
                    notify(
                            player,
                            "slots.not-enough-points",
                            "cost",
                            economyManager.formatCost(cost),
                            "currency",
                            economyManager.currencyLabel()
                    );
                    return CompletableFuture.completedFuture(false);
                }
                int chargedFrom = purchased;
                int chargedAmount = actual;
                return slotService.addPurchasedSlots(player, actual).thenApply(saved -> {
                    notify(
                            player,
                            "slots.purchase-success",
                            "amount",
                            String.valueOf(chargedAmount),
                            "purchased_slots",
                            String.valueOf(saved.purchasedSlots()),
                            "max_purchased",
                            String.valueOf(slotService.maxBuyableSlots(player)),
                            "total_slots",
                            String.valueOf(slotService.totalSlots(player, saved))
                    );
                    return true;
                }).exceptionally(throwable -> {
                    Bukkit.getScheduler().runTask(plugin, () ->
                            economyManager.refund(playerId, chargedFrom, chargedAmount));
                    notify(player, "slots.purchase-failed");
                    return false;
                });
            });
        }).exceptionally(throwable -> {
            notify(player, "error.database");
            return false;
        }).whenComplete((result, throwable) -> purchasing.remove(playerId));
    }

    private int remainingPurchasable(Player player, int purchased) {
        return Math.max(0, slotService.maxBuyableSlots(player) - purchased);
    }

    private void notifyLimit(Player player, int purchased) {
        notify(
                player,
                "slots.max-reached",
                "purchased_slots",
                String.valueOf(purchased),
                "max_purchased",
                String.valueOf(slotService.maxBuyableSlots(player))
        );
    }

    private void notify(Player player, String key, String... pairs) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (player.isOnline()) {
                messageService.send(player, key, pairs);
            }
        });
    }
}
