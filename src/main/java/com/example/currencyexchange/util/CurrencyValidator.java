package com.example.currencyexchange.util;

import com.example.currencyexchange.dto.request.CreateCurrencyRequest;
import com.example.currencyexchange.exception.ValidationException;

import java.util.Locale;

public final class CurrencyValidator {

    private static final String CURRENCY_CODE_PATTERN = "[A-Za-z]{3}";
    private static final int MAX_CURRENCY_NAME_LENGTH = 100;

    private CurrencyValidator() {
    }

    public static CreateCurrencyRequest validateCreation(
            String name,
            String code,
            String sign
    ) {
        String normalizedName = validateName(name);
        String normalizedCode = validateCode(code);
        String normalizedSign = validateSign(sign);

        return new CreateCurrencyRequest(
                normalizedName,
                normalizedCode,
                normalizedSign
        );
    }

    public static String validateCodeFromPath(String pathInfo) {
        if (pathInfo == null || pathInfo.equals("/")) {
            throw new ValidationException("Currency code is missing from the request path");
        }
        return validateCode(pathInfo.substring(1));
    }

    public static String validateCode(String code) {
        String normalizedCode = requireNotBlank(code, "code");
        if (!normalizedCode.matches(CURRENCY_CODE_PATTERN)) {
            throw new ValidationException(
                    "Currency code must contain exactly 3 Latin letters"
            );
        }
        return normalizedCode.toUpperCase(Locale.ROOT);
    }

    private static String validateName(String name) {
        String normalizedName = requireNotBlank(name, "name");
        if (normalizedName.codePointCount(0, normalizedName.length()) > MAX_CURRENCY_NAME_LENGTH) {
            throw new ValidationException(
                    "Currency name must not exceed 100 characters"
            );
        }
        return normalizedName;
    }

    private static String validateSign(String sign) {
        String normalizedSign = requireNotBlank(sign, "sign");
        if (normalizedSign.codePointCount(0, normalizedSign.length()) != 1) {
            throw new ValidationException(
                    "Currency sign must contain exactly 1 character"
            );
        }
        return normalizedSign;
    }

    private static String requireNotBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new ValidationException(
                    "Required field is missing: " + fieldName
            );
        }
        return value.trim();
    }
}
