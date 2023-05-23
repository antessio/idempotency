package antessio.idempotency;

import java.time.Instant;
import java.util.Optional;

public class IdempotencyKey <T>{
    private String key;
    private T target;
    private Instant createdAt;

    public IdempotencyKey(String key, T target) {
        this.key = key;
        this.target = target;
    }

    public IdempotencyKey(String key, T target, Instant createdAt) {
        this.key = key;
        this.target = target;
        this.createdAt = createdAt;
    }

    public IdempotencyKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public Optional<T> getTarget() {
        return Optional.ofNullable(target);
    }


    public Instant getCreatedAt() {
        return createdAt;
    }

}
