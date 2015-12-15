package com.siemens.cto.aem.persistence.configuration;

import com.siemens.cto.aem.common.domain.model.jvm.Jvm;
import com.siemens.cto.aem.common.domain.model.jvm.JvmState;
import com.siemens.cto.aem.common.domain.model.webserver.WebServer;
import com.siemens.cto.aem.common.domain.model.webserver.WebServerReachableState;
import com.siemens.cto.aem.persistence.jpa.service.ApplicationCrudService;
import com.siemens.cto.aem.persistence.jpa.service.impl.ApplicationCrudServiceImpl;
import com.siemens.cto.aem.persistence.jpa.service.GroupControlCrudService;
import com.siemens.cto.aem.persistence.jpa.service.GroupCrudService;
import com.siemens.cto.aem.persistence.jpa.service.impl.GroupCrudServiceImpl;
import com.siemens.cto.aem.persistence.jpa.service.impl.JpaGroupControlCrudServiceImpl;
import com.siemens.cto.aem.persistence.jpa.service.GroupJvmRelationshipService;
import com.siemens.cto.aem.persistence.jpa.service.impl.GroupJvmRelationshipServiceImpl;
import com.siemens.cto.aem.persistence.jpa.service.JvmCrudService;
import com.siemens.cto.aem.persistence.jpa.service.impl.JvmCrudServiceImpl;
import com.siemens.cto.aem.persistence.jpa.service.impl.JvmStateCrudServiceImpl;
import com.siemens.cto.aem.persistence.jpa.service.ResourceInstanceCrudService;
import com.siemens.cto.aem.persistence.jpa.service.impl.ResourceInstanceCrudServiceImpl;
import com.siemens.cto.aem.persistence.jpa.service.StateCrudService;
import com.siemens.cto.aem.persistence.jpa.service.impl.WebServerStateCrudServiceImpl;
import com.siemens.cto.aem.persistence.service.app.ApplicationPersistenceService;
import com.siemens.cto.aem.persistence.service.app.impl.JpaApplicationPersistenceServiceImpl;
import com.siemens.cto.aem.persistence.service.group.GroupControlPersistenceService;
import com.siemens.cto.aem.persistence.service.group.GroupPersistenceService;
import com.siemens.cto.aem.persistence.service.group.impl.JpaGroupControlPersistenceServiceImpl;
import com.siemens.cto.aem.persistence.service.group.impl.JpaGroupPersistenceServiceImpl;
import com.siemens.cto.aem.persistence.service.jvm.JvmPersistenceService;
import com.siemens.cto.aem.persistence.service.jvm.impl.JpaJvmPersistenceServiceImpl;
import com.siemens.cto.aem.persistence.service.jvm.impl.JvmJpaStatePersistenceServiceImpl;
import com.siemens.cto.aem.persistence.service.resource.ResourcePersistenceService;
import com.siemens.cto.aem.persistence.service.resource.impl.JpaResourcePersistenceServiceImpl;
import com.siemens.cto.aem.persistence.service.state.StatePersistenceService;
import com.siemens.cto.aem.persistence.service.webserver.impl.WebServerJpaStatePersistenceServiceImpl;
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
    public GroupControlPersistenceService getGroupControlPersistenceService() {
        return new JpaGroupControlPersistenceServiceImpl(getGroupControlCrudService());
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
    protected GroupControlCrudService getGroupControlCrudService() {
        return new JpaGroupControlCrudServiceImpl();
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

    @Bean(name = "jvmStateCrudService")
    protected StateCrudService<Jvm, JvmState> getJvmStateCrudService() {
        return new JvmStateCrudServiceImpl();
    }
}
