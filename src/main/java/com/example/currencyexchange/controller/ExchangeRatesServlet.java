package com.example.currencyexchange.controller;

import com.example.currencyexchange.config.ApplicationContext;
import com.example.currencyexchange.dto.request.CreateExchangeRateRequest;
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

@WebServlet("/exchangeRates")
public class ExchangeRatesServlet extends HttpServlet {

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
        JsonUtil.write(response, HttpServletResponse.SC_OK, exchangeRateService.findAll());
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        CreateExchangeRateRequest createRequest = ExchangeRateValidator.validateCreation(
                request.getParameter("baseCurrencyCode"),
                request.getParameter("targetCurrencyCode"),
                request.getParameter("rate")
        );
        ExchangeRateResponse responseBody = exchangeRateService.create(createRequest);
        JsonUtil.write(response, HttpServletResponse.SC_CREATED, responseBody);
    }
}
