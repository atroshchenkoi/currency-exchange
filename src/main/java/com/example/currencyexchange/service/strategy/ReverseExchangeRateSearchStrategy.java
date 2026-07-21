package com.example.currencyexchange.service.strategy;

import com.example.currencyexchange.dao.ExchangeRateDao;
import com.example.currencyexchange.entity.ExchangeRate;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Optional;

public class ReverseExchangeRateSearchStrategy implements ExchangeRateSearchStrategy {

    private final ExchangeRateDao exchangeRateDao;

    public ReverseExchangeRateSearchStrategy(ExchangeRateDao exchangeRateDao) {
        this.exchangeRateDao = exchangeRateDao;
    }

    @Override
    public Optional<ResolvedExchangeRate> find(String baseCurrencyCode, String targetCurrencyCode) {
        return exchangeRateDao.findByCurrencyCodes(targetCurrencyCode, baseCurrencyCode)
                .map(this::toResolvedRate);
    }

    private ResolvedExchangeRate toResolvedRate(ExchangeRate exchangeRate) {
        return new ResolvedExchangeRate(
                exchangeRate.getTargetCurrencyId(),
                exchangeRate.getBaseCurrencyId(),
                BigDecimal.ONE.divide(exchangeRate.getRate(), MathContext.DECIMAL64)
        );
    }
}
