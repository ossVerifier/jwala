package com.siemens.cto.aem.service.group.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
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
import com.siemens.cto.aem.domain.model.jvm.CurrentJvmState;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.jvm.JvmState;
import com.siemens.cto.aem.domain.model.jvm.command.CreateJvmCommand;
import com.siemens.cto.aem.domain.model.jvm.command.SetJvmStateCommand;
import com.siemens.cto.aem.domain.model.temporary.User;
import com.siemens.cto.aem.persistence.configuration.AemPersistenceServiceConfiguration;
import com.siemens.cto.aem.persistence.dao.app.ApplicationDao;
import com.siemens.cto.aem.persistence.dao.app.impl.jpa.JpaApplicationDaoImpl;
import com.siemens.cto.aem.persistence.dao.group.GroupDao;
import com.siemens.cto.aem.persistence.dao.group.impl.jpa.JpaGroupDaoImpl;
import com.siemens.cto.aem.persistence.service.group.GroupPersistenceService;
import com.siemens.cto.aem.persistence.service.jvm.JvmPersistenceService;
import com.siemens.cto.aem.persistence.service.jvm.JvmStatePersistenceService;
import com.siemens.cto.aem.service.configuration.TestJpaConfiguration;

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
        public ApplicationDao getApplicationDao() {
            return new JpaApplicationDaoImpl();
        }

        @Bean
        public GroupDao getGroupDao() {
            return new JpaGroupDaoImpl();
        }
        
        @Bean 
        public GroupStateManagerTableImpl getClassUnderTest() {
            return new GroupStateManagerTableImpl();
        }
    }   
    
    @Autowired
    GroupPersistenceService groupPersistenceService;

    @Autowired
    GroupStateManagerTableImpl classUnderTest;
    
    @Autowired
    JvmPersistenceService jvmPersistenceService;
    
    @Autowired
    JvmStatePersistenceService jvmStatePersistenceService;
    
    Group groupUsedInTest = null; // set during testing for test reuse;
    Jvm jvmUsedInTest = null;  // set during testing for test reuse;
    User testUser = new User("test");;

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
        jvmStatePersistenceService.updateJvmState(Event.create(new SetJvmStateCommand(new CurrentJvmState(jvm.getId(), JvmState.STOPPED, DateTime.now())),  AuditEvent.now(testUser)));
        
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
    public void testGSMIsModifyingGroupState() { 
        
        testOneStoppedJvmNewGroup();
        
        Group group = classUnderTest.getCurrentGroup();
        
        assertEquals(GroupState.STOPPED, group.getState());
    }

    @Test
    public void testIncomingJvmStartMessage() { 
        
        testOneStoppedJvmNewGroup();
        
        Jvm jvm = jvmUsedInTest;        
        jvmStatePersistenceService.updateJvmState(Event.create(new SetJvmStateCommand(new CurrentJvmState(jvm.getId(), JvmState.STARTED, DateTime.now())),  AuditEvent.now(testUser)));

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
        jvmStatePersistenceService.updateJvmState(Event.create(new SetJvmStateCommand(new CurrentJvmState(jvm.getId(), JvmState.STOPPED, DateTime.now())),  AuditEvent.now(testUser)));

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
        jvmStatePersistenceService.updateJvmState(Event.create(new SetJvmStateCommand(new CurrentJvmState(jvm.getId(), JvmState.STOPPED, DateTime.now())),  AuditEvent.now(testUser)));
        jvmStatePersistenceService.updateJvmState(Event.create(new SetJvmStateCommand(new CurrentJvmState(jvm2.getId(), JvmState.STARTED, DateTime.now())),  AuditEvent.now(testUser)));
        jvmStatePersistenceService.updateJvmState(Event.create(new SetJvmStateCommand(new CurrentJvmState(jvm3.getId(), JvmState.STOPPED, DateTime.now())),  AuditEvent.now(testUser)));
        
        classUnderTest.initializeGroup(group, testUser);

        // an INITIALIZED group will quickly enter some other group based on database state. 
        // As we have 2 of 3 Jvms with the stopped state, we should be in the PARTIAL state. 
        assertEquals(GroupState.PARTIAL, classUnderTest.getCurrentState());

        classUnderTest.signalStopRequested(testUser);
        // received a request to Stop the group
        assertEquals(GroupState.STOPPING, classUnderTest.getCurrentState());

        classUnderTest.jvmStopped(jvm.getId());
        // receive a stop event for an already stopped jvm, stay in STOPPING
        assertEquals(GroupState.STOPPING, classUnderTest.getCurrentState());

        jvmStatePersistenceService.updateJvmState(Event.create(new SetJvmStateCommand(new CurrentJvmState(jvm2.getId(), JvmState.STOPPED, DateTime.now())),  AuditEvent.now(testUser)));
        classUnderTest.jvmStopped(jvm2.getId());
        // received the final stop, go to STOPPED
        assertEquals(GroupState.STOPPED, classUnderTest.getCurrentState());

        classUnderTest.signalStartRequested(testUser);
        // start requested by user, 
        assertEquals(GroupState.STARTING, classUnderTest.getCurrentState());

        jvmStatePersistenceService.updateJvmState(Event.create(new SetJvmStateCommand(new CurrentJvmState(jvm.getId(), JvmState.START_REQUESTED, DateTime.now())),  AuditEvent.now(testUser)));
        jvmStatePersistenceService.updateJvmState(Event.create(new SetJvmStateCommand(new CurrentJvmState(jvm3.getId(), JvmState.START_REQUESTED, DateTime.now())),  AuditEvent.now(testUser)));
        jvmStatePersistenceService.updateJvmState(Event.create(new SetJvmStateCommand(new CurrentJvmState(jvm2.getId(), JvmState.START_REQUESTED, DateTime.now())),  AuditEvent.now(testUser)));
        // this call should never happen, but should be ok 
        classUnderTest.jvmStarted(jvm.getId());
        classUnderTest.jvmStarted(jvm2.getId());
        classUnderTest.jvmStarted(jvm3.getId());
        // received a start request for a jvm as a set of triggers
        assertEquals(GroupState.STARTING, classUnderTest.getCurrentState());

        jvmStatePersistenceService.updateJvmState(Event.create(new SetJvmStateCommand(new CurrentJvmState(jvm.getId(), JvmState.STARTED, DateTime.now())),  AuditEvent.now(testUser)));
        classUnderTest.jvmStarted(jvm2.getId());
        // received a start 1/3
        assertEquals(GroupState.STARTING, classUnderTest.getCurrentState());

        jvmStatePersistenceService.updateJvmState(Event.create(new SetJvmStateCommand(new CurrentJvmState(jvm2.getId(), JvmState.STARTED, DateTime.now())),  AuditEvent.now(testUser)));
        classUnderTest.jvmStarted(jvm2.getId());
        // received a start 2/3
        assertEquals(GroupState.STARTING, classUnderTest.getCurrentState());

        classUnderTest.jvmStarted(jvm2.getId());
        // received a start 2/3 - duplicate stay in STARTING
        assertEquals(GroupState.STARTING, classUnderTest.getCurrentState());

        jvmStatePersistenceService.updateJvmState(Event.create(new SetJvmStateCommand(new CurrentJvmState(jvm3.getId(), JvmState.STARTED, DateTime.now())),  AuditEvent.now(testUser)));
        classUnderTest.jvmStarted(jvm3.getId());
        // received the final Start, go to STARTED
        assertEquals(GroupState.STARTED, classUnderTest.getCurrentState());

        groupUsedInTest = group;
        jvmUsedInTest = jvm;
    }

}