package bm.b0b0b0.SoulFix.config;

import java.nio.file.Files;
import java.nio.file.Path;
import net.elytrium.serializer.language.object.YamlSerializable;
import org.bukkit.plugin.java.JavaPlugin;

public final class SerializedConfigReloader {

    private SerializedConfigReloader() {
    }

    public static void reload(JavaPlugin plugin, YamlSerializable settings, Path relativePath) {
        Path path = plugin.getDataFolder().toPath().resolve(relativePath);
        try {
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }
            settings.reload(path);
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to reload config at " + relativePath, exception);
        }
    }
}
