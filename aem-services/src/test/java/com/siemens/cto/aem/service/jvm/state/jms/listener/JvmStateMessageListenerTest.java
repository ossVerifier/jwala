package com.siemens.cto.aem.service.jvm.state.jms.listener;

import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.jvm.JvmState;
import com.siemens.cto.aem.domain.model.jvm.message.JvmStateMessage;
import com.siemens.cto.aem.domain.model.state.CurrentState;
import com.siemens.cto.aem.domain.command.state.JvmSetStateCommand;
import com.siemens.cto.aem.domain.model.user.User;
import com.siemens.cto.aem.service.jvm.state.jms.listener.message.JvmStateMapMessageConverter;
import com.siemens.cto.aem.service.state.StateService;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.TextMessage;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

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
        when(currentState.getState()).thenReturn(JvmState.JVM_STARTING);
        when(jvmStateService.getCurrentState(eq(Identifier.<Jvm>id(10L)))).thenReturn(currentState);
        when(newCurrentState.getState()).thenReturn(JvmState.JVM_STARTED);

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

    @Ignore //functionality removed
    @Test
    public void testHeartbeatPreventionStarting() throws Exception {
        when(currentState.getState()).thenReturn(JvmState.JVM_STARTING);
        when(jvmStateService.getCurrentState(eq(Identifier.<Jvm>id(10L)))).thenReturn(currentState);
        when(newCurrentState.getState()).thenReturn(JvmState.JVM_STOPPED);

        final MapMessage message = mock(MapMessage.class);
        when(converter.convert(eq(message))).thenReturn(convertedMessage);
        listener.onMessage(message);
        verify(listener, times(1)).handleMessage(eq(message));
        verify(listener, times(1)).processMessage(eq(message));
        verify(jvmStateService, times(0)).setCurrentState(eq(stateCommand), Matchers.<User>anyObject());
    }

    @Ignore //functionality removed
    @Test
    public void testHeartbeatPreventionStopping() throws Exception {
        when(currentState.getState()).thenReturn(JvmState.JVM_STOPPING);
        when(jvmStateService.getCurrentState(eq(Identifier.<Jvm>id(10L)))).thenReturn(currentState);
        when(newCurrentState.getState()).thenReturn(JvmState.JVM_STARTED);

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
        when(newCurrentState.getState()).thenReturn(JvmState.JVM_STARTED);

        final MapMessage message = mock(MapMessage.class);
        when(converter.convert(eq(message))).thenReturn(convertedMessage);
        listener.onMessage(message);
        verify(listener, times(1)).handleMessage(eq(message));
        verify(listener, times(1)).processMessage(eq(message));
        verify(jvmStateService, times(1)).setCurrentState(eq(stateCommand), Matchers.<User>anyObject());
    }

    /**
     * Test ensures that a started event is not filtered
     * just because the current state is stopping.
     */
    @Test
    public void testHeartbeatPreventionDeprecated() throws Exception {
        when(currentState.getState()).thenReturn(JvmState.JVM_STOPPING);
        when(jvmStateService.getCurrentState(eq(Identifier.<Jvm>id(10L)))).thenReturn(currentState);
        when(newCurrentState.getState()).thenReturn(JvmState.JVM_STARTED);
    
        final MapMessage message = mock(MapMessage.class);
        when(converter.convert(eq(message))).thenReturn(convertedMessage);
        listener.onMessage(message);
        verify(listener, times(1)).handleMessage(eq(message));
        verify(listener, times(1)).processMessage(eq(message));
        verify(jvmStateService, times(1)).setCurrentState(eq(stateCommand), Matchers.<User>anyObject());
    }
}
