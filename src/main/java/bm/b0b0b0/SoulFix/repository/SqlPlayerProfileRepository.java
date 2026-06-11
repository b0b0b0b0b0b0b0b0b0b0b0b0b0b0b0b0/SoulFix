package bm.b0b0b0.soulFix.repository;

import bm.b0b0b0.soulFix.model.PlayerRepairProfile;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import javax.sql.DataSource;

public final class SqlPlayerProfileRepository implements PlayerProfileRepository {

    private final DataSource dataSource;
    private final Executor executor;

    public SqlPlayerProfileRepository(DataSource dataSource, Executor executor) {
        this.dataSource = dataSource;
        this.executor = executor;
    }

    @Override
    public CompletableFuture<Optional<PlayerRepairProfile>> find(UUID playerId) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement statement = connection.prepareStatement(
                         "SELECT purchased_slots, cooldown_until FROM soulfix_players WHERE player_uuid = ?")) {
                statement.setString(1, playerId.toString());
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (!resultSet.next()) {
                        return Optional.empty();
                    }
                    return Optional.of(new PlayerRepairProfile(
                            playerId,
                            resultSet.getInt("purchased_slots"),
                            resultSet.getLong("cooldown_until")
                    ));
                }
            } catch (Exception exception) {
                throw new IllegalStateException(exception);
            }
        }, executor);
    }

    @Override
    public CompletableFuture<PlayerRepairProfile> save(PlayerRepairProfile profile) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = dataSource.getConnection()) {
                int updated;
                try (PreparedStatement update = connection.prepareStatement(
                        "UPDATE soulfix_players SET purchased_slots = ?, cooldown_until = ? WHERE player_uuid = ?")) {
                    update.setInt(1, profile.purchasedSlots());
                    update.setLong(2, profile.cooldownUntilEpochMs());
                    update.setString(3, profile.playerId().toString());
                    updated = update.executeUpdate();
                }
                if (updated == 0) {
                    try (PreparedStatement insert = connection.prepareStatement(
                            "INSERT INTO soulfix_players (player_uuid, purchased_slots, cooldown_until) VALUES (?, ?, ?)")) {
                        insert.setString(1, profile.playerId().toString());
                        insert.setInt(2, profile.purchasedSlots());
                        insert.setLong(3, profile.cooldownUntilEpochMs());
                        insert.executeUpdate();
                    }
                }
                return profile;
            } catch (Exception exception) {
                throw new IllegalStateException(exception);
            }
        }, executor);
    }
}
