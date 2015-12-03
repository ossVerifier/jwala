package com.siemens.cto.aem.service.state.jms;

import org.junit.Before;
import org.junit.Test;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Session;

import static org.mockito.Mockito.*;

public class JmsPackageTest {

    private JmsPackage jmsPackage;
    private Connection connection;
    private Session session;
    private MessageConsumer consumer;

    @Before
    public void setUp() throws Exception {
        connection = mock(Connection.class);
        session = mock(Session.class);
        consumer = mock(MessageConsumer.class);

        jmsPackage = new JmsPackage(connection,
                                    session,
                                    consumer);
    }

    @Test
    public void testClose() throws Exception {
        for (int i = 0; i < 10; i++) {
            jmsPackage.close();
        }
        verifyClosed(1);
    }

    @Test
    public void testCloseWithExceptions() throws Exception {
        final JMSException intentionalException = new JMSException("Intentional Test Failure");
        doThrow(intentionalException).when(connection).close();
        doThrow(intentionalException).when(session).close();
        doThrow(intentionalException).when(consumer).close();
        jmsPackage.close();
        verifyClosed(1);
    }

    private void verifyClosed(final int aNumberOfExpectedTimes) throws Exception {
        verify(connection, times(aNumberOfExpectedTimes)).close();
        verify(session, times(aNumberOfExpectedTimes)).close();
        verify(consumer, times(aNumberOfExpectedTimes)).close();
    }
}
