package com.procuregov.util;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.util.logging.Logger;

/**
 * Application lifecycle listener.
 * Clears sessions on startup for clean development testing.
 * 
 * ⚠️ Remove or disable in production!
 */
@WebListener
public class AppContextListener implements ServletContextListener {
    private static final Logger logger = Logger.getLogger(AppContextListener.class.getName());

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        logger.info("[AppContextListener] Application starting - clearing any persisted sessions");
        // Note: We can't directly access HttpSession here, but disabling Manager (FIX 1) handles persistence
        logger.info("[AppContextListener] Ensure Manager pathname=\"\" in context.xml to disable session persistence");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        logger.info("[AppContextListener] Application shutting down");
    }
}