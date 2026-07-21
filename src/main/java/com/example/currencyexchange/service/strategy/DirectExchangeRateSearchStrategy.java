package com.example.currencyexchange.service.strategy;

import com.example.currencyexchange.dao.ExchangeRateDao;
import com.example.currencyexchange.entity.ExchangeRate;

import java.util.Optional;

public class DirectExchangeRateSearchStrategy implements ExchangeRateSearchStrategy {

    private final ExchangeRateDao exchangeRateDao;

    public DirectExchangeRateSearchStrategy(ExchangeRateDao exchangeRateDao) {
        this.exchangeRateDao = exchangeRateDao;
    }

    @Override
    public Optional<ResolvedExchangeRate> find(String baseCurrencyCode, String targetCurrencyCode) {
        return exchangeRateDao.findByCurrencyCodes(baseCurrencyCode, targetCurrencyCode)
                .map(this::toResolvedRate);
    }

    private ResolvedExchangeRate toResolvedRate(ExchangeRate exchangeRate) {
        return new ResolvedExchangeRate(
                exchangeRate.getBaseCurrencyId(),
                exchangeRate.getTargetCurrencyId(),
                exchangeRate.getRate()
        );
    }
}
