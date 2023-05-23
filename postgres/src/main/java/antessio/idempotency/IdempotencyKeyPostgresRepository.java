package antessio.idempotency;

import java.util.Optional;

public class IdempotencyKeyPostgresRepository<T> implements IdempotencyKeyRepository<T> {

    private final IdempotencyKeyDao<T> idempotencyKeyDao;

    public IdempotencyKeyPostgresRepository(IdempotencyKeyDao<T> idempotencyKeyDao) {
        this.idempotencyKeyDao = idempotencyKeyDao;
    }

    @Override
    public Optional<IdempotencyKey<T>> loadByKey(String key) {
        return Optional.ofNullable(idempotencyKeyDao.findByKey(key));
    }


    @Override
    public void addKey(String key) {
        int affectedRows = idempotencyKeyDao.insert(key);
        if (affectedRows==0){
            throw new RuntimeException("no rows inserted");
        }
    }

    @Override
    public void updateTarget(String key, T target) {
        idempotencyKeyDao.update(key, target);
    }




}
