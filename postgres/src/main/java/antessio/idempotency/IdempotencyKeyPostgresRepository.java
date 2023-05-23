package antessio.idempotency;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class IdempotencyKeyPostgresRepository<T> implements IdempotencyKeyRepository<T> {

    private final IdempotencyKeyDao<T> idempotencyKeyDao;

    public IdempotencyKeyPostgresRepository(IdempotencyKeyDao<T> idempotencyKeyDao) {
        this.idempotencyKeyDao = idempotencyKeyDao;
    }

    @Override
    public Optional<IdempotencyKey<T>> loadByKey(String key) {
        return Optional.ofNullable(idempotencyKeyDao.findById(key));
    }


    @Override
    public void addKey(String key) {
        int affectedRows = idempotencyKeyDao.insert(key);
        if (affectedRows == 0) {
            throw new RuntimeException("no rows inserted");
        }
    }

    @Override
    public void updateTarget(String key, T target) {
        idempotencyKeyDao.update(key, target);
    }

    @Override
    public Stream<IdempotencyKey<T>> getCreatedBefore(Instant before) {
        return Stream.iterate(
                             idempotencyKeyDao.findWhereCreationDateBefore(before, 20),
                             l -> !l.isEmpty(),
                             __ -> idempotencyKeyDao.findWhereCreationDateBefore(before, 20))
                     .flatMap(List::stream);
    }


    @Override
    public void delete(String key) {
        idempotencyKeyDao.deleteById(key);
    }


}
