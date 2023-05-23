package antessio.idempotency;

import java.util.function.Supplier;

public class IdempotencyServiceBaseImpl<T> implements IdempotencyService<T> {

    private IdempotencyKeyRepository<T> idempotencyKeyRepository;

    public IdempotencyServiceBaseImpl(IdempotencyKeyRepository<T> idempotencyKeyRepository) {
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

    private T executeAndStoreIdempotencyKey(Supplier<T> executor, String key) {
        idempotencyKeyRepository.addKey(key);
        T target = executor.get();
        idempotencyKeyRepository.updateTarget(key, target);
        return target;
    }

}
