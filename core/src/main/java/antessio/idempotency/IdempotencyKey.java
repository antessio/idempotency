package antessio.idempotency;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

public class IdempotencyKey {
    private String key;
    private String entityId;
    private Instant expiresAt;


    public IdempotencyKey(String key, String entityId, Instant expiresAt) {
        this.key = key;
        this.entityId = entityId;
        this.expiresAt = expiresAt;
    }

    public IdempotencyKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public Optional<String> getEntityId() {
        return Optional.ofNullable(entityId);
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        IdempotencyKey that = (IdempotencyKey) o;

        if (!Objects.equals(key, that.key)) {
            return false;
        }
        if (!Objects.equals(entityId, that.entityId)) {
            return false;
        }
        return Objects.equals(expiresAt, that.expiresAt);
    }

    @Override
    public int hashCode() {
        int result = key != null ? key.hashCode() : 0;
        result = 31 * result + (entityId != null ? entityId.hashCode() : 0);
        result = 31 * result + (expiresAt != null ? expiresAt.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "IdempotencyKey{" +
               "key='" + key + '\'' +
               ", entityId='" + entityId + '\'' +
               ", expiresAt=" + expiresAt +
               '}';
    }

}
