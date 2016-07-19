package com.siemens.cto.aem.service.configuration.jms;

import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.jvm.Jvm;
import com.siemens.cto.aem.common.domain.model.jvm.JvmState;
import com.siemens.cto.aem.common.domain.model.state.CurrentState;
import com.siemens.cto.aem.common.properties.ApplicationProperties;
import com.siemens.cto.aem.service.jvm.JvmStateService;
import com.siemens.cto.aem.service.jvm.state.jms.listener.JvmStateMessageListener;
import com.siemens.cto.aem.service.jvm.state.jms.listener.message.JvmStateMapMessageConverterImpl;
import com.siemens.cto.aem.service.state.InMemoryStateManagerService;
import com.siemens.cto.aem.service.state.impl.InMemoryStateManagerServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.transaction.PlatformTransactionManager;

import javax.jms.MessageListener;
import javax.jms.Session;
import java.util.concurrent.TimeUnit;

@Configuration
public class AemMessageListenerConfig {

    @Autowired
    private AemJmsConfig jmsConfig;

    @Bean
    public DefaultMessageListenerContainer getJvmStateListenerContainer(final PlatformTransactionManager transactionManager,
                                                                        final MessageListener jvmMessageListener) {
        DefaultMessageListenerContainer container = new DefaultMessageListenerContainer();
        container.setTransactionManager(transactionManager);
        container.setConnectionFactory(jmsConfig.getConnectionFactory());
        container.setReceiveTimeout(TimeUnit.MILLISECONDS.convert(25, TimeUnit.SECONDS));
        container.setMessageListener(jvmMessageListener);
        container.setDestination(jmsConfig.getJvmStateDestination());
        container.setSessionTransacted(true);
        container.setSessionAcknowledgeMode(Session.SESSION_TRANSACTED);
        container.setPubSubDomain(true);
        container.setSubscriptionDurable(true);
        container.setDurableSubscriptionName(ApplicationProperties.get("toc.jms.heartbeat.durable-name"));
        container.setConcurrentConsumers(1);
        return container;
    }

    @Bean(name = "jvmInMemoryStateManagerService")
    public InMemoryStateManagerService<Identifier<Jvm>, CurrentState<Jvm, JvmState>> getInMemoryStateManagerService() {
        return new InMemoryStateManagerServiceImpl<>();
    }

    @Bean
    public MessageListener getJvmStateMessageListener(final JvmStateService jvmStateService) {
        return new JvmStateMessageListener(new JvmStateMapMessageConverterImpl(), jvmStateService);
    }

}