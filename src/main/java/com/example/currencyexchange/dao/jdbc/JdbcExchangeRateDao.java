package com.example.currencyexchange.dao.jdbc;

import com.example.currencyexchange.dao.ExchangeRateDao;
import com.example.currencyexchange.exception.DatabaseException;
import com.example.currencyexchange.exception.AlreadyExistsException;
import com.example.currencyexchange.exception.ObjectNotFoundException;
import com.example.currencyexchange.entity.ExchangeRate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcExchangeRateDao implements ExchangeRateDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(JdbcExchangeRateDao.class);

    private static final String FIND_ALL_SQL = """
            SELECT id, base_currency_id, target_currency_id, rate
            FROM exchange_rates
            """;

    private static final String FIND_BY_CURRENCY_CODES_SQL = """
            SELECT id, base_currency_id, target_currency_id, rate
            FROM exchange_rates
            WHERE base_currency_id = (SELECT id FROM currencies WHERE code = ?)
              AND target_currency_id = (SELECT id FROM currencies WHERE code = ?)
            """;

    private static final String SAVE_SQL = """
            INSERT INTO exchange_rates (base_currency_id, target_currency_id, rate)
            VALUES (?, ?, ?)
            RETURNING id, base_currency_id, target_currency_id, rate
            """;

    private static final String UPDATE_RATE_SQL = """
            UPDATE exchange_rates
            SET rate = ?
            WHERE base_currency_id = (SELECT id FROM currencies WHERE code = ?)
              AND target_currency_id = (SELECT id FROM currencies WHERE code = ?)
            RETURNING id, base_currency_id, target_currency_id, rate
            """;

    private static final String DELETE_SQL = """
            DELETE FROM exchange_rates
            WHERE base_currency_id = (SELECT id FROM currencies WHERE code = ?)
              AND target_currency_id = (SELECT id FROM currencies WHERE code = ?)
            """;

    private static final String POSTGRES_DUPLICATE_KEY_SQL_STATE = "23505";

    private final DataSource dataSource;

    public JdbcExchangeRateDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public List<ExchangeRate> findAll() {
        List<ExchangeRate> exchangeRates = new ArrayList<>();

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_ALL_SQL);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                exchangeRates.add(mapRow(resultSet));
            }
            return exchangeRates;
        } catch (SQLException exception) {
            LOGGER.error("Failed to load exchange rates", exception);
            throw new DatabaseException("Database is unavailable");
        }
    }

    @Override
    public Optional<ExchangeRate> findByCurrencyCodes(String baseCurrencyCode, String targetCurrencyCode) {
        try (Connection connection = dataSource.getConnection()) {
            return findByCurrencyCodes(connection, baseCurrencyCode, targetCurrencyCode);
        } catch (SQLException exception) {
            LOGGER.error(
                    "Failed to load exchange rate for currency pair {}/{}",
                    baseCurrencyCode,
                    targetCurrencyCode,
                    exception
            );
            throw new DatabaseException("Database is unavailable");
        }
    }

    @Override
    public ExchangeRate save(ExchangeRate exchangeRate) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(SAVE_SQL)) {

            statement.setLong(1, exchangeRate.getBaseCurrencyId());
            statement.setLong(2, exchangeRate.getTargetCurrencyId());
            statement.setBigDecimal(3, exchangeRate.getRate());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    LOGGER.error(
                            "Exchange rate insert returned no row for currency IDs {}/{}",
                            exchangeRate.getBaseCurrencyId(),
                            exchangeRate.getTargetCurrencyId()
                    );
                    throw new DatabaseException("Database is unavailable");
                }
                ExchangeRate savedExchangeRate = mapRow(resultSet);
                LOGGER.debug(
                        "Created exchange rate with ID {} for currency IDs {}/{}",
                        savedExchangeRate.getId(),
                        savedExchangeRate.getBaseCurrencyId(),
                        savedExchangeRate.getTargetCurrencyId()
                );
                return savedExchangeRate;
            }
        } catch (SQLException exception) {
            if (POSTGRES_DUPLICATE_KEY_SQL_STATE.equals(exception.getSQLState())) {
                LOGGER.debug(
                        "Exchange rate for currency IDs {}/{} already exists",
                        exchangeRate.getBaseCurrencyId(),
                        exchangeRate.getTargetCurrencyId()
                );
                throw new AlreadyExistsException("Exchange rate for this currency pair already exists");
            }
            LOGGER.error(
                    "Failed to create exchange rate for currency IDs {}/{}",
                    exchangeRate.getBaseCurrencyId(),
                    exchangeRate.getTargetCurrencyId(),
                    exception
            );
            throw new DatabaseException("Database is unavailable");
        }
    }

    @Override
    public ExchangeRate updateRate(String baseCurrencyCode, String targetCurrencyCode, BigDecimal rate) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_RATE_SQL)) {

            statement.setBigDecimal(1, rate);
            statement.setString(2, baseCurrencyCode);
            statement.setString(3, targetCurrencyCode);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    LOGGER.debug(
                            "Cannot update exchange rate because currency pair {}/{} was not found",
                            baseCurrencyCode,
                            targetCurrencyCode
                    );
                    throw new ObjectNotFoundException("Exchange rate not found");
                }

                ExchangeRate updatedExchangeRate = mapRow(resultSet);
                LOGGER.debug(
                        "Updated exchange rate with ID {} for currency pair {}/{}",
                        updatedExchangeRate.getId(),
                        baseCurrencyCode,
                        targetCurrencyCode
                );
                return updatedExchangeRate;
            }
        } catch (SQLException exception) {
            LOGGER.error(
                    "Failed to update exchange rate for currency pair {}/{}",
                    baseCurrencyCode,
                    targetCurrencyCode,
                    exception
            );
            throw new DatabaseException("Database is unavailable");
        }
    }

    @Override
    public void delete(String baseCurrencyCode, String targetCurrencyCode) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE_SQL)) {

            statement.setString(1, baseCurrencyCode);
            statement.setString(2, targetCurrencyCode);
            if (statement.executeUpdate() == 0) {
                LOGGER.debug(
                        "Cannot delete exchange rate because currency pair {}/{} was not found",
                        baseCurrencyCode,
                        targetCurrencyCode
                );
                throw new ObjectNotFoundException("Exchange rate not found");
            }
            LOGGER.debug(
                    "Deleted exchange rate for currency pair {}/{}",
                    baseCurrencyCode,
                    targetCurrencyCode
            );
        } catch (SQLException exception) {
            LOGGER.error(
                    "Failed to delete exchange rate for currency pair {}/{}",
                    baseCurrencyCode,
                    targetCurrencyCode,
                    exception
            );
            throw new DatabaseException("Database is unavailable");
        }
    }

    private Optional<ExchangeRate> findByCurrencyCodes(
            Connection connection,
            String baseCurrencyCode,
            String targetCurrencyCode
    ) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(FIND_BY_CURRENCY_CODES_SQL)) {
            statement.setString(1, baseCurrencyCode);
            statement.setString(2, targetCurrencyCode);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapRow(resultSet));
                }
                return Optional.empty();
            }
        }
    }

    private ExchangeRate mapRow(ResultSet resultSet) throws SQLException {
        return new ExchangeRate(
                resultSet.getLong(1),
                resultSet.getLong(2),
                resultSet.getLong(3),
                resultSet.getBigDecimal(4)
        );
    }
}
