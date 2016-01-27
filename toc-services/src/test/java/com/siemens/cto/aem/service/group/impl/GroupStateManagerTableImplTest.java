package com.siemens.cto.aem.service.group.impl;

import com.siemens.cto.aem.common.configuration.TestExecutionProfile;
import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.common.domain.model.group.GroupState;
import com.siemens.cto.aem.common.domain.model.jvm.Jvm;
import com.siemens.cto.aem.common.domain.model.jvm.JvmState;
import com.siemens.cto.aem.common.domain.model.path.Path;
import com.siemens.cto.aem.common.domain.model.state.CurrentState;
import com.siemens.cto.aem.common.domain.model.state.StateType;
import com.siemens.cto.aem.common.domain.model.user.User;
import com.siemens.cto.aem.common.domain.model.webserver.WebServer;
import com.siemens.cto.aem.common.domain.model.webserver.WebServerReachableState;
import com.siemens.cto.aem.common.request.group.AddJvmToGroupRequest;
import com.siemens.cto.aem.common.request.group.CreateGroupRequest;
import com.siemens.cto.aem.common.request.group.SetGroupStateRequest;
import com.siemens.cto.aem.common.request.jvm.CreateJvmRequest;
import com.siemens.cto.aem.common.request.state.JvmSetStateRequest;
import com.siemens.cto.aem.common.request.state.SetStateRequest;
import com.siemens.cto.aem.persistence.configuration.AemPersistenceServiceConfiguration;
import com.siemens.cto.aem.persistence.jpa.service.WebServerCrudService;
import com.siemens.cto.aem.persistence.service.GroupPersistenceService;
import com.siemens.cto.aem.persistence.service.JvmPersistenceService;
import com.siemens.cto.aem.persistence.service.StatePersistenceService;
import com.siemens.cto.aem.service.configuration.TestJpaConfiguration;
import com.siemens.cto.aem.service.group.GroupStateMachine;
import com.siemens.cto.aem.service.state.StateService;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.test.annotation.IfProfileValue;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.*;

@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = {
    GroupStateManagerTableImplTest.CommonConfiguration.class,
        TestJpaConfiguration.class,
        AemPersistenceServiceConfiguration.class})
@IfProfileValue(name = TestExecutionProfile.RUN_TEST_TYPES, value = TestExecutionProfile.INTEGRATION)
@RunWith(SpringJUnit4ClassRunner.class)
@EnableTransactionManagement
@Transactional
@Ignore
// TODO:
public class GroupStateManagerTableImplTest {

    @Configuration
    static class CommonConfiguration {

        @Bean
        @Scope((ConfigurableBeanFactory.SCOPE_PROTOTYPE))
        public GroupStateMachine getClassUnderTest() {
            return new GroupStateManagerTableImpl();
        }

        @Bean
        public WebServerCrudService getWebServerDao() {
            return Mockito.mock(WebServerCrudService.class);
        }

        @SuppressWarnings("unchecked")
        @Bean
        @Qualifier("webServerStateService")
        public StateService<WebServer, WebServerReachableState>    getWebServerStateService() {
            return Mockito.mock(StateService.class);
        }

    }

    @Autowired
    GroupPersistenceService groupPersistenceService;

    @Autowired
    GroupStateMachine classUnderTest;

    @Autowired
    JvmPersistenceService jvmPersistenceService;

    @Autowired
    @Qualifier("jvmStatePersistenceService")
    StatePersistenceService<Jvm, JvmState> jvmStatePersistenceService;

    Group groupUsedInTest = null; // set during testing for test reuse;
    Jvm jvmUsedInTest = null;  // set during testing for test reuse;
    User user;

    @Before
    public void setUp() throws Exception {
        user = new User("testUser");
        user.addToThread();
    }

    @After
    public void tearDown() {
        User.getThreadLocalUser().invalidate();
    }

    @Test
    public void testStateInitialized() {
        Group group = groupPersistenceService.createGroup(new CreateGroupRequest("testGroup"));
        group = groupPersistenceService.updateGroupStatus(new SetGroupStateRequest(group.getId(), GroupState.GRP_INITIALIZED));

        classUnderTest.synchronizedInitializeGroup(group, user);

        // an JVM_UNKNOWN group will quickly enter some other group based on database state.
        // Since we have no  group content, we will remain in the JVM_UNKNOWN state.
        assertEquals(GroupState.GRP_UNKNOWN, classUnderTest.getCurrentState());
        assertTrue(classUnderTest.canStart());
        assertTrue(classUnderTest.canStop());

    }

    @Test
    @Ignore
    // TODO: Fix this once the JVM and web server's new state monitoring mechanism is implemented.
    public void testOneStoppedJvmNewGroup() {
        Group group = groupPersistenceService.createGroup(new CreateGroupRequest("testGroup"));
        group = groupPersistenceService.updateGroupStatus(new SetGroupStateRequest(group.getId(), GroupState.GRP_INITIALIZED));
        Jvm jvm = jvmPersistenceService.createJvm(new CreateJvmRequest("test", "test", 80, 443, 443, 8005,
                8009, new Path("/hct"), "EXAMPLE_OPTS=%someEnv%/someVal"));
        group = groupPersistenceService.addJvmToGroup(new AddJvmToGroupRequest(group.getId(), jvm.getId()));
        jvmStatePersistenceService.updateState(createJvmSetStateRequest(jvm, JvmState.SVC_STOPPED));

        classUnderTest.synchronizedInitializeGroup(group, user);

        // an JVM_INITIALIZED group will quickly enter some other group based on database state.
        // As we have one Jvm with the stopped state, we should be in the JVM_STOPPED group.
        assertEquals(GroupState.GRP_STOPPED, classUnderTest.getCurrentState());
        assertTrue(classUnderTest.canStart());
        assertFalse(classUnderTest.canStop());

        groupUsedInTest = group;
        jvmUsedInTest = jvm;
    }

//    @Test
//    public void testResetFromError() {
//
//        Group group = groupPersistenceService.createGroup(new CreateGroupRequest("testGroup"));
//        group = groupPersistenceService.updateGroupStatus(new SetGroupStateRequest(group.getId(), GroupState.GRP_INITIALIZED));
//        Jvm jvm = jvmPersistenceService.createJvm(new CreateJvmRequest("test", "test", 80, 443, 443, 8005,
//                8009, new Path("/hct"), "EXAMPLE_OPTS=%someEnv%/someVal"));
//        group = groupPersistenceService.addJvmToGroup(new AddJvmToGroupRequest(group.getId(), jvm.getId()));
//        jvmStatePersistenceService.updateState(createJvmSetStateRequest(jvm, JvmState.JVM_FAILED));
//
//        classUnderTest.synchronizedInitializeGroup(group, user);
//
//        assertEquals(GroupState.GRP_FAILURE, classUnderTest.getCurrentState());
//
//        jvmStatePersistenceService.updateState(createJvmSetStateRequest(jvm, JvmState.JVM_STARTED));
//
//        classUnderTest.signalReset(user); // really doesn't do anything except refresh state now.
//
//        // should return here since there is no content.
//        assertEquals(GroupState.GRP_STARTED, classUnderTest.getCurrentState());
//        groupUsedInTest = group;
//    }

//    @Test
//    public void testIncomingJvmStartMessage() {
//
//        testOneStoppedJvmNewGroup();
//
//        Jvm jvm = jvmUsedInTest;
//        jvmStatePersistenceService.updateState(createJvmSetStateRequest(jvm, JvmState.JVM_STARTED));
//
//        assertEquals(GroupState.GRP_STOPPED, classUnderTest.getCurrentState());
//
//        assertTrue(classUnderTest.canStart());
//        assertFalse(classUnderTest.canStop());
//
//        classUnderTest.jvmStarted(jvm.getId());
//        classUnderTest.refreshState();
//
//        assertEquals(GroupState.GRP_STARTED, classUnderTest.getCurrentState());
//
//        assertFalse(classUnderTest.canStart());
//        assertTrue(classUnderTest.canStop());
//    }

//    @Test
//    public void testIncomingJvmStopMessage() {
//
//        testIncomingJvmStartMessage();
//
//        Jvm jvm = jvmUsedInTest;
//        jvmStatePersistenceService.updateState(createJvmSetStateRequest(jvm, JvmState.SVC_STOPPED));
//
//        assertFalse(classUnderTest.canStart());
//        assertTrue(classUnderTest.canStop());
//
//        assertEquals(GroupState.GRP_STARTED, classUnderTest.getCurrentState());
//
//        classUnderTest.jvmStopped(jvm.getId());
//        classUnderTest.refreshState();
//
//        assertEquals(GroupState.GRP_STOPPED, classUnderTest.getCurrentState());
//
//        assertTrue(classUnderTest.canStart());
//        assertFalse(classUnderTest.canStop());
//
//    }


//    @Test
//    public void testThreeJvmFullLifecycle() {
//        Group group = groupPersistenceService.createGroup(new CreateGroupRequest("testGroup"));
//        group = groupPersistenceService.updateGroupStatus(new SetGroupStateRequest(group.getId(), GroupState.GRP_INITIALIZED));
//        Jvm jvm = jvmPersistenceService.createJvm(new CreateJvmRequest("test", "test", 80, 443, 443, 8005,
//                8009, new Path("/hct"), "EXAMPLE_OPTS=%someEnv%/someVal"));
//        Jvm jvm2 = jvmPersistenceService.createJvm(new CreateJvmRequest("test2", "test", 80, 443, 443, 8005,
//                8009, new Path("/hct"), "EXAMPLE_OPTS=%someEnv%/someVal"));
//        Jvm jvm3 = jvmPersistenceService.createJvm(new CreateJvmRequest("test3", "test", 80, 443, 443, 8005,
//                8009, new Path("/hct"), "EXAMPLE_OPTS=%someEnv%/someVal"));
//        group = groupPersistenceService.addJvmToGroup(new AddJvmToGroupRequest(group.getId(), jvm.getId()));
//        group = groupPersistenceService.addJvmToGroup(new AddJvmToGroupRequest(group.getId(), jvm2.getId()));
//        group = groupPersistenceService.addJvmToGroup(new AddJvmToGroupRequest(group.getId(), jvm3.getId()));
//        jvmStatePersistenceService.updateState(createJvmSetStateRequest(jvm, JvmState.SVC_STOPPED));
//        jvmStatePersistenceService.updateState(createJvmSetStateRequest(jvm2, JvmState.JVM_STARTED));
//        jvmStatePersistenceService.updateState(createJvmSetStateRequest(jvm3, JvmState.SVC_STOPPED));
//
//        classUnderTest.synchronizedInitializeGroup(group, user);
//
//        // an JVM_INITIALIZED group will quickly enter some other group based on database state.
//        // As we have 2 of 3 Jvms with the stopped state, we should be in the GRP_PARTIAL state.
//        assertEquals(GroupState.GRP_PARTIAL, classUnderTest.getCurrentState());
//
//        classUnderTest.signalStopRequested(user);
//        // received a request to Stop the group
//        assertEquals(GroupState.GRP_STOPPING, classUnderTest.getCurrentState());
//
//        jvmStatePersistenceService.updateState(createJvmSetStateRequest(jvm2, JvmState.JVM_STOPPING));
//
//        classUnderTest.jvmStopped(jvm.getId());
//        classUnderTest.refreshState();
//        // receive a stop event for an already stopped jvm, we transit to JVM_STOPPING anyway (per re-design to node/edge 8/19, and update 8/20)
//        assertEquals(GroupState.GRP_STOPPING, classUnderTest.getCurrentState());
//
//        jvmStatePersistenceService.updateState(createJvmSetStateRequest(jvm2, JvmState.SVC_STOPPED));
//        classUnderTest.jvmStopped(jvm2.getId());
//        classUnderTest.refreshState();
//        // received the final stop, go to JVM_STOPPED
//        assertEquals(GroupState.GRP_STOPPED, classUnderTest.getCurrentState());
//
//        classUnderTest.signalStartRequested(user);
//        assertEquals(GroupState.GRP_STARTING, classUnderTest.getCurrentState());
//        // start requested by user, but the call to refreshState will reset this value (per re-design to node/edge 8/19, and update 8/20)
//        classUnderTest.refreshState();
//        assertEquals(GroupState.GRP_STOPPED, classUnderTest.getCurrentState());
//
//        jvmStatePersistenceService.updateState(createJvmSetStateRequest(jvm, JvmState.JVM_STARTING));
//        jvmStatePersistenceService.updateState(createJvmSetStateRequest(jvm3, JvmState.JVM_STARTING));
//        jvmStatePersistenceService.updateState(createJvmSetStateRequest(jvm2, JvmState.JVM_STARTING));
//        // this call should never happen, but should be ok
//        classUnderTest.jvmStarted(jvm.getId());
//        classUnderTest.jvmStarted(jvm2.getId());
//        classUnderTest.jvmStarted(jvm3.getId());
//        classUnderTest.refreshState();
//        // received a start request for a jvm as a set of triggers
//        assertEquals(GroupState.GRP_STARTING, classUnderTest.getCurrentState());
//
//        jvmStatePersistenceService.updateState(createJvmSetStateRequest(jvm, JvmState.JVM_STARTED));
//        classUnderTest.jvmStarted(jvm2.getId());
//        classUnderTest.refreshState();
//        // received a start 1/3 (per re-design to node/edge 8/19 and update 8/20)
//        assertEquals(GroupState.GRP_STARTING, classUnderTest.getCurrentState());
//
//        jvmStatePersistenceService.updateState(createJvmSetStateRequest(jvm2, JvmState.JVM_STARTED));
//        classUnderTest.jvmStarted(jvm2.getId());
//        classUnderTest.refreshState();
//        // received a start 2/3 (per re-design to node/edge 8/19, and update 8/20)
//        assertEquals(GroupState.GRP_STARTING, classUnderTest.getCurrentState());
//
//        classUnderTest.jvmStarted(jvm2.getId());
//        classUnderTest.refreshState();
//        // received a start 2/3 - duplicate stay in JVM_STARTED (per re-design to node/edge 8/19, and update 8/20)
//        assertEquals(GroupState.GRP_STARTING, classUnderTest.getCurrentState());
//
//        jvmStatePersistenceService.updateState(createJvmSetStateRequest(jvm3, JvmState.JVM_STARTED));
//        classUnderTest.jvmStarted(jvm3.getId());
//        classUnderTest.refreshState();
//        // received the final Start, go to JVM_STARTED
//        assertEquals(GroupState.GRP_STARTED, classUnderTest.getCurrentState());
//
//        groupUsedInTest = group;
//        jvmUsedInTest = jvm;
//    }

    private SetStateRequest<Jvm, JvmState> createJvmSetStateRequest(final Jvm jvm,
                                                                    final JvmState aState) {
        return new JvmSetStateRequest(new CurrentState<>(jvm.getId(), aState, DateTime.now(), StateType.JVM));
    }
}