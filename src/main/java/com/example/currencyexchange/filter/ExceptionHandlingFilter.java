package com.example.currencyexchange.filter;

import com.example.currencyexchange.dto.response.ErrorResponse;
import com.example.currencyexchange.exception.AlreadyExistsException;
import com.example.currencyexchange.exception.DatabaseException;
import com.example.currencyexchange.exception.ObjectNotFoundException;
import com.example.currencyexchange.exception.ValidationException;
import com.example.currencyexchange.util.JsonUtil;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebFilter("/*")
public class ExceptionHandlingFilter implements Filter {

    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain
    ) throws IOException, ServletException {
        try {
            chain.doFilter(request, response);
        } catch (ValidationException exception) {
            JsonUtil.write(
                    (HttpServletResponse) response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    new ErrorResponse(exception.getMessage())
            );
        } catch (AlreadyExistsException exception) {
            JsonUtil.write(
                    (HttpServletResponse) response,
                    HttpServletResponse.SC_CONFLICT,
                    new ErrorResponse(exception.getMessage())
            );
        } catch (ObjectNotFoundException exception) {
            JsonUtil.write(
                    (HttpServletResponse) response,
                    HttpServletResponse.SC_NOT_FOUND,
                    new ErrorResponse(exception.getMessage())
            );
        } catch (DatabaseException exception) {
            JsonUtil.write(
                    (HttpServletResponse) response,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    new ErrorResponse(exception.getMessage())
            );
        }
    }
}
