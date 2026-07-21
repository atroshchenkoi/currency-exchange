package com.example.currencyexchange.config;

import com.example.currencyexchange.dao.CurrencyDao;
import com.example.currencyexchange.dao.ExchangeRateDao;
import com.example.currencyexchange.dao.jdbc.JdbcCurrencyDao;
import com.example.currencyexchange.dao.jdbc.JdbcExchangeRateDao;
import com.example.currencyexchange.service.CurrencyService;
import com.example.currencyexchange.service.ExchangeService;
import com.example.currencyexchange.service.ExchangeRateService;

public final class ApplicationContext implements AutoCloseable {

    public static final String SERVLET_CONTEXT_ATTRIBUTE = ApplicationContext.class.getName();

    private final ConnectionPool connectionPool;
    private final CurrencyService currencyService;
    private final ExchangeRateService exchangeRateService;
    private final ExchangeService exchangeService;

    public ApplicationContext() {
        connectionPool = new ConnectionPool(DatabaseConfig.load());
        CurrencyDao currencyDao = new JdbcCurrencyDao(connectionPool.getDataSource());
        currencyService = new CurrencyService(currencyDao);
        ExchangeRateDao exchangeRateDao = new JdbcExchangeRateDao(connectionPool.getDataSource());
        exchangeRateService = new ExchangeRateService(exchangeRateDao, currencyDao);
        exchangeService = new ExchangeService(exchangeRateDao, currencyDao);
    }

    public CurrencyService getCurrencyService() {
        return currencyService;
    }

    public ExchangeRateService getExchangeRateService() {
        return exchangeRateService;
    }

    public ExchangeService getExchangeService() {
        return exchangeService;
    }

    @Override
    public void close() {
        connectionPool.close();
    }
}
