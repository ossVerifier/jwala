package com.siemens.cto.aem.service.resource;

import com.siemens.cto.aem.common.domain.model.app.Application;
import com.siemens.cto.aem.common.domain.model.group.CurrentGroupState;
import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.common.domain.model.group.GroupState;
import com.siemens.cto.aem.common.domain.model.group.History;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.jvm.Jvm;
import com.siemens.cto.aem.common.domain.model.jvm.JvmState;
import com.siemens.cto.aem.common.domain.model.path.FileSystemPath;
import com.siemens.cto.aem.common.domain.model.resource.*;
import com.siemens.cto.aem.common.domain.model.user.User;
import com.siemens.cto.aem.common.domain.model.webserver.WebServer;
import com.siemens.cto.aem.common.domain.model.webserver.WebServerReachableState;
import com.siemens.cto.aem.common.properties.ApplicationProperties;
import com.siemens.cto.aem.common.request.app.UploadAppTemplateRequest;
import com.siemens.cto.aem.common.request.app.UploadWebArchiveRequest;
import com.siemens.cto.aem.common.request.jvm.UploadJvmConfigTemplateRequest;
import com.siemens.cto.aem.common.request.webserver.UploadWebServerTemplateRequest;
import com.siemens.cto.aem.persistence.jpa.domain.JpaJvm;
import com.siemens.cto.aem.persistence.service.*;
import com.siemens.cto.aem.service.app.ApplicationService;
import com.siemens.cto.aem.service.app.PrivateApplicationService;
import com.siemens.cto.aem.service.resource.impl.ResourceServiceImpl;
import com.siemens.cto.toc.files.FileManager;
import com.siemens.cto.toc.files.RepositoryFileInformation;
import com.siemens.cto.toc.files.WebArchiveManager;
import org.apache.commons.lang.StringUtils;
import org.codehaus.groovy.runtime.ResourceGroovyMethods;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.verification.Times;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link ResourceService}.
 */
public class ResourceServiceImplTest {

    @Mock
    private FileManager mockFileManager;

    @Mock
    private ResourcePersistenceService mockResourcePersistenceService;

    @Mock
    private GroupPersistenceService mockGroupPesistenceService;

    @Mock
    private ApplicationPersistenceService mockAppPersistenceService;

    @Mock
    private JvmPersistenceService mockJvmPersistenceService;

    @Mock
    private WebServerPersistenceService mockWebServerPersistenceService;

    @Mock
    private ApplicationService mockAppService;

    private ResourceService resourceService;

    @Mock
    private PrivateApplicationService mockPrivateApplicationService;

    @Mock
    private ResourceDao mockResourceDao;

    @Mock
    private WebArchiveManager mockWebArchiveManager;

    @Mock
    private ResourceHandler mockResourceHandler;

    @Before
    public void setup() {
        // It is good practice to start with a clean sheet of paper before each test that is why resourceService is
        // initialized here. This makes sure that unrelated tests don't affect each other.
        MockitoAnnotations.initMocks(this);
        resourceService = new ResourceServiceImpl(mockResourcePersistenceService, mockGroupPesistenceService,
                mockAppPersistenceService, mockJvmPersistenceService, mockWebServerPersistenceService,
                mockPrivateApplicationService, mockResourceDao, mockWebArchiveManager, mockResourceHandler);

        when(mockJvmPersistenceService.findJvmByExactName(eq("someJvm"))).thenReturn(mock(Jvm.class));

        // emulates uploadIfBinaryData
        final RepositoryFileInformation mockRepositoryFileInformation = mock(RepositoryFileInformation.class);
        final Path mockPath = mock(Path.class);
        when(mockPath.toString()).thenReturn("thePath");
        when(mockRepositoryFileInformation.getPath()).thenReturn(mockPath);
        when(mockPrivateApplicationService.uploadWebArchiveData(any(UploadWebArchiveRequest.class)))
                .thenReturn(mockRepositoryFileInformation);

        System.setProperty(ApplicationProperties.PROPERTIES_ROOT_PATH, new File(".").getAbsolutePath() + "/src/test/resources");
    }

    @Test
    public void testEncryption() {
        assertEquals("sr94UX5Zuw7QBM992+lAvQ==", resourceService.encryptUsingPlatformBean("hello"));
    }

    @Test
    public void testCreate() {
        assertNotNull("");
    }

    @Test
    public void testUpdateAttributes() {
        assertNotNull("");
    }

    @Test
    public void testUpdateFriendlyName() {
        assertNotNull("");
    }

    @Test
    public void testRead() {
        assertNotNull("");
    }

    @Test
    public void getType() {
        assertNotNull("");
    }

    @Test
    public void testCreateJvmTemplate() {
        final InputStream metaDataIn = this.getClass().getClassLoader()
                .getResourceAsStream("resource-service-test-files/create-jvm-template-test-metadata.json");
        final InputStream templateIn = this.getClass().getClassLoader()
                .getResourceAsStream("resource-service-test-files/server.xml.tpl");
        Group mockGroup = mock(Group.class);
        Jvm mockJvm = mock(Jvm.class);
        when(mockJvm.getId()).thenReturn(new Identifier<Jvm>(1L));
        when(mockJvm.getAjpPort()).thenReturn(9103);
        when(mockJvm.getErrorStatus()).thenReturn(null);
        when(mockJvm.getGroups()).thenReturn(new HashSet<Group>());
        when(mockJvm.getHostName()).thenReturn("localhost");
        when(mockJvm.getHttpPort()).thenReturn(9100);
        when(mockJvm.getHttpsPort()).thenReturn(9101);
        when(mockJvm.getJvmName()).thenReturn("some jvm");
        when(mockJvm.getParentGroup()).thenReturn(mockGroup);
        when(mockJvm.getRedirectPort()).thenReturn(9102);
        when(mockJvm.getShutdownPort()).thenReturn(-1);
        when(mockJvm.getState()).thenReturn(JvmState.JVM_STOPPED);
        when(mockJvm.getStateLabel()).thenReturn(null);
        when(mockJvm.getStatusPath()).thenReturn(null);
        when(mockJvm.getStatusUri()).thenReturn(null);
        when(mockJvm.getSystemProperties()).thenReturn(null);
        when(mockGroupPesistenceService.getGroup(anyString())).thenReturn(mockGroup);
        when(mockJvmPersistenceService.findJvmByExactName(anyString())).thenReturn(mockJvm);
        User mockUser = mock(User.class);
        when(mockUser.getId()).thenReturn("user-id");
        resourceService.createTemplate(metaDataIn, templateIn, "some jvm", mockUser);
        verify(mockJvmPersistenceService).findJvmByExactName("some jvm");
        verify(mockJvmPersistenceService).uploadJvmTemplateXml(any(UploadJvmConfigTemplateRequest.class));
    }

    @Test
    public void testCreateGroupedJvmsTemplate() {
        final InputStream metaDataIn = this.getClass().getClassLoader()
                .getResourceAsStream("resource-service-test-files/create-grouped-jvms-template-test-metadata.json");
        final InputStream templateIn = this.getClass().getClassLoader()
                .getResourceAsStream("resource-service-test-files/server.xml.tpl");

        final Set<Jvm> jvmSet = new HashSet<>();
        jvmSet.add(mock(Jvm.class));
        jvmSet.add(mock(Jvm.class));
        final Group mockGroup = mock(Group.class);
        when(mockGroup.getJvms()).thenReturn(jvmSet);
        when(mockGroupPesistenceService.getGroup(eq("HEALTH CHECK 4.0"))).thenReturn(mockGroup);
        User mockUser = mock(User.class);
        when(mockUser.getId()).thenReturn("user-id");
        resourceService.createTemplate(metaDataIn, templateIn, "test-app-name", mockUser);
        verify(mockJvmPersistenceService, new Times(2)).uploadJvmTemplateXml(any(UploadJvmConfigTemplateRequest.class));
        verify(mockGroupPesistenceService).populateGroupJvmTemplates(eq("HEALTH CHECK 4.0"), any(List.class));
    }

    @Test
    public void testCreateWebServerTemplate() {
        final InputStream metaDataIn = this.getClass().getClassLoader()
                .getResourceAsStream("resource-service-test-files/create-ws-template-test-metadata.json");
        final InputStream templateIn = this.getClass().getClassLoader()
                .getResourceAsStream("resource-service-test-files/httpd.conf.tpl");
        User mockUser = mock(User.class);
        when(mockUser.getId()).thenReturn("user-id");
        resourceService.createTemplate(metaDataIn, templateIn, "some webserver", mockUser);
        verify(mockWebServerPersistenceService).findWebServerByName("some webserver");
        verify(mockWebServerPersistenceService).uploadWebServerConfigTemplate(any(UploadWebServerTemplateRequest.class), eq("/conf/httpd.conf"), eq("user-id"));
    }

    @Test
    public void testCreateGroupedWebServersTemplate() {
        final InputStream metaDataIn = this.getClass().getClassLoader()
                .getResourceAsStream("resource-service-test-files/create-grouped-ws-template-test-metadata.json");
        final InputStream templateIn = this.getClass().getClassLoader()
                .getResourceAsStream("resource-service-test-files/httpd.conf.tpl");

        final Set<WebServer> webServerSet = new HashSet<>();
        webServerSet.add(mock(WebServer.class));
        webServerSet.add(mock(WebServer.class));
        final Group mockGroup = mock(Group.class);
        when(mockGroup.getWebServers()).thenReturn(webServerSet);
        when(mockGroup.getName()).thenReturn("HEALTH CHECK 4.0");
        when(mockGroupPesistenceService.getGroupWithWebServers(eq("HEALTH CHECK 4.0"))).thenReturn(mockGroup);
        User mockUser = mock(User.class);
        when(mockUser.getId()).thenReturn("user-id");
        resourceService.createTemplate(metaDataIn, templateIn, "test-app-name", mockUser);
        verify(mockWebServerPersistenceService, new Times(2)).uploadWebServerConfigTemplate(any(UploadWebServerTemplateRequest.class), eq("/conf/httpd.conf"), eq("user-id"));
        verify(mockGroupPesistenceService).populateGroupWebServerTemplates(eq("HEALTH CHECK 4.0"), anyMap());
    }

    @Test
    public void testCreateAppTemplate() {
        final InputStream metaDataIn = this.getClass().getClassLoader()
                .getResourceAsStream("resource-service-test-files/create-app-template-test-metadata.json");
        final InputStream templateIn = this.getClass().getClassLoader()
                .getResourceAsStream("resource-service-test-files/app.xml.tpl");
        Jvm mockJvm = mock(Jvm.class);
        JpaJvm mockJpaJvm = mock(JpaJvm.class);
        when(mockJvmPersistenceService.findJvmByExactName(anyString())).thenReturn(mockJvm);
        when(mockJvmPersistenceService.getJpaJvm(any(Identifier.class), anyBoolean())).thenReturn(mockJpaJvm);
        User mockUser = mock(User.class);
        when(mockUser.getId()).thenReturn("user-id");
        resourceService.createTemplate(metaDataIn, templateIn, "test-jvm-name", mockUser);
        verify(mockAppPersistenceService).getApplication("some application");
        verify(mockAppPersistenceService).uploadAppTemplate(any(UploadAppTemplateRequest.class), any(JpaJvm.class));
    }

    @Test
    public void testCreateAppTemplateBinary() {
        final InputStream metaDataIn = this.getClass().getClassLoader()
                .getResourceAsStream("resource-service-test-files/create-app-template-test-metadata-binary.json");
        final InputStream templateIn = this.getClass().getClassLoader()
                .getResourceAsStream("resource-service-test-files/app.xml.tpl");
        Jvm mockJvm = mock(Jvm.class);
        JpaJvm mockJpaJvm = mock(JpaJvm.class);
        RepositoryFileInformation mockRepoFilInfo = mock(RepositoryFileInformation.class);
        Path mockPath = mock(Path.class);
        when(mockPath.toString()).thenReturn("./anyPath");
        when(mockRepoFilInfo.getPath()).thenReturn(mockPath);
        when(mockJvmPersistenceService.findJvmByExactName(anyString())).thenReturn(mockJvm);
        when(mockJvmPersistenceService.getJpaJvm(any(Identifier.class), anyBoolean())).thenReturn(mockJpaJvm);
        when(mockPrivateApplicationService.uploadWebArchiveData(any(UploadWebArchiveRequest.class))).thenReturn(mockRepoFilInfo);
        User mockUser = mock(User.class);
        when(mockUser.getId()).thenReturn("user-id");
        resourceService.createTemplate(metaDataIn, templateIn, "test-jvm-name", mockUser);
        verify(mockAppPersistenceService).getApplication("some application");
        verify(mockAppPersistenceService).uploadAppTemplate(any(UploadAppTemplateRequest.class), any(JpaJvm.class));
    }

    @Test
    public void testCreateGroupedAppsTemplate() {
        final InputStream metaDataIn = this.getClass().getClassLoader()
                .getResourceAsStream("resource-service-test-files/create-grouped-apps-template-test-metadata.json");
        final InputStream templateIn = this.getClass().getClassLoader()
                .getResourceAsStream("resource-service-test-files/httpd.conf.tpl");

        final List<Application> appList = new ArrayList<>();
        final Application mockApp = mock(Application.class);
        final Application mockApp2 = mock(Application.class);
        when(mockApp.getName()).thenReturn("test-app-name");
        when(mockApp2.getName()).thenReturn("test-app-name2");
        appList.add(mockApp);
        appList.add(mockApp2);
        final Group mockGroup = mock(Group.class);
        Set<Jvm> jvmSet = new HashSet<>();
        Jvm mockJvm = mock(Jvm.class);
        jvmSet.add(mockJvm);
        when(mockGroup.getJvms()).thenReturn(jvmSet);
        when(mockJvm.getJvmName()).thenReturn("test-jvm-name");
        when(mockAppPersistenceService.findApplicationsBelongingTo(eq("HEALTH CHECK 4.0"))).thenReturn(appList);
        when(mockGroupPesistenceService.getGroup(eq("HEALTH CHECK 4.0"))).thenReturn(mockGroup);
        User mockUser = mock(User.class);
        when(mockUser.getId()).thenReturn("user-id");
        resourceService.createTemplate(metaDataIn, templateIn, "test-app-name", mockUser);
        verify(mockAppPersistenceService).uploadAppTemplate(any(UploadAppTemplateRequest.class), any(JpaJvm.class));
        verify(mockGroupPesistenceService).populateGroupAppTemplate(anyString(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    public void testGenerateResourceFile() {
        File httpdTemplate = new File("../toc-template/src/test/resources/HttpdConfTemplate.tpl");
        try {
            List<Group> groups = new ArrayList<>();
            List<Jvm> jvms = new ArrayList<>();
            List<WebServer> webServers = new ArrayList<>();
            List<Application> applications = new ArrayList<>();
            Group group = new Group(new Identifier<Group>(1111L),
                    "groupName",
                    new HashSet<>(jvms),
                    new HashSet<>(webServers),
                    new CurrentGroupState(new Identifier<Group>(1111L), GroupState.GRP_STOPPED, DateTime.now()),
                    new HashSet<History>(),
                    new HashSet<>(applications));
            groups.add(group);
            applications.add(new Application(new Identifier<Application>(111L), "hello-world-1", "d:/stp/app/archive", "/hello-world-1", group, true, true, false, "testWar.war"));
            applications.add(new Application(new Identifier<Application>(222L), "hello-world-2", "d:/stp/app/archive", "/hello-world-2", group, true, true, false, "testWar.war"));
            applications.add(new Application(new Identifier<Application>(333L), "hello-world-3", "d:/stp/app/archive", "/hello-world-3", group, true, true, false, "testWar.war"));
            WebServer webServer = new WebServer(new Identifier<WebServer>(1L), groups, "Apache2.4", "localhost", 80, 443,
                    new com.siemens.cto.aem.common.domain.model.path.Path("/statusPath"), new FileSystemPath("D:/stp/app/data/httpd//httpd.conf"),
                    new com.siemens.cto.aem.common.domain.model.path.Path("./"), new com.siemens.cto.aem.common.domain.model.path.Path("htdocs"), WebServerReachableState.WS_UNREACHABLE, "");
            webServers.add(webServer);
            jvms.add(new Jvm(new Identifier<Jvm>(11L), "tc1", "usmlvv1ctoGenerateMe", new HashSet<>(groups), 11010, 11011, 11012, -1, 11013,
                    new com.siemens.cto.aem.common.domain.model.path.Path("/statusPath"), "EXAMPLE_OPTS=%someEvn%/someVal", JvmState.JVM_STOPPED, "", null, null, null, null));
            jvms.add(new Jvm(new Identifier<Jvm>(22L), "tc2", "usmlvv1ctoGenerateMe", new HashSet<>(groups), 11020, 11021, 11022, -1, 11023,
                    new com.siemens.cto.aem.common.domain.model.path.Path("/statusPath"), "EXAMPLE_OPTS=%someEvn%/someVal", JvmState.JVM_STOPPED, "", null, null, null, null));

            when(mockGroupPesistenceService.getGroups()).thenReturn(groups);
            when(mockAppPersistenceService.findApplicationsBelongingTo(anyString())).thenReturn(applications);
            when(mockJvmPersistenceService.getJvmsAndWebAppsByGroupName(anyString())).thenReturn(jvms);
            when(mockWebServerPersistenceService.getWebServersByGroupName(anyString())).thenReturn(webServers);

            System.setProperty(ApplicationProperties.PROPERTIES_ROOT_PATH,
                    this.getClass().getClassLoader().getResource("vars.properties").getPath().replace("vars.properties", ""));

            final ResourceGroup resourceGroup = resourceService.generateResourceGroup();
            String output = resourceService.generateResourceFile(ResourceGroovyMethods.getText(httpdTemplate), resourceGroup, webServer);

            String expected = ResourceGroovyMethods.getText(new File("../toc-template/src/test/resources/HttpdConfTemplate-EXPECTED.conf"));
            expected = expected.replaceAll("\\r", "").replaceAll("\\n", "");
            output = output.replaceAll("\\r", "").replaceAll("\\n", "");
            String diff = StringUtils.difference(output, expected);
            assertEquals(expected, output);
        } catch(IOException e ) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testCheckFileExists() throws IOException {
        final String testGroup = "testGroup";
        final String testFile = "testFile";
        final String testJvm = "testJvm";
        final String testApp = "testApp";
        final String testWebServer = "testWebServer";
        Map<String, String> expectedMap = new HashMap<>();
        Map<String, String> result = resourceService.checkFileExists(null, null, null, null, null);
        expectedMap.put("fileName", null);
        expectedMap.put("exists", "false");
        assertEquals(expectedMap, result);
        result = resourceService.checkFileExists(null, null, null, null, "");
        expectedMap.put("fileName", new String());
        expectedMap.put("exists", "false");
        assertEquals(expectedMap, result);
        result = resourceService.checkFileExists(testGroup, null, null, null, null);
        expectedMap.put("fileName", null);
        expectedMap.put("exists", "false");
        assertEquals(expectedMap, result);
        result = resourceService.checkFileExists(null, null, null, null, testFile);
        expectedMap.put("fileName", testFile);
        expectedMap.put("exists", "false");
        assertEquals(expectedMap, result);
        result = resourceService.checkFileExists(testGroup, null, null, null, testFile);
        expectedMap.put("fileName", testFile);
        expectedMap.put("exists", "false");
        assertEquals(expectedMap, result);

        when(mockGroupPesistenceService.checkGroupJvmResourceFileName(testGroup, testFile)).thenReturn(false);
        when(mockJvmPersistenceService.checkJvmResourceFileName(testGroup, testJvm, testFile)).thenReturn(true);
        result = resourceService.checkFileExists(testGroup, testJvm, null, null, testFile);
        expectedMap.put("fileName", testFile);
        expectedMap.put("exists", "true");
        assertEquals(expectedMap, result);

        when(mockGroupPesistenceService.checkGroupJvmResourceFileName(testGroup, testFile)).thenReturn(false);
        when(mockJvmPersistenceService.checkJvmResourceFileName(testGroup, testJvm, testFile)).thenReturn(false);
        result = resourceService.checkFileExists(testGroup, testJvm, null, null, testFile);
        expectedMap.put("fileName", testFile);
        expectedMap.put("exists", "false");
        assertEquals(expectedMap, result);

        when(mockGroupPesistenceService.checkGroupAppResourceFileName(testGroup, testFile)).thenReturn(false);
        when(mockAppPersistenceService.checkAppResourceFileName(testGroup, testApp, testFile)).thenReturn(false);
        result = resourceService.checkFileExists(testGroup, null, testApp, null, testFile);
        expectedMap.put("fileName", testFile);
        expectedMap.put("exists", "false");
        assertEquals(expectedMap, result);

        when(mockGroupPesistenceService.checkGroupAppResourceFileName(testGroup, testFile)).thenReturn(false);
        when(mockAppPersistenceService.checkAppResourceFileName(testGroup, testApp, testFile)).thenReturn(true);
        result = resourceService.checkFileExists(testGroup, null, testApp, null, testFile);
        expectedMap.put("fileName", testFile);
        expectedMap.put("exists", "true");
        assertEquals(expectedMap, result);

        when(mockGroupPesistenceService.checkGroupWebServerResourceFileName(testGroup, testFile)).thenReturn(false);
        when(mockWebServerPersistenceService.checkWebServerResourceFileName(testGroup, testWebServer, testFile)).thenReturn(false);
        result = resourceService.checkFileExists(testGroup, null, null, testWebServer, testFile);
        expectedMap.put("fileName", testFile);
        expectedMap.put("exists", "false");
        assertEquals(expectedMap, result);

        when(mockGroupPesistenceService.checkGroupWebServerResourceFileName(testGroup, testFile)).thenReturn(false);
        when(mockWebServerPersistenceService.checkWebServerResourceFileName(testGroup, testWebServer, testFile)).thenReturn(true);
        result = resourceService.checkFileExists(testGroup, null, null, testWebServer, testFile);
        expectedMap.put("fileName", testFile);
        expectedMap.put("exists", "true");
        assertEquals(expectedMap, result);
    }

    @Test
    public void testCreateJvmResource() {
        ResourceTemplateMetaData metaData = new ResourceTemplateMetaData();
        metaData.setContentType(ContentType.APPLICATION_BINARY.contentTypeStr);
        metaData.setEntity(new Entity());

        resourceService.createJvmResource(metaData, new ByteArrayInputStream("someData".getBytes()), "someJvm");
        verify(mockJvmPersistenceService).uploadJvmTemplateXml(any(UploadJvmConfigTemplateRequest.class));
    }

    @Test
    public void testCreateGroupLevelJvmResource() throws IOException {
        ResourceTemplateMetaData metaData = new ResourceTemplateMetaData();
        metaData.setContentType(ContentType.APPLICATION_BINARY.contentTypeStr);
        final Entity entity = new Entity();
        entity.setGroup("someGroup");
        metaData.setEntity(entity);

        final Group mockGroup = mock(Group.class);
        final Jvm mockJvm = mock(Jvm.class);
        final Set<Jvm> mockJvmSet = new HashSet<>();
        mockJvmSet.add(mockJvm);
        when(mockGroup.getJvms()).thenReturn(mockJvmSet);
        when(mockGroupPesistenceService.getGroup(eq("someGroup"))).thenReturn(mockGroup);

        resourceService.createGroupLevelJvmResource(metaData, new ByteArrayInputStream("someData".getBytes()), "someGroup");
        verify(mockJvmPersistenceService).uploadJvmTemplateXml(any(UploadJvmConfigTemplateRequest.class));
        verify(mockGroupPesistenceService).populateGroupJvmTemplates(eq("someGroup"), anyList());
    }

    @Test
    public void testCreateWebServerResource() {
        ResourceTemplateMetaData metaData = new ResourceTemplateMetaData();
        metaData.setContentType(ContentType.APPLICATION_BINARY.contentTypeStr);
        metaData.setEntity(new Entity());
        metaData.setDeployPath("c:\\somewhere");

        final WebServer mockWebServer = mock(WebServer.class);
        when(mockWebServerPersistenceService.findWebServerByName(anyString())).thenReturn(mockWebServer);

        resourceService.createWebServerResource(metaData, new ByteArrayInputStream("someData".getBytes()), "someWebServer",
                new User("jedi"));
        verify(mockWebServerPersistenceService).uploadWebServerConfigTemplate(any(UploadWebServerTemplateRequest.class),
                anyString(), anyString());
    }

    @Test
    public void testCreateGroupLevelWebServerResource() throws IOException {
        ResourceTemplateMetaData metaData = new ResourceTemplateMetaData();
        metaData.setContentType(ContentType.APPLICATION_BINARY.contentTypeStr);
        final Entity entity = new Entity();
        entity.setGroup("someGroup");
        metaData.setEntity(entity);
        metaData.setDeployPath("c:\\somewhere");

        final Group mockGroup = mock(Group.class);
        final WebServer mockWebServer = mock(WebServer.class);
        final Set<WebServer> mockWebServerSet = new HashSet<>();
        mockWebServerSet.add(mockWebServer);
        when(mockGroup.getWebServers()).thenReturn(mockWebServerSet);
        when(mockGroupPesistenceService.getGroupWithWebServers("someGroup")).thenReturn(mockGroup);

        resourceService.createGroupLevelWebServerResource(metaData, new ByteArrayInputStream("someData".getBytes()),
                "someGroup", new User("Jedi"));

        verify(mockWebServerPersistenceService).uploadWebServerConfigTemplate(any(UploadWebServerTemplateRequest.class), anyString(), anyString());
        verify(mockGroupPesistenceService).populateGroupWebServerTemplates(anyString(), anyMap());
    }

    @Test
    public void testCreateAppResource() {
        ResourceTemplateMetaData metaData = new ResourceTemplateMetaData();
        metaData.setContentType(ContentType.APPLICATION_BINARY.contentTypeStr);

        resourceService.createAppResource(metaData, new ByteArrayInputStream("someData".getBytes()), "someJvm",
                "someApp");

        final Jvm mockJvm = mock(Jvm.class);
        when(mockJvmPersistenceService.findJvmByExactName("someJvm")).thenReturn(mockJvm);
        verify(mockAppPersistenceService).getApplication(anyString());
        verify(mockAppPersistenceService).uploadAppTemplate(any(UploadAppTemplateRequest.class), any(JpaJvm.class));
    }

    @Test
    public void testCreateGroupedLevelAppResource() throws IOException {
        ResourceTemplateMetaData metaData = new ResourceTemplateMetaData();
        metaData.setContentType(ContentType.APPLICATION_BINARY.contentTypeStr);
        final Entity entity = new Entity();
        entity.setGroup("someGroup");
        entity.setDeployToJvms(true);
        metaData.setEntity(entity);

        final Group mockGroup = mock(Group.class);

        final Jvm mockJvm = mock(Jvm.class);
        final Set<Jvm> mockJvmSet = new HashSet<>();
        mockJvmSet.add(mockJvm);

        when(mockGroup.getJvms()).thenReturn(mockJvmSet);

        final Application mockApplication = mock(Application.class);
        when(mockApplication.getName()).thenReturn("someApp");
        final List<Application> mockAppList = new ArrayList<>();
        mockAppList.add(mockApplication);
        when(mockAppPersistenceService.findApplicationsBelongingTo(anyString())).thenReturn(mockAppList);
        when(mockGroupPesistenceService.getGroup(anyString())).thenReturn(mockGroup);

        resourceService.createGroupedLevelAppResource(metaData, new ByteArrayInputStream("someData".getBytes()), "someApp");
        verify(mockJvmPersistenceService).getJpaJvm(any(Identifier.class), eq(false));
        verify(mockAppPersistenceService).uploadAppTemplate(any(UploadAppTemplateRequest.class), any(JpaJvm.class));
        verify(mockGroupPesistenceService).populateGroupAppTemplate(eq("someGroup"), eq("someApp"), anyString(), anyString(),
                anyString());
        verify(mockAppPersistenceService, new Times(0)).updateWarInfo(eq("someApp"), anyString(), anyString());
    }

    @Test
    public void testCreateGroupedLevelAppWarResource() throws IOException {
        ResourceTemplateMetaData metaData = new ResourceTemplateMetaData();
        metaData.setContentType(ContentType.APPLICATION_BINARY.contentTypeStr);
        final Entity entity = new Entity();
        entity.setGroup("someGroup");
        entity.setDeployToJvms(true);
        metaData.setEntity(entity);

        final Group mockGroup = mock(Group.class);

        final Jvm mockJvm = mock(Jvm.class);
        final Set<Jvm> mockJvmSet = new HashSet<>();
        mockJvmSet.add(mockJvm);

        when(mockGroup.getJvms()).thenReturn(mockJvmSet);

        final Application mockApplication = mock(Application.class);
        when(mockApplication.getName()).thenReturn("someApp");
        final List<Application> mockAppList = new ArrayList<>();
        mockAppList.add(mockApplication);
        when(mockAppPersistenceService.findApplicationsBelongingTo(anyString())).thenReturn(mockAppList);
        when(mockGroupPesistenceService.getGroup(anyString())).thenReturn(mockGroup);

        final RepositoryFileInformation mockRepositoryFileInformation = mock(RepositoryFileInformation.class);
        final Path mockPath = mock(Path.class);
        when(mockPath.toString()).thenReturn("app.war");
        when(mockRepositoryFileInformation.getPath()).thenReturn(mockPath);
        when(mockPrivateApplicationService.uploadWebArchiveData(any(UploadWebArchiveRequest.class)))
                .thenReturn(mockRepositoryFileInformation);
        when(mockAppPersistenceService.getApplication(eq("someApp"))).thenReturn(mockApplication);

        resourceService.createGroupedLevelAppResource(metaData, new ByteArrayInputStream("someData".getBytes()), "someApp");
        verify(mockJvmPersistenceService).getJpaJvm(any(Identifier.class), eq(false));
        verify(mockAppPersistenceService).uploadAppTemplate(any(UploadAppTemplateRequest.class), any(JpaJvm.class));
        verify(mockGroupPesistenceService).populateGroupAppTemplate(eq("someGroup"), eq("someApp"), anyString(), anyString(),
                anyString());
        verify(mockAppPersistenceService).updateWarInfo(eq("someApp"), anyString(), anyString());
    }

    @Test
    public void testUploadExternalProperties() {
        InputStream mockInputStream = mock(InputStream.class);

        resourceService.uploadExternalProperties("external.properties", mockInputStream);

        verify(mockResourcePersistenceService).createResource(anyLong(), anyLong(), anyLong(), eq(EntityType.EXT_PROPERTIES), eq("external.properties"), eq(mockInputStream));
    }

    @Test
    public void testGetExternalProperties() {
        Properties result = resourceService.getExternalProperties();
        assertTrue(result.isEmpty());
    }
}
