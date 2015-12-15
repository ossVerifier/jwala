package com.siemens.cto.aem.persistence.jpa.service.app.impl;

import com.siemens.cto.aem.common.request.app.CreateApplicationRequest;
import com.siemens.cto.aem.common.request.group.CreateGroupRequest;
import com.siemens.cto.aem.common.configuration.TestExecutionProfile;
import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.common.domain.model.audit.AuditEvent;
import com.siemens.cto.aem.common.domain.model.event.Event;
import com.siemens.cto.aem.common.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.user.User;
import com.siemens.cto.aem.persistence.configuration.TestJpaConfiguration;
import com.siemens.cto.aem.persistence.jpa.domain.JpaApplication;
import com.siemens.cto.aem.persistence.jpa.domain.JpaGroup;
import com.siemens.cto.aem.persistence.jpa.service.ApplicationCrudService;
import com.siemens.cto.aem.persistence.jpa.service.GroupCrudService;
import com.siemens.cto.aem.persistence.jpa.service.impl.GroupCrudServiceImpl;
import com.siemens.cto.aem.persistence.jpa.service.GroupJvmRelationshipService;
import com.siemens.cto.aem.persistence.jpa.service.impl.GroupJvmRelationshipServiceImpl;
import com.siemens.cto.aem.persistence.jpa.service.impl.ApplicationCrudServiceImpl;
import com.siemens.cto.aem.persistence.jpa.service.JvmCrudService;
import com.siemens.cto.aem.persistence.jpa.service.impl.JvmCrudServiceImpl;
import com.siemens.cto.aem.persistence.service.group.GroupPersistenceService;
import com.siemens.cto.aem.persistence.service.group.impl.JpaGroupPersistenceServiceImpl;
import com.siemens.cto.aem.persistence.service.jvm.JvmPersistenceService;
import com.siemens.cto.aem.persistence.service.jvm.impl.JpaJvmPersistenceServiceImpl;
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

import static org.junit.Assert.*;

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
        jpaGroup            = groupCrudService.createGroup(new Event<CreateGroupRequest>(new CreateGroupRequest(textGroup), AuditEvent.now(userObj)));
        expGroupId          = jpaGroup.id();
    }
    
    @After
    public void tearDown() {
        try { groupCrudService.removeGroup(expGroupId); } catch (Exception x) { LOGGER.trace("Test tearDown", x); }
    }
    
    @Test(expected = BadRequestException.class)
    public void testApplicationCrudServiceEEE() {
        CreateApplicationRequest cmd = new CreateApplicationRequest(expGroupId,  textName, textContext, true, true);
        Event<CreateApplicationRequest> anAppToCreate = new Event<>(cmd, AuditEvent.now(new User(aUser)));

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
        CreateApplicationRequest cmd = new CreateApplicationRequest(expGroupId,  textName, textContext, true, true);
        Event<CreateApplicationRequest> anAppToCreate = new Event<>(cmd, AuditEvent.now(new User(aUser)));

        JpaApplication created2 = null;
        JpaApplication created = applicationCrudService.createApplication(anAppToCreate, jpaGroup);  
        
        assertNotNull(created);

        try {
            CreateApplicationRequest cmd2 = new CreateApplicationRequest(expGroupId,  textName + "-another", textContext, true, true);
            Event<CreateApplicationRequest> anAppToCreate2 = new Event<>(cmd2, AuditEvent.now(new User(aUser)));
    
            created2 = applicationCrudService.createApplication(anAppToCreate2, jpaGroup);  
    
            assertNotNull(created2);
        } finally { 
            try { applicationCrudService.removeApplication(created.id()); } catch (Exception x) { LOGGER.trace("Test tearDown", x); }
            try { if(created2 != null) { applicationCrudService.removeApplication(created2.id()); } } catch (Exception x) { LOGGER.trace("Test tearDown", x); }
        }
    }
}
