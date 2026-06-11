package bm.b0b0b0.soulFix.config;

import bm.b0b0b0.soulFix.config.settings.GuiFixSettings;
import bm.b0b0b0.soulFix.config.settings.SoulFixSettings;
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

    public Map<String, Integer> slotTiers() {
        return main.slots.permissionTiers;
    }

    public Map<String, Integer> purchaseLimitTiers() {
        return main.slots.purchaseLimitTiers;
    }

    public int slotRowSize() {
        return main.slots.rowSize;
    }

    public int repairGridSlotCount() {
        return gui.repairSlots.size();
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

    public boolean placeholderApiEnabled() {
        return main.integrations.placeholderApiEnabled;
    }
}
