package com.example.currencyexchange.service;

import com.example.currencyexchange.dao.CurrencyDao;
import com.example.currencyexchange.dao.ExchangeRateDao;
import com.example.currencyexchange.dto.request.CreateExchangeRateRequest;
import com.example.currencyexchange.dto.response.ExchangeRateResponse;
import com.example.currencyexchange.entity.Currency;
import com.example.currencyexchange.entity.ExchangeRate;
import com.example.currencyexchange.exception.ObjectNotFoundException;
import com.example.currencyexchange.mapper.ExchangeRateMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;

public class ExchangeRateService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExchangeRateService.class);

    private final ExchangeRateDao exchangeRateDao;
    private final CurrencyDao currencyDao;

    public ExchangeRateService(ExchangeRateDao exchangeRateDao, CurrencyDao currencyDao) {
        this.exchangeRateDao = exchangeRateDao;
        this.currencyDao = currencyDao;
    }

    public List<ExchangeRateResponse> findAll() {
        return exchangeRateDao.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public ExchangeRateResponse findByCurrencyCodes(String baseCurrencyCode, String targetCurrencyCode) {
        ExchangeRate exchangeRate = exchangeRateDao.findByCurrencyCodes(baseCurrencyCode, targetCurrencyCode)
                .orElseThrow(() -> new ObjectNotFoundException("Exchange rate not found"));
        return toResponse(exchangeRate);
    }

    public ExchangeRateResponse create(CreateExchangeRateRequest request) {
        Currency baseCurrency = currencyDao.findByCode(request.baseCurrencyCode())
                .orElseThrow(() -> new ObjectNotFoundException("Currency not found"));
        Currency targetCurrency = currencyDao.findByCode(request.targetCurrencyCode())
                .orElseThrow(() -> new ObjectNotFoundException("Currency not found"));
        ExchangeRate exchangeRate = new ExchangeRate(
                0L,
                baseCurrency.getId(),
                targetCurrency.getId(),
                request.rate()
        );
        ExchangeRate savedExchangeRate = exchangeRateDao.save(exchangeRate);
        LOGGER.info(
                "Exchange rate created: id={}, pair={}{}, rate={}",
                savedExchangeRate.getId(),
                baseCurrency.getCode(),
                targetCurrency.getCode(),
                savedExchangeRate.getRate()
        );
        return ExchangeRateMapper.toResponse(savedExchangeRate, baseCurrency, targetCurrency);
    }

    public ExchangeRateResponse updateRate(String baseCurrencyCode, String targetCurrencyCode, BigDecimal rate) {
        ExchangeRate updatedExchangeRate = exchangeRateDao.updateRate(baseCurrencyCode, targetCurrencyCode, rate);
        LOGGER.info(
                "Exchange rate updated: pair={}{}, rate={}",
                baseCurrencyCode,
                targetCurrencyCode,
                updatedExchangeRate.getRate()
        );
        return toResponse(updatedExchangeRate);
    }

    public void delete(String baseCurrencyCode, String targetCurrencyCode) {
        exchangeRateDao.delete(baseCurrencyCode, targetCurrencyCode);
        LOGGER.info("Exchange rate deleted: pair={}{}", baseCurrencyCode, targetCurrencyCode);
    }

    private ExchangeRateResponse toResponse(ExchangeRate exchangeRate) {
        Currency baseCurrency = currencyDao.findById(exchangeRate.getBaseCurrencyId())
                .orElseThrow(() -> new ObjectNotFoundException("Currency not found"));
        Currency targetCurrency = currencyDao.findById(exchangeRate.getTargetCurrencyId())
                .orElseThrow(() -> new ObjectNotFoundException("Currency not found"));
        return ExchangeRateMapper.toResponse(exchangeRate, baseCurrency, targetCurrency);
    }
}
