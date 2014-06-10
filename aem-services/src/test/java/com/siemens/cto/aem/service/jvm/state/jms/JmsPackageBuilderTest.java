package com.siemens.cto.aem.service.jvm.state.jms;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Session;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class JmsPackageBuilderTest {

    private JmsPackageBuilder builder;
    private ConnectionFactory connectionFactory;
    private Destination destination;
    private Connection connection;
    private Session session;
    private MessageConsumer consumer;
    private boolean transacted;
    private int acknowledgementMode;

    @Before
    public void setUp() throws Exception {
        connectionFactory = mock(ConnectionFactory.class);
        destination = mock(Destination.class);
        connection = mock(Connection.class);
        session = mock(Session.class);
        consumer = mock(MessageConsumer.class);
        transacted = true;
        acknowledgementMode = Session.SESSION_TRANSACTED;

        builder = new JmsPackageBuilder().setConnectionFactory(connectionFactory)
                                         .setDestination(destination)
                                         .setTransacted(transacted)
                                         .setAcknowledgeMode(acknowledgementMode);
    }

    @Test
    public void testBuild() throws Exception {
        when(connectionFactory.createConnection()).thenReturn(connection);
        when(connection.createSession(eq(transacted),
                                      eq(acknowledgementMode))).thenReturn(session);
        when(session.createConsumer(eq(destination))).thenReturn(consumer);

        final JmsPackage jmsPackage = builder.build();

        assertEquals(consumer,
                     jmsPackage.getConsumer());
        verify(connection, times(1)).start();
    }

    @Test(expected = IllegalStateException.class)
    public void testBuildWithException() throws Exception {
        when(connectionFactory.createConnection()).thenThrow(new JMSException("Intentional test failure"));

        builder.build();
    }
}
