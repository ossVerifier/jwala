package com.siemens.cto.aem.service.group.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
import com.siemens.cto.aem.domain.model.group.CreateGroupCommand;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.group.GroupState;
import com.siemens.cto.aem.domain.model.group.command.SetGroupStateCommand;
import com.siemens.cto.aem.domain.model.temporary.User;
import com.siemens.cto.aem.persistence.dao.app.ApplicationDao;
import com.siemens.cto.aem.persistence.dao.app.impl.jpa.JpaApplicationDaoImpl;
import com.siemens.cto.aem.persistence.dao.group.GroupDao;
import com.siemens.cto.aem.persistence.dao.group.impl.jpa.JpaGroupDaoImpl;
import com.siemens.cto.aem.persistence.jpa.service.group.GroupCrudService;
import com.siemens.cto.aem.persistence.jpa.service.group.impl.GroupCrudServiceImpl;
import com.siemens.cto.aem.persistence.jpa.service.groupjvm.GroupJvmRelationshipService;
import com.siemens.cto.aem.persistence.jpa.service.groupjvm.impl.GroupJvmRelationshipServiceImpl;
import com.siemens.cto.aem.persistence.jpa.service.jvm.JvmCrudService;
import com.siemens.cto.aem.persistence.jpa.service.jvm.JvmStateCrudService;
import com.siemens.cto.aem.persistence.jpa.service.jvm.impl.JvmCrudServiceImpl;
import com.siemens.cto.aem.persistence.jpa.service.jvm.impl.JvmStateCrudServiceImpl;
import com.siemens.cto.aem.persistence.service.group.GroupPersistenceService;
import com.siemens.cto.aem.persistence.service.group.impl.JpaGroupPersistenceServiceImpl;
import com.siemens.cto.aem.persistence.service.jvm.JvmStatePersistenceService;
import com.siemens.cto.aem.persistence.service.jvm.impl.JpaJvmStatePersistenceServiceImpl;
import com.siemens.cto.aem.service.configuration.TestJpaConfiguration;

@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = {
    GroupStateManagerTableImplTest.CommonConfiguration.class,
        TestJpaConfiguration.class })
@IfProfileValue(name = TestExecutionProfile.RUN_TEST_TYPES, value = TestExecutionProfile.INTEGRATION)
@RunWith(SpringJUnit4ClassRunner.class)
@EnableTransactionManagement
@Transactional
public class GroupStateManagerTableImplTest {

    @Configuration
    static class CommonConfiguration {

        @Bean
        public JvmCrudService getJvmCrudService() {
            return new JvmCrudServiceImpl();
        }

        @Bean
        public JvmStateCrudService getJvmStateCrudService() {
            return new JvmStateCrudServiceImpl();
        }

        @Bean
        public GroupCrudService getGroupCrudService() {
            return new GroupCrudServiceImpl();
        }
        

        @Bean
        public JvmStatePersistenceService getJvmPersistenceService() {
            return new JpaJvmStatePersistenceServiceImpl(getJvmStateCrudService());
        }
        
        @Bean
        public GroupJvmRelationshipService getGroupJvmRelationshipService() {
            return new GroupJvmRelationshipServiceImpl(getGroupCrudService(), getJvmCrudService());
        }
        
        @Bean
        public GroupPersistenceService getApplicationPersistenceService() {
            return new JpaGroupPersistenceServiceImpl(getGroupCrudService(), getGroupJvmRelationshipService());
        }
    
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
    
    @Test
    public void testStateInitialized() { 
        Group group = groupPersistenceService.createGroup(Event.create(new CreateGroupCommand("testGroup"), AuditEvent.now(new User("test"))));
        group = groupPersistenceService.updateGroupStatus(Event.create(new SetGroupStateCommand(group.getId(), GroupState.INITIALIZED), AuditEvent.now(new User("test"))));
        
        classUnderTest.initializeGroup(group);

        // an INITIALIZED group will quickly enter some other group based on database state. 
        // Since we have no  group content, we will remain in the INITIALIZED state. 
        assertEquals(GroupState.INITIALIZED, classUnderTest.getCurrentState());
        assertTrue(classUnderTest.canStart());
        assertTrue(classUnderTest.canStop());
        
        // not really part of this test!
        classUnderTest.signalReset();
    }
}