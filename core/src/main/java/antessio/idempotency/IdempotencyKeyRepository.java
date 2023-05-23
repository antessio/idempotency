package antessio.idempotency;

import java.util.Optional;

public interface IdempotencyKeyRepository < T>{

    Optional<IdempotencyKey<T>> loadByKey(String key);
    void addKey(String key);

    void updateTarget(String key, T target);

}
