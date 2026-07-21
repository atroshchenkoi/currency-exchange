package com.example.currencyexchange.listener;

import com.example.currencyexchange.config.ApplicationContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebListener
public class ApplicationContextListener implements ServletContextListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationContextListener.class);

    @Override
    public void contextInitialized(ServletContextEvent event) {
        LOGGER.info("Starting Currency Exchange application");
        try {
            ApplicationContext applicationContext = new ApplicationContext();
            event.getServletContext().setAttribute(
                    ApplicationContext.SERVLET_CONTEXT_ATTRIBUTE,
                    applicationContext
            );
            LOGGER.info("Currency Exchange application started successfully");
        } catch (RuntimeException exception) {
            LOGGER.error("Currency Exchange application failed to start", exception);
            throw exception;
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        Object attribute = event.getServletContext().getAttribute(
                ApplicationContext.SERVLET_CONTEXT_ATTRIBUTE
        );
        if (attribute instanceof ApplicationContext applicationContext) {
            LOGGER.info("Stopping Currency Exchange application");
            try {
                applicationContext.close();
                LOGGER.info("Currency Exchange application stopped successfully");
            } catch (RuntimeException exception) {
                LOGGER.error("Currency Exchange application failed to stop cleanly", exception);
            }
        } else {
            LOGGER.warn("Application context was not found during application shutdown");
        }
    }
}
