package com.siemens.cto.aem.service.jvm.jms.listener;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.TextMessage;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;

import com.siemens.cto.aem.domain.model.jvm.command.SetJvmStateCommand;
import com.siemens.cto.aem.domain.model.temporary.User;
import com.siemens.cto.aem.service.jvm.JvmStateService;
import com.siemens.cto.aem.service.jvm.jms.listener.message.JvmStateMapMessageConverter;
import com.siemens.cto.aem.domain.model.jvm.message.JvmStateMessage;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class JvmStateMessageListenerTest {

    private JvmStateMessageListener listener;
    private JvmStateService jvmStateService;
    private JvmStateMapMessageConverter converter;
    private JvmStateMessage convertedMessage;
    private SetJvmStateCommand stateCommand;

    @Before
    public void setup() throws Exception {
        jvmStateService = mock(JvmStateService.class);
        converter = mock(JvmStateMapMessageConverter.class);
        convertedMessage = mock(JvmStateMessage.class);
        stateCommand = mock(SetJvmStateCommand.class);
        when(convertedMessage.toCommand()).thenReturn(stateCommand);
        listener = spy(new JvmStateMessageListener(jvmStateService,
                                                   converter));
    }

    @Test
    public void testOnMapMessage() throws Exception {
        final MapMessage message = mock(MapMessage.class);
        when(converter.convert(eq(message))).thenReturn(convertedMessage);
        listener.onMessage(message);
        verify(listener, times(1)).handleMessage(eq(message));
        verify(listener, times(1)).processMessage(eq(message));
        verify(jvmStateService, times(1)).setCurrentJvmState(eq(stateCommand), Matchers.<User>anyObject());
    }

    @Test
    public void testOnNonMapMessage() throws Exception {
        final Message message = mock(TextMessage.class);
        listener.onMessage(message);
        verify(listener, times(1)).handleMessage(eq(message));
        verify(listener, never()).processMessage(Matchers.<MapMessage>anyObject());
        verify(jvmStateService, never()).setCurrentJvmState(Matchers.<SetJvmStateCommand>anyObject(), Matchers.<User>anyObject());
    }

    @Test
    public void testHandleJmsException() throws Exception {
        final Message message = mock(Message.class);
        when(message.getJMSMessageID()).thenThrow(JMSException.class);
        listener.onMessage(message);
    }
}
