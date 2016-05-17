package com.siemens.cto.aem.service.group.impl;

import com.siemens.cto.aem.common.domain.model.app.Application;
import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.jvm.Jvm;
import com.siemens.cto.aem.common.domain.model.resource.ResourceGroup;
import com.siemens.cto.aem.common.domain.model.user.User;
import com.siemens.cto.aem.common.domain.model.webserver.WebServer;
import com.siemens.cto.aem.common.domain.model.webserver.WebServerReachableState;
import com.siemens.cto.aem.common.exception.ApplicationException;
import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.common.request.group.*;
import com.siemens.cto.aem.common.request.jvm.UploadJvmTemplateRequest;
import com.siemens.cto.aem.common.request.webserver.UploadWebServerTemplateRequest;
import com.siemens.cto.aem.control.command.RemoteCommandExecutorImpl;
import com.siemens.cto.aem.persistence.jpa.domain.resource.config.template.ConfigTemplate;
import com.siemens.cto.aem.persistence.service.ApplicationPersistenceService;
import com.siemens.cto.aem.persistence.service.GroupPersistenceService;
import com.siemens.cto.aem.persistence.service.WebServerPersistenceService;
import com.siemens.cto.aem.service.VerificationBehaviorSupport;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.*;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class GroupServiceImplVerifyTest extends VerificationBehaviorSupport {

    private GroupServiceImpl groupService;
    private GroupPersistenceService groupPersistenceService;
    private ApplicationPersistenceService applicationPersistenceService;
    private WebServerPersistenceService webServerPersistenceService;
    private User user;
    private RemoteCommandExecutorImpl remoteCommandExecutor;

    @Before
    public void setUp() {
        groupPersistenceService = mock(GroupPersistenceService.class);
        applicationPersistenceService = mock(ApplicationPersistenceService.class);
        webServerPersistenceService = mock(WebServerPersistenceService.class);
        remoteCommandExecutor = mock(RemoteCommandExecutorImpl.class);

        groupService = new GroupServiceImpl(groupPersistenceService,
                webServerPersistenceService,
                applicationPersistenceService,
                remoteCommandExecutor);
        user = new User("unused");
    }

    @Test
    public void testCreateGroup() {

        final CreateGroupRequest command = mock(CreateGroupRequest.class);

        groupService.createGroup(command,
                user);

        verify(command, times(1)).validate();
        verify(groupPersistenceService, times(1)).createGroup(command);
    }

    @Test
    public void testGetGroup() {

        final Identifier<Group> id = new Identifier<>(-123456L);

        groupService.getGroup(id);

        verify(groupPersistenceService, times(1)).getGroup(eq(id));
    }

    @Test
    public void testGetGroups() {

        groupService.getGroups();

        verify(groupPersistenceService, times(1)).getGroups();
    }

    @Test
    public void testFindGroups() {

        final String fragment = "unused";

        groupService.findGroups(fragment);

        verify(groupPersistenceService, times(1)).findGroups(eq(fragment));
    }

    @Test(expected = BadRequestException.class)
    public void testFindGroupsWithBadName() {

        final String badFragment = "";

        groupService.findGroups(badFragment);
    }

    @Test
    public void testUpdateGroup() throws InterruptedException {
        final UpdateGroupRequest updateGroupRequest = mock(UpdateGroupRequest.class);
        groupService.updateGroup(updateGroupRequest, user);

        verify(updateGroupRequest).validate();
        verify(groupPersistenceService).updateGroup(updateGroupRequest);
    }

    @Test
    public void testRemoveGroup() {
        final Identifier<Group> id = new Identifier<>(-123456L);
        groupService.removeGroup(id);
        verify(groupPersistenceService, times(1)).removeGroup(eq(id));
    }

    @Test
    public void testAddJvmToGroup() {
        final AddJvmToGroupRequest addJvmToGroupRequest = mock(AddJvmToGroupRequest.class);
        groupService.addJvmToGroup(addJvmToGroupRequest, user);
        verify(addJvmToGroupRequest, times(1)).validate();
        verify(groupPersistenceService, times(1)).addJvmToGroup(addJvmToGroupRequest);
    }

    @Test
    public void testAddJvmsToGroup() {

        final AddJvmsToGroupRequest addJvmsToGroupRequest = mock(AddJvmsToGroupRequest.class);

        final Set<AddJvmToGroupRequest> addJvmToGroupRequests = createMockedAddRequests(5);
        when(addJvmsToGroupRequest.toRequests()).thenReturn(addJvmToGroupRequests);

        groupService.addJvmsToGroup(addJvmsToGroupRequest, user);

        verify(addJvmsToGroupRequest, times(1)).validate();
        for (final AddJvmToGroupRequest addJvmToGroupRequest : addJvmToGroupRequests) {
            verify(addJvmToGroupRequest, times(1)).validate();
            verify(groupPersistenceService, times(1)).addJvmToGroup(addJvmToGroupRequest);
        }
    }

    @Test
    public void testRemoveJvmFromGroup() {

        final RemoveJvmFromGroupRequest removeJvmFromGroupRequest = mock(RemoveJvmFromGroupRequest.class);

        groupService.removeJvmFromGroup(removeJvmFromGroupRequest, user);

        verify(removeJvmFromGroupRequest, times(1)).validate();
        verify(groupPersistenceService, times(1)).removeJvmFromGroup(removeJvmFromGroupRequest);

        // TODO: Remove if this is no londer needed.
        // verify(stateNotificationWorker).refreshState(eq(groupStateService), any(Group.class));
    }

    @Test
    public void testGetOtherGroupingDetailsOfJvms() {
        final Set<Group> groupSet = new HashSet<>();
        groupSet.add(new Group(new Identifier<Group>("1"), "Group1"));
        groupSet.add(new Group(new Identifier<Group>("2"), "Group2"));
        groupSet.add(new Group(new Identifier<Group>("3"), "Group3"));

        final Set<Jvm> jvmSet = new HashSet<>();
        jvmSet.add(new Jvm(new Identifier<Jvm>("1"), "Jvm1", groupSet));

        final Group group = new Group(new Identifier<Group>("1"), "Group1", jvmSet);

        when(groupPersistenceService.getGroup(any(Identifier.class), eq(false))).thenReturn(group);

        final List<Jvm> otherGroupingDetailsOfJvm = groupService.getOtherGroupingDetailsOfJvms(new Identifier<Group>("1"));

        assertTrue(otherGroupingDetailsOfJvm.size() == 1);
        assertEquals(otherGroupingDetailsOfJvm.get(0).getGroups().size(), 2);

        String groupNames = "";
        for (Group grp : otherGroupingDetailsOfJvm.get(0).getGroups()) {
            groupNames += grp.getName();
        }

        assertTrue("Group3Group2".equalsIgnoreCase(groupNames) || "Group2Group3".equalsIgnoreCase(groupNames));
    }

    @Test
    public void testGetOtherGroupingDetailsOfWebServers() {
        final List<Group> groupSet = new ArrayList<>();
        groupSet.add(new Group(new Identifier<Group>("2"), "Group2"));
        groupSet.add(new Group(new Identifier<Group>("3"), "Group3"));

        final Set<WebServer> webServerSet = new HashSet<>();
        webServerSet.add(new WebServer(new Identifier<WebServer>("1"), groupSet, "WebServer1", null, null, null, null,
                null, null, null, WebServerReachableState.WS_UNREACHABLE, null));

        groupSet.add(new Group(new Identifier<Group>("1"), "Group1", new HashSet<Jvm>(), webServerSet, null, null));

        when(groupPersistenceService.getGroup(any(Identifier.class), eq(true))).thenReturn(groupSet.get(2));

        final List<WebServer> otherGroupingDetailsOfWebServer =
                groupService.getOtherGroupingDetailsOfWebServers(new Identifier<Group>("1"));
        assertTrue(otherGroupingDetailsOfWebServer.size() == 1);

        String groupNames = "";
        for (Group grp : otherGroupingDetailsOfWebServer.get(0).getGroups()) {
            groupNames += grp.getName();
        }

        assertTrue("Group3Group2".equalsIgnoreCase(groupNames) || "Group2Group3".equalsIgnoreCase(groupNames));
    }

    @Test
    public void testPopulateJvmConfig() throws FileNotFoundException {
        List<UploadJvmTemplateRequest> uploadRequests = new ArrayList<>();
        InputStream data = new FileInputStream(new File("./src/test/resources/ServerXMLTemplate.tpl"));
        UploadJvmTemplateRequest uploadJvmRequest = new UploadJvmTemplateRequest(new Jvm(new Identifier<Jvm>(11L), "testJvm",
                new HashSet<Group>()), "ServerXMLTemplate.tpl", data, StringUtils.EMPTY) {
            @Override
            public String getConfFileName() {
                return "server.xml";
            }
        };
        uploadRequests.add(uploadJvmRequest);
        final Identifier<Group> aGroupId = new Identifier<>(11L);
        groupService.populateJvmConfig(aGroupId, uploadRequests, user, false);
        verify(groupPersistenceService, times(1)).populateJvmConfig(aGroupId, uploadRequests, user, false);
    }

    @Test
    public void testPopulateWebServerConfig() throws FileNotFoundException {
        List<UploadWebServerTemplateRequest> uploadRequests = new ArrayList<>();
        InputStream data = new FileInputStream(new File("./src/test/resources/HttpdSslConfTemplate.tpl"));
        UploadWebServerTemplateRequest uploadWSRequest = new UploadWebServerTemplateRequest(new WebServer(new Identifier<WebServer>(11L),
                new HashSet<Group>(), "testWebServer"), "HttpdSslConfTemplate.tpl", StringUtils.EMPTY, data) {
            @Override
            public String getConfFileName() {
                return "httpd.conf";
            }
        };
        uploadRequests.add(uploadWSRequest);
        final Identifier<Group> aGroupId = new Identifier<>(11L);
        groupService.populateWebServerConfig(aGroupId, uploadRequests, user, false);
        verify(webServerPersistenceService, times(1)).populateWebServerConfig(uploadRequests, user, false);
    }

    @Test
    public void testPopulateGroupJvmTemplates() throws FileNotFoundException {
        List<UploadJvmTemplateRequest> uploadRequests = new ArrayList<>();
        InputStream data = new FileInputStream(new File("./src/test/resources/ServerXMLTemplate.tpl"));
        UploadJvmTemplateRequest uploadJvmRequest = new UploadJvmTemplateRequest(new Jvm(new Identifier<Jvm>(11L), "testJvm",
                new HashSet<Group>()), "ServerXMLTemplate.tpl", data, StringUtils.EMPTY) {
            @Override
            public String getConfFileName() {
                return "server.xml";
            }
        };
        uploadRequests.add(uploadJvmRequest);
        groupService.populateGroupJvmTemplates("testGroupName", uploadRequests, user);
        verify(groupPersistenceService, times(1)).populateGroupJvmTemplates("testGroupName", uploadRequests);
    }

    @Test
    public void testPopulateGroupWebServerTemplates() throws FileNotFoundException {
        List<UploadWebServerTemplateRequest> uploadRequests = new ArrayList<>();
        InputStream data = new FileInputStream(new File("./src/test/resources/HttpdSslConfTemplate.tpl"));
        UploadWebServerTemplateRequest uploadWSRequest = new UploadWebServerTemplateRequest(new WebServer(new Identifier<WebServer>(11L),
                new HashSet<Group>(), "testWebServer"), "HttpdSslConfTemplate.tpl", StringUtils.EMPTY, data) {
            @Override
            public String getConfFileName() {
                return "httpd.conf";
            }
        };
        uploadRequests.add(uploadWSRequest);
        groupService.populateGroupWebServerTemplates("testGroupName", uploadRequests, user);
        verify(groupPersistenceService, times(1)).populateGroupWebServerTemplates("testGroupName", uploadRequests);
    }

    @Test
    public void testGroupJvmsResourceTemplateNames() {
        groupService.getGroupJvmsResourceTemplateNames("testGroupName");
        verify(groupPersistenceService, times(1)).getGroupJvmsResourceTemplateNames("testGroupName");


        List<String> jvmTemplates = new ArrayList<>();
        jvmTemplates.add("server.xml");
        when(groupPersistenceService.getGroupJvmsResourceTemplateNames(anyString())).thenReturn(jvmTemplates);
        List<String> templateNames = groupService.getGroupJvmsResourceTemplateNames("testGroupName");
        assertEquals(1, templateNames.size());
    }

    @Test
    public void testGroupWebServersResourceTemplateNames() {
        groupService.getGroupWebServersResourceTemplateNames("testGroupName");
        verify(groupPersistenceService, times(1)).getGroupWebServersResourceTemplateNames("testGroupName");
    }

    @Test
    public void testGetGroupJvmResourceTemplate() {
        groupService.getGroupJvmResourceTemplate("testGroupName", "server.xml", false);
        verify(groupPersistenceService, times(1)).getGroupJvmResourceTemplate("testGroupName", "server.xml");
        verify(groupPersistenceService, times(0)).getGroup("testGroupName");

        Group mockGroup = mock(Group.class);
        Set<Jvm> jvms = new HashSet<>();
        Set<Group> groups = new HashSet<>();
        groups.add(mockGroup);
        jvms.add(new Jvm(new Identifier<Jvm>(111L), "testJvmName", groups));
        when(mockGroup.getJvms()).thenReturn(jvms);
        when(groupPersistenceService.getGroup(anyString())).thenReturn(mockGroup);
        when(groupPersistenceService.getGroupJvmResourceTemplate(anyString(), anyString())).thenReturn("${jvm.jvmName}");
        String tokenizedTemplate = groupService.getGroupJvmResourceTemplate("testGroupName", "server.xml", true);
        assertEquals("testJvmName", tokenizedTemplate);
    }

    @Test
    public void testGetGroupWebServerResourceTemplate() {
        groupService.getGroupWebServerResourceTemplate("testGroupName", "httpd.conf", false);
        verify(groupPersistenceService, times(1)).getGroupWebServerResourceTemplate("testGroupName", "httpd.conf");
        verify(groupPersistenceService, times(0)).getGroup("testGroupName");

        Group mockGroup = mock(Group.class);
        Set<WebServer> webServers = new HashSet<>();
        Set<Group> groups = new HashSet<>();
        final WebServer testWebServer = new WebServer(new Identifier<WebServer>(111L), groups, "testWebServerName");
        webServers.add(testWebServer);
        when(mockGroup.getWebServers()).thenReturn(webServers);
        when(mockGroup.getId()).thenReturn(new Identifier<Group>(111L));
        when(groupPersistenceService.getGroup(anyString())).thenReturn(mockGroup);
        when(groupPersistenceService.getGroupWithWebServers(any(Identifier.class))).thenReturn(mockGroup);
        when(groupPersistenceService.getGroupWebServerResourceTemplate(anyString(), anyString())).thenReturn("${webServer.name}");
        String tokenizedTemplate = groupService.getGroupWebServerResourceTemplate("testGroupName", "httpd.conf", true);
        assertEquals("testWebServerName", tokenizedTemplate);
    }

    @Test
    public void testUpdateGroupJvmTemplate() {
        groupService.updateGroupJvmResourceTemplate("testGroupName", "server.xml", "${jvm.jvmName}");
        verify(groupPersistenceService).updateGroupJvmResourceTemplate("testGroupName", "server.xml", "${jvm.jvmName}");
    }

    @Test
    public void testUpdateGroupWebServerTemplate() {
        groupService.updateGroupWebServerResourceTemplate("testGroupName", "httpd.conf", "${webServer.name}");
        verify(groupPersistenceService).updateGroupWebServerResourceTemplate("testGroupName", "httpd.conf", "${webServer.name}");
    }

    @Test
    public void testPreviewJvmTemplate() {
        Group mockGroup = mock(Group.class);
        Set<Jvm> jvmList = new HashSet<>();
        Jvm jvm = new Jvm(new Identifier<Jvm>(111L), "testJvmName", new HashSet<Group>());
        when(groupPersistenceService.getGroup(anyString())).thenReturn(mockGroup);
        when(mockGroup.getJvms()).thenReturn(jvmList);
        String template = groupService.previewGroupJvmResourceTemplate("testGroupName", "${jvm.jvmName}");
        assertEquals("${jvm.jvmName}", template);

        jvmList.add(jvm);
        template = groupService.previewGroupJvmResourceTemplate("testGroupName", "${jvm.jvmName}");
        assertEquals("testJvmName", template);
    }

    @Test
    public void testPreviewWebServerTemplate() {
        Group mockGroup = mock(Group.class);
        Set<WebServer> wsList = new HashSet<>();
        WebServer webServer = new WebServer(new Identifier<WebServer>(111L), new HashSet<Group>(), "testWebServerName");
        when(groupPersistenceService.getGroup(anyString())).thenReturn(mockGroup);
        when(groupPersistenceService.getGroupWithWebServers(any(Identifier.class))).thenReturn(mockGroup);
        when(mockGroup.getWebServers()).thenReturn(wsList);
        String template = groupService.previewGroupWebServerResourceTemplate("testGroupName", "${webServer.name}");
        assertEquals("${webServer.name}", template);

        wsList.add(webServer);
        template = groupService.previewGroupWebServerResourceTemplate("testGroupName", "${webServer.name}");
        assertEquals("testWebServerName", template);
    }

    @Test
    public void testGetGroupWithWebServer() {
        final Identifier<Group> aGroupId = new Identifier<>(1212L);
        groupService.getGroupWithWebServers(aGroupId);
        verify(groupPersistenceService).getGroupWithWebServers(aGroupId);
    }

    @Test
    public void testGetGroupName() {
        final String testGroupName = "testGroupName";
        groupService.getGroup(testGroupName);
        verify(groupPersistenceService).getGroup(testGroupName);
    }

    @Test
    public void testGetGroupsFetchWebServers() {
        groupService.getGroups(true);
        verify(groupPersistenceService).getGroups(true);
    }

    @Test
    public void testRemoveGroupName() {
        final String testGroupName = "testGroupName";
        groupService.removeGroup(testGroupName);
        verify(groupPersistenceService).removeGroup(testGroupName);
    }

    @Test
    public void testPopulateGroupAppTemplates() {
        Application mockApplication = mock(Application.class);
        Group mockGroup = mock(Group.class);
        when(mockApplication.getGroup()).thenReturn(mockGroup);
        when(mockApplication.getWebAppContext()).thenReturn("/testApp");
        when(groupPersistenceService.populateGroupAppTemplate(any(Group.class), anyString(), anyString(), anyString()))
                .thenReturn(mock(ConfigTemplate.class));

        groupService.populateGroupAppTemplates(mockApplication, "app content meta data", "app content", "role mapping content meta data",
                "role mapping content", "properties content meta data" ,"properties content");

        verify(groupPersistenceService).populateGroupAppTemplate(mockGroup, "testApp.xml", "app content meta data", "app content");
        verify(groupPersistenceService).populateGroupAppTemplate(mockGroup, "testAppRoleMapping.properties",
                "role mapping content meta data", "role mapping content");
        verify(groupPersistenceService).populateGroupAppTemplate(mockGroup, "testApp.properties", "properties content meta data",
                "properties content");
    }

    @Test
    public void testPopulateGroupAppTemplate() {
        Group mockGroup = mock(Group.class);
        when(groupPersistenceService.getGroup(anyString())).thenReturn(mockGroup);
        when(groupPersistenceService.populateGroupAppTemplate(any(Group.class), anyString(), anyString(), anyString())).thenReturn(mock(ConfigTemplate.class));
        when(groupPersistenceService.getGroupAppResourceTemplate(anyString(), anyString())).thenReturn("template content");
        groupService.populateGroupAppTemplate("testGroup", "hct.xml", "hct meta data", "template content");

        verify(groupPersistenceService).populateGroupAppTemplate(mockGroup, "hct.xml", "hct meta data", "template content");
    }

    @Test
    public void testGetGroupAppTemplateNames() {
        when(groupPersistenceService.getGroupAppsResourceTemplateNames(anyString())).thenReturn(new ArrayList<String>());
        groupService.getGroupAppsResourceTemplateNames("testGroup");
        verify(groupPersistenceService).getGroupAppsResourceTemplateNames("testGroup");
    }

    @Test
    public void testUpdateGroupAppResourceTemplate() {
        when(groupPersistenceService.updateGroupAppResourceTemplate(anyString(), anyString(), anyString())).thenReturn("template content");
        groupService.updateGroupAppResourceTemplate("testGroup", "hct.xml", "template content");
        verify(groupPersistenceService).updateGroupAppResourceTemplate("testGroup", "hct.xml", "template content");
    }

    @Test(expected = ApplicationException.class)
    public void testPreviewGroupAppTemplate() {
        Group mockGroup = mock(Group.class);
        Set<Jvm> jvmsList = new HashSet<>();
        List<Application> appList = new ArrayList<>();
        Jvm mockJvm = mock(Jvm.class);
        jvmsList.add(mockJvm);
        Application mockApp = mock(Application.class);
        appList.add(mockApp);
        when(mockJvm.getJvmName()).thenReturn("testJvm");
        when(mockApp.getWebAppContext()).thenReturn("/hct");
        when(mockApp.getId()).thenReturn(new Identifier<Application>(99L));
        when(mockApp.getName()).thenReturn("testApp");
        when(mockApp.getWarPath()).thenReturn("./");
        when(mockApp.getGroup()).thenReturn(mockGroup);
        when(mockApp.isSecure()).thenReturn(true);
        when(mockApp.getWarName()).thenReturn("testApp.war");
        when(mockApp.isLoadBalanceAcrossServers()).thenReturn(true);
        when(mockGroup.getJvms()).thenReturn(jvmsList);
        when(groupPersistenceService.getGroup(anyString())).thenReturn(mockGroup);
        when(applicationPersistenceService.getApplications()).thenReturn(appList);
        when(applicationPersistenceService.findApplication(anyString(), anyString(), anyString())).thenReturn(mockApp);
        when(applicationPersistenceService.getApplication(anyString())).thenReturn(mockApp);
        when(groupPersistenceService.getGroupAppResourceTemplateMetaData(anyString(), anyString())).thenReturn("{\"entity\":{\"target\": \"testApp\"}}");

        String content = groupService.previewGroupAppResourceTemplate("testGroup", "hct.xml", "hct content", new ResourceGroup());
        assertEquals("hct content", content);

        groupService.previewGroupAppResourceTemplate("testGroup", "hct.xml", "hct content ${webApp.fail.token}", new ResourceGroup());
    }

    @Test
    public void testGetGroupAppResourceTemplate() {
        Group mockGroup = mock(Group.class);
        Set<Jvm> jvmsList = new HashSet<>();
        List<Application> appList = new ArrayList<>();
        Jvm mockJvm = mock(Jvm.class);
        jvmsList.add(mockJvm);
        Application mockApp = mock(Application.class);
        appList.add(mockApp);
        when(mockJvm.getJvmName()).thenReturn("testJvm");
        when(mockApp.getWebAppContext()).thenReturn("/hct");
        when(mockApp.getId()).thenReturn(new Identifier<Application>(99L));
        when(mockApp.getName()).thenReturn("testApp");
        when(mockApp.getWarPath()).thenReturn("./");
        when(mockApp.getGroup()).thenReturn(mockGroup);
        when(mockApp.isSecure()).thenReturn(true);
        when(mockApp.getWarName()).thenReturn("testApp.war");
        when(mockApp.isLoadBalanceAcrossServers()).thenReturn(true);
        when(mockGroup.getJvms()).thenReturn(jvmsList);
        when(groupPersistenceService.getGroup(anyString())).thenReturn(mockGroup);
        when(groupPersistenceService.getGroupAppResourceTemplate(anyString(), anyString())).thenReturn("hct content ${webApp.name}");
        when(applicationPersistenceService.getApplications()).thenReturn(appList);
        when(applicationPersistenceService.findApplication(anyString(), anyString(), anyString())).thenReturn(mockApp);
        when(applicationPersistenceService.getApplication(anyString())).thenReturn(mockApp);

        String content = groupService.getGroupAppResourceTemplate("testGroup", "hct.xml", false, new ResourceGroup());
        assertEquals("hct content ${webApp.name}", content);

        when(groupPersistenceService.getGroupAppResourceTemplateMetaData(anyString(), anyString())).thenReturn("{\"entity\":{\"target\": \"testApp\"}}");
        content = groupService.getGroupAppResourceTemplate("testGroup", "hct.xml", true, new ResourceGroup());
        assertEquals("hct content testApp", content);

        when(groupPersistenceService.getGroupAppResourceTemplate(anyString(), anyString())).thenReturn("hct content ${webApp.fail.name}");
        try {
            content = groupService.getGroupAppResourceTemplate("testGroup", "hct.xml", true, new ResourceGroup());
        } catch (ApplicationException ae) {
            assertEquals("Template token replacement failed.", ae.getMessage());
        } catch (Exception e) {
            assertFalse("Expecting ApplicationException but got " + e, true);
        }
    }

}
