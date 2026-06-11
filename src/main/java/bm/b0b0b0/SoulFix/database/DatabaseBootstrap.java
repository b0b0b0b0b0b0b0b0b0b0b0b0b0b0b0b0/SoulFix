package bm.b0b0b0.soulFix.database;

import bm.b0b0b0.soulFix.config.PluginConfig;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import org.bukkit.plugin.java.JavaPlugin;

public final class DatabaseBootstrap {

    private final JavaPlugin plugin;
    private final PluginConfig config;
    private final ExecutorService executor = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "SoulFix-Database");
        thread.setDaemon(true);
        return thread;
    });
    private DataSourceProvider provider;

    public DatabaseBootstrap(JavaPlugin plugin, PluginConfig config) {
        this.plugin = plugin;
        this.config = config;
    }

    public CompletableFuture<DataSourceProvider> start() {
        return CompletableFuture.supplyAsync(() -> {
            provider = new DataSourceProvider(plugin, config);
            try {
                SchemaMigration.migrate(provider.dataSource());
            } catch (Exception exception) {
                plugin.getLogger().log(Level.SEVERE, "Database migration failed", exception);
                throw new IllegalStateException(exception);
            }
            return provider;
        }, executor);
    }

    public DataSourceProvider provider() {
        return provider;
    }

    public ExecutorService executor() {
        return executor;
    }

    public void shutdown() {
        if (provider != null) {
            provider.close();
        }
        executor.shutdown();
    }
}
