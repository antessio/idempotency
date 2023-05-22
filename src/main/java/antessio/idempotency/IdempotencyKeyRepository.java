package antessio.idempotency;

import java.util.Optional;

public interface IdempotencyKeyRepository <K, T>{

    Optional<IdempotencyKey<K,T>> loadByKey(K key);
    void addKey(K key);

    void updateTarget(K key, T target);

}
