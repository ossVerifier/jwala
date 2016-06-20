package com.siemens.cto.aem.persistence.service.app;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.lang.RandomStringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.siemens.cto.aem.common.domain.model.app.Application;
import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.jvm.Jvm;
import com.siemens.cto.aem.common.domain.model.path.Path;
import com.siemens.cto.aem.common.domain.model.user.User;
import com.siemens.cto.aem.common.exception.NotFoundException;
import com.siemens.cto.aem.common.request.app.CreateApplicationRequest;
import com.siemens.cto.aem.common.request.app.RemoveWebArchiveRequest;
import com.siemens.cto.aem.common.request.app.UpdateApplicationRequest;
import com.siemens.cto.aem.common.request.app.UploadAppTemplateRequest;
import com.siemens.cto.aem.common.request.app.UploadWebArchiveRequest;
import com.siemens.cto.aem.common.request.group.AddJvmToGroupRequest;
import com.siemens.cto.aem.common.request.group.CreateGroupRequest;
import com.siemens.cto.aem.common.request.jvm.CreateJvmRequest;
import com.siemens.cto.aem.persistence.jpa.domain.JpaJvm;
import com.siemens.cto.aem.persistence.service.ApplicationPersistenceService;
import com.siemens.cto.aem.persistence.service.GroupPersistenceService;
import com.siemens.cto.aem.persistence.service.JvmPersistenceService;

@Transactional
public abstract class AbstractApplicationPersistenceServiceTest {

    private final static Logger LOGGER = LoggerFactory.getLogger(AbstractApplicationPersistenceServiceTest.class); 

    @Autowired
    private ApplicationPersistenceService applicationPersistenceService;

    @Autowired
    private GroupPersistenceService groupPersistenceService;

    @Autowired
    private JvmPersistenceService jvmPersistenceService;

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
        CreateApplicationRequest request = new CreateApplicationRequest(expGroupId,  textName, textContext, true, true, false);
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
        CreateApplicationRequest request = new CreateApplicationRequest(expGroupId,  textName, textContext, false, true, false);
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
        
        UpdateApplicationRequest updateApplicationRequest = new UpdateApplicationRequest(updateAppId, expUpdatedGroupId,  textUpdatedContext, textUpdatedName, true, true, false);
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
        CreateApplicationRequest request = new CreateApplicationRequest(expGroupId,  textName, textContext, true, true, false);
        Application created = applicationPersistenceService.createApplication(request, "", "", "");
        
        UploadWebArchiveRequest uploadWebArchiveRequest = new UploadWebArchiveRequest(created, "filename-uuid.war", 0L, null);

        Application uploaded = applicationPersistenceService.updateWARPath(uploadWebArchiveRequest, "D:\\APACHE\\TOMCAT\\WEBAPPS\\filename-uuid.war");
        assertEquals(uploaded.getWarPath(), "D:\\APACHE\\TOMCAT\\WEBAPPS\\filename-uuid.war");                
    }
    
    @Test
    public void testRemoveWARPath() {        
        CreateApplicationRequest request = new CreateApplicationRequest(expGroupId,  textName, textContext, true, true, false);
        Application created = applicationPersistenceService.createApplication(request, "", "", "");
        
        UploadWebArchiveRequest uploadWebArchiveRequest = new UploadWebArchiveRequest(created, "filename-uuid.war", 0L, null);

        Application uploaded = applicationPersistenceService.updateWARPath(uploadWebArchiveRequest, "D:\\APACHE\\TOMCAT\\WEBAPPS\\filename-uuid.war");
        assertEquals(uploaded.getWarPath(), "D:\\APACHE\\TOMCAT\\WEBAPPS\\filename-uuid.war");                        

        RemoveWebArchiveRequest removeWebArchiveRequest = new RemoveWebArchiveRequest(created);

        Application noWarApp = applicationPersistenceService.removeWarPathAndName(removeWebArchiveRequest);
        assertNull(noWarApp.getWarPath());
    }

    @Test
    public void testUpdateSecureFlag() {
        CreateApplicationRequest request = new CreateApplicationRequest(expGroupId,  textName, textContext, true, true, false);
        Application created = applicationPersistenceService.createApplication(request, "", "", "");
        assertTrue(created.isSecure());

        final UpdateApplicationRequest updateApplicationRequest =
                new UpdateApplicationRequest(created.getId(),
                                             created.getGroup().getId(),
                                             created.getWebAppContext(),
                                             created.getName(), false, true, false);
        Application updatedApplication = applicationPersistenceService.updateApplication(updateApplicationRequest);
        assertTrue(!updatedApplication.isSecure());
    }

    @Test
    public void testCreateAppConfForJvm() {
        String jvmName = "testJvmName";

        CreateGroupRequest createGroupReq = new CreateGroupRequest("testGroupName");
        Group group = groupPersistenceService.createGroup(createGroupReq);

        CreateApplicationRequest request = new CreateApplicationRequest(group.getId(), "testAppName", "/hctTest", true, true, false);
        Application app = applicationPersistenceService.createApplication(request, "app context template", "role mapping properties", "app properties template");

        CreateJvmRequest createJvmRequest = new CreateJvmRequest(jvmName, "testHost", 9101, 9102, 9103, -1, 9104, new Path("./"), "", null, null);
        Jvm jvm = jvmPersistenceService.createJvm(createJvmRequest);

        AddJvmToGroupRequest addJvmToGroup = new AddJvmToGroupRequest(group.getId(), jvm.getId());
        group = groupPersistenceService.addJvmToGroup(addJvmToGroup);

        applicationPersistenceService.createApplicationConfigTemplateForJvm(jvmName, app, group.getId(), "app context meta data", "app context template");
        String resourceContent = applicationPersistenceService.getResourceTemplate(app.getName(), "hctTest.xml", jvmName, group.getName());
        assertEquals("app context template", resourceContent);

        applicationPersistenceService.removeApplication(app.getId());
        jvmPersistenceService.removeJvm(jvm.getId());
        groupPersistenceService.removeGroup(group.getId());
    }

    @Test
    public void testUpdateResourceTemplate() {
        String jvmName = "testJvmName";

        CreateGroupRequest createGroupReq = new CreateGroupRequest("testGroupName");
        Group group = groupPersistenceService.createGroup(createGroupReq);

        CreateJvmRequest createJvmRequest = new CreateJvmRequest(jvmName, "testHost", 9101, 9102, 9103, -1, 9104, new Path("./"), "", null, null);
        Jvm jvm = jvmPersistenceService.createJvm(createJvmRequest);

        AddJvmToGroupRequest addJvmToGroup = new AddJvmToGroupRequest(group.getId(), jvm.getId());
        group = groupPersistenceService.addJvmToGroup(addJvmToGroup);

        CreateApplicationRequest request = new CreateApplicationRequest(group.getId(), "testAppName", "/hctTest", true, true, false);
        Application app = applicationPersistenceService.createApplication(request, "app context template", "role mapping properties", "app properties template");

// NOTE: The codes below fails because create hct resource files have been commented out on application creation.
//       In the near future the codes below should be removed since the consensus is to generate generic resources
//       when an application is created.

//        String oldContent = applicationPersistenceService.getResourceTemplate(app.getName(), "hctTest.xml", jvmName, group.getName());
//        assertEquals("app context template", oldContent);
//
//        String newContent = applicationPersistenceService.updateResourceTemplate(app.getName(), "hctTest.xml", "new app context template", jvm.getJvmName(), group.getName());
//        assertEquals("new app context template", newContent);
//
        applicationPersistenceService.removeApplication(app.getId());
        jvmPersistenceService.removeJvm(jvm.getId());
        groupPersistenceService.removeGroup(group.getId());
    }

    @Test
    public void testUploadAppTemplate() throws FileNotFoundException {
        CreateJvmRequest createJvmRequest = new CreateJvmRequest("testJvmName", "testHostName", 9101, 9102, 9103, -1, 9104, new Path("./"), "", null, null);

        CreateGroupRequest createGroupReq = new CreateGroupRequest("testGroupName");
        Group group = groupPersistenceService.createGroup(createGroupReq);

        Jvm jvm = jvmPersistenceService.createJvm(createJvmRequest);
        JpaJvm jpaJvm = jvmPersistenceService.getJpaJvm(jvm.getId(), false);

        AddJvmToGroupRequest addJvmGrpRequest = new AddJvmToGroupRequest(group.getId(), jvm.getId());
        group = groupPersistenceService.addJvmToGroup(addJvmGrpRequest);

        CreateApplicationRequest request = new CreateApplicationRequest(group.getId(), "testAppName", "/hctTest", true, true, false);
        Application app = applicationPersistenceService.createApplication(request, "app context template", "role mapping properties", "app properties template");

        Application sameApp = applicationPersistenceService.getApplication(app.getId());
        assertEquals(app.getName(), sameApp.getName());

        sameApp = applicationPersistenceService.findApplication(app.getName(), group.getName(), jvm.getJvmName());
        assertEquals(app.getName(), sameApp.getName());

        List<Application> appList = applicationPersistenceService.findApplicationsBelongingToJvm(jvm.getId());
        assertEquals(1, appList.size());
        assertEquals(app.getName(), appList.get(0).getName());

        InputStream dataStream = new FileInputStream(new File("./src/test/resources/ServerXMLTemplate.tpl"));
        UploadAppTemplateRequest uploadAppTemplateRequest = new UploadAppTemplateRequest(app, "ServerXMLTemplate.tpl", "hctTest.xml", jvm.getJvmName(), "meta data", dataStream);

        applicationPersistenceService.uploadAppTemplate(uploadAppTemplateRequest, jpaJvm);

        applicationPersistenceService.removeApplication(app.getId());
        jvmPersistenceService.removeJvm(jvm.getId());
        groupPersistenceService.removeGroup(group.getId());
    }
}