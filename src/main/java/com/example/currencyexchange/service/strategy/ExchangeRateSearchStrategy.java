package com.example.currencyexchange.service.strategy;

import java.util.Optional;

public interface ExchangeRateSearchStrategy {

    Optional<ResolvedExchangeRate> find(String baseCurrencyCode, String targetCurrencyCode);
}
