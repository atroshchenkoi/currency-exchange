package com.example.currencyexchange.dao;

import com.example.currencyexchange.entity.Currency;

import java.util.List;
import java.util.Optional;

public interface CurrencyDao {

    List<Currency> findAll();

    Optional<Currency> findByCode(String code);

    Optional<Currency> findById(long id);

    Currency save(Currency currency);
}
