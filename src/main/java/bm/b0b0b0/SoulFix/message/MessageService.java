package bm.b0b0b0.SoulFix.message;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class MessageService {

    private final MessageLoader loader;
    private final Map<UUID, String> playerLocales = new HashMap<>();

    public MessageService(MessageLoader loader) {
        this.loader = loader;
    }

    public void setPlayerLocale(UUID playerId, String locale) {
        playerLocales.put(playerId, locale);
    }

    public String locale(Player player) {
        return playerLocales.getOrDefault(player.getUniqueId(), loader.defaultLocale());
    }

    public Component component(Player player, String key, String... pairs) {
        return component(locale(player), key, pairs);
    }

    public Component component(String locale, String key, String... pairs) {
        String prefix = loader.raw(locale, "prefix");
        String body = HexColorParser.replacePlaceholders(loader.raw(locale, key), pairs);
        return HexColorParser.parse(prefix + body);
    }

    public Component guiText(Player player, String key, String... pairs) {
        String body = HexColorParser.replacePlaceholders(loader.raw(locale(player), key), pairs);
        return HexColorParser.parse(body);
    }

    public List<Component> guiLore(Player player, String key, String... pairs) {
        return loader.lore(locale(player), key).stream()
                .map(line -> HexColorParser.parse(HexColorParser.replacePlaceholders(line, pairs)))
                .toList();
    }

    public List<Component> lore(Player player, String key, String... pairs) {
        return lore(locale(player), key, pairs);
    }

    public List<Component> lore(String locale, String key, String... pairs) {
        return loader.lore(locale, key).stream()
                .map(line -> HexColorParser.parse(HexColorParser.replacePlaceholders(line, pairs)))
                .toList();
    }

    public void send(Player player, String key, String... pairs) {
        player.sendMessage(component(player, key, pairs));
    }

    public void send(CommandSender sender, String key, String... pairs) {
        if (sender instanceof Player player) {
            send(player, key, pairs);
            return;
        }
        sender.sendMessage(HexColorParser.parse(
                HexColorParser.replacePlaceholders(loader.raw(loader.defaultLocale(), key), pairs)
        ));
    }

    public void reload() {
        loader.load();
    }
}
