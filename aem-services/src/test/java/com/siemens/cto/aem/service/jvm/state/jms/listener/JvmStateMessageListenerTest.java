package com.siemens.cto.aem.service.jvm.state.jms.listener;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.TextMessage;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.jvm.JvmState;
import com.siemens.cto.aem.domain.model.jvm.message.JvmStateMessage;
import com.siemens.cto.aem.domain.model.state.CurrentState;
import com.siemens.cto.aem.domain.model.state.command.JvmSetStateCommand;
import com.siemens.cto.aem.domain.model.temporary.User;
import com.siemens.cto.aem.service.jvm.state.jms.listener.message.JvmStateMapMessageConverter;
import com.siemens.cto.aem.service.state.StateService;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class JvmStateMessageListenerTest {

    @Mock
    private StateService<Jvm, JvmState> jvmStateService;

    @Mock
    private JvmSetStateCommand stateCommand;
    
    @Mock
    private CurrentState<Jvm, JvmState> currentState;

    @Mock
    private CurrentState<Jvm, JvmState> newCurrentState;
    
    private JvmStateMessageListener listener;
    private JvmStateMapMessageConverter converter;
    private JvmStateMessage convertedMessage;

    @Before
    public void setup() throws Exception {
        converter = mock(JvmStateMapMessageConverter.class);
        convertedMessage = mock(JvmStateMessage.class);
        when(convertedMessage.toCommand()).thenReturn(stateCommand);
        when(stateCommand.getNewState()).thenReturn(newCurrentState);
        when(newCurrentState.getId()).thenReturn(Identifier.<Jvm>id(10L));
        listener = spy(new JvmStateMessageListener(jvmStateService,
                                                   converter));
    }

    @Test
    public void testOnMapMessage() throws Exception {
        when(currentState.getState()).thenReturn(JvmState.START_REQUESTED);
        when(jvmStateService.getCurrentState(eq(Identifier.<Jvm>id(10L)))).thenReturn(currentState);
        when(newCurrentState.getState()).thenReturn(JvmState.STARTED);

        final MapMessage message = mock(MapMessage.class);
        when(converter.convert(eq(message))).thenReturn(convertedMessage);
        listener.onMessage(message);
        verify(listener, times(1)).handleMessage(eq(message));
        verify(listener, times(1)).processMessage(eq(message));
        verify(jvmStateService, times(1)).setCurrentState(eq(stateCommand), Matchers.<User>anyObject());
    }

    @Test
    public void testOnNonMapMessage() throws Exception {
        final Message message = mock(TextMessage.class);
        listener.onMessage(message);
        verify(listener, times(1)).handleMessage(eq(message));
        verify(listener, never()).processMessage(Matchers.<MapMessage>anyObject());
        verify(jvmStateService, never()).setCurrentState(Matchers.<JvmSetStateCommand>anyObject(), Matchers.<User>anyObject());
    }

    @Test
    public void testHandleJmsException() throws Exception {
        final Message message = mock(Message.class);
        when(message.getJMSMessageID()).thenThrow(JMSException.class);
        listener.onMessage(message);
    }

    @Test
    public void testHeartbeatPreventionStarting() throws Exception {
        when(currentState.getState()).thenReturn(JvmState.START_REQUESTED);
        when(jvmStateService.getCurrentState(eq(Identifier.<Jvm>id(10L)))).thenReturn(currentState);
        when(newCurrentState.getState()).thenReturn(JvmState.STOPPED);

        final MapMessage message = mock(MapMessage.class);
        when(converter.convert(eq(message))).thenReturn(convertedMessage);
        listener.onMessage(message);
        verify(listener, times(1)).handleMessage(eq(message));
        verify(listener, times(1)).processMessage(eq(message));
        verify(jvmStateService, times(0)).setCurrentState(eq(stateCommand), Matchers.<User>anyObject());
    }

    @Test
    public void testHeartbeatPreventionStopping() throws Exception {
        when(currentState.getState()).thenReturn(JvmState.STOP_REQUESTED);
        when(jvmStateService.getCurrentState(eq(Identifier.<Jvm>id(10L)))).thenReturn(currentState);
        when(newCurrentState.getState()).thenReturn(JvmState.STARTED);

        final MapMessage message = mock(MapMessage.class);
        when(converter.convert(eq(message))).thenReturn(convertedMessage);
        listener.onMessage(message);
        verify(listener, times(1)).handleMessage(eq(message));
        verify(listener, times(1)).processMessage(eq(message));
        verify(jvmStateService, times(0)).setCurrentState(eq(stateCommand), Matchers.<User>anyObject());
    }

    @Test
    public void testHeartbeatPreventionNullCurrentState() throws Exception {
        when(jvmStateService.getCurrentState(eq(Identifier.<Jvm>id(10L)))).thenReturn(null);
        when(newCurrentState.getState()).thenReturn(JvmState.STARTED);

        final MapMessage message = mock(MapMessage.class);
        when(converter.convert(eq(message))).thenReturn(convertedMessage);
        listener.onMessage(message);
        verify(listener, times(1)).handleMessage(eq(message));
        verify(listener, times(1)).processMessage(eq(message));
        verify(jvmStateService, times(1)).setCurrentState(eq(stateCommand), Matchers.<User>anyObject());
    }
}
