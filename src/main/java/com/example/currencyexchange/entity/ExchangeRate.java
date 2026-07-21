package com.example.currencyexchange.entity;

import lombok.Value;

import java.math.BigDecimal;

@Value
public class ExchangeRate {
    long id;
    long baseCurrencyId;
    long targetCurrencyId;
    BigDecimal rate;
}
