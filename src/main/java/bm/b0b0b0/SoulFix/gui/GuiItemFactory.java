package bm.b0b0b0.SoulFix.gui;

import bm.b0b0b0.SoulFix.config.settings.GuiFixSettings;
import bm.b0b0b0.SoulFix.message.MessageService;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public final class GuiItemFactory {

    private final MessageService messageService;

    public GuiItemFactory(MessageService messageService) {
        this.messageService = messageService;
    }

    public ItemStack build(Player player, GuiFixSettings.GuiElementSettings element, String... pairs) {
        Material material = parseMaterial(element.material);
        ItemStack itemStack = ItemStack.of(material);
        ItemMeta meta = itemStack.getItemMeta();
        if (!element.nameKey.isEmpty()) {
            meta.displayName(messageService.guiText(player, element.nameKey, pairs));
        }
        if (!element.loreKeys.isEmpty()) {
            List<Component> lore = new ArrayList<>();
            for (String loreKey : element.loreKeys) {
                lore.addAll(messageService.guiLore(player, loreKey, pairs));
            }
            meta.lore(lore);
        }
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);
        itemStack.setItemMeta(meta);
        return itemStack;
    }

    public ItemStack filler(Player player, GuiFixSettings.GuiElementSettings element) {
        return build(player, element);
    }

    private Material parseMaterial(String name) {
        try {
            return Material.valueOf(name.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            return Material.STONE;
        }
    }
}
