package bm.b0b0b0.SoulFix.integration;

import bm.b0b0b0.SoulFix.config.PluginConfig;
import bm.b0b0b0.SoulFix.config.settings.SoulFixSettings;

public final class SlotPurchasePricing {

    private SlotPurchasePricing() {
    }

    public static double nextSlotCost(PluginConfig config, boolean vaultActive, int alreadyPurchased) {
        return singleSlotCost(config, vaultActive, alreadyPurchased);
    }

    public static double batchCost(PluginConfig config, boolean vaultActive, int alreadyPurchased, int amount) {
        if (amount <= 0) {
            return 0.0;
        }
        double sum = 0.0;
        for (int index = 0; index < amount; index++) {
            sum += singleSlotCost(config, vaultActive, alreadyPurchased + index);
        }
        return sum;
    }

    private static double singleSlotCost(PluginConfig config, boolean vaultActive, int purchasedIndex) {
        double base = vaultActive ? config.economyVaultCost() : config.economyPlayerPointsCost();
        SoulFixSettings.EconomySettings.ScalingSettings scaling = config.economyScaling();
        if (!scaling.enabled || purchasedIndex <= 0) {
            return applyCap(base, base, scaling);
        }
        double multiplier;
        double rate = scaling.percentPerPurchasedSlot / 100.0;
        if (scaling.compound) {
            multiplier = Math.pow(1.0 + rate, purchasedIndex);
        } else {
            multiplier = 1.0 + rate * purchasedIndex;
        }
        multiplier = Math.min(multiplier, scaling.maxMultiplier);
        return applyCap(base * multiplier, base, scaling);
    }

    private static double applyCap(double cost, double base, SoulFixSettings.EconomySettings.ScalingSettings scaling) {
        double capped = Math.min(cost, base * scaling.maxMultiplier);
        if (scaling.maxCostCap > 0) {
            capped = Math.min(capped, scaling.maxCostCap);
        }
        return capped;
    }
}
