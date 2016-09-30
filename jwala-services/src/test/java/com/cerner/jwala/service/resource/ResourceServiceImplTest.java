package com.cerner.jwala.service.resource;

import com.cerner.jwala.common.domain.model.app.Application;
import com.cerner.jwala.common.domain.model.app.ApplicationControlOperation;
import com.cerner.jwala.common.domain.model.group.CurrentGroupState;
import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.group.GroupState;
import com.cerner.jwala.common.domain.model.group.History;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.domain.model.jvm.JvmState;
import com.cerner.jwala.common.domain.model.path.FileSystemPath;
import com.cerner.jwala.common.domain.model.resource.*;
import com.cerner.jwala.common.domain.model.user.User;
import com.cerner.jwala.common.domain.model.webserver.WebServer;
import com.cerner.jwala.common.domain.model.webserver.WebServerReachableState;
import com.cerner.jwala.common.exception.InternalErrorException;
import com.cerner.jwala.common.exec.CommandOutput;
import com.cerner.jwala.common.exec.ExecCommand;
import com.cerner.jwala.common.exec.ExecReturnCode;
import com.cerner.jwala.common.properties.ApplicationProperties;
import com.cerner.jwala.common.request.app.RemoveWebArchiveRequest;
import com.cerner.jwala.common.request.app.UploadAppTemplateRequest;
import com.cerner.jwala.common.request.app.UploadWebArchiveRequest;
import com.cerner.jwala.common.request.jvm.UploadJvmConfigTemplateRequest;
import com.cerner.jwala.common.request.webserver.UploadWebServerTemplateRequest;
import com.cerner.jwala.control.command.PlatformCommandProvider;
import com.cerner.jwala.control.command.RemoteCommandExecutorImpl;
import com.cerner.jwala.exception.CommandFailureException;
import com.cerner.jwala.files.FileManager;
import com.cerner.jwala.files.RepositoryFileInformation;
import com.cerner.jwala.files.WebArchiveManager;
import com.cerner.jwala.persistence.jpa.domain.JpaJvm;
import com.cerner.jwala.persistence.jpa.domain.resource.config.template.ConfigTemplate;
import com.cerner.jwala.persistence.service.*;
import com.cerner.jwala.service.HistoryService;
import com.cerner.jwala.service.MessagingService;
import com.cerner.jwala.service.app.ApplicationService;
import com.cerner.jwala.service.app.PrivateApplicationService;

import com.cerner.jwala.service.exception.ResourceServiceException;
import com.cerner.jwala.service.resource.impl.ResourceContentGeneratorServiceImpl;
import com.cerner.jwala.service.resource.impl.CreateResourceResponseWrapper;
import com.cerner.jwala.service.resource.impl.ResourceServiceImpl;
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
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.anyList;
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

    @Mock
    private RemoteCommandExecutorImpl mockRemoteCommandExector;

    @Mock
    private MessagingService mockMessagingService;

    @Mock
    private HistoryService mockHistoryService;

    Map<String, ReentrantReadWriteLock> resourceWriteLockMap = new HashMap<>();

    @Before
    public void setup() {
        // It is good practice to start with a clean sheet of paper before each test that is why resourceService is
        // initialized here. This makes sure that unrelated tests don't affect each other.
        MockitoAnnotations.initMocks(this);
        System.setProperty(ApplicationProperties.PROPERTIES_ROOT_PATH, new File(".").getAbsolutePath() + "/src/test/resources");

        ResourceContentGeneratorService resourceContentGeneratorService = new ResourceContentGeneratorServiceImpl(mockGroupPesistenceService,
                mockWebServerPersistenceService, mockJvmPersistenceService, mockAppPersistenceService, mockHistoryService);

        resourceService = new ResourceServiceImpl(mockResourcePersistenceService, mockGroupPesistenceService,
                mockAppPersistenceService, mockJvmPersistenceService, mockWebServerPersistenceService,
                mockPrivateApplicationService, mockResourceDao, mockWebArchiveManager, mockResourceHandler, mockRemoteCommandExector, resourceWriteLockMap,
                resourceContentGeneratorService);

        when(mockJvmPersistenceService.findJvmByExactName(eq("someJvm"))).thenReturn(mock(Jvm.class));

        // emulates uploadIfBinaryData
        final RepositoryFileInformation mockRepositoryFileInformation = mock(RepositoryFileInformation.class);
        final Path mockPath = mock(Path.class);
        when(mockPath.toString()).thenReturn("thePath");
        when(mockRepositoryFileInformation.getPath()).thenReturn(mockPath);
        when(mockPrivateApplicationService.uploadWebArchiveData(any(UploadWebArchiveRequest.class)))
                .thenReturn(mockRepositoryFileInformation);

    }

    @Test
    public void testEncryption() {
        final String encryptedHello = "aGVsbG8=";
        final String clearTextHello = "hello";
        assertEquals(encryptedHello, resourceService.encryptUsingPlatformBean(clearTextHello));
        assertEquals(clearTextHello, resourceService.decryptUsingPlatformBean(encryptedHello));
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
        verify(mockJvmPersistenceService).uploadJvmConfigTemplate(any(UploadJvmConfigTemplateRequest.class));
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
        verify(mockJvmPersistenceService, new Times(2)).uploadJvmConfigTemplate(any(UploadJvmConfigTemplateRequest.class));
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
        File httpdTemplate = new File("../jwala-common/src/test/resources/HttpdConfTemplate.tpl");
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
            applications.add(new Application(new Identifier<Application>(111L), "hello-world-1", "d:/jwala/app/archive", "/hello-world-1", group, true, true, false, "testWar.war"));
            applications.add(new Application(new Identifier<Application>(222L), "hello-world-2", "d:/jwala/app/archive", "/hello-world-2", group, true, true, false, "testWar.war"));
            applications.add(new Application(new Identifier<Application>(333L), "hello-world-3", "d:/jwala/app/archive", "/hello-world-3", group, true, true, false, "testWar.war"));
            WebServer webServer = new WebServer(new Identifier<WebServer>(1L), groups, "Apache2.4", "localhost", 80, 443,
                    new com.cerner.jwala.common.domain.model.path.Path("/statusPath"), new FileSystemPath("D:/jwala/app/data/httpd//httpd.conf"),
                    new com.cerner.jwala.common.domain.model.path.Path("./"), new com.cerner.jwala.common.domain.model.path.Path("htdocs"), WebServerReachableState.WS_UNREACHABLE, "");
            webServers.add(webServer);
            jvms.add(new Jvm(new Identifier<Jvm>(11L), "tc1", "usmlvv1ctoGenerateMe", new HashSet<>(groups), 11010, 11011, 11012, -1, 11013,
                    new com.cerner.jwala.common.domain.model.path.Path("/statusPath"), "EXAMPLE_OPTS=%someEvn%/someVal", JvmState.JVM_STOPPED, "", null, null, null, null));
            jvms.add(new Jvm(new Identifier<Jvm>(22L), "tc2", "usmlvv1ctoGenerateMe", new HashSet<>(groups), 11020, 11021, 11022, -1, 11023,
                    new com.cerner.jwala.common.domain.model.path.Path("/statusPath"), "EXAMPLE_OPTS=%someEvn%/someVal", JvmState.JVM_STOPPED, "", null, null, null, null));

            when(mockGroupPesistenceService.getGroups()).thenReturn(groups);
            when(mockAppPersistenceService.findApplicationsBelongingTo(anyString())).thenReturn(applications);
            when(mockJvmPersistenceService.getJvmsAndWebAppsByGroupName(anyString())).thenReturn(jvms);
            when(mockWebServerPersistenceService.getWebServersByGroupName(anyString())).thenReturn(webServers);

            System.setProperty(ApplicationProperties.PROPERTIES_ROOT_PATH,
                    this.getClass().getClassLoader().getResource("vars.properties").getPath().replace("vars.properties", ""));

            final ResourceGroup resourceGroup = resourceService.generateResourceGroup();
            String output = resourceService.generateResourceFile(ResourceGroovyMethods.getText(httpdTemplate), ResourceGroovyMethods.getText(httpdTemplate), resourceGroup, webServer);

            String expected = ResourceGroovyMethods.getText(new File("../jwala-common/src/test/resources/HttpdConfTemplate-EXPECTED.conf"));
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
    public void testGetExternalProperties() {
        Properties result = resourceService.getExternalProperties();
        assertEquals(1, result.size());
    }

    @Test
    public void testGetResourcesContent() {
        ResourceIdentifier identifier = mock(ResourceIdentifier.class);
        ConfigTemplate mockConfigTemplate = mock(ConfigTemplate.class);
        when(mockConfigTemplate.getMetaData()).thenReturn("{}");
        when(mockConfigTemplate.getTemplateContent()).thenReturn("key=value");
        when(mockResourceHandler.fetchResource(any(ResourceIdentifier.class))).thenReturn(mockConfigTemplate);

        ResourceContent result = resourceService.getResourceContent(identifier);
        assertEquals("{}", result.getMetaData());
        assertEquals("key=value", result.getContent());
    }

    @Test
    public void testGetResourceContentWhenNull() {
        ResourceIdentifier identifier = mock(ResourceIdentifier.class);
        when(mockResourceHandler.fetchResource(any(ResourceIdentifier.class))).thenReturn(null);

        ResourceContent result = resourceService.getResourceContent(identifier);
        assertNull(result);
    }

    @Test
    public void testUpdateResourceContent() {
        ResourceIdentifier.Builder idBuilder = new ResourceIdentifier.Builder();
        ResourceIdentifier identifier = idBuilder.setResourceName("external.properties").build();
        ConfigTemplate mockConfigTemplate = mock(ConfigTemplate.class);
        when(mockConfigTemplate.getTemplateContent()).thenReturn("newkey=newvalue");
        when(mockResourceHandler.fetchResource(any(ResourceIdentifier.class))).thenReturn(mockConfigTemplate);

        String result = resourceService.updateResourceContent(identifier, "newkey=newvalue");
        assertEquals("newkey=newvalue", result);
        verify(mockResourceDao).updateResource(eq(identifier), eq(EntityType.EXT_PROPERTIES), eq("newkey=newvalue"));
    }

    @Test
    public void testPreviewResourceContent() {
        String result = resourceService.previewResourceContent(null, null, "key=value");
        assertEquals("key=value", result);
    }

    @Test
    public void testGetExternalPropertiesFile() {
        List<String> resultList = new ArrayList<>();
        resultList.add("external.properties");
        ResourceIdentifier.Builder idBuilder = new ResourceIdentifier.Builder();
        ResourceIdentifier resourceId = idBuilder.setResourceName("external.properties").build();
        when(mockResourceDao.getResourceNames(any(ResourceIdentifier.class), any(EntityType.class))).thenReturn(resultList);

        List<String> result = resourceService.getResourceNames(resourceId);
        verify(mockResourceDao).getResourceNames(eq(resourceId), eq(EntityType.EXT_PROPERTIES));
        assertEquals("external.properties", result.get(0));
    }

    @Test
    public void testDeployResourceTemplateToHost() throws CommandFailureException {
        ResourceIdentifier mockResourceIdentifier = mock(ResourceIdentifier.class);
        ConfigTemplate mockConfigTemplate = mock(ConfigTemplate.class);

        when(mockResourceHandler.fetchResource(any(ResourceIdentifier.class))).thenReturn(mockConfigTemplate);
        when(mockConfigTemplate.getMetaData()).thenReturn("{\"deployPath\":\"c:/fake/path\", \"deployFileName\":\"deploy-me.txt\"}");
        when(mockConfigTemplate.getTemplateContent()).thenReturn("key=value");
        when(mockRemoteCommandExector.executeRemoteCommand(anyString(), anyString(), eq(ApplicationControlOperation.SECURE_COPY), any(PlatformCommandProvider.class), anyString(), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(0), "SUCCESS", ""));
        when(mockRemoteCommandExector.executeRemoteCommand(anyString(), anyString(), eq(ApplicationControlOperation.CHECK_FILE_EXISTS), any(PlatformCommandProvider.class), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(0), "SUCCESS", ""));
        when(mockRemoteCommandExector.executeRemoteCommand(anyString(), anyString(), eq(ApplicationControlOperation.BACK_UP_FILE), any(PlatformCommandProvider.class), anyString(), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(0), "SUCCESS", ""));
        when(mockRemoteCommandExector.executeRemoteCommand(anyString(), anyString(), eq(ApplicationControlOperation.CREATE_DIRECTORY), any(PlatformCommandProvider.class), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(0), "SUCCESS", ""));

        CommandOutput result = resourceService.deployTemplateToHost("external.properties", "test-host", mockResourceIdentifier);
        assertEquals(new Integer(0), result.getReturnCode().getReturnCode());
    }

    @Test
    public void testDeployResourceTemplateToHostFailsBackup() throws CommandFailureException {
        ResourceIdentifier mockResourceIdentifier = mock(ResourceIdentifier.class);
        ConfigTemplate mockConfigTemplate = mock(ConfigTemplate.class);

        when(mockResourceHandler.fetchResource(any(ResourceIdentifier.class))).thenReturn(mockConfigTemplate);
        when(mockConfigTemplate.getMetaData()).thenReturn("{\"deployPath\":\"c:/fake/path\", \"deployFileName\":\"deploy-me.txt\"}");
        when(mockConfigTemplate.getTemplateContent()).thenReturn("key=value");
        when(mockRemoteCommandExector.executeRemoteCommand(anyString(), anyString(), eq(ApplicationControlOperation.SECURE_COPY), any(PlatformCommandProvider.class), anyString(), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(0), "SUCCESS", ""));
        when(mockRemoteCommandExector.executeRemoteCommand(anyString(), anyString(), eq(ApplicationControlOperation.CHECK_FILE_EXISTS), any(PlatformCommandProvider.class), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(0), "SUCCESS", ""));
        when(mockRemoteCommandExector.executeRemoteCommand(anyString(), anyString(), eq(ApplicationControlOperation.BACK_UP_FILE), any(PlatformCommandProvider.class), anyString(), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(1), "", "FAILED BACK UP BUT CONTINUE WITH COPY"));
        when(mockRemoteCommandExector.executeRemoteCommand(anyString(), anyString(), eq(ApplicationControlOperation.CREATE_DIRECTORY), any(PlatformCommandProvider.class), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(0), "SUCCESS", ""));

        CommandOutput result = resourceService.deployTemplateToHost("external.properties", "test-host", mockResourceIdentifier);
        assertEquals(new Integer(0), result.getReturnCode().getReturnCode());
    }

    @Test
    public void testDeployResourceTemplateToAllHosts() throws CommandFailureException {
        ResourceIdentifier mockResourceIdentifier = mock(ResourceIdentifier.class);
        ConfigTemplate mockConfigTemplate = mock(ConfigTemplate.class);
        List<Group> groupList = new ArrayList<>();
        Group mockGroup = mock(Group.class);
        groupList.add(mockGroup);
        List<String> hostsList = new ArrayList<>();
        hostsList.add("test-host-1");
        hostsList.add("test-host-2");

        when(mockResourceHandler.fetchResource(any(ResourceIdentifier.class))).thenReturn(mockConfigTemplate);
        when(mockConfigTemplate.getMetaData()).thenReturn("{\"deployPath\":\"c:/fake/path\", \"deployFileName\":\"deploy-me.txt\"}");
        when(mockConfigTemplate.getTemplateContent()).thenReturn("key=value");
        when(mockRemoteCommandExector.executeRemoteCommand(anyString(), anyString(), eq(ApplicationControlOperation.SECURE_COPY), any(PlatformCommandProvider.class), anyString(), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(0), "SUCCESS", ""));
        when(mockRemoteCommandExector.executeRemoteCommand(anyString(), anyString(), eq(ApplicationControlOperation.CHECK_FILE_EXISTS), any(PlatformCommandProvider.class), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(0), "SUCCESS", ""));
        when(mockRemoteCommandExector.executeRemoteCommand(anyString(), anyString(), eq(ApplicationControlOperation.BACK_UP_FILE), any(PlatformCommandProvider.class), anyString(), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(0), "SUCCESS", ""));
        when(mockRemoteCommandExector.executeRemoteCommand(anyString(), anyString(), eq(ApplicationControlOperation.CREATE_DIRECTORY), any(PlatformCommandProvider.class), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(0), "SUCCESS", ""));
        when(mockGroupPesistenceService.getGroups()).thenReturn(groupList);
        when(mockGroupPesistenceService.getHosts(anyString())).thenReturn(hostsList);
        when(mockGroup.getName()).thenReturn("test-group");

        resourceService.deployTemplateToAllHosts("external.properties", mockResourceIdentifier);
        verify(mockRemoteCommandExector, times(2)).executeRemoteCommand(anyString(), anyString(), eq(ApplicationControlOperation.SECURE_COPY), any(PlatformCommandProvider.class), anyString(), anyString());
        verify(mockRemoteCommandExector, times(2)).executeRemoteCommand(anyString(), anyString(), eq(ApplicationControlOperation.CHECK_FILE_EXISTS), any(PlatformCommandProvider.class), anyString());
        verify(mockRemoteCommandExector, times(2)).executeRemoteCommand(anyString(), anyString(), eq(ApplicationControlOperation.BACK_UP_FILE), any(PlatformCommandProvider.class), anyString(), anyString());
        verify(mockRemoteCommandExector, times(2)).executeRemoteCommand(anyString(), anyString(), eq(ApplicationControlOperation.CREATE_DIRECTORY), any(PlatformCommandProvider.class), anyString());
    }

    @Test (expected = InternalErrorException.class)
    public void testDeployResourceTemplateToAllHostsFails() throws CommandFailureException {
        ResourceIdentifier mockResourceIdentifier = mock(ResourceIdentifier.class);
        ConfigTemplate mockConfigTemplate = mock(ConfigTemplate.class);
        List<Group> groupList = new ArrayList<>();
        Group mockGroup = mock(Group.class);
        groupList.add(mockGroup);
        List<String> hostsList = new ArrayList<>();
        hostsList.add("test-host-1");
        hostsList.add("test-host-2");

        when(mockResourceHandler.fetchResource(any(ResourceIdentifier.class))).thenReturn(mockConfigTemplate);
        when(mockConfigTemplate.getMetaData()).thenReturn("{\"deployPath\":\"c:/fake/path\", \"deployFileName\":\"deploy-me.txt\"}");
        when(mockConfigTemplate.getTemplateContent()).thenReturn("key=value");
        when(mockRemoteCommandExector.executeRemoteCommand(anyString(), anyString(), eq(ApplicationControlOperation.SECURE_COPY), any(PlatformCommandProvider.class), anyString(), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(1), "", "Command Failed"));
        when(mockRemoteCommandExector.executeRemoteCommand(anyString(), anyString(), eq(ApplicationControlOperation.CHECK_FILE_EXISTS), any(PlatformCommandProvider.class), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(0), "SUCCESS", ""));
        when(mockRemoteCommandExector.executeRemoteCommand(anyString(), anyString(), eq(ApplicationControlOperation.BACK_UP_FILE), any(PlatformCommandProvider.class), anyString(), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(0), "SUCCESS", ""));
        when(mockGroupPesistenceService.getGroups()).thenReturn(groupList);
        when(mockGroupPesistenceService.getHosts(anyString())).thenReturn(hostsList);
        when(mockGroup.getName()).thenReturn("test-group");

        resourceService.deployTemplateToAllHosts("external.properties", mockResourceIdentifier);
    }

    @Test (expected = InternalErrorException.class)
    public void testDeployResourceTemplateToHostThrowsCommandFailureException() throws CommandFailureException {
        ResourceIdentifier mockResourceIdentifier = mock(ResourceIdentifier.class);
        ConfigTemplate mockConfigTemplate = mock(ConfigTemplate.class);

        when(mockResourceHandler.fetchResource(any(ResourceIdentifier.class))).thenReturn(mockConfigTemplate);
        when(mockConfigTemplate.getMetaData()).thenReturn("{\"deployPath\":\"c:/fake/path\", \"deployFileName\":\"deploy-me.txt\"}");
        when(mockConfigTemplate.getTemplateContent()).thenReturn("key=value");
        when(mockRemoteCommandExector.executeRemoteCommand(anyString(), anyString(), eq(ApplicationControlOperation.SECURE_COPY), any(PlatformCommandProvider.class), anyString(), anyString())).thenThrow(new CommandFailureException(new ExecCommand("Failed command"), new Throwable()));
        when(mockRemoteCommandExector.executeRemoteCommand(anyString(), anyString(), eq(ApplicationControlOperation.CHECK_FILE_EXISTS), any(PlatformCommandProvider.class), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(0), "SUCCESS", ""));
        when(mockRemoteCommandExector.executeRemoteCommand(anyString(), anyString(), eq(ApplicationControlOperation.BACK_UP_FILE), any(PlatformCommandProvider.class), anyString(), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(0), "SUCCESS", ""));
        when(mockRemoteCommandExector.executeRemoteCommand(anyString(), anyString(), eq(ApplicationControlOperation.CREATE_DIRECTORY), any(PlatformCommandProvider.class), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(0), "SUCCESS", ""));

        resourceService.deployTemplateToHost("external.properties", "test-host", mockResourceIdentifier);
    }

    @Test
    public void testUploadResource() {
        final RepositoryFileInformation mockRepositoryFileInformation = mock(RepositoryFileInformation.class);
        final Path mockPath = mock(Path.class);
        when(mockPath.toString()).thenReturn("thePath");
        when(mockRepositoryFileInformation.getPath()).thenReturn(mockPath);
        when(mockPrivateApplicationService.uploadWebArchiveData(any(UploadWebArchiveRequest.class)))
                .thenReturn(mockRepositoryFileInformation);
        assertEquals("thePath", resourceService.uploadResource(mock(ResourceTemplateMetaData.class), new ByteArrayInputStream("data".getBytes())));
    }

    @Test
    public void testGetExternalPropertiesAsFile() throws IOException {

        // test for an existing external properties file
        when(mockResourceDao.getResourceNames(any(ResourceIdentifier.class), any(EntityType.class))).thenReturn(new ArrayList<String>());
        boolean exceptionThrown = false;
        try {
            resourceService.getExternalPropertiesAsFile();
        } catch (InternalErrorException iee){
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);

        // now test the success case
        final ArrayList<String> extPropertiesResourceNames = new ArrayList<>();
        extPropertiesResourceNames.add("external.properties");
        final ConfigTemplate extPropertiesConfigTemplate = new ConfigTemplate();
        extPropertiesConfigTemplate.setMetaData("{\"deployPath\":\"c:/fake/path\", \"deployFileName\":\"external.properties\"}");
        extPropertiesConfigTemplate.setTemplateContent("key=value");

        when(mockResourceDao.getResourceNames(any(ResourceIdentifier.class), any(EntityType.class))).thenReturn(extPropertiesResourceNames);
        when(mockResourceHandler.fetchResource(any(ResourceIdentifier.class))).thenReturn(extPropertiesConfigTemplate);
        when(mockGroupPesistenceService.getGroups()).thenReturn(new ArrayList<Group>());

        File result = resourceService.getExternalPropertiesAsFile();
        assertTrue(result.length() > 0);
        assertTrue(result.delete());
    }

    @Test
    public void testGetExternalPropertiesAsString() {
        String result = resourceService.getExternalPropertiesAsString();
        assertEquals("newkey=newvalue\n", result);
    }

    @Test
    public void testDeleteGroupLevelAppResources() throws IOException {
        List<String> templateList = new ArrayList<>();
        templateList.add("test.war");
        List<Jvm> jvms = new ArrayList<>();
        Jvm mockJvm = mock(Jvm.class);
        jvms.add(mockJvm);
        Application mockApp = mock(Application.class);
        RepositoryFileInformation mockRepositoryFileInformation = mock(RepositoryFileInformation.class);
        when(mockResourceDao.deleteGroupLevelAppResources(anyString(), anyString(), anyList())).thenReturn(1);
        when(mockJvmPersistenceService.getJvmsByGroupName(anyString())).thenReturn(jvms);
        when(mockResourceDao.deleteAppResources(anyList(), anyString(), anyString())).thenReturn(1);
        when(mockAppPersistenceService.getApplication(anyString())).thenReturn(mockApp);
        when(mockAppPersistenceService.deleteWarInfo(anyString())).thenReturn(mockApp);
        when(mockWebArchiveManager.remove(any(RemoveWebArchiveRequest.class))).thenReturn(mockRepositoryFileInformation);
        when(mockRepositoryFileInformation.getType()).thenReturn(RepositoryFileInformation.Type.DELETED);
        assertEquals(1, resourceService.deleteGroupLevelAppResources("test-app", "test-group", templateList));
    }

    @Test
    public void testDeleteGroupLevelAppResourcesFail() throws IOException {
        List<String> templateList = new ArrayList<>();
        templateList.add("test.war");
        List<Jvm> jvms = new ArrayList<>();
        Jvm mockJvm = mock(Jvm.class);
        jvms.add(mockJvm);
        Application mockApp = mock(Application.class);
        RepositoryFileInformation mockRepositoryFileInformation = mock(RepositoryFileInformation.class);
        when(mockResourceDao.deleteGroupLevelAppResources(anyString(), anyString(), anyList())).thenReturn(1);
        when(mockJvmPersistenceService.getJvmsByGroupName(anyString())).thenReturn(jvms);
        when(mockResourceDao.deleteAppResources(anyList(), anyString(), anyString())).thenReturn(1);
        when(mockAppPersistenceService.getApplication(anyString())).thenReturn(mockApp);
        when(mockAppPersistenceService.deleteWarInfo(anyString())).thenReturn(mockApp);
        when(mockWebArchiveManager.remove(any(RemoveWebArchiveRequest.class))).thenThrow(IOException.class);
        when(mockRepositoryFileInformation.getType()).thenReturn(RepositoryFileInformation.Type.DELETED);
        assertEquals(1, resourceService.deleteGroupLevelAppResources("test-app", "test-group", templateList));
    }

    @Test
    public void testCreateResource() {
        ResourceIdentifier resourceIdentifier = mock(ResourceIdentifier.class);
        ResourceTemplateMetaData resourceTemplateMetaData = mock(ResourceTemplateMetaData.class);
        InputStream inputStream = mock(InputStream.class);
        CreateResourceResponseWrapper createResourceResponseWrapper = mock(CreateResourceResponseWrapper.class);
        when(mockResourceHandler.createResource(eq(resourceIdentifier), eq(resourceTemplateMetaData), anyString())).thenReturn(createResourceResponseWrapper);
        assertEquals(createResourceResponseWrapper, resourceService.createResource(resourceIdentifier, resourceTemplateMetaData, inputStream));
    }

    @Test (expected = ResourceServiceException.class)
    public void testCreateResourceFail() {
        ResourceIdentifier resourceIdentifier = mock(ResourceIdentifier.class);
        ResourceTemplateMetaData resourceTemplateMetaData = mock(ResourceTemplateMetaData.class);
        InputStream inputStream = mock(InputStream.class);
        when(mockResourceHandler.createResource(eq(resourceIdentifier), eq(resourceTemplateMetaData), anyString())).thenThrow(ResourceServiceException.class);
        resourceService.createResource(resourceIdentifier, resourceTemplateMetaData, inputStream);
    }

    @Test (expected = InternalErrorException.class)
    public void testDeployTemplateToHostFail() throws CommandFailureException {
        ResourceIdentifier mockResourceIdentifier = mock(ResourceIdentifier.class);
        ConfigTemplate mockConfigTemplate = mock(ConfigTemplate.class);

        when(mockResourceHandler.fetchResource(any(ResourceIdentifier.class))).thenReturn(mockConfigTemplate);
        when(mockConfigTemplate.getMetaData()).thenReturn("\"deployPath\":\"c:/fake/path\", \"deployFileName\":\"deploy-me.txt\"}");
        when(mockConfigTemplate.getTemplateContent()).thenReturn("key=value");
        when(mockRemoteCommandExector.executeRemoteCommand(anyString(), anyString(), eq(ApplicationControlOperation.SECURE_COPY), any(PlatformCommandProvider.class), anyString(), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(0), "SUCCESS", ""));
        when(mockRemoteCommandExector.executeRemoteCommand(anyString(), anyString(), eq(ApplicationControlOperation.CHECK_FILE_EXISTS), any(PlatformCommandProvider.class), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(0), "SUCCESS", ""));
        when(mockRemoteCommandExector.executeRemoteCommand(anyString(), anyString(), eq(ApplicationControlOperation.BACK_UP_FILE), any(PlatformCommandProvider.class), anyString(), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(0), "SUCCESS", ""));
        when(mockRemoteCommandExector.executeRemoteCommand(anyString(), anyString(), eq(ApplicationControlOperation.CREATE_DIRECTORY), any(PlatformCommandProvider.class), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(0), "SUCCESS", ""));

        resourceService.deployTemplateToHost("external.properties", "test-host", mockResourceIdentifier);
    }

    @Test
    public void testDeleteWebServerResource() {
        when(mockResourceDao.deleteWebServerResource(anyString(), anyString())).thenReturn(1);
        assertEquals(1, resourceService.deleteWebServerResource("testFilename", "testWebServer"));
    }

    @Test
    public void testDeleteGroupLevelWebServerResource() {
        when(mockResourceDao.deleteGroupLevelWebServerResource(anyString(), anyString())).thenReturn(1);
        assertEquals(1, resourceService.deleteGroupLevelWebServerResource("testFilename", "testGroupName"));
    }

    @Test
    public void testDeleteJvmResource() {
        when(mockResourceDao.deleteJvmResource(anyString(), anyString())).thenReturn(1);
        assertEquals(1, resourceService.deleteJvmResource("testFilename", "testJvm"));
    }

    @Test
    public void testDeleteGroupLevelJvmResource() {
        when(mockResourceDao.deleteGroupLevelJvmResource(anyString(), anyString())).thenReturn(1);
        assertEquals(1, resourceService.deleteGroupLevelJvmResource("testFilename", "testGroupName"));
    }

    @Test
    public void testDeleteAppResource() {
        when(mockResourceDao.deleteAppResource(anyString(), anyString(), anyString())).thenReturn(1);
        assertEquals(1, resourceService.deleteAppResource("testFilename", "testApp", "testJvm"));
    }

    @Test
    public void testDeleteGroupLevelAppResource() {
        Application application = mock(Application.class);
        when(mockAppPersistenceService.getApplication(anyString())).thenReturn(application);
        when(application.getName()).thenReturn("testApp");
        Group group = mock(Group.class);
        when(application.getGroup()).thenReturn(group);
        when(group.getName()).thenReturn("testGroup");
        when(mockResourceDao.deleteGroupLevelAppResource(anyString(), anyString(), anyString())).thenReturn(1);
        assertEquals(1, resourceService.deleteGroupLevelAppResource("testAppName", "testFilename"));
    }

    @Test
    public void testGetApplicationResourceNames() {
        List<String> strings = new ArrayList<>();
        final String groupName = "testGroupName";
        final String appName = "testAppName";
        when(mockResourcePersistenceService.getApplicationResourceNames(eq(groupName), eq(appName))).thenReturn(strings);
        assertEquals(strings, resourceService.getApplicationResourceNames(groupName, appName));
    }

    @Test
    public void testGetAppTemplate() {
        final String result = "test";
        final String groupName = "testGroupName";
        final String appName = "testAppName";
        final String templateName = "testTemplate";
        when(mockResourcePersistenceService.getAppTemplate(eq(groupName), eq(appName), eq(templateName))).thenReturn(result);
        assertEquals(result, resourceService.getAppTemplate(groupName, appName, templateName));
    }

    @Test
    public void testUpdateResourceMetaData() {
        ResourceIdentifier mockResourceIdentifier = mock(ResourceIdentifier.class);
        when(mockResourceHandler.updateResourceMetaData(any(ResourceIdentifier.class), anyString(), anyString())).thenReturn("{}");
        final String resourceName = "test-resource-name";
        final String metaData = "{\"key\":\"value\"}";
        resourceService.updateResourceMetaData(mockResourceIdentifier, resourceName, metaData);
        verify(mockResourceHandler).updateResourceMetaData(eq(mockResourceIdentifier), eq(resourceName), eq(metaData));
    }
}
