package com.siemens.cto.aem.persistence.jpa.service.app.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.apache.commons.lang.RandomStringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.IfProfileValue;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import com.siemens.cto.aem.common.configuration.TestExecutionProfile;
import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.domain.model.app.CreateApplicationCommand;
import com.siemens.cto.aem.domain.model.audit.AuditEvent;
import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.aem.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.domain.model.group.CreateGroupCommand;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.temporary.User;
import com.siemens.cto.aem.persistence.configuration.TestJpaConfiguration;
import com.siemens.cto.aem.persistence.jpa.domain.JpaApplication;
import com.siemens.cto.aem.persistence.jpa.domain.JpaGroup;
import com.siemens.cto.aem.persistence.jpa.service.app.ApplicationCrudService;
import com.siemens.cto.aem.persistence.jpa.service.group.GroupCrudService;
import com.siemens.cto.aem.persistence.jpa.service.group.impl.GroupCrudServiceImpl;
import com.siemens.cto.aem.persistence.jpa.service.groupjvm.GroupJvmRelationshipService;
import com.siemens.cto.aem.persistence.jpa.service.groupjvm.impl.GroupJvmRelationshipServiceImpl;
import com.siemens.cto.aem.persistence.jpa.service.jvm.JvmCrudService;
import com.siemens.cto.aem.persistence.jpa.service.jvm.impl.JvmCrudServiceImpl;
import com.siemens.cto.aem.persistence.service.group.GroupPersistenceService;
import com.siemens.cto.aem.persistence.service.group.impl.JpaGroupPersistenceServiceImpl;
import com.siemens.cto.aem.persistence.service.jvm.JvmPersistenceService;
import com.siemens.cto.aem.persistence.service.jvm.impl.JpaJvmPersistenceServiceImpl;

@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
@EnableTransactionManagement
@IfProfileValue(name = TestExecutionProfile.RUN_TEST_TYPES, value = TestExecutionProfile.INTEGRATION)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class,
                      classes = {ApplicationCrudServiceImplTest.Config.class
                      })
public class ApplicationCrudServiceImplTest {

    private final static Logger LOGGER = LoggerFactory.getLogger(ApplicationCrudServiceImplTest.class); 

    @Configuration
    @Import(TestJpaConfiguration.class)
    static class Config {

        @Bean
        public GroupPersistenceService getGroupPersistenceService() {
            return new JpaGroupPersistenceServiceImpl(getGroupCrudService(),
                                                      getGroupJvmRelationshipService());
        }

        @Bean
        public JvmPersistenceService getJvmPersistenceService() {
            return new JpaJvmPersistenceServiceImpl(getJvmCrudService(),
                                                    getGroupJvmRelationshipService());
        }

        @Bean
        public GroupCrudService getGroupCrudService() {
            return new GroupCrudServiceImpl();
        }

        @Bean
        public ApplicationCrudService getApplicationCrudService() {
            return new ApplicationCrudServiceImpl();
        }

        @Bean
        public GroupJvmRelationshipService getGroupJvmRelationshipService() {
            return new GroupJvmRelationshipServiceImpl(getGroupCrudService(),
                                                       getJvmCrudService());
        }

        @Bean
        public JvmCrudService getJvmCrudService() {
            return new JvmCrudServiceImpl();
        }
    }
    
    @Autowired
    ApplicationCrudService applicationCrudService;

    @Autowired
    GroupCrudService groupCrudService;
    
    private String aUser;
    
    private String alphaLower = "abcdefghijklmnopqrstuvwxyz";
    private String alpha = alphaLower + alphaLower.toUpperCase();
    private String alphaNum = alpha + "0123456789,.-/_$ ";
    private String alphaUnsafe = alphaNum + "\\\t\r\n";
    
    private String textContext = "/" + RandomStringUtils.random(25,alphaUnsafe.toCharArray());
    private String textName    = RandomStringUtils.random(25,alphaUnsafe.toCharArray());
    private String textGroup   = RandomStringUtils.random(25,alphaUnsafe.toCharArray());


    private Identifier<Group> expGroupId;
    private JpaGroup jpaGroup;

    private User userObj;
    
    @Before
    public void setup() {
        aUser = "TestUserId";
        userObj             = new User(aUser);
        jpaGroup            = groupCrudService.createGroup(new Event<CreateGroupCommand>(new CreateGroupCommand(textGroup), AuditEvent.now(userObj)));        
        expGroupId          = jpaGroup.id();
    }
    
    @After
    public void tearDown() {
        try { groupCrudService.removeGroup(expGroupId); } catch (Exception x) { LOGGER.trace("Test tearDown", x); }
    }
    
    @Test(expected = BadRequestException.class)
    public void testApplicationCrudServiceEEE() {
        CreateApplicationCommand cmd = new CreateApplicationCommand(expGroupId,  textName, textContext);
        Event<CreateApplicationCommand> anAppToCreate = new Event<>(cmd, AuditEvent.now(new User(aUser)));

        JpaApplication created = applicationCrudService.createApplication(anAppToCreate, jpaGroup);  
        
        assertNotNull(created);
        
        try {
            JpaApplication duplicate = applicationCrudService.createApplication(anAppToCreate, jpaGroup);
            fail(duplicate.toString());
        } catch(BadRequestException e) {
            assertEquals(AemFaultType.DUPLICATE_APPLICATION, e.getMessageResponseStatus());
            throw e;
        } finally { 
            try { applicationCrudService.removeApplication(created.id()); } catch (Exception x) { LOGGER.trace("Test tearDown", x); }
        }
        
    }

    @Test
    public void testDuplicateContextsOk() {
        CreateApplicationCommand cmd = new CreateApplicationCommand(expGroupId,  textName, textContext);
        Event<CreateApplicationCommand> anAppToCreate = new Event<>(cmd, AuditEvent.now(new User(aUser)));

        JpaApplication created2 = null;
        JpaApplication created = applicationCrudService.createApplication(anAppToCreate, jpaGroup);  
        
        assertNotNull(created);

        try {
            CreateApplicationCommand cmd2 = new CreateApplicationCommand(expGroupId,  textName + "-another", textContext + "-another");
            Event<CreateApplicationCommand> anAppToCreate2 = new Event<>(cmd2, AuditEvent.now(new User(aUser)));
    
            created2 = applicationCrudService.createApplication(anAppToCreate2, jpaGroup);  
    
            assertNotNull(created2);
        } finally { 
            try { applicationCrudService.removeApplication(created.id()); } catch (Exception x) { LOGGER.trace("Test tearDown", x); }
            try { if(created2 != null) { applicationCrudService.removeApplication(created2.id()); } } catch (Exception x) { LOGGER.trace("Test tearDown", x); }
        }
    }
}
