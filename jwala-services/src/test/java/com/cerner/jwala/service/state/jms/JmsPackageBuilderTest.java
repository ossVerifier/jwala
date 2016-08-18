package com.cerner.jwala.service.state.jms;

import org.junit.Before;
import org.junit.Test;

import com.cerner.jwala.service.state.jms.JmsPackage;
import com.cerner.jwala.service.state.jms.JmsPackageBuilder;

import javax.jms.*;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

import java.lang.IllegalStateException;

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
