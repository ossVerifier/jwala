package com.cerner.jwala.ws.rest.v1.service.group.impl;

import com.cerner.jwala.common.domain.model.app.Application;
import com.cerner.jwala.common.domain.model.app.ApplicationControlOperation;
import com.cerner.jwala.common.domain.model.fault.AemFaultType;
import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.domain.model.jvm.JvmState;
import com.cerner.jwala.common.domain.model.resource.ResourceGroup;
import com.cerner.jwala.common.domain.model.resource.ResourceType;
import com.cerner.jwala.common.domain.model.user.User;
import com.cerner.jwala.common.domain.model.webserver.WebServer;
import com.cerner.jwala.common.domain.model.webserver.WebServerReachableState;
import com.cerner.jwala.common.exception.InternalErrorException;
import com.cerner.jwala.common.exec.CommandOutput;
import com.cerner.jwala.common.exec.ExecReturnCode;
import com.cerner.jwala.common.properties.ApplicationProperties;
import com.cerner.jwala.common.request.group.CreateGroupRequest;
import com.cerner.jwala.common.request.jvm.ControlJvmRequest;
import com.cerner.jwala.common.request.webserver.ControlWebServerRequest;
import com.cerner.jwala.control.application.command.impl.WindowsApplicationPlatformCommandProvider;
import com.cerner.jwala.control.command.RemoteCommandExecutorImpl;
import com.cerner.jwala.exception.CommandFailureException;
import com.cerner.jwala.persistence.jpa.service.exception.ResourceTemplateUpdateException;
import com.cerner.jwala.persistence.service.ApplicationPersistenceService;
import com.cerner.jwala.persistence.service.GroupPersistenceService;
import com.cerner.jwala.persistence.service.WebServerPersistenceService;
import com.cerner.jwala.service.app.ApplicationService;
import com.cerner.jwala.service.group.GroupControlService;
import com.cerner.jwala.service.group.GroupJvmControlService;
import com.cerner.jwala.service.group.GroupService;
import com.cerner.jwala.service.group.GroupWebServerControlService;
import com.cerner.jwala.service.group.impl.GroupServiceImpl;
import com.cerner.jwala.service.jvm.JvmControlService;
import com.cerner.jwala.service.jvm.JvmService;
import com.cerner.jwala.service.jvm.state.JvmStateReceiverAdapter;
import com.cerner.jwala.service.resource.ResourceService;
import com.cerner.jwala.service.webserver.WebServerCommandService;
import com.cerner.jwala.service.webserver.WebServerControlService;
import com.cerner.jwala.service.webserver.WebServerService;
import com.cerner.jwala.ws.rest.v1.provider.AuthenticatedUser;
import com.cerner.jwala.ws.rest.v1.response.ApplicationResponse;
import com.cerner.jwala.ws.rest.v1.service.app.ApplicationServiceRest;
import com.cerner.jwala.ws.rest.v1.service.app.impl.ApplicationServiceRestImpl;
import com.cerner.jwala.ws.rest.v1.service.group.GroupServiceRest;
import com.cerner.jwala.ws.rest.v1.service.jvm.JvmServiceRest;
import com.cerner.jwala.ws.rest.v1.service.jvm.impl.JvmServiceRestImpl;
import com.cerner.jwala.ws.rest.v1.service.webserver.WebServerServiceRest;
import com.cerner.jwala.ws.rest.v1.service.webserver.impl.WebServerServiceRestImpl;

import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
//@IfProfileValue(name = TestExecutionProfile.RUN_TEST_TYPES, value = TestExecutionProfile.INTEGRATION)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class,
        classes = {GroupServiceImplDeployTest.Config.class
        })
public class GroupServiceImplDeployTest {

    @Autowired
    GroupServiceRest groupServiceRest;

    @Autowired
    JvmServiceRest jvmServiceRest;

    @Autowired
    WebServerServiceRest webServerServiceRest;

    @Autowired
    ApplicationServiceRest applicationServiceRest;

    static final GroupService mockGroupService = mock(GroupService.class);
    static final ResourceService mockResourceService = mock(ResourceService.class);
    static final GroupControlService mockGroupControlService = mock(GroupControlService.class);
    static final GroupJvmControlService mockGroupJvmControlService = mock(GroupJvmControlService.class);
    static final GroupWebServerControlService mockGroupWebServerControlService = mock(GroupWebServerControlService.class);
    static final JvmService mockJvmService = mock(JvmService.class);
    static final JvmControlService mockJvmControlService = mock(JvmControlService.class);
    static final WebServerService mockWebServerService = mock(WebServerService.class);
    static final WebServerControlService mockWebServerControlService = mock(WebServerControlService.class);
    static final ApplicationService mockApplicationService = mock(ApplicationService.class);
    static final JvmStateReceiverAdapter mockJvmStateReceiverAdapter = mock(JvmStateReceiverAdapter.class);

    private AuthenticatedUser mockAuthUser = mock(AuthenticatedUser.class);
    private User mockUser = mock(User.class);
    private String httpdConfDirPath;
    private String generatedResourceDir;

    public GroupServiceImplDeployTest() {
        System.setProperty(ApplicationProperties.PROPERTIES_ROOT_PATH,
                this.getClass().getClassLoader().getResource("vars.properties").getPath().replace("vars.properties", ""));
    }

    @Before
    public void setUp() {
        when(mockAuthUser.getUser()).thenReturn(mockUser);

        System.setProperty(ApplicationProperties.PROPERTIES_ROOT_PATH, "./src/test/resources");
        httpdConfDirPath = ApplicationProperties.get("paths.httpd.conf");
        // assertTrue(new File(httpdConfDirPath).mkdirs());
        new File(httpdConfDirPath).mkdirs();
        generatedResourceDir = ApplicationProperties.get("paths.generated.resource.dir");
        // assertTrue(new File(generatedResourceDir).mkdirs());
        new File(httpdConfDirPath).mkdirs();
    }

    @After
    public void tearDown() throws IOException {
        FileUtils.deleteDirectory(new File(httpdConfDirPath));
        FileUtils.deleteDirectory(new File(generatedResourceDir));
        System.clearProperty(ApplicationProperties.PROPERTIES_ROOT_PATH);
    }

    @Test
    public void testCreateGroup() {
        reset(mockResourceService);

        Group mockGroup = mock(Group.class);
        when(mockGroupService.createGroup(any(CreateGroupRequest.class), any(User.class))).thenReturn(mockGroup);

        groupServiceRest.createGroup("testGroup", mockAuthUser);

        verify(mockGroupService, times(1)).createGroup(any(CreateGroupRequest.class), any(User.class));
    }

    @Test
    public void testGroupJvmDeploy() throws CommandFailureException, IOException {
        Group mockGroup = mock(Group.class);
        Jvm mockJvm = mock(Jvm.class);
        Response mockResponse = mock(Response.class);
        ResourceType mockResourceType = mock(ResourceType.class);

        Set<Jvm> jvmSet = new HashSet<>();
        jvmSet.add(mockJvm);
        Collection<ResourceType> resourcesList = new ArrayList<>();
        resourcesList.add(mockResourceType);

        when(mockGroup.getJvms()).thenReturn(jvmSet);
        when(mockJvm.getJvmName()).thenReturn("testJvm");
        when(mockJvm.getId()).thenReturn(new Identifier<Jvm>(99L));
        when(mockJvm.getState()).thenReturn(JvmState.JVM_STOPPED);
        when(mockResponse.getStatus()).thenReturn(200);
        when(mockResourceType.getRelativeDir()).thenReturn("./");
        when(mockResourceType.getEntityType()).thenReturn("jvm");
        when(mockResourceType.getConfigFileName()).thenReturn("server.xml");
        when(mockGroupService.getGroup(anyString())).thenReturn(mockGroup);
        when(mockGroupService.getGroupJvmResourceTemplate(anyString(), anyString(), any(ResourceGroup.class), anyBoolean())).thenReturn("new server.xml content");
        when(mockJvmService.updateResourceTemplate(anyString(), anyString(), anyString())).thenReturn("new server.xml content");
        when(mockJvmService.getJvm(anyString())).thenReturn(mockJvm);
        when(mockJvmService.generateConfigFile(anyString(), anyString())).thenReturn("new server.xml content");
        when(mockJvmControlService.secureCopyFile(any(ControlJvmRequest.class), anyString(), anyString(), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(0), "SUCCESS", ""));

        Response returnedResponse = groupServiceRest.generateAndDeployGroupJvmFile("testGroup", "server.xml", mockAuthUser);
        assertEquals(200, returnedResponse.getStatusInfo().getStatusCode());

        boolean internalError = false;
        when(mockJvm.getState()).thenReturn(JvmState.JVM_STARTED);
        try {
            groupServiceRest.generateAndDeployGroupJvmFile("testGroup", "server.xml", mockAuthUser);
        } catch (InternalErrorException iee) {
            internalError = true;
        }
        assertTrue(internalError);
    }

    @Test
    @Ignore
    // TODO: Fix this.
    public void testGroupWebServerDeploy() throws CommandFailureException, IOException {
        Group mockGroup = mock(Group.class);
        WebServer mockWebServer = mock(WebServer.class);
        Response mockResponse = mock(Response.class);
        ResourceType mockResourceType = mock(ResourceType.class);

        Set<WebServer> webServerSet = new HashSet<>();
        webServerSet.add(mockWebServer);
        Collection<ResourceType> resourcesList = new ArrayList<>();
        resourcesList.add(mockResourceType);

        when(mockGroup.getWebServers()).thenReturn(webServerSet);
        when(mockWebServer.getName()).thenReturn("testWebServer");
        when(mockWebServer.getId()).thenReturn(new Identifier<WebServer>(99L));
        when(mockWebServer.getState()).thenReturn(WebServerReachableState.WS_UNREACHABLE);
        when(mockResponse.getStatus()).thenReturn(200);
        when(mockResourceType.getRelativeDir()).thenReturn("./");
        when(mockResourceType.getEntityType()).thenReturn("webServer");
        when(mockResourceType.getConfigFileName()).thenReturn("httpd.conf");
        when(mockGroupService.getGroup(anyString())).thenReturn(mockGroup);
        when(mockGroupService.getGroupWithWebServers(any(Identifier.class))).thenReturn(mockGroup);
        when(mockGroupService.getGroupWebServerResourceTemplate(anyString(), anyString(), anyBoolean(), any(ResourceGroup.class))).thenReturn("new httpd.conf context");
        when(mockResourceService.generateResourceGroup()).thenReturn(new ResourceGroup());
        when(mockWebServerService.getResourceTemplateMetaData(anyString(), anyString())).thenReturn("{\"contentType\":\"text/plain\",\"deployPath\":\"./anyPath\"}");
        when(mockWebServerService.updateResourceTemplate(anyString(), anyString(), anyString())).thenReturn("new httpd.conf context");
        when(mockWebServerService.generateHttpdConfig(anyString(), any(ResourceGroup.class))).thenReturn("new httpd.conf context");
        when(mockWebServerControlService.secureCopyFile(anyString(), anyString(), anyString(), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(0), "SUCCESS", ""));

        Response returnedResponse = groupServiceRest.generateAndDeployGroupWebServersFile("testGroup", "httpd.conf", mockAuthUser);
        assertEquals(200, returnedResponse.getStatusInfo().getStatusCode());

        when(mockWebServerControlService.secureCopyFile(anyString(), anyString(), anyString(), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(1), "", "NOT OK"));
        try {
            groupServiceRest.generateAndDeployGroupWebServersFile("testGroup", "httpd.conf", mockAuthUser);
        } catch (InternalErrorException ie) {
            assertEquals(AemFaultType.REMOTE_COMMAND_FAILURE, ie.getMessageResponseStatus());
        }
    }

    @Test(expected = InternalErrorException.class)
    public void testGenerateAndDeployGroupWebServerFileWithWebServerStarted() {
        Group mockGroup = mock(Group.class);
        WebServer mockWebServer = mock(WebServer.class);
        Set<WebServer> webServerSet = new HashSet<>();
        webServerSet.add(mockWebServer);
        when(mockGroupService.getGroupWithWebServers(any(Identifier.class))).thenReturn(mockGroup);
        when(mockGroupService.getGroup(anyString())).thenReturn(mockGroup);
        when(mockGroupService.getGroupWebServerResourceTemplate(anyString(), anyString(), anyBoolean(), any(ResourceGroup.class))).thenReturn("Httpd.conf template content");
        when(mockGroup.getWebServers()).thenReturn(webServerSet);
        when(mockWebServerService.isStarted(any(WebServer.class))).thenReturn(true);
        groupServiceRest.generateAndDeployGroupWebServersFile("groupName", "httpd.conf", mockAuthUser);
    }

    @Test
    public void testGroupAppDeploy() throws CommandFailureException {
        Group mockGroup = mock(Group.class);
        Jvm mockJvm = mock(Jvm.class);
        Application mockApp = mock(Application.class);
        Response mockResponse = mock(Response.class);
        ResourceType mockResourceType = mock(ResourceType.class);
        String hostName = "testHost";

        Set<Jvm> jvmSet = new HashSet<>();
        jvmSet.add(mockJvm);
        Set<Application> appSet = new HashSet<>();
        appSet.add(mockApp);
        Collection<ResourceType> resourcesList = new ArrayList<>();
        resourcesList.add(mockResourceType);

        when(mockGroup.getJvms()).thenReturn(jvmSet);
        when(mockJvm.getJvmName()).thenReturn("testJvm");
        when(mockJvm.getId()).thenReturn(new Identifier<Jvm>(99L));
        when(mockJvm.getState()).thenReturn(JvmState.JVM_STOPPED);
        when(mockResponse.getStatus()).thenReturn(200);
        when(mockResourceType.getRelativeDir()).thenReturn("./");
        when(mockResourceType.getEntityType()).thenReturn("webApp");
        when(mockResourceType.getConfigFileName()).thenReturn("hct.xml");
        when(mockGroupService.getGroup(anyString())).thenReturn(mockGroup);
        when(mockGroupService.getGroupAppResourceTemplate(anyString(), anyString(), anyString(), anyBoolean(), any(ResourceGroup.class))).thenReturn("new hct.xml content");
        when(mockGroupService.getGroupAppResourceTemplateMetaData(anyString(), anyString())).thenReturn("{\"entity\":{\"target\": \"testApp\"}}");
        when(mockJvmService.getJvm(anyString())).thenReturn(mockJvm);
        when(mockApplicationService.updateResourceTemplate(anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn("new hct.xml content");
        when(mockApplicationService.deployConf(anyString(), anyString(), anyString(), anyString(), any(ResourceGroup.class), any(User.class))).thenReturn(new CommandOutput(new ExecReturnCode(0), "SUCCESS", ""));
        Response returnResponse = groupServiceRest.generateAndDeployGroupAppFile("testGroup", "hct.xml", mockAuthUser, null);
        assertEquals(200, returnResponse.getStatus());

        when(mockJvm.getHostName()).thenReturn("TestHost");
        returnResponse = groupServiceRest.generateAndDeployGroupAppFile("testGroup", "hct.xml", mockAuthUser, hostName);
        assertEquals(200, returnResponse.getStatus());

        when(mockJvm.getHostName()).thenReturn("otherhostname");
        returnResponse = groupServiceRest.generateAndDeployGroupAppFile("testGroup", "hct.xml", mockAuthUser, hostName);
        assertEquals(200, returnResponse.getStatus());

        when(mockGroupService.getGroupAppResourceTemplateMetaData(anyString(), anyString())).thenReturn("{\"entity\":{\"target\": \"testApp\", \"deployToJvms\": false}}");
        when(mockJvm.getHostName()).thenReturn("TestHost");
        when(mockGroupService.deployGroupAppTemplate(anyString(), anyString(), any(ResourceGroup.class), any(Application.class), anyString())).thenReturn(
                new CommandOutput(new ExecReturnCode(0), "SUCCESS", ""));
        returnResponse = groupServiceRest.generateAndDeployGroupAppFile("testGroup", "hct.xml", mockAuthUser, hostName);
        assertEquals(200, returnResponse.getStatus());

        when(mockApplicationService.deployConf(anyString(), anyString(), anyString(), anyString(), any(ResourceGroup.class), any(User.class))).thenReturn(new CommandOutput(new ExecReturnCode(1), "", "NOT OK"));
        try {
            groupServiceRest.generateAndDeployGroupAppFile("testGroup", "hct.xml", mockAuthUser, null);
        } catch (InternalErrorException ie) {
            assertEquals(AemFaultType.REMOTE_COMMAND_FAILURE, ie.getMessageResponseStatus());
        }

        boolean internalErrorException = false;
        when(mockJvm.getState()).thenReturn(JvmState.JVM_STARTED);
        try {
            groupServiceRest.generateAndDeployGroupAppFile("testGroup", "hct.xml", mockAuthUser, null);
        } catch (InternalErrorException ie) {
            internalErrorException = true;
            assertTrue(ie.getMessage().contains("All JVMs in the group must be stopped"));
        }
        assertTrue(internalErrorException);
    }

    @Test
    public void testGroupAppDeployNotToJvms() {
        Group mockGroup = mock(Group.class);
        Jvm mockJvm = mock(Jvm.class);
        Application mockApp = mock(Application.class);
        Response mockResponse = mock(Response.class);
        ResourceType mockResourceType = mock(ResourceType.class);

        Set<Jvm> jvmSet = new HashSet<>();
        jvmSet.add(mockJvm);
        Set<Application> appSet = new HashSet<>();
        appSet.add(mockApp);
        Collection<ResourceType> resourcesList = new ArrayList<>();
        resourcesList.add(mockResourceType);

        when(mockGroup.getJvms()).thenReturn(jvmSet);
        when(mockJvm.getJvmName()).thenReturn("testJvm");
        when(mockJvm.getId()).thenReturn(new Identifier<Jvm>(99L));
        when(mockJvm.getState()).thenReturn(JvmState.JVM_STOPPED);
        when(mockJvm.getHostName()).thenReturn("mockHost");
        when(mockResponse.getStatus()).thenReturn(200);
        when(mockResourceType.getRelativeDir()).thenReturn("./");
        when(mockResourceType.getEntityType()).thenReturn("webApp");
        when(mockResourceType.getConfigFileName()).thenReturn("hct.xml");
        when(mockGroupService.getGroup(anyString())).thenReturn(mockGroup);
        when(mockGroupService.getGroupAppResourceTemplate(anyString(), anyString(), anyString(), anyBoolean(), any(ResourceGroup.class))).thenReturn("new hct.xml content");
        when(mockGroupService.getGroupAppResourceTemplateMetaData(anyString(), anyString())).thenReturn("{\"entity\":{\"target\": \"testApp\", \"deployToJvms\":false}}");
        when(mockGroupService.deployGroupAppTemplate(anyString(), anyString(), any(ResourceGroup.class), any(Application.class), any(Jvm.class))).thenReturn(new CommandOutput(new ExecReturnCode(0), "SUCCESS", ""));
        when(mockJvmService.getJvm(anyString())).thenReturn(mockJvm);
        when(mockResourceService.generateResourceGroup()).thenReturn(new ResourceGroup());
        when(mockApplicationService.updateResourceTemplate(anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn("new hct.xml content");
        when(mockResourceService.generateResourceGroup()).thenReturn(new ResourceGroup());
        when(mockApplicationService.getApplication(anyString())).thenReturn(mockApp);
        Response returnResponse = groupServiceRest.generateAndDeployGroupAppFile("testGroup", "hct.xml", mockAuthUser, null);
        assertEquals(200, returnResponse.getStatus());

        when(mockJvm.getState()).thenReturn(JvmState.JVM_STARTED);
        boolean internalErrorExceptionThrown = false;
        try {
            groupServiceRest.generateAndDeployGroupAppFile("testGroup", "hct.xml", mockAuthUser, null);
        } catch (InternalErrorException e) {
            internalErrorExceptionThrown = true;
        }
        assertTrue(internalErrorExceptionThrown);
    }

    @Test
    public void testDeployGroupAppTemplateWar() throws CommandFailureException, IOException {
        String groupName = "testGroup";
        String fileName = "testFile";
        Group group = mock(Group.class);
        List<Group> groups = new ArrayList<>();
        groups.add(group);
        ResourceGroup resourceGroup = mock(ResourceGroup.class);
        Application application = mock(Application.class);
        Set<Application> applications = new HashSet<>();
        applications.add(application);
        Jvm jvm = mock(Jvm.class);
        Set<Jvm> jvms = new HashSet<>();
        jvms.add(jvm);

        GroupPersistenceService groupPersistenceService = mock(GroupPersistenceService.class);
        WebServerPersistenceService webServerPersistenceService = mock(WebServerPersistenceService.class);
        ApplicationPersistenceService applicationPersistenceService = mock(ApplicationPersistenceService.class);
        RemoteCommandExecutorImpl remoteCommandExecutorImpl = mock(RemoteCommandExecutorImpl.class);
        GroupServiceImpl groupServiceImpl = new GroupServiceImpl(groupPersistenceService, webServerPersistenceService, applicationPersistenceService, remoteCommandExecutorImpl);
        CommandOutput commandOutput = mock(CommandOutput.class);

        String metaData = "{\"templateName\":\"someTemplateName\",\"contentType\":\"application/binary\",\"deployPath\":" +
                "\"testLocation\",\"deployFileName\":\"someTemplateName\",\"unpack\": true,\"entity\":{\"type\":" +
                "\"GROUPED_WEBSERVERS\",\"group\":\"testGroup\",\"target\":\"testApp\",\"deployToJvms\": false}}";
        String jvmName = "testJvm";
        String appName = "testApp";
        String hostName = "testHost";
        ExecReturnCode execReturnCode = mock(ExecReturnCode.class);

        when(groupPersistenceService.getGroupAppResourceTemplateMetaData(anyString(), anyString())).thenReturn(metaData);
        when(groupPersistenceService.getGroupAppResourceTemplate(anyString(), anyString(), anyString())).thenReturn("");
        when(applicationPersistenceService.getApplication(anyString())).thenReturn(application);
        when(application.getName()).thenReturn(appName);
        when(jvm.getJvmName()).thenReturn(jvmName);
        when(jvm.getHostName()).thenReturn(hostName);
        when(group.getName()).thenReturn(groupName);
        when(application.getParentJvm()).thenReturn(jvm);
        when(application.getGroup()).thenReturn(group);
        when(group.getWebServers()).thenReturn(null);
        when(group.getApplications()).thenReturn(applications);
        when(group.getJvms()).thenReturn(jvms);
        when(resourceGroup.getGroups()).thenReturn(groups);
        when(remoteCommandExecutorImpl.executeRemoteCommand(eq(jvmName), eq(hostName), eq(ApplicationControlOperation.CHECK_FILE_EXISTS),
                any(WindowsApplicationPlatformCommandProvider.class), anyString())).thenReturn(commandOutput);
        when(remoteCommandExecutorImpl.executeRemoteCommand(eq(jvmName), eq(hostName), eq(ApplicationControlOperation.BACK_UP_FILE),
                any(WindowsApplicationPlatformCommandProvider.class), anyString(), anyString())).thenReturn(commandOutput);
        when(remoteCommandExecutorImpl.executeRemoteCommand(eq(jvmName), eq(hostName), eq(ApplicationControlOperation.SECURE_COPY),
                any(WindowsApplicationPlatformCommandProvider.class), anyString(), anyString())).thenReturn(commandOutput);
        when(remoteCommandExecutorImpl.executeRemoteCommand(anyString(), eq(hostName), eq(ApplicationControlOperation.CREATE_DIRECTORY),
                any(WindowsApplicationPlatformCommandProvider.class), anyString())).thenReturn(commandOutput);
        when(remoteCommandExecutorImpl.executeRemoteCommand(anyString(), eq(hostName), eq(ApplicationControlOperation.SECURE_COPY),
                any(WindowsApplicationPlatformCommandProvider.class), anyString(), anyString())).thenReturn(commandOutput);
        when(remoteCommandExecutorImpl.executeRemoteCommand(anyString(), eq(hostName), eq(ApplicationControlOperation.CHANGE_FILE_MODE),
                any(WindowsApplicationPlatformCommandProvider.class), anyString(), anyString(), anyString())).thenReturn(commandOutput);
        when(remoteCommandExecutorImpl.executeRemoteCommand(anyString(), eq(hostName), eq(ApplicationControlOperation.UNPACK_WAR),
                any(WindowsApplicationPlatformCommandProvider.class), anyString())).thenReturn(commandOutput);
        when(commandOutput.getReturnCode()).thenReturn(execReturnCode);
        when(execReturnCode.wasSuccessful()).thenReturn(true);

        assertEquals(commandOutput, groupServiceImpl.deployGroupAppTemplate(groupName, fileName, resourceGroup, application, jvm));
    }

    @Test
    @Ignore
    // TODO: Fix this.
    public void testGenerateAndDeployWebServers() throws CommandFailureException, IOException {
        Set<WebServer> mockWSList = new HashSet<>();
        Group mockGroup = mock(Group.class);
        WebServer mockWebServer = mock(WebServer.class);
        mockWSList.add(mockWebServer);
        CommandOutput successCommandOutput = new CommandOutput(new ExecReturnCode(0), "SUCCESS", "");
        List<String> resourceTemplateNames = new ArrayList<>();
        resourceTemplateNames.add("httpd.conf");

        when(mockWebServer.getName()).thenReturn("webServerName");
        when(mockGroup.getId()).thenReturn(new Identifier<Group>(111L));
        when(mockWebServer.getState()).thenReturn(WebServerReachableState.WS_UNREACHABLE);
        when(mockGroup.getWebServers()).thenReturn(mockWSList);
        when(mockGroupService.getGroupWithWebServers(any(Identifier.class))).thenReturn(mockGroup);
        when(mockWebServerService.isStarted(any(WebServer.class))).thenReturn(false);
        when(mockWebServerService.getWebServer(anyString())).thenReturn(mockWebServer);
        when(mockWebServerService.getResourceTemplateMetaData(anyString(), anyString())).thenReturn("{\"contentType\":\"text/plain\",\"deployPath\":./anyPath}");
        when(mockResourceService.generateResourceGroup()).thenReturn(new ResourceGroup());
        when(mockWebServerControlService.controlWebServer(any(ControlWebServerRequest.class), any(User.class))).thenReturn(successCommandOutput);
        when(mockWebServerControlService.createDirectory(any(WebServer.class), anyString())).thenReturn(successCommandOutput);
        when(mockWebServerControlService.makeExecutableUnixFormat(any(WebServer.class), anyString(), anyString(), anyString())).thenReturn(successCommandOutput);
        when(mockWebServerService.generateHttpdConfig(anyString(), any(ResourceGroup.class))).thenReturn("httpd.conf content");
        when(mockWebServerControlService.secureCopyFile(anyString(), anyString(), anyString(), anyString())).thenReturn(successCommandOutput);
        when(mockWebServerService.generateInvokeWSBat(any(WebServer.class))).thenReturn("invokeWS.bat content");
        when(mockWebServerService.getResourceTemplateNames(anyString())).thenReturn(resourceTemplateNames);

        Response response = groupServiceRest.generateGroupWebservers(mockGroup.getId(), mockAuthUser);
        assertNotNull(response);
    }

    @Test(expected = InternalErrorException.class)
    public void testGenerateGroupWebServersWithWebServerStarted() {
        Group mockGroup = mock(Group.class);
        Set<WebServer> webServersSet = new HashSet<>();
        WebServer mockWebServer = mock(WebServer.class);
        webServersSet.add(mockWebServer);
        when(mockGroup.getWebServers()).thenReturn(webServersSet);
        when(mockWebServerService.isStarted(any(WebServer.class))).thenReturn(true);
        when(mockGroupService.getGroupWithWebServers(any(Identifier.class))).thenReturn(mockGroup);
        groupServiceRest.generateGroupWebservers(new Identifier<Group>(111L), mockAuthUser);
    }

    @Test
    public void testGenerateGroupWebServersWithNoWebServers() {
        Group mockGroup = mock(Group.class);
        Set<WebServer> webServersSet = new HashSet<>();
        when(mockGroup.getWebServers()).thenReturn(webServersSet);
        when(mockGroupService.getGroupWithWebServers(any(Identifier.class))).thenReturn(mockGroup);
        Response response = groupServiceRest.generateGroupWebservers(new Identifier<Group>(111L), mockAuthUser);
        assertTrue(response.getStatus() > 199 && response.getStatus() < 300);
    }

    @Test
    public void testGenerateAndDeployJvms() throws CommandFailureException {
        reset(mockGroupService);
        reset(mockJvmService);
        reset(mockJvmControlService);
        reset(mockResourceService);

        Group mockGroup = mock(Group.class);
        Jvm mockJvm = mock(Jvm.class);
        CommandOutput successCommandOutput = new CommandOutput(new ExecReturnCode(0), "SUCCESS", "");
        Set<Jvm> jvmsSet = new HashSet<>();
        jvmsSet.add(mockJvm);

        when(mockGroup.getJvms()).thenReturn(jvmsSet);
        when(mockJvm.getJvmName()).thenReturn("jvmName");
        when(mockJvm.getState()).thenReturn(JvmState.JVM_STOPPED);
        when(mockJvm.getId()).thenReturn(new Identifier<Jvm>(1111L));
        when(mockGroupService.getGroup(any(Identifier.class))).thenReturn(mockGroup);
        when(mockJvmService.getJvm(anyString())).thenReturn(mockJvm);
        when(mockJvmControlService.controlJvm(any(ControlJvmRequest.class), any(User.class))).thenReturn(successCommandOutput);
        when(mockJvmControlService.createDirectory(any(Jvm.class), anyString())).thenReturn(successCommandOutput);
        when(mockJvmControlService.makeExecutableUnixFormat(any(Jvm.class), anyString(), anyString(), anyString())).thenReturn(successCommandOutput);
        when(mockJvmControlService.secureCopyFile(any(ControlJvmRequest.class), anyString(), anyString(), anyString())).thenReturn(successCommandOutput);

        Response response = groupServiceRest.generateGroupJvms(new Identifier<Group>(111L), mockAuthUser);
        assertNotNull(response);
    }

    @Test
    public void testGenerateAndDeployJvmsNoJvms() {
        Group mockGroup = mock(Group.class);
        when(mockGroup.getJvms()).thenReturn(new HashSet<Jvm>());
        when(mockGroupService.getGroup(any(Identifier.class))).thenReturn(mockGroup);
        Response response = groupServiceRest.generateGroupJvms(new Identifier<Group>(11212L), mockAuthUser);
        assertTrue(response.getStatus() > 199 && response.getStatus() < 300);

    }

    @Test(expected = InternalErrorException.class)
    public void testGenerateGroupJvmsWithJvmStarted() {
        Group mockGroup = mock(Group.class);
        Set<Jvm> jvmSet = new HashSet<>();
        Jvm mockJvm = mock(Jvm.class);
        jvmSet.add(mockJvm);
        when(mockJvm.getState()).thenReturn(JvmState.JVM_STARTED);
        when(mockGroup.getJvms()).thenReturn(jvmSet);
        when(mockGroupService.getGroup(any(Identifier.class))).thenReturn(mockGroup);
        groupServiceRest.generateGroupJvms(new Identifier<Group>(111L), mockAuthUser);
    }

    @Test
    public void testUpdateGroupAppTemplate() {
        Group mockGroupWithJvms = mock(Group.class);
        Jvm mockJvm = mock(Jvm.class);
        Set<Jvm> mockJvms = new HashSet<>();
        mockJvms.add(mockJvm);
        when(mockJvm.getJvmName()).thenReturn("mockJvmName");
        when(mockGroupWithJvms.getJvms()).thenReturn(mockJvms);
        when(mockGroupService.updateGroupAppResourceTemplate(anyString(), anyString(), anyString(), anyString())).thenReturn("new hct.xml content");
        when(mockGroupService.getGroup(anyString())).thenReturn(mockGroupWithJvms);
        when(mockGroupService.getGroupAppResourceTemplateMetaData(anyString(), anyString())).thenReturn("{\"entity\":{\"target\": \"testApp\"}}");
        when(mockApplicationService.updateResourceTemplate(anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn("new hct.xml content");

        Response response = groupServiceRest.updateGroupAppResourceTemplate("testGroup", "testAppName", "hct.xml", "new hct.xml context");
        verify(mockGroupService).updateGroupAppResourceTemplate(anyString(), anyString(), anyString(), anyString());
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        when(mockGroupService.updateGroupAppResourceTemplate(anyString(), anyString(), anyString(), anyString())).thenThrow(new ResourceTemplateUpdateException("testApp", "hct.xml"));
        response = groupServiceRest.updateGroupAppResourceTemplate("testGroup", "testAppName", "hct.xml", "newer hct.xml content");
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());

        reset(mockGroupService);
        when(mockGroupService.updateGroupAppResourceTemplate(anyString(), anyString(), anyString(), anyString())).thenReturn("new hct.xml content");
        when(mockGroupService.getGroupAppResourceTemplateMetaData(anyString(), anyString())).thenReturn("{\"entity\":{\"target\": \"testApp\"}}");
        when(mockGroupService.getGroup(anyString())).thenReturn(mockGroupWithJvms);
        when(mockGroupWithJvms.getJvms()).thenReturn(null);
        response = groupServiceRest.updateGroupAppResourceTemplate("testGroup", "testAppName", "hct.xml", "newer hct.xml content");
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    public void testUpdateGroupAppTemplateJsonParseException() {
        when(mockGroupService.getGroupAppResourceTemplateMetaData(anyString(), anyString())).thenReturn("{\"throwParseException\":true}");
        when(mockGroupService.getGroup(anyString())).thenReturn(mock(Group.class));
        when(mockGroupService.updateGroupAppResourceTemplate(anyString(), anyString(), anyString(), anyString())).thenReturn("updated group app resource template content");
        Response notOk = groupServiceRest.updateGroupAppResourceTemplate("testGroup", "testAppName", "hct.xml", "newer hct.xml content");
        assertTrue(((ApplicationResponse) notOk.getEntity()).getApplicationResponseContent().toString().contains("Unrecognized field"));
        assertEquals(500, notOk.getStatus());

        when(mockGroupService.getGroupAppResourceTemplateMetaData(anyString(), anyString())).thenReturn("{\"throwParseException\"");
        notOk = groupServiceRest.updateGroupAppResourceTemplate("testGroup", "testAppName", "hct.xml", "newer hct.xml content");
        assertTrue(((ApplicationResponse) notOk.getEntity()).getApplicationResponseContent().toString().contains("Unexpected end-of-input"));
        assertEquals(500, notOk.getStatus());

        reset(mockGroupService);
    }

    @Test
    public void testGetStartedAndStoppedWebserversAndJvmsCount() {
        List<Group> groupList = new ArrayList<>();
        Group mockGroup = mock(Group.class);
        when(mockGroup.getName()).thenReturn("test-group-name");
        groupList.add(mockGroup);

        when(mockJvmService.getJvmStartedCount(anyString())).thenReturn(0L);
        when(mockJvmService.getJvmStoppedCount(anyString())).thenReturn(0L);
        when(mockJvmService.getJvmForciblyStoppedCount(anyString())).thenReturn(0L);
        when(mockJvmService.getJvmCount(anyString())).thenReturn(0L);
        when(mockWebServerService.getWebServerStartedCount(anyString())).thenReturn(0L);
        when(mockWebServerService.getWebServerStoppedCount(anyString())).thenReturn(0L);
        when(mockWebServerService.getWebServerCount(anyString())).thenReturn(0L);
        when(mockGroupService.getGroups()).thenReturn(groupList);

        Response response = groupServiceRest.getStartedAndStoppedWebServersAndJvmsCount();
        assertEquals(200, response.getStatus());
        final GroupServerInfo groupServerInfoResponse = (GroupServerInfo) ((ArrayList)(((ApplicationResponse) response.getEntity()).getApplicationResponseContent())).get(0);
        assertEquals(Long.valueOf(0), groupServerInfoResponse.getJvmStartedCount());
        assertEquals(Long.valueOf(0), groupServerInfoResponse.getJvmStoppedCount());
        assertEquals(Long.valueOf(0), groupServerInfoResponse.getJvmForciblyStoppedCount());
        assertEquals(Long.valueOf(0), groupServerInfoResponse.getJvmCount());
        assertEquals(Long.valueOf(0), groupServerInfoResponse.getWebServerCount());
        assertEquals(Long.valueOf(0), groupServerInfoResponse.getWebServerStartedCount());
        assertEquals(Long.valueOf(0), groupServerInfoResponse.getWebServerStoppedCount());
    }

    @Test
    public void testgetStartedAndStoppedWebServersAndJvmsCount() {
        when(mockJvmService.getJvmStartedCount(anyString())).thenReturn(0L);
        when(mockJvmService.getJvmStoppedCount(anyString())).thenReturn(0L);
        when(mockJvmService.getJvmForciblyStoppedCount(anyString())).thenReturn(0L);
        when(mockJvmService.getJvmCount(anyString())).thenReturn(0L);
        when(mockWebServerService.getWebServerStartedCount(anyString())).thenReturn(0L);
        when(mockWebServerService.getWebServerStoppedCount(anyString())).thenReturn(0L);
        when(mockWebServerService.getWebServerCount(anyString())).thenReturn(0L);

        Response response = groupServiceRest.getStartedAndStoppedWebServersAndJvmsCount("test-group-name");
        assertEquals(200, response.getStatus());
        final GroupServerInfo groupServerInfoResponse = (GroupServerInfo) (((ApplicationResponse) response.getEntity()).getApplicationResponseContent());
        assertEquals(Long.valueOf(0), groupServerInfoResponse.getJvmStartedCount());
        assertEquals(Long.valueOf(0), groupServerInfoResponse.getJvmStoppedCount());
        assertEquals(Long.valueOf(0), groupServerInfoResponse.getJvmForciblyStoppedCount());
        assertEquals(Long.valueOf(0), groupServerInfoResponse.getJvmCount());
        assertEquals(Long.valueOf(0), groupServerInfoResponse.getWebServerCount());
        assertEquals(Long.valueOf(0), groupServerInfoResponse.getWebServerStartedCount());
        assertEquals(Long.valueOf(0), groupServerInfoResponse.getWebServerStoppedCount());
    }

    @Configuration
    static class Config {

        @Bean
        public GroupServiceRest getGroupServiceRest() {
            return new GroupServiceRestImpl(mockGroupService, mockResourceService, mockGroupControlService, mockGroupJvmControlService,
                    mockGroupWebServerControlService, mockJvmService, mockWebServerService, mockApplicationService);
        }

        @Bean
        public JvmServiceRest getJvmServiceRest() {
            return new JvmServiceRestImpl(mockJvmService, mockJvmControlService, mockResourceService);
        }

        @Bean
        WebServerServiceRest getWebServerServiceRest() {
            return new WebServerServiceRestImpl(mockWebServerService, mockWebServerControlService, mock(WebServerCommandService.class), new HashMap<String, ReentrantReadWriteLock>(), mockResourceService, mockGroupService);
        }

        @Bean
        ApplicationServiceRest getApplicationServiceRest() {
            return new ApplicationServiceRestImpl(mockApplicationService, mock(ResourceService.class), new ServletFileUpload(), mockGroupService);
        }

        @Bean
        public GroupControlService getGroupControlService() {
            return mockGroupControlService;
        }

        @Bean
        public GroupJvmControlService getGroupJvmControlService() {
            return mockGroupJvmControlService;
        }

        @Bean
        public GroupWebServerControlService getGroupWebServerControlService() {
            return mockGroupWebServerControlService;
        }
    }
}
