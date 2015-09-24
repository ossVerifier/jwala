package com.siemens.cto.aem.ws.rest.v1.service.webserver.impl;

import com.siemens.cto.aem.common.AemConstants;
import com.siemens.cto.aem.common.exception.InternalErrorException;
import com.siemens.cto.aem.common.properties.ApplicationProperties;
import com.siemens.cto.aem.control.command.RuntimeCommandBuilder;
import com.siemens.cto.aem.domain.model.exec.ExecData;
import com.siemens.cto.aem.domain.model.exec.ExecReturnCode;
import com.siemens.cto.aem.domain.model.exec.RuntimeCommand;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.path.FileSystemPath;
import com.siemens.cto.aem.domain.model.path.Path;
import com.siemens.cto.aem.domain.model.state.CurrentState;
import com.siemens.cto.aem.domain.model.state.StateType;
import com.siemens.cto.aem.domain.model.temporary.User;
import com.siemens.cto.aem.domain.model.webserver.*;
import com.siemens.cto.aem.domain.model.webserver.command.ControlWebServerCommand;
import com.siemens.cto.aem.exception.CommandFailureException;
import com.siemens.cto.aem.persistence.jpa.service.exception.ResourceTemplateUpdateException;
import com.siemens.cto.aem.service.state.StateService;
import com.siemens.cto.aem.service.webserver.WebServerCommandService;
import com.siemens.cto.aem.service.webserver.WebServerControlService;
import com.siemens.cto.aem.service.webserver.impl.WebServerServiceImpl;
import com.siemens.cto.aem.ws.rest.v1.provider.AuthenticatedUser;
import com.siemens.cto.aem.ws.rest.v1.provider.WebServerIdsParameterProvider;
import com.siemens.cto.aem.ws.rest.v1.response.ApplicationResponse;
import org.apache.commons.io.FileUtils;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/**
 * @author horspe00
 */
@RunWith(MockitoJUnitRunner.class)
public class WebServerServiceRestImplTest {

    private static final String name = "webserverName";
    private static final String host = "localhost";
    private static final Path statusPath = new Path("/statusPath");
    private static final FileSystemPath httpConfigFile = new FileSystemPath("d:/some-dir/httpd.conf");
    private static final Path SVR_ROOT = new Path("./");
    private static final Path DOC_ROOT = new Path("htdocs");
    private static final List<WebServer> webServerList = createWebServerList();
    private static final WebServer webServer = webServerList.get(0);

    @Mock
    private WebServerServiceImpl impl;

    @Mock
    private WebServerControlService controlImpl;

    @Mock
    private WebServerCommandService commandImpl;

    @Mock
    private WebServerControlHistory webServerControlHistory;

    @Mock
    private StateService<WebServer, WebServerReachableState> webServerStateService;

    @Mock
    private AuthenticatedUser authenticatedUser;

    @Mock
    private RuntimeCommandBuilder rtCommandBuilder;

    @Mock
    private RuntimeCommand rtCommand;

    private WebServerServiceRestImpl cut;

    private static List<WebServer> createWebServerList() {
        final Group groupOne = new Group(Identifier.id(1L, Group.class), "ws-groupOne");
        final Group groupTwo = new Group(Identifier.id(2L, Group.class), "ws-groupTwo");

        final List<Group> groupsList = new ArrayList<>();
        groupsList.add(groupOne);
        groupsList.add(groupTwo);

        final WebServer ws = new WebServer(Identifier.id(1L, WebServer.class), groupsList, name, host, 8080, 8009,
                statusPath, httpConfigFile, SVR_ROOT, DOC_ROOT);
        final List<WebServer> result = new ArrayList<>();
        result.add(ws);
        return result;
    }

    @Before
    public void setUp() {
        cut = new WebServerServiceRestImpl(impl, controlImpl, commandImpl, webServerStateService);
        when(authenticatedUser.getUser()).thenReturn(new User("Unused"));
    }

    @Test
    public void testGetWebServerList() {
        when(impl.getWebServers()).thenReturn(webServerList);

        final Response response = cut.getWebServers(null);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        final ApplicationResponse applicationResponse = (ApplicationResponse) response.getEntity();
        final Object content = applicationResponse.getApplicationResponseContent();
        assertTrue(content instanceof List);

        final List<WebServer> receivedList = (List<WebServer>) content;
        final WebServer received = receivedList.get(0);
        assertEquals(webServer, received);
    }

    @Test
    public void testGetWebServer() {
        when(impl.getWebServer(any(Identifier.class))).thenReturn(webServer);

        final Response response = cut.getWebServer(Identifier.id(1l, WebServer.class));
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        final ApplicationResponse applicationResponse = (ApplicationResponse) response.getEntity();
        final Object content = applicationResponse.getApplicationResponseContent();
        assertTrue(content instanceof WebServer);

        final WebServer received = (WebServer) content;
        assertEquals(webServer, received);
    }

    @Test
    public void testCreateWebServer() {
        final JsonCreateWebServer jsonCreateWebServer = mock(JsonCreateWebServer.class);
        when(impl.createWebServer(any(CreateWebServerCommand.class), any(User.class))).thenReturn(webServer);

        final Response response = cut.createWebServer(jsonCreateWebServer, authenticatedUser);
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());

        final ApplicationResponse applicationResponse = (ApplicationResponse) response.getEntity();
        final Object content = applicationResponse.getApplicationResponseContent();
        assertTrue(content instanceof WebServer);

        final WebServer received = (WebServer) content;
        assertEquals(webServer, received);
    }

    @Test
    public void testUpdateWebServer() {
        final JsonUpdateWebServer jsonUpdateWebServer = mock(JsonUpdateWebServer.class);
        when(impl.updateWebServer(any(UpdateWebServerCommand.class), any(User.class))).thenReturn(webServer);

        final Response response = cut.updateWebServer(jsonUpdateWebServer, authenticatedUser);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        final ApplicationResponse applicationResponse = (ApplicationResponse) response.getEntity();

        final Object content = applicationResponse.getApplicationResponseContent();
        assertTrue(content instanceof WebServer);

        final WebServer received = (WebServer) content;
        assertEquals(webServer, received);
    }

    @Test
    public void testRemoveWebServer() {
        final Response response = cut.removeWebServer(Identifier.id(1l, WebServer.class));
        verify(impl, atLeastOnce()).removeWebServer(any(Identifier.class));
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        final ApplicationResponse applicationResponse = (ApplicationResponse) response.getEntity();
        assertNull(applicationResponse);
    }

    @Test
    public void testControlWebServer() {
        when(controlImpl.controlWebServer(any(ControlWebServerCommand.class), any(User.class))).thenReturn(webServerControlHistory);

        final ExecData execData = mock(ExecData.class);
        final ExecReturnCode execDataReturnCode = mock(ExecReturnCode.class);
        when(execDataReturnCode.wasSuccessful()).thenReturn(true);
        when(execData.getReturnCode()).thenReturn(execDataReturnCode);
        when(webServerControlHistory.getExecData()).thenReturn(execData);

        final JsonControlWebServer jsonControlWebServer = new JsonControlWebServer("start");
        final Response response = cut.controlWebServer(Identifier.id(1l, WebServer.class), jsonControlWebServer, authenticatedUser);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        final ApplicationResponse applicationResponse = (ApplicationResponse) response.getEntity();
        final Object content = applicationResponse.getApplicationResponseContent();
        assertTrue(content instanceof WebServerControlHistory);

        final WebServerControlHistory received = (WebServerControlHistory) content;
        assertEquals(webServerControlHistory, received);
    }

    @Test
    public void testGenerateHttpdConfig() {
        when(impl.generateHttpdConfig(anyString(), anyBoolean())).thenReturn("httpd configuration");
        Response response = cut.generateConfig("any-server-name", null);
        assertEquals("httpd configuration", response.getEntity());
    }

    @Test
    public void testGenerateWorkerProperties() {
        when(impl.generateWorkerProperties(anyString()))
                .thenReturn("worker properties");
        Response response = cut.generateLoadBalancerConfig("");
        assertEquals("worker properties", response.getEntity());
    }

    @Test
    public void testGetWebServersByGroup() {
        final List<WebServer> webServers = new ArrayList<>();
        webServers.add(new WebServer(null, new ArrayList<Group>(), "test", null, null, null, new Path("/statusPath"),
                new FileSystemPath("d:/some-dir/httpd.conf"), SVR_ROOT, DOC_ROOT));

        final Identifier<Group> groupId = new Identifier<>("1");

        when(impl.findWebServers(Matchers.eq(groupId))).thenReturn(webServers);
        final Response response = cut.getWebServers(groupId);

        final List<WebServer> result =
                (List<WebServer>) ((ApplicationResponse) response.getEntity()).getApplicationResponseContent();
        assertEquals("test", result.get(0).getName());
    }

    @Test
    public void testGenerateAndDeployConfig() throws CommandFailureException, IOException {
        System.setProperty(AemConstants.PROPERTIES_ROOT_PATH, "./src/test/resources");
        final String httpdConfDirPath = ApplicationProperties.get("paths.httpd.conf");
        assertTrue(new File(httpdConfDirPath).mkdirs());
        ExecData retSuccessExecData = new ExecData(new ExecReturnCode(0), "", "");
        when(rtCommand.execute()).thenReturn(retSuccessExecData);
        when(rtCommandBuilder.build()).thenReturn(rtCommand);
        when(commandImpl.secureCopyHttpdConf(anyString(), anyString(), any(RuntimeCommandBuilder.class))).thenReturn(retSuccessExecData);
        Response response = cut.generateAndDeployConfig(webServer.getName());
        assertTrue(response.hasEntity());
        FileUtils.deleteDirectory(new File(httpdConfDirPath));
        System.clearProperty(AemConstants.PROPERTIES_ROOT_PATH);
    }

    @Test
    public void testGenerateAndDeployConfigFailsSecureCopy() throws CommandFailureException {
        System.setProperty(AemConstants.PROPERTIES_ROOT_PATH, "./src/test/resources");
        ExecData retSuccessExecData = new ExecData(new ExecReturnCode(0), "", "");
        when(rtCommand.execute()).thenReturn(retSuccessExecData);
        when(rtCommandBuilder.build()).thenReturn(rtCommand);
        when(commandImpl.secureCopyHttpdConf(anyString(), anyString(), any(RuntimeCommandBuilder.class))).thenReturn(new ExecData(new ExecReturnCode(1), "", "FAILED SECURE COPY TEST"));
        boolean failedSecureCopy = false;
        try {
            Response response = cut.generateAndDeployConfig(webServer.getName());
        } catch (InternalErrorException e) {
            failedSecureCopy = true;
        }
        assertTrue(failedSecureCopy);
        System.clearProperty(AemConstants.PROPERTIES_ROOT_PATH);
    }

    @Test
    public void testGetCurrentWebServerStates() {
        CurrentState<WebServer, WebServerReachableState> webServerState = new CurrentState<>(webServer.getId(), WebServerReachableState.WS_REACHABLE, new DateTime(), StateType.WEB_SERVER);
        Set<CurrentState<WebServer, WebServerReachableState>> currentStates = new HashSet<>();
        currentStates.add(webServerState);
        when(webServerStateService.getCurrentStates(anySet())).thenReturn(currentStates);
        Set<String> wsIds = new HashSet<>();
        wsIds.add(webServer.getId().getId() + "");
        WebServerIdsParameterProvider wsIdParamProvider = new WebServerIdsParameterProvider(wsIds);
        Response response = cut.getCurrentWebServerStates(wsIdParamProvider);
        assertTrue(response.hasEntity());
    }

    @Test
    public void testGetCurrentWebServerStatesEmptyIdSet() {
        CurrentState<WebServer, WebServerReachableState> webServerState = new CurrentState<>(webServer.getId(), WebServerReachableState.WS_REACHABLE, new DateTime(), StateType.WEB_SERVER);
        Set<CurrentState<WebServer, WebServerReachableState>> currentStates = new HashSet<>();
        currentStates.add(webServerState);
        when(webServerStateService.getCurrentStates()).thenReturn(currentStates);
        Set<String> wsIds = new HashSet<>();
        WebServerIdsParameterProvider wsIdParamProvider = new WebServerIdsParameterProvider(wsIds);
        Response response = cut.getCurrentWebServerStates(wsIdParamProvider);
        assertTrue(response.hasEntity());
    }

    @Test
    public void testGetHttpdConfig() throws CommandFailureException {
        when(commandImpl.getHttpdConf(webServer.getId())).thenReturn(new ExecData(new ExecReturnCode(0), "", ""));
        Response response = cut.getHttpdConfig(webServer.getId());
        assertTrue(response.hasEntity());
    }

    @Test
    public void testGetHttpdConfigThrowsException() throws CommandFailureException {
        when(commandImpl.getHttpdConf(webServer.getId())).thenThrow(CommandFailureException.class);
        Response response = cut.getHttpdConfig(webServer.getId());
        assertTrue(response.hasEntity());
    }

    @Test
    public void testGetResourceName() {
        List<String> resourceNames = new ArrayList<>();
        resourceNames.add("httpd-test.tpl");
        when(impl.getResourceTemplateNames(webServer.getName())).thenReturn(resourceNames);
        Response response = cut.getResourceNames(webServer.getName());
        assertTrue(response.hasEntity());
    }

    @Test
    public void testGetResourceTemplates() {
        String resourceTemplateName = "httpd-conf.tpl";
        when(impl.getResourceTemplate(webServer.getName(), resourceTemplateName, false)).thenReturn("ServerRoot=./test");
        Response response = cut.getResourceTemplate(webServer.getName(), resourceTemplateName, false);
        assertTrue(response.hasEntity());
    }

    @Test
    public void testUpdateResourceTemplate() {
        String resourceTemplateName = "httpd-conf.tpl";
        String content = "ServerRoot=./test-update";
        when(impl.updateResourceTemplate(webServer.getName(), resourceTemplateName, content)).thenReturn(content);
        Response response = cut.updateResourceTemplate(webServer.getName(), resourceTemplateName, content);
        assertTrue(response.hasEntity());
    }

    @Test
    public void testUpdateResourceTemplateException() {
        String resourceTemplateName = "httpd-conf.tpl";
        String content = "ServerRoot=./test-update";
        when(impl.updateResourceTemplate(webServer.getName(), resourceTemplateName, content)).thenThrow(ResourceTemplateUpdateException.class);
        Response response = cut.updateResourceTemplate(webServer.getName(), resourceTemplateName, content);
        assertTrue(response.hasEntity());
    }
}
