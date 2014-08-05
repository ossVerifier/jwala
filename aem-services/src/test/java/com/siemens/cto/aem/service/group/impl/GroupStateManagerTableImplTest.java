package com.siemens.cto.aem.service.group.impl;

import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.IfProfileValue;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import com.siemens.cto.aem.common.configuration.TestExecutionProfile;
import com.siemens.cto.aem.domain.model.audit.AuditEvent;
import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.aem.domain.model.group.AddJvmToGroupCommand;
import com.siemens.cto.aem.domain.model.group.CreateGroupCommand;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.group.GroupState;
import com.siemens.cto.aem.domain.model.group.command.SetGroupStateCommand;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.jvm.JvmState;
import com.siemens.cto.aem.domain.model.jvm.command.CreateJvmCommand;
import com.siemens.cto.aem.domain.model.state.CurrentState;
import com.siemens.cto.aem.domain.model.state.StateType;
import com.siemens.cto.aem.domain.model.state.command.JvmSetStateCommand;
import com.siemens.cto.aem.domain.model.state.command.SetStateCommand;
import com.siemens.cto.aem.domain.model.temporary.User;
import com.siemens.cto.aem.domain.model.webserver.WebServer;
import com.siemens.cto.aem.domain.model.webserver.WebServerReachableState;
import com.siemens.cto.aem.persistence.configuration.AemPersistenceServiceConfiguration;
import com.siemens.cto.aem.persistence.dao.webserver.WebServerDao;
import com.siemens.cto.aem.persistence.service.group.GroupPersistenceService;
import com.siemens.cto.aem.persistence.service.jvm.JvmPersistenceService;
import com.siemens.cto.aem.persistence.service.state.StatePersistenceService;
import com.siemens.cto.aem.service.configuration.TestJpaConfiguration;
import com.siemens.cto.aem.service.group.GroupStateMachine;
import com.siemens.cto.aem.service.state.StateService;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = {
    GroupStateManagerTableImplTest.CommonConfiguration.class,
        TestJpaConfiguration.class,
        AemPersistenceServiceConfiguration.class})
@IfProfileValue(name = TestExecutionProfile.RUN_TEST_TYPES, value = TestExecutionProfile.INTEGRATION)
@RunWith(SpringJUnit4ClassRunner.class)
@EnableTransactionManagement
@Transactional
public class GroupStateManagerTableImplTest {

    @Configuration
    static class CommonConfiguration {

        @Bean
        public GroupStateMachine getClassUnderTest() {
            return new GroupStateManagerTableImpl();
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
    User testUser = new User("test");

    @Test
    public void testStateInitialized() {
        Group group = groupPersistenceService.createGroup(Event.create(new CreateGroupCommand("testGroup"), AuditEvent.now(testUser)));
        group = groupPersistenceService.updateGroupStatus(Event.create(new SetGroupStateCommand(group.getId(), GroupState.INITIALIZED), AuditEvent.now(testUser)));

        classUnderTest.initializeGroup(group, testUser);

        // an INITIALIZED group will quickly enter some other group based on database state.
        // Since we have no  group content, we will remain in the INITIALIZED state.
        assertEquals(GroupState.INITIALIZED, classUnderTest.getCurrentState());
        assertTrue(classUnderTest.canStart());
        assertTrue(classUnderTest.canStop());

        // not really part of this test!
        classUnderTest.signalReset(testUser);
    }

    @Test
    public void testOneStoppedJvmNewGroup() {
        Group group = groupPersistenceService.createGroup(Event.create(new CreateGroupCommand("testGroup"), AuditEvent.now(testUser)));
        group = groupPersistenceService.updateGroupStatus(Event.create(new SetGroupStateCommand(group.getId(), GroupState.INITIALIZED), AuditEvent.now(testUser)));
        Jvm jvm = jvmPersistenceService.createJvm(Event.create(new CreateJvmCommand("test", "test", 80, 443, 443, 8005, 8009), AuditEvent.now(testUser)));
        groupPersistenceService.addJvmToGroup(Event.create(new AddJvmToGroupCommand(group.getId(), jvm.getId()),  AuditEvent.now(testUser)));
        jvmStatePersistenceService.updateState(Event.create(createJvmSetStateCommand(jvm, JvmState.STOPPED), AuditEvent.now(testUser)));

        classUnderTest.initializeGroup(group, testUser);

        // an INITIALIZED group will quickly enter some other group based on database state.
        // As we have one Jvm with the stopped state, we should be in the STOPPED group.
        assertEquals(GroupState.STOPPED, classUnderTest.getCurrentState());
        assertTrue(classUnderTest.canStart());
        assertFalse(classUnderTest.canStop());

        groupUsedInTest = group;
        jvmUsedInTest = jvm;
    }

    @Test
    public void testResetFromError() {

        Group group = groupPersistenceService.createGroup(Event.create(new CreateGroupCommand("testGroupInError"), AuditEvent.now(testUser)));
        group = groupPersistenceService.updateGroupStatus(Event.create(new SetGroupStateCommand(group.getId(), GroupState.ERROR), AuditEvent.now(testUser)));

        classUnderTest.initializeGroup(group, testUser);

        // should return here since there is no content.
        assertEquals(GroupState.ERROR, classUnderTest.getCurrentState());

        classUnderTest.signalReset(testUser);

        // should return here since there is no content.
        assertEquals(GroupState.INITIALIZED, classUnderTest.getCurrentState());
        groupUsedInTest = group;
    }

    @Test
    public void testIncomingJvmStartMessage() {

        testOneStoppedJvmNewGroup();

        Jvm jvm = jvmUsedInTest;
        jvmStatePersistenceService.updateState(Event.create(createJvmSetStateCommand(jvm, JvmState.STARTED), AuditEvent.now(testUser)));

        assertEquals(GroupState.STOPPED, classUnderTest.getCurrentState());

        assertTrue(classUnderTest.canStart());
        assertFalse(classUnderTest.canStop());

        classUnderTest.jvmStarted(jvm.getId());

        assertEquals(GroupState.STARTED, classUnderTest.getCurrentState());

        assertFalse(classUnderTest.canStart());
        assertTrue(classUnderTest.canStop());
    }

    @Test
    public void testIncomingJvmStopMessage() {

        testIncomingJvmStartMessage();

        Jvm jvm = jvmUsedInTest;
        jvmStatePersistenceService.updateState(Event.create(createJvmSetStateCommand(jvm, JvmState.STOPPED), AuditEvent.now(testUser)));

        assertFalse(classUnderTest.canStart());
        assertTrue(classUnderTest.canStop());

        assertEquals(GroupState.STARTED, classUnderTest.getCurrentState());

        classUnderTest.jvmStopped(jvm.getId());

        assertEquals(GroupState.STOPPED, classUnderTest.getCurrentState());

        assertTrue(classUnderTest.canStart());
        assertFalse(classUnderTest.canStop());

    }


    @Test
    public void testThreeJvmFullLifecycle() {
        Group group = groupPersistenceService.createGroup(Event.create(new CreateGroupCommand("testGroup"), AuditEvent.now(testUser)));
        group = groupPersistenceService.updateGroupStatus(Event.create(new SetGroupStateCommand(group.getId(), GroupState.INITIALIZED), AuditEvent.now(testUser)));
        Jvm jvm = jvmPersistenceService.createJvm(Event.create(new CreateJvmCommand("test", "test", 80, 443, 443, 8005, 8009), AuditEvent.now(testUser)));
        Jvm jvm2 = jvmPersistenceService.createJvm(Event.create(new CreateJvmCommand("test2", "test", 80, 443, 443, 8005, 8009), AuditEvent.now(testUser)));
        Jvm jvm3 = jvmPersistenceService.createJvm(Event.create(new CreateJvmCommand("test3", "test", 80, 443, 443, 8005, 8009), AuditEvent.now(testUser)));
        groupPersistenceService.addJvmToGroup(Event.create(new AddJvmToGroupCommand(group.getId(), jvm.getId()),  AuditEvent.now(testUser)));
        groupPersistenceService.addJvmToGroup(Event.create(new AddJvmToGroupCommand(group.getId(), jvm2.getId()),  AuditEvent.now(testUser)));
        groupPersistenceService.addJvmToGroup(Event.create(new AddJvmToGroupCommand(group.getId(), jvm3.getId()),  AuditEvent.now(testUser)));
        jvmStatePersistenceService.updateState(Event.create(createJvmSetStateCommand(jvm, JvmState.STOPPED), AuditEvent.now(testUser)));
        jvmStatePersistenceService.updateState(Event.create(createJvmSetStateCommand(jvm2, JvmState.STARTED), AuditEvent.now(testUser)));
        jvmStatePersistenceService.updateState(Event.create(createJvmSetStateCommand(jvm3, JvmState.STOPPED), AuditEvent.now(testUser)));

        classUnderTest.initializeGroup(group, testUser);

        // an INITIALIZED group will quickly enter some other group based on database state.
        // As we have 2 of 3 Jvms with the stopped state, we should be in the PARTIAL state.
        assertEquals(GroupState.PARTIAL, classUnderTest.getCurrentState());

        classUnderTest.signalStopRequested(testUser);
        // received a request to Stop the group
        assertEquals(GroupState.STOPPING, classUnderTest.getCurrentState());

        jvmStatePersistenceService.updateState(Event.create(createJvmSetStateCommand(jvm2, JvmState.STOP_REQUESTED), AuditEvent.now(testUser)));

        classUnderTest.jvmStopped(jvm.getId());
        // receive a stop event for an already stopped jvm, stay in STOPPING because of jvm2
        assertEquals(GroupState.STOPPING, classUnderTest.getCurrentState());

        jvmStatePersistenceService.updateState(Event.create(createJvmSetStateCommand(jvm2, JvmState.STOPPED), AuditEvent.now(testUser)));
        classUnderTest.jvmStopped(jvm2.getId());
        // received the final stop, go to STOPPED
        assertEquals(GroupState.STOPPED, classUnderTest.getCurrentState());

        classUnderTest.signalStartRequested(testUser);
        // start requested by user,
        assertEquals(GroupState.STARTING, classUnderTest.getCurrentState());

        jvmStatePersistenceService.updateState(Event.create(createJvmSetStateCommand(jvm, JvmState.START_REQUESTED), AuditEvent.now(testUser)));
        jvmStatePersistenceService.updateState(Event.create(createJvmSetStateCommand(jvm3, JvmState.START_REQUESTED), AuditEvent.now(testUser)));
        jvmStatePersistenceService.updateState(Event.create(createJvmSetStateCommand(jvm2, JvmState.START_REQUESTED), AuditEvent.now(testUser)));
        // this call should never happen, but should be ok
        classUnderTest.jvmStarted(jvm.getId());
        classUnderTest.jvmStarted(jvm2.getId());
        classUnderTest.jvmStarted(jvm3.getId());
        // received a start request for a jvm as a set of triggers
        assertEquals(GroupState.STARTING, classUnderTest.getCurrentState());

        jvmStatePersistenceService.updateState(Event.create(createJvmSetStateCommand(jvm, JvmState.STARTED), AuditEvent.now(testUser)));
        classUnderTest.jvmStarted(jvm2.getId());
        // received a start 1/3
        assertEquals(GroupState.STARTING, classUnderTest.getCurrentState());

        jvmStatePersistenceService.updateState(Event.create(createJvmSetStateCommand(jvm2, JvmState.STARTED), AuditEvent.now(testUser)));
        classUnderTest.jvmStarted(jvm2.getId());
        // received a start 2/3
        assertEquals(GroupState.STARTING, classUnderTest.getCurrentState());

        classUnderTest.jvmStarted(jvm2.getId());
        // received a start 2/3 - duplicate stay in STARTING
        assertEquals(GroupState.STARTING, classUnderTest.getCurrentState());

        jvmStatePersistenceService.updateState(Event.create(createJvmSetStateCommand(jvm3, JvmState.STARTED), AuditEvent.now(testUser)));
        classUnderTest.jvmStarted(jvm3.getId());
        // received the final Start, go to STARTED
        assertEquals(GroupState.STARTED, classUnderTest.getCurrentState());

        groupUsedInTest = group;
        jvmUsedInTest = jvm;
    }

    private SetStateCommand<Jvm, JvmState> createJvmSetStateCommand(final Jvm jvm,
                                                                    final JvmState aState) {
        return new JvmSetStateCommand(new CurrentState<>(jvm.getId(), aState, DateTime.now(), StateType.JVM));
    }
}