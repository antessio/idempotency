package antessio.idempotency;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public class IdempotencyServiceImpl implements IdempotencyService {

    private final IdempotencyTokenRepository idempotencyTokenRepository;
    private final Duration tokenDuration;
    private final Clock clock;

    public IdempotencyServiceImpl(IdempotencyTokenRepository idempotencyTokenRepository, Duration tokenDuration, Clock clock) {
        this.idempotencyTokenRepository = idempotencyTokenRepository;
        this.tokenDuration = tokenDuration;
        this.clock = clock;
    }

    @Override
    public <ENTITY> ENTITY createWithIdempotency(
            String token,
            Function<String, Optional<ENTITY>> retrieve,
            Supplier<ENTITY> create,
            Function<ENTITY, String> entityToId) {
        Instant now = clock.instant();
        Supplier<ENTITY> createEntityAndStoreIdempotencyKey = () -> {
            idempotencyTokenRepository.createIdempotencyKey(token, now.plus(tokenDuration));
            try {
                ENTITY entity = create.get();
                idempotencyTokenRepository.updateIdempotencyKey(
                        token,
                        entityToId.apply(entity));
                return entity;
            } catch (Exception e) {
                idempotencyTokenRepository.cleanupIdempotencyKey(token);
                throw e;
            }
        };
        return idempotencyTokenRepository.getIdempotencyKey(token)
                                         .map(idempotencyKey -> {
                                             if (now.isBefore(idempotencyKey.getExpiresAt())) {
                                                 return idempotencyKey.getEntityId()
                                                                      .flatMap(retrieve)
                                                                      .orElseThrow(() -> new IdempotencyException(IdempotencyException.ErrorCode.IN_PROGRESS_REQUEST));
                                             } else {
                                                 if (idempotencyKey.getEntityId().isPresent()) {
                                                     throw new IllegalArgumentException("cannot override existing token");
                                                 }
                                                 idempotencyTokenRepository.cleanupIdempotencyKey(token);
                                                 return createEntityAndStoreIdempotencyKey.get();
                                             }
                                         })
                                         .orElseGet(createEntityAndStoreIdempotencyKey);
    }

}