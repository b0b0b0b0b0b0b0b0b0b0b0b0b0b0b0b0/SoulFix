package bm.b0b0b0.SoulFix.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.DataSource;

public final class SchemaMigration {

    private SchemaMigration() {
    }

    public static void migrate(DataSource dataSource) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS soulfix_players (
                        player_uuid VARCHAR(36) PRIMARY KEY,
                        purchased_slots INT NOT NULL DEFAULT 0,
                        cooldown_until BIGINT NOT NULL DEFAULT 0
                    )
                    """);
        }
    }
}
