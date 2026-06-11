package bm.b0b0b0.SoulFix.repository;

import bm.b0b0b0.SoulFix.model.PlayerRepairProfile;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface PlayerProfileRepository {

    CompletableFuture<Optional<PlayerRepairProfile>> find(UUID playerId);

    CompletableFuture<PlayerRepairProfile> save(PlayerRepairProfile profile);
}
