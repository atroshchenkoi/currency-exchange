package com.example.currencyexchange.dto.response;

import java.math.BigDecimal;

public record ExchangeRateResponse(
        long id,
        CurrencyResponse baseCurrency,
        CurrencyResponse targetCurrency,
        BigDecimal rate
) {
}
