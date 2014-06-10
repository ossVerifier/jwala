package com.siemens.cto.aem.service.jvm.state.jms;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JmsPackage {

    private static final Logger LOGGER = LoggerFactory.getLogger(JmsPackage.class);

    private final Connection connection;
    private final Session session;
    private final MessageConsumer consumer;
    private volatile boolean wasClosed;

    public JmsPackage(final Connection theConnection,
                      final Session theSession,
                      final MessageConsumer theConsumer) {
        connection = theConnection;
        session = theSession;
        consumer = theConsumer;
        wasClosed = false;
    }

    public MessageConsumer getConsumer() {
        return consumer;
    }

    public void close() {
        if (!wasClosed) {
            wasClosed = true;
            closeConsumer();
            closeSession();
            closeConnection();
        }
    }

    protected void closeConsumer() {
        if (consumer != null) {
            try {
                consumer.close();
            } catch (final JMSException jmse) {
                LOGGER.warn("Problem closing JMS MessageConsumer", jmse);
            }
        }
    }

    protected void closeSession() {
        if (session != null) {
            try {
                session.close();
            } catch (final JMSException jmse) {
                LOGGER.warn("Problem closing JMS Session", jmse);
            }
        }
    }

    protected void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
            } catch (final JMSException jmse) {
                LOGGER.warn("Problem closing JMS Connection", jmse);
            }
        }
    }
}
