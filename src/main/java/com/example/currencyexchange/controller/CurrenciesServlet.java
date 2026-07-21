package com.example.currencyexchange.controller;

import com.example.currencyexchange.config.ApplicationContext;
import com.example.currencyexchange.dto.request.CreateCurrencyRequest;
import com.example.currencyexchange.dto.response.CurrencyResponse;
import com.example.currencyexchange.service.CurrencyService;
import com.example.currencyexchange.util.CurrencyValidator;
import com.example.currencyexchange.util.JsonUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/currencies")
public class CurrenciesServlet extends HttpServlet {

    private CurrencyService currencyService;

    @Override
    public void init() throws ServletException {
        Object attribute = getServletContext().getAttribute(ApplicationContext.SERVLET_CONTEXT_ATTRIBUTE);
        if (!(attribute instanceof ApplicationContext applicationContext)) {
            throw new ServletException("Application context has not been initialized");
        }
        currencyService = applicationContext.getCurrencyService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        JsonUtil.write(response, HttpServletResponse.SC_OK, currencyService.findAll());
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        CreateCurrencyRequest createRequest = CurrencyValidator.validateCreation(
                request.getParameter("name"),
                request.getParameter("code"),
                request.getParameter("sign")
        );
        CurrencyResponse responseBody = currencyService.create(createRequest);
        JsonUtil.write(response, HttpServletResponse.SC_CREATED, responseBody);
    }
}
