package antessio.idempotency;

import java.time.Instant;
import java.util.Optional;

public interface IdempotencyTokenRepository {

    Optional<IdempotencyKey> getIdempotencyKey(String key);

    void updateIdempotencyKey(String key, String entityId);

    void createIdempotencyKey(String idempotencyKey, Instant plus);

    void cleanupIdempotencyKey(String idempotencyKey);

}
