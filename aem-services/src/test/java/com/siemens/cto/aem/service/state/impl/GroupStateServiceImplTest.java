package com.siemens.cto.aem.service.state.impl;

import java.util.HashSet;
import java.util.Set;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
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
import org.springframework.integration.handler.ServiceActivatingHandler;
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
import com.siemens.cto.aem.domain.model.temporary.User;
import com.siemens.cto.aem.domain.model.webserver.WebServer;
import com.siemens.cto.aem.domain.model.webserver.WebServerReachableState;
import com.siemens.cto.aem.persistence.dao.webserver.WebServerDao;
import com.siemens.cto.aem.persistence.service.group.GroupPersistenceService;
import com.siemens.cto.aem.persistence.service.jvm.JvmPersistenceService;
import com.siemens.cto.aem.persistence.service.jvm.JvmStatePersistenceService;
import com.siemens.cto.aem.service.group.GroupStateMachine;
import com.siemens.cto.aem.service.state.GroupStateService;
import com.siemens.cto.aem.service.state.StateNotificationGateway;
import com.siemens.cto.aem.service.state.StateService;

import static com.siemens.cto.aem.domain.model.id.Identifier.id;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


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
    TaskExecutor notificationExecutor;

    @Autowired
    ServiceActivatingHandler  groupStateServiceActivator;

    @Autowired
    GroupStateService.API   groupStateService;

    @Autowired
    GroupStateMachine   groupStateManagerTableImpl;

    int updateReceived = 0;

    @Test
    public void testSubscribeAndReceive() throws InterruptedException {
        stateUpdates.unsubscribe(groupStateServiceActivator);
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
        stateUpdates.unsubscribe(groupStateServiceActivator);
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
            for(int i = 0; i< 5; ++i) {
                stateNotificationGateway.jvmStateChanged(new CurrentState<>(id(0L, Jvm.class), JvmState.STARTED, DateTime.now(), StateType.JVM));
            }
            this.wait(250); // for the first one
        }

        Thread.sleep(100); // for the rest.

        assertEquals(5, updateReceived);
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

    }

    @SuppressWarnings({"unchecked", "rawtypes"}) // Mockito
    @Test
    public void testStateUpdatedJVM() {

        when(groupStateManagerTableImpl.getCurrentStateDetail()).thenReturn(new CurrentGroupState(group.getId(), GroupState.INITIALIZED, DateTime.now()));
        when(groupStateManagerTableImpl.getCurrentState()).thenReturn(GroupState.INITIALIZED);
        groupStateService.stateUpdateJvm(new CurrentState<>(id(1L, Jvm.class), JvmState.UNKNOWN, DateTime.now(), StateType.JVM));

        ArgumentCaptor<Event> command = ArgumentCaptor.forClass(Event.class);
        verify(groupPersistenceService).updateGroupStatus(command.capture());
        SetGroupStateCommand sgsc = (SetGroupStateCommand)(command.getValue().getCommand());
        assertEquals(GroupState.INITIALIZED, sgsc.getNewGroupState() );
        groupStateService.stateUpdateJvm(new CurrentState<>(id(1L, Jvm.class), JvmState.INITIALIZED, DateTime.now(), StateType.JVM));

        verify(groupPersistenceService, times(2)).updateGroupStatus(command.capture());
        sgsc = (SetGroupStateCommand)(command.getValue().getCommand());
        assertEquals(GroupState.INITIALIZED, sgsc.getNewGroupState() );

        updateJvmState(JvmState.START_REQUESTED);

        verify(groupPersistenceService, times(3)).updateGroupStatus(command.capture());
        sgsc = (SetGroupStateCommand)(command.getValue().getCommand());
        assertEquals(GroupState.INITIALIZED, sgsc.getNewGroupState() );

        when(groupStateManagerTableImpl.getCurrentState()).thenReturn(GroupState.STARTED);
        when(groupStateManagerTableImpl.getCurrentStateDetail()).thenReturn(new CurrentGroupState(group.getId(), GroupState.STARTED, DateTime.now()));

        updateJvmState(JvmState.STARTED);

        verify(groupPersistenceService, times(4)).updateGroupStatus(command.capture());
        sgsc = (SetGroupStateCommand)(command.getValue().getCommand());
        assertEquals(GroupState.STARTED, sgsc.getNewGroupState() );

        verify(groupStateManagerTableImpl, times(1)).jvmStarted(eq(jvm.getId()));

        group = new Group(group.getId(), group.getName(), jvms, GroupState.STARTED, DateTime.now() );
        when(groupPersistenceService.getGroup(eq(group.getId()))).thenReturn(group);
        updateJvmState(JvmState.STOP_REQUESTED);

        // stop requested while started does not change state, so times(4) still
        verify(groupPersistenceService, times(4)).updateGroupStatus(command.capture());

        when(groupStateManagerTableImpl.getCurrentState()).thenReturn(GroupState.STOPPED);
        when(groupStateManagerTableImpl.getCurrentStateDetail()).thenReturn(new CurrentGroupState(group.getId(), GroupState.STOPPED, DateTime
                .now()));
        updateJvmState(JvmState.STOPPED);

        verify(groupStateManagerTableImpl, times(1)).jvmStopped(eq(jvm.getId()));

        verify(groupPersistenceService, times(5)).updateGroupStatus(command.capture());
        sgsc = (SetGroupStateCommand)(command.getValue().getCommand());
        assertEquals(GroupState.STOPPED, sgsc.getNewGroupState() );

        group = new Group(group.getId(), group.getName(), jvms, GroupState.STOPPED, DateTime.now() );
        when(groupPersistenceService.getGroup(eq(group.getId()))).thenReturn(group);
        updateJvmState(JvmState.FAILED);

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

    private void updateJvmState(final JvmState aState) {
        groupStateService.stateUpdateJvm(new CurrentState<>(id(1L, Jvm.class), aState, DateTime.now(), StateType.JVM));
    }

    @Configuration
    @ImportResource("classpath*:META-INF/spring/integration-state.xml")
    static class CommonConfiguration {

        @Bean
        public GroupStateMachine getGroupStateManagerTableImpl() {
            return Mockito.mock(GroupStateMachine.class);
        }
        @Bean
        public JvmPersistenceService getJvmPersistenceService() {
            return Mockito.mock(JvmPersistenceService.class);
        }
        @Bean
        public JvmStatePersistenceService getJvmStatePersistenceService() {
            return Mockito.mock(JvmStatePersistenceService.class);
        }
        @Bean
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
    }
}
