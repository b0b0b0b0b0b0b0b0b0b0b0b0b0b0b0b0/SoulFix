package bm.b0b0b0.soulFix.repository;

import bm.b0b0b0.soulFix.model.PlayerRepairProfile;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface PlayerProfileRepository {

    CompletableFuture<Optional<PlayerRepairProfile>> find(UUID playerId);

    CompletableFuture<PlayerRepairProfile> save(PlayerRepairProfile profile);
}
