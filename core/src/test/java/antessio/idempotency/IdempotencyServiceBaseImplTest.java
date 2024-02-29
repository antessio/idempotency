package antessio.idempotency;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class IdempotencyServiceBaseImplTest {


    private IdempotencyTokenInMemoryRepository repository;
    private Clock clock;

    private IdempotencyService idempotencyService;



    @BeforeEach
    void setUp() {
        repository = new IdempotencyTokenInMemoryRepository();
        repository.reset();
        clock = mock(Clock.class);
        idempotencyService = new IdempotencyServiceImpl(repository, Duration.ofHours(24), clock);
    }


    @Test
    void shouldStoreIdempotencyKeyAndProcessRequest() {
        // given
        StubTarget target = new StubTarget("field1", 111, false);
        AtomicBoolean isCalled = new AtomicBoolean(false);
        List<StubTarget> data = List.of(target);
        String idempotencyKey = "idempotencyKey";
        Instant now = Instant.now();
        when(clock.instant()).thenReturn(now);

        // when
        StubTarget result = idempotencyService.createWithIdempotency(
                idempotencyKey,
                id -> data.stream().filter(s -> s.getField1().equals(id)).findFirst(),
                () -> {
                    isCalled.set(true);
                    return target;
                }, StubTarget::getField1);

        // then
        assertThat(result)
                .isEqualTo(target);
        assertThat(isCalled.get()).isTrue();
        assertThat(repository.getStore())
                .containsOnly(new AbstractMap.SimpleEntry<>(
                        idempotencyKey,
                        new IdempotencyKey(idempotencyKey, target.getField1(), now.plus(24, ChronoUnit.HOURS))));


    }

    @Test
    void shouldStoreIdempotencyKeyAndProcessRequestIfExpired() {
        // given
        StubTarget target = new StubTarget("field1", 111, false);
        String oldValue = "oldValue";
        StubTarget oldTarget = new StubTarget(oldValue, 111, false);
        AtomicBoolean isCalled = new AtomicBoolean(false);
        List<StubTarget> data = List.of(target, oldTarget);
        String idempotencyKey = "idempotencyKey";
        Instant now = Instant.now();
        when(clock.instant()).thenReturn(now);
        IdempotencyKey existingToken = new IdempotencyKey(
                idempotencyKey,
                null,
                now.minus(48, ChronoUnit.HOURS));
        repository.getStore().put(idempotencyKey, existingToken);
        // when
        StubTarget result = idempotencyService.createWithIdempotency(
                idempotencyKey,
                id -> data.stream().filter(s -> s.getField1().equals(id)).findFirst(),
                () -> {
                    isCalled.set(true);
                    return target;
                }, StubTarget::getField1);

        // then
        assertThat(result)
                .isEqualTo(target);
        assertThat(isCalled.get()).isTrue();
        assertThat(repository.getStore())
                .containsOnly(new AbstractMap.SimpleEntry<>(
                        idempotencyKey,
                        new IdempotencyKey(idempotencyKey, target.getField1(), now.plus(24, ChronoUnit.HOURS))));


    }

    @Test
    void shouldReturnExistingValueTokenValid() {
        // given
        StubTarget target = new StubTarget("field1", 111, false);
        AtomicBoolean isCalled = new AtomicBoolean(false);
        List<StubTarget> data = List.of(target);
        String idempotencyKey = "idempotencyKey";
        Instant now = Instant.now();
        when(clock.instant()).thenReturn(now);
        IdempotencyKey existingToken = new IdempotencyKey(
                idempotencyKey,
                target.getField1(),
                now.plus(48, ChronoUnit.HOURS));
        repository.getStore().put(idempotencyKey, existingToken);
        // when
        StubTarget result = idempotencyService.createWithIdempotency(
                idempotencyKey,
                id -> data.stream().filter(s -> s.getField1().equals(id)).findFirst(),
                () -> {
                    isCalled.set(true);
                    return target;
                }, StubTarget::getField1);

        // then
        assertThat(result)
                .isEqualTo(target);
        assertThat(isCalled.get()).isFalse();
        assertThat(repository.getStore())
                .containsOnly(new AbstractMap.SimpleEntry<>(idempotencyKey, existingToken));


    }

    @Test
    void shouldRaiseErrorIfProcessingInProgress() {
        // given
        // given
        StubTarget target = new StubTarget("field1", 111, false);
        AtomicBoolean isCalled = new AtomicBoolean(false);
        List<StubTarget> data = List.of(target);
        String idempotencyKey = "idempotencyKey";
        Instant now = Instant.now();
        when(clock.instant()).thenReturn(now);
        IdempotencyKey existingToken = new IdempotencyKey(idempotencyKey, null, now.plus(24, ChronoUnit.HOURS));
        repository.getStore().put(idempotencyKey, existingToken);
        // when
        assertThatExceptionOfType(IdempotencyException.class)
                .isThrownBy(() -> idempotencyService.createWithIdempotency(
                        idempotencyKey,
                        id -> data.stream().filter(s -> s.getField1().equals(id)).findFirst(),
                        () -> {
                            isCalled.set(true);
                            return target;
                        }, StubTarget::getField1))
                .matches(e -> e.getErrorCode() == IdempotencyException.ErrorCode.IN_PROGRESS_REQUEST);

        // then
        assertThat(isCalled.get()).isFalse();
        assertThat(repository.getStore())
                .containsOnly(new AbstractMap.SimpleEntry<>(idempotencyKey, existingToken));

    }

    @Test
    void shouldCleanupTokenWhenCreationInError() {
        // given
        StubTarget target = new StubTarget("field1", 111, false);
        AtomicBoolean isCalled = new AtomicBoolean(false);
        List<StubTarget> data = List.of(target);
        String idempotencyKey = "idempotencyKey";
        Instant now = Instant.now();
        when(clock.instant()).thenReturn(now);

        // when
        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> idempotencyService.createWithIdempotency(
                        idempotencyKey,
                        id -> data.stream().filter(s -> s.getField1().equals(id)).findFirst(),
                        () -> {
                            isCalled.set(true);
                            throw new RuntimeException("fatal error");
                        }, StubTarget::getField1));

        // then
        assertThat(isCalled.get()).isTrue();
        assertThat(repository.getStore())
                .isEmpty();
    }

    static class IdempotencyTokenInMemoryRepository implements IdempotencyTokenRepository {

        private Map<String, IdempotencyKey> store = new HashMap<>();


        @Override
        public Optional<IdempotencyKey> getIdempotencyKey(String key) {
            return Optional.ofNullable(store.get(key));
        }

        @Override
        public void updateIdempotencyKey(String key, String entityId) {
            IdempotencyKey idempotencyKey = getIdempotencyKey(key)
                    .orElseThrow(() -> new IllegalArgumentException("key " + key + " doesn't eexist"));
            store.put(idempotencyKey.getKey(), new IdempotencyKey(
                    idempotencyKey.getKey(),
                    entityId,
                    idempotencyKey.getExpiresAt()
            ));
        }

        @Override
        public void createIdempotencyKey(String idempotencyKey, Instant expiresAt) {
            store.put(idempotencyKey, new IdempotencyKey(idempotencyKey, null, expiresAt));
        }

        @Override
        public void cleanupIdempotencyKey(String idempotencyKey) {
            store.remove(idempotencyKey);
        }

        public Map<String, IdempotencyKey> getStore() {
            return store;
        }

        public void reset() {
            this.store = new HashMap<>();
        }

    }

}