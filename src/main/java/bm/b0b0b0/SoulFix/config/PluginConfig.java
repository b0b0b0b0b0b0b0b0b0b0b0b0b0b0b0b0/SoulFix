package bm.b0b0b0.SoulFix.config;

import bm.b0b0b0.SoulFix.config.settings.GuiFixSettings;
import bm.b0b0b0.SoulFix.config.settings.SoulFixSettings;
import java.util.List;
import java.util.Map;
import org.bukkit.Material;

public final class PluginConfig {

    private final SoulFixSettings main;
    private final GuiFixSettings gui;

    public PluginConfig(SoulFixSettings main, GuiFixSettings gui) {
        this.main = main;
        this.gui = gui;
    }

    public SoulFixSettings main() {
        return main;
    }

    public GuiFixSettings gui() {
        return gui;
    }

    public String storageType() {
        return main.storageType;
    }

    public SoulFixSettings.StorageSettings storage() {
        return main.storage;
    }

    public String defaultLocale() {
        return main.locale.defaultLocale;
    }

    public String fallbackLocale() {
        return main.locale.fallbackLocale;
    }

    public String permissionUse() {
        return main.permissions.use;
    }

    public String permissionAdmin() {
        return main.permissions.admin;
    }

    public String permissionBypassCooldown() {
        return main.permissions.bypassCooldown;
    }

    public List<String> rowUnlockPermissions() {
        return main.slots.rowUnlockPermissions;
    }

    public Map<String, Integer> purchaseLimitTiers() {
        return main.slots.purchaseLimitTiers;
    }

    public Map<String, String> rowRankKeys() {
        return main.slots.rowRankKeys;
    }

    public int freePurchaseRowSlots() {
        return main.slots.freePurchaseRowSlots;
    }

    public int purchaseRowBuyCap() {
        return Math.max(0, purchaseRowSlots().size() - freePurchaseRowSlots());
    }

    public int purchaseRowIndex() {
        return gui.repairRows.size() - 1;
    }

    public List<Integer> repairRow(int index) {
        return gui.repairRows.get(index);
    }

    public List<Integer> purchaseRowSlots() {
        return gui.repairRows.get(purchaseRowIndex());
    }

    public int repairGridSlotCount() {
        return gui.repairRows.stream().mapToInt(List::size).sum();
    }

    public String economyMode() {
        return main.economy.mode;
    }

    public int economyPlayerPointsCost() {
        return main.economy.playerPointsCost;
    }

    public double economyVaultCost() {
        return main.economy.vaultCost;
    }

    public String economyCurrencyLabel() {
        return main.economy.currencyLabel;
    }

    public String economyCurrencyLabelSource() {
        return main.economy.currencyLabelSource;
    }

    public SoulFixSettings.EconomySettings.ScalingSettings economyScaling() {
        return main.economy.scaling;
    }

    public int cooldownBaseSeconds() {
        return main.cooldown.baseSeconds;
    }

    public int cooldownPerItemSeconds() {
        return main.cooldown.perItemSeconds;
    }

    public boolean repairRequireDamage() {
        return main.repair.requireDamage;
    }

    public List<String> blockedMaterials() {
        return main.repair.blockedMaterials;
    }

    public boolean isMaterialBlocked(Material material) {
        return main.repair.blockedMaterials.contains(material.name());
    }

    public SoulFixSettings.AnimationSettings animation() {
        return main.animation;
    }

    public SoulFixSettings.PurchaseCelebrationSettings purchaseCelebration() {
        return main.purchaseCelebration;
    }

    public boolean placeholderApiEnabled() {
        return main.integrations.placeholderApiEnabled;
    }
}
