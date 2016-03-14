package com.siemens.cto.aem.service.jvm.impl;

import com.siemens.cto.aem.common.domain.model.app.Application;
import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.jvm.Jvm;
import com.siemens.cto.aem.common.domain.model.jvm.JvmState;
import com.siemens.cto.aem.common.domain.model.path.Path;
import com.siemens.cto.aem.common.domain.model.user.User;
import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.common.properties.ApplicationProperties;
import com.siemens.cto.aem.common.request.group.AddJvmToGroupRequest;
import com.siemens.cto.aem.common.request.jvm.CreateJvmAndAddToGroupsRequest;
import com.siemens.cto.aem.common.request.jvm.CreateJvmRequest;
import com.siemens.cto.aem.common.request.jvm.UpdateJvmRequest;
import com.siemens.cto.aem.common.request.jvm.UploadJvmTemplateRequest;
import com.siemens.cto.aem.persistence.service.JvmPersistenceService;
import com.siemens.cto.aem.service.VerificationBehaviorSupport;
import com.siemens.cto.aem.service.app.ApplicationService;
import com.siemens.cto.aem.service.group.GroupService;
import com.siemens.cto.aem.service.state.StateNotificationService;
import com.siemens.cto.aem.service.webserver.component.ClientFactoryHelper;
import com.siemens.cto.toc.files.FileManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class JvmServiceImplVerifyTest extends VerificationBehaviorSupport {


    private JvmPersistenceService jvmPersistenceService = mock(JvmPersistenceService.class);
    private GroupService groupService = mock(GroupService.class);
    private User user;
    private FileManager fileManager = mock(FileManager.class);
    private StateNotificationService stateNotificationService = mock(StateNotificationService.class);
    private ApplicationService applicationService = mock(ApplicationService.class);
    private SimpMessagingTemplate mockMessagingTemplate = mock(SimpMessagingTemplate.class);

    @Mock
    private ClientFactoryHelper mockClientFactoryHelper;

    @InjectMocks
    private JvmServiceImpl impl = new JvmServiceImpl(jvmPersistenceService, groupService, applicationService, fileManager,
            stateNotificationService, mockMessagingTemplate);

    @SuppressWarnings("unchecked")
    @Before
    public void setup() {
        user = new User("unused");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCreateValidate() {
        System.setProperty(ApplicationProperties.PROPERTIES_ROOT_PATH, "./src/test/resources");

        final CreateJvmRequest createJvmRequest = mock(CreateJvmRequest.class);
        final Jvm jvm = new Jvm(new Identifier<Jvm>(99L), "testJvm", new HashSet<Group>());
        when(jvmPersistenceService.createJvm(any(CreateJvmRequest.class))).thenReturn(jvm);

        impl.createJvm(createJvmRequest, user);

        verify(createJvmRequest, times(1)).validate();
        verify(jvmPersistenceService, times(1)).createJvm(createJvmRequest);

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
        when(jvmPersistenceService.createJvm(createJvmRequest)).thenReturn(jvm);

        impl.createAndAssignJvm(command,
                user);

        verify(createJvmRequest, times(1)).validate();
        verify(jvmPersistenceService, times(1)).createJvm(createJvmRequest);
        for (final AddJvmToGroupRequest addCommand : addCommands) {
            verify(groupService, times(1)).addJvmToGroup(matchCommand(addCommand),
                    eq(user));
        }
    }

    @Test
    public void testUpdateJvmShouldValidateCommand() {

        final UpdateJvmRequest updateJvmRequest = mock(UpdateJvmRequest.class);
        final Set<AddJvmToGroupRequest> addCommands = createMockedAddRequests(5);

        when(updateJvmRequest.getAssignmentCommands()).thenReturn(addCommands);

        impl.updateJvm(updateJvmRequest,
                user);

        verify(updateJvmRequest, times(1)).validate();
        verify(jvmPersistenceService, times(1)).updateJvm(updateJvmRequest);
        verify(jvmPersistenceService, times(1)).removeJvmFromGroups(Matchers.<Identifier<Jvm>>anyObject());
        for (final AddJvmToGroupRequest addCommand : addCommands) {
            verify(groupService, times(1)).addJvmToGroup(matchCommand(addCommand),
                    eq(user));
        }
    }

    @Test
    public void testRemoveJvm() {

        final Identifier<Jvm> id = new Identifier<>(-123456L);

        impl.removeJvm(id);

        verify(jvmPersistenceService, times(1)).removeJvm(eq(id));
    }

    @Test
    public void testFindByName() {

        final String fragment = "unused";

        impl.findJvms(fragment);

        verify(jvmPersistenceService, times(1)).findJvms(eq(fragment));
    }

    @Test(expected = BadRequestException.class)
    public void testFindByInvalidName() {

        final String badFragment = "";

        impl.findJvms(badFragment);
    }

    @Test
    public void testFindByGroup() {

        final Identifier<Group> id = new Identifier<>(-123456L);

        impl.findJvms(id);

        verify(jvmPersistenceService, times(1)).findJvmsBelongingTo(eq(id));
    }

    @Test
    public void testGetAll() {

        impl.getJvms();

        verify(jvmPersistenceService, times(1)).getJvms();
    }


    @Test
    public void testGenerateConfig() throws IOException {

        final Jvm jvm = new Jvm(new Identifier<Jvm>(-123456L),
                "jvm-name", "host-name", new HashSet<Group>(), 80, 443, 443, 8005, 8009, new Path("/"),
                "EXAMPLE_OPTS=%someEnv%/someVal", JvmState.JVM_STOPPED, null);

        when(jvmPersistenceService.findJvmByExactName(eq(jvm.getJvmName()))).thenReturn(jvm);
        when(jvmPersistenceService.getJvmTemplate(eq("server.xml"), eq(jvm.getId()))).thenReturn("<server>test</server>");
        String generatedXml = impl.generateConfigFile(jvm.getJvmName(), "server.xml");

        assert !generatedXml.isEmpty();
    }

    @Test(expected = BadRequestException.class)
    public void testGenerateThrowsExceptionForEmptyTemplate() {
        final Jvm jvm = new Jvm(new Identifier<Jvm>(-123456L),
                "jvm-name", "host-name", new HashSet<Group>(), 80, 443, 443, 8005, 8009, new Path("/"),
                "EXAMPLE_OPTS=%someEnv%/someVal", JvmState.JVM_STOPPED, null);

        when(jvmPersistenceService.findJvmByExactName(eq(jvm.getJvmName()))).thenReturn(jvm);
        when(jvmPersistenceService.getJvmTemplate(eq("server.xml"), eq(jvm.getId()))).thenReturn("");
        impl.generateConfigFile(jvm.getJvmName(), "server.xml");
    }

    @Test
    public void testGetSpecific() {

        final Identifier<Jvm> id = new Identifier<>(-123456L);

        impl.getJvm(id);

        verify(jvmPersistenceService, times(1)).getJvm(eq(id));
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
        when(jvmPersistenceService.findJvmByExactName(testJvmName)).thenReturn(testJvm);
        String expectedValue = "<server>xml-content</server>";
        when(jvmPersistenceService.getJvmTemplate(anyString(), any(Identifier.class))).thenReturn(expectedValue);

        // happy case
        String serverXml = impl.generateConfigFile(testJvmName, "server.xml");
        assertEquals(expectedValue, serverXml);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGenerateContextXmlConfig() {
        String testJvmName = "testjvm";
        final Jvm jvm = new Jvm(new Identifier<Jvm>(99L), testJvmName, new HashSet<Group>());
        when(jvmPersistenceService.findJvmByExactName(testJvmName)).thenReturn(jvm);
        String expectedValue = "<server>xml-content</server>";
        when(jvmPersistenceService.getJvmTemplate(anyString(), any(Identifier.class))).thenReturn(expectedValue);

        // happy case
        String serverXml = impl.generateConfigFile(testJvmName, "server.xml");
        assertEquals(expectedValue, serverXml);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGenerateSetenvBatConfig() {
        String testJvmName = "testjvm";
        final Jvm testJvm = new Jvm(new Identifier<Jvm>(99L), testJvmName, new HashSet<Group>());
        when(jvmPersistenceService.findJvmByExactName(testJvmName)).thenReturn(testJvm);
        String expectedValue = "<server>xml-content</server>";
        when(jvmPersistenceService.getJvmTemplate(anyString(), any(Identifier.class))).thenReturn(expectedValue);

        // happy case
        String serverXml = impl.generateConfigFile(testJvmName, "server.xml");
        assertEquals(expectedValue, serverXml);
    }

    @Test
    public void testPerformDiagnosis() throws IOException, URISyntaxException {
        Identifier<Jvm> aJvmId = new Identifier<>(11L);
        Jvm jvm = mock(Jvm.class);
        when(jvm.getId()).thenReturn(aJvmId);
        when(jvm.getStatusUri()).thenReturn(new URI("http://test.com"));
        when(jvmPersistenceService.getJvm(aJvmId)).thenReturn(jvm);

        ClientHttpResponse mockResponse = mock(ClientHttpResponse.class);
        when(mockResponse.getStatusCode()).thenReturn(HttpStatus.OK);
        when(mockClientFactoryHelper.requestGet(any(URI.class))).thenReturn(mockResponse);

        String diagnosis = impl.performDiagnosis(aJvmId);
        assertTrue(!diagnosis.isEmpty());

        when(mockResponse.getStatusCode()).thenReturn(HttpStatus.REQUEST_TIMEOUT);
        diagnosis = impl.performDiagnosis(aJvmId);
        assertTrue(!diagnosis.isEmpty());
    }

    @Test
    public void testPerformDiagnosisThrowsIOException() throws URISyntaxException, IOException {
        Identifier<Jvm> aJvmId = new Identifier<>(11L);
        Jvm jvm = mock(Jvm.class);
        when(jvm.getId()).thenReturn(aJvmId);
        when(jvm.getStatusUri()).thenReturn(new URI("http://test.com"));
        when(jvmPersistenceService.getJvm(aJvmId)).thenReturn(jvm);

        when(mockClientFactoryHelper.requestGet(any(URI.class))).thenThrow(new IOException("TEST IO EXCEPTION"));
        String diagnosis = impl.performDiagnosis(aJvmId);
        assertTrue(!diagnosis.isEmpty());
    }

    @Test
    public void testPerformDiagnosisThrowsRuntimeException() throws IOException, URISyntaxException {
        Identifier<Jvm> aJvmId = new Identifier<>(11L);
        Jvm jvm = mock(Jvm.class);
        when(jvm.getId()).thenReturn(aJvmId);
        when(jvm.getStatusUri()).thenReturn(new URI("http://test.com"));
        when(jvmPersistenceService.getJvm(aJvmId)).thenReturn(jvm);

        when(mockClientFactoryHelper.requestGet(any(URI.class))).thenThrow(new RuntimeException("RUN!!"));
        String diagnosis = impl.performDiagnosis(aJvmId);
        assertTrue(!diagnosis.isEmpty());
    }

    @Test
    public void testGetResourceTemplateNames() {
        String testJvmName = "testJvmName";
        ArrayList<String> value = new ArrayList<>();
        when(jvmPersistenceService.getResourceTemplateNames(testJvmName)).thenReturn(value);
        value.add("testJvm.tpl");
        List<String> result = impl.getResourceTemplateNames(testJvmName);
        assertTrue(result.size() == 1);
    }

    @Test
    public void testGetResourceTemplate() {
        String testJvmName = "testJvmName";
        String resourceTemplateName = "test-resource.tpl";
        Jvm jvm = mock(Jvm.class);
        String expectedValue = "<template>resource</template>";
        when(jvmPersistenceService.getResourceTemplate(testJvmName, resourceTemplateName)).thenReturn(expectedValue);
        List<Jvm> jvmList = new ArrayList<>();
        jvmList.add(jvm);
        when(jvmPersistenceService.findJvms(testJvmName)).thenReturn(jvmList);
        String result = impl.getResourceTemplate(testJvmName, resourceTemplateName, true);
        assertEquals(expectedValue, result);

        result = impl.getResourceTemplate(testJvmName, resourceTemplateName, false);
        assertEquals(expectedValue, result);
    }

    @Test
    public void testUpdateResourceTemplate() {
        String testJvmName = "testJvmName";
        String resourceTemplateName = "test-resource.tpl";
        String template = "<template>update</template>";
        when(jvmPersistenceService.updateResourceTemplate(testJvmName, resourceTemplateName, template)).thenReturn(template);
        String result = impl.updateResourceTemplate(testJvmName, resourceTemplateName, template);
        assertEquals(template, result);
    }

    @Test
    public void testGenerateInvokeBat() {
        final Jvm jvm = mock(Jvm.class);
        final List<Jvm> jvms = new ArrayList<>();
        jvms.add(jvm);
        when(jvmPersistenceService.findJvms(anyString())).thenReturn(jvms);
        when(jvmPersistenceService.getJvms()).thenReturn(jvms);
        when(fileManager.getResourceTypeTemplate(anyString())).thenReturn("template contents");
        final String result = impl.generateInvokeBat(anyString());
        assertEquals("template contents", result);
    }

    @Test
    public void testGetJpaJvm() {
        impl.getJpaJvm(new Identifier<Jvm>(1L), false);
        verify(jvmPersistenceService).getJpaJvm(new Identifier<Jvm>(1L), false);
    }

    @Test
    public void testGetJvmByName() {
        impl.getJvm("testJvm");
        verify(jvmPersistenceService).findJvmByExactName("testJvm");
    }

    @Test
    public void testPreviewTemplate() {
        final String jvmName = "jvm-1Test";
        Jvm testJvm = new Jvm(new Identifier<Jvm>(111L), jvmName, "testHost", new HashSet<Group>(), 9101, 9102, 9103, -1, 9104, new Path("./"), "", JvmState.JVM_STOPPED, "");
        List<Jvm> jvmList = new ArrayList<>();
        jvmList.add(testJvm);
        when(jvmPersistenceService.findJvmByExactName(anyString())).thenReturn(testJvm);
        when(jvmPersistenceService.getJvms()).thenReturn(jvmList);

        String preview = impl.previewResourceTemplate(jvmName, "groupTest", "TEST ${jvm.jvmName} TEST");
        assertEquals("TEST jvm-1Test TEST", preview);
    }

    @Test
    public void testUploadTemplateXML() throws FileNotFoundException {
        Jvm mockJvm = mock(Jvm.class);
        when(mockJvm.getId()).thenReturn(new Identifier<Jvm>(99L));
        UploadJvmTemplateRequest uploadRequest = new UploadJvmTemplateRequest(mockJvm, "ServerXMLTemplate.tpl", new FileInputStream(new File("./src/test/resources/ServerXMLTemplate.tpl"))) {
            @Override
            public String getConfFileName() {
                return "server.xml";
            }
        };
        impl.uploadJvmTemplateXml(uploadRequest, user);
        verify(jvmPersistenceService).uploadJvmTemplateXml(any(UploadJvmTemplateRequest.class));
    }

    @Test
    public void testUpdateState() {
        Identifier<Jvm> jvmId = new Identifier<Jvm>(999L);
        impl.updateState(jvmId, JvmState.JVM_STOPPED);
        verify(jvmPersistenceService).updateState(jvmId, JvmState.JVM_STOPPED);

        impl.updateState(jvmId, JvmState.JVM_STOPPED, "test error status");
        verify(jvmPersistenceService).updateState(jvmId, JvmState.JVM_STOPPED, "test error status");
    }

    @Test
    public void testAddTemplatesForJvm() {
        final Jvm jvm = mockJvmWithId(new Identifier<Jvm>(111L));
        Application mockApp = mock(Application.class);

        Set<Identifier<Group>> groups = new HashSet<>();
        final Identifier<Group> groupId = new Identifier<>(101L);
        groups.add(groupId);
        List<Application> mockAppList = new ArrayList<>();
        mockAppList.add(mockApp);

        when(applicationService.findApplications(any(Identifier.class))).thenReturn(mockAppList);

        impl.addAppTemplatesForJvm(jvm, groups);

        verify(applicationService).createAppConfigTemplateForJvm(jvm, mockApp, groupId);
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

        when(applicationService.getResourceTemplateNames(anyString())).thenReturn(templateNamesList);
        when(applicationService.findApplications(any(Identifier.class))).thenReturn(appList);
        when(jvmPersistenceService.findGroupsByJvm(any(Identifier.class))).thenReturn(groupsList);
        impl.deployApplicationContextXMLs(jvm);
        verify(applicationService).deployConf(anyString(), anyString(), anyString(), anyString(), anyBoolean(), any(User.class));
    }
}
;