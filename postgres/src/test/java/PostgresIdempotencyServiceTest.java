import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.testcontainers.utility.MountableFile.forClasspathResource;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;

import antessio.idempotency.IdempotencyKeyDao;
import antessio.idempotency.IdempotencyKeyDaoAdapter;
import antessio.idempotency.IdempotencyKeyPostgresRepository;
import antessio.idempotency.IdempotencyService;
import antessio.idempotency.IdempotencyServiceBaseImpl;

public class PostgresIdempotencyServiceTest {

    private static PostgreSQLContainer<?> postgreSQLContainer;

    private IdempotencyService<Payment> idempotencyService;


    @BeforeAll
    static void init() {
        postgreSQLContainer = new PostgreSQLContainer<>("postgres:15")
                .withDatabaseName("test")
                .withUsername("idempotencyService")
                .withPassword("idempotencyService")
                .withCopyFileToContainer(
                        forClasspathResource("init.sql"),
                        "/docker-entrypoint-initdb.d/1-init.sql");
        postgreSQLContainer.start();


        System.setProperty("datasource.hikari.JDBC_URL", postgreSQLContainer.getJdbcUrl());
        System.setProperty("datasource.hikari.USERNAME", postgreSQLContainer.getUsername());
        System.setProperty("datasource.hikari.PASSWORD", postgreSQLContainer.getPassword());
    }

    @BeforeEach
    void setUp() {
        idempotencyService = new IdempotencyServiceBaseImpl<>(
                new IdempotencyKeyPostgresRepository<>(
                        new IdempotencyKeyDaoAdapter<>(Payment.class)
                )
        );
    }

    @Test
    void shouldExecuteOperationAndStoreIdempotencyKey() {
        Payment payment = new Payment(
                UUID.randomUUID(),
                200_00L,
                "EUR",
                "acc_11111"
        );
        AtomicBoolean called = new AtomicBoolean(false);
        Payment result = idempotencyService.executeWithIdempotencyKey(() -> payment.getId().toString(), () -> {
            called.set(true);
            return payment;
        });
        assertThat(result)
                .isEqualTo(payment);
        assertThat(called.get()).isTrue();
        called.set(false);

        result = idempotencyService.executeWithIdempotencyKey(() -> payment.getId().toString(), () -> {
            called.set(true);
            return payment;
        });
        assertThat(result)
                .isEqualTo(payment);
        assertThat(called.get()).isFalse();

    }

}
