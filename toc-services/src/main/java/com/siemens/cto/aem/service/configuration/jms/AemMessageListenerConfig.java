package com.siemens.cto.aem.service.configuration.jms;

import com.siemens.cto.aem.common.properties.ApplicationProperties;
import com.siemens.cto.aem.persistence.service.JvmPersistenceService;
import com.siemens.cto.aem.service.configuration.service.AemServiceConfiguration;
import com.siemens.cto.aem.service.jvm.JvmService;
import com.siemens.cto.aem.service.jvm.state.jms.listener.JvmStateMessageListener;
import com.siemens.cto.aem.service.jvm.state.jms.listener.message.JvmStateMapMessageConverterImpl;
import com.siemens.cto.aem.service.spring.component.GrpStateComputationAndNotificationSvc;
import com.siemens.cto.aem.service.state.StateNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.transaction.jta.JtaTransactionManager;

import javax.jms.MessageListener;
import javax.jms.Session;
import java.util.concurrent.TimeUnit;

@Configuration
public class AemMessageListenerConfig {

    @Autowired
    private JtaTransactionManager transactionManager;

    @Autowired
    private AemJmsConfig jmsConfig;

    @Autowired
    private AemServiceConfiguration serviceConfig;

    @Autowired
    private JvmPersistenceService jvmPersistenceService;

    @Autowired
    private GrpStateComputationAndNotificationSvc grpStateComputationAndNotificationSvc;

    @Autowired
    private JvmService jvmService;

    @Autowired
    private StateNotificationService stateNotificationService;

    @Bean
    public DefaultMessageListenerContainer getJvmStateListenerContainer() {
        final DefaultMessageListenerContainer container = new DefaultMessageListenerContainer();

        container.setTransactionManager(transactionManager);
        container.setConnectionFactory(jmsConfig.getConnectionFactory());
        container.setReceiveTimeout(TimeUnit.MILLISECONDS.convert(25, TimeUnit.SECONDS));
        container.setMessageListener(getJvmStateMessageListener());
        container.setDestination(jmsConfig.getJvmStateDestination());
        container.setSessionTransacted(true);
        container.setSessionAcknowledgeMode(Session.SESSION_TRANSACTED);
        container.setPubSubDomain(true);
        container.setSubscriptionDurable(true);
        container.setDurableSubscriptionName(ApplicationProperties.get("toc.jms.heartbeat.durable-name"));
        container.setConcurrentConsumers(1);

        return container;
    }

    @Bean
    @Autowired
    public MessageListener getJvmStateMessageListener() {
        return new JvmStateMessageListener(new JvmStateMapMessageConverterImpl(), jvmService,
                grpStateComputationAndNotificationSvc, stateNotificationService);
    }

}
