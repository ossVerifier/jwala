package com.siemens.cto.aem.service.configuration.service;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSchException;
import com.siemens.cto.aem.commandprocessor.CommandExecutor;
import com.siemens.cto.aem.commandprocessor.impl.jsch.JschBuilder;
import com.siemens.cto.aem.commandprocessor.jsch.impl.ChannelSessionKey;
import com.siemens.cto.aem.commandprocessor.jsch.impl.KeyedPooledJschChannelFactory;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.ssh.SshConfiguration;
import com.siemens.cto.aem.common.domain.model.webserver.WebServer;
import com.siemens.cto.aem.common.domain.model.webserver.WebServerReachableState;
import com.siemens.cto.aem.common.properties.ApplicationProperties;
import com.siemens.cto.aem.control.configuration.AemCommandExecutorConfig;
import com.siemens.cto.aem.control.configuration.AemSshConfig;
import com.siemens.cto.aem.persistence.configuration.AemPersistenceServiceConfiguration;
import com.siemens.cto.aem.persistence.jpa.service.GroupCrudService;
import com.siemens.cto.aem.persistence.jpa.service.GroupJvmRelationshipService;
import com.siemens.cto.aem.persistence.jpa.service.HistoryCrudService;
import com.siemens.cto.aem.persistence.jpa.service.JvmCrudService;
import com.siemens.cto.aem.persistence.jpa.service.impl.GroupJvmRelationshipServiceImpl;
import com.siemens.cto.aem.persistence.service.GroupPersistenceService;
import com.siemens.cto.aem.persistence.service.JvmPersistenceService;
import com.siemens.cto.aem.persistence.service.impl.JpaJvmPersistenceServiceImpl;
import com.siemens.cto.aem.service.HistoryService;
import com.siemens.cto.aem.service.app.ApplicationCommandService;
import com.siemens.cto.aem.service.app.ApplicationService;
import com.siemens.cto.aem.service.app.PrivateApplicationService;
import com.siemens.cto.aem.service.app.impl.ApplicationCommandServiceImpl;
import com.siemens.cto.aem.service.app.impl.ApplicationServiceImpl;
import com.siemens.cto.aem.service.app.impl.PrivateApplicationServiceImpl;
import com.siemens.cto.aem.service.configuration.jms.AemJmsConfig;
import com.siemens.cto.aem.service.dispatch.CommandDispatchGateway;
import com.siemens.cto.aem.service.group.*;
import com.siemens.cto.aem.service.group.impl.*;
import com.siemens.cto.aem.service.impl.HistoryServiceImpl;
import com.siemens.cto.aem.service.jvm.JvmControlService;
import com.siemens.cto.aem.service.jvm.JvmService;
import com.siemens.cto.aem.service.jvm.impl.JvmControlServiceImpl;
import com.siemens.cto.aem.service.jvm.impl.JvmServiceImpl;
import com.siemens.cto.aem.service.initializer.JGroupsClusterInitializer;
import com.siemens.cto.aem.service.jvm.state.JvmStateReceiverAdapter;
import com.siemens.cto.aem.service.resource.ResourceService;
import com.siemens.cto.aem.service.resource.impl.ResourceServiceImpl;
import com.siemens.cto.aem.service.ssl.hc.HttpClientRequestFactory;
import com.siemens.cto.aem.service.state.StateNotificationConsumerBuilder;
import com.siemens.cto.aem.service.state.StateNotificationService;
import com.siemens.cto.aem.service.state.jms.JmsStateNotificationConsumerBuilderImpl;
import com.siemens.cto.aem.service.state.jms.JmsStateNotificationServiceImpl;
import com.siemens.cto.aem.service.webserver.WebServerCommandService;
import com.siemens.cto.aem.service.webserver.WebServerControlService;
import com.siemens.cto.aem.service.webserver.WebServerService;
import com.siemens.cto.aem.service.webserver.WebServerStateRetrievalScheduledTaskHandler;
import com.siemens.cto.aem.service.webserver.component.WebServerStateSetterWorker;
import com.siemens.cto.aem.service.webserver.impl.WebServerCommandServiceImpl;
import com.siemens.cto.aem.service.webserver.impl.WebServerControlServiceImpl;
import com.siemens.cto.aem.service.webserver.impl.WebServerServiceImpl;
import com.siemens.cto.aem.template.HarmonyTemplateEngine;
import com.siemens.cto.toc.files.FileManager;
import org.apache.commons.pool2.impl.GenericKeyedObjectPool;
import org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import javax.annotation.Resource;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync
@EnableScheduling
@ComponentScan({"com.siemens.cto.aem.service.webserver.component", "com.siemens.cto.aem.service.state",
        "com.siemens.cto.aem.service.spring.component", "com.siemens.cto.aem.commandprocessor.jsch.impl.spring.component",
        "com.siemens.cto.aem.service.group.impl.spring.component"})
public class AemServiceConfiguration implements SchedulingConfigurer {

    @Autowired
    private AemPersistenceServiceConfiguration persistenceServiceConfiguration;

    @Autowired
    private AemCommandExecutorConfig aemCommandExecutorConfig;

    @Autowired
    private AemJmsConfig aemJmsConfig;

    @Autowired
    private CommandDispatchGateway commandDispatchGateway;

    @Autowired
    private FileManager fileManager;

    @Autowired
    private CommandExecutor commandExecutor;

    @Autowired
    private AemSshConfig aemSshConfig;

    @Autowired
    private HarmonyTemplateEngine harmonyTemplateEngine;

    @Autowired
    private WebServerService webServerPersistenceService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private GenericKeyedObjectPool<ChannelSessionKey, Channel> channelPool;

    @Autowired
    private GroupStateNotificationService groupStateNotificationService;

    private final Map<Identifier<WebServer>, WebServerReachableState> webServerReachableStateMap = new HashMap<>();
    private final Map<Identifier<WebServer>, Future<?>> webServerFutureMap = new HashMap<>();

    @Resource
    private Environment env;

    @Autowired
    private Executor taskScheduler;


    /**
     * Make toc.properties available to spring integration configuration
     * System properties are only used if there is no setting in toc.properties.
     */
    @Bean(name = "aemServiceConfigurationPropertiesConfigurer")
    public static PropertySourcesPlaceholderConfigurer configurer() {
        PropertySourcesPlaceholderConfigurer ppc = new PropertySourcesPlaceholderConfigurer();
        ppc.setLocation(new ClassPathResource("META-INF/spring/toc-defaults.properties"));
        ppc.setLocalOverride(true);
        ppc.setProperties(ApplicationProperties.getProperties());
        return ppc;
    }

    @Bean
    public GroupService getGroupService() {
        return new GroupServiceImpl(
                persistenceServiceConfiguration.getGroupPersistenceService(),
                getWebServerService(), persistenceServiceConfiguration.getApplicationPersistenceService());
    }

    @Bean(name = "jvmService")
    public JvmService getJvmService() {
        final JvmPersistenceService jvmPersistenceService = persistenceServiceConfiguration.getJvmPersistenceService();
        return new JvmServiceImpl(jvmPersistenceService, getGroupService(), getApplicationService(jvmPersistenceService),
                fileManager, getStateNotificationService(), messagingTemplate, groupStateNotificationService);
    }

    @Bean(name = "webServerService")
    public WebServerService getWebServerService() {
        return new WebServerServiceImpl(
                persistenceServiceConfiguration.getWebServerPersistenceService(),
                fileManager
        );
    }

    @Bean
    @Autowired
    public GroupJvmRelationshipService groupJvmRelationshipService(final GroupCrudService groupCrudService,
                                                                   final JvmCrudService jvmCrudService) {
        return new GroupJvmRelationshipServiceImpl(groupCrudService, jvmCrudService);
    }

    @Bean
    @Autowired
    public JvmPersistenceService getJvmPersistenceService(final JvmCrudService jvmCrudService,
                                                          final GroupJvmRelationshipService groupJvmRelationshipService) {
        return new JpaJvmPersistenceServiceImpl(jvmCrudService, groupJvmRelationshipService);
    }

    @Bean
    @Autowired
    public ApplicationService getApplicationService(final JvmPersistenceService jvmPersistenceService) {
        return new ApplicationServiceImpl(persistenceServiceConfiguration.getApplicationPersistenceService(),
                jvmPersistenceService, aemCommandExecutorConfig.getRemoteCommandExecutor(), getGroupService(), fileManager,
                null, null);
    }

    @Bean
    public PrivateApplicationService getPrivateApplicationService() {
        return new PrivateApplicationServiceImpl(/** Relying on autowire */);
    }

    @Bean(name = "jvmControlService")
    @Autowired
    public JvmControlService getJvmControlService(final HistoryCrudService historyCrudService) {
        return new JvmControlServiceImpl(getJvmService(), aemCommandExecutorConfig.getRemoteCommandExecutor(),
                getHistoryService(historyCrudService), messagingTemplate);
    }

    @Bean(name = "groupControlService")
    @Autowired
    public GroupControlService getGroupControlService(final GroupPersistenceService groupPersistenceService,
                                                      final JvmPersistenceService jvmPersistenceService) {
        return new GroupControlServiceImpl(
                getGroupWebServerControlService(),
                getGroupJvmControlService()
        );
    }

    @Bean(name = "groupJvmControlService")
    public GroupJvmControlService getGroupJvmControlService() {
        return new GroupJvmControlServiceImpl(getGroupService(), commandDispatchGateway);
    }

    @Bean(name = "groupWebServerControlService")
    public GroupWebServerControlService getGroupWebServerControlService() {
        return new GroupWebServerControlServiceImpl(getGroupService(), commandDispatchGateway);
    }

    @Bean(name = "webServerControlService")
    @Autowired
    public WebServerControlService getWebServerControlService(final HistoryService historyService) {
        return new WebServerControlServiceImpl(getWebServerService(), aemCommandExecutorConfig.getRemoteCommandExecutor(),
                webServerReachableStateMap, historyService, getStateNotificationService(), messagingTemplate);
    }

    @Bean(name = "webServerCommandService")
    @Autowired
    public WebServerCommandService getWebServerCommandService() {
        final SshConfiguration sshConfig = aemSshConfig.getSshConfiguration();

        final JschBuilder jschBuilder = new JschBuilder().setPrivateKeyFileName(sshConfig.getPrivateKeyFile())
                .setKnownHostsFileName(sshConfig.getKnownHostsFile());

        return new WebServerCommandServiceImpl(getWebServerService(), commandExecutor, jschBuilder, sshConfig, channelPool);
    }

    @Bean
    public ApplicationCommandService getApplicationCommandService() {
        return new ApplicationCommandServiceImpl(aemSshConfig.getSshConfiguration(), aemSshConfig.getJschBuilder());
    }

    @Bean(name = "stateNotificationService")
    public StateNotificationService getStateNotificationService() {
        return new JmsStateNotificationServiceImpl(aemJmsConfig.getJmsTemplate(),
                aemJmsConfig.getStateNotificationDestination(),
                getStateNotificationConsumerBuilder());
    }

    @Bean
    public StateNotificationConsumerBuilder getStateNotificationConsumerBuilder() {
        return new JmsStateNotificationConsumerBuilderImpl(aemJmsConfig.getJmsPackageBuilder());
    }

    @Bean(name = "resourceService")
    public ResourceService getResourceService() {
        return new ResourceServiceImpl(fileManager, harmonyTemplateEngine, persistenceServiceConfiguration.getResourcePersistenceService(), persistenceServiceConfiguration.getGroupPersistenceService());
    }

    @Bean(name = "webServerHttpRequestFactory")
    @DependsOn("aemServiceConfigurationPropertiesConfigurer")
    // TODO: Check why the bean name says webServer while the parameter seems to be for JVM. Is this bean actively in used ?
    public HttpClientRequestFactory getHttpClientRequestFactory(
            @Value("${ping.jvm.connectTimeout}") final int connectionTimeout,
            @Value("${ping.jvm.readTimeout}") final int readTimeout,
            @Value("${ping.jvm.period.millis}") final long millis,
            @Value("${ping.jvm.maxHttpConnections}") final int maxHttpConnections) throws UnrecoverableKeyException,
            NoSuchAlgorithmException,
            KeyStoreException,
            KeyManagementException {
        HttpClientRequestFactory httpClientRequestFactory = new HttpClientRequestFactory();
        httpClientRequestFactory.setConnectTimeout(connectionTimeout);
        httpClientRequestFactory.setReadTimeout(readTimeout);
        httpClientRequestFactory.setPeriodMillis(millis);
        httpClientRequestFactory.setMaxHttpConnections(maxHttpConnections);
        return httpClientRequestFactory;
    }

    @Bean(name = "webServerStateRetrievalScheduledTaskHandler")
    @Autowired
    public WebServerStateRetrievalScheduledTaskHandler getWebServerStateRetrievalScheduledTaskHandler(
            final WebServerService webServerService, final WebServerStateSetterWorker webServerStateSetterWorker) {
        return new WebServerStateRetrievalScheduledTaskHandler(webServerService, webServerStateSetterWorker, webServerFutureMap, true);
    }

    @Bean
    @Autowired
    public WebServerStateSetterWorker getWebServerStateSetterWorker(final WebServerStateSetterWorker webServerStateSetterWorker) {
        webServerStateSetterWorker.setWebServerReachableStateMap(webServerReachableStateMap);
        webServerStateSetterWorker.setWebServerService(getWebServerService());
        webServerStateSetterWorker.setStateNotificationService(getStateNotificationService());
        webServerStateSetterWorker.setMessagingTemplate(messagingTemplate);
        webServerStateSetterWorker.setGroupStateNotificationService(groupStateNotificationService);
        return webServerStateSetterWorker;
    }

    @Bean(name = "webServerTaskExecutor")
    @Autowired
    public TaskExecutor getWebServerTaskExecutor(@Qualifier("pollingThreadFactory") final ThreadFactory threadFactory,
                                                 @Value("${webserver.thread-task-executor.pool.size}") final int corePoolSize,
                                                 @Value("${webserver.thread-task-executor.pool.max-size}") final int maxPoolSize,
                                                 @Value("${webserver.thread-task-executor.pool.queue-capacity}") final int queueCapacity,
                                                 @Value("${webserver.thread-task-executor.pool.keep-alive-sec}") final int keepAliveSeconds) {
        final ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setCorePoolSize(corePoolSize);
        threadPoolTaskExecutor.setMaxPoolSize(maxPoolSize);
        threadPoolTaskExecutor.setQueueCapacity(queueCapacity);
        threadPoolTaskExecutor.setKeepAliveSeconds(keepAliveSeconds);
        threadPoolTaskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        threadPoolTaskExecutor.setThreadFactory(threadFactory);
        return threadPoolTaskExecutor;
    }

    @Bean
    public HistoryService getHistoryService(final HistoryCrudService historyCrudService) {
        return new HistoryServiceImpl(historyCrudService);
    }

    @Bean
    @Autowired
    public GenericKeyedObjectPool<ChannelSessionKey, Channel> getChannelPool(final AemSshConfig sshConfig) throws JSchException {
        final GenericKeyedObjectPoolConfig genericKeyedObjectPoolConfig = new GenericKeyedObjectPoolConfig();
        genericKeyedObjectPoolConfig.setMaxTotalPerKey(10);
        genericKeyedObjectPoolConfig.setBlockWhenExhausted(true);
        return new GenericKeyedObjectPool(new KeyedPooledJschChannelFactory(sshConfig.getJschBuilder().build()));
    }

    @Bean
    @Autowired
    public JvmStateReceiverAdapter getSimpleJvmReceiverAdapter() {
        return new JvmStateReceiverAdapter(getJvmService(), getStateNotificationService(),
                messagingTemplate, groupStateNotificationService);
    }

    @Bean
    public JGroupsClusterInitializer jGroupsClusterInitializer() {
        return new JGroupsClusterInitializer(getSimpleJvmReceiverAdapter());
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setScheduler(taskScheduler);
    }

}
