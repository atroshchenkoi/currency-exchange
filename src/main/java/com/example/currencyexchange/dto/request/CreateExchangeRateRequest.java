package com.example.currencyexchange.dto.request;

import java.math.BigDecimal;

public record CreateExchangeRateRequest(
        String baseCurrencyCode,
        String targetCurrencyCode,
        BigDecimal rate
) { }
