package com.example.currencyexchange.dao;

import com.example.currencyexchange.entity.ExchangeRate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ExchangeRateDao {

    List<ExchangeRate> findAll();

    Optional<ExchangeRate> findByCurrencyCodes(String baseCurrencyCode, String targetCurrencyCode);

    ExchangeRate save(ExchangeRate exchangeRate);

    ExchangeRate updateRate(String baseCurrencyCode, String targetCurrencyCode, BigDecimal rate);

    void delete(String baseCurrencyCode, String targetCurrencyCode);
}
