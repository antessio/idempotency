package antessio.idempotency;

import java.time.Duration;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public interface IdempotencyService {

    <ENTITY> ENTITY createWithIdempotency(
            String idempotencyKey,
            Function<String, Optional<ENTITY>> retrieve,
            Supplier<ENTITY> create,
            Function<ENTITY, String> entityToId
    );

}

