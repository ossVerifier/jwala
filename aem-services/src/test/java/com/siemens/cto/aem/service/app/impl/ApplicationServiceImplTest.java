package com.siemens.cto.aem.service.app.impl;

import com.siemens.cto.aem.common.AemConstants;
import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.control.command.RuntimeCommandBuilder;
import com.siemens.cto.aem.control.configuration.AemSshConfig;
import com.siemens.cto.aem.domain.command.app.CreateApplicationCommand;
import com.siemens.cto.aem.domain.command.app.UpdateApplicationCommand;
import com.siemens.cto.aem.domain.command.app.UploadAppTemplateCommand;
import com.siemens.cto.aem.domain.command.app.UploadWebArchiveCommand;
import com.siemens.cto.aem.domain.model.app.*;
import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.aem.domain.command.exec.CommandOutput;
import com.siemens.cto.aem.domain.command.exec.ExecReturnCode;
import com.siemens.cto.aem.domain.command.exec.RuntimeCommand;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.ssh.SshConfiguration;
import com.siemens.cto.aem.domain.model.user.User;
import com.siemens.cto.aem.exception.CommandFailureException;
import com.siemens.cto.aem.persistence.dao.app.ApplicationDao;
import com.siemens.cto.aem.persistence.dao.jvm.JvmDao;
import com.siemens.cto.aem.persistence.service.app.ApplicationPersistenceService;
import com.siemens.cto.aem.persistence.service.jvm.JvmPersistenceService;
import com.siemens.cto.aem.service.app.ApplicationCommandService;
import com.siemens.cto.aem.service.app.PrivateApplicationService;
import com.siemens.cto.aem.service.group.GroupService;
import com.siemens.cto.aem.service.webserver.component.ClientFactoryHelper;
import com.siemens.cto.toc.files.FileManager;
import com.siemens.cto.toc.files.RepositoryFileInformation;
import com.siemens.cto.toc.files.WebArchiveManager;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationServiceImplTest {

    @Mock
    private ApplicationDao applicationDao;

    @Mock
    private ApplicationPersistenceService applicationPersistenceService;

    @Mock
    private WebArchiveManager webArchiveManager;

    @Mock
    private PrivateApplicationService privateApplicationService = new PrivateApplicationServiceImpl();

    @Mock
    private JvmDao jvmDao;

    @Mock
    private JvmPersistenceService jvmPersistenceService;

    @Mock
    private ApplicationCommandService applicationCommandService;

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
        System.setProperty(AemConstants.PROPERTIES_ROOT_PATH, new File(".").getAbsolutePath() + "/src/test/resources");
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
        when(applicationDao.getApplication(any(Identifier.class))).thenReturn(mockApplication);
        final Application application = applicationService.getApplication(new Identifier<Application>(1L));
        assertEquals(new Identifier<Application>(1L), application.getId());
        assertEquals(groupId, application.getGroup().getId());
        assertEquals("TOC 1.0", application.getName());
        assertEquals("the-ws-group-name", application.getGroup().getName());
        assertEquals("the-ws-group-name/toc-1.0.war", application.getWarPath());
    }

    @Test
    public void testAllGet() {
        when(applicationDao.getApplications()).thenReturn(applications2);
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
        when(applicationDao.findApplicationsBelongingTo(any(Identifier.class))).thenReturn(applications2);
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
        when(applicationPersistenceService.createApplication(any(Event.class), anyString(), anyString(), anyString())).thenReturn(mockApplication2);

        CreateApplicationCommand cac = new CreateApplicationCommand(Identifier.id(1L, Group.class), "", "", true, true);
        Application created = applicationService.createApplication(cac, new User("user"));

        assertTrue(created == mockApplication2);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCreate() {
        when(applicationPersistenceService.createApplication(any(Event.class), anyString(), anyString(), anyString())).thenReturn(mockApplication2);

        CreateApplicationCommand cac = new CreateApplicationCommand(Identifier.id(1L, Group.class), "wan", "/wan", true, true);
        Application created = applicationService.createApplication(cac, new User("user"));

        assertTrue(created == mockApplication2);
    }


    @SuppressWarnings("unchecked")
    @Test
    public void testUpdate() {
        when(applicationPersistenceService.updateApplication(any(Event.class))).thenReturn(mockApplication2);

        UpdateApplicationCommand cac = new UpdateApplicationCommand(mockApplication2.getId(), Identifier.id(1L, Group.class), "wan", "/wan", true, true);
        Application created = applicationService.updateApplication(cac, new User("user"));

        assertTrue(created == mockApplication2);
    }


    @SuppressWarnings("unchecked")
    @Test
    public void testRemove() {
        applicationService.removeApplication(mockApplication.getId(), testUser);

        verify(applicationPersistenceService, Mockito.times(1)).removeApplication(Mockito.any(Identifier.class));
    }

    private class IsValidUploadEvent extends ArgumentMatcher<Event<UploadWebArchiveCommand>> {

        @SuppressWarnings("unchecked")
        @Override
        public boolean matches(Object arg) {
            Event<UploadWebArchiveCommand> event = (Event<UploadWebArchiveCommand>) arg;
            UploadWebArchiveCommand uwac = event.getCommand();
            uwac.validateCommand();
            return true;
        }

    }

    @SuppressWarnings("unchecked")
    @Test
    public void testUploadWebArchive() throws IOException {
        UploadWebArchiveCommand uwac = new UploadWebArchiveCommand(mockApplication, "fn.war", 2L, uploadedFile);

        when(webArchiveManager.store(any(Event.class))).thenReturn(RepositoryFileInformation.stored(FileSystems.getDefault().getPath("D:\\fn.war"), 2L));

        applicationService.uploadWebArchive(uwac, testUser);

        verify(privateApplicationService, Mockito.times(1)).uploadWebArchiveData(argThat(new IsValidUploadEvent()));
        verify(privateApplicationService, Mockito.times(1)).uploadWebArchiveUpdateDB(argThat(new IsValidUploadEvent()), any(RepositoryFileInformation.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testDeleteWebArchive() throws IOException {
        when(webArchiveManager.remove(any(Event.class))).thenReturn(RepositoryFileInformation.deleted(FileSystems.getDefault().getPath("D:\\fn.war")));

        applicationService.deleteWebArchive(mockApplication.getId(), testUser);

        verify(webArchiveManager, Mockito.times(1)).remove(any(Event.class));
        verify(applicationPersistenceService, Mockito.times(1)).removeWARPath((any(Event.class)));
    }

    @Test(expected = BadRequestException.class)
    public void testDeleteWebArchiveWithIoException() throws IOException {
        when(webArchiveManager.remove(any(Event.class))).thenThrow(IOException.class);
        applicationService.deleteWebArchive(mockApplication.getId(), testUser);
    }

    @Test(expected = BadRequestException.class)
    public void testDeleteWebArchiveDeleteFailed() throws IOException {
        when(webArchiveManager.remove(any(Event.class))).thenReturn(RepositoryFileInformation.none());

        applicationService.deleteWebArchive(mockApplication.getId(), testUser);

        verify(webArchiveManager, Mockito.times(1)).remove(any(Event.class));
        verify(applicationPersistenceService, Mockito.times(1)).removeWARPath((any(Event.class)));
    }

    @Test
    public void testGetResourceTemplateNames() {
        final String[] nameArray = {"hct.xml"};
        when(applicationPersistenceService.getResourceTemplateNames(eq("hct"))).thenReturn(Arrays.asList(nameArray));
        final List names = applicationService.getResourceTemplateNames("hct");
        assertEquals("hct.xml", names.get(0));
    }

    @Test
    public void testGetResourceTemplate() {
        final String theTemplate = "<context>${webApp.warPath}</context>";
        when(applicationPersistenceService.getResourceTemplate(eq("hct"), eq("hct.xml"))).thenReturn(theTemplate);
        assertEquals(theTemplate, applicationService.getResourceTemplate("hct", null, null, "hct.xml", false));
    }

    @Test
    public void testGetResourceTemplateWithTokensReplaced() {
        final String theTemplate = "<context>${webApp.warPath}</context>";
        when(applicationPersistenceService.getResourceTemplate(eq("hct"), eq("hct.xml"))).thenReturn(theTemplate);
        final Application app = mock(Application.class);
        when(app.getWarPath()).thenReturn("theWarPath");
        when(applicationDao.findApplication(eq("hct"), anyString(), anyString())).thenReturn(app);
        when(jvmDao.findJvm(anyString(), anyString())).thenReturn(null);
        assertEquals("<context>theWarPath</context>", applicationService.getResourceTemplate("hct", null, null, "hct.xml", true));
    }

    @Test
    public void testUpdateResourceTemplate() {
        applicationService.updateResourceTemplate("hct", "hct.xml", "content");
        verify(applicationPersistenceService).updateResourceTemplate(eq("hct"), eq("hct.xml"), eq("content"));
    }

    @Test
    public void

    testDeployConf() throws CommandFailureException {
        final Jvm jvm = mock(Jvm.class);
        when(jvm.getHostName()).thenReturn("localhost");
        final List<Jvm> jvmList = new ArrayList();
        jvmList.add(jvm);
        when(jvmPersistenceService.findJvms(eq("jvm-1"))).thenReturn(jvmList);
        final CommandOutput execData = mock(CommandOutput.class);
        when(execData.getReturnCode()).thenReturn(new ExecReturnCode(0));
        when(applicationCommandService.secureCopyConfFile(anyString(),
                anyString(),
                anyString(),
                any(RuntimeCommandBuilder.class))).thenReturn(execData);


        when(applicationPersistenceService.getResourceTemplate(eq("hct"), eq("hct.xml"))).thenReturn("Test template");
        when(applicationDao.findApplication(eq("hct"), eq("hct-group"), eq("jvm-1"))).thenReturn(mockApplication);

        when(jvmDao.findJvm(eq("jvm-1"), eq("hct-group"))).thenReturn(jvm);

        final CommandOutput retExecData = applicationService.deployConf("hct", "hct-group", "jvm-1", "hct.xml", testUser);
        assertTrue(retExecData.getReturnCode().wasSuccessful());
    }

    @Test
    public void testDeployConfWithPropertiesExtension() throws CommandFailureException {
        final Jvm jvm = mock(Jvm.class);
        when(jvm.getHostName()).thenReturn("localhost");
        final List<Jvm> jvmList = new ArrayList();
        jvmList.add(jvm);
        when(jvmPersistenceService.findJvms(eq("jvm-1"))).thenReturn(jvmList);
        final CommandOutput execData = mock(CommandOutput.class);
        when(execData.getReturnCode()).thenReturn(new ExecReturnCode(0));
        when(applicationCommandService.secureCopyConfFile(anyString(),
                anyString(),
                anyString(),
                any(RuntimeCommandBuilder.class))).thenReturn(execData);


        when(applicationPersistenceService.getResourceTemplate(eq("hct"), eq("roleMapping.properties"))).thenReturn("Test template properties");
        when(applicationDao.findApplication(eq("hct"), eq("hct-group"), eq("jvm-1"))).thenReturn(mockApplication);

        when(jvmDao.findJvm(eq("jvm-1"), eq("hct-group"))).thenReturn(jvm);

        final CommandOutput retExecData = applicationService.deployConf("hct", "hct-group", "jvm-1", "roleMapping.properties", testUser);
        assertTrue(retExecData.getReturnCode().wasSuccessful());
    }

    @Test(expected = DeployApplicationConfException.class)
    public void testDeployConfExecDataWasNotSuccessful() throws CommandFailureException {
        final Jvm jvm = mock(Jvm.class);
        when(jvm.getHostName()).thenReturn("localhost");
        final List<Jvm> jvmList = new ArrayList();
        jvmList.add(jvm);
        when(jvmPersistenceService.findJvms(eq("jvm-1"))).thenReturn(jvmList);
        final CommandOutput execData = mock(CommandOutput.class);
        when(execData.getReturnCode()).thenReturn(new ExecReturnCode(ExecReturnCode.STP_EXIT_CODE_NO_OP));
        when(execData.getStandardError()).thenReturn("No operation!");
        when(applicationCommandService.secureCopyConfFile(anyString(),
                anyString(),
                anyString(),
                any(RuntimeCommandBuilder.class))).thenReturn(execData);
        when(applicationPersistenceService.getResourceTemplate(eq("hct"), eq("hct.xml"))).thenReturn("Test template");
        when(applicationDao.findApplication(eq("hct"), eq("hct-group"), eq("jvm-1"))).thenReturn(mockApplication);
        when(jvmDao.findJvm(eq("jvm-1"), eq("hct-group"))).thenReturn(jvm);
        final CommandOutput retExecData = applicationService.deployConf("hct", "hct-group", "jvm-1", "hct.xml", testUser);
    }

    @Test(expected = DeployApplicationConfException.class)
    public void testDeployConfExecDataCommandFailureException() throws CommandFailureException {
        final Jvm jvm = mock(Jvm.class);
        when(jvm.getHostName()).thenReturn("localhost");
        final List<Jvm> jvmList = new ArrayList();
        jvmList.add(jvm);
        when(jvmPersistenceService.findJvms(eq("jvm-1"))).thenReturn(jvmList);
        final CommandOutput execData = mock(CommandOutput.class);
        when(execData.getReturnCode()).thenReturn(new ExecReturnCode(ExecReturnCode.STP_EXIT_CODE_NO_OP));
        when(execData.getStandardError()).thenReturn("No operation!");
        when(applicationCommandService.secureCopyConfFile(anyString(),
                anyString(),
                anyString(),
                any(RuntimeCommandBuilder.class))).thenThrow(CommandFailureException.class);
        when(applicationPersistenceService.getResourceTemplate(eq("hct"), eq("hct.xml"))).thenReturn("Test template");
        when(applicationDao.findApplication(eq("hct"), eq("hct-group"), eq("jvm-1"))).thenReturn(mockApplication);
        when(jvmDao.findJvm(eq("jvm-1"), eq("hct-group"))).thenReturn(jvm);
        final CommandOutput retExecData = applicationService.deployConf("hct", "hct-group", "jvm-1", "hct.xml", testUser);
    }

    @Test(expected = DeployApplicationConfException.class)
    public void testDeployConfExecDataFileNotFoundException() throws CommandFailureException {
        final Jvm jvm = mock(Jvm.class);
        when(jvm.getHostName()).thenReturn("localhost");
        final List<Jvm> jvmList = new ArrayList();
        jvmList.add(jvm);
        when(jvmPersistenceService.findJvms(eq("jvm-1"))).thenReturn(jvmList);
        final CommandOutput execData = mock(CommandOutput.class);
        when(execData.getReturnCode()).thenReturn(new ExecReturnCode(ExecReturnCode.STP_EXIT_CODE_NO_OP));
        when(execData.getStandardError()).thenReturn("No operation!");
        when(applicationCommandService.secureCopyConfFile(anyString(),
                anyString(),
                anyString(),
                any(RuntimeCommandBuilder.class))).thenThrow(FileNotFoundException.class);
        when(applicationPersistenceService.getResourceTemplate(eq("hct"), eq("hct.xml"))).thenReturn("Test template");
        when(applicationDao.findApplication(eq("hct"), eq("hct-group"), eq("jvm-1"))).thenReturn(mockApplication);
        when(jvmDao.findJvm(eq("jvm-1"), eq("hct-group"))).thenReturn(jvm);
        final CommandOutput retExecData = applicationService.deployConf("hct", "hct-group", "jvm-1", "hct.xml", testUser);
    }

    @Test
    public void testPreviewResourceTemplate() {
        final Jvm jvm = mock(Jvm.class);
        when(applicationDao.findApplication(eq("hct"), eq("hct-group"), eq("jvm-1"))).thenReturn(mockApplication);
        when(jvmDao.findJvm(eq("jvm-1"), eq("hct-group"))).thenReturn(jvm);
        final String preview = applicationService.previewResourceTemplate("hct", "hct-group", "jvm-1", "Template contents");
        assertEquals("Template contents", preview);
    }

    @Test
    public void testUploadTemplate() {
        final UploadAppTemplateCommand cmd = mock(UploadAppTemplateCommand.class);
        applicationService.uploadAppTemplate(cmd, testUser);
        verify(cmd).validateCommand();
        verify(applicationPersistenceService).uploadAppTemplate(any(Event.class));
    }

    @Test
    public void testFindApplicationsByJvmId() {
        final Identifier<Jvm> id = new Identifier<Jvm>(1l);
        applicationService.findApplicationsByJvmId(id);
        verify(applicationDao).findApplicationsBelongingToJvm(eq(id));
    }

    @Test
    public void testCopyApplicationToGroupHosts() throws IOException {
        RuntimeCommandBuilder mockRuntimeCommandBuilder = mock(RuntimeCommandBuilder.class);
        RuntimeCommand mockCommand = mock(RuntimeCommand.class);
        final HashSet<Jvm> jvmSet = new HashSet<>();
        Jvm mockJvm = mock(Jvm.class);
        jvmSet.add(mockJvm);
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
        when(mockRuntimeCommandBuilder.build()).thenReturn(mockCommand);
        when(mockCommand.execute()).thenReturn(new CommandOutput(new ExecReturnCode(0), "", ""));

        ApplicationServiceImpl mockApplicationService = new ApplicationServiceImpl(applicationDao, applicationPersistenceService, jvmPersistenceService, mock(ClientFactoryHelper.class), applicationCommandService, jvmDao, aemSshConfig, mockGroupService, fileManager, webArchiveManager, privateApplicationService);
        mockApplicationService.copyApplicationWarToGroupHosts(mockApplication, mockRuntimeCommandBuilder);
        verify(mockCommand).execute();
        new File("./src/test/resources/webapps/test.war").delete();

        when(mockCommand.execute()).thenReturn(new CommandOutput(new ExecReturnCode(1), "", "Test copy failed"));
        boolean exceptionThrown = false;
        try {
            mockApplicationService.copyApplicationWarToGroupHosts(mockApplication, mockRuntimeCommandBuilder);
        } catch (Exception e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
        new File("./src/test/resources/webapps/test.war").delete();

        when(mockApplication.getWarPath()).thenReturn("./src/test/resources/archive/test_archive_FAIL_COPY.war");
        exceptionThrown = false;
        try {
            mockApplicationService.copyApplicationWarToGroupHosts(mockApplication, mockRuntimeCommandBuilder);
        } catch (Exception e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);

    }
}
