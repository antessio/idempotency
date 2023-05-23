package antessio.idempotency;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

public abstract class IdempotencyKeyDao<T> {

    private final static String QUERY_BY_KEY = "SELECT id, target FROM idempotency_key WHERE id=?";
    private static final String INSERT_EMPTY_TARGET = "INSERT INTO idempotency_key (id) VALUES (?)";
    private static final String UPDATE_TARGET = "UPDATE idempotency_key SET target=? ::jsonb where id=?";
    private Connection connection;

    public IdempotencyKeyDao() {
        try {
            connection = DatabaseManager.getDataSource().getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    public IdempotencyKey<T> findByKey(String key) {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        IdempotencyKey<T> result = null;
        try {
            preparedStatement = connection.prepareStatement(QUERY_BY_KEY);
            preparedStatement.setString(1, key);
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                String k = resultSet.getString("id");
                String target = resultSet.getString("target");
                result = new IdempotencyKey<T>(k, fromJson(target));
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
            if (preparedStatement != null) {
                try {
                    preparedStatement.close();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        return result;
    }

    public int insert(String key) {
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = connection.prepareStatement(INSERT_EMPTY_TARGET);
            preparedStatement.setString(1, key);
            return preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            if (preparedStatement != null) {
                try {
                    preparedStatement.close();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public void update(String key, T target) {
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = connection.prepareStatement(UPDATE_TARGET);
            preparedStatement.setString(1, toJson(target));
            preparedStatement.setString(2, key);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            if (preparedStatement != null) {
                try {
                    preparedStatement.close();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public abstract T fromJson(String json);

    public abstract String toJson(T target);


}
