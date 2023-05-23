package antessio.idempotency;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class IdempotencyServiceBaseImplTest {


    private IdempotencyKeyRepository<StubTarget> repository;
    private Clock clock;

    private IdempotencyService<StubTarget> idempotencyService;

    @BeforeEach
    void setUp() {
        repository = mock(IdempotencyKeyRepository.class);
        clock = mock(Clock.class);
        idempotencyService = new IdempotencyServiceBaseImpl<>(clock, repository);
    }


    @Test
    void shouldStoreIdempotencyKeyAndProcessRequest() {
        // given
        when(repository.loadByKey(any())).thenReturn(Optional.empty());

        StubTarget target = new StubTarget("field1", 111, false);
        AtomicBoolean isCalled = new AtomicBoolean(false);

        // when
        StubTarget result = idempotencyService.executeWithIdempotencyKey(target::getField1, () -> {
            isCalled.set(true);
            return target;
        });

        // then
        assertThat(result)
                .isEqualTo(target);
        assertThat(isCalled.get()).isTrue();
        verify(repository).addKey(target.getField1());
        verify(repository).updateTarget(target.getField1(), target);

    }

    @Test
    void shouldNotProcess() {
        // given
        StubTarget target = new StubTarget("field1", 111, false);
        when(repository.loadByKey(any())).thenReturn(Optional.of(new IdempotencyKey<>(target.getField1(), target)));

        AtomicBoolean isCalled = new AtomicBoolean(false);

        // when
        StubTarget result = idempotencyService.executeWithIdempotencyKey(target::getField1, () -> {
            isCalled.set(true);
            return target;
        });

        // then
        assertThat(result)
                .isEqualTo(target);
        assertThat(isCalled.get()).isFalse();
        verifyNoInteractionWithRepository();

    }

    @Test
    void shouldRaiseErrorIfProcessingInProgress() {
        // given
        StubTarget target = new StubTarget("field1", 111, false);
        when(repository.loadByKey(any())).thenReturn(Optional.of(new IdempotencyKey<>(target.getField1())));

        AtomicBoolean isCalled = new AtomicBoolean(false);

        // when
        assertThatExceptionOfType(IdempotencyException.class)
                .isThrownBy(() -> idempotencyService.executeWithIdempotencyKey(target::getField1, () -> {
                    isCalled.set(true);
                    return target;
                }))
                .matches(e -> e.getErrorCode() == IdempotencyException.ErrorCode.IN_PROGRESS_REQUEST);

        // then
        assertThat(isCalled.get()).isFalse();
        verifyNoInteractionWithRepository();

    }

    private void verifyNoInteractionWithRepository() {
        verify(repository, never()).addKey(any());
        verify(repository, never()).updateTarget(any(), any());
    }

}