package bm.b0b0b0.SoulFix.integration;

import java.util.UUID;
import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.Bukkit;

public final class PlayerPointsEconomyProvider implements EconomyProvider {

    private final String currencyLabel;
    private PlayerPointsAPI api;

    public PlayerPointsEconomyProvider(String currencyLabel) {
        this.currencyLabel = currencyLabel;
    }

    @Override
    public String id() {
        return "playerpoints";
    }

    @Override
    public boolean hook() {
        if (Bukkit.getPluginManager().getPlugin("PlayerPoints") instanceof PlayerPoints playerPoints) {
            api = playerPoints.getAPI();
            return true;
        }
        api = null;
        return false;
    }

    @Override
    public boolean available() {
        return api != null;
    }

    @Override
    public double balance(UUID playerId) {
        return api == null ? 0.0 : api.look(playerId);
    }

    @Override
    public boolean has(UUID playerId, double amount) {
        return api != null && api.look(playerId) >= amount;
    }

    @Override
    public boolean withdraw(UUID playerId, double amount) {
        return api != null && api.take(playerId, (int) amount);
    }

    @Override
    public String currencyLabel() {
        return currencyLabel;
    }
}
