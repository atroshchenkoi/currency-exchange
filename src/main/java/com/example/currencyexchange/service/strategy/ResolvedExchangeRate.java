package com.example.currencyexchange.service.strategy;

import java.math.BigDecimal;

public record ResolvedExchangeRate(
        long baseCurrencyId,
        long targetCurrencyId,
        BigDecimal rate
) {
}
