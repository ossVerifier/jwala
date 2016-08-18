package com.cerner.jwala.service.state.jms.sender.message;

import com.cerner.jwala.common.domain.model.state.message.CommonStateKey;
import com.cerner.jwala.common.domain.model.state.message.StateKey;
import com.cerner.jwala.service.state.jms.sender.message.MessageCreatorKeyValueStateConsumer;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.Session;

import static org.junit.Assert.assertSame;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MessageCreatorKeyValueStateConsumerTest {

    private MessageCreatorKeyValueStateConsumer consumer;

    @Mock
    private Session session;

    @Mock
    private MapMessage message;

    @Before
    public void setup() throws Exception {
        consumer = new MessageCreatorKeyValueStateConsumer();
        when(session.createMapMessage()).thenReturn(message);
    }

    @Test
    public void testConsume() throws Exception {
        final StateKey key = CommonStateKey.ID;
        final String value = "the value I'm expecting";

        consumer.set(key,
                     value);

        final Message actualMessage = consumer.createMessage(session);

        assertSame(message,
                   actualMessage);
        verify(message, times(1)).setString(eq(key.getKey()),
                                            eq(value));
    }
}
