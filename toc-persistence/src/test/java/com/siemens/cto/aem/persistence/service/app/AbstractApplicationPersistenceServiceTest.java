package com.siemens.cto.aem.persistence.service.app;

import com.siemens.cto.aem.common.exception.NotFoundException;
import com.siemens.cto.aem.common.request.app.*;
import com.siemens.cto.aem.common.request.group.CreateGroupRequest;
import com.siemens.cto.aem.common.request.app.CreateApplicationRequest;
import com.siemens.cto.aem.common.request.app.UpdateApplicationRequest;
import com.siemens.cto.aem.common.domain.model.app.*;
import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.user.User;
import com.siemens.cto.aem.persistence.service.ApplicationPersistenceService;
import com.siemens.cto.aem.persistence.service.GroupPersistenceService;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.*;

@Transactional
public abstract class

        AbstractApplicationPersistenceServiceTest {

    private final static Logger LOGGER = LoggerFactory.getLogger(AbstractApplicationPersistenceServiceTest.class); 

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

    @Before
    public void setup() {
        User user = new User("testUser");
        user.addToThread();

        Group group = groupPersistenceService.createGroup(new CreateGroupRequest(textGroup));
        Group updGroup = groupPersistenceService.createGroup(new CreateGroupRequest(textUpdatedGroup));
        expGroupId = group.getId();
        expUpdatedGroupId = updGroup.getId();
        
        deleteAppId = null;
        updateAppId = null;
    }
    
    @After
    public void tearDown() {
        if(updateAppId != null) { 
            try { applicationPersistenceService.removeApplication(updateAppId); } catch (Exception x) { LOGGER.trace("Test tearDown", x); }
        }
        try { groupPersistenceService.removeGroup(expUpdatedGroupId); } catch (Exception x) { LOGGER.trace("Test tearDown", x); }
        try { groupPersistenceService.removeGroup(expGroupId); } catch (Exception x) { LOGGER.trace("Test tearDown", x); }
        User.getThreadLocalUser().invalidate();
    }
    
    @Test
    public void testCreateApp() {
        CreateApplicationRequest request = new CreateApplicationRequest(expGroupId,  textName, textContext, true, true);
        Application created = applicationPersistenceService.createApplication(request, "", "", "");
        assertNotNull(created.getGroup());
        assertEquals(expGroupId, created.getGroup().getId());
        assertEquals(textName, created.getName());
        assertEquals(textContext, created.getWebAppContext());
        assertTrue(created.isSecure());
        updateAppId = created.getId(); 
        deleteAppId = created.getId();
    }

    @Test
    public void testCreateNonSecureApp() {
        CreateApplicationRequest request = new CreateApplicationRequest(expGroupId,  textName, textContext, false, true);
        Application created = applicationPersistenceService.createApplication(request, "", "", "");
        assertNotNull(created.getGroup());
        assertEquals(expGroupId, created.getGroup().getId());
        assertEquals(textName, created.getName());
        assertEquals(textContext, created.getWebAppContext());
        assertTrue(!created.isSecure());
        updateAppId = created.getId();
        deleteAppId = created.getId();
    }
    
    @Test
    public void testUpdateApp() {
        if(updateAppId == null) {
            testCreateApp();
        }
        
        UpdateApplicationRequest updateApplicationRequest = new UpdateApplicationRequest(updateAppId, expUpdatedGroupId,  textUpdatedContext, textUpdatedName, true, true);
        Application created = applicationPersistenceService.updateApplication(updateApplicationRequest);
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

    @Test(expected = NotFoundException.class)
    public void testRemoveAppAndFailUpdate() {
        testRemoveApp();
        testUpdateApp();
    }    
    
    @Test
    public void testUpdateWARPath() { 
        CreateApplicationRequest request = new CreateApplicationRequest(expGroupId,  textName, textContext, true, true);
        Application created = applicationPersistenceService.createApplication(request, "", "", "");
        
        UploadWebArchiveRequest uploadWebArchiveRequest = new UploadWebArchiveRequest(created, "filename-uuid.war", 0L, null);

        Application uploaded = applicationPersistenceService.updateWARPath(uploadWebArchiveRequest, "D:\\APACHE\\TOMCAT\\WEBAPPS\\filename-uuid.war");
        assertEquals(uploaded.getWarPath(), "D:\\APACHE\\TOMCAT\\WEBAPPS\\filename-uuid.war");                
    }
    
    @Test
    public void testRemoveWARPath() {        
        CreateApplicationRequest request = new CreateApplicationRequest(expGroupId,  textName, textContext, true, true);
        Application created = applicationPersistenceService.createApplication(request, "", "", "");
        
        UploadWebArchiveRequest uploadWebArchiveRequest = new UploadWebArchiveRequest(created, "filename-uuid.war", 0L, null);

        Application uploaded = applicationPersistenceService.updateWARPath(uploadWebArchiveRequest, "D:\\APACHE\\TOMCAT\\WEBAPPS\\filename-uuid.war");
        assertEquals(uploaded.getWarPath(), "D:\\APACHE\\TOMCAT\\WEBAPPS\\filename-uuid.war");                        

        RemoveWebArchiveRequest removeWebArchiveRequest = new RemoveWebArchiveRequest(created);

        Application noWarApp = applicationPersistenceService.removeWarPath(removeWebArchiveRequest);
        assertNull(noWarApp.getWarPath());
    }

    @Test
    public void testUpdateSecureFlag() {
        CreateApplicationRequest request = new CreateApplicationRequest(expGroupId,  textName, textContext, true, true);
        Application created = applicationPersistenceService.createApplication(request, "", "", "");
        assertTrue(created.isSecure());

        final UpdateApplicationRequest updateApplicationRequest =
                new UpdateApplicationRequest(created.getId(),
                                             created.getGroup().getId(),
                                             created.getWebAppContext(),
                                             created.getName(), false, true);
        Application updatedApplication = applicationPersistenceService.updateApplication(updateApplicationRequest);
        assertTrue(!updatedApplication.isSecure());
    }
}