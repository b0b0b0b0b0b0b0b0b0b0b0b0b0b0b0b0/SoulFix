package bm.b0b0b0.SoulFix.database;

import bm.b0b0b0.SoulFix.config.PluginConfig;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.sql.DataSource;
import org.bukkit.plugin.java.JavaPlugin;

public final class DataSourceProvider {

    private final HikariDataSource dataSource;

    public DataSourceProvider(JavaPlugin plugin, PluginConfig config) {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setPoolName("SoulFix");
        hikariConfig.setMaximumPoolSize(config.storage().poolSize);
        hikariConfig.setConnectionTimeout(config.storage().connectionTimeoutMs);
        if ("mysql".equalsIgnoreCase(config.storageType())) {
            hikariConfig.setJdbcUrl(buildMysqlUrl(config));
            hikariConfig.setUsername(config.storage().mysqlUser);
            hikariConfig.setPassword(config.storage().mysqlPassword);
            hikariConfig.setDriverClassName("com.mysql.cj.jdbc.Driver");
        } else {
            Path databasePath = plugin.getDataFolder().toPath().resolve(config.storage().sqliteFile);
            try {
                Path parent = databasePath.getParent();
                if (parent != null) {
                    Files.createDirectories(parent);
                }
            } catch (IOException exception) {
                throw new IllegalStateException("Failed to create database directory", exception);
            }
            hikariConfig.setJdbcUrl("jdbc:sqlite:" + databasePath.toAbsolutePath());
            hikariConfig.setDriverClassName("org.sqlite.JDBC");
        }
        this.dataSource = new HikariDataSource(hikariConfig);
    }

    public DataSource dataSource() {
        return dataSource;
    }

    public void close() {
        dataSource.close();
    }

    private String buildMysqlUrl(PluginConfig config) {
        return "jdbc:mysql://"
                + config.storage().mysqlHost
                + ":"
                + config.storage().mysqlPort
                + "/"
                + config.storage().mysqlDatabase
                + "?useSSL=false&allowPublicKeyRetrieval=true&characterEncoding=utf8";
    }
}
