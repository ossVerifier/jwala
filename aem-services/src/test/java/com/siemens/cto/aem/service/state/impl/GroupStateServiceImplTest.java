package com.siemens.cto.aem.service.state.impl;

import static com.siemens.cto.aem.domain.model.id.Identifier.id;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.joda.time.DateTime;
import org.junit.Before;
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

import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.aem.domain.model.group.CurrentGroupState;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.group.GroupState;
import com.siemens.cto.aem.domain.model.group.LiteGroup;
import com.siemens.cto.aem.domain.model.group.command.SetGroupStateCommand;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.jvm.JvmState;
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
import com.siemens.cto.aem.service.state.GroupStateService;
import com.siemens.cto.aem.service.state.StateNotificationGateway;
import com.siemens.cto.aem.service.state.StateNotificationService;
import com.siemens.cto.aem.service.state.StateService;


@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext(classMode=ClassMode.AFTER_EACH_TEST_METHOD)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = { GroupStateServiceImplTest.CommonConfiguration.class })
public class GroupStateServiceImplTest {

    @Autowired
    @Qualifier("jvmStateUpdates")
    SubscribableChannel stateUpdates;

    @Autowired
    StateNotificationGateway stateNotificationGateway;

    @Autowired
    JvmPersistenceService jvmPersistenceService;

    @Autowired
    GroupPersistenceService groupPersistenceService;

    @Autowired
    TaskExecutor statePubSubBusExecutor;

    @Autowired
    GroupStateService.API   groupStateService;

    @Autowired
    GroupStateMachine   groupStateManagerTableImpl;

    int updateReceived = 0;

    @Test
    public void testSubscribeAndReceive() throws InterruptedException {
        stateUpdates.subscribe(new MessageHandler() {

            @Override
            public void handleMessage(Message<?> message) throws MessagingException {
                updateReceived = 1;
                synchronized(GroupStateServiceImplTest.this) {
                    GroupStateServiceImplTest.this.notify();
                }
            }
        });

        synchronized(this) {
            stateNotificationGateway.jvmStateChanged(new CurrentState<>(id(0L, Jvm.class), JvmState.STARTED, DateTime.now(), StateType.JVM));
            this.wait(5000);
        }

        assertTrue(updateReceived > 0);
    }

    @Test
    public void testSubscribeAndReceiveIsParallel() throws InterruptedException {
        stateUpdates.subscribe(new MessageHandler() {

            @Override
            public void handleMessage(Message<?> message) throws MessagingException {
                try {
                    Thread.sleep(100);
                    synchronized(GroupStateServiceImplTest.this) {
                        ++updateReceived;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                synchronized(GroupStateServiceImplTest.this) {
                    GroupStateServiceImplTest.this.notify();
                }
            }
        });

        // test
        synchronized(this) {
            for(int i = 0; i< 3; ++i) {
                stateNotificationGateway.jvmStateChanged(new CurrentState<>(id(0L, Jvm.class), JvmState.STARTED, DateTime.now(), StateType.JVM));
            }
            this.wait(250); // for the first one
        }

        Thread.sleep(100); // for the rest.

        assertEquals(3, updateReceived);
    }

    @Test
    public void testStateUpdatedJVMResiliency() {
        for(JvmState js : JvmState.values()) {
            groupStateService.stateUpdateJvm(new CurrentState<>(id(0L, Jvm.class), js, DateTime.now(), StateType.JVM));
        }
    }

    Group group;
    LiteGroup lgroup;
    Set<LiteGroup> lgroups = new HashSet<>();
    Jvm jvm;
    Set<Jvm> jvms= new HashSet<>();

    @SuppressWarnings("unchecked")
    @Before
    public void setupEntities() {

        group = new Group(id(1L, Group.class), "");
        lgroup = new LiteGroup(id(1L, Group.class), "");
        lgroups.add(lgroup);
        jvm = new Jvm(id(1L, Jvm.class), "", "", lgroups, 0,0,0,0,0);
        jvms.add(jvm);
        group = new Group(group.getId(),  group.getName(), jvms);

        when(jvmPersistenceService.getJvm(eq(jvm.getId()))).thenReturn(jvm);
        when(groupPersistenceService.getGroup(eq(group.getId()))).thenReturn(group);

        when(groupPersistenceService.updateState(Mockito.any(Event.class))).thenAnswer(new Answer<CurrentState<Group, GroupState>>() {

            @Override
            public CurrentState<Group, GroupState> answer(InvocationOnMock invocation) throws Throwable {

                Event<SetStateCommand<Group, GroupState>> event = (Event<SetStateCommand<Group, GroupState>>) invocation.getArguments()[0];
                group = new Group(group.getId(), group.getName(), group.getJvms(), event.getCommand().getNewState().getState(), DateTime.now());
                return group.getCurrentState();
            }

        });

    }

    @Test
    public void testStateUpdatedJVM() {

        List<SetGroupStateCommand> updates;
        SetGroupStateCommand sgsc;
        
        when(groupStateManagerTableImpl.getCurrentStateDetail()).thenReturn(new CurrentGroupState(group.getId(), GroupState.INITIALIZED, DateTime.now()));
        when(groupStateManagerTableImpl.getCurrentState()).thenReturn(GroupState.INITIALIZED);
        updates = groupStateService.stateUpdateJvm(new CurrentState<>(id(1L, Jvm.class), JvmState.UNKNOWN, DateTime.now(), StateType.JVM));

        sgsc = updates.get(0);
         
        assertEquals(GroupState.INITIALIZED, sgsc.getNewState().getState() );
        updates = groupStateService.stateUpdateJvm(new CurrentState<>(id(1L, Jvm.class), JvmState.INITIALIZED, DateTime.now(), StateType.JVM));

        sgsc = updates.get(0);
        assertEquals(GroupState.INITIALIZED, sgsc.getNewState().getState() );

        updates = updateJvmState(JvmState.START_REQUESTED);

        sgsc = updates.get(0);
        assertEquals(GroupState.INITIALIZED, sgsc.getNewState().getState() );

        when(groupStateManagerTableImpl.getCurrentState()).thenReturn(GroupState.STARTED);
        when(groupStateManagerTableImpl.getCurrentStateDetail()).thenReturn(new CurrentGroupState(group.getId(), GroupState.STARTED, DateTime.now()));

        updates = updateJvmState(JvmState.STARTED);

        sgsc = updates.get(0);
        assertEquals(GroupState.STARTED, sgsc.getNewState().getState() );

        verify(groupStateManagerTableImpl, times(1)).jvmStarted(eq(jvm.getId()));

        group = new Group(group.getId(), group.getName(), jvms, GroupState.STARTED, DateTime.now() );
        when(groupPersistenceService.getGroup(eq(group.getId()))).thenReturn(group);
        updates = updateJvmState(JvmState.STOP_REQUESTED);

        when(groupStateManagerTableImpl.getCurrentState()).thenReturn(GroupState.STOPPED);
        when(groupStateManagerTableImpl.getCurrentStateDetail()).thenReturn(new CurrentGroupState(group.getId(), GroupState.STOPPED, DateTime
                .now()));
        updates = updateJvmState(JvmState.STOPPED);

        verify(groupStateManagerTableImpl, times(1)).jvmStopped(eq(jvm.getId()));

        sgsc = updates.get(0);
        assertEquals(GroupState.STOPPED, sgsc.getNewState().getState() );

        group = new Group(group.getId(), group.getName(), jvms, GroupState.STOPPED, DateTime.now() );
        when(groupPersistenceService.getGroup(eq(group.getId()))).thenReturn(group);
        updates = updateJvmState(JvmState.FAILED);

        verify(groupStateManagerTableImpl, times(1)).jvmError(eq(jvm.getId()));
    }

    @Test
    public void testWebServerStateUnfinished() {
        groupStateService.stateUpdateWebServer(new CurrentState<>(id(0L, WebServer.class), WebServerReachableState.REACHABLE, DateTime
                .now(), StateType.WEB_SERVER));
    }

    @Test
    public void testSignals() {
        groupStateService.signalReset(id(1L, Group.class), User.getSystemUser());
        verify(groupStateManagerTableImpl, times(1)).signalReset(eq(User.getSystemUser()));
        groupStateService.signalStartRequested(id(1L, Group.class), User.getSystemUser());
        verify(groupStateManagerTableImpl, times(1)).signalStartRequested(eq(User.getSystemUser()));
        groupStateService.signalStopRequested(id(1L, Group.class), User.getSystemUser());
        verify(groupStateManagerTableImpl, times(1)).signalStopRequested(eq(User.getSystemUser()));
    }

    @Test
    public void testQuery() {
        groupStateService.canStart(id(1L, Group.class), User.getSystemUser());
        verify(groupPersistenceService).getGroup(eq(group.getId()));
        verify(groupStateManagerTableImpl, times(1)).canStart();
        verify(groupStateManagerTableImpl, times(1)).initializeGroup(group, User.getSystemUser());

        groupStateService.canStop(id(1L, Group.class), User.getSystemUser());
        verify(groupStateManagerTableImpl, times(1)).canStop();
    }

    private List<SetGroupStateCommand> updateJvmState(final JvmState aState) {
        return groupStateService.stateUpdateJvm(new CurrentState<>(id(1L, Jvm.class), aState, DateTime.now(), StateType.JVM));
    }

    @Configuration
    @ImportResource("classpath*:META-INF/spring/integration-state.xml")
    static class CommonConfiguration {

        @Autowired
        StateNotificationGateway stateNotification;

        @Bean
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
            return (StatePersistenceService<Jvm, JvmState>)Mockito.mock(StatePersistenceService.class);
        }

        @Bean(name = "groupPersistenceService")
        public GroupPersistenceService getGroupPersistenceService() {
            return Mockito.mock(GroupPersistenceService.class);
        }
        @Bean
        public WebServerDao getWebServerDao() {
            return Mockito.mock(WebServerDao.class);
        }

        @SuppressWarnings("unchecked")
        @Bean
        public StateService<WebServer, WebServerReachableState>    getWebServerStateService() {
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
                    stateNotification
                    );
        }
    }
}
