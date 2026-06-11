package bm.b0b0b0.SoulFix.integration;

import bm.b0b0b0.SoulFix.config.PluginConfig;
import java.util.Locale;
public final class SlotEconomyManager {

    private final PluginConfig config;
    private final PlayerPointsEconomyProvider playerPoints;
    private final VaultEconomyProvider vault;
    private EconomyProvider active;

    public SlotEconomyManager(PluginConfig config) {
        this.config = config;
        this.playerPoints = new PlayerPointsEconomyProvider(config.economyCurrencyLabel());
        this.vault = new VaultEconomyProvider(config.economyCurrencyLabel());
    }

    public EconomyHookStatus hook() {
        String mode = config.economyMode().toLowerCase(Locale.ROOT);
        playerPoints.hook();
        vault.hook();
        active = null;

        if ("playerpoints".equals(mode)) {
            return selectRequired(playerPoints, "mode=playerpoints, PlayerPoints not available");
        }

        if ("vault".equals(mode)) {
            return selectRequired(vault, "mode=vault, Vault economy not available");
        }

        if (playerPoints.available()) {
            active = playerPoints;
            return availableStatus("PlayerPoints", "auto → PlayerPoints");
        }

        if (vault.available()) {
            active = vault;
            return availableStatus("Vault", "auto → Vault (PlayerPoints missing)");
        }

        return unavailableStatus("auto: no PlayerPoints or Vault economy");
    }

    private EconomyHookStatus selectRequired(EconomyProvider preferred, String failureDetail) {
        if (preferred.available()) {
            active = preferred;
            return availableStatus(preferred.id(), preferred.id());
        }
        return unavailableStatus(failureDetail);
    }

    private EconomyHookStatus availableStatus(String providerId, String message) {
        return new EconomyHookStatus(true, providerId, "Economy: " + message + " (slot shop enabled)", false);
    }

    private EconomyHookStatus unavailableStatus(String detail) {
        return new EconomyHookStatus(false, "none", "Economy: " + detail + " (slot shop disabled)", true);
    }

    public boolean isShopAvailable() {
        return active != null && active.available();
    }

    public String activeProviderId() {
        return active == null ? "none" : active.id();
    }

    public String currencyLabel() {
        String configured = config.economyCurrencyLabel();
        if (active == null) {
            return configured;
        }
        String source = config.economyCurrencyLabelSource().toLowerCase(Locale.ROOT);
        if ("vault".equals(source) && active instanceof VaultEconomyProvider vaultProvider) {
            return vaultProvider.vaultCurrencyLabelOr(configured);
        }
        if ("playerpoints".equals(source) && active instanceof PlayerPointsEconomyProvider) {
            return configured;
        }
        return configured;
    }

    public boolean usesVaultPricing() {
        return active instanceof VaultEconomyProvider;
    }

    public double unitCost() {
        return SlotPurchasePricing.nextSlotCost(config, usesVaultPricing(), 0);
    }

    public double nextSlotCost(int alreadyPurchased) {
        return SlotPurchasePricing.nextSlotCost(config, usesVaultPricing(), alreadyPurchased);
    }

    public double totalCost(int alreadyPurchased, int amount) {
        return SlotPurchasePricing.batchCost(config, usesVaultPricing(), alreadyPurchased, amount);
    }

    public String formatCost(double cost) {
        if (cost == Math.rint(cost)) {
            return String.valueOf((long) Math.rint(cost));
        }
        return String.format(Locale.US, "%.2f", cost);
    }

    public boolean withdraw(java.util.UUID playerId, int alreadyPurchased, int slotAmount) {
        if (!isShopAvailable()) {
            return false;
        }
        double cost = chargeAmount(alreadyPurchased, slotAmount);
        if (!active.has(playerId, cost)) {
            return false;
        }
        return active.withdraw(playerId, cost);
    }

    public boolean hasFunds(java.util.UUID playerId, int alreadyPurchased, int slotAmount) {
        return isShopAvailable() && active.has(playerId, chargeAmount(alreadyPurchased, slotAmount));
    }

    private double chargeAmount(int alreadyPurchased, int slotAmount) {
        double cost = totalCost(alreadyPurchased, slotAmount);
        if (active instanceof PlayerPointsEconomyProvider) {
            return Math.ceil(cost);
        }
        return cost;
    }
}
