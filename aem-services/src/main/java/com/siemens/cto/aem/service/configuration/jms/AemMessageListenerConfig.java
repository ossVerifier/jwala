package com.siemens.cto.aem.service.configuration.jms;

import java.util.concurrent.TimeUnit;

import javax.jms.MessageListener;
import javax.jms.Session;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.transaction.jta.JtaTransactionManager;

import com.siemens.cto.aem.service.configuration.service.AemServiceConfiguration;
import com.siemens.cto.aem.service.jvm.state.jms.listener.JvmStateMessageListener;
import com.siemens.cto.aem.service.jvm.state.jms.listener.message.JvmStateMapMessageConverterImpl;

@Configuration
public class AemMessageListenerConfig {

    @Autowired
    private JtaTransactionManager transactionManager;

    @Autowired
    private AemJmsConfig jmsConfig;

    @Autowired
    private AemServiceConfiguration serviceConfig;

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
        container.setDurableSubscriptionName("TocJvmStateDurableSubscriber");
        container.setConcurrentConsumers(1);
        return container;
    }

    @Bean
    public MessageListener getJvmStateMessageListener() {
        return new JvmStateMessageListener(serviceConfig.getJvmStateService(),
                                           new JvmStateMapMessageConverterImpl());
    }
}
