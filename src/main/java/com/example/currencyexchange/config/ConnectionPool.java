package com.example.currencyexchange.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

public final class ConnectionPool implements AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionPool.class);
    private static final String POOL_NAME = "currency-exchange-pool";

    private final HikariDataSource dataSource;

    public ConnectionPool(DatabaseConfig databaseConfig) {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(databaseConfig.getUrl());
        hikariConfig.setUsername(databaseConfig.getUsername());
        hikariConfig.setPassword(databaseConfig.getPassword());
        hikariConfig.setMaximumPoolSize(databaseConfig.getMaximumPoolSize());
        hikariConfig.setMinimumIdle(1);
        hikariConfig.setConnectionTimeout(10_000);
        hikariConfig.setPoolName(POOL_NAME);
        hikariConfig.setDriverClassName(databaseConfig.getDriverClassName());
        LOGGER.info(
                "Starting database connection pool: name={}, maximumPoolSize={}",
                POOL_NAME,
                databaseConfig.getMaximumPoolSize()
        );
        dataSource = new HikariDataSource(hikariConfig);
        LOGGER.info("Database connection pool started: name={}", POOL_NAME);
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    @Override
    public void close() {
        LOGGER.info("Stopping database connection pool: name={}", POOL_NAME);
        dataSource.close();
        LOGGER.info("Database connection pool stopped: name={}", POOL_NAME);
    }
}
