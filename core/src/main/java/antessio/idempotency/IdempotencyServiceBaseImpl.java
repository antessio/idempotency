package antessio.idempotency;

import java.time.Clock;
import java.time.Duration;
import java.util.function.Supplier;

public class IdempotencyServiceBaseImpl<T> implements IdempotencyService<T> {

    private final IdempotencyKeyRepository<T> idempotencyKeyRepository;
    private final Clock clock;

    public IdempotencyServiceBaseImpl(
            Clock clock,
            IdempotencyKeyRepository<T> idempotencyKeyRepository) {
        this.clock = clock;
        this.idempotencyKeyRepository = idempotencyKeyRepository;
    }

    @Override
    public T executeWithIdempotencyKey(
            Supplier<String> idempotencyKeyExtractor,
            Supplier<T> executor) {

        String key = idempotencyKeyExtractor.get();
        return idempotencyKeyRepository.loadByKey(key)
                                       .map(idempotencyKey -> idempotencyKey
                                               .getTarget()
                                               .orElseThrow(() -> new IdempotencyException(IdempotencyException.ErrorCode.IN_PROGRESS_REQUEST)))
                                       .orElseGet(() -> executeAndStoreIdempotencyKey(executor, key));

    }

    @Override
    public void cleanupExpired(Duration idempotencyKeyDuration) {
        idempotencyKeyRepository.getCreatedBefore(clock.instant().minus(idempotencyKeyDuration))
                                .map(IdempotencyKey::getKey)
                                .forEach(idempotencyKeyRepository::delete);
    }

    private T executeAndStoreIdempotencyKey(Supplier<T> executor, String key) {
        idempotencyKeyRepository.addKey(key);
        T target = executor.get();
        idempotencyKeyRepository.updateTarget(key, target);
        return target;
    }

}
