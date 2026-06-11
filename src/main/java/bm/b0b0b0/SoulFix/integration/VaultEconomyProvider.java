package bm.b0b0b0.SoulFix.integration;

import java.util.UUID;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

public final class VaultEconomyProvider implements EconomyProvider {

    private final String fallbackCurrencyLabel;
    private Economy economy;

    public VaultEconomyProvider(String fallbackCurrencyLabel) {
        this.fallbackCurrencyLabel = fallbackCurrencyLabel;
    }

    @Override
    public String id() {
        return "vault";
    }

    @Override
    public boolean hook() {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            economy = null;
            return false;
        }
        RegisteredServiceProvider<Economy> provider = Bukkit.getServicesManager().getRegistration(Economy.class);
        economy = provider == null ? null : provider.getProvider();
        return economy != null;
    }

    @Override
    public boolean available() {
        return economy != null;
    }

    @Override
    public double balance(UUID playerId) {
        return economy == null ? 0.0 : economy.getBalance(offline(playerId));
    }

    @Override
    public boolean has(UUID playerId, double amount) {
        return economy != null && economy.has(offline(playerId), amount);
    }

    @Override
    public boolean withdraw(UUID playerId, double amount) {
        return economy != null && economy.withdrawPlayer(offline(playerId), amount).transactionSuccess();
    }

    public String vaultCurrencyLabelOr(String fallback) {
        if (economy != null) {
            String plural = economy.currencyNamePlural();
            if (plural != null && !plural.isBlank()) {
                return plural;
            }
            String singular = economy.currencyNameSingular();
            if (singular != null && !singular.isBlank()) {
                return singular;
            }
        }
        return fallback;
    }

    @Override
    public String currencyLabel() {
        return vaultCurrencyLabelOr(fallbackCurrencyLabel);
    }
}
