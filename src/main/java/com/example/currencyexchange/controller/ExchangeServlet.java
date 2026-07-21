package com.example.currencyexchange.controller;

import com.example.currencyexchange.config.ApplicationContext;
import com.example.currencyexchange.dto.request.ExchangeRequest;
import com.example.currencyexchange.dto.response.ExchangeResponse;
import com.example.currencyexchange.service.ExchangeService;
import com.example.currencyexchange.util.ExchangeRateValidator;
import com.example.currencyexchange.util.JsonUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/exchange")
public class ExchangeServlet extends HttpServlet {

    private ExchangeService exchangeService;

    @Override
    public void init() throws ServletException {
        Object attribute = getServletContext().getAttribute(ApplicationContext.SERVLET_CONTEXT_ATTRIBUTE);
        if (!(attribute instanceof ApplicationContext applicationContext)) {
            throw new ServletException("Application context has not been initialized");
        }
        exchangeService = applicationContext.getExchangeService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ExchangeRequest exchangeRequest = ExchangeRateValidator.validateExchange(
                request.getParameter("from"),
                request.getParameter("to"),
                request.getParameter("amount")
        );
        ExchangeResponse responseBody = exchangeService.exchange(exchangeRequest);
        JsonUtil.write(response, HttpServletResponse.SC_OK, responseBody);
    }
}
