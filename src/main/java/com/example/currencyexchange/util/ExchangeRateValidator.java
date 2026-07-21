package com.example.currencyexchange.util;

import com.example.currencyexchange.dto.request.CreateExchangeRateRequest;
import com.example.currencyexchange.dto.request.CurrencyPairRequest;
import com.example.currencyexchange.dto.request.ExchangeRequest;
import com.example.currencyexchange.exception.ValidationException;

import java.math.BigDecimal;
import java.util.regex.Pattern;

public final class ExchangeRateValidator {

    private static final Pattern CURRENCY_PAIR_PATTERN = Pattern.compile("[A-Za-z]{6}");
    private static final int RATE_MAX_FRACTION_DIGITS = 6;
    private static final BigDecimal RATE_UPPER_BOUND = new BigDecimal("1000000");

    private ExchangeRateValidator() {
    }

    public static CreateExchangeRateRequest validateCreation(String baseCurrencyCode, String targetCurrencyCode, String rate) {
        String normalizedBaseCode = CurrencyValidator.validateCode(baseCurrencyCode);
        String normalizedTargetCode = CurrencyValidator.validateCode(targetCurrencyCode);
        validateDifferentCurrencies(normalizedBaseCode, normalizedTargetCode);
        return new CreateExchangeRateRequest(
                normalizedBaseCode,
                normalizedTargetCode,
                validateRate(rate)
        );
    }

    private static void validateDifferentCurrencies(String baseCurrencyCode, String targetCurrencyCode) {
        if (baseCurrencyCode.equals(targetCurrencyCode)) {
            throw new ValidationException("Base and target currencies must be different");
        }
    }

    public static ExchangeRequest validateExchange(String fromCurrencyCode, String toCurrencyCode, String amount) {
        return new ExchangeRequest(
                CurrencyValidator.validateCode(fromCurrencyCode),
                CurrencyValidator.validateCode(toCurrencyCode),
                validatePositiveDecimal(amount, "amount")
        );
    }

    public static CurrencyPairRequest validateCurrencyPairFromPath(String pathInfo) {
        if (pathInfo == null || pathInfo.equals("/")) {
            throw new ValidationException("Currency pair codes are missing from the request path");
        }

        String currencyPair = pathInfo.substring(1);
        if (!CURRENCY_PAIR_PATTERN.matcher(currencyPair).matches()) {
            throw new ValidationException(
                    "Currency pair must contain two 3-letter currency codes"
            );
        }

        return new CurrencyPairRequest(
                CurrencyValidator.validateCode(currencyPair.substring(0, 3)),
                CurrencyValidator.validateCode(currencyPair.substring(3, 6))
        );
    }

    public static BigDecimal validateRate(String rate) {
        BigDecimal decimalRate = validatePositiveDecimal(rate, "rate");
        BigDecimal normalizedRate = decimalRate.stripTrailingZeros();
        int fractionDigits = Math.max(normalizedRate.scale(), 0);

        if (decimalRate.compareTo(RATE_UPPER_BOUND) >= 0) {
            throw new ValidationException("Rate must have no more than 6 digits before the decimal point");
        }
        if (fractionDigits > RATE_MAX_FRACTION_DIGITS) {
            throw new ValidationException("Rate must have no more than 6 digits after the decimal point");
        }
        return decimalRate;
    }

    private static BigDecimal validatePositiveDecimal(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new ValidationException("Required field (rate) is missing.");
        }

        try {
            BigDecimal decimalValue = new BigDecimal(value.trim());
            if (decimalValue.signum() <= 0) {
                throw new ValidationException(fieldName + " must be greater than zero");
            }
            return decimalValue;
        } catch (NumberFormatException exception) {
            throw new ValidationException(fieldName + " must be a decimal number");
        }
    }
}
