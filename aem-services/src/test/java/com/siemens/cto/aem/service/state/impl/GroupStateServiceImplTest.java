package com.siemens.cto.aem.service.state.impl;

import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.group.GroupState;
import com.siemens.cto.aem.domain.model.group.LiteGroup;
import com.siemens.cto.aem.domain.model.id.Identifier;
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
import com.siemens.cto.aem.persistence.service.state.StatePersistenceService;
import com.siemens.cto.aem.service.group.GroupStateMachine;
import com.siemens.cto.aem.service.state.GroupStateService;
import com.siemens.cto.aem.service.state.StateNotificationService;
import com.siemens.cto.aem.service.state.StateNotificationWorker;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationContext;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * JUnit test for {@link GroupStateServiceImpl}
 *
 * Created by JC043760 on 11/20/2015.
 */
public class GroupStateServiceImplTest {

    @Mock
    private StatePersistenceService<Group, GroupState> persistenceService;

    @Mock
    private StateNotificationService stateNotificationService;

    @Mock
    private GroupStateService.API groupStateServiceApi;

    @Mock
    private StateNotificationWorker stateNotificationWorker;

    @Mock
    private GroupPersistenceService groupPersistenceService;

    @Mock
    private JvmPersistenceService jvmPersistenceService;

    @Mock
    private WebServerDao webServerDao;

    @Mock
    private ApplicationContext applicationContext;

    @Mock
    private GroupStateMachine groupStateMachine;

    private GroupStateServiceImpl groupStateServiceImpl;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        groupStateServiceImpl = new GroupStateServiceImpl(persistenceService,
                                                          stateNotificationService,
                                                          StateType.GROUP,
                                                          groupStateServiceApi,
                                                          stateNotificationWorker,
                                                          groupPersistenceService,
                                                          jvmPersistenceService,
                                                          webServerDao);
    }

    @Test
    public void testStateUpdateJvmForNullJvm() throws InterruptedException {
        final Identifier<Jvm> id = new Identifier<>(1l);
        CurrentState<Jvm, JvmState> cjs = mock(CurrentState.class);
        when(cjs.getId()).thenReturn(id);
        when(jvmPersistenceService.getJvm(eq(id))).thenReturn(null);
        assertEquals(0, groupStateServiceImpl.stateUpdateJvm(cjs).size());
    }

    @Test
    public void testStateUpdateJvmWithGroupsNull() throws InterruptedException {
        final Identifier<Jvm> id = new Identifier<>(1l);
        CurrentState<Jvm, JvmState> cjs = mock(CurrentState.class);
        when(cjs.getId()).thenReturn(id);
        final Jvm jvm = mock(Jvm.class);
        when(jvm.getGroups()).thenReturn(null);
        when(jvmPersistenceService.getJvm(eq(id))).thenReturn(jvm);
        assertEquals(0, groupStateServiceImpl.stateUpdateJvm(cjs).size());
    }

    @Test
    public void testStateUpdateJvm() throws InterruptedException {
        final Identifier<Jvm> id = new Identifier<>(1l);
        CurrentState<Jvm, JvmState> cjs = mock(CurrentState.class);
        when(cjs.getId()).thenReturn(id);
        final Jvm jvm = mock(Jvm.class);
        final LiteGroup liteGroup = new LiteGroup(new Identifier<Group>(1l), "theGroup");
        final Set<LiteGroup> liteGroups = new HashSet<>();
        liteGroups.add(liteGroup);
        when(jvm.getGroups()).thenReturn(liteGroups);
        when(jvmPersistenceService.getJvm(eq(id))).thenReturn(jvm);
        final Group group = new Group(liteGroup.getId(), "theFullGroup");
        when(groupPersistenceService.getGroup(eq(liteGroup.getId()))).thenReturn(group);
        when(applicationContext.getBean(eq("groupStateMachine"), eq(GroupStateMachine.class))).thenReturn(groupStateMachine);
        groupStateServiceImpl.setApplicationContext(applicationContext);
        assertNotNull(groupStateServiceImpl.stateUpdateJvm(cjs));
    }

    @Test(expected = RuntimeException.class)
    public void testStateUpdateJvmWithRuntimeEx() throws InterruptedException {
        final Identifier<Jvm> id = new Identifier<>(1l);
        CurrentState<Jvm, JvmState> cjs = mock(CurrentState.class);
        when(cjs.getId()).thenReturn(id);
        final Jvm jvm = mock(Jvm.class);
        final LiteGroup liteGroup = new LiteGroup(new Identifier<Group>(1l), "theGroup");
        final Set<LiteGroup> liteGroups = new HashSet<>();
        liteGroups.add(liteGroup);
        when(jvm.getGroups()).thenReturn(liteGroups);
        when(jvmPersistenceService.getJvm(eq(id))).thenReturn(jvm);
        final Group group = new Group(liteGroup.getId(), "theFullGroup");
        when(groupPersistenceService.getGroup(eq(liteGroup.getId()))).thenThrow(RuntimeException.class);
        when(applicationContext.getBean(eq("groupStateMachine"), eq(GroupStateMachine.class))).thenReturn(groupStateMachine);
        groupStateServiceImpl.stateUpdateJvm(cjs);
    }

    @Test
    public void testStateUpdateWebServer() throws InterruptedException {
        final Identifier<WebServer> id = new Identifier<>(1l);
        CurrentState<WebServer, WebServerReachableState> cjs = mock(CurrentState.class);
        when(cjs.getId()).thenReturn(id);
        final WebServer webServer = mock(WebServer.class);
        final Group group = new Group(new Identifier<Group>(1l), "theGroup");
        final Set<Group> groups = new HashSet<>();
        groups.add(group);
        when(webServer.getGroups()).thenReturn(groups);
        when(webServerDao.getWebServer(eq(id))).thenReturn(webServer);
        when(applicationContext.getBean(eq("groupStateMachine"), eq(GroupStateMachine.class))).thenReturn(groupStateMachine);
        groupStateServiceImpl.setApplicationContext(applicationContext);
        assertNotNull(groupStateServiceImpl.stateUpdateWebServer(cjs));
    }

    @Test
    public void testStateUpdateRequest() throws InterruptedException {
        final Group group = new Group(new Identifier<Group>(1l), "theGroup");
        when(applicationContext.getBean(eq("groupStateMachine"), eq(GroupStateMachine.class))).thenReturn(groupStateMachine);
        groupStateServiceImpl.setApplicationContext(applicationContext);
        assertNotNull(groupStateServiceImpl.stateUpdateRequest(group));
        verify(groupStateMachine).synchronizedInitializeGroup(group, User.getSystemUser());
    }

    @Test
    public void testSignalReset() {
        when(applicationContext.getBean(eq("groupStateMachine"), eq(GroupStateMachine.class))).thenReturn(groupStateMachine);
        groupStateServiceImpl.setApplicationContext(applicationContext);
        groupStateServiceImpl.signalReset(new Identifier<Group>(1l), User.getSystemUser());
        verify(groupStateMachine).signalReset(User.getSystemUser());
    }

    @Test
    public void testSignalStopRequested() {
        when(applicationContext.getBean(eq("groupStateMachine"), eq(GroupStateMachine.class))).thenReturn(groupStateMachine);
        final Identifier<Group> groupId = new Identifier<>(1l);
        final Group group = new Group(groupId, "theGroup");
        when(groupPersistenceService.getGroup(eq(groupId))).thenReturn(group);
        groupStateServiceImpl.setApplicationContext(applicationContext);
        groupStateServiceImpl.signalStopRequested(groupId, User.getSystemUser());
        verify(groupPersistenceService).getGroup(groupId);
        verify(groupStateMachine).synchronizedInitializeGroup(group, User.getSystemUser());
        verify(groupStateMachine).signalStopRequested(User.getSystemUser());
    }

    @Test
    public void testSignalStartRequested() {
        when(applicationContext.getBean(eq("groupStateMachine"), eq(GroupStateMachine.class))).thenReturn(groupStateMachine);
        final Identifier<Group> groupId = new Identifier<>(1l);
        final Group group = new Group(groupId, "theGroup");
        when(groupPersistenceService.getGroup(eq(groupId))).thenReturn(group);
        groupStateServiceImpl.setApplicationContext(applicationContext);
        groupStateServiceImpl.signalStartRequested(groupId, User.getSystemUser());
        verify(groupPersistenceService).getGroup(groupId);
        verify(groupStateMachine).synchronizedInitializeGroup(group, User.getSystemUser());
        verify(groupStateMachine).signalStartRequested(User.getSystemUser());
    }

    @Test
    public void testCanStart() {
        when(applicationContext.getBean(eq("groupStateMachine"), eq(GroupStateMachine.class))).thenReturn(groupStateMachine);
        final Identifier<Group> groupId = new Identifier<>(1l);
        final Group group = new Group(groupId, "theGroup");
        when(groupPersistenceService.getGroup(eq(groupId))).thenReturn(group);
        groupStateServiceImpl.setApplicationContext(applicationContext);
        groupStateServiceImpl.canStart(groupId, User.getSystemUser());
        verify(groupPersistenceService).getGroup(groupId);
        verify(groupStateMachine).synchronizedInitializeGroup(group, User.getSystemUser());
    }

    @Test
    public void testCanStop() {
        when(applicationContext.getBean(eq("groupStateMachine"), eq(GroupStateMachine.class))).thenReturn(groupStateMachine);
        final Identifier<Group> groupId = new Identifier<>(1l);
        final Group group = new Group(groupId, "theGroup");
        when(groupPersistenceService.getGroup(eq(groupId))).thenReturn(group);
        groupStateServiceImpl.setApplicationContext(applicationContext);
        groupStateServiceImpl.canStop(groupId, User.getSystemUser());
        verify(groupPersistenceService).getGroup(groupId);
        verify(groupStateMachine).synchronizedInitializeGroup(group, User.getSystemUser());
    }

    @Test
    public void testCreateUnknown() {
        
    }

    @Test
    public void testSignal() {

    }

    @Test
    public void testGroupStatePersist() {

    }

    @Test
    public void testGroupStateNotify() {

    }

    @Test
    public void testGroupStateUnlock() {

    }

    @Test
    public void testSendNotification() {

    }

    @Test
    public void testSetApplicationContext() {

    }

    @Test
    public void testCheckForStoppedStates() {

    }
}