package com.example.currencyexchange.mapper;

import com.example.currencyexchange.dto.response.ExchangeRateResponse;
import com.example.currencyexchange.entity.Currency;
import com.example.currencyexchange.entity.ExchangeRate;

public final class ExchangeRateMapper {

    private ExchangeRateMapper() {
    }

    public static ExchangeRateResponse toResponse(
            ExchangeRate exchangeRate,
            Currency baseCurrency,
            Currency targetCurrency
    ) {
        return new ExchangeRateResponse(
                exchangeRate.getId(),
                CurrencyMapper.toResponse(baseCurrency),
                CurrencyMapper.toResponse(targetCurrency),
                exchangeRate.getRate()
        );
    }
}
