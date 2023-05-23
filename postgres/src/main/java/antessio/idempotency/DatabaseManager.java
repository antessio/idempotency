package antessio.idempotency;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;

public class DatabaseManager {

    private static HikariDataSource dataSource;

    private static final String JDBC_URL = "datasource.hikari.JDBC_URL";
    private static final String USERNAME = "datasource.hikari.USERNAME";
    private static final String PASSWORD = "datasource.hikari.PASSWORD";
    private static final String POOL_NAME = "datasource.hikari.POOL_NAME";
    private static final String MAX_POOL_SIZE = "datasource.hikari.MAX_POOL_SIZE";
    private static final String MIN_IDLE = "datasource.hikari.MIN_IDLE";
    private static final String IDLE_TIMEOUT = "datasource.hikari.IDLE_TIMEOUT";
    private static final String CONNECTION_TIMEOUT = "datasource.hikari.CONNECTION_TIMEOUT";
    private static final String MAX_LIFETIME = "datasource.hikari.MAX_LIFETIME";
    private static final String CONNECTION_TEST_QUERY = "datasource.hikari.CONNECTION_TEST_QUERY";
    private static final String CONNECTION_INIT_SQL = "datasource.hikari.CONNECTION_INIT_SQL";


    private DatabaseManager() {

    }

    public static DataSource getDataSource() {
        if (dataSource == null) {
            HikariConfig config = new HikariConfig();
            // JDBC URL
            config.setJdbcUrl(System.getProperty(JDBC_URL));

            // Username and Password
            config.setUsername(System.getProperty(USERNAME));
            config.setPassword(System.getProperty(PASSWORD));

            // Other configuration properties (optional)
            if (System.getProperty(POOL_NAME) != null) {
                config.setPoolName(System.getProperty(POOL_NAME));
            }
            if (System.getProperty(MAX_POOL_SIZE) != null) {
                config.setMaximumPoolSize(Integer.parseInt(System.getProperty(MAX_POOL_SIZE)));
            }
            if (System.getProperty(MIN_IDLE) != null) {
                config.setMinimumIdle(Integer.parseInt(System.getProperty(MIN_IDLE)));
            }
            if (System.getProperty(IDLE_TIMEOUT) != null) {
                config.setIdleTimeout(Long.parseLong(System.getProperty(IDLE_TIMEOUT)));
            }
            if (System.getProperty(CONNECTION_TIMEOUT) != null) {
                config.setConnectionTimeout(Long.parseLong(System.getProperty(CONNECTION_TIMEOUT)));
            }
            if (System.getProperty(MAX_LIFETIME) != null) {
                config.setMaxLifetime(Long.parseLong(System.getProperty(MAX_LIFETIME)));
            }
            if (System.getProperty(CONNECTION_TEST_QUERY) != null) {
                config.setConnectionTestQuery(System.getProperty(CONNECTION_TEST_QUERY));
            }
            if (System.getProperty(CONNECTION_INIT_SQL) != null) {
                config.setConnectionInitSql(System.getProperty(CONNECTION_INIT_SQL));
            }

            dataSource = new HikariDataSource(config);
        }
        return dataSource;
    }
}
