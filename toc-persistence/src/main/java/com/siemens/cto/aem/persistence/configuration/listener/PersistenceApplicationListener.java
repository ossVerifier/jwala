package com.siemens.cto.aem.persistence.configuration.listener;

import com.siemens.cto.aem.persistence.dao.app.ApplicationDao;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

/**
 * Listen to events and eagerly initialize OpenJPA
 */
public class PersistenceApplicationListener implements ApplicationListener<ContextRefreshedEvent> {

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (event instanceof ContextRefreshedEvent) {
            ApplicationContext applicationContext = event.getApplicationContext();
            ApplicationDao simpleDao = applicationContext.getBean(ApplicationDao.class);
            simpleDao.getApplications(); // read nothing.
        }      
    }
}
