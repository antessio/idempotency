package antessio.idempotency;

import java.util.function.Supplier;

public class IdempotencyServiceBaseImpl<K, T> implements IdempotencyService<K, T> {

    private IdempotencyKeyRepository<K, T> idempotencyKeyRepository;

    public IdempotencyServiceBaseImpl(IdempotencyKeyRepository<K, T> idempotencyKeyRepository) {
        this.idempotencyKeyRepository = idempotencyKeyRepository;
    }

    @Override
    public T executeWithIdempotencyKey(
            Supplier<K> idempotencyKeyExtractor,
            Supplier<T> executor) {

        K key = idempotencyKeyExtractor.get();
        return idempotencyKeyRepository.loadByKey(key)
                                       .map(idempotencyKey -> idempotencyKey
                                               .getTarget()
                                               .orElseThrow(() -> new IdempotencyException(IdempotencyException.ErrorCode.IN_PROGRESS_REQUEST)))
                                       .orElseGet(() -> executeAndStoreIdempotencyKey(executor, key));

    }

    private T executeAndStoreIdempotencyKey(Supplier<T> executor, K key) {
        idempotencyKeyRepository.addKey(key);
        T target = executor.get();
        idempotencyKeyRepository.updateTarget(key, target);
        return target;
    }

}
