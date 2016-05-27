package com.siemens.cto.aem.service.jvm.impl;

import com.siemens.cto.aem.common.domain.model.app.Application;
import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.jvm.Jvm;
import com.siemens.cto.aem.common.domain.model.jvm.JvmState;
import com.siemens.cto.aem.common.domain.model.path.Path;
import com.siemens.cto.aem.common.domain.model.resource.ResourceGroup;
import com.siemens.cto.aem.common.domain.model.user.User;
import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.common.properties.ApplicationProperties;
import com.siemens.cto.aem.common.request.group.AddJvmToGroupRequest;
import com.siemens.cto.aem.common.request.jvm.CreateJvmAndAddToGroupsRequest;
import com.siemens.cto.aem.common.request.jvm.CreateJvmRequest;
import com.siemens.cto.aem.common.request.jvm.UpdateJvmRequest;
import com.siemens.cto.aem.common.request.jvm.UploadJvmTemplateRequest;
import com.siemens.cto.aem.persistence.jpa.domain.resource.config.template.JpaJvmConfigTemplate;
import com.siemens.cto.aem.persistence.service.JvmPersistenceService;
import com.siemens.cto.aem.service.VerificationBehaviorSupport;
import com.siemens.cto.aem.service.app.ApplicationService;
import com.siemens.cto.aem.service.group.GroupService;
import com.siemens.cto.aem.service.group.GroupStateNotificationService;
import com.siemens.cto.aem.service.jvm.JvmService;
import com.siemens.cto.aem.service.resource.ResourceService;
import com.siemens.cto.aem.service.state.StateNotificationService;
import com.siemens.cto.aem.service.webserver.component.ClientFactoryHelper;
import com.siemens.cto.toc.files.FileManager;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
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
    private StateNotificationService mockStateNotificationService;

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

    private JvmService jvmService;

    private JvmServiceImpl jvmServiceImpl;

    @Before
    public void setup() {
        initMocks(this);
        jvmServiceImpl = new JvmServiceImpl(mockJvmPersistenceService, mockGroupService, mockApplicationService, mockFileManager, mockStateNotificationService,
                mockMessagingTemplate, mockGroupStateNotificationService, mockResourceService, mockClientFactoryHelper,
                "/topic/server-states");
        jvmService = jvmServiceImpl;
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCreateValidate() {
        System.setProperty(ApplicationProperties.PROPERTIES_ROOT_PATH, "./src/test/resources");

        final CreateJvmRequest createJvmRequest = mock(CreateJvmRequest.class);
        final Jvm jvm = new Jvm(new Identifier<Jvm>(99L), "testJvm", new HashSet<Group>());
        when(mockJvmPersistenceService.createJvm(any(CreateJvmRequest.class))).thenReturn(jvm);

        jvmService.createJvm(createJvmRequest, mockUser);

        verify(createJvmRequest, times(1)).validate();
        verify(mockJvmPersistenceService, times(1)).createJvm(createJvmRequest);

        System.clearProperty(ApplicationProperties.PROPERTIES_ROOT_PATH);
    }

    @Test
    public void testCreateValidateAdd() {

        final CreateJvmRequest createJvmRequest = mock(CreateJvmRequest.class);
        final CreateJvmAndAddToGroupsRequest command = mock(CreateJvmAndAddToGroupsRequest.class);
        final Jvm jvm = mockJvmWithId(new Identifier<Jvm>(-123456L));
        final Set<AddJvmToGroupRequest> addCommands = createMockedAddRequests(3);

        when(command.toAddRequestsFor(eq(jvm.getId()))).thenReturn(addCommands);
        when(command.getCreateCommand()).thenReturn(createJvmRequest);
        when(mockJvmPersistenceService.createJvm(createJvmRequest)).thenReturn(jvm);

        jvmService.createAndAssignJvm(command, mockUser);

        verify(createJvmRequest, times(1)).validate();
        verify(mockJvmPersistenceService, times(1)).createJvm(createJvmRequest);
        for (final AddJvmToGroupRequest addCommand : addCommands) {
            verify(mockGroupService, times(1)).addJvmToGroup(matchCommand(addCommand),
                    eq(mockUser));
        }
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

        jvmService.removeJvm(id);

        verify(mockJvmPersistenceService, times(1)).removeJvm(eq(id));
    }

    @Test
    public void testFindByName() {

        final String fragment = "unused";

        jvmService.findJvms(fragment);

        verify(mockJvmPersistenceService, times(1)).findJvms(eq(fragment));
    }

    @Test(expected = BadRequestException.class)
    public void testFindByInvalidName() {

        final String badFragment = "";

        jvmService.findJvms(badFragment);
    }

    @Test
    public void testFindByGroup() {

        final Identifier<Group> id = new Identifier<>(-123456L);

        jvmService.findJvms(id);

        verify(mockJvmPersistenceService, times(1)).findJvmsBelongingTo(eq(id));
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
                "EXAMPLE_OPTS=%someEnv%/someVal", JvmState.JVM_STOPPED, null, null, null);

        when(mockJvmPersistenceService.findJvmByExactName(eq(jvm.getJvmName()))).thenReturn(jvm);
        final String templateContent = "<server>test</server>";
        when(mockJvmPersistenceService.getJvmTemplate(eq("server.xml"), eq(jvm.getId()))).thenReturn(templateContent);
        when(mockResourceService.generateResourceGroup()).thenReturn(new ResourceGroup());
        when(mockResourceService.generateResourceFile(anyString(), any(ResourceGroup.class), any(Jvm.class))).thenReturn(templateContent);
        String generatedXml = jvmService.generateConfigFile(jvm.getJvmName(), "server.xml");

        assert !generatedXml.isEmpty();
    }

    @Test(expected = BadRequestException.class)
    public void testGenerateThrowsExceptionForEmptyTemplate() {
        final Jvm jvm = new Jvm(new Identifier<Jvm>(-123456L),
                "jvm-name", "host-name", new HashSet<Group>(), 80, 443, 443, 8005, 8009, new Path("/"),
                "EXAMPLE_OPTS=%someEnv%/someVal", JvmState.JVM_STOPPED, null, null, null);

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
        when(mockResourceService.generateResourceFile(eq(expectedValue), any(ResourceGroup.class), eq(testJvm))).thenReturn(expectedValue);
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
        when(mockResourceService.generateResourceFile(eq(expectedValue), any(ResourceGroup.class), eq(jvm))).thenReturn(expectedValue);

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
        when(mockResourceService.generateResourceFile(eq(expectedValue), any(ResourceGroup.class), eq(testJvm))).thenReturn(expectedValue);

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
        when(mockResourceService.generateResourceFile(eq(expectedValue), any(ResourceGroup.class), eq(jvm))).thenReturn(expectedValue);
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
        when(mockResourceService.generateResourceFile(anyString(), any(ResourceGroup.class), eq(jvm))).thenReturn(expectedValue);
        final String result = jvmService.generateInvokeBat(anyString());
        assertEquals(expectedValue, result);
    }

    @Test
    public void testGetJpaJvm() {
        jvmService.getJpaJvm(new Identifier<Jvm>(1L), false);
        verify(mockJvmPersistenceService).getJpaJvm(new Identifier<Jvm>(1L), false);
    }

    @Test
    public void testGetJvmByName() {
        jvmService.getJvm("testJvm");
        verify(mockJvmPersistenceService).findJvmByExactName("testJvm");
    }

    @Test
    public void testPreviewTemplate() {
        final String jvmName = "jvm-1Test";
        Jvm testJvm = new Jvm(new Identifier<Jvm>(111L), jvmName, "testHost", new HashSet<Group>(), 9101, 9102, 9103, -1, 9104, new Path("./"), "", JvmState.JVM_STOPPED, "", null, null);
        List<Jvm> jvmList = new ArrayList<>();
        jvmList.add(testJvm);
        when(mockJvmPersistenceService.findJvm(anyString(), anyString())).thenReturn(testJvm);
        when(mockResourceService.generateResourceGroup()).thenReturn(new ResourceGroup());
        when(mockResourceService.generateResourceFile(anyString(), any(ResourceGroup.class), eq(testJvm))).thenReturn("TEST jvm-1Test TEST");

        String preview = jvmService.previewResourceTemplate(jvmName, "groupTest", "TEST ${jvm.jvmName} TEST");
        assertEquals("TEST jvm-1Test TEST", preview);
    }

    @Test
    public void testUploadTemplateXML() throws FileNotFoundException {
        Jvm mockJvm = mock(Jvm.class);
        when(mockJvm.getId()).thenReturn(new Identifier<Jvm>(99L));
        UploadJvmTemplateRequest uploadRequest = new UploadJvmTemplateRequest(mockJvm, "ServerXMLTemplate.tpl",
                new FileInputStream(new File("./src/test/resources/ServerXMLTemplate.tpl")), StringUtils.EMPTY) {
            @Override
            public String getConfFileName() {
                return "server.xml";
            }
        };
        jvmService.uploadJvmTemplateXml(uploadRequest, mockUser);
        verify(mockJvmPersistenceService).uploadJvmTemplateXml(any(UploadJvmTemplateRequest.class));
    }

    @Test
    public void testUpdateState() {
        Identifier<Jvm> jvmId = new Identifier<Jvm>(999L);
        jvmService.updateState(jvmId, JvmState.JVM_STOPPED);
        verify(mockJvmPersistenceService).updateState(jvmId, JvmState.JVM_STOPPED, "");

        jvmService.updateState(jvmId, JvmState.JVM_STOPPED, "test error status");
        verify(mockJvmPersistenceService).updateState(jvmId, JvmState.JVM_STOPPED, "test error status");
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

        when(mockApplicationService.getResourceTemplateNames(anyString())).thenReturn(templateNamesList);
        when(mockApplicationService.findApplications(any(Identifier.class))).thenReturn(appList);
        when(mockJvmPersistenceService.findGroupsByJvm(any(Identifier.class))).thenReturn(groupsList);
        jvmService.deployApplicationContextXMLs(jvm);
        verify(mockApplicationService).deployConf(anyString(), anyString(), anyString(), anyString(), any(ResourceGroup.class), any(User.class));
    }

    @Test
    public void testGenerateResourceFiles() throws IOException {
        final String jvmName = "testJvmName";
        final JpaJvmConfigTemplate mockJpaJvmConfigTemplate = mock(JpaJvmConfigTemplate.class);
        final ResourceGroup mockResourceGroup = mock(ResourceGroup.class);
        List<JpaJvmConfigTemplate> jpaJvmConfigTemplates = new ArrayList<>();
        jpaJvmConfigTemplates.add(mockJpaJvmConfigTemplate);
        final String metadata = "{\"contentType\":\"text/plain\",\"deployPath\":\"D:/stp/app/instances/testJvmName/bin\",\"deployFileName\": \"test.file\"}";
        Map<String, String> expectedMap = new HashMap<>();
        expectedMap.put("C:/Temp/test.file", "D:/stp/app/instances/testJvmName/bin/test.file");

        when(mockResourceService.generateResourceGroup()).thenReturn(mockResourceGroup);
        when(mockResourceService.generateResourceFile(anyString(), any(ResourceGroup.class), any(Jvm.class))).thenReturn(metadata);
        when(mockJvmPersistenceService.getConfigTemplates(jvmName)).thenReturn(jpaJvmConfigTemplates);

        Map<String, String> result = jvmService.generateResourceFiles(jvmName);
        assertEquals(result.size(), 1);
        for(Map.Entry<String, String> entry:result.entrySet()) {
            FileUtils.forceDelete(new File(entry.getKey()));
        }
    }

    @Test
    public void testGenerateBinaryFile() throws IOException {
        final String jvmName = "testJvmName";
        //final JpaJvmConfigTemplate mockJpaJvmConfigTemplate = mock(JpaJvmConfigTemplate.class);
        final ResourceGroup mockResourceGroup = mock(ResourceGroup.class);
        List<JpaJvmConfigTemplate> jpaJvmConfigTemplates = new ArrayList<>();
        JpaJvmConfigTemplate jpaJvmConfigTemplate = new JpaJvmConfigTemplate();
        jpaJvmConfigTemplate.setTemplateContent("C:/Temp/test.file");
        jpaJvmConfigTemplates.add(jpaJvmConfigTemplate);
        final String metadata = "{\"contentType\":\"application/binary\",\"deployPath\":\"D:/stp/app/instances/testJvmName/bin\",\"deployFileName\": \"test.file\"}";

        when(mockResourceService.generateResourceGroup()).thenReturn(mockResourceGroup);
        when(mockResourceService.generateResourceFile(anyString(), any(ResourceGroup.class), any(Jvm.class))).thenReturn(metadata);
        when(mockJvmPersistenceService.getConfigTemplates(jvmName)).thenReturn(jpaJvmConfigTemplates);

        Map<String, String> result = jvmService.generateResourceFiles(jvmName);
        assertEquals(result.size(), 1);
    }

}
