package com.example.currencyexchange.dao.jdbc;

import com.example.currencyexchange.dao.CurrencyDao;
import com.example.currencyexchange.exception.DatabaseException;
import com.example.currencyexchange.exception.AlreadyExistsException;
import com.example.currencyexchange.entity.Currency;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcCurrencyDao implements CurrencyDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(JdbcCurrencyDao.class);

    private static final String FIND_ALL_SQL = """
            SELECT id, full_name, code, sign
            FROM currencies
            ORDER BY id
            """;

    private static final String FIND_BY_CODE_SQL = """
            SELECT id, full_name, code, sign
            FROM currencies
            WHERE code = ?
            """;

    private static final String FIND_BY_ID_SQL = """
            SELECT id, full_name, code, sign
            FROM currencies
            WHERE id = ?
            """;

    private static final String SAVE_SQL = """
            INSERT INTO currencies (full_name, code, sign)
            VALUES (?, ?, ?)
            RETURNING id, full_name, code, sign
            """;

    private static final String POSTGRES_DUPLICATE_KEY_SQL_STATE = "23505";

    private final DataSource dataSource;

    public JdbcCurrencyDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public List<Currency> findAll() {
        List<Currency> currencies = new ArrayList<>();

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_ALL_SQL);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                currencies.add(mapRow(resultSet));
            }
            return currencies;
        } catch (SQLException exception) {
            LOGGER.error("Failed to load currencies", exception);
            throw new DatabaseException("Database is unavailable");
        }
    }

    @Override
    public Optional<Currency> findByCode(String code) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_BY_CODE_SQL)) {

            statement.setString(1, code);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapRow(resultSet));
                }
                return Optional.empty();
            }
        } catch (SQLException exception) {
            LOGGER.error("Failed to load currency with code {}", code, exception);
            throw new DatabaseException("Database is unavailable");
        }
    }

    @Override
    public Optional<Currency> findById(long id) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_BY_ID_SQL)) {

            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapRow(resultSet));
                }
                return Optional.empty();
            }
        } catch (SQLException exception) {
            LOGGER.error("Failed to load currency with ID {}", id, exception);
            throw new DatabaseException("Database is unavailable");
        }
    }

    @Override
    public Currency save(Currency currency) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(SAVE_SQL)) {

            statement.setString(1, currency.getName());
            statement.setString(2, currency.getCode());
            statement.setString(3, currency.getSign());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    LOGGER.error("Currency insert returned no row for code {}", currency.getCode());
                    throw new DatabaseException("Database is unavailable");
                }
                Currency savedCurrency = mapRow(resultSet);
                LOGGER.debug(
                        "Created currency with ID {} and code {}",
                        savedCurrency.getId(),
                        savedCurrency.getCode()
                );
                return savedCurrency;
            }
        } catch (SQLException exception) {
            if (POSTGRES_DUPLICATE_KEY_SQL_STATE.equals(exception.getSQLState())) {
                LOGGER.debug("Currency with code {} already exists", currency.getCode());
                throw new AlreadyExistsException("Currency with code " + currency.getCode() + " already exists");
            }
            LOGGER.error("Failed to create currency with code {}", currency.getCode(), exception);
            throw new DatabaseException("Database is unavailable");
        }
    }

    private Currency mapRow(ResultSet resultSet) throws SQLException {
        return new Currency(
                resultSet.getLong("id"),
                resultSet.getString("full_name"),
                resultSet.getString("code"),
                resultSet.getString("sign")
        );
    }
}
