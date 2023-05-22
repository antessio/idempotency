package antessio.idempotency;

import java.util.Optional;

public class IdempotencyKey <K, T>{
    private K key;
    private T target;

    public IdempotencyKey(K key, T target) {
        this.key = key;
        this.target = target;
    }

    public IdempotencyKey(K key) {
        this.key = key;
    }

    public K getKey() {
        return key;
    }

    public Optional<T> getTarget() {
        return Optional.ofNullable(target);
    }

}
