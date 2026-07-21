package com.example.currencyexchange.service;

import com.example.currencyexchange.dao.CurrencyDao;
import com.example.currencyexchange.dao.ExchangeRateDao;
import com.example.currencyexchange.dto.request.ExchangeRequest;
import com.example.currencyexchange.dto.response.ExchangeResponse;
import com.example.currencyexchange.entity.Currency;
import com.example.currencyexchange.exception.ObjectNotFoundException;
import com.example.currencyexchange.mapper.CurrencyMapper;
import com.example.currencyexchange.service.strategy.DirectExchangeRateSearchStrategy;
import com.example.currencyexchange.service.strategy.ExchangeRateSearchStrategy;
import com.example.currencyexchange.service.strategy.ResolvedExchangeRate;
import com.example.currencyexchange.service.strategy.ReverseExchangeRateSearchStrategy;
import com.example.currencyexchange.service.strategy.UsdCrossExchangeRateSearchStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.RoundingMode;
import java.util.List;

public class ExchangeService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExchangeService.class);
    private static final int CONVERTED_AMOUNT_SCALE = 2;

    private final CurrencyDao currencyDao;
    private final List<ExchangeRateSearchStrategy> searchStrategies;

    public ExchangeService(ExchangeRateDao exchangeRateDao, CurrencyDao currencyDao) {
        this.currencyDao = currencyDao;
        searchStrategies = List.of(
                new DirectExchangeRateSearchStrategy(exchangeRateDao),
                new ReverseExchangeRateSearchStrategy(exchangeRateDao),
                new UsdCrossExchangeRateSearchStrategy(exchangeRateDao)
        );
    }

    public ExchangeResponse exchange(ExchangeRequest request) {
        LOGGER.debug(
                "Searching exchange rate for currency pair {} -> {}",
                request.fromCurrencyCode(),
                request.toCurrencyCode()
        );

        if (request.fromCurrencyCode().equals(request.toCurrencyCode())) {
            LOGGER.debug("Exchange rate requested for identical currency codes: {}", request.fromCurrencyCode());
            throw new ObjectNotFoundException("Exchange rate not found");
        }

        ResolvedExchangeRate exchangeRate = searchStrategies.stream()
                .map(strategy -> {
                    var result = strategy.find(request.fromCurrencyCode(), request.toCurrencyCode());
                    result.ifPresent(ignored -> LOGGER.debug(
                            "Exchange rate for {} -> {} resolved by {}",
                            request.fromCurrencyCode(),
                            request.toCurrencyCode(),
                            strategy.getClass().getSimpleName()
                    ));
                    return result;
                })
                .flatMap(java.util.Optional::stream)
                .findFirst()
                .orElseThrow(() -> {
                    LOGGER.debug(
                            "Exchange rate not found for currency pair {} -> {}",
                            request.fromCurrencyCode(),
                            request.toCurrencyCode()
                    );
                    return new ObjectNotFoundException("Exchange rate not found");
                });

        Currency baseCurrency = currencyDao.findById(exchangeRate.baseCurrencyId())
                .orElseThrow(() -> {
                    LOGGER.error("Base currency with id {} was not found", exchangeRate.baseCurrencyId());
                    return new ObjectNotFoundException("Currency not found");
                });
        Currency targetCurrency = currencyDao.findById(exchangeRate.targetCurrencyId())
                .orElseThrow(() -> {
                    LOGGER.error("Target currency with id {} was not found", exchangeRate.targetCurrencyId());
                    return new ObjectNotFoundException("Currency not found");
                });

        return new ExchangeResponse(
                CurrencyMapper.toResponse(baseCurrency),
                CurrencyMapper.toResponse(targetCurrency),
                exchangeRate.rate(),
                request.amount(),
                request.amount()
                        .multiply(exchangeRate.rate())
                        .setScale(CONVERTED_AMOUNT_SCALE, RoundingMode.HALF_UP)
        );
    }
}
