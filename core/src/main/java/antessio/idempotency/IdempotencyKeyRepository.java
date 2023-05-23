package antessio.idempotency;

import java.time.Instant;
import java.util.Optional;
import java.util.stream.Stream;

public interface IdempotencyKeyRepository<T> {

    Optional<IdempotencyKey<T>> loadByKey(String key);

    void addKey(String key);

    void updateTarget(String key, T target);

    Stream<IdempotencyKey<T>> getCreatedBefore(Instant before);
    void delete(String key);

}
