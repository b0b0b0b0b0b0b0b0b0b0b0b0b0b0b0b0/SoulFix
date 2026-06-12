package bm.b0b0b0.SoulFix.integration;

import bm.b0b0b0.SoulFix.service.CooldownService;
import bm.b0b0b0.SoulFix.service.SlotService;
import java.util.UUID;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class PlaceholderApiHook extends PlaceholderExpansion {

    private final JavaPlugin plugin;
    private final SlotService slotService;
    private final CooldownService cooldownService;

    public PlaceholderApiHook(JavaPlugin plugin, SlotService slotService, CooldownService cooldownService) {
        this.plugin = plugin;
        this.slotService = slotService;
        this.cooldownService = cooldownService;
    }

    public void registerIfPresent() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            register();
        }
    }

    @Override
    public String getIdentifier() {
        return "soulfix";
    }

    @Override
    public String getAuthor() {
        return "b0b0b0";
    }

    @Override
    public String getVersion() {
        return plugin.getPluginMeta().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        if (player == null) {
            return "";
        }
        UUID playerId = player.getUniqueId();
        slotService.warmCache(playerId);
        return switch (params.toLowerCase()) {
            case "base_slots", "tier_slots" -> player.isOnline() && player.getPlayer() != null
                    ? String.valueOf(slotService.unlockedPermissionRowSlots(player.getPlayer()))
                    : "0";
            case "purchased_slots" -> String.valueOf(slotService.cachedProfile(playerId)
                    .map(profile -> profile.purchasedSlots())
                    .orElse(0));
            case "max_purchased_slots" -> player.isOnline() && player.getPlayer() != null
                    ? String.valueOf(slotService.maxBuyableSlots(player.getPlayer()))
                    : "0";
            case "total_slots" -> {
                if (player.isOnline() && player.getPlayer() instanceof Player online) {
                    yield String.valueOf(slotService.cachedProfile(playerId)
                            .map(profile -> slotService.totalSlots(online, profile))
                            .orElse(slotService.unlockedPermissionRowSlots(online)));
                }
                yield String.valueOf(slotService.cachedProfile(playerId)
                        .map(profile -> profile.purchasedSlots())
                        .orElse(0));
            }
            case "cooldown" -> {
                long until = slotService.cachedProfile(playerId)
                        .map(profile -> profile.cooldownUntilEpochMs())
                        .orElse(0L);
                yield String.valueOf(cooldownService.remainingSeconds(playerId, until));
            }
            default -> "";
        };
    }
}
