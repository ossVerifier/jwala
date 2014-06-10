package com.siemens.cto.aem.service.jvm.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.runners.MockitoJUnitRunner;

import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.CurrentJvmState;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.jvm.command.SetJvmStateCommand;
import com.siemens.cto.aem.domain.model.temporary.User;
import com.siemens.cto.aem.persistence.service.jvm.JvmStatePersistenceService;
import com.siemens.cto.aem.service.jvm.JvmStateNotificationService;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class JvmStateServiceImplVerificationTest {

    private JvmStateServiceImpl impl;
    private JvmStatePersistenceService persistenceService;
    private JvmStateNotificationService notificationService;
    private SetJvmStateCommand command;
    private User user;
    private CurrentJvmState jvmState;
    private Identifier<Jvm> jvmId;

    @Captor
    private ArgumentCaptor<Event<SetJvmStateCommand>> eventCaptor;

    @Before
    public void setUp() throws Exception {
        persistenceService = mock(JvmStatePersistenceService.class);
        notificationService = mock(JvmStateNotificationService.class);
        command = mock(SetJvmStateCommand.class);
        user = mock(User.class);
        jvmState = mock(CurrentJvmState.class);
        jvmId = new Identifier<>(123456L);

        when(command.getNewJvmState()).thenReturn(jvmState);
        when(jvmState.getJvmId()).thenReturn(jvmId);

        impl = new JvmStateServiceImpl(persistenceService,
                                       notificationService);
    }

    @Test
    public void testStateChanged() {
        impl.setCurrentJvmState(command,
                                user);
        verify(command, times(1)).validateCommand();
        verify(persistenceService, times(1)).updateJvmState(eventCaptor.capture());
        verify(notificationService, times(1)).notifyJvmStateUpdated(eq(jvmId));
        assertEquals(command,
                     eventCaptor.getValue().getCommand());

    }
}
