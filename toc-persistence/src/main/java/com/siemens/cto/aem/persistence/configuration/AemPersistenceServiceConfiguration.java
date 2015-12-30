package com.siemens.cto.aem.persistence.configuration;

import com.siemens.cto.aem.common.domain.model.jvm.Jvm;
import com.siemens.cto.aem.common.domain.model.jvm.JvmState;
import com.siemens.cto.aem.common.domain.model.webserver.WebServer;
import com.siemens.cto.aem.common.domain.model.webserver.WebServerReachableState;
import com.siemens.cto.aem.persistence.jpa.service.*;
import com.siemens.cto.aem.persistence.jpa.service.impl.*;
import com.siemens.cto.aem.persistence.service.*;
import com.siemens.cto.aem.persistence.service.impl.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.support.SharedEntityManagerBean;

@Configuration
public class AemPersistenceServiceConfiguration {

    @Autowired
    private SharedEntityManagerBean sharedEntityManager;

    @Bean
    public ResourcePersistenceService getResourcePersistenceService() {
        return new JpaResourcePersistenceServiceImpl(getResourceInstanceCrudService());
    }

    @Bean(name="jvmPersistenceService")
    public JvmPersistenceService getJvmPersistenceService() {
        return new JpaJvmPersistenceServiceImpl(getJvmCrudService(),
                                                getGroupJvmRelationshipService());
    }

    @Bean(name="groupPersistenceService")
    public GroupPersistenceService getGroupPersistenceService() {
        return new JpaGroupPersistenceServiceImpl(getGroupCrudService(),
                                                  getGroupJvmRelationshipService());
    }

    @Bean
    protected GroupJvmRelationshipService getGroupJvmRelationshipService() {
        return new GroupJvmRelationshipServiceImpl(getGroupCrudService(),
                                                   getJvmCrudService());
    }

    @Bean
    protected GroupCrudService getGroupCrudService() {
        return new GroupCrudServiceImpl();
    }

    @Bean
    protected JvmCrudService getJvmCrudService() {
        return new JvmCrudServiceImpl();
    }

    @Bean
    protected ResourceInstanceCrudService getResourceInstanceCrudService() {
        return new ResourceInstanceCrudServiceImpl(getGroupCrudService());
    }

    @Bean
    protected ApplicationCrudService getApplicationCrudService() {
        return new ApplicationCrudServiceImpl();
    }

    @Bean
    public ApplicationPersistenceService getApplicationPersistenceService() {
        return new JpaApplicationPersistenceServiceImpl(getApplicationCrudService(), getGroupCrudService());
    }

    @Bean(name = "webServerStatePersistenceService")
    public StatePersistenceService<WebServer, WebServerReachableState> getWebServerStatePersistenceService() {
        return new WebServerJpaStatePersistenceServiceImpl(getWebServerStateCrudService());
    }

    @Bean(name = "webServerStateCrudService")
    protected StateCrudService<WebServer, WebServerReachableState> getWebServerStateCrudService() {
        return new WebServerStateCrudServiceImpl();
    }

    @Bean(name = "jvmStatePersistenceService")
    public StatePersistenceService<Jvm, JvmState> getJvmStatePersistenceService() {
        return new JvmJpaStatePersistenceServiceImpl(getJvmStateCrudService());
    }

    @Bean
    public WebServerPersistenceService getWebServerPersistenceService() {
        return new WebServerPersistenceServiceImpl(getGroupCrudService(), getWebserverCrudService());
    }

    @Bean(name="webServerCrudService")
    public WebServerCrudService getWebserverCrudService() {
        return new WebServerCrudServiceImpl();
    }

    @Bean(name = "jvmStateCrudService")
    protected StateCrudService<Jvm, JvmState> getJvmStateCrudService() {
        return new JvmStateCrudServiceImpl();
    }
}
