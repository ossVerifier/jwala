package com.siemens.cto.aem.persistence.service.app;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.apache.commons.lang.RandomStringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.domain.model.app.Application;
import com.siemens.cto.aem.domain.model.app.CreateApplicationCommand;
import com.siemens.cto.aem.domain.model.app.UpdateApplicationCommand;
import com.siemens.cto.aem.domain.model.audit.AuditEvent;
import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.aem.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.domain.model.group.CreateGroupCommand;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.temporary.User;
import com.siemens.cto.aem.persistence.service.group.GroupPersistenceService;

@Transactional
public abstract class AbstractApplicationPersistenceServiceTest {

    @Autowired
    private ApplicationPersistenceService applicationPersistenceService;

    @Autowired
    private GroupPersistenceService groupPersistenceService;

    private String aUser;
    
    private String alphaLower = "abcdefghijklmnopqrstuvwxyz";
    private String alpha = alphaLower + alphaLower.toUpperCase();
    private String alphaNum = alpha + "0123456789,.-/_$ ";
    private String alphaUnsafe = alphaNum + "\\\t\r\n";
    
    private String textContext = "/" + RandomStringUtils.random(25,alphaUnsafe.toCharArray());
    private String textWarPath = RandomStringUtils.random(25,alphaUnsafe.toCharArray()) + ".war";
    private String textName    = RandomStringUtils.random(25,alphaUnsafe.toCharArray());
    private String textGroup   = RandomStringUtils.random(25,alphaUnsafe.toCharArray());

    private String textUpdatedContext = "/updated" + RandomStringUtils.random(25,alphaUnsafe.toCharArray());
    private String textUpdatedWarPath = RandomStringUtils.random(25,alphaUnsafe.toCharArray()) + "-updated.war";
    private String textUpdatedName    = textName + "-updated";
    private String textUpdatedGroup   = textGroup+ "-updated";

    private Identifier<Group> expGroupId;
    private Identifier<Group> expUpdatedGroupId;
    private Identifier<Application> updateAppId;
    private Identifier<Application> deleteAppId;

    private User userObj;
    
    @Before
    public void setup() {
        aUser = "TestUserId";
        userObj = new User(aUser);
        Group group = groupPersistenceService.createGroup(new Event<CreateGroupCommand>(new CreateGroupCommand(textGroup), AuditEvent.now(userObj)));        
        Group updGroup = groupPersistenceService.createGroup(new Event<CreateGroupCommand>(new CreateGroupCommand(textUpdatedGroup), AuditEvent.now(userObj)));        
        expGroupId = group.getId();
        expUpdatedGroupId = updGroup.getId();
        
        deleteAppId = null;
        updateAppId = null;
    }
    
    @After
    public void tearDown() {
        if(updateAppId != null) { 
            try { applicationPersistenceService.removeApplication(updateAppId); } catch (Exception x) { }
        }
        try { groupPersistenceService.removeGroup(expUpdatedGroupId); } catch (Exception x) { }
        try { groupPersistenceService.removeGroup(expGroupId); } catch (Exception x) { }
    }
    
    @Test
    public void testCreateApp() {
        CreateApplicationCommand cmd = new CreateApplicationCommand(expGroupId,  textName, textContext);
        Event<CreateApplicationCommand> anAppToCreate = new Event<>(cmd, AuditEvent.now(new User(aUser)));
        Application created = applicationPersistenceService.createApplication(anAppToCreate);
        assertNotNull(created.getGroup());
        assertEquals(expGroupId, created.getGroup().getId());
        assertEquals(textName, created.getName());
        assertEquals(textContext, created.getWebAppContext());
        updateAppId = created.getId(); 
        deleteAppId = created.getId();
    }
    
    @Test
    public void testUpdateApp() {
        if(updateAppId == null) {
            testCreateApp();
        }
        
        UpdateApplicationCommand cmd = new UpdateApplicationCommand(updateAppId, expUpdatedGroupId,  textUpdatedContext, textUpdatedName);
        Event<UpdateApplicationCommand> anAppToCreate = new Event<>(cmd, AuditEvent.now(new User(aUser)));
        Application created = applicationPersistenceService.updateApplication(anAppToCreate);
        assertEquals(updateAppId, created.getId());
        assertNotNull(created.getGroup());
        assertEquals(expUpdatedGroupId, created.getGroup().getId());
        assertEquals(textUpdatedName, created.getName());
        assertEquals(textUpdatedContext, created.getWebAppContext());
        
    }
    
    @Test
    public void testRemoveApp() {
        testCreateApp();
        
        applicationPersistenceService.removeApplication(deleteAppId);
    }

    @Test(expected = BadRequestException.class)
    public void testRemoveAppAndFailUpdate() {
        testRemoveApp();
        testUpdateApp();
    }    
}
