package com.siemens.cto.aem.service.state.jms;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JmsPackageBuilder {
    private static final Logger LOGGER = LoggerFactory.getLogger(JmsPackageBuilder.class);

    private ConnectionFactory connectionFactory;
    private Destination destination;
    private boolean transacted;
    private int acknowledgeMode;

    public JmsPackageBuilder() {
        transacted = false;
        acknowledgeMode = Session.AUTO_ACKNOWLEDGE;
    }

    public JmsPackageBuilder setConnectionFactory(final ConnectionFactory aFactory) {
        connectionFactory = aFactory;
        return this;
    }

    public JmsPackageBuilder setDestination(final Destination aDestination) {
        destination = aDestination;
        return this;
    }

    public JmsPackageBuilder setTransacted(final boolean isTransacted) {
        transacted = isTransacted;
        return this;
    }

    public JmsPackageBuilder setAcknowledgeMode(final int anAcknowledgeMode) {
        acknowledgeMode = anAcknowledgeMode;
        return this;
    }

    public JmsPackage build() {
        try {
            final Connection connection = buildConnection();
            final Session session = buildSession(connection);
            final MessageConsumer consumer = buildConsumer(session);
            connection.start();
            return new JmsPackage(connection, session, consumer);
        } catch (final JMSException jmse) {
            throw new IllegalStateException("Unable to construct the necessary JMS instances", jmse);
        }

    }

    protected Connection buildConnection() throws JMSException {
        return connectionFactory.createConnection();
    }

    protected Session buildSession(final Connection aConnection) throws JMSException {
        return aConnection.createSession(transacted, acknowledgeMode);
    }

    protected MessageConsumer buildConsumer(final Session aSession) throws JMSException {
        return aSession.createConsumer(destination);
    }
}
