package antessio.idempotency;

import java.util.function.Supplier;

public interface IdempotencyService<K, T> {


    T executeWithIdempotencyKey(
            Supplier<K> idempotencyKeyExtractor,
            Supplier<T> executor);


}
