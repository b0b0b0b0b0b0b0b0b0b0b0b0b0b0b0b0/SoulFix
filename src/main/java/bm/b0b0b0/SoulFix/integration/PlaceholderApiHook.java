package bm.b0b0b0.soulFix.integration;

import bm.b0b0b0.soulFix.service.CooldownService;
import bm.b0b0b0.soulFix.service.SlotService;
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
        return switch (params.toLowerCase()) {
            case "base_slots" -> player.isOnline() && player.getPlayer() != null
                    ? String.valueOf(slotService.baseSlots(player.getPlayer()))
                    : "0";
            case "purchased_slots" -> String.valueOf(slotService.profile(playerId).join().purchasedSlots());
            case "max_purchased_slots" -> player.isOnline() && player.getPlayer() != null
                    ? String.valueOf(slotService.maxPurchasableSlots(player.getPlayer()))
                    : "0";
            case "total_slots" -> player.isOnline() && player.getPlayer() != null
                    ? String.valueOf(slotService.totalSlots(player.getPlayer()).join())
                    : String.valueOf(slotService.profile(playerId).join().purchasedSlots());
            case "cooldown" -> {
                long until = slotService.profile(playerId).join().cooldownUntilEpochMs();
                yield String.valueOf(cooldownService.remainingSeconds(playerId, until));
            }
            default -> "";
        };
    }
}
