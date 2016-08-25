package com.cerner.jwala.listener;

import com.cerner.jwala.service.DbService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * A listener that start H2 when the application is initialized and stops h2 when the application is destroyed
 *
 * Created by JC043760 on 8/25/2016.
 */
public class H2ServletContextListener implements ServletContextListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(H2ServletContextListener.class);

    private final DbService dbService;

    public H2ServletContextListener() {
        final ApplicationContext context = new ClassPathXmlApplicationContext(new String[] {"beans.xml"});
        dbService = (DbService) context.getBean("h2Service");
    }

    @Override
    public void contextInitialized(final ServletContextEvent sce) {
        LOGGER.info("Starting H2...");
        dbService.startServer();
    }

    @Override
    public void contextDestroyed(final ServletContextEvent sce) {
        LOGGER.info("Stopping H2...");
        dbService.stopServer();
    }
}
