package com.example.currencyexchange.controller;

import com.example.currencyexchange.config.ApplicationContext;
import com.example.currencyexchange.dto.request.CurrencyPairRequest;
import com.example.currencyexchange.dto.response.ExchangeRateResponse;
import com.example.currencyexchange.service.ExchangeRateService;
import com.example.currencyexchange.util.ExchangeRateValidator;
import com.example.currencyexchange.util.JsonUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

@WebServlet("/exchangeRate/*")
public class ExchangeRateServlet extends HttpServlet {

    private ExchangeRateService exchangeRateService;

    @Override
    public void init() throws ServletException {
        Object attribute = getServletContext().getAttribute(ApplicationContext.SERVLET_CONTEXT_ATTRIBUTE);
        if (!(attribute instanceof ApplicationContext applicationContext)) {
            throw new ServletException("Application context has not been initialized");
        }
        exchangeRateService = applicationContext.getExchangeRateService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        CurrencyPairRequest currencyPair = ExchangeRateValidator.validateCurrencyPairFromPath(request.getPathInfo());
        ExchangeRateResponse responseBody = exchangeRateService.findByCurrencyCodes(
                currencyPair.baseCurrencyCode(),
                currencyPair.targetCurrencyCode()
        );
        JsonUtil.write(response, HttpServletResponse.SC_OK, responseBody);
    }

    @Override
    protected void doPatch(HttpServletRequest request, HttpServletResponse response) throws IOException {
        CurrencyPairRequest currencyPair = ExchangeRateValidator.validateCurrencyPairFromPath(request.getPathInfo());
        ExchangeRateResponse responseBody = exchangeRateService.updateRate(
                currencyPair.baseCurrencyCode(),
                currencyPair.targetCurrencyCode(),
                ExchangeRateValidator.validateRate(readFormParameter(request, "rate"))
        );
        JsonUtil.write(response, HttpServletResponse.SC_OK, responseBody);
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        CurrencyPairRequest currencyPair = ExchangeRateValidator.validateCurrencyPairFromPath(request.getPathInfo());
        exchangeRateService.delete(currencyPair.baseCurrencyCode(), currencyPair.targetCurrencyCode());
        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    private String readFormParameter(HttpServletRequest request, String parameterName) throws IOException {
        String parameter = request.getParameter(parameterName);
        if (parameter != null) {
            return parameter;
        }

        String body = request.getReader().lines().collect(Collectors.joining("&"));
        for (String field : body.split("&")) {
            String[] keyValue = field.split("=", 2);
            if (keyValue.length == 2 && parameterName.equals(URLDecoder.decode(keyValue[0], StandardCharsets.UTF_8))) {
                return URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8);
            }
        }
        return null;
    }
}
