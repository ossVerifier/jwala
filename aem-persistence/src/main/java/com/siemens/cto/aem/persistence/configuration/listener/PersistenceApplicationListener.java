package com.siemens.cto.aem.persistence.configuration.listener;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import com.siemens.cto.aem.domain.model.temporary.PaginationParameter;
import com.siemens.cto.aem.persistence.dao.app.ApplicationDao;

/**
 * Listen to events and eagerly initialize OpenJPA
 */
public class PersistenceApplicationListener implements ApplicationListener<ContextRefreshedEvent> {

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (event instanceof ContextRefreshedEvent) {
            ApplicationContext applicationContext = ((ContextRefreshedEvent) event).getApplicationContext();
            ApplicationDao simpleDao = applicationContext.getBean(ApplicationDao.class);
            simpleDao.getApplications(new PaginationParameter(1, 0)); // read nothing.
        }      
    }
}
