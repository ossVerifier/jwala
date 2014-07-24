package com.siemens.cto.aem.service.group.impl;

import static com.siemens.cto.aem.domain.model.id.Identifier.id;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
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
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import com.siemens.cto.aem.domain.model.audit.AuditEvent;
import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.aem.domain.model.group.AddJvmToGroupCommand;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.group.GroupState;
import com.siemens.cto.aem.domain.model.group.LiteGroup;
import com.siemens.cto.aem.domain.model.group.command.SetGroupStateCommand;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.jvm.JvmState;
import com.siemens.cto.aem.domain.model.jvm.command.SetJvmStateCommand;
import com.siemens.cto.aem.domain.model.state.CurrentState;
import com.siemens.cto.aem.domain.model.state.StateType;
import com.siemens.cto.aem.domain.model.temporary.PaginationParameter;
import com.siemens.cto.aem.domain.model.temporary.User;
import com.siemens.cto.aem.domain.model.webserver.WebServer;
import com.siemens.cto.aem.domain.model.webserver.WebServerReachableState;
import com.siemens.cto.aem.persistence.dao.webserver.WebServerDao;
import com.siemens.cto.aem.persistence.service.group.GroupPersistenceService;
import com.siemens.cto.aem.persistence.service.jvm.JvmPersistenceService;
import com.siemens.cto.aem.persistence.service.state.StatePersistenceService;
import com.siemens.cto.aem.service.group.GroupStateMachine;
import com.siemens.cto.aem.service.state.StateService;

@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = {
    MoreGroupStateMachineTest.CommonConfiguration.class})
@RunWith(SpringJUnit4ClassRunner.class)
public class MoreGroupStateMachineTest {

    @Configuration
    static class CommonConfiguration {
        
        @Bean 
        public GroupStateMachine getClassUnderTest() {
            return new GroupStateManagerTableImpl();
        }

        @Bean
        public JvmPersistenceService getJvmPersistenceService() {
            return Mockito.mock(JvmPersistenceService.class);
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
    WebServerDao webServerDao;
    
    @Autowired
    @Qualifier("jvmStatePersistenceService")
    StatePersistenceService<Jvm, JvmState> jvmStatePersistenceService;
    
    @Autowired
    StateService<WebServer, WebServerReachableState> webServerStateService;
    
    User testUser = new User("test");;
    
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
        jvm = new Jvm(id(1L, Jvm.class), "", "", lgroups, 0,0,0,0,0);
        jvms.add(jvm);
        ws = new WebServer(id(1L, WebServer.class), groups, "ws-1", "localhost", 80,  443);
        wsList.add(ws);
        mockGroup = new Group(mockGroup.getId(),  mockGroup.getName(), jvms, GroupState.INITIALIZED, DateTime.now());
        wsReachableSet.add(new CurrentState<WebServer, WebServerReachableState>(ws.getId(), WebServerReachableState.REACHABLE, DateTime.now(), StateType.WEB_SERVER));
        
        when(webServerDao.getWebServer(eq(ws.getId()))).thenReturn(ws);
        
        when(webServerDao.findWebServersBelongingTo(eq(mockGroup.getId()), eq(PaginationParameter.all()))).thenReturn(wsList);
        
        when(jvmPersistenceService.getJvm(eq(jvm.getId()))).thenReturn(jvm);
        
        when(groupPersistenceService.getGroup(eq(mockGroup.getId()))).thenAnswer(new Answer<Group>() {

            @Override
            public Group answer(InvocationOnMock invocation) throws Throwable {
                return mockGroup;
            }
            
        });

        groupPersistenceService.addJvmToGroup(Event.create(new AddJvmToGroupCommand(mockGroup.getId(), jvm.getId()),  AuditEvent.now(testUser)));        
        
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
                Event<SetJvmStateCommand> event = (Event<SetJvmStateCommand>) invocation.getArguments()[0];
                currentJvmState = new CurrentState<Jvm, JvmState>(jvm.getId(), event.getCommand().getNewJvmState().getJvmState(), DateTime.now(), StateType.JVM);
                return currentJvmState;
           }

        });
        
        when(groupPersistenceService.updateGroupStatus(Mockito.any(Event.class))).thenAnswer(new Answer<Group>() {

            @Override
            public Group answer(InvocationOnMock invocation) throws Throwable {
                
                Event<SetGroupStateCommand> event = (Event<SetGroupStateCommand>) invocation.getArguments()[0];
                mockGroup = new Group(mockGroup.getId(), mockGroup.getName(), mockGroup.getJvms(), event.getCommand().getNewState().getState(), DateTime.now());
                return mockGroup;
            }
            
        });

    }
        
    @Test
    public void testWebServerTriggers() { 

        setWsState(WebServerReachableState.UNREACHABLE);
        setJvmState(JvmState.STOPPED);
        classUnderTest.initializeGroup(mockGroup, testUser);
        assertEquals(GroupState.STOPPED, classUnderTest.getCurrentState());

        setWsState(WebServerReachableState.REACHABLE);
        setJvmState(JvmState.STOPPED);
        classUnderTest.wsUnreachable(ws.getId());
        assertEquals(GroupState.PARTIAL, classUnderTest.getCurrentState());
        
        setWsState(WebServerReachableState.UNREACHABLE);
        setJvmState(JvmState.STOPPED);
        classUnderTest.wsReachable(ws.getId());
        assertEquals(GroupState.STOPPED, classUnderTest.getCurrentState());

        setWsState(WebServerReachableState.UNKNOWN);
        setJvmState(JvmState.FAILED);
        classUnderTest.wsError(ws.getId());
        assertEquals(GroupState.ERROR, classUnderTest.getCurrentState());

    }

    private void setWsState(WebServerReachableState reachable) {
        wsReachableSet.clear();
        wsReachableSet.add(new CurrentState<WebServer, WebServerReachableState>(ws.getId(), reachable, DateTime.now(), StateType.WEB_SERVER));
    }

    private void setJvmState(JvmState state) {
        currentJvmState = new CurrentState<Jvm, JvmState>(jvm.getId(), state, DateTime.now(), StateType.JVM);
    }

}