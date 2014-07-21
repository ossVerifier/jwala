package com.siemens.cto.aem.service.configuration.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import com.siemens.cto.aem.control.configuration.AemCommandExecutorConfig;
import com.siemens.cto.aem.domain.model.webserver.WebServer;
import com.siemens.cto.aem.domain.model.webserver.WebServerReachableState;
import com.siemens.cto.aem.persistence.configuration.AemDaoConfiguration;
import com.siemens.cto.aem.persistence.configuration.AemPersistenceServiceConfiguration;
import com.siemens.cto.aem.service.app.ApplicationService;
import com.siemens.cto.aem.service.app.PrivateApplicationService;
import com.siemens.cto.aem.service.app.impl.ApplicationServiceImpl;
import com.siemens.cto.aem.service.app.impl.PrivateApplicationServiceImpl;
import com.siemens.cto.aem.service.configuration.jms.AemJmsConfig;
import com.siemens.cto.aem.service.dispatch.CommandDispatchGateway;
import com.siemens.cto.aem.service.group.GroupControlService;
import com.siemens.cto.aem.service.group.GroupJvmControlService;
import com.siemens.cto.aem.service.group.GroupService;
import com.siemens.cto.aem.service.group.impl.GroupControlServiceImpl;
import com.siemens.cto.aem.service.group.impl.GroupJvmControlServiceImpl;
import com.siemens.cto.aem.service.group.impl.GroupServiceImpl;
import com.siemens.cto.aem.service.group.impl.GroupStateManagerTableImpl;
import com.siemens.cto.aem.service.jvm.JvmControlService;
import com.siemens.cto.aem.service.jvm.JvmService;
import com.siemens.cto.aem.service.jvm.impl.JvmControlServiceImpl;
import com.siemens.cto.aem.service.jvm.impl.JvmServiceImpl;
import com.siemens.cto.aem.service.jvm.state.JvmStateNotificationService;
import com.siemens.cto.aem.service.jvm.state.JvmStateService;
import com.siemens.cto.aem.service.jvm.state.impl.JvmStateServiceImpl;
import com.siemens.cto.aem.service.jvm.state.jms.JmsJvmStateNotificationServiceImpl;
import com.siemens.cto.aem.service.state.StateNotificationGateway;
import com.siemens.cto.aem.service.state.StateNotificationService;
import com.siemens.cto.aem.service.state.StateService;
import com.siemens.cto.aem.service.webserver.GroupWebServerControlService;
import com.siemens.cto.aem.service.webserver.WebServerControlService;
import com.siemens.cto.aem.service.webserver.WebServerService;
import com.siemens.cto.aem.service.webserver.impl.GroupWebServerControlServiceImpl;
import com.siemens.cto.aem.service.webserver.impl.WebServerControlServiceImpl;
import com.siemens.cto.aem.service.webserver.impl.WebServerServiceImpl;
import com.siemens.cto.aem.service.webserver.impl.WebServerStateServiceImpl;
import com.siemens.cto.aem.service.webserver.state.jms.WebServerJmsStateNotificationServiceImpl;
import com.siemens.cto.toc.files.TemplateManager;

@Configuration
public class AemServiceConfiguration {

    @Autowired
    private AemPersistenceServiceConfiguration persistenceServiceConfiguration;

    @Autowired
    private AemDaoConfiguration aemDaoConfiguration;

    @Autowired
    private AemCommandExecutorConfig aemCommandExecutorConfig;

    @Autowired
    private AemJmsConfig aemJmsConfig;

    @Autowired
    private CommandDispatchGateway commandDispatchGateway;

    @Autowired
    private TemplateManager templateManager;

    @Autowired
    private StateNotificationGateway stateNotificationGateway;

    @Bean
    @Scope((ConfigurableBeanFactory.SCOPE_PROTOTYPE))
    public GroupStateManagerTableImpl getGroupStateManagerTableImpl() {
        return new GroupStateManagerTableImpl();
    }

    @Bean
    public GroupService getGroupService() {
        return new GroupServiceImpl(persistenceServiceConfiguration.getGroupPersistenceService());
    }

    @Bean
    public JvmService getJvmService() {
        return new JvmServiceImpl(persistenceServiceConfiguration.getJvmPersistenceService(),
                                  getGroupService());
    }

    @Bean(name="webServerService")
    public WebServerService getWebServerService() {
        return new WebServerServiceImpl(aemDaoConfiguration.getWebServerDao(), templateManager);
    }

    @Bean
    public ApplicationService getApplicationService() {
        return new ApplicationServiceImpl(aemDaoConfiguration.getApplicationDao(), persistenceServiceConfiguration.getApplicationPersistenceService());
    }

    @Bean
    public PrivateApplicationService getPrivateApplicationService() {
        return new PrivateApplicationServiceImpl(/** Relying on autowire */);
    }

    @Bean(name="jvmControlService")
    public JvmControlService getJvmControlService() {
        return new JvmControlServiceImpl(persistenceServiceConfiguration.getJvmControlPersistenceService(),
                                         getJvmService(),
                                         aemCommandExecutorConfig.getJvmCommandExecutor(),
                                         getJvmStateService());
    }

    @Bean(name="groupControlService")
    public GroupControlService getGroupControlService() {
        return new GroupControlServiceImpl(getGroupWebServerControlService(), getGroupJvmControlService());
    }

    @Bean(name="groupJvmControlService")
    public GroupJvmControlService getGroupJvmControlService() {
        return new GroupJvmControlServiceImpl(persistenceServiceConfiguration.getGroupControlPersistenceService(),
                                         getGroupService(),
                                         commandDispatchGateway);
    }

    @Bean(name="groupWebServerControlService")
    public GroupWebServerControlService getGroupWebServerControlService() {
        return new GroupWebServerControlServiceImpl(persistenceServiceConfiguration.getGroupControlPersistenceService(),
                                         getGroupService(),
                                         commandDispatchGateway);
    }

    @Bean(name="webServerControlService")
    public WebServerControlService getWebServerControlService() {
        return new WebServerControlServiceImpl(persistenceServiceConfiguration.getWebServerControlPersistenceService(),
                                               getWebServerService(),
                                               aemCommandExecutorConfig.getWebServerCommandExecutor());
    }

    @Bean
    public JvmStateService getJvmStateService() {
        return new JvmStateServiceImpl(persistenceServiceConfiguration.getJvmStatePersistenceService(),
                                       getJvmStateNotificationService(),
                                       stateNotificationGateway);
    }

    @Bean
    public JvmStateNotificationService getJvmStateNotificationService() {
        return new JmsJvmStateNotificationServiceImpl(aemJmsConfig.getJmsPackageBuilder(),
                                                      aemJmsConfig.getJmsTemplate(),
                                                      aemJmsConfig.getJvmStateNotificationDestination());
    }

    @Bean(name = "webServerStateNotificationService")
    public StateNotificationService<WebServer> getWebServerStateNotificationService() {
        return new WebServerJmsStateNotificationServiceImpl(aemJmsConfig.getJmsPackageBuilder(),
                                                            aemJmsConfig.getJmsTemplate(),
                                                            aemJmsConfig.getWebServerStateNotificationDestination());
    }

    @Bean(name = "webServerStateService")
    public StateService<WebServer, WebServerReachableState> getWebServerStateService() {
        return new WebServerStateServiceImpl(persistenceServiceConfiguration.getWebServerStatePersistenceService(),
                                             getWebServerStateNotificationService());
    }
}
