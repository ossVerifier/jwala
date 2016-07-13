package com.siemens.cto.aem.service.configuration.service;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.siemens.cto.aem.commandprocessor.CommandExecutor;
import com.siemens.cto.aem.commandprocessor.impl.jsch.JschBuilder;
import com.siemens.cto.aem.commandprocessor.jsch.impl.ChannelSessionKey;
import com.siemens.cto.aem.commandprocessor.jsch.impl.KeyedPooledJschChannelFactory;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.jvm.Jvm;
import com.siemens.cto.aem.common.domain.model.jvm.JvmState;
import com.siemens.cto.aem.common.domain.model.ssh.SshConfiguration;
import com.siemens.cto.aem.common.domain.model.state.CurrentState;
import com.siemens.cto.aem.common.domain.model.webserver.WebServer;
import com.siemens.cto.aem.common.domain.model.webserver.WebServerControlOperation;
import com.siemens.cto.aem.common.domain.model.webserver.WebServerReachableState;
import com.siemens.cto.aem.common.properties.ApplicationProperties;
import com.siemens.cto.aem.control.command.RemoteCommandExecutor;
import com.siemens.cto.aem.control.configuration.AemCommandExecutorConfig;
import com.siemens.cto.aem.control.configuration.AemSshConfig;
import com.siemens.cto.aem.persistence.configuration.AemPersistenceServiceConfiguration;
import com.siemens.cto.aem.persistence.jpa.service.*;
import com.siemens.cto.aem.persistence.jpa.service.impl.GroupJvmRelationshipServiceImpl;
import com.siemens.cto.aem.persistence.service.ApplicationPersistenceService;
import com.siemens.cto.aem.persistence.service.JvmPersistenceService;
import com.siemens.cto.aem.persistence.service.ResourceDao;
import com.siemens.cto.aem.persistence.service.WebServerPersistenceService;
import com.siemens.cto.aem.persistence.service.impl.JpaJvmPersistenceServiceImpl;
import com.siemens.cto.aem.persistence.service.impl.ResourceDaoImpl;
import com.siemens.cto.aem.service.HistoryService;
import com.siemens.cto.aem.service.MessagingService;
import com.siemens.cto.aem.service.RemoteCommandExecutorService;
import com.siemens.cto.aem.service.app.ApplicationCommandService;
import com.siemens.cto.aem.service.app.ApplicationService;
import com.siemens.cto.aem.service.app.PrivateApplicationService;
import com.siemens.cto.aem.service.app.impl.ApplicationCommandServiceImpl;
import com.siemens.cto.aem.service.app.impl.ApplicationServiceImpl;
import com.siemens.cto.aem.service.app.impl.PrivateApplicationServiceImpl;
import com.siemens.cto.aem.service.configuration.jms.AemJmsConfig;
import com.siemens.cto.aem.service.group.*;
import com.siemens.cto.aem.service.group.impl.GroupControlServiceImpl;
import com.siemens.cto.aem.service.group.impl.GroupJvmControlServiceImpl;
import com.siemens.cto.aem.service.group.impl.GroupServiceImpl;
import com.siemens.cto.aem.service.group.impl.GroupWebServerControlServiceImpl;
import com.siemens.cto.aem.service.impl.HistoryServiceImpl;
import com.siemens.cto.aem.service.initializer.JGroupsClusterInitializer;
import com.siemens.cto.aem.service.jvm.JvmControlService;
import com.siemens.cto.aem.service.jvm.JvmService;
import com.siemens.cto.aem.service.jvm.JvmStateService;
import com.siemens.cto.aem.service.jvm.impl.JvmControlServiceImpl;
import com.siemens.cto.aem.service.jvm.impl.JvmServiceImpl;
import com.siemens.cto.aem.service.jvm.state.JvmStateReceiverAdapter;
import com.siemens.cto.aem.service.resource.ResourceService;
import com.siemens.cto.aem.service.resource.impl.ResourceServiceImpl;
import com.siemens.cto.aem.service.ssl.hc.HttpClientRequestFactory;
import com.siemens.cto.aem.service.state.InMemoryStateManagerService;
import com.siemens.cto.aem.service.state.StateNotificationConsumerBuilder;
import com.siemens.cto.aem.service.state.StateNotificationService;
import com.siemens.cto.aem.service.state.impl.InMemoryStateManagerServiceImpl;
import com.siemens.cto.aem.service.state.jms.JmsStateNotificationConsumerBuilderImpl;
import com.siemens.cto.aem.service.state.jms.JmsStateNotificationServiceImpl;
import com.siemens.cto.aem.service.webserver.WebServerCommandService;
import com.siemens.cto.aem.service.webserver.WebServerControlService;
import com.siemens.cto.aem.service.webserver.WebServerService;
import com.siemens.cto.aem.service.webserver.WebServerStateRetrievalScheduledTaskHandler;
import com.siemens.cto.aem.service.webserver.component.ClientFactoryHelper;
import com.siemens.cto.aem.service.webserver.component.WebServerStateSetterWorker;
import com.siemens.cto.aem.service.webserver.impl.WebServerCommandServiceImpl;
import com.siemens.cto.aem.service.webserver.impl.WebServerControlServiceImpl;
import com.siemens.cto.aem.service.webserver.impl.WebServerServiceImpl;
import com.siemens.cto.toc.files.FileManager;
import com.siemens.cto.toc.files.WebArchiveManager;
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
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.annotation.Resource;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync
@EnableScheduling
@ComponentScan({"com.siemens.cto.aem.service.webserver.component",
        "com.siemens.cto.aem.service.state",
        "com.siemens.cto.aem.service.spring.component",
        "com.siemens.cto.aem.commandprocessor.jsch.impl.spring.component",
        "com.siemens.cto.aem.service.group.impl.spring.component",
        "com.siemens.cto.aem.service.jvm.impl.spring.component",
        "com.siemens.cto.aem.service.impl.spring.component"})
public class AemServiceConfiguration {

    @Autowired
    private AemPersistenceServiceConfiguration persistenceServiceConfiguration;

    @Autowired
    private AemCommandExecutorConfig aemCommandExecutorConfig;

    @Autowired
    private AemJmsConfig aemJmsConfig;

    @Autowired
    private FileManager fileManager;

    @Autowired
    private CommandExecutor commandExecutor;

    @Autowired
    private AemSshConfig aemSshConfig;

    @Autowired
    private WebServerService webServerPersistenceService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private GenericKeyedObjectPool<ChannelSessionKey, Channel> channelPool;

    @Autowired
    private GroupStateNotificationService groupStateNotificationService;

    private final Map webServerFutureMap = new HashMap();

    @Resource
    private Environment env;

    @Autowired
    private MessagingService messagingService;

    @Autowired
    @Qualifier("jvmInMemoryStateManagerService")
    private InMemoryStateManagerService<Identifier<Jvm>, CurrentState<Jvm, JvmState>> jvmInMemoryStateManagerService;

    @Autowired
    private ClientFactoryHelper clientFactoryHelper;

    public AemServiceConfiguration() {
    }

    /**
     * Make vars.properties available to spring integration configuration
     * System properties are only used if there is no setting in vars.properties.
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
    public GroupService getGroupService(final WebServerPersistenceService webServerPersistenceService) {
        return new GroupServiceImpl(persistenceServiceConfiguration.getGroupPersistenceService(), webServerPersistenceService,
                persistenceServiceConfiguration.getApplicationPersistenceService(), aemCommandExecutorConfig.getRemoteCommandExecutor());
    }

    @Bean(name = "jvmService")
    public JvmService getJvmService(final GroupService groupService,
                                    final ApplicationService applicationService,
                                    final ResourceService resourceService, final ClientFactoryHelper clientFactoryHelper,
                                    @Value("${spring.messaging.topic.serverStates:/topic/server-states}") final String topicServerStates) {
        final JvmPersistenceService jvmPersistenceService = persistenceServiceConfiguration.getJvmPersistenceService();
        return new JvmServiceImpl(jvmPersistenceService, groupService, applicationService,
                fileManager, getStateNotificationService(), messagingTemplate, groupStateNotificationService, resourceService,
                clientFactoryHelper, topicServerStates);
    }

    @Bean(name = "webServerService")
    public WebServerService getWebServerService(final ResourceService resourceService,
                                                @Value("${paths.resource-templates:../data/templates}") final String templatePath) {
        return new WebServerServiceImpl(persistenceServiceConfiguration.getWebServerPersistenceService(),
                fileManager, resourceService, templatePath);
    }

    @Bean
    public GroupJvmRelationshipService groupJvmRelationshipService(final GroupCrudService groupCrudService,
                                                                   final JvmCrudService jvmCrudService) {
        return new GroupJvmRelationshipServiceImpl(groupCrudService, jvmCrudService);
    }

    @Bean
    public JvmPersistenceService getJvmPersistenceService(final JvmCrudService jvmCrudService,
                                                          final ApplicationCrudService applicationCrudService,
                                                          final GroupJvmRelationshipService groupJvmRelationshipService) {
        return new JpaJvmPersistenceServiceImpl(jvmCrudService, applicationCrudService, groupJvmRelationshipService);
    }

    @Bean
    public ApplicationService getApplicationService(final JvmPersistenceService jvmPersistenceService, final GroupService groupService,
            final HistoryCrudService historyCrudService, final MessagingService messagingService, final ResourceService resourceService) {
        return new ApplicationServiceImpl(persistenceServiceConfiguration.getApplicationPersistenceService(),
                jvmPersistenceService, aemCommandExecutorConfig.getRemoteCommandExecutor(), groupService, null, null,
                getHistoryService(historyCrudService), messagingService, resourceService);
    }

    @Bean
    public PrivateApplicationService getPrivateApplicationService() {
        return new PrivateApplicationServiceImpl(/** Relying on autowire */);
    }

    @Bean(name = "jvmControlService")
    public JvmControlService getJvmControlService(final HistoryCrudService historyCrudService,
                                                  final MessagingService messagingService,
                                                  final JvmStateService jvmStateService,
                                                  final RemoteCommandExecutorService remoteCommandExecutorService,
                                                  final SshConfiguration sshConfig,
                                                  final JvmService jvmService) {
        return new JvmControlServiceImpl(jvmService, aemCommandExecutorConfig.getRemoteCommandExecutor(),
                getHistoryService(historyCrudService), messagingService, jvmStateService, remoteCommandExecutorService,
                sshConfig, groupStateNotificationService);
    }

    @Bean(name = "groupControlService")
    public GroupControlService getGroupControlService(final GroupJvmControlService groupJvmControlService,
                                                      final GroupWebServerControlService groupWebServerControlService) {
        return new GroupControlServiceImpl(groupWebServerControlService, groupJvmControlService);
    }

    @Bean(name = "groupJvmControlService")
    public GroupJvmControlService getGroupJvmControlService(final GroupService groupService, final JvmControlService jvmControlService) {
        return new GroupJvmControlServiceImpl(groupService, jvmControlService);
    }

    @Bean(name = "groupWebServerControlService")
    public GroupWebServerControlService getGroupWebServerControlService(final GroupService groupService, final WebServerControlService webServerControlService) {
        return new GroupWebServerControlServiceImpl(groupService, webServerControlService);
    }

    @Bean(name = "webServerControlService")
    public WebServerControlService getWebServerControlService(final WebServerService webServerService,
                                                              final RemoteCommandExecutor<WebServerControlOperation> commandExecutor,
                                                              final InMemoryStateManagerService<Identifier<WebServer>, WebServerReachableState> inMemoryStateManagerService,
                                                              final HistoryService historyService,
                                                              final MessagingService messagingService,
                                                              final RemoteCommandExecutorService remoteCommandExecutorService,
                                                              final SshConfiguration sshConfig) {
        return new WebServerControlServiceImpl(webServerService, commandExecutor, inMemoryStateManagerService, historyService,
                messagingService, remoteCommandExecutorService, sshConfig);
    }

    @Bean(name = "webServerCommandService")
    public WebServerCommandService getWebServerCommandService(final WebServerService webServerService,
                                                              final RemoteCommandExecutorService remoteCommandExecutorService) {
        final SshConfiguration sshConfig = aemSshConfig.getSshConfiguration();

        final JschBuilder jschBuilder = new JschBuilder().setPrivateKeyFileName(sshConfig.getPrivateKeyFile())
                .setKnownHostsFileName(sshConfig.getKnownHostsFile());

        return new WebServerCommandServiceImpl(
                webServerService,
                commandExecutor,
                jschBuilder,
                sshConfig,
                channelPool,
                remoteCommandExecutorService);
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
    public ResourceService getResourceService(final ApplicationPersistenceService applicationPersistenceService,
                                              final JvmPersistenceService jvmPersistenceService,
                                              final WebServerPersistenceService webServerPersistenceService,
                                              final ResourceDao resourceDao,
                                              final WebArchiveManager webArchiveManager) {
        return new ResourceServiceImpl(persistenceServiceConfiguration.getResourcePersistenceService(),
                persistenceServiceConfiguration.getGroupPersistenceService(), applicationPersistenceService,
                jvmPersistenceService, webServerPersistenceService, getPrivateApplicationService(), resourceDao, webArchiveManager);
    }

    @Bean
    public ResourceDao getResourceDao() {
        return new ResourceDaoImpl();
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
    public WebServerStateRetrievalScheduledTaskHandler getWebServerStateRetrievalScheduledTaskHandler(
            final WebServerService webServerService, final WebServerStateSetterWorker webServerStateSetterWorker) {
        return new WebServerStateRetrievalScheduledTaskHandler(webServerService, webServerStateSetterWorker, webServerFutureMap, true);
    }

    @Bean(name = "webServerTaskExecutor")
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

    @Bean(name = "jvmTaskExecutor")
    public TaskExecutor getJvmTaskExecutor(@Qualifier("pollingThreadFactory") final ThreadFactory threadFactory,
                                           @Value("${jvm.thread-task-executor.pool.size}") final int corePoolSize,
                                           @Value("${jvm.thread-task-executor.pool.max-size}") final int maxPoolSize,
                                           @Value("${jvm.thread-task-executor.pool.queue-capacity}") final int queueCapacity,
                                           @Value("${jvm.thread-task-executor.pool.keep-alive-sec}") final int keepAliveSeconds) {
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
    public GenericKeyedObjectPool<ChannelSessionKey, Channel> getChannelPool(final AemSshConfig sshConfig) throws JSchException {
        final GenericKeyedObjectPoolConfig genericKeyedObjectPoolConfig = new GenericKeyedObjectPoolConfig();
        genericKeyedObjectPoolConfig.setMaxTotalPerKey(10);
        genericKeyedObjectPoolConfig.setBlockWhenExhausted(true);
        return new GenericKeyedObjectPool(new KeyedPooledJschChannelFactory(sshConfig.getJschBuilder().build()));
    }

    @Bean
    public JvmStateReceiverAdapter getJvmReceiverAdapter(final JvmStateService jvmStateService) {
        return new JvmStateReceiverAdapter(jvmStateService);
    }

    @Bean
    public JGroupsClusterInitializer jGroupsClusterInitializer(final JvmStateReceiverAdapter jvmStateReceiverAdapter) {
        return new JGroupsClusterInitializer(jvmStateReceiverAdapter);
    }

    @Bean(name = "webServerInMemoryStateManagerService")
    InMemoryStateManagerService<Identifier<WebServer>, WebServerReachableState> getWebServerInMemoryStateManagerService() {
        return new InMemoryStateManagerServiceImpl<>();
    }

    @Bean
    public JSch getJSch() {
        return new JSch();
    }

    /**
     * Bean method to create a thread factory that creates daemon threads.
     * <code>
     <bean id="pollingThreadFactory" class="org.springframework.scheduling.concurrent.CustomizableThreadFactory">
     <constructor-arg value="polling-"/>
     </bean></code> */
    @Bean(name="pollingThreadFactory") public ThreadFactory getPollingThreadFactory() {
        CustomizableThreadFactory tf = new CustomizableThreadFactory("polling-");
        tf.setDaemon(true);
        return tf;
    }

}
