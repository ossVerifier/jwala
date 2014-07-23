//package com.siemens.cto.aem.service.jvm.state.impl;
//
//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertTrue;
//import static org.mockito.Matchers.eq;
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.times;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//
//import java.util.HashSet;
//import java.util.Set;
//
//import org.joda.time.DateTime;
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.mockito.ArgumentCaptor;
//import org.mockito.Captor;
//import org.mockito.Matchers;
//import org.mockito.Mock;
//import org.mockito.runners.MockitoJUnitRunner;
//
//import com.siemens.cto.aem.domain.model.event.Event;
//import com.siemens.cto.aem.domain.model.id.Identifier;
//import com.siemens.cto.aem.domain.model.jvm.CurrentJvmState;
//import com.siemens.cto.aem.domain.model.jvm.Jvm;
//import com.siemens.cto.aem.domain.model.jvm.JvmState;
//import com.siemens.cto.aem.domain.model.jvm.command.SetJvmStateCommand;
//import com.siemens.cto.aem.domain.model.temporary.User;
//import com.siemens.cto.aem.persistence.service.jvm.JvmStatePersistenceService;
//import com.siemens.cto.aem.service.jvm.state.JvmStateNotificationService;
//import com.siemens.cto.aem.service.state.StateNotificationGateway;
//
//@RunWith(MockitoJUnitRunner.class)
//public class JvmStateServiceImplVerificationTest {
//
//    private JvmStateServiceImpl impl;
//
//    @Mock
//    private StateNotificationGateway stateNotificationGateway;
//
//    private JvmStatePersistenceService persistenceService;
//    private JvmStateNotificationService notificationService;
//    private SetJvmStateCommand command;
//    private User user;
//    private CurrentJvmState jvmState;
//    private Identifier<Jvm> jvmId;
//
//    @Captor
//    private ArgumentCaptor<Event<SetJvmStateCommand>> eventCaptor;
//
//    @Captor
//    private ArgumentCaptor<Identifier<Jvm>> jvmIdCaptor;
//
//    @Before
//    public void setUp() throws Exception {
//        persistenceService = mock(JvmStatePersistenceService.class);
//        notificationService = mock(JvmStateNotificationService.class);
//        command = mock(SetJvmStateCommand.class);
//        user = mock(User.class);
//        jvmState = mock(CurrentJvmState.class);
//        jvmId = new Identifier<>(123456L);
//
//        when(command.getNewJvmState()).thenReturn(jvmState);
//        when(jvmState.getJvmId()).thenReturn(jvmId);
//
//        impl = new JvmStateServiceImpl(persistenceService,
//                                       notificationService,
//                                       stateNotificationGateway);
//    }
//
//    @Test
//    public void testStateChanged() {
//        impl.setCurrentJvmState(command,
//                                user);
//        verify(command, times(1)).validateCommand();
//        verify(persistenceService, times(1)).updateJvmState(eventCaptor.capture());
//        verify(notificationService, times(1)).notifyJvmStateUpdated(eq(jvmId));
//        assertEquals(command,
//                     eventCaptor.getValue().getCommand());
//
//    }
//
//    @Test
//    public void testGetCurrentState() {
//        final CurrentJvmState expectedJvmState = new CurrentJvmState(jvmId,
//                                                                     JvmState.STARTED,
//                                                                     DateTime.now());
//        when(persistenceService.getJvmState(eq(jvmId))).thenReturn(expectedJvmState);
//        final CurrentJvmState actualJvmState = impl.getCurrentJvmState(jvmId);
//        assertEquals(expectedJvmState,
//                     actualJvmState);
//    }
//
//    @Test
//    public void testUnknownJvmId() {
//        when(persistenceService.getJvmState(eq(jvmId))).thenReturn(null);
//        final CurrentJvmState actualState = impl.getCurrentJvmState(jvmId);
//        assertEquals(JvmState.UNKNOWN,
//                     actualState.getJvmState());
//    }
//
//    @Test
//    public void testGetStates() {
//        when(persistenceService.getJvmState(Matchers.<Identifier<Jvm>>anyObject())).thenReturn(null);
//        final Set<Identifier<Jvm>> requestedIds = new HashSet<>();
//        final int numberOfIds = 100;
//        for (int i = 0; i < numberOfIds; i++) {
//            requestedIds.add(new Identifier<Jvm>((long)i));
//        }
//        final Set<CurrentJvmState> actualStates = impl.getCurrentJvmStates(requestedIds);
//        assertEquals(numberOfIds,
//                     actualStates.size());
//        for (final CurrentJvmState state : actualStates) {
//            assertTrue(requestedIds.contains(state.getJvmId()));
//            assertEquals(JvmState.UNKNOWN,
//                         state.getJvmState());
//        }
//    }
//}
