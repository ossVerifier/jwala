package com.siemens.cto.aem.service.jvm.state.jms.listener;

import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.jvm.Jvm;
import com.siemens.cto.aem.common.domain.model.jvm.JvmState;
import com.siemens.cto.aem.common.domain.model.jvm.message.JvmStateMessage;
import com.siemens.cto.aem.common.domain.model.state.CurrentState;
import com.siemens.cto.aem.common.request.state.JvmSetStateRequest;
import com.siemens.cto.aem.common.domain.model.user.User;
import com.siemens.cto.aem.persistence.service.JvmPersistenceService;
import com.siemens.cto.aem.service.jvm.state.jms.listener.message.JvmStateMapMessageConverter;
import com.siemens.cto.aem.service.spring.component.GrpStateComputationAndNotificationSvc;
import com.siemens.cto.aem.service.state.StateNotificationService;
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
    private JvmSetStateRequest stateCommand;
    
    @Mock
    private CurrentState<Jvm, JvmState> currentState;

    @Mock
    private CurrentState<Jvm, JvmState> newCurrentState;

    @Mock
    private StateNotificationService mockStateNotificationService;

    @Mock
    private JvmPersistenceService mockJvmPersistenceService;

    @Mock
    private GrpStateComputationAndNotificationSvc mockGrpStateComputationAndNotificationSvc;

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
        listener = spy(new JvmStateMessageListener(converter, mockJvmPersistenceService, mockGrpStateComputationAndNotificationSvc));
    }

    @Test
    @Ignore
    // TODO: Fix!
    public void testOnMapMessage() throws Exception {
        when(currentState.getState()).thenReturn(JvmState.JVM_STARTING);

        // TODO: Do we need to get the state ? It is already in the JVM. rewrite this test...
        // when(jvmStateService.getCurrentState(eq(Identifier.<Jvm>id(10L)))).thenReturn(currentState);

        when(newCurrentState.getState()).thenReturn(JvmState.JVM_STARTED);

        final MapMessage message = mock(MapMessage.class);
        when(converter.convert(eq(message))).thenReturn(convertedMessage);
        listener.onMessage(message);
        verify(listener, times(1)).handleMessage(eq(message));
        verify(listener, times(1)).processMessage(eq(message));
        verify(mockJvmPersistenceService, times(1)).updateState(any(Identifier.class), eq(JvmState.JVM_STARTED));
    }

    @Test
    public void testOnNonMapMessage() throws Exception {
        final Message message = mock(TextMessage.class);
        listener.onMessage(message);
        verify(listener, times(1)).handleMessage(eq(message));
        verify(listener, never()).processMessage(Matchers.<MapMessage>anyObject());
        verify(mockJvmPersistenceService, never()).updateState(any(Identifier.class), any(JvmState.class));
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

        // TODO: Rewrite this test. The state is already in the JVM...
        // when(jvmStateService.getCurrentState(eq(Identifier.<Jvm>id(10L)))).thenReturn(currentState);

        when(newCurrentState.getState()).thenReturn(JvmState.JVM_STOPPED);

        final MapMessage message = mock(MapMessage.class);
        when(converter.convert(eq(message))).thenReturn(convertedMessage);
        listener.onMessage(message);
        verify(listener, times(1)).handleMessage(eq(message));
        verify(listener, times(1)).processMessage(eq(message));
        verify(mockJvmPersistenceService, times(0)).updateState(any(Identifier.class), any(JvmState.class));
    }

    @Ignore //functionality removed
    @Test
    public void testHeartbeatPreventionStopping() throws Exception {
        when(currentState.getState()).thenReturn(JvmState.JVM_STOPPING);

        // TODO: Rewrite this test. The state is already in the JVM...
        // when(jvmStateService.getCurrentState(eq(Identifier.<Jvm>id(10L)))).thenReturn(currentState);

        when(newCurrentState.getState()).thenReturn(JvmState.JVM_STARTED);

        final MapMessage message = mock(MapMessage.class);
        when(converter.convert(eq(message))).thenReturn(convertedMessage);
        listener.onMessage(message);
        verify(listener, times(1)).handleMessage(eq(message));
        verify(listener, times(1)).processMessage(eq(message));
        verify(mockJvmPersistenceService, times(0)).updateState(any(Identifier.class), any(JvmState.class));
    }

    @Test
    @Ignore
    // TODO: Fix!
    public void testHeartbeatPreventionNullCurrentState() throws Exception {
        // TODO: Rewrite this test. The state is already in the JVM...
        // when(jvmStateService.getCurrentState(eq(Identifier.<Jvm>id(10L)))).thenReturn(null);
        when(newCurrentState.getState()).thenReturn(JvmState.JVM_STARTED);

        final MapMessage message = mock(MapMessage.class);
        when(converter.convert(eq(message))).thenReturn(convertedMessage);
        listener.onMessage(message);
        verify(listener, times(1)).handleMessage(eq(message));
        verify(listener, times(1)).processMessage(eq(message));
        verify(mockJvmPersistenceService, times(1)).updateState(any(Identifier.class), any(JvmState.class));
    }

}
