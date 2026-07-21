package com.example.currencyexchange.service.strategy;

import com.example.currencyexchange.dao.ExchangeRateDao;
import com.example.currencyexchange.entity.ExchangeRate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Optional;

public class UsdCrossExchangeRateSearchStrategy implements ExchangeRateSearchStrategy {

    private static final Logger LOGGER = LoggerFactory.getLogger(UsdCrossExchangeRateSearchStrategy.class);
    private static final String USD = "USD";
    private static final MathContext RATE_MATH_CONTEXT = new MathContext(16, RoundingMode.HALF_UP);

    private final ExchangeRateDao exchangeRateDao;

    public UsdCrossExchangeRateSearchStrategy(ExchangeRateDao exchangeRateDao) {
        this.exchangeRateDao = exchangeRateDao;
    }

    @Override
    public Optional<ResolvedExchangeRate> find(String baseCurrencyCode, String targetCurrencyCode) {
        LOGGER.debug("Searching USD cross exchange rate for currency pair {} -> {}", baseCurrencyCode, targetCurrencyCode);
        return exchangeRateDao.findByCurrencyCodes(USD, baseCurrencyCode)
                .flatMap(usdToBase -> findUsdToTarget(usdToBase, targetCurrencyCode));
    }

    private Optional<ResolvedExchangeRate> findUsdToTarget(ExchangeRate usdToBase, String targetCurrencyCode) {
        return exchangeRateDao.findByCurrencyCodes(USD, targetCurrencyCode)
                .map(usdToTarget -> {
                    LOGGER.debug("USD cross exchange rate found for target currency id {}", usdToTarget.getTargetCurrencyId());
                    return new ResolvedExchangeRate(
                            usdToBase.getTargetCurrencyId(),
                            usdToTarget.getTargetCurrencyId(),
                            usdToTarget.getRate().divide(usdToBase.getRate(), RATE_MATH_CONTEXT)
                    );
                });
    }
}
