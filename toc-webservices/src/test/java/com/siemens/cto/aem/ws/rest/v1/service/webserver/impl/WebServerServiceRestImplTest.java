package com.siemens.cto.aem.ws.rest.v1.service.webserver.impl;

import com.siemens.cto.aem.common.domain.model.app.Application;
import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.path.FileSystemPath;
import com.siemens.cto.aem.common.domain.model.path.Path;
import com.siemens.cto.aem.common.domain.model.resource.ResourceType;
import com.siemens.cto.aem.common.domain.model.user.User;
import com.siemens.cto.aem.common.domain.model.webserver.WebServer;
import com.siemens.cto.aem.common.domain.model.webserver.WebServerReachableState;
import com.siemens.cto.aem.common.exception.InternalErrorException;
import com.siemens.cto.aem.common.exec.CommandOutput;
import com.siemens.cto.aem.common.exec.ExecCommand;
import com.siemens.cto.aem.common.exec.ExecReturnCode;
import com.siemens.cto.aem.common.exec.RuntimeCommand;
import com.siemens.cto.aem.common.properties.ApplicationProperties;
import com.siemens.cto.aem.common.request.webserver.ControlWebServerRequest;
import com.siemens.cto.aem.common.request.webserver.CreateWebServerRequest;
import com.siemens.cto.aem.common.request.webserver.UpdateWebServerRequest;
import com.siemens.cto.aem.common.request.webserver.UploadWebServerTemplateRequest;
import com.siemens.cto.aem.control.command.RuntimeCommandBuilder;
import com.siemens.cto.aem.exception.CommandFailureException;
import com.siemens.cto.aem.persistence.jpa.service.exception.ResourceTemplateUpdateException;
import com.siemens.cto.aem.service.resource.ResourceService;
import com.siemens.cto.aem.service.state.StateService;
import com.siemens.cto.aem.service.webserver.WebServerCommandService;
import com.siemens.cto.aem.service.webserver.WebServerControlService;
import com.siemens.cto.aem.service.webserver.impl.WebServerServiceImpl;
import com.siemens.cto.aem.ws.rest.v1.provider.AuthenticatedUser;
import com.siemens.cto.aem.ws.rest.v1.response.ApplicationResponse;
import org.apache.commons.fileupload.FileUploadBase;
import org.apache.commons.io.FileUtils;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

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
    private WebServerControlService webServerControlService;

    @Mock
    private WebServerCommandService commandImpl;

    @Mock
    private StateService<WebServer, WebServerReachableState> webServerStateService;

    @Mock
    private ResourceService resourceService;

    @Mock
    private AuthenticatedUser authenticatedUser;

    @Mock
    private RuntimeCommandBuilder rtCommandBuilder;

    @Mock
    private RuntimeCommand rtCommand;

    private WebServerServiceRestImpl webServerServiceRest;
    private Map<String, ReentrantReadWriteLock> writeLockMap = new HashMap<>();

    private static List<WebServer> createWebServerList() {
        final Group groupOne = new Group(Identifier.id(1L, Group.class), "ws-groupOne");
        final Group groupTwo = new Group(Identifier.id(2L, Group.class), "ws-groupTwo");

        final List<Group> groupsList = new ArrayList<>();
        groupsList.add(groupOne);
        groupsList.add(groupTwo);

        final WebServer ws = new WebServer(Identifier.id(1L, WebServer.class), groupsList, name, host, 8080, 8009, statusPath,
                httpConfigFile, SVR_ROOT, DOC_ROOT, WebServerReachableState.WS_UNREACHABLE, null);
        final List<WebServer> result = new ArrayList<>();
        result.add(ws);
        return result;
    }

    @Before
    public void setUp() {
        webServerServiceRest = new WebServerServiceRestImpl(impl, webServerControlService, commandImpl, writeLockMap, resourceService);
        when(authenticatedUser.getUser()).thenReturn(new User("Unused"));
        final ArrayList<ResourceType> resourceTypes = new ArrayList<>();
        ResourceType mockWsResourceType = mock(ResourceType.class);
        when(mockWsResourceType.getConfigFileName()).thenReturn("httpd.conf");
        when(mockWsResourceType.getEntityType()).thenReturn("webServer");
        when(mockWsResourceType.getTemplateName()).thenReturn("HttpdSslConfTemplate.tpl");
        when(resourceService.getResourceTypes()).thenReturn(resourceTypes);
        try {
            webServerServiceRest.afterPropertiesSet();
        } catch (Exception e) {
            assertTrue("This should not fail, but ... " + e.getMessage(), false);
        }
    }

    @Test
    public void testGetWebServerList() {
        when(impl.getWebServers()).thenReturn(webServerList);

        final Response response = webServerServiceRest.getWebServers(null);
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

        final Response response = webServerServiceRest.getWebServer(Identifier.id(1l, WebServer.class));
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
        when(impl.createWebServer(any(CreateWebServerRequest.class), any(User.class))).thenReturn(webServer);

        final Response response = webServerServiceRest.createWebServer(jsonCreateWebServer, authenticatedUser);
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());

        final ApplicationResponse applicationResponse = (ApplicationResponse) response.getEntity();
        final Object content = applicationResponse.getApplicationResponseContent();
        assertTrue(content instanceof WebServer);

        final WebServer received = (WebServer) content;
        assertEquals(webServer, received);
    }

    @Test
    public void testCreateWebServerAndPopulateConfigs() {
        final JsonCreateWebServer jsonCreateWebServer = mock(JsonCreateWebServer.class);
        when(impl.createWebServer(any(CreateWebServerRequest.class), any(User.class))).thenReturn(webServer);

        final ArrayList<ResourceType> resourceTypes = new ArrayList<>();
        ResourceType mockWsResourceType = mock(ResourceType.class);
        when(mockWsResourceType.getConfigFileName()).thenReturn("httpd.conf");
        when(mockWsResourceType.getEntityType()).thenReturn("webServer");
        when(mockWsResourceType.getTemplateName()).thenReturn("HttpdSslConfTemplate.tpl");
        resourceTypes.add(mockWsResourceType);
        when(resourceService.getResourceTypes()).thenReturn(resourceTypes);

        final Response response = webServerServiceRest.createWebServer(jsonCreateWebServer, authenticatedUser);
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
    }

    @Test(expected = InternalErrorException.class)
    public void testCreateWebServerAndPopulateConfigsThrowsExceptionForFileNotFound() {
        final JsonCreateWebServer jsonCreateWebServer = mock(JsonCreateWebServer.class);
        when(impl.createWebServer(any(CreateWebServerRequest.class), any(User.class))).thenReturn(webServer);

        final ArrayList<ResourceType> resourceTypes = new ArrayList<>();
        ResourceType mockWsResourceType = mock(ResourceType.class);
        when(mockWsResourceType.getConfigFileName()).thenReturn("httpd.conf");
        when(mockWsResourceType.getEntityType()).thenReturn("webServer");
        when(mockWsResourceType.getTemplateName()).thenReturn("HttpdSslConfTemplate_FILE-NOT-FOUND.tpl");
        resourceTypes.add(mockWsResourceType);
        when(resourceService.getResourceTypes()).thenReturn(resourceTypes);

        webServerServiceRest.createWebServer(jsonCreateWebServer, authenticatedUser);
    }

    @Test
    public void testUpdateWebServer() {
        final JsonUpdateWebServer jsonUpdateWebServer = mock(JsonUpdateWebServer.class);
        when(impl.updateWebServer(any(UpdateWebServerRequest.class), any(User.class))).thenReturn(webServer);

        final Response response = webServerServiceRest.updateWebServer(jsonUpdateWebServer, authenticatedUser);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        final ApplicationResponse applicationResponse = (ApplicationResponse) response.getEntity();

        final Object content = applicationResponse.getApplicationResponseContent();
        assertTrue(content instanceof WebServer);

        final WebServer received = (WebServer) content;
        assertEquals(webServer, received);
    }

    @Test
    public void testRemoveWebServer() {
        final Response response = webServerServiceRest.removeWebServer(Identifier.id(1l, WebServer.class));
        verify(impl, atLeastOnce()).removeWebServer(any(Identifier.class));
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        final ApplicationResponse applicationResponse = (ApplicationResponse) response.getEntity();
        assertNull(applicationResponse);
    }

    @Test
    public void testControlWebServer() {

        final CommandOutput execData = mock(CommandOutput.class);
        final ExecReturnCode execDataReturnCode = mock(ExecReturnCode.class);
        when(execDataReturnCode.wasSuccessful()).thenReturn(true);
        when(execData.getReturnCode()).thenReturn(execDataReturnCode);

        final JsonControlWebServer jsonControlWebServer = new JsonControlWebServer("start");
        when(webServerControlService.controlWebServer(any(ControlWebServerRequest.class), any(User.class))).thenReturn(execData);
        final Response response = webServerServiceRest.controlWebServer(Identifier.id(1l, WebServer.class), jsonControlWebServer, authenticatedUser);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Test(expected = InternalErrorException.class)
    public void testControlWebServerThrowsExpcetionForFailedCommandOutput() {
        final CommandOutput execData = mock(CommandOutput.class);
        final ExecReturnCode execDataReturnCode = mock(ExecReturnCode.class);
        final JsonControlWebServer jsonControlWebServer = new JsonControlWebServer("start");
        when(execDataReturnCode.wasSuccessful()).thenReturn(false);
        when(execData.getReturnCode()).thenReturn(new ExecReturnCode(1));
        when(execData.getStandardError()).thenReturn("TEST ERROR");
        when(execData.getStandardOutput()).thenReturn("");
        when(webServerControlService.controlWebServer(any(ControlWebServerRequest.class), any(User.class))).thenReturn(execData);
        webServerServiceRest.controlWebServer(Identifier.id(1l, WebServer.class), jsonControlWebServer, authenticatedUser);
    }

    @Test
    public void testGenerateHttpdConfig() {
        when(impl.generateHttpdConfig(anyString(), anyBoolean())).thenReturn("httpd configuration");
        Response response = webServerServiceRest.generateConfig("any-server-name", null);
        assertEquals("httpd configuration", response.getEntity());
    }

    @Test
    public void testGenerateWorkerProperties() {
        when(impl.generateWorkerProperties(anyString()))
                .thenReturn("worker properties");
        Response response = webServerServiceRest.generateLoadBalancerConfig("");
        assertEquals("worker properties", response.getEntity());
    }

    @Test
    public void testGetWebServersByGroup() {
        final List<WebServer> webServers = new ArrayList<>();
        webServers.add(new WebServer(null, new ArrayList<Group>(), "test", null, null, null, new Path("/statusPath"),
                new FileSystemPath("d:/some-dir/httpd.conf"), SVR_ROOT, DOC_ROOT, WebServerReachableState.WS_UNREACHABLE,
                null));

        final Identifier<Group> groupId = new Identifier<>("1");

        when(impl.findWebServers(Matchers.eq(groupId))).thenReturn(webServers);
        final Response response = webServerServiceRest.getWebServers(groupId);

        final List<WebServer> result =
                (List<WebServer>) ((ApplicationResponse) response.getEntity()).getApplicationResponseContent();
        assertEquals("test", result.get(0).getName());
    }

    @Test
    public void testGenerateAndDeployConfig() throws CommandFailureException, IOException {
        System.setProperty(ApplicationProperties.PROPERTIES_ROOT_PATH, "./src/test/resources");
        final String httpdConfDirPath = ApplicationProperties.get("paths.httpd.conf");
        assertTrue(new File(httpdConfDirPath).mkdirs());
        CommandOutput retSuccessExecData = new CommandOutput(new ExecReturnCode(0), "", "");
        when(rtCommand.execute()).thenReturn(retSuccessExecData);
        when(rtCommandBuilder.build()).thenReturn(rtCommand);
        when(webServerControlService.secureCopyHttpdConf(anyString(), anyString(), anyString())).thenReturn(retSuccessExecData);
        Response response = webServerServiceRest.generateAndDeployConfig(webServer.getName());
        assertTrue(response.hasEntity());
        FileUtils.deleteDirectory(new File(httpdConfDirPath));
        System.clearProperty(ApplicationProperties.PROPERTIES_ROOT_PATH);
    }

    @Test(expected = InternalErrorException.class)
    public void testGenerateAndDeployConfigThrowsExceptionForFileNotFound() throws CommandFailureException, IOException {
        webServerServiceRest.generateAndDeployConfig(webServer.getName());
    }

    @Test
    public void testGenerateAndDeployConfigThrowsExceptionForWebServerNotStopped() throws CommandFailureException, IOException {
        System.setProperty(ApplicationProperties.PROPERTIES_ROOT_PATH, "./src/test/resources");

        final String httpdConfDirPath = ApplicationProperties.get("paths.httpd.conf");
        assertTrue(new File(httpdConfDirPath).mkdirs());
        when(impl.isStarted(any(WebServer.class))).thenReturn(true);

        boolean exceptionThrown = false;
        try {
            webServerServiceRest.generateAndDeployConfig(webServer.getName());
        } catch (InternalErrorException ie) {
            exceptionThrown = true;
            assertEquals(ie.getMessage(), "The target Web Server must be stopped before attempting to update the resource file");
        } finally {
            assertTrue(exceptionThrown);
            FileUtils.deleteDirectory(new File(httpdConfDirPath));
            System.clearProperty(ApplicationProperties.PROPERTIES_ROOT_PATH);
        }
    }

    @Test
    public void testGenerateAndDeployConfigFailsSecureCopy() throws CommandFailureException, IOException {
        System.setProperty(ApplicationProperties.PROPERTIES_ROOT_PATH, "./src/test/resources");
        final String httpdConfDirPath = ApplicationProperties.get("paths.httpd.conf");
        assertTrue(new File(httpdConfDirPath).mkdirs());
        CommandOutput retSuccessExecData = new CommandOutput(new ExecReturnCode(0), "", "");
        when(rtCommand.execute()).thenReturn(retSuccessExecData);
        when(rtCommandBuilder.build()).thenReturn(rtCommand);
        when(webServerControlService.secureCopyHttpdConf(anyString(), anyString(), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(1), "", "FAILED SECURE COPY TEST"));
        boolean failedSecureCopy = false;
        Response response = null;
        try {
            response = webServerServiceRest.generateAndDeployConfig(webServer.getName());
        } catch (InternalErrorException e) {
            failedSecureCopy = true;
        } finally {
            FileUtils.deleteDirectory(new File(httpdConfDirPath));
        }
        assertFalse(failedSecureCopy);
        assertNotNull(response);
        assertEquals(webServer.getName(), ((Map) ((ApplicationResponse) response.getEntity()).getApplicationResponseContent()).get("webServerName"));
        System.clearProperty(ApplicationProperties.PROPERTIES_ROOT_PATH);
    }

    @Test
    public void testGenerateAndDeployConfigThrowsException() throws IOException, CommandFailureException {
        System.setProperty(ApplicationProperties.PROPERTIES_ROOT_PATH, "./src/test/resources");
        final String httpdConfDirPath = ApplicationProperties.get("paths.httpd.conf");
        assertTrue(new File(httpdConfDirPath).mkdirs());
        CommandOutput retSuccessExecData = new CommandOutput(new ExecReturnCode(0), "", "");
        when(rtCommand.execute()).thenReturn(retSuccessExecData);
        when(rtCommandBuilder.build()).thenReturn(rtCommand);
        when(webServerControlService.secureCopyHttpdConf(anyString(), anyString(), anyString())).thenThrow(new CommandFailureException(new ExecCommand("Fail secure copy"), new Exception()));
        Response response = null;
        try {
            response = webServerServiceRest.generateAndDeployConfig(webServer.getName());
        } finally {
            FileUtils.deleteDirectory(new File(httpdConfDirPath));
        }
        assertNotNull(response);
        assertEquals(webServer.getName(), ((Map) ((ApplicationResponse) response.getEntity()).getApplicationResponseContent()).get("webServerName"));
        System.clearProperty(ApplicationProperties.PROPERTIES_ROOT_PATH);
    }

    @Test
    public void testGetHttpdConfig() throws CommandFailureException {
        when(commandImpl.getHttpdConf(webServer.getId())).thenReturn(new CommandOutput(new ExecReturnCode(0), "", ""));
        Response response = webServerServiceRest.getHttpdConfig(webServer.getId());
        assertTrue(response.hasEntity());
    }

    @Test
    public void testGetHttpdConfigThrowsException() throws CommandFailureException {
        when(commandImpl.getHttpdConf(webServer.getId())).thenThrow(CommandFailureException.class);
        Response response = webServerServiceRest.getHttpdConfig(webServer.getId());
        assertTrue(response.hasEntity());
    }

    @Test
    public void testGetResourceName() {
        List<String> resourceNames = new ArrayList<>();
        resourceNames.add("httpd-test.tpl");
        when(impl.getResourceTemplateNames(webServer.getName())).thenReturn(resourceNames);
        Response response = webServerServiceRest.getResourceNames(webServer.getName());
        assertTrue(response.hasEntity());
    }

    @Test
    public void testGetResourceTemplates() {
        String resourceTemplateName = "httpd-conf.tpl";
        when(impl.getResourceTemplate(webServer.getName(), resourceTemplateName, false)).thenReturn("ServerRoot=./test");
        Response response = webServerServiceRest.getResourceTemplate(webServer.getName(), resourceTemplateName, false);
        assertTrue(response.hasEntity());
    }

    @Test
    public void testUpdateResourceTemplate() {
        String resourceTemplateName = "httpd-conf.tpl";
        String content = "ServerRoot=./test-update";
        when(impl.updateResourceTemplate(webServer.getName(), resourceTemplateName, content)).thenReturn(content);
        Response response = webServerServiceRest.updateResourceTemplate(webServer.getName(), resourceTemplateName, content);
        assertTrue(response.hasEntity());
    }

    @Test
    public void testUpdateResourceTemplateException() {
        String resourceTemplateName = "httpd-conf.tpl";
        String content = "ServerRoot=./test-update";
        when(impl.updateResourceTemplate(webServer.getName(), resourceTemplateName, content)).thenThrow(ResourceTemplateUpdateException.class);
        Response response = webServerServiceRest.updateResourceTemplate(webServer.getName(), resourceTemplateName, content);
        assertTrue(response.hasEntity());
    }

    @Test
    public void testUploadConfigTemplateThrowsBadStreamException() {
        MessageContext mockMessageContext = mock(MessageContext.class);
        HttpHeaders mockHttpHeaders = mock(HttpHeaders.class);
        HttpServletRequest mockHttpServletReq = mock(HttpServletRequest.class);
        List<MediaType> mediaTypeList = new ArrayList<>();
        mediaTypeList.add(MediaType.APPLICATION_JSON_TYPE);
        when(mockHttpHeaders.getAcceptableMediaTypes()).thenReturn(mediaTypeList);
        when(mockMessageContext.getHttpHeaders()).thenReturn(mockHttpHeaders);
        when(mockMessageContext.getHttpServletRequest()).thenReturn(mockHttpServletReq);
        when(impl.getWebServer(webServer.getName())).thenReturn(webServer);
        webServerServiceRest.setMessageContext(mockMessageContext);
        try {
            webServerServiceRest.uploadConfigTemplate(webServer.getName(), authenticatedUser, "HttpdSslConfTemplate.tpl");
        } catch (Exception e) {
            assertEquals("Error receiving data", e.getMessage());
        }
    }

    @Test
    public void testPreviewResourceTemplate() {
        Response response = webServerServiceRest.previewResourceTemplate(webServer.getName(), "groupName", "httpd.conf");
        assertNotNull(response);

        when(impl.previewResourceTemplate(anyString(), anyString(), anyString())).thenThrow(new RuntimeException("test runtime exception"));
        response = webServerServiceRest.previewResourceTemplate(webServer.getName(), "groupName", "httpd.conf");
        assertNotNull(response);
    }

    @Test(expected = InternalErrorException.class)
    public void testUploadConfigTemplateThrowsInternalErrorExceptionForNoWebServer() throws IOException {
        final MessageContext msgContextMock = mock(MessageContext.class);
        final HttpHeaders httpHeadersMock = mock(HttpHeaders.class);
        final List<MediaType> mediaTypeList = new ArrayList<>();
        final HttpServletRequest httpServletRequestMock = mock(HttpServletRequest.class);
        final HttpServletResponse httpServletResponseMock = mock(HttpServletResponse.class);
        when(httpHeadersMock.getAcceptableMediaTypes()).thenReturn(mediaTypeList);
        when(msgContextMock.getHttpHeaders()).thenReturn(httpHeadersMock);
        when(msgContextMock.getHttpServletRequest()).thenReturn(httpServletRequestMock);
        when(msgContextMock.getHttpServletResponse()).thenReturn(httpServletResponseMock);
        when(httpServletRequestMock.getContentType()).thenReturn("multipart/form-data; boundary=----WebKitFormBoundaryXRxegBGqTe4gApI2");
        when(httpServletRequestMock.getInputStream()).thenReturn(new DelegatingServletInputStream());
        webServerServiceRest.setMessageContext(msgContextMock);

        final SecurityContext securityContextMock = mock(SecurityContext.class);
        final AuthenticatedUser authenticatedUser = new AuthenticatedUser(securityContextMock);

        webServerServiceRest.uploadConfigTemplate(webServer.getName(), authenticatedUser, "HttpdSslConfTemplate.tpl");
        verify(impl).uploadWebServerConfig(any(UploadWebServerTemplateRequest.class), any(User.class));
    }

    @Test
    public void testUploadConfigTemplate() throws IOException {
        final MessageContext msgContextMock = mock(MessageContext.class);
        final HttpHeaders httpHeadersMock = mock(HttpHeaders.class);
        final List<MediaType> mediaTypeList = new ArrayList<>();
        final HttpServletRequest httpServletRequestMock = mock(HttpServletRequest.class);
        final HttpServletResponse httpServletResponseMock = mock(HttpServletResponse.class);
        when(httpHeadersMock.getAcceptableMediaTypes()).thenReturn(mediaTypeList);
        when(msgContextMock.getHttpHeaders()).thenReturn(httpHeadersMock);
        when(msgContextMock.getHttpServletRequest()).thenReturn(httpServletRequestMock);
        when(msgContextMock.getHttpServletResponse()).thenReturn(httpServletResponseMock);
        when(httpServletRequestMock.getContentType()).thenReturn("multipart/form-data; boundary=----WebKitFormBoundaryXRxegBGqTe4gApI2");
        when(httpServletRequestMock.getInputStream()).thenReturn(new DelegatingServletInputStream());
        when(impl.getWebServer(webServer.getName())).thenReturn(webServer);
        webServerServiceRest.setMessageContext(msgContextMock);

        final SecurityContext securityContextMock = mock(SecurityContext.class);
        final AuthenticatedUser authenticatedUser = new AuthenticatedUser(securityContextMock);

        webServerServiceRest.uploadConfigTemplate(webServer.getName(), authenticatedUser, "HttpdSslConfTemplate.tpl");
        verify(impl).uploadWebServerConfig(any(UploadWebServerTemplateRequest.class), any(User.class));

    }

    @Test
    public void testUploadConfigNoContent() throws IOException {

        verify(impl, never()).uploadWebServerConfig(any(UploadWebServerTemplateRequest.class), any(User.class));

        // ISO8859-1
        String boundary = "--WebKitFormBoundarywBZFyEeqG5xW80nx";

        String content = "";

        String charsetBin = "ISO-8859-1";
        ByteBuffer bbBuffer = Charset.forName(charsetBin).encode(content);
        Application mockApp = mock(Application.class);
        final HttpServletRequest mockHsr = mock(HttpServletRequest.class);
        final MessageContext msgContextMock = mock(MessageContext.class);
        final HttpServletResponse httpServletResponseMock = mock(HttpServletResponse.class);
        final HttpHeaders httpHeadersMock = mock(HttpHeaders.class);
        final List<MediaType> mediaTypeList = new ArrayList<>();
        when(httpHeadersMock.getAcceptableMediaTypes()).thenReturn(mediaTypeList);
        when(msgContextMock.getHttpHeaders()).thenReturn(httpHeadersMock);
        when(msgContextMock.getHttpServletRequest()).thenReturn(mockHsr);
        when(msgContextMock.getHttpServletResponse()).thenReturn(httpServletResponseMock);
        when(mockHsr.getCharacterEncoding()).thenReturn(charsetBin);
        when(mockHsr.getInputStream()).thenReturn(new MyIS(new ByteArrayInputStream(bbBuffer.array())));
        when(mockHsr.getContentType()).thenReturn(FileUploadBase.MULTIPART_FORM_DATA + ";boundary=" + boundary);
        when(impl.getWebServer(webServer.getName())).thenReturn(webServer);
        when(mockApp.getName()).thenReturn("NoContentTestApp");
        webServerServiceRest.setMessageContext(msgContextMock);

        Response resp = webServerServiceRest.uploadConfigTemplate(webServer.getName(), authenticatedUser, "HttpdSslConfTemplate.tpl");
        assertEquals(Response.Status.NO_CONTENT.getStatusCode(), resp.getStatus());
    }

    /**
     * Instead of mocking the ServletInputStream, let's extend it instead.
     *
     * @see "http://stackoverflow.com/questions/20995874/how-to-mock-a-javax-servlet-servletinputstream"
     */
    static class DelegatingServletInputStream extends ServletInputStream {

        private InputStream inputStream;

        public DelegatingServletInputStream() {
            inputStream = new ByteArrayInputStream("------WebKitFormBoundaryXRxegBGqTe4gApI2\r\nContent-Disposition: form-data; name=\"hct.properties\"; filename=\"hotel-booking.txt\"\r\nContent-Type: text/plain\r\n\r\n\r\n------WebKitFormBoundaryXRxegBGqTe4gApI2--".getBytes(Charset.defaultCharset()));
        }

        /**
         * Return the underlying source stream (never <code>null</code>).
         */
        public final InputStream getSourceStream() {
            return inputStream;
        }


        public int read() throws IOException {
            return inputStream.read();
        }

        public void close() throws IOException {
            super.close();
            inputStream.close();
        }

    }

    private class MyIS extends ServletInputStream {

        private InputStream backingStream;

        public MyIS(InputStream backingStream) {
            this.backingStream = backingStream;
        }

        @Override
        public int read() throws IOException {
            return backingStream.read();
        }

    }

}
