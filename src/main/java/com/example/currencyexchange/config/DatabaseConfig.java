package com.example.currencyexchange.config;

import lombok.Value;
import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Value
public class DatabaseConfig {

    private static final String CONFIG_FILE = "application.properties";
    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseConfig.class);

    String url;
    String username;
    @ToString.Exclude
    String password;
    String driverClassName;
    int maximumPoolSize;

    public static DatabaseConfig load() {
        Properties properties = loadProperties();
        String maximumPoolSizeValue = value(
                properties,
                "database.pool.maximum-size",
                "DB_POOL_MAXIMUM_SIZE"
        );
        int maximumPoolSize;
        try {
            maximumPoolSize = Integer.parseInt(maximumPoolSizeValue);
        } catch (NumberFormatException exception) {
            throw new IllegalStateException("database.pool.maximum-size must be a positive integer", exception);
        }
        if (maximumPoolSize <= 0) {
            throw new IllegalStateException("database.pool.maximum-size must be a positive integer");
        }

        DatabaseConfig databaseConfig = new DatabaseConfig(
                value(properties, "database.url", "DB_URL"),
                value(properties, "database.username", "DB_USERNAME"),
                value(properties, "database.password", "DB_PASSWORD"),
                value(properties, "database.driver-class-name", "DB_DRIVER_CLASS_NAME"),
                maximumPoolSize
        );
        LOGGER.info(
                "Database configuration loaded: driver={}, maximumPoolSize={}",
                databaseConfig.getDriverClassName(),
                databaseConfig.getMaximumPoolSize()
        );
        return databaseConfig;
    }

    private static Properties loadProperties() {
        Properties properties = new Properties();
        ClassLoader classLoader = DatabaseConfig.class.getClassLoader();

        try (InputStream inputStream = classLoader.getResourceAsStream(CONFIG_FILE)) {
            if (inputStream == null) {
                throw new IllegalStateException(CONFIG_FILE + " was not found in classpath");
            }
            properties.load(inputStream);
            return properties;
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to read " + CONFIG_FILE, exception);
        }
    }

    private static String value(Properties properties, String propertyName, String environmentName) {
        String environmentValue = System.getenv(environmentName);
        if (environmentValue != null && !environmentValue.isBlank()) {
            return environmentValue;
        }

        String propertyValue = properties.getProperty(propertyName);
        if (propertyValue == null) {
            throw new IllegalStateException("Missing database property: " + propertyName);
        }
        return propertyValue;
    }
}
