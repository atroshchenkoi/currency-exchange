package com.example.currencyexchange.service;

import com.example.currencyexchange.dao.CurrencyDao;
import com.example.currencyexchange.dto.request.CreateCurrencyRequest;
import com.example.currencyexchange.dto.response.CurrencyResponse;
import com.example.currencyexchange.entity.Currency;
import com.example.currencyexchange.exception.ObjectNotFoundException;
import com.example.currencyexchange.mapper.CurrencyMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class CurrencyService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CurrencyService.class);

    private final CurrencyDao currencyDao;

    public CurrencyService(CurrencyDao currencyDao) {
        this.currencyDao = currencyDao;
    }

    public List<CurrencyResponse> findAll() {
        return currencyDao.findAll().stream()
                .map(CurrencyMapper::toResponse)
                .toList();
    }

    public CurrencyResponse create(CreateCurrencyRequest request) {
        Currency currency = new Currency(0L, request.name(), request.code(), request.sign());
        Currency savedCurrency = currencyDao.save(currency);
        LOGGER.info("Currency created: id={}, code={}", savedCurrency.getId(), savedCurrency.getCode());
        return CurrencyMapper.toResponse(savedCurrency);
    }

    public CurrencyResponse findByCode(String code) {
        return currencyDao.findByCode(code)
                .map(CurrencyMapper::toResponse)
                .orElseThrow(() -> new ObjectNotFoundException("Currency not found"));
    }
}
