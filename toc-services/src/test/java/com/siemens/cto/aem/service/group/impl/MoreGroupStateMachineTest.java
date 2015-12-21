package com.siemens.cto.aem.service.group.impl;

import com.siemens.cto.aem.common.request.group.AddJvmToGroupRequest;
import com.siemens.cto.aem.common.request.state.JvmSetStateRequest;
import com.siemens.cto.aem.common.domain.model.audit.AuditEvent;
import com.siemens.cto.aem.common.domain.model.event.Event;
import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.common.domain.model.group.GroupState;
import com.siemens.cto.aem.common.domain.model.group.LiteGroup;
import com.siemens.cto.aem.common.request.group.SetGroupStateRequest;
import com.siemens.cto.aem.common.domain.model.jvm.Jvm;
import com.siemens.cto.aem.common.domain.model.jvm.JvmState;
import com.siemens.cto.aem.common.domain.model.path.FileSystemPath;
import com.siemens.cto.aem.common.domain.model.path.Path;
import com.siemens.cto.aem.common.domain.model.state.CurrentState;
import com.siemens.cto.aem.common.domain.model.state.StateType;
import com.siemens.cto.aem.common.domain.model.user.User;
import com.siemens.cto.aem.common.domain.model.webserver.WebServer;
import com.siemens.cto.aem.common.domain.model.webserver.WebServerReachableState;
import com.siemens.cto.aem.persistence.jpa.service.GroupCrudService;
import com.siemens.cto.aem.persistence.jpa.service.WebServerCrudService;
import com.siemens.cto.aem.persistence.service.GroupPersistenceService;
import com.siemens.cto.aem.persistence.service.JvmPersistenceService;
import com.siemens.cto.aem.persistence.service.StatePersistenceService;
import com.siemens.cto.aem.persistence.service.WebServerPersistenceService;
import com.siemens.cto.aem.persistence.service.impl.WebServerPersistenceServiceImpl;
import com.siemens.cto.aem.service.group.GroupStateMachine;
import com.siemens.cto.aem.service.state.StateService;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.siemens.cto.aem.common.domain.model.id.Identifier.id;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = {
    MoreGroupStateMachineTest.CommonConfiguration.class})
@RunWith(SpringJUnit4ClassRunner.class)
public class MoreGroupStateMachineTest {

    @Configuration
    static class CommonConfiguration {

        @Bean
        @Scope((ConfigurableBeanFactory.SCOPE_PROTOTYPE))
        public GroupStateMachine getClassUnderTest() {
            return new GroupStateManagerTableImpl();
        }

        @Bean
        public JvmPersistenceService getJvmPersistenceService() {
            return Mockito.mock(JvmPersistenceService.class);
        }

        @Bean
        public WebServerPersistenceService getWebServerPersistenceService() {
            return new WebServerPersistenceServiceImpl(getGroupCrudService(), getWebServerCrudService());
        }

        @Bean
        public GroupPersistenceService getGroupPersistenceService() {
            return Mockito.mock(GroupPersistenceService.class);
        }

        @Bean
        public GroupCrudService getGroupCrudService() {
            return Mockito.mock(GroupCrudService.class);
        }

        @Bean
        public WebServerCrudService getWebServerCrudService() {
            return Mockito.mock(WebServerCrudService.class);
        }

        @SuppressWarnings("unchecked")
        @Bean
        @Qualifier("webServerStateService")
        public StateService<WebServer, WebServerReachableState>    getWebServerStateService() {
            return Mockito.mock(StateService.class);
        }

        @SuppressWarnings("unchecked")
        @Bean
        @Qualifier("jvmStatePersistenceService")
        public StatePersistenceService<Jvm, JvmState> getJvmStatePersistenceService() {
            return (StatePersistenceService<Jvm, JvmState>)Mockito.mock(StatePersistenceService.class);
        }
    }

    @Autowired
    GroupPersistenceService groupPersistenceService;

    @Autowired
    GroupStateMachine classUnderTest;

    @Autowired
    JvmPersistenceService jvmPersistenceService;

    @Autowired
    WebServerPersistenceService webServerPersistenceService;

    @Autowired
    @Qualifier("jvmStatePersistenceService")
    StatePersistenceService<Jvm, JvmState> jvmStatePersistenceService;

    @Autowired
    StateService<WebServer, WebServerReachableState> webServerStateService;

    User testUser = new User("test");

    Group mockGroup;
    LiteGroup lgroup;
    Set<LiteGroup> lgroups = new HashSet<>();
    Set<Group> groups = new HashSet<>();
    Jvm jvm;
    WebServer ws;
    List<WebServer> wsList = new ArrayList<>();
    Set<Jvm> jvms= new HashSet<>();
    CurrentState<Jvm, JvmState> currentJvmState;
    Set<CurrentState<WebServer, WebServerReachableState>> wsReachableSet = new HashSet<>();

    @SuppressWarnings("unchecked")
    @Before
    public void setupEntities() {

        mockGroup = new Group(id(1L, Group.class), "");
        lgroup = new LiteGroup(id(1L, Group.class), "");
        lgroups.add(lgroup);
        groups.add(mockGroup);
        jvm = new Jvm(id(1L, Jvm.class), "", "", lgroups, 0, 0, 0, 0, 0, new Path("/abc"),
                "EXAMPLE_OPTS=%someEnv%/someVal");
        jvms.add(jvm);
        ws = new WebServer(id(1L, WebServer.class),
                           groups, "ws-1",
                           "localhost",
                           80,
                           443,
                           new Path("/statusPath"),
                           new FileSystemPath("d:/some-dir/httpd.conf"),
                           new Path("./"),
                           new Path("htdocs"));
        wsList.add(ws);
        mockGroup = new Group(mockGroup.getId(),  mockGroup.getName(), jvms, GroupState.GRP_INITIALIZED, DateTime.now());
        wsReachableSet.add(new CurrentState(ws.getId(), WebServerReachableState.WS_REACHABLE, DateTime.now(), StateType.WEB_SERVER));

        when(webServerPersistenceService.getWebServer(eq(ws.getId()))).thenReturn(ws);

        when(webServerPersistenceService.findWebServersBelongingTo(eq(mockGroup.getId()))).thenReturn(wsList);

        when(jvmPersistenceService.getJvm(eq(jvm.getId()))).thenReturn(jvm);

        when(groupPersistenceService.getGroup(eq(mockGroup.getId()))).thenAnswer(new Answer<Group>() {

            @Override
            public Group answer(InvocationOnMock invocation) throws Throwable {
                return mockGroup;
            }

        });

        groupPersistenceService.addJvmToGroup(Event.create(new AddJvmToGroupRequest(mockGroup.getId(), jvm.getId()),  AuditEvent.now(testUser)));

        when(jvmStatePersistenceService.getState(eq(jvm.getId()))).thenAnswer(new Answer<CurrentState<Jvm, JvmState>>() {

            @Override
            public CurrentState<Jvm, JvmState> answer(InvocationOnMock invocation) throws Throwable {
                return currentJvmState;
            }

        });

        when(webServerStateService.getCurrentStates(Mockito.anySet())).thenAnswer(new Answer<Set<CurrentState<WebServer, WebServerReachableState>>>() {

            @Override
            public Set<CurrentState<WebServer, WebServerReachableState>> answer(InvocationOnMock invocation)
                    throws Throwable {
                return wsReachableSet;
            }
        });

        when(jvmStatePersistenceService.updateState(Mockito.any(Event.class))).thenAnswer(new Answer<CurrentState<Jvm, JvmState>>() {

            @Override
            public CurrentState<Jvm, JvmState> answer(InvocationOnMock invocation) throws Throwable {
                Event<JvmSetStateRequest> event = (Event<JvmSetStateRequest>) invocation.getArguments()[0];
                currentJvmState = new CurrentState<>(jvm.getId(), event.getRequest().getNewState().getState(), DateTime.now(), StateType.JVM);
                return currentJvmState;
           }

        });

        when(groupPersistenceService.updateGroupStatus(Mockito.any(Event.class))).thenAnswer(new Answer<Group>() {

            @Override
            public Group answer(InvocationOnMock invocation) throws Throwable {

                Event<SetGroupStateRequest> event = (Event<SetGroupStateRequest>) invocation.getArguments()[0];
                mockGroup = new Group(mockGroup.getId(), mockGroup.getName(), mockGroup.getJvms(), event.getRequest().getNewState().getState(), DateTime.now());
                return mockGroup;
            }

        });

    }

    @Test
    public void testWebServerTriggers() {

        setWsState(WebServerReachableState.WS_UNREACHABLE);
        setJvmState(JvmState.SVC_STOPPED);
        classUnderTest.synchronizedInitializeGroup(mockGroup, testUser);
        assertEquals(GroupState.GRP_STOPPED, classUnderTest.getCurrentState());

        setWsState(WebServerReachableState.WS_REACHABLE);
        setJvmState(JvmState.SVC_STOPPED);
        classUnderTest.wsUnreachable(ws.getId());
        classUnderTest.refreshState();
        assertEquals(GroupState.GRP_PARTIAL, classUnderTest.getCurrentState());

        setWsState(WebServerReachableState.WS_UNREACHABLE);
        setJvmState(JvmState.SVC_STOPPED);
        classUnderTest.wsReachable(ws.getId());
        classUnderTest.refreshState();
        assertEquals(GroupState.GRP_STOPPED, classUnderTest.getCurrentState());

        setWsState(WebServerReachableState.WS_UNKNOWN);
        setJvmState(JvmState.JVM_FAILED);
        classUnderTest.wsError(ws.getId());
        classUnderTest.refreshState();
        assertEquals(GroupState.GRP_FAILURE, classUnderTest.getCurrentState());

    }

    private void setWsState(WebServerReachableState reachable) {
        wsReachableSet.clear();
        wsReachableSet.add(new CurrentState<>(ws.getId(), reachable, DateTime.now(), StateType.WEB_SERVER));
    }

    private void setJvmState(JvmState state) {
        currentJvmState = new CurrentState<>(jvm.getId(), state, DateTime.now(), StateType.JVM);
    }

}