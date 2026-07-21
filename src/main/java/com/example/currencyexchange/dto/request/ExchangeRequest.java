package com.example.currencyexchange.dto.request;

import java.math.BigDecimal;

public record ExchangeRequest(
        String fromCurrencyCode,
        String toCurrencyCode,
        BigDecimal amount
) { }
