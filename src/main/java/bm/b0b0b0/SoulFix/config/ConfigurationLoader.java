package bm.b0b0b0.SoulFix.config;

import bm.b0b0b0.SoulFix.config.settings.GuiFixSettings;
import bm.b0b0b0.SoulFix.config.settings.SoulFixSettings;
import java.nio.file.Path;
import org.bukkit.plugin.java.JavaPlugin;

public final class ConfigurationLoader {

    private final SoulFixSettings mainSettings = new SoulFixSettings();
    private final GuiFixSettings guiSettings = new GuiFixSettings();
    private PluginConfig pluginConfig;

    public PluginConfig load(JavaPlugin plugin) {
        SerializedConfigReloader.reload(plugin, mainSettings, Path.of("config.yml"));
        SerializedConfigReloader.reload(plugin, guiSettings, Path.of("gui", "general.yml"));
        pluginConfig = new PluginConfig(mainSettings, guiSettings);
        return pluginConfig;
    }

    public PluginConfig reload(JavaPlugin plugin) {
        return load(plugin);
    }

    public SoulFixSettings mainSettings() {
        return mainSettings;
    }

    public GuiFixSettings guiSettings() {
        return guiSettings;
    }

    public PluginConfig pluginConfig() {
        return pluginConfig;
    }
}
