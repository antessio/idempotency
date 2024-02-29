package antessio.idempotency;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class IdempotencyKeyDao {

    private final static String QUERY_BY_ID = "SELECT id, target, expires_at FROM idempotency_key WHERE id=?";
    private static final String INSERT_EMPTY_TARGET = "INSERT INTO idempotency_key (id, expires_at) VALUES (?,?)";
    private static final String UPDATE_TARGET = "UPDATE idempotency_key SET target=? where id=?";
    private final static String DELETE = "DELETE FROM idempotency_key WHERE id=?";
    private static final String QUERY_expires_at_LT = "SELECT id, target, expires_at FROM idempotency_key WHERE expires_at < ? ORDER BY expires_at LIMIT ?";
    private static final String QUERY_ALL = "SELECT id, target, expires_at FROM idempotency_key ORDER BY expires_at OFFSET ? LIMIT ?";
    private Connection connection;

    public IdempotencyKeyDao() {
        try {
            connection = DatabaseManager.getDataSource().getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    public IdempotencyKey findById(String id) {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        IdempotencyKey result = null;
        try {
            preparedStatement = connection.prepareStatement(QUERY_BY_ID);
            preparedStatement.setString(1, id);
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                result = fromResultSet(resultSet);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            closeResultSet(resultSet);
            closePreparedStatement(preparedStatement);
        }

        return result;
    }


    public int insert(String key, Instant expiresAt) {
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = connection.prepareStatement(INSERT_EMPTY_TARGET);
            preparedStatement.setString(1, key);
            preparedStatement.setTimestamp(2, Timestamp.from(expiresAt));
            return preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            closePreparedStatement(preparedStatement);
        }
    }

    public void update(String key, String target) {
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = connection.prepareStatement(UPDATE_TARGET);
            preparedStatement.setString(1, target);
            preparedStatement.setString(2, key);
            int rowsUpdated = preparedStatement.executeUpdate();
            if (rowsUpdated != 1){
                throw new IllegalArgumentException("no key found to update");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            closePreparedStatement(preparedStatement);
        }
    }

    public List<IdempotencyKey> findWhereCreationDateBefore(
            Instant creationDateBefore,
            int limit) {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        List<IdempotencyKey> result = new ArrayList<>();
        try {
            preparedStatement = connection.prepareStatement(QUERY_expires_at_LT);
            preparedStatement.setTimestamp(1, Timestamp.from(creationDateBefore));
            preparedStatement.setInt(2, limit);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                result.add(fromResultSet(resultSet));
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            closeResultSet(resultSet);
            closePreparedStatement(preparedStatement);
        }

        return result;
    }

    public List<IdempotencyKey> findAll(Integer limit, Integer offset) {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        List<IdempotencyKey> result = new ArrayList<>();
        try {
            preparedStatement = connection.prepareStatement(QUERY_ALL);
            preparedStatement.setInt(1, Optional.ofNullable(offset).orElse(0));
            preparedStatement.setInt(2, Optional.ofNullable(limit).orElse(20));
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                IdempotencyKey idempotencyKey = fromResultSet(resultSet);
                result.add(idempotencyKey);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            closeResultSet(resultSet);
            closePreparedStatement(preparedStatement);
        }

        return result;
    }

    private IdempotencyKey fromResultSet(ResultSet resultSet) throws SQLException {
        String k = resultSet.getString("id");
        String target = resultSet.getString("target");
        Timestamp creationDateTimestamp = resultSet.getTimestamp("expires_at");
        return new IdempotencyKey(k, target, creationDateTimestamp.toInstant());
    }


    public void deleteById(String key) {
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = connection.prepareStatement(DELETE);
            preparedStatement.setString(1, key);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            closePreparedStatement(preparedStatement);
        }
    }


    private static void closePreparedStatement(PreparedStatement preparedStatement) {
        if (preparedStatement != null) {
            try {
                preparedStatement.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static void closeResultSet(ResultSet resultSet) {
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
