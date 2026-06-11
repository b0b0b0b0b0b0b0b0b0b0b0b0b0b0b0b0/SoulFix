package bm.b0b0b0.soulFix.service;

import bm.b0b0b0.soulFix.config.PluginConfig;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

public final class RepairItemValidator {

    private final PluginConfig config;

    public RepairItemValidator(PluginConfig config) {
        this.config = config;
    }

    public boolean isRepairable(ItemStack itemStack) {
        if (itemStack == null || itemStack.getType().isAir()) {
            return false;
        }
        Material material = itemStack.getType();
        if (config.isMaterialBlocked(material)) {
            return false;
        }
        if (itemStack.getType().getMaxDurability() <= 0) {
            return false;
        }
        if (!config.repairRequireDamage()) {
            return true;
        }
        ItemMeta meta = itemStack.getItemMeta();
        if (!(meta instanceof Damageable damageable)) {
            return false;
        }
        return damageable.getDamage() > 0;
    }

    public ItemStack repair(ItemStack source) {
        ItemStack repaired = source.clone();
        ItemMeta meta = repaired.getItemMeta();
        if (meta instanceof Damageable damageable) {
            damageable.setDamage(0);
            repaired.setItemMeta(meta);
        }
        return repaired;
    }
}
