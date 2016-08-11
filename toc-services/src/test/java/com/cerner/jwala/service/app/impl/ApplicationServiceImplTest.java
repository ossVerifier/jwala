package com.cerner.jwala.service.app.impl;

import com.cerner.jwala.common.domain.model.app.Application;
import com.cerner.jwala.common.domain.model.app.ApplicationControlOperation;
import com.cerner.jwala.common.domain.model.fault.AemFaultType;
import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.domain.model.jvm.JvmState;
import com.cerner.jwala.common.domain.model.resource.ResourceGroup;
import com.cerner.jwala.common.domain.model.ssh.SshConfiguration;
import com.cerner.jwala.common.domain.model.user.User;
import com.cerner.jwala.common.exception.ApplicationException;
import com.cerner.jwala.common.exception.BadRequestException;
import com.cerner.jwala.common.exception.InternalErrorException;
import com.cerner.jwala.common.exec.CommandOutput;
import com.cerner.jwala.common.exec.ExecCommand;
import com.cerner.jwala.common.exec.ExecReturnCode;
import com.cerner.jwala.common.properties.ApplicationProperties;
import com.cerner.jwala.common.request.app.*;
import com.cerner.jwala.control.application.command.impl.WindowsApplicationPlatformCommandProvider;
import com.cerner.jwala.control.command.PlatformCommandProvider;
import com.cerner.jwala.control.command.RemoteCommandExecutor;
import com.cerner.jwala.control.configuration.AemSshConfig;
import com.cerner.jwala.exception.CommandFailureException;
import com.cerner.jwala.files.FileManager;
import com.cerner.jwala.files.RepositoryFileInformation;
import com.cerner.jwala.files.WebArchiveManager;
import com.cerner.jwala.persistence.jpa.domain.JpaJvm;
import com.cerner.jwala.persistence.service.ApplicationPersistenceService;
import com.cerner.jwala.persistence.service.JvmPersistenceService;
import com.cerner.jwala.persistence.service.ResourceDao;
import com.cerner.jwala.service.HistoryService;
import com.cerner.jwala.service.MessagingService;
import com.cerner.jwala.service.app.PrivateApplicationService;
import com.cerner.jwala.service.app.impl.ApplicationServiceImpl;
import com.cerner.jwala.service.app.impl.DeployApplicationConfException;
import com.cerner.jwala.service.app.impl.PrivateApplicationServiceImpl;
import com.cerner.jwala.service.group.GroupService;
import com.cerner.jwala.service.resource.ResourceService;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.FileSystems;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationServiceImplTest {

    static final String META_DATA_TEST_VALUES = "{\"deployPath\":\"./test/deploy-path/conf/stp/localhost\",\"contentType\":\"text/xml\",\"entity\":{\"type\":\"APPLICATION\",\"target\":\"soarcom-hct\",\"group\":\"soarcom-616\",\"parentName\":null,\"deployToJvms\":true},\"templateName\":\"hctXmlTemplate.tpl\",\"deployFileName\":\"hct.xml\"}";

    @Mock
    private ApplicationPersistenceService applicationPersistenceService;

    @Mock
    private WebArchiveManager webArchiveManager;

    @Mock
    private PrivateApplicationService privateApplicationService = new PrivateApplicationServiceImpl();

    @Mock
    private JvmPersistenceService jvmPersistenceService;

    @Mock
    private RemoteCommandExecutor<ApplicationControlOperation> remoteCommandExecutor;

    @Mock
    private AemSshConfig aemSshConfig;

    @Mock
    private GroupService groupService;

    @Mock
    private FileManager fileManager;
    @InjectMocks
    @Spy
    private ApplicationServiceImpl applicationService;

    @Mock
    private Application mockApplication;
    @Mock
    private Application mockApplication2;
    
    @Mock
    private HistoryService mockHistoryService;
    
    @Mock
    private MessagingService mockMessagingService;

    @Mock
    private ResourceDao mockResourceDao;

    @Mock
    private ResourceService mockResourceService;

    private Group group;
    private Group group2;
    private Identifier<Group> groupId;
    private Identifier<Group> groupId2;

    private ArrayList<Application> applications2 = new ArrayList<>(1);

    private User testUser = new User("testUser");

    // Managed by setup/teardown
    ByteArrayInputStream uploadedFile;
    Application app;

    @BeforeClass
    public static void init() {
        System.setProperty(ApplicationProperties.PROPERTIES_ROOT_PATH, new File(".").getAbsolutePath() + "/src/test/resources");
    }

    @Before
    public void setUp() {
        groupId = new Identifier<Group>(1L);
        groupId2 = new Identifier<Group>(2L);
        group = new Group(groupId, "the-ws-group-name");
        group2 = new Group(groupId2, "the-ws-group-name-2");

        when(mockApplication.getId()).thenReturn(new Identifier<Application>(1L));
        when(mockApplication.getWarPath()).thenReturn("the-ws-group-name/toc-1.0.war");
        when(mockApplication.getName()).thenReturn("TOC 1.0");
        when(mockApplication.getGroup()).thenReturn(group);
        when(mockApplication.getWebAppContext()).thenReturn("/aem");
        when(mockApplication.isSecure()).thenReturn(true);

        when(mockApplication2.getId()).thenReturn(new Identifier<Application>(2L));
        when(mockApplication2.getWarPath()).thenReturn("the-ws-group-name-2/toc-1.1.war");
        when(mockApplication2.getName()).thenReturn("TOC 1.1");
        when(mockApplication2.getGroup()).thenReturn(group2);
        when(mockApplication2.getWebAppContext()).thenReturn("/aem");
        when(mockApplication2.isSecure()).thenReturn(false);

        applications2.add(mockApplication);
        applications2.add(mockApplication2);

        ByteBuffer buf = java.nio.ByteBuffer.allocate(2); // 2 byte file
        buf.asShortBuffer().put((short) 0xc0de);

        uploadedFile = new ByteArrayInputStream(buf.array());

        SshConfiguration mockSshConfig = mock(SshConfiguration.class);
        aemSshConfig = mock(AemSshConfig.class);
        when(mockSshConfig.getUserName()).thenReturn("mockUser");
        when(aemSshConfig.getSshConfiguration()).thenReturn(mockSshConfig);

        groupService = mock(GroupService.class);
        when(groupService.getGroup(any(Identifier.class))).thenReturn(group);

        when(fileManager.getResourceTypeTemplate(eq("AppContextXMLTemplate.tpl"))).thenReturn("The application context template.");
        when(fileManager.getResourceTypeTemplate(eq("RoleMappingTemplate.tpl"))).thenReturn("The role mapping properties template.");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSingleGet() {
        when(applicationPersistenceService.getApplication(any(Identifier.class))).thenReturn(mockApplication);
        final Application application = applicationService.getApplication(new Identifier<Application>(1L));
        assertEquals(new Identifier<Application>(1L), application.getId());
        assertEquals(groupId, application.getGroup().getId());
        assertEquals("TOC 1.0", application.getName());
        assertEquals("the-ws-group-name", application.getGroup().getName());
        assertEquals("the-ws-group-name/toc-1.0.war", application.getWarPath());
    }

    @Test
    public void testGetApplicationByName() {
        when(applicationPersistenceService.getApplication(anyString())).thenReturn(mockApplication);
        final Application application = applicationService.getApplication("TOC 1.0");
        assertEquals(new Identifier<Application>(1L), application.getId());
        assertEquals(groupId, application.getGroup().getId());
        assertEquals("TOC 1.0", application.getName());
        assertEquals("the-ws-group-name", application.getGroup().getName());
        assertEquals("the-ws-group-name/toc-1.0.war", application.getWarPath());
    }

    @Test
    public void testAllGet() {
        when(applicationPersistenceService.getApplications()).thenReturn(applications2);
        final List<Application> apps = applicationService.getApplications();
        assertEquals(applications2.size(), apps.size());

        Application application = apps.get(0);
        assertEquals(new Identifier<Application>(1L), application.getId());
        assertEquals(groupId, application.getGroup().getId());
        assertEquals("TOC 1.0", application.getName());
        assertEquals("the-ws-group-name", application.getGroup().getName());
        assertEquals("the-ws-group-name/toc-1.0.war", application.getWarPath());

        application = apps.get(1);
        assertEquals(new Identifier<Application>(2L), application.getId());
        assertEquals(groupId2, application.getGroup().getId());
        assertEquals("TOC 1.1", application.getName());
        assertEquals("the-ws-group-name-2", application.getGroup().getName());
        assertEquals("the-ws-group-name-2/toc-1.1.war", application.getWarPath());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testFindByGroupId() {
        when(applicationPersistenceService.findApplicationsBelongingTo(any(Identifier.class))).thenReturn(applications2);
        final List<Application> apps = applicationService.findApplications(groupId);
        assertEquals(applications2.size(), apps.size());

        Application application = apps.get(1);

        assertEquals(new Identifier<Application>(2L), application.getId());
        assertEquals(groupId2, application.getGroup().getId());
        assertEquals("TOC 1.1", application.getName());
        assertEquals("the-ws-group-name-2", application.getGroup().getName());
        assertEquals("the-ws-group-name-2/toc-1.1.war", application.getWarPath());
    }

    @SuppressWarnings("unchecked")
    @Test(expected = BadRequestException.class)
    public void testCreateBadRequest() {
        when(applicationPersistenceService.createApplication(any(CreateApplicationRequest.class))).thenReturn(mockApplication2);

        CreateApplicationRequest cac = new CreateApplicationRequest(Identifier.id(1L, Group.class), "", "", true, true, false);
        Application created = applicationService.createApplication(cac, new User("user"));

        assertTrue(created == mockApplication2);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCreate() {
        when(applicationPersistenceService.createApplication(any(CreateApplicationRequest.class))).thenReturn(mockApplication2);

        CreateApplicationRequest cac = new CreateApplicationRequest(Identifier.id(1L, Group.class), "wan", "/wan", true, true, false);
        Application created = applicationService.createApplication(cac, new User("user"));

        assertTrue(created == mockApplication2);
    }


    @SuppressWarnings("unchecked")
    @Test
    public void testUpdate() {
        when(applicationPersistenceService.updateApplication(any(UpdateApplicationRequest.class))).thenReturn(mockApplication2);

        UpdateApplicationRequest cac = new UpdateApplicationRequest(mockApplication2.getId(), Identifier.id(1L, Group.class), "wan", "/wan", true, true, false);
        Application created = applicationService.updateApplication(cac, new User("user"));

        assertTrue(created == mockApplication2);
    }


    @SuppressWarnings("unchecked")
    @Test
    public void testRemove() {
        applicationService.removeApplication(mockApplication.getId(), testUser);

        verify(applicationPersistenceService, Mockito.times(1)).removeApplication(Mockito.any(Identifier.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testUploadWebArchive() throws IOException {
        UploadWebArchiveRequest uwac = new UploadWebArchiveRequest(mockApplication, "fn.war", 2L, uploadedFile);

        when(webArchiveManager.store(uwac)).thenReturn(RepositoryFileInformation.stored(FileSystems.getDefault().getPath("D:\\fn.war"), 2L));

        applicationService.uploadWebArchive(uwac, testUser);

        verify(privateApplicationService, Mockito.times(1)).uploadWebArchiveData(uwac);
        verify(privateApplicationService, Mockito.times(1)).uploadWebArchiveUpdateDB(any(UploadWebArchiveRequest.class), any(RepositoryFileInformation.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testDeleteWebArchive() throws IOException {
        when(mockApplication.getName()).thenReturn("hct");
        when(mockApplication.getWarName()).thenReturn("hct.war");
        when(applicationPersistenceService.getApplication(any(Identifier.class))).thenReturn(mockApplication);
        when(webArchiveManager.remove(any(RemoveWebArchiveRequest.class))).thenReturn(RepositoryFileInformation.deleted(FileSystems.getDefault().getPath("D:\\fn.war")));

        applicationService.deleteWebArchive(mockApplication.getId(), testUser);

        verify(webArchiveManager, Mockito.times(1)).remove(any(RemoveWebArchiveRequest.class));
        verify(mockResourceService).deleteGroupLevelAppResources(eq("hct"), anyString(), anyList());
    }

    @Test
    public void testDeleteWebArchiveWithIoException() throws IOException {
        when(applicationPersistenceService.getApplication(any(Identifier.class))).thenReturn(mockApplication);
        when(webArchiveManager.remove(any(RemoveWebArchiveRequest.class))).thenThrow(IOException.class);
        applicationService.deleteWebArchive(mockApplication.getId(), testUser);
        verify(webArchiveManager).remove(any(RemoveWebArchiveRequest.class));
    }

    @Test
    public void testDeleteWebArchiveDeleteFailedDueToArchiveNotFound() throws IOException {
        when(applicationPersistenceService.getApplication(any(Identifier.class))).thenReturn(mockApplication);
        final RepositoryFileInformation mockRepositoryFileInformation = mock(RepositoryFileInformation.class);
        when(mockRepositoryFileInformation.getType()).thenReturn(RepositoryFileInformation.Type.NONE);
        when(webArchiveManager.remove(any(RemoveWebArchiveRequest.class))).thenReturn(mockRepositoryFileInformation);
        applicationService.deleteWebArchive(mockApplication.getId(), testUser);
        verify(webArchiveManager).remove(any(RemoveWebArchiveRequest.class));
    }

    @Test
    public void testGetResourceTemplateNames() {
        final String[] nameArray = {"hct.xml"};
        when(applicationPersistenceService.getResourceTemplateNames(eq("hct"), anyString())).thenReturn(Arrays.asList(nameArray));
        final List names = applicationService.getResourceTemplateNames("hct", "any");
        assertEquals("hct.xml", names.get(0));
    }

    @Test
    public void testGetResourceTemplate() {
        final String theTemplate = "<context>${webApp.warPath}</context>";
        when(applicationPersistenceService.getResourceTemplate(eq("hct"), eq("hct.xml"), eq("jvm1"), eq("group1"))).thenReturn(theTemplate);
        assertEquals(theTemplate, applicationService.getResourceTemplate("hct", "group1", "jvm1", "hct.xml", new ResourceGroup(), false));
    }

    @Test
    public void testGetResourceTemplateWithTokensReplaced() {
        final String theTemplate = "<context>${webApp.warPath}</context>";
        when(applicationPersistenceService.getResourceTemplate(eq("hct"), eq("hct.xml"), eq("jvm1"), eq("group1"))).thenReturn(theTemplate);
        final Application app = mock(Application.class);
        when(app.getWarPath()).thenReturn("theWarPath");
        when(applicationPersistenceService.findApplication(eq("hct"), anyString(), anyString())).thenReturn(app);
        when(jvmPersistenceService.findJvm(anyString(), anyString())).thenReturn(null);
        assertEquals("<context>theWarPath</context>", applicationService.getResourceTemplate("hct", "group1", "jvm1", "hct.xml", new ResourceGroup(), true));

        when(applicationPersistenceService.getResourceTemplate(eq("hct"), eq("hct.xml"), eq("jvm1"), eq("group1"))).thenReturn(theTemplate);
        try {
            applicationService.getResourceTemplate("hct", "group1", "jvm1", "hct.xml", new ResourceGroup(), true);
        } catch (ApplicationException ae) {
            assertTrue(ae.getMessage().contains("replacement failed"));
        }
    }

    @Test
    public void testUpdateResourceTemplate() {
        applicationService.updateResourceTemplate("hct", "hct.xml", "content", "jvm1", "group1");
        verify(applicationPersistenceService).updateResourceTemplate(eq("hct"), eq("hct.xml"), eq("content"), eq("jvm1"), eq("group1"));
    }

    @Test
    public void testDeployConf() throws CommandFailureException {
        final Jvm jvm = mock(Jvm.class);
        when(jvm.getHostName()).thenReturn("localhost");
        when(jvm.getState()).thenReturn(JvmState.JVM_STOPPED);
        when(jvmPersistenceService.findJvmByExactName(eq("jvm-1"))).thenReturn(jvm);
        final CommandOutput execData = mock(CommandOutput.class);
        when(execData.getReturnCode()).thenReturn(new ExecReturnCode(0));
        when(remoteCommandExecutor.executeRemoteCommand(
                anyString(), anyString(), any(ApplicationControlOperation.class), any(WindowsApplicationPlatformCommandProvider.class), anyString(), anyString())).thenReturn(execData);
        when(remoteCommandExecutor.executeRemoteCommand(anyString(), anyString(), eq(ApplicationControlOperation.CHECK_FILE_EXISTS), any(WindowsApplicationPlatformCommandProvider.class), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(1), "", ""));

        when(applicationPersistenceService.getResourceTemplate(eq("hct"), eq("hct.xml"), eq("jvm-1"), eq("hct-group"))).thenReturn("Test template");
        when(applicationPersistenceService.findApplication(eq("hct"), eq("hct-group"), eq("jvm-1"))).thenReturn(mockApplication);
        when(applicationPersistenceService.getMetaData(anyString(), anyString(), anyString(), anyString())).thenReturn(META_DATA_TEST_VALUES);

        when(jvmPersistenceService.findJvm(eq("jvm-1"), eq("hct-group"))).thenReturn(jvm);

        CommandOutput retExecData = applicationService.deployConf("hct", "hct-group", "jvm-1", "hct.xml", mock(ResourceGroup.class), testUser);
        assertTrue(retExecData.getReturnCode().wasSuccessful());

        when(mockApplication.isSecure()).thenReturn(false);
        retExecData = applicationService.deployConf("hct", "hct-group", "jvm-1", "hct.xml", mock(ResourceGroup.class), testUser);
        assertTrue(retExecData.getReturnCode().wasSuccessful());

        when(mockApplication.isSecure()).thenReturn(true);
        retExecData = applicationService.deployConf("hct", "hct-group", "jvm-1", "hct.xml", mock(ResourceGroup.class), testUser);
        assertTrue(retExecData.getReturnCode().wasSuccessful());

        // test errors
        when(execData.getReturnCode()).thenReturn(new ExecReturnCode(1));
        when(execData.getStandardError()).thenReturn("REMOTE COMMAND FAILURE");
        when(remoteCommandExecutor.executeRemoteCommand(
                anyString(), anyString(), eq(ApplicationControlOperation.SECURE_COPY), any(WindowsApplicationPlatformCommandProvider.class), anyString(), anyString())).thenReturn(execData);
        when(remoteCommandExecutor.executeRemoteCommand(anyString(), anyString(), eq(ApplicationControlOperation.CHECK_FILE_EXISTS), any(WindowsApplicationPlatformCommandProvider.class), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(1), "", ""));
        try {
            applicationService.deployConf("hct", "hct-group", "jvm-1", "hct.xml", mock(ResourceGroup.class), testUser);
        } catch (DeployApplicationConfException ee) {
            assertEquals("REMOTE COMMAND FAILURE", ee.getMessage());
        }

        when(remoteCommandExecutor.executeRemoteCommand(
                anyString(), anyString(), eq(ApplicationControlOperation.SECURE_COPY), any(WindowsApplicationPlatformCommandProvider.class), anyString(), anyString())).thenThrow(new CommandFailureException(new ExecCommand("fail me"), new Throwable("should fail")));
        when(remoteCommandExecutor.executeRemoteCommand(anyString(), anyString(), eq(ApplicationControlOperation.CHECK_FILE_EXISTS), any(WindowsApplicationPlatformCommandProvider.class), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(1), "", ""));
        try {
            applicationService.deployConf("hct", "hct-group", "jvm-1", "hct.xml", mock(ResourceGroup.class), testUser);
        } catch (DeployApplicationConfException ee) {
            assertTrue(ee.getCause() instanceof CommandFailureException);
        }

    }

    @Test(expected = DeployApplicationConfException.class)
    public void testDeployConfExecDataWasNotSuccessful() throws CommandFailureException {
        final Jvm jvm = mock(Jvm.class);
        when(jvm.getHostName()).thenReturn("localhost");
        when(jvm.getState()).thenReturn(JvmState.JVM_STOPPED);
        when(jvmPersistenceService.findJvmByExactName(eq("jvm-1"))).thenReturn(jvm);
        final CommandOutput execData = mock(CommandOutput.class);
        when(execData.getReturnCode()).thenReturn(new ExecReturnCode(ExecReturnCode.STP_EXIT_CODE_NO_OP));
        when(execData.getStandardError()).thenReturn("No operation!");
        when(remoteCommandExecutor.executeRemoteCommand(
                anyString(), anyString(), any(ApplicationControlOperation.class), any(WindowsApplicationPlatformCommandProvider.class), anyString(), anyString())).thenReturn(execData);
        when(remoteCommandExecutor.executeRemoteCommand(anyString(), anyString(), eq(ApplicationControlOperation.CHECK_FILE_EXISTS), any(WindowsApplicationPlatformCommandProvider.class), anyString())).thenReturn(execData);
        when(applicationPersistenceService.getResourceTemplate(eq("hct"), eq("hct.xml"), eq("jvm-1"), eq("hct-group"))).thenReturn("Test template");
        when(applicationPersistenceService.findApplication(eq("hct"), eq("hct-group"), eq("jvm-1"))).thenReturn(mockApplication);
        when(applicationPersistenceService.getMetaData(anyString(), anyString(), anyString(), anyString())).thenReturn(META_DATA_TEST_VALUES);
        when(jvmPersistenceService.findJvm(eq("jvm-1"), eq("hct-group"))).thenReturn(jvm);
        final CommandOutput retExecData = applicationService.deployConf("hct", "hct-group", "jvm-1", "hct.xml", mock(ResourceGroup.class), testUser);
    }

    @Test(expected = DeployApplicationConfException.class)
    public void testDeployConfExecDataCommandFailureException() throws CommandFailureException {
        final Jvm jvm = mock(Jvm.class);
        when(jvm.getHostName()).thenReturn("localhost");
        when(jvm.getState()).thenReturn(JvmState.JVM_STOPPED);
        when(jvmPersistenceService.findJvmByExactName(eq("jvm-1"))).thenReturn(jvm);
        final CommandOutput execData = mock(CommandOutput.class);
        when(execData.getReturnCode()).thenReturn(new ExecReturnCode(ExecReturnCode.STP_EXIT_CODE_NO_OP));
        when(execData.getStandardError()).thenReturn("No operation!");
        when(remoteCommandExecutor.executeRemoteCommand(
                anyString(), anyString(), any(ApplicationControlOperation.class), any(WindowsApplicationPlatformCommandProvider.class), anyString(), anyString())).thenReturn(execData);
        when(remoteCommandExecutor.executeRemoteCommand(anyString(), anyString(), eq(ApplicationControlOperation.CHECK_FILE_EXISTS), any(WindowsApplicationPlatformCommandProvider.class), anyString())).thenReturn(execData);
        when(applicationPersistenceService.getResourceTemplate(eq("hct"), eq("hct.xml"), eq("jvm-1"), eq("hct-group"))).thenReturn("Test template");
        when(applicationPersistenceService.findApplication(eq("hct"), eq("hct-group"), eq("jvm-1"))).thenReturn(mockApplication);
        when(applicationPersistenceService.getMetaData(anyString(), anyString(), anyString(), anyString())).thenReturn(META_DATA_TEST_VALUES);
        when(jvmPersistenceService.findJvm(eq("jvm-1"), eq("hct-group"))).thenReturn(jvm);
        final CommandOutput retExecData = applicationService.deployConf("hct", "hct-group", "jvm-1", "hct.xml", mock(ResourceGroup.class), testUser);
    }

    @Test(expected = DeployApplicationConfException.class)
    public void testDeployConfExecDataFileNotFoundException() throws CommandFailureException {
        final Jvm jvm = mock(Jvm.class);
        when(jvm.getHostName()).thenReturn("localhost");
        when(jvm.getState()).thenReturn(JvmState.JVM_STOPPED);
        when(jvmPersistenceService.findJvmByExactName(eq("jvm-1"))).thenReturn(jvm);
        final CommandOutput execData = mock(CommandOutput.class);
        when(execData.getReturnCode()).thenReturn(new ExecReturnCode(ExecReturnCode.STP_EXIT_CODE_NO_OP));
        when(execData.getStandardError()).thenReturn("No operation!");
        when(remoteCommandExecutor.executeRemoteCommand(
                anyString(), anyString(), any(ApplicationControlOperation.class), any(WindowsApplicationPlatformCommandProvider.class), anyString(), anyString())).thenReturn(execData);
        when(remoteCommandExecutor.executeRemoteCommand(anyString(), anyString(), eq(ApplicationControlOperation.CHECK_FILE_EXISTS), any(WindowsApplicationPlatformCommandProvider.class), anyString())).thenReturn(execData);
        when(applicationPersistenceService.getResourceTemplate(eq("hct"), eq("hct.xml"), eq("jvm-1"), eq("hct-group"))).thenReturn("Test template");
        when(applicationPersistenceService.findApplication(eq("hct"), eq("hct-group"), eq("jvm-1"))).thenReturn(mockApplication);
        when(applicationPersistenceService.getMetaData(anyString(), anyString(), anyString(), anyString())).thenReturn(META_DATA_TEST_VALUES);
        when(jvmPersistenceService.findJvm(eq("jvm-1"), eq("hct-group"))).thenReturn(jvm);
        final CommandOutput retExecData = applicationService.deployConf("hct", "hct-group", "jvm-1", "hct.xml", mock(ResourceGroup.class), testUser);
    }

    @Test(expected = InternalErrorException.class)
    public void testDeployConfJvmNotStopped() {
        Jvm mockJvm = mock(Jvm.class);
        when(mockJvm.getState()).thenReturn(JvmState.JVM_STARTED);
        when(jvmPersistenceService.findJvmByExactName(anyString())).thenReturn(mockJvm);
        when(jvmPersistenceService.findJvm(anyString(), anyString())).thenReturn(mockJvm);
        when(applicationPersistenceService.findApplication(anyString(), anyString(), anyString())).thenReturn(mockApplication);
        when(applicationPersistenceService.getResourceTemplate(anyString(), anyString(), anyString(), anyString())).thenReturn("IGNORED CONTENT");
        applicationService.deployConf("testApp", "testGroup", "testJvm", "HttpSslConfTemplate.tpl", mock(ResourceGroup.class), testUser);
    }

    @Test
    public void testPreviewResourceTemplate() {
        final Jvm jvm = mock(Jvm.class);
        when(applicationPersistenceService.findApplication(eq("hct"), eq("hct-group"), eq("jvm-1"))).thenReturn(mockApplication);
        when(jvmPersistenceService.findJvm(eq("jvm-1"), eq("hct-group"))).thenReturn(jvm);
        final String preview = applicationService.previewResourceTemplate("hct", "hct-group", "jvm-1", "Template contents", new ResourceGroup());
        assertEquals("Template contents", preview);
    }

    @Test
    public void testUploadTemplate() {
        final UploadAppTemplateRequest cmd = mock(UploadAppTemplateRequest.class);
        when(cmd.getConfFileName()).thenReturn("roleMapping.properties");
        when(cmd.getJvmName()).thenReturn("testJvmName");
        Jvm mockJvm = mock(Jvm.class);
        when(mockJvm.getId()).thenReturn(new Identifier<Jvm>(111L));
        when(jvmPersistenceService.findJvmByExactName(anyString())).thenReturn(mockJvm);
        JpaJvm mockJpaJvm = mock(JpaJvm.class);
        when(jvmPersistenceService.getJpaJvm(any(Identifier.class), anyBoolean())).thenReturn(mockJpaJvm);
        applicationService.uploadAppTemplate(cmd);
        verify(cmd).validate();
        verify(applicationPersistenceService).uploadAppTemplate(any(UploadAppTemplateRequest.class), any(JpaJvm.class));

        List<Jvm> jvmList = new ArrayList<>();
        jvmList.add(mockJvm);
        when(mockJvm.getJvmName()).thenReturn("testJvmName");
        when(cmd.getConfFileName()).thenReturn("hct.xml");
        applicationService.uploadAppTemplate(cmd);
        verify(cmd, times(2)).validate();
        verify(applicationPersistenceService, times(2)).uploadAppTemplate(any(UploadAppTemplateRequest.class), any(JpaJvm.class));

        when(mockJvm.getJvmName()).thenReturn("notTestJvmName");
        applicationService.uploadAppTemplate(cmd);
        verify(cmd, times(3)).validate();
        verify(applicationPersistenceService, times(3)).uploadAppTemplate(any(UploadAppTemplateRequest.class), any(JpaJvm.class));

    }

    @Test
    public void testFindApplicationsByJvmId() {
        final Identifier<Jvm> id = new Identifier<Jvm>(1l);
        applicationService.findApplicationsByJvmId(id);
        verify(applicationPersistenceService).findApplicationsBelongingToJvm(eq(id));
    }


    @Test
    public void testCopyApplicationToGroupHosts() throws IOException {
        final HashSet<Jvm> jvmSet = new HashSet<>();
        Jvm mockJvm = mock(Jvm.class);
        Jvm mockJvm2 = mock(Jvm.class);
        jvmSet.add(mockJvm);
        jvmSet.add(mockJvm2);
        Group mockGroup = mock(Group.class);
        GroupService mockGroupService = mock(GroupService.class);
        final Identifier<Group> mockGroupId = new Identifier<>(999L);
        when(mockGroup.getId()).thenReturn(mockGroupId);
        when(mockGroup.getJvms()).thenReturn(jvmSet);
        when(mockApplication.getWarPath()).thenReturn("./src/test/resources/archive/test_archive.war");
        when(mockApplication.getWarName()).thenReturn("test.war");
        when(mockApplication.getGroup()).thenReturn(mockGroup);
        when(mockGroupService.getGroup(any(Identifier.class))).thenReturn(mockGroup);
        when(mockGroupService.getGroup(anyString())).thenReturn(mockGroup);
        when(mockJvm.getHostName()).thenReturn("localhost");
        when(mockJvm2.getHostName()).thenReturn("localhost");

        ApplicationServiceImpl mockApplicationService = new ApplicationServiceImpl(applicationPersistenceService,
                jvmPersistenceService, remoteCommandExecutor, mockGroupService, webArchiveManager, privateApplicationService,
                mockHistoryService, mockMessagingService, mockResourceService);

        try {
            CommandOutput successCommandOutput = new CommandOutput(new ExecReturnCode(0), "SUCCESS", "");
            when(remoteCommandExecutor.executeRemoteCommand(anyString(), anyString(), any(ApplicationControlOperation.class), any(PlatformCommandProvider.class), anyString(), anyString())).thenReturn(successCommandOutput);
            mockApplicationService.copyApplicationWarToGroupHosts(mockApplication);
        } catch (CommandFailureException e) {
            assertTrue("should not fail " + e.getMessage(), false);
        }
        new File("./src/test/resources/webapps/test.war").delete();

        try {
            CommandOutput failedCommandOutput = new CommandOutput(new ExecReturnCode(1), "FAILED", "");
            when(remoteCommandExecutor.executeRemoteCommand(anyString(), anyString(), any(ApplicationControlOperation.class), any(PlatformCommandProvider.class), anyString(), anyString())).thenReturn(failedCommandOutput);
            mockApplicationService.copyApplicationWarToGroupHosts(mockApplication);
        } catch (CommandFailureException e) {
            assertTrue("should not fail " + e.getMessage(), false);
        } catch (InternalErrorException ie) {
            assertEquals(AemFaultType.REMOTE_COMMAND_FAILURE, ie.getMessageResponseStatus());
        }
        new File("./src/test/resources/webapps/test.war").delete();

        boolean exceptionThrown = false;
        try {
            when(remoteCommandExecutor.executeRemoteCommand(anyString(), anyString(), any(ApplicationControlOperation.class), any(PlatformCommandProvider.class), anyString(), anyString())).thenThrow(new CommandFailureException(new ExecCommand("FAILED"), new Throwable("Expected to fail test")));
            mockApplicationService.copyApplicationWarToGroupHosts(mockApplication);
        } catch (Exception e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
        new File("./src/test/resources/webapps/test.war").delete();

        when(mockApplication.getWarPath()).thenReturn("./src/test/resources/archive/test_archive_FAIL_COPY.war");
        exceptionThrown = false;
        try {
            mockApplicationService.copyApplicationWarToGroupHosts(mockApplication);
        } catch (Exception e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);

    }

    @Test
    public void testCopyApplicationWarToHost() {
        when(mockApplication.getWarPath()).thenReturn("./src/test/resources/archive/test_archive.war");
        when(mockApplication.getWarName()).thenReturn("test.war");

        ApplicationServiceImpl mockApplicationService = new ApplicationServiceImpl(applicationPersistenceService,
                jvmPersistenceService, remoteCommandExecutor, null, webArchiveManager, privateApplicationService,
                mockHistoryService, mockMessagingService, mockResourceService);

        try {
            CommandOutput successCommandOutput = new CommandOutput(new ExecReturnCode(0), "SUCCESS", "");
            when(remoteCommandExecutor.executeRemoteCommand(anyString(), anyString(), any(ApplicationControlOperation.class), any(PlatformCommandProvider.class), anyString(), anyString())).thenReturn(successCommandOutput);
            mockApplicationService.copyApplicationWarToHost(mockApplication, "localhost");
        } catch (CommandFailureException e) {
            assertTrue("should not fail " + e.getMessage(), false);
        }
        new File("./src/test/resources/webapps/test.war").delete();

        try {
            CommandOutput failedCommandOutput = new CommandOutput(new ExecReturnCode(1), "FAILED", "");
            when(remoteCommandExecutor.executeRemoteCommand(anyString(), anyString(), any(ApplicationControlOperation.class), any(PlatformCommandProvider.class), anyString(), anyString())).thenReturn(failedCommandOutput);
            mockApplicationService.copyApplicationWarToHost(mockApplication, "localhost");
        } catch (CommandFailureException e) {
            assertTrue("should not fail " + e.getMessage(), false);
        } catch (InternalErrorException ie) {
            assertEquals(AemFaultType.REMOTE_COMMAND_FAILURE, ie.getMessageResponseStatus());
        }
        new File("./src/test/resources/webapps/test.war").delete();

        boolean exceptionThrown = false;
        try {
            when(remoteCommandExecutor.executeRemoteCommand(anyString(), anyString(), any(ApplicationControlOperation.class), any(PlatformCommandProvider.class), anyString(), anyString())).thenThrow(new CommandFailureException(new ExecCommand("FAILED"), new Throwable("Expected to fail test")));
            mockApplicationService.copyApplicationWarToHost(mockApplication, "localhost");
        } catch (Exception e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
        new File("./src/test/resources/webapps/test.war").delete();

        when(mockApplication.getWarPath()).thenReturn("./src/test/resources/archive/test_archive_FAIL_COPY.war");
        exceptionThrown = false;
        try {
            mockApplicationService.copyApplicationWarToGroupHosts(mockApplication);
        } catch (Exception e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
    }

    @Test
    public void testCopyToGroupJvms() throws CommandFailureException {
        GroupService mockGroupService = mock(GroupService.class);
        Group mockGroup = mock(Group.class);
        when(mockGroupService.getGroup(any(Identifier.class))).thenReturn(mockGroup);
        when(mockGroupService.getGroup(anyString())).thenReturn(mockGroup);
        Set<Group> groupSet = new HashSet<>();
        groupSet.add(mockGroup);

        Set<Jvm> jvms = new HashSet<>();
        final Jvm testjvm = mock(Jvm.class);
        when(testjvm.getId()).thenReturn(new Identifier<Jvm>(11111L));
        when(testjvm.getJvmName()).thenReturn("testjvm");
        when(testjvm.getState()).thenReturn(JvmState.JVM_STOPPED);
        jvms.add(testjvm);

        when(jvmPersistenceService.findJvmByExactName(anyString())).thenReturn(testjvm);
        when(mockGroup.getJvms()).thenReturn(jvms);
        when(mockGroup.getName()).thenReturn("testGroupName");
        List<String> templateNames = new ArrayList<>();
        templateNames.add("app.xml");
        when(applicationPersistenceService.getResourceTemplateNames(anyString(), anyString())).thenReturn(templateNames);
        when(applicationPersistenceService.findApplication(anyString(), anyString(), anyString())).thenReturn(new Application(new Identifier<Application>(111L), "appName", "./warPath", "/context", mockGroup, true, true, false, "app.war"));
        when(applicationPersistenceService.getResourceTemplate(anyString(), anyString(), anyString(), anyString())).thenReturn("template this!");
        when(remoteCommandExecutor.executeRemoteCommand(anyString(), anyString(), any(ApplicationControlOperation.class), any(PlatformCommandProvider.class), anyString(), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(0), "Success!", ""));

        ApplicationServiceImpl mockApplicationService = new ApplicationServiceImpl(applicationPersistenceService,
                jvmPersistenceService, remoteCommandExecutor, mockGroupService, webArchiveManager, privateApplicationService,
                mockHistoryService, mockMessagingService, mockResourceService);
        mockApplicationService.copyApplicationConfigToGroupJvms(mockGroup, "testApp", mock(ResourceGroup.class), testUser);
    }

}
