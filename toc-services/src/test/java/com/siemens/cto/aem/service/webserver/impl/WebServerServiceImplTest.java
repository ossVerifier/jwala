package com.siemens.cto.aem.service.webserver.impl;

import com.siemens.cto.aem.common.domain.model.app.Application;
import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.jvm.Jvm;
import com.siemens.cto.aem.common.domain.model.path.FileSystemPath;
import com.siemens.cto.aem.common.domain.model.path.Path;
import com.siemens.cto.aem.common.domain.model.resource.ResourceGroup;
import com.siemens.cto.aem.common.domain.model.user.User;
import com.siemens.cto.aem.common.domain.model.webserver.WebServer;
import com.siemens.cto.aem.common.domain.model.webserver.WebServerReachableState;
import com.siemens.cto.aem.common.exception.InternalErrorException;
import com.siemens.cto.aem.common.properties.ApplicationProperties;
import com.siemens.cto.aem.common.request.webserver.CreateWebServerRequest;
import com.siemens.cto.aem.common.request.webserver.UpdateWebServerRequest;
import com.siemens.cto.aem.common.request.webserver.UploadWebServerTemplateRequest;
import com.siemens.cto.aem.persistence.jpa.service.exception.NonRetrievableResourceTemplateContentException;
import com.siemens.cto.aem.persistence.service.WebServerPersistenceService;
import com.siemens.cto.aem.service.resource.ResourceService;
import com.siemens.cto.toc.files.FileManager;
import com.siemens.cto.toc.files.RepositoryFileInformation;
import com.siemens.cto.toc.files.TocFile;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * Created by z0031wps on 4/2/2014.
 */
@RunWith(MockitoJUnitRunner.class)
public class WebServerServiceImplTest {

    @Mock
    private WebServerPersistenceService webServerPersistenceService;

    private WebServerServiceImpl wsService;

    @Mock
    private WebServer mockWebServer;
    @Mock
    private WebServer mockWebServer2;

    @Mock
    private FileManager fileManager;

    @Mock
    private RepositoryFileInformation repositoryFileInformation;

    private ArrayList<WebServer> mockWebServersAll = new ArrayList<>();
    private ArrayList<WebServer> mockWebServers11 = new ArrayList<>();
    private ArrayList<WebServer> mockWebServers12 = new ArrayList<>();

    private Group group;
    private Group group2;
    private Identifier<Group> groupId;
    private Identifier<Group> groupId2;
    private Collection<Identifier<Group>> groupIds;
    private Collection<Identifier<Group>> groupIds2;
    private Collection<Group> groups;
    private Collection<Group> groups2;

    private User testUser = new User("testUser");
    private ResourceService resourceService;
    private ResourceGroup resourceGroup;


    @Before
    public void setUp() throws IOException {

        groupId = new Identifier<>(1L);
        groupId2 = new Identifier<>(2L);
        groupIds = new ArrayList<>(1);
        groupIds2 = new ArrayList<>(1);
        groupIds.add(groupId);
        groupIds2.add(groupId2);
        group = new Group(groupId, "the-ws-group-name");
        group2 = new Group(new Identifier<Group>(2L), "the-ws-group-name-2");
        groups = new ArrayList<>(1);
        groups2 = new ArrayList<>(1);
        groups.add(group);
        groups2.add(group2);
        resourceService = mock(ResourceService.class);

        when(resourceService.generateResourceGroup()).thenReturn(new ResourceGroup(new ArrayList<Group>()));

        when(mockWebServer.getId()).thenReturn(new Identifier<WebServer>(1L));
        when(mockWebServer.getName()).thenReturn("the-ws-name");
        when(mockWebServer.getHost()).thenReturn("the-ws-hostname");
        when(mockWebServer.getGroups()).thenReturn(groups);
        when(mockWebServer.getGroupIds()).thenReturn(groupIds);
        when(mockWebServer.getPort()).thenReturn(51000);
        when(mockWebServer.getHttpsPort()).thenReturn(52000);
        when(mockWebServer.getStatusPath()).thenReturn(new Path("/statusPath"));
        when(mockWebServer.getHttpConfigFile()).thenReturn(new FileSystemPath("d:/some-dir/httpd.conf"));
        when(mockWebServer.getSvrRoot()).thenReturn(new Path("./"));
        when(mockWebServer.getDocRoot()).thenReturn(new Path("htdocs"));


        when(mockWebServer2.getId()).thenReturn(new Identifier<WebServer>(2L));
        when(mockWebServer2.getName()).thenReturn("the-ws-name-2");
        when(mockWebServer2.getHost()).thenReturn("the-ws-hostname");
        when(mockWebServer2.getGroups()).thenReturn(groups2);
        when(mockWebServer2.getPort()).thenReturn(51000);
        when(mockWebServer2.getHttpsPort()).thenReturn(52000);
        when(mockWebServer2.getStatusPath()).thenReturn(new Path("/statusPath"));
        when(mockWebServer2.getHttpConfigFile()).thenReturn(new FileSystemPath("d:/some-dir/httpd.conf"));
        when(mockWebServer2.getSvrRoot()).thenReturn(new Path("./"));
        when(mockWebServer2.getDocRoot()).thenReturn(new Path("htdocs"));

        mockWebServersAll.add(mockWebServer);
        mockWebServersAll.add(mockWebServer2);

        mockWebServers11.add(mockWebServer);
        mockWebServers12.add(mockWebServer2);

        wsService = new WebServerServiceImpl(webServerPersistenceService, fileManager, resourceService, StringUtils.EMPTY);

        when(repositoryFileInformation.getType()).thenReturn(RepositoryFileInformation.Type.NONE);
        when(fileManager.getAbsoluteLocation(any(TocFile.class))).thenAnswer(new Answer<String>() {

            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                TocFile file = (TocFile) invocation.getArguments()[0];
                if (file != null) {
                    return "/" + file.getFileName();
                }
                return null;
            }
        });

        resourceGroup = new ResourceGroup(new ArrayList<>(groups));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void test() {
        when(webServerPersistenceService.getWebServer(any(Identifier.class))).thenReturn(mockWebServer);
        final WebServer webServer = wsService.getWebServer(new Identifier<WebServer>(1L));
        assertEquals(new Identifier<WebServer>(1L), webServer.getId());
        assertEquals(group.getId(), webServer.getGroups().iterator().next().getId());
        assertEquals("the-ws-name", webServer.getName());
        assertEquals("the-ws-group-name", webServer.getGroups().iterator().next().getName());
        assertEquals("the-ws-hostname", webServer.getHost());
    }

    @Test
    public void testGetWebServers() {
        when(webServerPersistenceService.getWebServers()).thenReturn(mockWebServersAll);
        final List<WebServer> webServers = wsService.getWebServers();
        assertEquals(2, webServers.size());
    }

    @Test
    public void testFindWebServersBelongingTo() {
        when(webServerPersistenceService.findWebServersBelongingTo(eq(new Identifier<Group>(1L)))).thenReturn(mockWebServers11);
        when(webServerPersistenceService.findWebServersBelongingTo(eq(new Identifier<Group>(2L)))).thenReturn(mockWebServers12);

        final List<WebServer> webServers = wsService.findWebServers(group.getId());
        final List<WebServer> webServers2 = wsService.findWebServers(group2.getId());

        assertEquals(1, webServers.size());
        assertEquals(1, webServers2.size());

        verify(webServerPersistenceService, times(1)).findWebServersBelongingTo(eq(group.getId()));
        verify(webServerPersistenceService, times(1)).findWebServersBelongingTo(eq(group2.getId()));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCreateWebServers() {
        System.setProperty(ApplicationProperties.PROPERTIES_ROOT_PATH, "./src/test/resources");

        when(webServerPersistenceService.createWebServer(any(WebServer.class), anyString())).thenReturn(mockWebServer);
        CreateWebServerRequest cmd = new CreateWebServerRequest(mockWebServer.getGroupIds(),
                                                                mockWebServer.getName(),
                                                                mockWebServer.getHost(),
                                                                mockWebServer.getPort(),
                                                                mockWebServer.getHttpsPort(),
                                                                mockWebServer.getStatusPath(),
                                                                mockWebServer.getSvrRoot(),
                                                                mockWebServer.getDocRoot(),
                                                                mockWebServer.getState(),
                                                                mockWebServer.getErrorStatus());
        final WebServer webServer = wsService.createWebServer(cmd, testUser);

        assertEquals(new Identifier<WebServer>(1L), webServer.getId());
        assertEquals(group.getId(), webServer.getGroups().iterator().next().getId());
        assertEquals("the-ws-name", webServer.getName());
        assertEquals("the-ws-group-name", webServer.getGroups().iterator().next().getName());
        assertEquals("the-ws-hostname", webServer.getHost());

        System.clearProperty(ApplicationProperties.PROPERTIES_ROOT_PATH);
    }

    @Test
    public void testDeleteWebServers() {
        when(webServerPersistenceService.getWebServers()).thenReturn(mockWebServersAll);
        wsService.removeWebServer(mockWebServer.getId());
        verify(webServerPersistenceService, atLeastOnce()).removeWebServer(mockWebServer.getId());
    }


    @SuppressWarnings("unchecked")
    @Test
    public void testUpdateWebServers() {
        when(webServerPersistenceService.updateWebServer(any(WebServer.class), anyString())).thenReturn(mockWebServer2);

        UpdateWebServerRequest cmd = new UpdateWebServerRequest(mockWebServer2.getId(),
                                                                groupIds2,
                                                                mockWebServer2.getName(),
                                                                mockWebServer2.getHost(),
                                                                mockWebServer2.getPort(),
                                                                mockWebServer2.getHttpsPort(),
                                                                mockWebServer2.getStatusPath(),
                                                                mockWebServer2.getHttpConfigFile(),
                                                                mockWebServer2.getSvrRoot(),
                                                                mockWebServer2.getDocRoot(),
                                                                mockWebServer2.getState(),
                                                                mockWebServer2.getErrorStatus());
        final WebServer webServer = wsService.updateWebServer(cmd, testUser);

        assertEquals(new Identifier<WebServer>(2L), webServer.getId());
        assertEquals(group2.getId(), webServer.getGroups().iterator().next().getId());
        assertEquals("the-ws-name-2", webServer.getName());
        assertEquals(group2.getName(), webServer.getGroups().iterator().next().getName());
        assertEquals("the-ws-hostname", webServer.getHost());
        assertEquals("d:/some-dir/httpd.conf", webServer.getHttpConfigFile().getUriPath());
    }

    @Test
    public void testFindWebServersNameFragment() {
        when(webServerPersistenceService.findWebServers(eq("the-ws-name-2"))).thenReturn(mockWebServers12);
        when(webServerPersistenceService.findWebServers(eq("the-ws-name"))).thenReturn(mockWebServersAll);

        final List<WebServer> webServers = wsService.findWebServers("the-ws-name-2");
        final List<WebServer> webServers2 = wsService.findWebServers("the-ws-name");

        assertEquals(1, webServers.size());
        assertEquals(2, webServers2.size());

        verify(webServerPersistenceService, times(1)).findWebServers(eq("the-ws-name-2"));
        verify(webServerPersistenceService, times(1)).findWebServers(eq("the-ws-name"));
    }

    @Test
    public void testRemoveWebServersBelongingTo() {
        wsService.removeWebServersBelongingTo(mockWebServer.getGroups().iterator().next().getId());
        verify(webServerPersistenceService, atLeastOnce()).removeWebServersBelongingTo(group.getId());
    }

    private final String readReferenceFile(String file) throws IOException {
        BufferedReader bufferedReader =
                new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream(file)));
        StringBuilder referenceHttpdConfBuilder = new StringBuilder();
        String line;
        do {
            line = bufferedReader.readLine();
            if (line != null) {
                referenceHttpdConfBuilder.append(line);
            }
        } while (line != null);

        return referenceHttpdConfBuilder.toString();
    }

    @Test
    public void testGenerateHttpdConfig() throws IOException {
        System.setProperty(ApplicationProperties.PROPERTIES_ROOT_PATH, new File(".").getAbsolutePath() + "/src/test/resources");
        Application app1 = new Application(null, "hello-world-1", null, "/hello-world-1", null, true, true, false, "testWar.war");
        Application app2 = new Application(null, "hello-world-2", null, "/hello-world-2", null, true, true, false, "testWar.war");

        Application[] appArray = {app1, app2};
        Jvm[] jvmArray = {};

        when(webServerPersistenceService.findWebServerByName(anyString())).thenReturn(mockWebServer);
        when(webServerPersistenceService.findApplications(anyString())).thenReturn(Arrays.asList(appArray));
        when(webServerPersistenceService.findJvms(anyString())).thenReturn(Arrays.asList(jvmArray));
        when(webServerPersistenceService.getResourceTemplate(anyString(), anyString())).thenReturn("httpd.conf template content");

        String generatedHttpdConf = wsService.generateHttpdConfig("Apache2.4", resourceGroup);

        assertEquals("httpd.conf template content", generatedHttpdConf);
    }

    @Test
    public void testGenerateHttpdConfigWithSsl() throws IOException {
        Application app1 = new Application(null, "hello-world-1", null, "/hello-world-1", null, true, true, false, "testWar.war");
        Application app2 = new Application(null, "hello-world-2", null, "/hello-world-2", null, true, true, false, "testWar.war");

        Application[] appArray = {app1, app2};

        when(webServerPersistenceService.findWebServerByName(anyString())).thenReturn(mockWebServer);
        final List<Application> applications = Arrays.asList(appArray);
        when(webServerPersistenceService.findApplications(anyString())).thenReturn(applications);
        when(webServerPersistenceService.getResourceTemplate(anyString(), anyString())).thenReturn(readReferenceFile("/httpd-ssl-conf.tpl"));
        final ArrayList<Group> groupList = new ArrayList<>();
        Group mockGroup = mock(Group.class);
        when(mockGroup.getApplications()).thenReturn(new LinkedHashSet<>(applications));
        groupList.add(mockGroup);
        when(resourceService.generateResourceGroup()).thenReturn(new ResourceGroup(groupList));

        String generatedHttpdConf = wsService.generateHttpdConfig("Apache2.4", new ResourceGroup(groupList));

        assertEquals(removeCarriageReturnsAndNewLines(readReferenceFile("/httpd-ssl.conf")),
                removeCarriageReturnsAndNewLines(generatedHttpdConf));
    }

    @Test(expected = InternalErrorException.class)
    public void testGenerateHttpdConfigWithNonRetrievableResourceTemplateContentException() throws IOException {
        Application app1 = new Application(null, "hello-world-1", null, "/hello-world-1", null, true, true, false, "testWar.war");
        Application app2 = new Application(null, "hello-world-2", null, "/hello-world-2", null, true, true, false, "testWar.war");

        Application[] appArray = {app1, app2};

        when(webServerPersistenceService.findWebServerByName(anyString())).thenReturn(mockWebServer);
        when(webServerPersistenceService.findApplications(anyString())).thenReturn(Arrays.asList(appArray));

        when(webServerPersistenceService.getResourceTemplate(anyString(), anyString())).thenThrow(NonRetrievableResourceTemplateContentException.class);

        String generatedHttpdConf = wsService.generateHttpdConfig("Apache2.4", resourceGroup);

        assertEquals(removeCarriageReturnsAndNewLines(readReferenceFile("/httpd-ssl.conf")),
                removeCarriageReturnsAndNewLines(generatedHttpdConf));
    }

    @Test
    public void testGetWebServer() {
        final WebServer mockWebServer = mock(WebServer.class);
        when(mockWebServer.getName()).thenReturn("mockWebServer");
        when(webServerPersistenceService.findWebServerByName(eq("aWebServer"))).thenReturn(mockWebServer);
        assertEquals("mockWebServer", wsService.getWebServer("aWebServer").getName());
    }

    @Test
    public void testGetResourceTemplateNames() {
        final String[] nameArray = {"httpd.conf"};
        when(webServerPersistenceService.getResourceTemplateNames(eq("Apache2.4"))).thenReturn(Arrays.asList(nameArray));
        final List names = wsService.getResourceTemplateNames("Apache2.4");
        assertEquals("httpd.conf", names.get(0));
    }

    @Test
    public void testGetResourceTemplate() {
        when(webServerPersistenceService.getResourceTemplate(anyString(), anyString())).thenReturn("<template/>");
        assertEquals("<template/>", wsService.getResourceTemplate("any", "any", false, new ResourceGroup()));
    }

    @Test
    public void testGetResourceTemplateTokensReplaced() {
        final WebServer mockWebServer = mock(WebServer.class);
        when(mockWebServer.getName()).thenReturn("mockWebServer");
        when(webServerPersistenceService.getResourceTemplate(anyString(), anyString())).thenReturn("<template>${webServer.name}</template>");
        when(webServerPersistenceService.findWebServerByName(anyString())).thenReturn(mockWebServer);
        assertEquals("<template>mockWebServer</template>", wsService.getResourceTemplate("any", "httpd.conf", true, new ResourceGroup()));
    }

    @Test
    public void testNonHttpdConfGetResourceTemplateTokensReplaced() {
        final WebServer mockWebServer = mock(WebServer.class);
        when(mockWebServer.getName()).thenReturn("mockWebServer");
        when(webServerPersistenceService.getResourceTemplate(anyString(), anyString())).thenReturn("<template>${webServer.name}</template>");
        when(webServerPersistenceService.findWebServerByName(anyString())).thenReturn(mockWebServer);
        assertEquals("<template>mockWebServer</template>", wsService.getResourceTemplate("any", "any-except-httpd.conf", true, new ResourceGroup()));
    }

    @Test
    public void testPopulateWebServerConfig() {
        final List<UploadWebServerTemplateRequest> theList = new ArrayList<>();
        final User user = new User("id");
        final boolean overwriteExisting = false;
        wsService.populateWebServerConfig(theList, user, overwriteExisting);
        verify(webServerPersistenceService).populateWebServerConfig(eq(theList), eq(user), eq(overwriteExisting));
    }

    @Test
    public void testUpdateResourceTemplate() {
        when(webServerPersistenceService.getResourceTemplate("wsName", "resourceTemplateName")).thenReturn("template");
        assertEquals("template", wsService.updateResourceTemplate("wsName", "resourceTemplateName", "template"));
        verify(webServerPersistenceService).updateResourceTemplate(eq("wsName"), eq("resourceTemplateName"), eq("template"));
    }

    @Test
    public void testUploadWebServerConfig() {
        UploadWebServerTemplateRequest request = mock(UploadWebServerTemplateRequest.class);
        when(request.getMetaData()).thenReturn("{\"deployPath\":\"d:/httpd-data\",\"deployFileName\":\"httpd.conf\"}");
        wsService.uploadWebServerConfig(request, testUser);
        verify(webServerPersistenceService).uploadWebServerConfigTemplate(eq(request), anyString(), eq("testUser"));
    }

    @Test
    public void testPreviewResourceTemplate() {
        List<Application> appList = new ArrayList<>();
        List<Jvm> jvmList = new ArrayList<>();
        when(webServerPersistenceService.findWebServerByName(anyString())).thenReturn(mockWebServer);
        when(webServerPersistenceService.findJvms(anyString())).thenReturn(jvmList);
        when(webServerPersistenceService.findApplications(anyString())).thenReturn(appList);
        wsService.previewResourceTemplate("wsName", "groupName", "my template");
        verify(resourceService).generateResourceFile(eq("my template"), any(ResourceGroup.class), any(WebServer.class));
    }

    @Test
    public void testIsStarted() {
        when(mockWebServer.getState()).thenReturn(WebServerReachableState.WS_REACHABLE);
        assertTrue(wsService.isStarted(mockWebServer));
    }

    @Test
    public void testUpdateErrorStatus() {
        wsService.updateErrorStatus(mockWebServer.getId(), "test update error status");
        verify(webServerPersistenceService).updateErrorStatus(new Identifier<WebServer>(1L), "test update error status");
    }

    @Test
    public void testUpdateState() {
        wsService.updateState(mockWebServer.getId(), WebServerReachableState.WS_REACHABLE, "");
        verify(webServerPersistenceService).updateState(new Identifier<WebServer>(1L), WebServerReachableState.WS_REACHABLE, "");
    }

    private String removeCarriageReturnsAndNewLines(String s) {
        return s.replaceAll("\\r", "").replaceAll("\\n", "");
    }
}