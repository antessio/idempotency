package antessio.idempotency;

import java.time.Instant;
import java.util.Optional;

public class IdempotencyKeyPostgresRepository implements IdempotencyTokenRepository {

    private final IdempotencyKeyDao idempotencyKeyDao;

    public IdempotencyKeyPostgresRepository(IdempotencyKeyDao idempotencyKeyDao) {
        this.idempotencyKeyDao = idempotencyKeyDao;
    }


    @Override
    public Optional<IdempotencyKey> getIdempotencyKey(String key) {
        return Optional.ofNullable(idempotencyKeyDao.findById(key));
    }

    @Override
    public void updateIdempotencyKey(String key, String entityId) {
        idempotencyKeyDao.update(key, entityId);
    }

    @Override
    public void createIdempotencyKey(String idempotencyKey, Instant expiresAt) {
        idempotencyKeyDao.insert(idempotencyKey, expiresAt);
    }

    @Override
    public void cleanupIdempotencyKey(String idempotencyKey) {
        idempotencyKeyDao.deleteById(idempotencyKey);
    }

}
