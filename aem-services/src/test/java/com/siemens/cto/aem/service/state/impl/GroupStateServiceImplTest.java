package com.siemens.cto.aem.service.state.impl;

import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.aem.domain.model.group.*;
import com.siemens.cto.aem.domain.model.group.command.ControlGroupCommand;
import com.siemens.cto.aem.domain.model.group.command.SetGroupStateCommand;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.jvm.JvmState;
import com.siemens.cto.aem.domain.model.path.Path;
import com.siemens.cto.aem.domain.model.state.CurrentState;
import com.siemens.cto.aem.domain.model.state.StateType;
import com.siemens.cto.aem.domain.model.state.command.SetStateCommand;
import com.siemens.cto.aem.domain.model.temporary.User;
import com.siemens.cto.aem.domain.model.webserver.WebServer;
import com.siemens.cto.aem.domain.model.webserver.WebServerReachableState;
import com.siemens.cto.aem.persistence.dao.webserver.WebServerDao;
import com.siemens.cto.aem.persistence.service.group.GroupPersistenceService;
import com.siemens.cto.aem.persistence.service.jvm.JvmPersistenceService;
import com.siemens.cto.aem.persistence.service.state.StatePersistenceService;
import com.siemens.cto.aem.service.group.GroupStateMachine;
import com.siemens.cto.aem.service.state.*;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.integration.Message;
import org.springframework.integration.MessagingException;
import org.springframework.integration.core.MessageHandler;
import org.springframework.integration.core.SubscribableChannel;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import java.util.*;
import java.util.concurrent.ScheduledExecutorService;

import static com.siemens.cto.aem.domain.model.id.Identifier.id;
import static org.junit.Assert.*;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;


@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = {GroupStateServiceImplTest.CommonConfiguration.class})
@Ignore
@Deprecated
public class GroupStateServiceImplTest {

    @Autowired
    @Qualifier("jvmStateUpdates")
    SubscribableChannel stateUpdates;

    @Autowired
    @Qualifier("groupStateService")
    GroupStateService.API getGroupStateService;

    @Autowired
    StateNotificationWorker stateNotificationWorker;

    @Autowired
    JvmPersistenceService jvmPersistenceService;

    @Autowired
    GroupPersistenceService groupPersistenceService;

    @Autowired
    TaskExecutor statePubSubBusExecutor;

    @Autowired
    GroupStateService.API groupStateService;

    @Autowired
    GroupStateMachine groupStateManager;

    @Autowired
    WebServerDao webServerDao;

    int updateReceived = 0;

    @Ignore // right now we are using direct channel, not pub/sub
    @Test
    public void testSubscribeAndReceive() throws InterruptedException {
        stateUpdates.subscribe(new MessageHandler() {

            @Override
            public void handleMessage(Message<?> message) throws MessagingException {
                updateReceived = 1;
                synchronized (GroupStateServiceImplTest.this) {
                    GroupStateServiceImplTest.this.notify();
                }
            }
        });

        synchronized (this) {
            stateNotificationWorker.sendStateChangeNotification(groupStateService,
                    new CurrentState<>(id(0L, Jvm.class), JvmState.JVM_STARTED, DateTime.now(), StateType.JVM));
            this.wait(5000);
        }

        assertTrue(updateReceived > 0);
    }

    @Ignore // right now we are using direct channel, not pub/sub
    @Test
    public void testSubscribeAndReceiveIsParallel() throws InterruptedException {
        stateUpdates.subscribe(new MessageHandler() {

            @Override
            public void handleMessage(Message<?> message) throws MessagingException {
                try {
                    Thread.sleep(100);
                    synchronized (GroupStateServiceImplTest.this) {
                        ++updateReceived;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                synchronized (GroupStateServiceImplTest.this) {
                    GroupStateServiceImplTest.this.notify();
                }
            }
        });

        // test
        synchronized (this) {
            for (int i = 0; i < 3; ++i) {
                stateNotificationWorker.sendStateChangeNotification(groupStateService,
                        new CurrentState<>(id(0L, Jvm.class), JvmState.JVM_STARTED, DateTime.now(), StateType.JVM));
            }
            this.wait(250); // for the first one
        }

        Thread.sleep(100); // for the rest.

        assertEquals(3, updateReceived);
    }

    ScheduledExecutorService concurrencyActions = java.util.concurrent.Executors.newScheduledThreadPool(10);

    @Test
    public void testStateUpdatedJVMResiliency() throws InterruptedException {
        for (JvmState js : JvmState.values()) {
            groupStateService.stateUpdateJvm(new CurrentState<>(id(0L, Jvm.class), js, DateTime.now(), StateType.JVM));
        }
    }

    Group group, groupWith3;
    LiteGroup lgroup, lgroupWith3;
    Set<LiteGroup> lgroups = new HashSet<>(), lgroupsWith3 = new HashSet<>();
    Jvm jvm, jvm2, jvm3, jvm4;
    Set<Jvm> jvms = new HashSet<>(), jvmsThree = new HashSet<>();

    @SuppressWarnings("unchecked")
    @Before
    public void setupEntities() {

        group = new Group(id(1L, Group.class), "");
        groupWith3 = new Group(id(2L, Group.class), "");
        lgroup = new LiteGroup(id(1L, Group.class), "");
        lgroupWith3 = new LiteGroup(id(2L, Group.class), "");
        lgroups.add(lgroup);
        lgroupsWith3.add(lgroupWith3);
        jvm = new Jvm(id(1L, Jvm.class), "", "", lgroups, 0, 0, 0, 0, 0, new Path("/hct"),
                "EXAMPLE_OPTS=%someEnv%/someVal");
        jvms.add(jvm);
        jvm2 = new Jvm(id(2L, Jvm.class), "", "", lgroupsWith3, 0, 0, 0, 0, 0, new Path("/hct"),
                "EXAMPLE_OPTS=%someEnv%/someVal");
        jvmsThree.add(jvm2);
        jvm3 = new Jvm(id(3L, Jvm.class), "", "", lgroupsWith3, 0, 0, 0, 0, 0, new Path("/hct"),
                "EXAMPLE_OPTS=%someEnv%/someVal");
        jvmsThree.add(jvm3);
        jvm4 = new Jvm(id(4L, Jvm.class), "", "", lgroupsWith3, 0, 0, 0, 0, 0, new Path("/hct"),
                "EXAMPLE_OPTS=%someEnv%/someVal");
        jvmsThree.add(jvm4);
        group = new Group(group.getId(), group.getName(), jvms);
        groupWith3 = new Group(groupWith3.getId(), groupWith3.getName(), jvmsThree);

        when(jvmPersistenceService.getJvm(eq(jvm.getId()))).thenReturn(jvm);
        when(jvmPersistenceService.getJvm(eq(jvm2.getId()))).thenReturn(jvm2);
        when(jvmPersistenceService.getJvm(eq(jvm3.getId()))).thenReturn(jvm3);
        when(jvmPersistenceService.getJvm(eq(jvm4.getId()))).thenReturn(jvm4);
        when(groupPersistenceService.getGroup(eq(group.getId()))).thenReturn(group);
        when(groupPersistenceService.getGroup(eq(groupWith3.getId()))).thenReturn(groupWith3);

        when(groupPersistenceService.updateState(Mockito.any(Event.class))).thenAnswer(new Answer<CurrentState<Group, GroupState>>() {

            @Override
            public CurrentState<Group, GroupState> answer(InvocationOnMock invocation) throws Throwable {

                Event<SetStateCommand<Group, GroupState>> event = (Event<SetStateCommand<Group, GroupState>>) invocation.getArguments()[0];
                if (group.getId().getId() == 1L) {
                    group = new Group(group.getId(), group.getName(), group.getJvms(), event.getCommand().getNewState().getState(), DateTime.now());
                    return group.getCurrentState();
                } else if (group.getId().getId() == 2L) {
                    groupWith3 = new Group(groupWith3.getId(), groupWith3.getName(), groupWith3.getJvms(), event.getCommand().getNewState().getState(), DateTime.now());
                    return group.getCurrentState();
                }
                fail("Unexpected groupId " + group.getId());
                return null;
            }

        });

    }

    @Test
    public void testStateUpdatedJVM() throws InterruptedException {

        List<SetGroupStateCommand> updates;
        SetGroupStateCommand sgsc;

        when(groupStateManager.getCurrentStateDetail()).thenReturn(new CurrentGroupState(group.getId(), GroupState.GRP_INITIALIZED, DateTime.now()));
        when(groupStateManager.getCurrentState()).thenReturn(GroupState.GRP_INITIALIZED);
        updates = groupStateService.stateUpdateJvm(new CurrentState<>(id(1L, Jvm.class), JvmState.JVM_UNKNOWN, DateTime.now(), StateType.JVM));

        sgsc = updates.get(0);
        groupStateService.groupStateUnlock(sgsc);

        assertEquals(GroupState.GRP_INITIALIZED, sgsc.getNewState().getState());
        updates = groupStateService.stateUpdateJvm(new CurrentState<>(id(1L, Jvm.class), JvmState.JVM_INITIALIZED, DateTime.now(), StateType.JVM));

        sgsc = updates.get(0);
        groupStateService.groupStateUnlock(sgsc);
        assertEquals(GroupState.GRP_INITIALIZED, sgsc.getNewState().getState());

        updates = updateJvmState(id(1L, Jvm.class), JvmState.JVM_STARTING);

        sgsc = updates.get(0);
        assertEquals(GroupState.GRP_INITIALIZED, sgsc.getNewState().getState());

        when(groupStateManager.getCurrentState()).thenReturn(GroupState.GRP_STARTED);
        when(groupStateManager.getCurrentStateDetail()).thenReturn(new CurrentGroupState(group.getId(), GroupState.GRP_STARTED, DateTime.now()));

        updates = updateJvmState(id(1L, Jvm.class), JvmState.JVM_STARTED);

        sgsc = updates.get(0);
        assertEquals(GroupState.GRP_STARTED, sgsc.getNewState().getState());

        // In the new GSM, we do not call the trigger methods. The GSM is 
        // evaluated based on current state.
        // verify(groupStateManager, times(1)).jvmStarted(eq(jvm.getId()));

        group = new Group(group.getId(), group.getName(), jvms, GroupState.GRP_STARTED, DateTime.now());
        when(groupPersistenceService.getGroup(eq(group.getId()))).thenReturn(group);
        updates = updateJvmState(id(1L, Jvm.class), JvmState.JVM_STOPPING);

        when(groupStateManager.getCurrentState()).thenReturn(GroupState.GRP_STOPPED);
        when(groupStateManager.getCurrentStateDetail()).thenReturn(new CurrentGroupState(group.getId(), GroupState.GRP_STOPPED, DateTime
                .now()));
        updates = updateJvmState(id(1L, Jvm.class), JvmState.JVM_STOPPED);

        // In the new GSM, we do not call the trigger methods. The GSM is 
        // evaluated based on current state.
        // verify(groupStateManager, times(1)).jvmStopped(eq(jvm.getId()));

        sgsc = updates.get(0);
        assertEquals(GroupState.GRP_STOPPED, sgsc.getNewState().getState());

        group = new Group(group.getId(), group.getName(), jvms, GroupState.GRP_STOPPED, DateTime.now());
        when(groupPersistenceService.getGroup(eq(group.getId()))).thenReturn(group);
        updates = updateJvmState(id(1L, Jvm.class), JvmState.JVM_FAILED);

        // In the new GSM, we do not call the trigger methods. The GSM is 
        // evaluated based on current state.
        // verify(groupStateManager, times(1)).jvmError(eq(jvm.getId()));
    }

    @Test
    public void testWebServerStateUnfinished() throws InterruptedException {
        groupStateService.stateUpdateWebServer(new CurrentState<>(id(0L, WebServer.class), WebServerReachableState.WS_REACHABLE, DateTime
                .now(), StateType.WEB_SERVER));
    }

    @Test
    public void testSignals() {
        groupStateService.signalReset(id(1L, Group.class), User.getSystemUser());
        verify(groupStateManager, times(1)).signalReset(eq(User.getSystemUser()));
        groupStateService.signalStartRequested(id(1L, Group.class), User.getSystemUser());
        verify(groupStateManager, times(1)).signalStartRequested(eq(User.getSystemUser()));
        groupStateService.signalStopRequested(id(1L, Group.class), User.getSystemUser());
        verify(groupStateManager, times(1)).signalStopRequested(eq(User.getSystemUser()));
    }

    @Test
    public void testQuery() {
        groupStateService.canStart(id(1L, Group.class), User.getSystemUser());
        verify(groupPersistenceService).getGroup(eq(group.getId()));
        verify(groupStateManager, times(1)).canStart();
        verify(groupStateManager, times(1)).synchronizedInitializeGroup(group, User.getSystemUser());

        groupStateService.canStop(id(1L, Group.class), User.getSystemUser());
        verify(groupStateManager, times(1)).canStop();
    }

    private List<SetGroupStateCommand> updateJvmState(final Identifier<Jvm> jvmId, final JvmState aState) throws InterruptedException {
        List<SetGroupStateCommand> potentiallyLocked = groupStateService.stateUpdateJvm(new CurrentState<>(jvmId, aState, DateTime.now(), StateType.JVM));
        for (SetGroupStateCommand sgsc : potentiallyLocked) {
            groupStateService.groupStateUnlock(sgsc);
        }
        return potentiallyLocked;
    }

    @Test
    public void testUpdateJvmEmptyGroups() {
        Identifier<Jvm> theId = new Identifier<>(1L);
        Jvm jvmNoGroups = new Jvm(theId, "testJvmName", new HashSet<LiteGroup>());
        when(jvmPersistenceService.getJvm(theId)).thenReturn(jvmNoGroups);
        boolean wasInterrupted = false;
        List<SetGroupStateCommand> result = null;
        try {
            result = groupStateService.stateUpdateJvm(new CurrentState<Jvm, JvmState>(theId, JvmState.JVM_STARTED, DateTime.now(), StateType.JVM));
        } catch (InterruptedException e) {
            wasInterrupted = true;
        }
        assertFalse(wasInterrupted);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testStateUpdateWebserverEmptyGroups() {
        Identifier<WebServer> theId = new Identifier<WebServer>(1L);
        WebServer testWS = mock(WebServer.class);
        when(testWS.getId()).thenReturn(theId);
        when(testWS.getGroups()).thenReturn(null);
        when(webServerDao.getWebServer(theId)).thenReturn(testWS);
        boolean wasInterrupted = false;
        List<SetGroupStateCommand> result = null;
        try {
            result = groupStateService.stateUpdateWebServer(new CurrentState<>(theId, WebServerReachableState.WS_REACHABLE, DateTime.now(), StateType.WEB_SERVER));
        } catch (InterruptedException e) {
            wasInterrupted = true;
        }
        assertFalse(wasInterrupted);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testStateUpdateWebserverNonEmptyGroups() {
        Identifier<WebServer> theId = new Identifier<WebServer>(1L);
        WebServer testWS = mock(WebServer.class);
        Collection<Group> groupList = new ArrayList<>();
        Group testGroup = mock(Group.class);
        Identifier<Group> groupId = new Identifier<>(99L);
        groupList.add(testGroup);
        when(testWS.getId()).thenReturn(theId);
        when(testGroup.getId()).thenReturn(groupId);
        when(testWS.getGroups()).thenReturn(groupList);
        when(webServerDao.getWebServer(theId)).thenReturn(testWS);
        boolean wasInterrupted = false;
        List<SetGroupStateCommand> result = null;
        try {
            result = groupStateService.stateUpdateWebServer(new CurrentState<>(theId, WebServerReachableState.WS_REACHABLE, DateTime.now(), StateType.WEB_SERVER));
        } catch (InterruptedException e) {
            wasInterrupted = true;
        }
        assertFalse(wasInterrupted);
        assertNotNull(result);
        assertFalse(result.isEmpty());

        Collection<Group> groupList2 = new ArrayList<>();
        Group testGroup2 = mock(Group.class);
        Identifier<Group> group2Id = new Identifier<>(999L);
        groupList2.add(testGroup2);
        when(testWS.getGroups()).thenReturn(groupList2);
        when(testGroup.getId()).thenReturn(group2Id);
        when(testGroup2.getCurrentState()).thenThrow(new RuntimeException("Force refreshGroups to run catch clause"));
        boolean runtimeException = false;
        try {
            result = groupStateService.stateUpdateWebServer(new CurrentState<>(theId, WebServerReachableState.WS_REACHABLE, DateTime.now(), StateType.WEB_SERVER));
        } catch (InterruptedException e) {
            wasInterrupted = true;
        } catch (RuntimeException e) {
            runtimeException = true;
        }
        assertFalse(wasInterrupted);
        assertTrue(runtimeException);
    }

    @Test
    public void testStateUpdateRequest() {
        Group mockGroup = mock(Group.class);
        when(mockGroup.getId()).thenReturn(new Identifier<Group>(99L));
        boolean wasInterrupted = false;
        SetGroupStateCommand result = null;
        try {
            result = groupStateService.stateUpdateRequest(mockGroup);
        } catch (InterruptedException e) {
            wasInterrupted = true;
        }
        assertFalse(wasInterrupted);
        assertNotNull(result);
    }

    @Test
    public void testSignal(){
        Identifier<Group> groupId = new Identifier<>(99L);
        ControlGroupCommand mockCommand = mock(ControlGroupCommand.class);
        User mockUser = mock(User.class);
        Group mockGroup = mock(Group.class);
        when(mockGroup.getId()).thenReturn(groupId);
        when(mockCommand.getControlOperation()).thenReturn(GroupControlOperation.START);
        when(mockCommand.getGroupId()).thenReturn(groupId);
        when(groupPersistenceService.getGroup(groupId)).thenReturn(mockGroup);
        when(groupStateManager.signalStartRequested(mockUser)).thenReturn(new CurrentGroupState(groupId, GroupState.GRP_STARTING, DateTime.now(), new CurrentGroupState.StateDetail(0,1), new CurrentGroupState.StateDetail(2,3)));
        when(groupStateManager.signalStopRequested(mockUser)).thenReturn(new CurrentGroupState(groupId, GroupState.GRP_STARTING, DateTime.now(), new CurrentGroupState.StateDetail(0,1), new CurrentGroupState.StateDetail(2,3)));
        CurrentGroupState result = groupStateService.signal(mockCommand, mockUser);
        assertNotNull(result);

        when(mockCommand.getControlOperation()).thenReturn(GroupControlOperation.STOP);
        result = groupStateService.signal(mockCommand, mockUser);
        assertNotNull(result);
    }

    @Test
    public void testGroupStatePersist(){
        SetGroupStateCommand mockGroupCommand = mock(SetGroupStateCommand.class);
        CurrentState<Group, GroupState> mockCurrentState = mock(CurrentState.class);
        Identifier<Group> groupId = new Identifier<>(99L);
        when(mockCurrentState.getId()).thenReturn(groupId);
        when(mockGroupCommand.getNewState()).thenReturn(mockCurrentState);
        when(groupPersistenceService.updateGroupStatus(any(Event.class))).thenThrow(new RuntimeException("Test catch clause in groupStatePersist"));

        boolean threwException = false;
        try {
            groupStateService.signalStartRequested(groupId, mock(User.class));
            groupStateService.groupStatePersist(mockGroupCommand);
        } catch (RuntimeException re){
            threwException = true;
        }
        assertTrue(threwException);
    }

    @Configuration
    static class CommonConfiguration {

        @Autowired
        public StateNotificationWorker stateNotificationWorker;

        @Bean(name = "groupStateMachine")
        public GroupStateMachine getGroupStateManagerTableImpl() {
            return Mockito.mock(GroupStateMachine.class);
        }

        @Bean
        public JvmPersistenceService getJvmPersistenceService() {
            return Mockito.mock(JvmPersistenceService.class);
        }

        @SuppressWarnings("unchecked")
        @Bean
        @Qualifier("jvmStatePersistenceService")
        public StatePersistenceService<Jvm, JvmState> getJvmStatePersistenceService() {
            return (StatePersistenceService<Jvm, JvmState>) Mockito.mock(StatePersistenceService.class);
        }

        @Bean(name = "groupPersistenceService")
        public GroupPersistenceService getGroupPersistenceService() {
            return Mockito.mock(GroupPersistenceService.class);
        }

        @Bean(name = "webServerDao")
        public WebServerDao getWebServerDao() {
            return Mockito.mock(WebServerDao.class);
        }

        @SuppressWarnings("unchecked")
        @Bean
        @Qualifier("webServerStateService")
        public StateService<WebServer, WebServerReachableState> getWebServerStateService() {
            return Mockito.mock(StateService.class);
        }

        @Bean(name = "stateNotificationService")
        public StateNotificationService getStateNotificationService() {
            return Mockito.mock(StateNotificationService.class);
        }

        @Bean(name = "groupStateService")
        public GroupStateService.API getGroupStateService() {
            return new GroupStateServiceImpl(
                    getGroupPersistenceService(),
                    getStateNotificationService(),
                    StateType.GROUP,
                    getGroupStateService(),
                    stateNotificationWorker
            );
        }
    }
}
