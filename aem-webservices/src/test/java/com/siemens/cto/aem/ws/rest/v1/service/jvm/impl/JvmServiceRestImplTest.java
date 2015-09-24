package com.siemens.cto.aem.ws.rest.v1.service.jvm.impl;

import com.siemens.cto.aem.common.AemConstants;
import com.siemens.cto.aem.common.exception.InternalErrorException;
import com.siemens.cto.aem.control.command.RuntimeCommandBuilder;
import com.siemens.cto.aem.domain.model.exec.ExecData;
import com.siemens.cto.aem.domain.model.exec.ExecReturnCode;
import com.siemens.cto.aem.domain.model.exec.RuntimeCommand;
import com.siemens.cto.aem.domain.model.group.LiteGroup;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.jvm.JvmControlHistory;
import com.siemens.cto.aem.domain.model.jvm.JvmControlOperation;
import com.siemens.cto.aem.domain.model.jvm.JvmState;
import com.siemens.cto.aem.domain.model.jvm.command.ControlJvmCommand;
import com.siemens.cto.aem.domain.model.jvm.command.CreateJvmAndAddToGroupsCommand;
import com.siemens.cto.aem.domain.model.jvm.command.CreateJvmCommand;
import com.siemens.cto.aem.domain.model.jvm.command.UpdateJvmCommand;
import com.siemens.cto.aem.domain.model.path.Path;
import com.siemens.cto.aem.domain.model.temporary.User;
import com.siemens.cto.aem.exception.CommandFailureException;
import com.siemens.cto.aem.service.jvm.JvmControlService;
import com.siemens.cto.aem.service.jvm.impl.JvmServiceImpl;
import com.siemens.cto.aem.service.resource.ResourceService;
import com.siemens.cto.aem.service.state.StateService;
import com.siemens.cto.aem.ws.rest.v1.provider.AuthenticatedUser;
import com.siemens.cto.aem.ws.rest.v1.provider.JvmIdsParameterProvider;
import com.siemens.cto.aem.ws.rest.v1.response.ApplicationResponse;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * @author meleje00
 */
@RunWith(MockitoJUnitRunner.class)
public class JvmServiceRestImplTest {

    private static final String name = "jvmName";
    private final static String hostName = "localhost";
    private static final List<Jvm> jvmList = createJvmList();
    private static final Jvm jvm = jvmList.get(0);

    // JVM ports
    private static final String httpPort = "80";
    private static final String httpsPort = "81";
    private static final String redirectPort = "82";
    private static final String shutdownPort = "83";
    private static final String ajpPort = "84";
    private static final Path statusPath = new Path("/statusPath");
    private static final String systemProperties = "EXAMPLE_OPTS=%someEnv%/someVal";

    @Mock
    private JvmServiceImpl impl;
    @Mock
    private JvmControlService controlImpl;
    @Mock
    private JvmControlHistory jvmControlHistory;
    @Mock
    private StateService<Jvm, JvmState> jvmStateService;
    @Mock
    private AuthenticatedUser authenticatedUser;
    @Mock
    private RuntimeCommandBuilder runtimeCmdBuilder;
    @Mock
    private RuntimeCommand runtimeCmd;
    @Mock
    private ResourceService resourceService;

    private Map<String, ReentrantReadWriteLock> writeLockMap;

    private JvmServiceRestImpl cut;

    private static List<Jvm> createJvmList() {
        final Set<LiteGroup> groups = new HashSet<>();
        final Jvm ws = new Jvm(Identifier.id(1L, Jvm.class),
                name,
                hostName,
                groups,
                Integer.valueOf(httpPort),
                Integer.valueOf(httpsPort),
                Integer.valueOf(redirectPort),
                Integer.valueOf(shutdownPort),
                Integer.valueOf(ajpPort),
                statusPath,
                systemProperties);
        final List<Jvm> result = new ArrayList<>();
        result.add(ws);
        return result;
    }

    @Before
    public void setUp() {
        System.setProperty(AemConstants.PROPERTIES_ROOT_PATH, "./src/test/resources");
        writeLockMap = new HashMap<>();
        cut = new JvmServiceRestImpl(impl, controlImpl, jvmStateService, resourceService, runtimeCmdBuilder, writeLockMap);
        when(authenticatedUser.getUser()).thenReturn(new User("Unused"));
    }

    @After
    public void cleanUp() {
        System.clearProperty(AemConstants.PROPERTIES_ROOT_PATH);
    }

    @Test
    public void testGetJvmList() {
        when(impl.getJvms()).thenReturn(jvmList);

        final Response response = cut.getJvms();
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        final ApplicationResponse applicationResponse = (ApplicationResponse) response.getEntity();
        final Object content = applicationResponse.getApplicationResponseContent();
        assertTrue(content instanceof List);

        final List<Jvm> receivedList = (List<Jvm>) content;
        final Jvm received = receivedList.get(0);
        assertEquals(jvm, received);
    }

    @Test
    public void testGetJvm() {
        when(impl.getJvm(any(Identifier.class))).thenReturn(jvm);

        final Response response = cut.getJvm(Identifier.id(1l, Jvm.class));
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        final ApplicationResponse applicationResponse = (ApplicationResponse) response.getEntity();
        final Object content = applicationResponse.getApplicationResponseContent();
        assertTrue(content instanceof Jvm);

        final Jvm received = (Jvm) content;
        assertEquals(jvm, received);
    }

    @Test
    public void testCreateJvm() {
        when(impl.createJvm(any(CreateJvmCommand.class), any(User.class))).thenReturn(jvm);

        final JsonCreateJvm jsonCreateJvm = new JsonCreateJvm(name, hostName, httpPort, httpsPort, redirectPort,
                shutdownPort, ajpPort, statusPath.getUriPath(), systemProperties);
        final Response response = cut.createJvm(jsonCreateJvm, authenticatedUser);
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());

        final ApplicationResponse applicationResponse = (ApplicationResponse) response.getEntity();
        final Object content = applicationResponse.getApplicationResponseContent();
        assertTrue(content instanceof Jvm);

        final Jvm received = (Jvm) content;
        assertEquals(jvm, received);
    }

    @Test
    public void testCreateJvmWithGroups() {
        when(impl.createAndAssignJvm(any(CreateJvmAndAddToGroupsCommand.class), any(User.class))).thenReturn(jvm);

        final Set<String> groupIds = new HashSet<>();
        groupIds.add("1");
        final JsonCreateJvm jsonCreateJvm = new JsonCreateJvm(name,
                hostName,
                groupIds,
                httpPort,
                httpsPort,
                redirectPort,
                shutdownPort,
                ajpPort,
                statusPath.getUriPath(),
                systemProperties);
        final Response response = cut.createJvm(jsonCreateJvm, authenticatedUser);
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());

        final ApplicationResponse applicationResponse = (ApplicationResponse) response.getEntity();
        final Object content = applicationResponse.getApplicationResponseContent();
        assertTrue(content instanceof Jvm);

        final Jvm received = (Jvm) content;
        assertEquals(jvm, received);
    }

    @Test
    public void testUpdateJvm() {
        final Set<String> groupIds = new HashSet<>();
        final JsonUpdateJvm jsonUpdateJvm = new JsonUpdateJvm("1", name, hostName, groupIds, "5", "4", "3", "2", "1",
                statusPath.getUriPath(), systemProperties);
        when(impl.updateJvm(any(UpdateJvmCommand.class), any(User.class))).thenReturn(jvm);

        // Check rules for the JVM
        UpdateJvmCommand updateJvmCommand = jsonUpdateJvm.toUpdateJvmCommand();
        updateJvmCommand.validateCommand();
        updateJvmCommand.hashCode();
        updateJvmCommand.equals(jsonUpdateJvm.toUpdateJvmCommand());
        String check = updateJvmCommand.toString();

        final Response response = cut.updateJvm(jsonUpdateJvm, authenticatedUser);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        final ApplicationResponse applicationResponse = (ApplicationResponse) response.getEntity();

        final Object content = applicationResponse.getApplicationResponseContent();
        assertTrue(content instanceof Jvm);

        final Jvm received = (Jvm) content;
        assertEquals(jvm, received);
    }

    @Test
    public void testRemoveJvm() {
        when(impl.isJvmStarted(any(Jvm.class))).thenReturn(false);
        when(impl.getJvm(jvm.getId())).thenReturn(jvm);
        when(jvmControlHistory.getExecData()).thenReturn(new ExecData(new ExecReturnCode(0), "",""));
        when(controlImpl.controlJvm(any(ControlJvmCommand.class), any(User.class))).thenReturn(jvmControlHistory);
        final Response response = cut.removeJvm(jvm.getId(), authenticatedUser);
        verify(impl, atLeastOnce()).removeJvm(jvm.getId());
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        final ApplicationResponse applicationResponse = (ApplicationResponse) response.getEntity();
        assertNull(applicationResponse);
    }

    @Test
    public void testControlJvm() {
        when(jvmControlHistory.getExecData()).thenReturn(new ExecData(new ExecReturnCode(0), "", ""));
        when(controlImpl.controlJvm(any(ControlJvmCommand.class), any(User.class))).thenReturn(jvmControlHistory);

        final ExecData execData = mock(ExecData.class);
        final ExecReturnCode execDataReturnCode = mock(ExecReturnCode.class);
        when(execDataReturnCode.wasSuccessful()).thenReturn(true);
        when(execData.getReturnCode()).thenReturn(execDataReturnCode);
        when(jvmControlHistory.getExecData()).thenReturn(execData);

        final JsonControlJvm jsonControlJvm = new JsonControlJvm("start");
        final Response response = cut.controlJvm(Identifier.id(1l, Jvm.class), jsonControlJvm, authenticatedUser);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        final ApplicationResponse applicationResponse = (ApplicationResponse) response.getEntity();
        final Object content = applicationResponse.getApplicationResponseContent();
        assertTrue(content instanceof JvmControlHistory);

        final JvmControlHistory received = (JvmControlHistory) content;
        assertEquals(jvmControlHistory, received);
    }

    @Test
    public void testGetCurrentJvmStates() {
        Set<String> jvmIds = new HashSet<>();
        jvmIds.add(jvm.getId().getId() + "");
        JvmIdsParameterProvider jvmIdProvider = new JvmIdsParameterProvider(jvmIds);
        assertTrue(cut.getCurrentJvmStates(jvmIdProvider).hasEntity());
    }

    @Test
    public void testGenerateConfig() {
        when(impl.generateConfigFile(jvm.getJvmName(), "server.xml")).thenReturn("<server>xml-content</server>");
        Response responseObj = cut.generateConfig(jvm.getJvmName());
        assertTrue(responseObj.hasEntity());
    }

    @Test
    public void testGenerateAndDeployConfig() throws CommandFailureException, IOException {
        System.setProperty(AemConstants.PROPERTIES_ROOT_PATH, "./src/test/resources");
        when(impl.getJvm(jvm.getJvmName())).thenReturn(jvm);
        when(impl.generateConfigFile(jvm.getJvmName(), "server.xml")).thenReturn("<server>xml-content</server>");
        when(impl.generateConfigFile(jvm.getJvmName(), "context.xml")).thenReturn("<content>xml-content</content>");
        when(impl.generateConfigFile(jvm.getJvmName(), "setenv.bat")).thenReturn("SET TEST=xxtestxx");
        when(impl.secureCopyFile(runtimeCmdBuilder, jvm.getJvmName() + "_config.tar", ".", jvm.getHostName(), ".")).thenReturn(new ExecData(new ExecReturnCode(0), "", ""));
        when(runtimeCmd.execute()).thenReturn(new ExecData(new ExecReturnCode(0), "", ""));
        when(runtimeCmdBuilder.build()).thenReturn(runtimeCmd);
        when(jvmControlHistory.getExecData()).thenReturn(new ExecData(new ExecReturnCode(0), "", ""));
        when(controlImpl.controlJvm(any(ControlJvmCommand.class), any(User.class))).thenReturn(jvmControlHistory);

        boolean failsScp = false;
        Response response = null;
        try {
            response = cut.generateAndDeployConf(jvm.getJvmName(), authenticatedUser);
        } catch (InternalErrorException e) {
            failsScp = true;
        }
        assertFalse(failsScp);
        assertTrue(response != null && response.hasEntity());
	FileUtils.deleteDirectory(new File("./" + jvm.getJvmName()));
    }

    @Test
    public void testGenerateAndDeployConfigFailControlService() throws CommandFailureException {
        System.setProperty(AemConstants.PROPERTIES_ROOT_PATH, "./src/test/resources");
        when(impl.getJvm(jvm.getJvmName())).thenReturn(jvm);
        when(impl.generateConfigFile(jvm.getJvmName(), "server.xml")).thenReturn("<server>xml-content</server>");
        when(impl.generateConfigFile(jvm.getJvmName(), "context.xml")).thenReturn("<content>xml-content</content>");
        when(impl.generateConfigFile(jvm.getJvmName(), "setenv.bat")).thenReturn("SET TEST=xxtestxx");
        when(impl.secureCopyFile(runtimeCmdBuilder, jvm.getJvmName() + "_config.tar", ".", jvm.getHostName(), ".")).thenReturn(new ExecData(new ExecReturnCode(0), "", ""));
        when(runtimeCmd.execute()).thenReturn(new ExecData(new ExecReturnCode(0), "", ""));
        when(runtimeCmdBuilder.build()).thenReturn(runtimeCmd);
        when(jvmControlHistory.getExecData()).thenReturn(new ExecData(new ExecReturnCode(1), "", "FAIL CONTROL SERVICE"));
        when(controlImpl.controlJvm(any(ControlJvmCommand.class), any(User.class))).thenReturn(jvmControlHistory);
        final Response response = cut.generateAndDeployConf(jvm.getJvmName(), authenticatedUser);
        assertEquals("FAIL CONTROL SERVICE", ((ApplicationResponse) response.getEntity()).getApplicationResponseContent());
    }

    @Test
    public void testGenerateAndDeployConfigFailSecureCopyService() throws CommandFailureException {
        System.setProperty(AemConstants.PROPERTIES_ROOT_PATH, "./src/test/resources");
        when(impl.getJvm(jvm.getJvmName())).thenReturn(jvm);
        when(impl.generateConfigFile(jvm.getJvmName(), "server.xml")).thenReturn("<server>xml-content</server>");
        when(impl.generateConfigFile(jvm.getJvmName(), "context.xml")).thenReturn("<content>xml-content</content>");
        when(impl.generateConfigFile(jvm.getJvmName(), "setenv.bat")).thenReturn("SET TEST=xxtestxx");
        when(impl.secureCopyFile(runtimeCmdBuilder, jvm.getJvmName() + "_config.tar", ".", jvm.getHostName(), ".")).thenReturn(new ExecData(new ExecReturnCode(1), "", "FAIL THE SERVICE SECURE COPY TEST"));
        when(runtimeCmd.execute()).thenReturn(new ExecData(new ExecReturnCode(0), "", ""));
        when(runtimeCmdBuilder.build()).thenReturn(runtimeCmd);
        when(jvmControlHistory.getExecData()).thenReturn(new ExecData(new ExecReturnCode(0), "", ""));
        when(controlImpl.controlJvm(any(ControlJvmCommand.class), any(User.class))).thenReturn(jvmControlHistory);
        final Response response = cut.generateAndDeployConf(jvm.getJvmName(), authenticatedUser);
        assertEquals("FAIL THE SERVICE SECURE COPY TEST", ((ApplicationResponse) response.getEntity()).getApplicationResponseContent());
    }

    @Test
    public void testGenerateAndDeployConfigFailsRuntimeCommand() throws CommandFailureException {
        System.setProperty(AemConstants.PROPERTIES_ROOT_PATH, "./src/test/resources");
        when(impl.getJvm(jvm.getJvmName())).thenReturn(jvm);
        when(impl.generateConfigFile(jvm.getJvmName(), "server.xml")).thenReturn("<server>xml-content</server>");
        when(impl.generateConfigFile(jvm.getJvmName(), "context.xml")).thenReturn("<content>xml-content</content>");
        when(impl.generateConfigFile(jvm.getJvmName(), "setenv.bat")).thenReturn("SET TEST=xxtestxx");
        when(impl.secureCopyFile(runtimeCmdBuilder, jvm.getJvmName() + "_config.tar", ".", jvm.getHostName(), ".")).thenReturn(new ExecData(new ExecReturnCode(0), "", ""));
        when(controlImpl.controlJvm(new ControlJvmCommand(jvm.getId(), JvmControlOperation.DELETE_SERVICE), authenticatedUser.getUser())).thenReturn(new JvmControlHistory(null, null, null, null, new ExecData(new ExecReturnCode(0), "", "")));
        when(runtimeCmd.execute()).thenReturn(new ExecData(new ExecReturnCode(1), "", "FAIL THE RUNTIME COMMAND TEST"));
        when(runtimeCmdBuilder.build()).thenReturn(runtimeCmd);
        final Response response = cut.generateAndDeployConf(jvm.getJvmName(), authenticatedUser);
        assertEquals("FAIL THE RUNTIME COMMAND TEST", ((ApplicationResponse) response.getEntity()).getApplicationResponseContent());
    }

    @Test
    public void testDiagnoseJvm() {
        when(impl.performDiagnosis(jvm.getId())).thenReturn("Good Diagnosis!");
        Response response = cut.diagnoseJvm(jvm.getId());
        assertTrue(response.hasEntity());
    }

    @Test
    public void testGetResourceNames() {
        List<String> resourceNames = new ArrayList<>();
        resourceNames.add("a resource name");
        when(impl.getResourceTemplateNames(jvm.getJvmName())).thenReturn(resourceNames);
        Response response = cut.getResourceNames(jvm.getJvmName());
        assertTrue(response.hasEntity());
    }

    @Test
    public void testGetResourceTemplate() {
        String resourceTemplateName = "template.tpl";
        when(impl.getResourceTemplate(jvm.getJvmName(), resourceTemplateName, false)).thenReturn("${jvm.jvmName");
        Response response = cut.getResourceTemplate(jvm.getJvmName(), resourceTemplateName, false);
        assertTrue(response.hasEntity());
    }
}
