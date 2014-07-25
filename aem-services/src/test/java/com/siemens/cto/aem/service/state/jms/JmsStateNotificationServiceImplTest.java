package com.siemens.cto.aem.service.state.jms;

import javax.jms.Destination;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.jms.core.JmsTemplate;

import com.siemens.cto.aem.domain.model.state.CurrentState;
import com.siemens.cto.aem.service.state.StateNotificationConsumer;
import com.siemens.cto.aem.service.state.StateNotificationConsumerBuilder;
import com.siemens.cto.aem.service.state.StateNotificationService;
import com.siemens.cto.aem.service.state.jms.sender.message.MessageCreatorKeyValueStateConsumer;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class JmsStateNotificationServiceImplTest {

    private StateNotificationService service;

    @Mock
    private JmsTemplate template;

    @Mock
    private Destination destination;

    @Mock
    private StateNotificationConsumerBuilder builder;

    @Mock
    private StateNotificationConsumer consumer;

    @Mock
    private CurrentState currentState;

    @Captor
    private ArgumentCaptor<MessageCreatorKeyValueStateConsumer> messageCreatorCaptor;

    @Before
    public void setup() throws Exception {
        setupBuilderAndConsumer();
        service = new JmsStateNotificationServiceImpl(template,
                                                      destination,
                                                      builder);
    }

    @Test
    public void testNotifyStateUpdated() throws Exception {
        service.register();
        service.notifyStateUpdated(currentState);

        verify(consumer, times(1)).isStale();
        verify(currentState, times(1)).provideState(messageCreatorCaptor.capture());
        verify(template, times(1)).send(eq(destination),
                                        eq(messageCreatorCaptor.getValue()));
    }

    private void setupBuilderAndConsumer() {
        when(builder.build()).thenReturn(consumer);
        when(consumer.isStale()).thenReturn(false);
    }
}
