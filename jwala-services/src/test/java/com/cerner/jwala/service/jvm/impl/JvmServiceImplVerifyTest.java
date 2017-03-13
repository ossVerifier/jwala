package com.cerner.jwala.service.jvm.impl;

import com.cerner.jwala.common.domain.model.app.Application;
import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.domain.model.jvm.JvmControlOperation;
import com.cerner.jwala.common.domain.model.jvm.JvmState;
import com.cerner.jwala.common.domain.model.path.Path;
import com.cerner.jwala.common.domain.model.resource.ResourceGroup;
import com.cerner.jwala.common.domain.model.resource.ResourceTemplateMetaData;
import com.cerner.jwala.common.domain.model.resource.ResourceType;
import com.cerner.jwala.common.domain.model.user.User;
import com.cerner.jwala.common.exception.BadRequestException;
import com.cerner.jwala.common.exception.InternalErrorException;
import com.cerner.jwala.common.exec.CommandOutput;
import com.cerner.jwala.common.exec.ExecCommand;
import com.cerner.jwala.common.exec.ExecReturnCode;
import com.cerner.jwala.common.properties.ApplicationProperties;
import com.cerner.jwala.common.request.group.AddJvmToGroupRequest;
import com.cerner.jwala.common.request.jvm.ControlJvmRequest;
import com.cerner.jwala.common.request.jvm.CreateJvmAndAddToGroupsRequest;
import com.cerner.jwala.common.request.jvm.CreateJvmRequest;
import com.cerner.jwala.common.request.jvm.UpdateJvmRequest;
import com.cerner.jwala.control.AemControl;
import com.cerner.jwala.exception.CommandFailureException;
import com.cerner.jwala.files.FileManager;
import com.cerner.jwala.persistence.jpa.service.exception.NonRetrievableResourceTemplateContentException;
import com.cerner.jwala.persistence.service.JvmPersistenceService;
import com.cerner.jwala.service.VerificationBehaviorSupport;
import com.cerner.jwala.service.app.ApplicationService;
import com.cerner.jwala.service.binarydistribution.BinaryDistributionLockManager;
import com.cerner.jwala.service.binarydistribution.BinaryDistributionService;
import com.cerner.jwala.service.group.GroupService;
import com.cerner.jwala.service.group.GroupStateNotificationService;
import com.cerner.jwala.service.jvm.JvmControlService;
import com.cerner.jwala.service.jvm.JvmService;
import com.cerner.jwala.service.jvm.JvmStateService;
import com.cerner.jwala.service.resource.ResourceService;
import com.cerner.jwala.service.resource.impl.ResourceGeneratorType;
import com.cerner.jwala.service.webserver.component.ClientFactoryHelper;
import com.jcraft.jsch.JSchException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.anyObject;
import static org.mockito.Mockito.contains;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class JvmServiceImplVerifyTest extends VerificationBehaviorSupport {

    @Mock
    private JvmPersistenceService mockJvmPersistenceService;

    @Mock
    private GroupService mockGroupService;

    @Mock
    private User mockUser;

    @Mock
    private FileManager mockFileManager;

    @Mock
    private ApplicationService mockApplicationService;

    @Mock
    private SimpMessagingTemplate mockMessagingTemplate;

    @Mock
    private GroupStateNotificationService mockGroupStateNotificationService;

    @Mock
    private ClientFactoryHelper mockClientFactoryHelper;

    @Mock
    private ResourceService mockResourceService;

    @Mock
    private JvmControlService mockJvmControlService;

    @Mock
    private BinaryDistributionService mockBinaryDistributionService;

    @Mock
    private BinaryDistributionLockManager mockBinaryDistributionLockManager;

    @Mock
    private JvmStateService mockJvmStateService;

    private JvmService jvmService;

    private final Map<String, ReentrantReadWriteLock> lockMap = new HashMap<>();

    @Before
    public void setup() {

        System.setProperty(ApplicationProperties.PROPERTIES_ROOT_PATH, "./src/test/resources");
        initMocks(this);
        final JvmServiceImpl jvmServiceImpl = new JvmServiceImpl(mockJvmPersistenceService, mockGroupService,
                mockApplicationService, mockFileManager, mockMessagingTemplate, mockGroupStateNotificationService,
                mockResourceService, mockClientFactoryHelper, "/topic/server-states", mockJvmControlService,
                mockBinaryDistributionService, mockBinaryDistributionLockManager);
        jvmServiceImpl.setJvmStateService(mockJvmStateService);
        jvmService = jvmServiceImpl;
    }

    @Test
    public void testCreateValidate() {
        System.setProperty(ApplicationProperties.PROPERTIES_ROOT_PATH, "./src/test/resources");

        final CreateJvmRequest createJvmRequest = mock(CreateJvmRequest.class);
        final CreateJvmAndAddToGroupsRequest createJvmAndAddToGroupsRequest = mock(CreateJvmAndAddToGroupsRequest.class);
        final Jvm jvm = new Jvm(new Identifier<Jvm>(99L), "testJvm", new HashSet<Group>());

        when(mockJvmPersistenceService.createJvm(any(CreateJvmRequest.class))).thenReturn(jvm);
        when(mockJvmPersistenceService.getJvm(any(Identifier.class))).thenReturn(jvm);
        when(createJvmAndAddToGroupsRequest.getCreateCommand()).thenReturn(createJvmRequest);

        jvmService.createJvm(createJvmAndAddToGroupsRequest, mockUser);

        verify(createJvmAndAddToGroupsRequest, times(1)).validate();
        verify(mockJvmPersistenceService, times(1)).createJvm(createJvmRequest);

        System.clearProperty(ApplicationProperties.PROPERTIES_ROOT_PATH);
    }

    @Test
    public void testCreateValidateAdd() {

        final CreateJvmRequest createJvmRequest = mock(CreateJvmRequest.class);
        final CreateJvmAndAddToGroupsRequest command = mock(CreateJvmAndAddToGroupsRequest.class);
        final Jvm jvm = mockJvmWithId(new Identifier<Jvm>(-123456L));
        final Set<AddJvmToGroupRequest> addCommands = createMockedAddRequests(3);
        final Set<Identifier<Group>> groupsSet = new HashSet<>();
        groupsSet.add(new Identifier<Group>(111L));

        when(command.toAddRequestsFor(eq(jvm.getId()))).thenReturn(addCommands);
        when(command.getCreateCommand()).thenReturn(createJvmRequest);
        when(command.getGroups()).thenReturn(groupsSet);
        when(mockJvmPersistenceService.createJvm(createJvmRequest)).thenReturn(jvm);
        when(mockJvmPersistenceService.getJvm(any(Identifier.class))).thenReturn(jvm);

        jvmService.createJvm(command, mockUser);

        verify(command, times(1)).validate();
        verify(mockJvmPersistenceService, times(1)).createJvm(createJvmRequest);
        for (final AddJvmToGroupRequest addCommand : addCommands) {
            verify(mockGroupService, times(1)).addJvmToGroup(matchCommand(addCommand),
                    eq(mockUser));
        }
    }

    @Test
    public void testCreateValidateInheritsDefaultTemplates() throws IOException {
        System.setProperty(ApplicationProperties.PROPERTIES_ROOT_PATH, "./src/test/resources");

        final CreateJvmRequest createJvmRequest = mock(CreateJvmRequest.class);
        final CreateJvmAndAddToGroupsRequest createJvmAndAddToGroupsRequest = mock(CreateJvmAndAddToGroupsRequest.class);
        final Group mockGroup = mock(Group.class);

        Set<Group> groupSet = new HashSet<>();
        groupSet.add(mockGroup);
        List<String> templateNames = new ArrayList<>();
        templateNames.add("template-name");
        List<String> appTemplateNames = new ArrayList<>();
        appTemplateNames.add("app-template-name");
        final Jvm jvm = new Jvm(new Identifier<Jvm>(99L), "testJvm", groupSet);

        ResourceTemplateMetaData mockMetaData = mock(ResourceTemplateMetaData.class);
        when(mockMetaData.getDeployFileName()).thenReturn("app-context.xml");

        when(createJvmAndAddToGroupsRequest.getCreateCommand()).thenReturn(createJvmRequest);
        when(mockJvmPersistenceService.createJvm(any(CreateJvmRequest.class))).thenReturn(jvm);
        when(mockJvmPersistenceService.getJvm(any(Identifier.class))).thenReturn(jvm);
        when(mockResourceService.generateResourceGroup()).thenReturn(mock(ResourceGroup.class));
        when(mockResourceService.getAppTemplate(anyString(), anyString(), anyString())).thenReturn("<context>xml</context>");
        when(mockGroup.getName()).thenReturn("mock-group-name");
        when(mockGroupService.getGroupJvmsResourceTemplateNames(anyString())).thenReturn(templateNames);
        when(mockGroupService.getGroupJvmResourceTemplate(anyString(), anyString(), any(ResourceGroup.class), anyBoolean())).thenReturn("<server>xml</server>");
        when(mockGroupService.getGroupJvmResourceTemplateMetaData(anyString(), anyString())).thenReturn("{\"deployPath\":\"c:/fake/path\", \"deployFileName\":\"server-deploy.xml\"}");
        when(mockGroupService.getGroupAppsResourceTemplateNames(anyString())).thenReturn(appTemplateNames);
        when(mockGroupService.getGroupAppResourceTemplateMetaData(anyString(), anyString())).thenReturn("{\"deployPath\":\"c:/fake/app/path\", \"deployFileName\":\"app-context.xml\", \"entity\":{\"deployToJvms\":\"true\", \"target\":\"app-target\"}}");
        when(mockResourceService.getTokenizedMetaData(anyString(), Matchers.anyObject(), anyString())).thenReturn(mockMetaData);

        jvmService.createJvm(createJvmAndAddToGroupsRequest, mockUser);

        verify(createJvmAndAddToGroupsRequest, times(1)).validate();
        verify(mockJvmPersistenceService, times(1)).createJvm(createJvmRequest);

        System.clearProperty(ApplicationProperties.PROPERTIES_ROOT_PATH);
    }

    @Test
    public void testCreateValidateInheritsDefaultTemplatesFromMultipleGroups() {
        System.setProperty(ApplicationProperties.PROPERTIES_ROOT_PATH, "./src/test/resources");

        final CreateJvmRequest createJvmRequest = mock(CreateJvmRequest.class);
        final CreateJvmAndAddToGroupsRequest createJvmAndAddToGroupsRequest = mock(CreateJvmAndAddToGroupsRequest.class);
        final Group mockGroup = mock(Group.class);
        final Group mockGroup2 = mock(Group.class);

        Set<Group> groupSet = new HashSet<>();
        groupSet.add(mockGroup);
        groupSet.add(mockGroup2);
        List<String> templateNames = new ArrayList<>();
        List<String> appTemplateNames = new ArrayList<>();
        final Jvm jvm = new Jvm(new Identifier<Jvm>(99L), "testJvm", groupSet);

        when(createJvmAndAddToGroupsRequest.getCreateCommand()).thenReturn(createJvmRequest);
        when(mockJvmPersistenceService.createJvm(any(CreateJvmRequest.class))).thenReturn(jvm);
        when(mockJvmPersistenceService.getJvm(any(Identifier.class))).thenReturn(jvm);
        when(mockResourceService.generateResourceGroup()).thenReturn(mock(ResourceGroup.class));
        when(mockGroup.getName()).thenReturn("mock-group-name");
        when(mockGroupService.getGroupJvmsResourceTemplateNames(anyString())).thenReturn(templateNames);
        when(mockGroupService.getGroupAppsResourceTemplateNames(anyString())).thenReturn(appTemplateNames);

        jvmService.createJvm(createJvmAndAddToGroupsRequest, mockUser);

        verify(createJvmAndAddToGroupsRequest, times(1)).validate();
        verify(mockJvmPersistenceService, times(1)).createJvm(createJvmRequest);

        System.clearProperty(ApplicationProperties.PROPERTIES_ROOT_PATH);
    }

    @Test (expected = InternalErrorException.class)
    public void testCreateValidateInheritsDefaultTemplatesJvmTemplateThrowsIOException() throws IOException {
        System.setProperty(ApplicationProperties.PROPERTIES_ROOT_PATH, "./src/test/resources");

        final CreateJvmRequest createJvmRequest = mock(CreateJvmRequest.class);
        final CreateJvmAndAddToGroupsRequest createJvmAndAddToGroupsRequest = mock(CreateJvmAndAddToGroupsRequest.class);
        final Group mockGroup = mock(Group.class);

        Set<Group> groupSet = new HashSet<>();
        groupSet.add(mockGroup);
        List<String> templateNames = new ArrayList<>();
        templateNames.add("template-name");
        final Jvm jvm = new Jvm(new Identifier<Jvm>(99L), "testJvm", groupSet);

        when(createJvmAndAddToGroupsRequest.getCreateCommand()).thenReturn(createJvmRequest);
        when(mockJvmPersistenceService.createJvm(any(CreateJvmRequest.class))).thenReturn(jvm);
        when(mockJvmPersistenceService.getJvm(any(Identifier.class))).thenReturn(jvm);
        when(mockResourceService.generateResourceGroup()).thenReturn(mock(ResourceGroup.class));
        when(mockResourceService.getTokenizedMetaData(anyString(), Matchers.anyObject(), anyString())).thenThrow(new IOException("FAIL converting meta data"));
        when(mockGroup.getName()).thenReturn("mock-group-name");
        when(mockGroupService.getGroupJvmsResourceTemplateNames(anyString())).thenReturn(templateNames);
        when(mockGroupService.getGroupJvmResourceTemplate(anyString(), anyString(), any(ResourceGroup.class), anyBoolean())).thenReturn("<server>xml</server>");
        when(mockGroupService.getGroupJvmResourceTemplateMetaData(anyString(), anyString())).thenReturn("{deployPath:c:/fake/path}");

        jvmService.createJvm(createJvmAndAddToGroupsRequest, mockUser);

        verify(createJvmRequest, times(1)).validate();
        verify(mockJvmPersistenceService, times(1)).createJvm(createJvmRequest);

        System.clearProperty(ApplicationProperties.PROPERTIES_ROOT_PATH);
    }

    @Test (expected = InternalErrorException.class)
    public void testCreateValidateInheritsDefaultTemplatesAppTemplateThrowsIOException() {
        System.setProperty(ApplicationProperties.PROPERTIES_ROOT_PATH, "./src/test/resources");

        final CreateJvmRequest createJvmRequest = mock(CreateJvmRequest.class);
        final CreateJvmAndAddToGroupsRequest createJvmAndAddToGroupsRequest = mock(CreateJvmAndAddToGroupsRequest.class);
        final Group mockGroup = mock(Group.class);

        Set<Group> groupSet = new HashSet<>();
        groupSet.add(mockGroup);
        List<String> templateNames = new ArrayList<>();
        List<String> appTemplateNames = new ArrayList<>();
        appTemplateNames.add("app-template-name");
        final Jvm jvm = new Jvm(new Identifier<Jvm>(99L), "testJvm", groupSet);

        when(createJvmAndAddToGroupsRequest.getCreateCommand()).thenReturn(createJvmRequest);
        when(mockJvmPersistenceService.createJvm(any(CreateJvmRequest.class))).thenReturn(jvm);
        when(mockJvmPersistenceService.getJvm(any(Identifier.class))).thenReturn(jvm);
        when(mockResourceService.generateResourceGroup()).thenReturn(mock(ResourceGroup.class));
        when(mockResourceService.getAppTemplate(anyString(), anyString(), anyString())).thenReturn("<context>xml</context>");
        when(mockGroup.getName()).thenReturn("mock-group-name");
        when(mockGroupService.getGroupJvmsResourceTemplateNames(anyString())).thenReturn(templateNames);
        when(mockGroupService.getGroupAppsResourceTemplateNames(anyString())).thenReturn(appTemplateNames);
        when(mockGroupService.getGroupAppResourceTemplateMetaData(anyString(), anyString())).thenReturn("{deployPath:c:/fake/app/path}");

        jvmService.createJvm(createJvmAndAddToGroupsRequest, mockUser);

        verify(createJvmRequest, times(1)).validate();
        verify(mockJvmPersistenceService, times(1)).createJvm(createJvmRequest);

        System.clearProperty(ApplicationProperties.PROPERTIES_ROOT_PATH);
    }

    @Test
    public void testUpdateJvmShouldValidateCommand() {

        final UpdateJvmRequest updateJvmRequest = mock(UpdateJvmRequest.class);
        final Set<AddJvmToGroupRequest> addCommands = createMockedAddRequests(5);

        when(updateJvmRequest.getAssignmentCommands()).thenReturn(addCommands);

        jvmService.updateJvm(updateJvmRequest,
                mockUser);

        verify(updateJvmRequest, times(1)).validate();
        verify(mockJvmPersistenceService, times(1)).updateJvm(updateJvmRequest);
        verify(mockJvmPersistenceService, times(1)).removeJvmFromGroups(Matchers.<Identifier<Jvm>>anyObject());
        for (final AddJvmToGroupRequest addCommand : addCommands) {
            verify(mockGroupService, times(1)).addJvmToGroup(matchCommand(addCommand),
                    eq(mockUser));
        }
    }

    @Test
    public void testRemoveJvm() {

        final Identifier<Jvm> id = new Identifier<>(-123456L);
        Jvm mockJvm = mockJvmWithId(id);
        when(mockJvmPersistenceService.getJvm(any(Identifier.class))).thenReturn(mockJvm);
        when(mockJvm.getState()).thenReturn(JvmState.JVM_STOPPED);

        when(mockJvmControlService.controlJvm(any(ControlJvmRequest.class), any(User.class))).thenReturn(new CommandOutput(new ExecReturnCode(0), "SUCCESS", ""));

        jvmService.removeJvm(id, mockUser);

        verify(mockJvmPersistenceService, times(1)).removeJvm(eq(id));
    }

    @Test (expected = InternalErrorException.class)
    public void testRemoveJvmInStartedState() {
        final Identifier<Jvm> id = new Identifier<>(-123456L);
        Jvm mockJvm = mockJvmWithId(id);
        when(mockJvmPersistenceService.getJvm(any(Identifier.class))).thenReturn(mockJvm);
        when(mockJvm.getState()).thenReturn(JvmState.JVM_STARTED);

        jvmService.removeJvm(id, mockUser);
    }

    @Test
    public void testDeleteJvmWindowsServiceForNonExistentService() {
        Jvm mockJvm = mock(Jvm.class);
        ControlJvmRequest controlJvmRequest = new ControlJvmRequest(new Identifier<Jvm>(123L), JvmControlOperation.DELETE_SERVICE);
        CommandOutput commandOutput = new CommandOutput(new ExecReturnCode(36), "", "Fail for non-existent service");

        when(mockJvm.getState()).thenReturn(JvmState.JVM_STOPPED);
        when(mockJvm.getJvmName()).thenReturn("jvm-name-delete-service");
        when(mockJvmControlService.controlJvm(eq(controlJvmRequest), any(User.class))).thenReturn(commandOutput);
        
        jvmService.deleteJvmWindowsService(controlJvmRequest, mockJvm, mockUser);
        verify(mockJvmControlService).controlJvm(eq(controlJvmRequest), eq(mockUser));
    }

    @Test (expected = InternalErrorException.class)
    public void testDeleteJvmWindowsServiceFailsForOtherErrorCode() {
        Jvm mockJvm = mock(Jvm.class);
        ControlJvmRequest controlJvmRequest = new ControlJvmRequest(new Identifier<Jvm>(123L), JvmControlOperation.DELETE_SERVICE);
        CommandOutput commandOutput = new CommandOutput(new ExecReturnCode(1111), "", "Fail some other reason than service does not exist");

        when(mockJvm.getState()).thenReturn(JvmState.JVM_STOPPED);
        when(mockJvm.getJvmName()).thenReturn("jvm-name-delete-service");
        when(mockJvmControlService.controlJvm(eq(controlJvmRequest), any(User.class))).thenReturn(commandOutput);

        jvmService.deleteJvmWindowsService(controlJvmRequest, mockJvm, mockUser);
        verify(mockJvmControlService).controlJvm(eq(controlJvmRequest), eq(mockUser));
    }

    @Test
    public void testGetAll() {

        jvmService.getJvms();

        verify(mockJvmPersistenceService, times(1)).getJvms();
    }


    @Test
    public void testGenerateConfig() throws IOException {

        final Jvm jvm = new Jvm(new Identifier<Jvm>(-123456L),
                "jvm-name", "host-name", new HashSet<Group>(), 80, 443, 443, 8005, 8009, new Path("/"),
                "EXAMPLE_OPTS=%someEnv%/someVal", JvmState.JVM_STOPPED, null, null, null, null, null);

        when(mockJvmPersistenceService.findJvmByExactName(eq(jvm.getJvmName()))).thenReturn(jvm);
        final String templateContent = "<server>test</server>";
        when(mockJvmPersistenceService.getJvmTemplate(eq("server.xml"), eq(jvm.getId()))).thenReturn(templateContent);
        when(mockResourceService.generateResourceGroup()).thenReturn(new ResourceGroup());
        when(mockResourceService.generateResourceFile(anyString(), anyString(), any(ResourceGroup.class), any(Jvm.class), any(ResourceGeneratorType.class))).thenReturn(templateContent);
        String generatedXml = jvmService.generateConfigFile(jvm.getJvmName(), "server.xml");

        assert !generatedXml.isEmpty();
    }

    @Test(expected = BadRequestException.class)
    public void testGenerateThrowsExceptionForEmptyTemplate() {
        final Jvm jvm = new Jvm(new Identifier<Jvm>(-123456L),
                "jvm-name", "host-name", new HashSet<Group>(), 80, 443, 443, 8005, 8009, new Path("/"),
                "EXAMPLE_OPTS=%someEnv%/someVal", JvmState.JVM_STOPPED, null, null, null, null, null);

        when(mockJvmPersistenceService.findJvmByExactName(eq(jvm.getJvmName()))).thenReturn(jvm);
        when(mockJvmPersistenceService.getJvmTemplate(eq("server.xml"), eq(jvm.getId()))).thenReturn("");
        jvmService.generateConfigFile(jvm.getJvmName(), "server.xml");
    }

    @Test
    public void testGetSpecific() {

        final Identifier<Jvm> id = new Identifier<>(-123456L);

        jvmService.getJvm(id);

        verify(mockJvmPersistenceService, times(1)).getJvm(eq(id));
    }

    protected Jvm mockJvmWithId(final Identifier<Jvm> anId) {
        final Jvm jvm = mock(Jvm.class);
        when(jvm.getId()).thenReturn(anId);
        return jvm;
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGenerateServerXmlConfig() {
        String testJvmName = "testjvm";
        final Jvm testJvm = new Jvm(new Identifier<Jvm>(99L), testJvmName, new HashSet<Group>());
        when(mockJvmPersistenceService.findJvmByExactName(testJvmName)).thenReturn(testJvm);
        String expectedValue = "<server>xml-content</server>";
        when(mockResourceService.generateResourceGroup()).thenReturn(new ResourceGroup());
        when(mockResourceService.generateResourceFile(anyString(), eq(expectedValue), any(ResourceGroup.class), eq(testJvm), any(ResourceGeneratorType.class))).thenReturn(expectedValue);
        when(mockJvmPersistenceService.getJvmTemplate(anyString(), any(Identifier.class))).thenReturn(expectedValue);

        // happy case
        String serverXml = jvmService.generateConfigFile(testJvmName, "server.xml");
        assertEquals(expectedValue, serverXml);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGenerateContextXmlConfig() {
        String testJvmName = "testjvm";
        final Jvm jvm = new Jvm(new Identifier<Jvm>(99L), testJvmName, new HashSet<Group>());
        when(mockJvmPersistenceService.findJvmByExactName(testJvmName)).thenReturn(jvm);
        String expectedValue = "<server>xml-content</server>";
        when(mockJvmPersistenceService.getJvmTemplate(anyString(), any(Identifier.class))).thenReturn(expectedValue);
        when(mockResourceService.generateResourceGroup()).thenReturn(new ResourceGroup());
        when(mockResourceService.generateResourceFile(anyString(), eq(expectedValue), any(ResourceGroup.class), eq(jvm), any(ResourceGeneratorType.class))).thenReturn(expectedValue);

        // happy case
        String serverXml = jvmService.generateConfigFile(testJvmName, "server.xml");
        assertEquals(expectedValue, serverXml);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGenerateSetenvBatConfig() {
        String testJvmName = "testjvm";
        final Jvm testJvm = new Jvm(new Identifier<Jvm>(99L), testJvmName, new HashSet<Group>());
        when(mockJvmPersistenceService.findJvmByExactName(testJvmName)).thenReturn(testJvm);
        String expectedValue = "<server>xml-content</server>";
        when(mockJvmPersistenceService.getJvmTemplate(anyString(), any(Identifier.class))).thenReturn(expectedValue);
        when(mockResourceService.generateResourceGroup()).thenReturn(new ResourceGroup());
        when(mockResourceService.generateResourceFile(anyString(), eq(expectedValue), any(ResourceGroup.class), eq(testJvm), any(ResourceGeneratorType.class))).thenReturn(expectedValue);

        // happy case
        String serverXml = jvmService.generateConfigFile(testJvmName, "server.xml");
        assertEquals(expectedValue, serverXml);
    }

    @Test
    public void testPerformDiagnosis() throws IOException, URISyntaxException {
        Identifier<Jvm> aJvmId = new Identifier<>(11L);
        Jvm jvm = mock(Jvm.class);
        when(jvm.getId()).thenReturn(aJvmId);
        when(jvm.getStatusUri()).thenReturn(new URI("http://test.com"));
        when(mockJvmPersistenceService.getJvm(aJvmId)).thenReturn(jvm);

        ClientHttpResponse mockResponse = mock(ClientHttpResponse.class);
        when(mockResponse.getStatusCode()).thenReturn(HttpStatus.OK);
        when(mockClientFactoryHelper.requestGet(any(URI.class))).thenReturn(mockResponse);

        String diagnosis = jvmService.performDiagnosis(aJvmId);
        assertTrue(!diagnosis.isEmpty());

        when(mockResponse.getStatusCode()).thenReturn(HttpStatus.REQUEST_TIMEOUT);
        diagnosis = jvmService.performDiagnosis(aJvmId);
        assertTrue(!diagnosis.isEmpty());
    }

    @Test
    public void testPerformDiagnosisThrowsIOException() throws URISyntaxException, IOException {
        Identifier<Jvm> aJvmId = new Identifier<>(11L);
        Jvm jvm = mock(Jvm.class);
        when(jvm.getId()).thenReturn(aJvmId);
        when(jvm.getStatusUri()).thenReturn(new URI("http://test.com"));
        when(mockJvmPersistenceService.getJvm(aJvmId)).thenReturn(jvm);

        when(mockClientFactoryHelper.requestGet(any(URI.class))).thenThrow(new IOException("TEST IO EXCEPTION"));
        String diagnosis = jvmService.performDiagnosis(aJvmId);
        assertTrue(!diagnosis.isEmpty());
    }

    @Test
    public void testPerformDiagnosisThrowsRuntimeException() throws IOException, URISyntaxException {
        Identifier<Jvm> aJvmId = new Identifier<>(11L);
        Jvm jvm = mock(Jvm.class);
        when(jvm.getId()).thenReturn(aJvmId);
        when(jvm.getStatusUri()).thenReturn(new URI("http://test.com"));
        when(mockJvmPersistenceService.getJvm(aJvmId)).thenReturn(jvm);

        when(mockClientFactoryHelper.requestGet(any(URI.class))).thenThrow(new RuntimeException("RUN!!"));
        String diagnosis = jvmService.performDiagnosis(aJvmId);
        assertTrue(!diagnosis.isEmpty());
    }

    @Test
    public void testGetResourceTemplateNames() {
        String testJvmName = "testJvmName";
        ArrayList<String> value = new ArrayList<>();
        when(mockJvmPersistenceService.getResourceTemplateNames(testJvmName)).thenReturn(value);
        value.add("testJvm.tpl");
        List<String> result = jvmService.getResourceTemplateNames(testJvmName);
        assertTrue(result.size() == 1);
    }

    @Test
    public void testGetResourceTemplate() {
        String testJvmName = "testJvmName";
        String resourceTemplateName = "test-resource.tpl";
        Jvm jvm = mock(Jvm.class);
        String expectedValue = "<template>resource</template>";
        when(mockJvmPersistenceService.getResourceTemplate(testJvmName, resourceTemplateName)).thenReturn(expectedValue);
        List<Jvm> jvmList = new ArrayList<>();
        jvmList.add(jvm);
        when(mockJvmPersistenceService.findJvmByExactName(anyString())).thenReturn(jvm);
        when(mockResourceService.generateResourceGroup()).thenReturn(new ResourceGroup());
        when(mockResourceService.generateResourceFile(eq(resourceTemplateName), eq(expectedValue), any(ResourceGroup.class), eq(jvm), any(ResourceGeneratorType.class))).thenReturn(expectedValue);
        String result = jvmService.getResourceTemplate(testJvmName, resourceTemplateName, true);
        assertEquals(expectedValue, result);

        result = jvmService.getResourceTemplate(testJvmName, resourceTemplateName, false);
        assertEquals(expectedValue, result);
    }

    @Test
    public void testUpdateResourceTemplate() {
        String testJvmName = "testJvmName";
        String resourceTemplateName = "test-resource.tpl";
        String template = "<template>update</template>";
        when(mockJvmPersistenceService.updateResourceTemplate(testJvmName, resourceTemplateName, template)).thenReturn(template);
        String result = jvmService.updateResourceTemplate(testJvmName, resourceTemplateName, template);
        assertEquals(template, result);
    }

    @Test
    public void testGenerateInvokeBat() {
        final Jvm jvm = mock(Jvm.class);
        final List<Jvm> jvms = new ArrayList<>();
        jvms.add(jvm);
        when(mockJvmPersistenceService.findJvmByExactName(anyString())).thenReturn(jvm);
        final String expectedValue = "template contents";
        when(mockFileManager.getResourceTypeTemplate(anyString())).thenReturn(expectedValue);
        when(mockResourceService.generateResourceGroup()).thenReturn(new ResourceGroup());
        when(mockResourceService.generateResourceFile(anyString(), anyString(), any(ResourceGroup.class), eq(jvm), any(ResourceGeneratorType.class))).thenReturn(expectedValue);
        final String result = jvmService.generateInvokeBat(anyString());
        assertEquals(expectedValue, result);
    }

    @Test
    public void testGetJvmByName() {
        jvmService.getJvm("testJvm");
        verify(mockJvmPersistenceService).findJvmByExactName("testJvm");
    }

    @Test
    public void testPreviewTemplate() {
        final String jvmName = "jvm-1Test";
        Jvm testJvm = new Jvm(new Identifier<Jvm>(111L), jvmName, "testHost", new HashSet<Group>(), 9101, 9102, 9103, -1, 9104, new Path("./"), "", JvmState.JVM_STOPPED, "", null, null, null, null);
        List<Jvm> jvmList = new ArrayList<>();
        jvmList.add(testJvm);
        when(mockJvmPersistenceService.findJvm(anyString(), anyString())).thenReturn(testJvm);
        when(mockResourceService.generateResourceGroup()).thenReturn(new ResourceGroup());
        when(mockResourceService.generateResourceFile(anyString(), anyString(), any(ResourceGroup.class), eq(testJvm), any(ResourceGeneratorType.class))).thenReturn("TEST jvm-1Test TEST");

        String preview = jvmService.previewResourceTemplate("myFile", jvmName, "groupTest", "TEST ${jvm.jvmName} TEST");
        assertEquals("TEST jvm-1Test TEST", preview);
    }

    @Test
    public void testUpdateState() {
        Identifier<Jvm> jvmId = new Identifier<Jvm>(999L);
        jvmService.updateState(jvmId, JvmState.JVM_STOPPED);
        verify(mockJvmPersistenceService).updateState(jvmId, JvmState.JVM_STOPPED, "");
    }

    @Test
    public void testDeployApplicationContextXMLs() {
        final Identifier<Jvm> jvmId = new Identifier<>(2323L);
        final Identifier<Group> groupId = new Identifier<>(222L);
        final Jvm jvm = mockJvmWithId(jvmId);
        when(jvm.getJvmName()).thenReturn("testJvmName");

        List<Group> groupsList = new ArrayList<Group>();
        Group mockGroup = mock(Group.class);
        groupsList.add(mockGroup);
        when(mockGroup.getId()).thenReturn(groupId);
        when(mockGroup.getName()).thenReturn("testGroupName");

        List<Application> appList = new ArrayList<>();
        Application mockApp = mock(Application.class);
        appList.add(mockApp);
        when(mockApp.getName()).thenReturn("testAppName");

        List<String> templateNamesList = new ArrayList<>();
        templateNamesList.add("testAppResource.xml");

        when(mockApplicationService.getResourceTemplateNames(anyString(), anyString())).thenReturn(templateNamesList);
        final User mockUser = mock(User.class);
        when(mockUser.getId()).thenReturn("user-id");

        when(mockApplicationService.findApplications(any(Identifier.class))).thenReturn(appList);
        when(mockJvmPersistenceService.findGroupsByJvm(any(Identifier.class))).thenReturn(groupsList);
        jvmService.deployApplicationContextXMLs(jvm, mockUser);
        verify(mockApplicationService).deployConf(anyString(), anyString(), anyString(), anyString(), any(ResourceGroup.class), any(User.class));
    }

    @Test (expected = InternalErrorException.class)
    public void testCheckSetenvBat() {
        final String jvmName = "test-jvm-check-for-setenvbat";
        when(mockJvmPersistenceService.getResourceTemplate(jvmName, "setenv.bat")).thenReturn("ignore template content, just need to check no exception is thrown");
        jvmService.checkForSetenvBat(jvmName);

        verify(mockJvmPersistenceService).getResourceTemplate(anyString(), anyString());

        when(mockJvmPersistenceService.getResourceTemplate(jvmName, "setenv.bat")).thenThrow(new NonRetrievableResourceTemplateContentException("JVM", "setenv.bat", new Throwable()));
        jvmService.checkForSetenvBat(jvmName);
    }

    @Test
    public void testGenerateAndDeployConfig() throws CommandFailureException, IOException {

        Collection<ResourceType> mockResourceTypes = new ArrayList<>();
        ResourceType mockResource = mock(ResourceType.class);
        mockResourceTypes.add(mockResource);
        CommandOutput commandOutput = mock(CommandOutput.class);
        Jvm mockJvm = mock(Jvm.class);
        ResourceGroup mockResourceGroup = mock(ResourceGroup.class);

        when(mockJvm.getState()).thenReturn(JvmState.JVM_STOPPED);
        when(mockJvm.getJvmName()).thenReturn("test-jvm-deploy-config");
        when(mockJvm.getId()).thenReturn(new Identifier<Jvm>(111L));
        when(commandOutput.getReturnCode()).thenReturn(new ExecReturnCode(0));
        when(mockResource.getEntityType()).thenReturn("jvm");
        when(mockResource.getTemplateName()).thenReturn("ServerXMLTemplate.tpl");
        when(mockResource.getConfigFileName()).thenReturn("server.xml");
        when(mockJvmControlService.secureCopyFile(any(ControlJvmRequest.class), anyString(), anyString(), anyString(), anyBoolean())).thenReturn(commandOutput);
        when(mockJvmControlService.executeCreateDirectoryCommand(any(Jvm.class), anyString())).thenReturn(commandOutput);
        when(mockJvmControlService.executeChangeFileModeCommand(any(Jvm.class), anyString(), anyString(), anyString())).thenReturn(commandOutput);

        when(mockJvmControlService.controlJvm(eq(new ControlJvmRequest(mockJvm.getId(), JvmControlOperation.DELETE_SERVICE)), any(User.class))).thenReturn(commandOutput);
        when(mockJvmControlService.controlJvm(eq(new ControlJvmRequest(mockJvm.getId(), JvmControlOperation.DEPLOY_CONFIG_ARCHIVE)), any(User.class))).thenReturn(commandOutput);
        when(mockJvmControlService.controlJvm(eq(new ControlJvmRequest(mockJvm.getId(), JvmControlOperation.INVOKE_SERVICE)), any(User.class))).thenReturn(commandOutput);

        when(mockJvmPersistenceService.getResourceTemplateMetaData(anyString(), anyString())).thenReturn("{\"deployFileName\":\"server-test-deploy-config.xml\", \"deployPath\":\"c:/fake/test/path\"}");
        when(mockJvmPersistenceService.findJvmByExactName(anyString())).thenReturn(mockJvm);
        when(mockJvmPersistenceService.getJvmTemplate(anyString(), any(Identifier.class))).thenReturn("<server>some xml</server>");

        when(mockResourceService.generateResourceGroup()).thenReturn(mockResourceGroup);
        when(mockResourceService.generateResourceFile(anyString(), anyString(), any(ResourceGroup.class), anyObject(), any(ResourceGeneratorType.class))).thenReturn("<server>some xml</server>");

        Jvm response = jvmService.generateAndDeployJvm(mockJvm.getJvmName(), mockUser);
        assertEquals(response.getJvmName(), mockJvm.getJvmName());

        // test failing the invoke service
        CommandOutput mockExecDataFail = mock(CommandOutput.class);
        when(mockExecDataFail.getReturnCode()).thenReturn(new ExecReturnCode(1));
        when(mockExecDataFail.getStandardError()).thenReturn("ERROR");

        when(mockJvmControlService.controlJvm(eq(new ControlJvmRequest(mockJvm.getId(), JvmControlOperation.INVOKE_SERVICE)), any(User.class))).thenReturn(mockExecDataFail);

        boolean exceptionThrown = false;
        try {
            jvmService.generateAndDeployJvm(mockJvm.getJvmName(), mockUser);
        } catch (Exception e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);

        exceptionThrown = false;
        try {
            jvmService.generateAndDeployJvm(mockJvm.getJvmName(), mockUser);
        } catch (Exception e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);

        // test secure copy fails
        when(mockJvmControlService.secureCopyFile(any(ControlJvmRequest.class), anyString(), anyString(), anyString(), anyBoolean())).thenReturn(mockExecDataFail);
        exceptionThrown = false;
        try {
            jvmService.generateAndDeployJvm(mockJvm.getJvmName(), mockUser);
        } catch (Exception e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);

        // test secure copy throws a command failure exception
        ExecCommand execCommand = new ExecCommand("fail command");
        Throwable throwable = new JSchException("Failed scp");
        final CommandFailureException commandFailureException = new CommandFailureException(execCommand, throwable);
        when(mockJvmControlService.secureCopyFile(any(ControlJvmRequest.class), anyString(), anyString(), anyString(), anyBoolean())).thenThrow(commandFailureException);
        exceptionThrown = false;
        try {
            jvmService.generateAndDeployJvm(mockJvm.getJvmName(), mockUser);
        } catch (Exception e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);

        exceptionThrown = false;
        try {
            jvmService.generateAndDeployJvm(mockJvm.getJvmName(), mockUser);
        } catch (Exception e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
//        FileUtils.deleteDirectory(new File("./" + jvm.getJvmName()));
//        FileUtils.deleteDirectory(new File("./" + jvm.getJvmName() + "null"));
    }


    @Test (expected = InternalErrorException.class)
    public void testGenerateAndDeployJVMFailsJVMStarted() {
        Jvm mockJvm = mock(Jvm.class);

        when(mockJvmPersistenceService.findJvmByExactName(anyString())).thenReturn(mockJvm);
        when(mockJvm.getState()).thenReturn(JvmState.JVM_STARTED);
        when(mockJvm.getId()).thenReturn(new Identifier<Jvm>(111L));
        when(mockJvm.getHostName()).thenReturn("testHostName");
        jvmService.generateAndDeployJvm("test-jvm-fails-started", mockUser);
    }

    @Test (expected = InternalErrorException.class)
    public void testGenerateAndDeployJVMFailsCreateDirectory() throws CommandFailureException {
        Jvm mockJvm = mock(Jvm.class);
        CommandOutput commandOutputFails = new CommandOutput(new ExecReturnCode(1), "", "Fail creating the directory");

        when(mockJvmPersistenceService.findJvmByExactName(anyString())).thenReturn(mockJvm);
        when(mockJvmControlService.executeCreateDirectoryCommand(any(Jvm.class), anyString())).thenReturn(commandOutputFails);
        when(mockJvm.getId()).thenReturn(new Identifier<Jvm>(111L));
        when(mockJvm.getState()).thenReturn(JvmState.JVM_STOPPED);

        jvmService.generateAndDeployJvm("test-jvm-fails-started", mockUser);
    }

    @Test (expected = InternalErrorException.class)
    public void testGenerateAndDeployJVMFailsDeployingInvokeService() throws CommandFailureException {
        Jvm mockJvm = mock(Jvm.class);
        CommandOutput commandOutputFails = new CommandOutput(new ExecReturnCode(1), "", "Fail creating the directory");
        CommandOutput commandOutputSucceeds = new CommandOutput(new ExecReturnCode(0), "SUCCESS", "");

        when(mockJvmPersistenceService.findJvmByExactName(anyString())).thenReturn(mockJvm);
        when(mockJvmControlService.executeCreateDirectoryCommand(any(Jvm.class), anyString())).thenReturn(commandOutputSucceeds);
        when(mockJvmControlService.secureCopyFile(any(ControlJvmRequest.class), contains(AemControl.Properties.DEPLOY_CONFIG_ARCHIVE_SCRIPT_NAME.getValue()), anyString(), anyString(), anyBoolean())).thenReturn(commandOutputSucceeds);
        when(mockJvmControlService.secureCopyFile(any(ControlJvmRequest.class), contains(AemControl.Properties.INVOKE_SERVICE_SCRIPT_NAME.getValue()), anyString(), anyString(), anyBoolean())).thenReturn(commandOutputFails);
        when(mockJvm.getId()).thenReturn(new Identifier<Jvm>(111L));
        when(mockJvm.getState()).thenReturn(JvmState.JVM_STOPPED);

        jvmService.generateAndDeployJvm("test-jvm-fails-started", mockUser);
    }

    @Test (expected = InternalErrorException.class)
    public void testGenerateAndDeployJVMFailsChangeFileMode() throws CommandFailureException {
        Jvm mockJvm = mock(Jvm.class);
        CommandOutput commandOutputFails = new CommandOutput(new ExecReturnCode(1), "", "Fail creating the directory");
        CommandOutput commandOutputSucceeds = new CommandOutput(new ExecReturnCode(0), "SUCCESS", "");

        when(mockJvmPersistenceService.findJvmByExactName(anyString())).thenReturn(mockJvm);
        when(mockJvmControlService.executeCreateDirectoryCommand(any(Jvm.class), anyString())).thenReturn(commandOutputSucceeds);
        when(mockJvmControlService.secureCopyFile(any(ControlJvmRequest.class), anyString(), anyString(), anyString(), anyBoolean())).thenReturn(commandOutputSucceeds);
        when(mockJvmControlService.executeChangeFileModeCommand(any(Jvm.class), anyString(), anyString(), anyString())).thenReturn(commandOutputFails);
        when(mockJvm.getId()).thenReturn(new Identifier<Jvm>(111L));
        when(mockJvm.getState()).thenReturn(JvmState.JVM_STOPPED);

        jvmService.generateAndDeployJvm("test-jvm-fails-started", mockUser);
    }
    /*
    @Test
    public void testGenerateAndDeployConfigFailControlService() throws CommandFailureException {
        System.setProperty(ApplicationProperties.PROPERTIES_ROOT_PATH, "./src/test/resources");
        when(jvmService.getJvm(jvm.getJvmName())).thenReturn(jvm);
        when(jvmService.generateConfigFile(jvm.getJvmName(), "server.xml")).thenReturn("<server>xml-content</server>");
        when(jvmService.generateConfigFile(jvm.getJvmName(), "context.xml")).thenReturn("<content>xml-content</content>");
        when(jvmService.generateConfigFile(jvm.getJvmName(), "setenv.bat")).thenReturn("SET TEST=xxtestxx");
        when(jvmService.generateInvokeBat(jvm.getJvmName())).thenReturn("REM invoke service");
        final CommandOutput successCommandOutput = new CommandOutput(new ExecReturnCode(0), "", "");
        when(jvmControlService.secureCopyFile(any(ControlJvmRequest.class), anyString(), anyString(), anyString())).thenReturn(successCommandOutput);
        when(jvmControlService.executeCreateDirectoryCommand(any(Jvm.class), anyString())).thenReturn(successCommandOutput);
        when(jvmControlService.executeChangeFileModeCommand(any(Jvm.class), anyString(), anyString(), anyString())).thenReturn(successCommandOutput);

        when(jvmControlService.controlJvm(any(ControlJvmRequest.class), any(User.class))).thenReturn(new CommandOutput(new ExecReturnCode(1), "", "FAIL CONTROL SERVICE"));

        ControlJvmRequest deleteServiceRequest = new ControlJvmRequest(jvm.getId(), JvmControlOperation.DELETE_SERVICE);
        when(jvmControlService.controlJvm(eq(deleteServiceRequest), any(User.class))).thenReturn(new CommandOutput(new ExecReturnCode(0), "DELETE SUCCESS", ""));

        final Response response = jvmService.generateAndDeployJvm(jvm.getJvmName(), authenticatedUser);
        System.err.println("JMJM " + ((ApplicationResponse) response.getEntity()).getApplicationResponseContent());
        assertEquals(CommandOutputReturnCode.FAILED.getDesc(), ((Map) (((ApplicationResponse) response.getEntity()).getApplicationResponseContent())).get("message"));
    }

    @Test
    public void testGenerateAndDeployConfigFailSecureCopyService() throws CommandFailureException {
        System.setProperty(ApplicationProperties.PROPERTIES_ROOT_PATH, "./src/test/resources");
        when(jvmService.getJvm(jvm.getJvmName())).thenReturn(jvm);
        when(jvmService.generateConfigFile(jvm.getJvmName(), "server.xml")).thenReturn("<server>xml-content</server>");
        when(jvmService.generateConfigFile(jvm.getJvmName(), "context.xml")).thenReturn("<content>xml-content</content>");
        when(jvmService.generateConfigFile(jvm.getJvmName(), "setenv.bat")).thenReturn("SET TEST=xxtestxx");
        when(jvmControlService.secureCopyFile(any(ControlJvmRequest.class), anyString(), anyString(), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(1), "", "FAIL THE SERVICE SECURE COPY TEST"));
        final CommandOutput successCommandOutput = new CommandOutput(new ExecReturnCode(0), "", "");
        when(jvmControlService.executeCreateDirectoryCommand(any(Jvm.class), anyString())).thenReturn(successCommandOutput);
        when(jvmControlService.executeChangeFileModeCommand(any(Jvm.class), anyString(), anyString(), anyString())).thenReturn(successCommandOutput);

        when(jvmControlService.controlJvm(new ControlJvmRequest(jvm.getId(), JvmControlOperation.DELETE_SERVICE), authenticatedUser.getUser())).thenReturn(successCommandOutput);

        final Response response = jvmServiceRest.generateAndDeployJvm(jvm.getJvmName(), authenticatedUser);
        assertEquals("Failed to secure copy ./src/test/resources/deploy-config-tar.sh during the creation of jvmName", ((Map) (((ApplicationResponse) response.getEntity()).getApplicationResponseContent())).get("message"));
    }
*/


    @Test
    public void testGenerateAndDeployFile() throws CommandFailureException, IOException {
        CommandOutput mockExecData = mock(CommandOutput.class);
        final Jvm mockJvm = mockJvmWithId(new Identifier<Jvm>(111L));
        ResourceTemplateMetaData mockMetaData = mock(ResourceTemplateMetaData.class);

        when(mockJvm.getJvmName()).thenReturn("test-jvm-deploy-file");
        when(mockJvm.getState()).thenReturn(JvmState.JVM_STOPPED);
        when(mockExecData.getReturnCode()).thenReturn(new ExecReturnCode(0));
        when(mockJvmPersistenceService.findJvmByExactName(anyString())).thenReturn(mockJvm);
        when(mockResourceService.generateResourceFile(anyString(), anyString(), any(ResourceGroup.class), anyString(), any(ResourceGeneratorType.class))).thenReturn("<server>xml</server>");
        when(mockJvmPersistenceService.getResourceTemplateMetaData(anyString(), anyString())).thenReturn("{\"deployFileName\":\"server.xml\", \"deployPath\":\"/\",\"contentType\":\"text/plain\"}");
        when(mockJvmPersistenceService.getJvmTemplate(anyString(), any(Identifier.class))).thenReturn("<server>xml</server>");
        when(mockJvmControlService.secureCopyFile(any(ControlJvmRequest.class), anyString(), anyString(), anyString(), anyBoolean())).thenReturn(mockExecData);
        when(mockJvmControlService.executeCreateDirectoryCommand(any(Jvm.class), anyString())).thenReturn(mockExecData);
        when(mockResourceService.generateResourceGroup()).thenReturn(new ResourceGroup());
        when(mockResourceService.generateResourceFile(anyString(), anyString(), any(ResourceGroup.class), any(), any(ResourceGeneratorType.class))).thenReturn("{\"deployFileName\":\"server.xml\", \"deployPath\":\"/\",\"contentType\":\"text/plain\"}");
        when(mockMetaData.getDeployFileName()).thenReturn("server.xml");
        when(mockMetaData.getDeployPath()).thenReturn("/");
        when(mockMetaData.getContentType()).thenReturn("text/plain");
        when(mockResourceService.getTokenizedMetaData(anyString(), Matchers.anyObject(), anyString())).thenReturn(mockMetaData);
        Jvm jvm = jvmService.generateAndDeployFile("test-jvm-deploy-file", "server.xml", mockUser);
        assertEquals(mockJvm, jvm);

        when(mockExecData.getReturnCode()).thenReturn(new ExecReturnCode(1));
        when(mockExecData.getStandardError()).thenReturn("ERROR");
        when(mockJvmControlService.secureCopyFile(any(ControlJvmRequest.class), anyString(), anyString(), anyString(), anyBoolean())).thenReturn(mockExecData);
        boolean exceptionThrown = false;
        try {
            jvmService.generateAndDeployFile(jvm.getJvmName(), "server.xml", mockUser);
        } catch (Exception e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);

        exceptionThrown = false;
        try {
            jvmService.generateAndDeployFile(jvm.getJvmName(), "server.xml", mockUser);
        } catch (InternalErrorException e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);

        when(mockJvmControlService.secureCopyFile(any(ControlJvmRequest.class), anyString(), anyString(), anyString(), anyBoolean())).thenThrow(new CommandFailureException(new ExecCommand("fail for secure copy"), new Throwable("test fail")));
        exceptionThrown = false;
        try {
            jvmService.generateAndDeployFile(jvm.getJvmName(), "server.xml", mockUser);
        } catch (InternalErrorException e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);

        exceptionThrown = false;
        try {
            jvmService.generateAndDeployFile(jvm.getJvmName(), "server.xml", mockUser);
        } catch (InternalErrorException e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);

//        FileUtils.deleteDirectory(new File("./" + jvm.getJvmName()));
    }

    @Test(expected = InternalErrorException.class)
    public void testGenerateAndDeployFileJvmStarted() {
        Jvm mockJvm = mock(Jvm.class);
        when(mockJvm.getState()).thenReturn(JvmState.JVM_STARTED);
        when(mockJvm.getId()).thenReturn(new Identifier<Jvm>(11111L));
        when(mockJvmPersistenceService.findJvmByExactName(anyString())).thenReturn(mockJvm);
        jvmService.generateAndDeployFile("jvmName", "fileName", mockUser);
    }
}
