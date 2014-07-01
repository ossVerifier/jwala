package com.siemens.cto.aem.persistence.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.support.SharedEntityManagerBean;

import com.siemens.cto.aem.persistence.jpa.service.app.ApplicationCrudService;
import com.siemens.cto.aem.persistence.jpa.service.app.impl.ApplicationCrudServiceImpl;
import com.siemens.cto.aem.persistence.jpa.service.group.GroupControlCrudService;
import com.siemens.cto.aem.persistence.jpa.service.group.GroupCrudService;
import com.siemens.cto.aem.persistence.jpa.service.group.impl.GroupCrudServiceImpl;
import com.siemens.cto.aem.persistence.jpa.service.group.impl.JpaGroupControlCrudServiceImpl;
import com.siemens.cto.aem.persistence.jpa.service.groupjvm.GroupJvmRelationshipService;
import com.siemens.cto.aem.persistence.jpa.service.groupjvm.impl.GroupJvmRelationshipServiceImpl;
import com.siemens.cto.aem.persistence.jpa.service.jvm.JvmControlCrudService;
import com.siemens.cto.aem.persistence.jpa.service.jvm.JvmCrudService;
import com.siemens.cto.aem.persistence.jpa.service.jvm.JvmStateCrudService;
import com.siemens.cto.aem.persistence.jpa.service.jvm.impl.JvmControlCrudServiceImpl;
import com.siemens.cto.aem.persistence.jpa.service.jvm.impl.JvmCrudServiceImpl;
import com.siemens.cto.aem.persistence.jpa.service.jvm.impl.JvmStateCrudServiceImpl;
import com.siemens.cto.aem.persistence.jpa.service.webserver.WebServerControlCrudService;
import com.siemens.cto.aem.persistence.jpa.service.webserver.impl.WebServerControlCrudServiceImpl;
import com.siemens.cto.aem.persistence.service.app.ApplicationPersistenceService;
import com.siemens.cto.aem.persistence.service.app.impl.JpaApplicationPersistenceServiceImpl;
import com.siemens.cto.aem.persistence.service.group.GroupControlPersistenceService;
import com.siemens.cto.aem.persistence.service.group.GroupPersistenceService;
import com.siemens.cto.aem.persistence.service.group.impl.JpaGroupControlPersistenceServiceImpl;
import com.siemens.cto.aem.persistence.service.group.impl.JpaGroupPersistenceServiceImpl;
import com.siemens.cto.aem.persistence.service.jvm.JvmControlPersistenceService;
import com.siemens.cto.aem.persistence.service.jvm.JvmPersistenceService;
import com.siemens.cto.aem.persistence.service.jvm.JvmStatePersistenceService;
import com.siemens.cto.aem.persistence.service.jvm.impl.JpaJvmControlPersistenceServiceImpl;
import com.siemens.cto.aem.persistence.service.jvm.impl.JpaJvmPersistenceServiceImpl;
import com.siemens.cto.aem.persistence.service.jvm.impl.JpaJvmStatePersistenceServiceImpl;
import com.siemens.cto.aem.persistence.service.webserver.WebServerControlPersistenceService;
import com.siemens.cto.aem.persistence.service.webserver.impl.JpaWebServerControlPersistenceServiceImpl;

@Configuration
public class AemPersistenceServiceConfiguration {

    @Autowired
    private SharedEntityManagerBean sharedEntityManager;

    @Bean
    public JvmPersistenceService getJvmPersistenceService() {
        return new JpaJvmPersistenceServiceImpl(getJvmCrudService(),
                                                getGroupJvmRelationshipService());
    }
    
    @Bean
    public GroupPersistenceService getGroupPersistenceService() {
        return new JpaGroupPersistenceServiceImpl(getGroupCrudService(),
                                                  getGroupJvmRelationshipService());
    }

    @Bean
    public JvmControlPersistenceService getJvmControlPersistenceService() {
        return new JpaJvmControlPersistenceServiceImpl(getJvmControlCrudService());
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
    protected JvmControlCrudService getJvmControlCrudService() {
        return new JvmControlCrudServiceImpl();
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

    @Bean
    public WebServerControlPersistenceService getWebServerControlPersistenceService() {
        return new JpaWebServerControlPersistenceServiceImpl(getWebServerControlCrudService());
    }

    @Bean
    protected WebServerControlCrudService getWebServerControlCrudService() {
        return new WebServerControlCrudServiceImpl();
    }

    @Bean
    public JvmStatePersistenceService getJvmStatePersistenceService() {
        return new JpaJvmStatePersistenceServiceImpl(getJvmStateCrudService());
    }

    @Bean
    protected JvmStateCrudService getJvmStateCrudService() {
        return new JvmStateCrudServiceImpl();
    }
}
