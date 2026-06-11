package bm.b0b0b0.SoulFix.integration;

import java.util.UUID;
import org.bukkit.OfflinePlayer;

public interface EconomyProvider {

    String id();

    boolean hook();

    boolean available();

    double balance(UUID playerId);

    boolean has(UUID playerId, double amount);

    boolean withdraw(UUID playerId, double amount);

    boolean deposit(UUID playerId, double amount);

    String currencyLabel();

    default OfflinePlayer offline(UUID playerId) {
        return org.bukkit.Bukkit.getOfflinePlayer(playerId);
    }
}
