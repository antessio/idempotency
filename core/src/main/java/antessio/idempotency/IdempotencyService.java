package antessio.idempotency;

import java.util.function.Supplier;

public interface IdempotencyService<T> {


    T executeWithIdempotencyKey(
            Supplier<String> idempotencyKeyExtractor,
            Supplier<T> executor);


}
