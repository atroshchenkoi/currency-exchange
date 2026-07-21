package com.example.currencyexchange.mapper;

import com.example.currencyexchange.dto.response.CurrencyResponse;
import com.example.currencyexchange.entity.Currency;

public final class CurrencyMapper {

    private CurrencyMapper() {
    }

    public static CurrencyResponse toResponse(Currency currency) {
        return new CurrencyResponse(
                currency.getId(),
                currency.getName(),
                currency.getCode(),
                currency.getSign()
        );
    }
}
