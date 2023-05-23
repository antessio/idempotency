package antessio.idempotency;

import java.time.Duration;
import java.util.function.Supplier;

public interface IdempotencyService<T> {


    T executeWithIdempotencyKey(
            Supplier<String> idempotencyKeyExtractor,
            Supplier<T> executor);


    void cleanupExpired(Duration idempotencyKeyDuration);

}
