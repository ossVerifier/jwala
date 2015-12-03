package com.siemens.cto.aem.service.configuration.jms;

import com.siemens.cto.aem.service.state.jms.JmsPackageBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jndi.JndiLocatorDelegate;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.naming.NamingException;

@Configuration
public class AemJmsConfig {
    private static final String JNDI_JMS_CONNECTION_FACTORY = "jms/toc-cf";
    private static final String JNDI_HEARTBEAT_TOPIC_DESTINATION = "jms/toc-status";
    private static final String JNDI_UI_TOPIC_DESTINATION = "jms/toc-state-notification";

    @Bean
    public ConnectionFactory getConnectionFactory() {
        return lookup(JNDI_JMS_CONNECTION_FACTORY, ConnectionFactory.class);
    }

    @Bean
    public Destination getJvmStateDestination() {
        return lookup(JNDI_HEARTBEAT_TOPIC_DESTINATION, Destination.class);
    }

    @Bean
    public Destination getStateNotificationDestination() {
        return lookup(JNDI_UI_TOPIC_DESTINATION, Destination.class);
    }

    @Bean
    public JmsTemplate getJmsTemplate() {
        return new JmsTemplate(getConnectionFactory());
    }

    @Bean
    public JmsPackageBuilder getJmsPackageBuilder() {
        return new JmsPackageBuilder().setConnectionFactory(getConnectionFactory())
                                      .setDestination(getStateNotificationDestination());
    }

    protected <T> T lookup(final String aJndiReference,
                           final Class<T> aT) {
        try {
            return JndiLocatorDelegate.createDefaultResourceRefLocator().lookup(aJndiReference,
                                                                                aT);
        } catch (final NamingException ne) {
            throw new RuntimeException("Naming Exception while looking up JMS resources", ne);
        }
    }
}
