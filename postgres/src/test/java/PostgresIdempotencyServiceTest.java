import static org.assertj.core.api.Assertions.assertThat;
import static org.testcontainers.utility.MountableFile.forClasspathResource;

import java.time.Clock;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.testcontainers.containers.PostgreSQLContainer;

import antessio.idempotency.IdempotencyKey;
import antessio.idempotency.IdempotencyKeyDao;
import antessio.idempotency.IdempotencyKeyPostgresRepository;
import antessio.idempotency.IdempotencyService;
import antessio.idempotency.IdempotencyServiceImpl;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PostgresIdempotencyServiceTest {

    private static PostgreSQLContainer<?> postgreSQLContainer;

    private IdempotencyService idempotencyService;
    private IdempotencyKeyDao dao;


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
        dao = new IdempotencyKeyDao();
        IdempotencyKeyPostgresRepository idempotencyKeyPostgresRepository = new IdempotencyKeyPostgresRepository(dao);
        idempotencyService = new IdempotencyServiceImpl(
                idempotencyKeyPostgresRepository, Duration.ofHours(24),
                Clock.systemUTC()
        );
    }

    @Test
    @Order(1)
    void shouldExecuteOperationAndStoreIdempotencyKey() {
        Payment payment = new Payment(
                UUID.randomUUID(),
                200_00L,
                "EUR",
                "acc_11111"
        );
        String idempotencyKey = "idempotencyKey";
        List<Payment> paymentStore = new ArrayList<>();
        Function<String, Optional<Payment>> retrieve = paymentId -> paymentStore.stream()
                                                                                              .filter(p -> p.getId().equals(UUID.fromString(paymentId)))
                                                                                              .findFirst();
        Supplier<Payment> store = () -> {
            paymentStore.add(payment);
            return payment;
        };
        Function<Payment, String> paymentToEntityId = p -> p.getId().toString();
        Payment result = idempotencyService.createWithIdempotency(
                idempotencyKey,
                retrieve,
                store,
                paymentToEntityId);
        assertThat(result)
                .isEqualTo(payment);
        assertThat(paymentStore).containsOnly(payment);
        result = idempotencyService.createWithIdempotency(
                idempotencyKey,
                retrieve,
                store,
                paymentToEntityId);
        assertThat(result)
                .isEqualTo(payment);
        assertThat(paymentStore).containsOnly(payment);
        List<IdempotencyKey> allIdempotencyKeys = dao.findAll(20, null);
        assertThat(allIdempotencyKeys)
                .asList()
                .hasSize(1);
    }

//    @Test
//    @Order(2)
//    void shouldCleanupTokens(){
//
//        idempotencyService.cleanupExpired(Duration.ZERO);
//
//        List<IdempotencyKey<Payment>> allIdempotencyKeys = dao.findAll(20, null);
//        assertThat(allIdempotencyKeys)
//                .asList()
//                .hasSize(0);
//
//    }
}
