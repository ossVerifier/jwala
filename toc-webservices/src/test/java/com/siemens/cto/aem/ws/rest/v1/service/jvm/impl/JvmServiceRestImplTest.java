package com.siemens.cto.aem.ws.rest.v1.service.jvm.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import org.apache.commons.fileupload.FileUploadBase;
import org.apache.commons.io.FileUtils;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.jcraft.jsch.JSchException;
import com.siemens.cto.aem.common.domain.model.app.Application;
import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.jvm.Jvm;
import com.siemens.cto.aem.common.domain.model.jvm.JvmControlOperation;
import com.siemens.cto.aem.common.domain.model.jvm.JvmState;
import com.siemens.cto.aem.common.domain.model.path.Path;
import com.siemens.cto.aem.common.domain.model.resource.ResourceGroup;
import com.siemens.cto.aem.common.domain.model.resource.ResourceTemplateMetaData;
import com.siemens.cto.aem.common.domain.model.resource.ResourceType;
import com.siemens.cto.aem.common.domain.model.user.User;
import com.siemens.cto.aem.common.exception.InternalErrorException;
import com.siemens.cto.aem.common.exec.CommandOutput;
import com.siemens.cto.aem.common.exec.CommandOutputReturnCode;
import com.siemens.cto.aem.common.exec.ExecCommand;
import com.siemens.cto.aem.common.exec.ExecReturnCode;
import com.siemens.cto.aem.common.properties.ApplicationProperties;
import com.siemens.cto.aem.common.request.jvm.ControlJvmRequest;
import com.siemens.cto.aem.common.request.jvm.CreateJvmAndAddToGroupsRequest;
import com.siemens.cto.aem.common.request.jvm.CreateJvmRequest;
import com.siemens.cto.aem.common.request.jvm.UpdateJvmRequest;
import com.siemens.cto.aem.common.request.jvm.UploadJvmTemplateRequest;
import com.siemens.cto.aem.exception.CommandFailureException;
import com.siemens.cto.aem.persistence.jpa.service.exception.ResourceTemplateUpdateException;
import com.siemens.cto.aem.service.jvm.JvmControlService;
import com.siemens.cto.aem.service.jvm.impl.JvmServiceImpl;
import com.siemens.cto.aem.service.jvm.state.JvmStateReceiverAdapter;
import com.siemens.cto.aem.service.resource.ResourceService;
import com.siemens.cto.aem.ws.rest.v1.provider.AuthenticatedUser;
import com.siemens.cto.aem.ws.rest.v1.response.ApplicationResponse;

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
    private static final String userName = "JoeThePlumber";
    private static final String encryptedPassword = "The Quick Brown Fox";
    
    @Mock
    private JvmServiceImpl jvmService;
    @Mock
    private JvmControlService jvmControlService;
    @Mock
    private AuthenticatedUser authenticatedUser;
    @Mock
    private ResourceService resourceService;
    @Mock
    private JvmStateReceiverAdapter jvmStateReceiverAdapter;

    private Map<String, ReentrantReadWriteLock> writeLockMap;

    private JvmServiceRestImpl jvmServiceRest;

    private static List<Jvm> createJvmList() {
        final Set<Group> groups = new HashSet<>();
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
                systemProperties,
                JvmState.JVM_STOPPED,
                null, null, userName, encryptedPassword);
        final List<Jvm> result = new ArrayList<>();
        result.add(ws);
        return result;
    }

    @Before
    public void setUp() {
        System.setProperty(ApplicationProperties.PROPERTIES_ROOT_PATH, "./src/test/resources");
        writeLockMap = new HashMap<>();
        jvmServiceRest = new JvmServiceRestImpl(jvmService, jvmControlService, resourceService, Executors.newFixedThreadPool(12), writeLockMap);
        when(authenticatedUser.getUser()).thenReturn(new User("Unused"));
        try {
            jvmServiceRest.afterPropertiesSet();
        } catch (Exception e) {
            assertTrue("This should not fail, but ... " + e.getMessage(), false);
        }
    }

    @After
    public void cleanUp() {
        System.clearProperty(ApplicationProperties.PROPERTIES_ROOT_PATH);
    }

    @Test
    public void testGetJvmList() {
        when(jvmService.getJvms()).thenReturn(jvmList);

        final Response response = jvmServiceRest.getJvms();
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
        when(jvmService.getJvm(any(Identifier.class))).thenReturn(jvm);

        final Response response = jvmServiceRest.getJvm(Identifier.id(1l, Jvm.class));
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        final ApplicationResponse applicationResponse = (ApplicationResponse) response.getEntity();
        final Object content = applicationResponse.getApplicationResponseContent();
        assertTrue(content instanceof Jvm);

        final Jvm received = (Jvm) content;
        assertEquals(jvm, received);
    }

    @Test
    public void testCreateJvm() {
        when(jvmService.createJvm(any(CreateJvmRequest.class), any(User.class))).thenReturn(jvm);

        final JsonCreateJvm jsonCreateJvm = new JsonCreateJvm(name, hostName, httpPort, httpsPort, redirectPort,
                shutdownPort, ajpPort, statusPath.getUriPath(), systemProperties, userName, encryptedPassword);
        final Response response = jvmServiceRest.createJvm(jsonCreateJvm, authenticatedUser);
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());

        final ApplicationResponse applicationResponse = (ApplicationResponse) response.getEntity();
        final Object content = applicationResponse.getApplicationResponseContent();
        assertTrue(content instanceof Jvm);

        final Jvm received = (Jvm) content;
        assertEquals(jvm, received);
    }

    @Test
    public void testCreateJvmWithGroups() {
        when(jvmService.createAndAssignJvm(any(CreateJvmAndAddToGroupsRequest.class), any(User.class))).thenReturn(jvm);

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
                systemProperties,
                userName,
                encryptedPassword);
        final Response response = jvmServiceRest.createJvm(jsonCreateJvm, authenticatedUser);
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
                statusPath.getUriPath(), systemProperties, userName, encryptedPassword);
        when(jvmService.updateJvm(any(UpdateJvmRequest.class), any(User.class))).thenReturn(jvm);

        // Check rules for the JVM
        UpdateJvmRequest updateJvmCommand = jsonUpdateJvm.toUpdateJvmRequest();
        updateJvmCommand.validate();
        updateJvmCommand.hashCode();
        updateJvmCommand.equals(jsonUpdateJvm.toUpdateJvmRequest());
        String check = updateJvmCommand.toString();

        final Response response = jvmServiceRest.updateJvm(jsonUpdateJvm, authenticatedUser);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        final ApplicationResponse applicationResponse = (ApplicationResponse) response.getEntity();

        final Object content = applicationResponse.getApplicationResponseContent();
        assertTrue(content instanceof Jvm);

        final Jvm received = (Jvm) content;
        assertEquals(jvm, received);
    }

    @Test
    public void testRemoveJvm() {
        when(jvmService.getJvm(jvm.getId())).thenReturn(jvm);
        when(jvmService.getJvm(anyString())).thenReturn(jvm);
        when(jvmControlService.controlJvm(any(ControlJvmRequest.class), any(User.class))).thenReturn(new CommandOutput(new ExecReturnCode(0), "", ""));

        Response response = jvmServiceRest.removeJvm(jvm.getId(), authenticatedUser);
        verify(jvmService, atLeastOnce()).removeJvm(jvm.getId());
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        final ApplicationResponse applicationResponse = (ApplicationResponse) response.getEntity();
        assertNull(applicationResponse);

        Jvm mockJvmStarted = mock(Jvm.class);
        when(mockJvmStarted.getState()).thenReturn(JvmState.JVM_STARTED);
        when(jvmService.getJvm(any(Identifier.class))).thenReturn(mockJvmStarted);
        boolean exceptionThrown = false;
        try {
            response = jvmServiceRest.removeJvm(jvm.getId(), authenticatedUser);
        } catch (Exception e) {
            assertEquals("The target JVM must be stopped before attempting to delete it", e.getMessage());
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
    }

    @Test
    public void testControlJvm() {
        when(jvmControlService.controlJvm(any(ControlJvmRequest.class), any(User.class))).thenReturn(new CommandOutput(new ExecReturnCode(0), "", ""));

        final CommandOutput execData = mock(CommandOutput.class);
        final ExecReturnCode execDataReturnCode = mock(ExecReturnCode.class);
        when(execDataReturnCode.wasSuccessful()).thenReturn(true);
        when(execData.getReturnCode()).thenReturn(execDataReturnCode);

        final JsonControlJvm jsonControlJvm = new JsonControlJvm("start");
        final Response response = jvmServiceRest.controlJvm(Identifier.id(1l, Jvm.class), jsonControlJvm, authenticatedUser);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());


        final ApplicationResponse applicationResponse = (ApplicationResponse) response.getEntity();
        final Object content = applicationResponse.getApplicationResponseContent();
        assertTrue(content instanceof CommandOutput);

        when(execDataReturnCode.wasSuccessful()).thenReturn(false);
        when(execDataReturnCode.getReturnCode()).thenReturn(123);
        boolean exceptionThrown = false;

        when(jvmControlService.controlJvm(any(ControlJvmRequest.class), any(User.class))).thenReturn(new CommandOutput(execDataReturnCode, "", "Jvm Control Failed"));
        try {
            jvmServiceRest.controlJvm(Identifier.id(1l, Jvm.class), jsonControlJvm, authenticatedUser);
        } catch (Exception e) {
            exceptionThrown = true;
            assertEquals(CommandOutputReturnCode.NO_SUCH_SERVICE.getDesc(), e.getMessage());
        }
        assertTrue(exceptionThrown);
    }

    @Test
    public void testGenerateAndDeployConfigExecutorService() throws CommandFailureException, IOException {
        System.setProperty(ApplicationProperties.PROPERTIES_ROOT_PATH, "./src/test/resources");
        when(jvmService.getJvm(jvm.getJvmName())).thenReturn(jvm);
        when(jvmService.generateConfigFile(jvm.getJvmName(), "server.xml")).thenReturn("<server>xml-content</server>");
        when(jvmService.generateConfigFile(jvm.getJvmName(), "context.xml")).thenReturn("<content>xml-content</content>");
        when(jvmService.generateConfigFile(jvm.getJvmName(), "setenv.bat")).thenReturn("SET TEST=xxtestxx");
        when(jvmControlService.secureCopyFile(any(ControlJvmRequest.class), anyString(), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(0), "", ""));

        boolean failsScp = false;
        Response response = null;
        try {
            response = jvmServiceRest.generateAndDeployJvm(jvm.getJvmName(), authenticatedUser);
        } catch (InternalErrorException e) {
            failsScp = true;
        }
        assertFalse(failsScp);
        assertTrue(response != null && response.hasEntity());
        FileUtils.deleteDirectory(new File("./" + jvm.getJvmName()));
    }

    @Test
    public void testGenerateAndDeployConfig() throws CommandFailureException, IOException {
        Collection<ResourceType> mockResourceTypes = new ArrayList<>();
        ResourceType mockResource = mock(ResourceType.class);
        mockResourceTypes.add(mockResource);
        CommandOutput commandOutput = mock(CommandOutput.class);
        Jvm mockJvm = mock(Jvm.class);
        when(mockJvm.getState()).thenReturn(JvmState.JVM_STOPPED);
        when(commandOutput.getReturnCode()).thenReturn(new ExecReturnCode(0));
        when(mockResource.getEntityType()).thenReturn("jvm");
        when(mockResource.getTemplateName()).thenReturn("ServerXMLTemplate.tpl");
        when(mockResource.getConfigFileName()).thenReturn("server.xml");
        when(jvmControlService.secureCopyFile(any(ControlJvmRequest.class), anyString(), anyString())).thenReturn(commandOutput);
        when(jvmControlService.createDirectory(any(Jvm.class), anyString())).thenReturn(commandOutput);
        when(jvmControlService.changeFileMode(any(Jvm.class), anyString(), anyString(), anyString())).thenReturn(commandOutput);

        when(jvmControlService.controlJvm(new ControlJvmRequest(jvm.getId(), JvmControlOperation.DELETE_SERVICE), authenticatedUser.getUser())).thenReturn(commandOutput);
        when(jvmControlService.controlJvm(new ControlJvmRequest(jvm.getId(), JvmControlOperation.DEPLOY_CONFIG_ARCHIVE), authenticatedUser.getUser())).thenReturn(commandOutput);
        when(jvmControlService.controlJvm(new ControlJvmRequest(jvm.getId(), JvmControlOperation.INVOKE_SERVICE), authenticatedUser.getUser())).thenReturn(commandOutput);

        when(jvmService.getJvm(anyString())).thenReturn(mockJvm);
        when(jvmService.generateInvokeBat(anyString())).thenReturn("");
        when(jvmService.generateResourceFiles(anyString())).thenReturn(null);
        Jvm response = jvmServiceRest.generateConfFilesAndDeploy(jvm, authenticatedUser);
        assertEquals(response, jvm);
        FileUtils.deleteDirectory(new File("./src/test/resources/jvm-resources_test/" + jvm.getJvmName()));

        CommandOutput mockExecDataFail = mock(CommandOutput.class);
        when(mockExecDataFail.getReturnCode()).thenReturn(new ExecReturnCode(1));
        when(mockExecDataFail.getStandardError()).thenReturn("ERROR");

        when(jvmControlService.controlJvm(new ControlJvmRequest(jvm.getId(), JvmControlOperation.INVOKE_SERVICE), authenticatedUser.getUser())).thenReturn(mockExecDataFail);

        boolean exceptionThrown = false;
        try {
            jvmServiceRest.generateConfFilesAndDeploy(jvm, authenticatedUser);
        } catch (Exception e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
        FileUtils.deleteDirectory(new File("./src/test/resources/jvm-resources_test/" + jvm.getJvmName()));

        exceptionThrown = false;
        try {
            jvmServiceRest.generateConfFilesAndDeploy(jvm, authenticatedUser);
        } catch (Exception e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
        FileUtils.deleteDirectory(new File("./src/test/resources/jvm-resources_test/" + jvm.getJvmName()));

        when(jvmControlService.secureCopyFile(any(ControlJvmRequest.class), anyString(), anyString())).thenReturn(mockExecDataFail);
        exceptionThrown = false;
        try {
            jvmServiceRest.generateConfFilesAndDeploy(jvm, authenticatedUser);
        } catch (Exception e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
        FileUtils.deleteDirectory(new File("./src/test/resources/jvm-resources_test/" + jvm.getJvmName()));

        ExecCommand execCommand = new ExecCommand("fail command");
        Throwable throwable = new JSchException("Failed scp");
        final CommandFailureException commandFailureException = new CommandFailureException(execCommand, throwable);
        when(jvmControlService.secureCopyFile(any(ControlJvmRequest.class), anyString(), anyString())).thenThrow(commandFailureException);
        exceptionThrown = false;
        try {
            jvmServiceRest.generateConfFilesAndDeploy(jvm, authenticatedUser);
        } catch (Exception e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
        FileUtils.deleteDirectory(new File("./src/test/resources/jvm-resources_test/" + jvm.getJvmName()));

        exceptionThrown = false;
        try {
            jvmServiceRest.generateConfFilesAndDeploy(jvm, authenticatedUser);
        } catch (Exception e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
        FileUtils.deleteDirectory(new File("./" + jvm.getJvmName()));
        FileUtils.deleteDirectory(new File("./" + jvm.getJvmName() + "null"));
        FileUtils.deleteDirectory(new File("./src/test/resources/jvm-resources_test/" + jvm.getJvmName()));
        assertTrue(new File("./src/test/resources/jvm-resources_test/" + jvm.getJvmName() + "_config.jar").delete());
    }

    @Test
    public void testGenerateAndDeployConfigFailControlService() throws CommandFailureException {
        System.setProperty(ApplicationProperties.PROPERTIES_ROOT_PATH, "./src/test/resources");
        when(jvmService.getJvm(jvm.getJvmName())).thenReturn(jvm);
        when(jvmService.generateConfigFile(jvm.getJvmName(), "server.xml")).thenReturn("<server>xml-content</server>");
        when(jvmService.generateConfigFile(jvm.getJvmName(), "context.xml")).thenReturn("<content>xml-content</content>");
        when(jvmService.generateConfigFile(jvm.getJvmName(), "setenv.bat")).thenReturn("SET TEST=xxtestxx");
        final CommandOutput successCommandOutput = new CommandOutput(new ExecReturnCode(0), "", "");
        when(jvmControlService.secureCopyFile(any(ControlJvmRequest.class), anyString(), anyString())).thenReturn(successCommandOutput);
        when(jvmControlService.createDirectory(any(Jvm.class), anyString())).thenReturn(successCommandOutput);
        when(jvmControlService.changeFileMode(any(Jvm.class), anyString(), anyString(), anyString())).thenReturn(successCommandOutput);

        when(jvmControlService.controlJvm(any(ControlJvmRequest.class), any(User.class))).thenReturn(new CommandOutput(new ExecReturnCode(1), "", "FAIL CONTROL SERVICE"));

        final Response response = jvmServiceRest.generateAndDeployJvm(jvm.getJvmName(), authenticatedUser);
        assertEquals(CommandOutputReturnCode.FAILED.getDesc(), ((Map) (((ApplicationResponse) response.getEntity()).getApplicationResponseContent())).get("message"));
    }

    @Test
    public void testGenerateAndDeployConfigFailSecureCopyService() throws CommandFailureException {
        System.setProperty(ApplicationProperties.PROPERTIES_ROOT_PATH, "./src/test/resources");
        when(jvmService.getJvm(jvm.getJvmName())).thenReturn(jvm);
        when(jvmService.generateConfigFile(jvm.getJvmName(), "server.xml")).thenReturn("<server>xml-content</server>");
        when(jvmService.generateConfigFile(jvm.getJvmName(), "context.xml")).thenReturn("<content>xml-content</content>");
        when(jvmService.generateConfigFile(jvm.getJvmName(), "setenv.bat")).thenReturn("SET TEST=xxtestxx");
        when(jvmControlService.secureCopyFile(any(ControlJvmRequest.class), anyString(), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(1), "", "FAIL THE SERVICE SECURE COPY TEST"));
        final CommandOutput successCommandOutput = new CommandOutput(new ExecReturnCode(0), "", "");
        when(jvmControlService.createDirectory(any(Jvm.class), anyString())).thenReturn(successCommandOutput);
        when(jvmControlService.changeFileMode(any(Jvm.class), anyString(), anyString(), anyString())).thenReturn(successCommandOutput);

        when(jvmControlService.controlJvm(new ControlJvmRequest(jvm.getId(), JvmControlOperation.DELETE_SERVICE), authenticatedUser.getUser())).thenReturn(successCommandOutput);

        final Response response = jvmServiceRest.generateAndDeployJvm(jvm.getJvmName(), authenticatedUser);
        assertEquals("Failed to secure copy ./src/test/resources/deploy-config-tar.sh during the creation of jvmName", ((Map) (((ApplicationResponse) response.getEntity()).getApplicationResponseContent())).get("message"));
    }

    @Test
    public void testDiagnoseJvm() {
        when(jvmService.performDiagnosis(jvm.getId())).thenReturn("Good Diagnosis!");
        Response response = jvmServiceRest.diagnoseJvm(jvm.getId());
        assertTrue(response.hasEntity());
    }

    @Test
    public void testGetResourceNames() {
        List<String> resourceNames = new ArrayList<>();
        resourceNames.add("a resource name");
        when(jvmService.getResourceTemplateNames(jvm.getJvmName())).thenReturn(resourceNames);
        Response response = jvmServiceRest.getResourceNames(jvm.getJvmName());
        assertTrue(response.hasEntity());
    }

    @Test
    public void testGetResourceTemplate() {
        String resourceTemplateName = "template.tpl";
        when(jvmService.getResourceTemplate(jvm.getJvmName(), resourceTemplateName, false)).thenReturn("${jvm.jvmName");
        Response response = jvmServiceRest.getResourceTemplate(jvm.getJvmName(), resourceTemplateName, false);
        assertTrue(response.hasEntity());
    }

    @Test
    public void testUploadConfigTemplateThrowsBadStreamException() {
        MessageContext mockContext = mock(MessageContext.class);
        HttpHeaders mockHttpHeaders = mock(HttpHeaders.class);
        HttpServletRequest mockHttpServletReq = mock(HttpServletRequest.class);
        List<MediaType> mediaTypeList = new ArrayList<>();
        mediaTypeList.add(MediaType.APPLICATION_JSON_TYPE);
        when(mockHttpHeaders.getAcceptableMediaTypes()).thenReturn(mediaTypeList);
        when(mockContext.getHttpHeaders()).thenReturn(mockHttpHeaders);
        when(mockContext.getHttpServletRequest()).thenReturn(mockHttpServletReq);
        when(jvmService.getJvm(jvm.getJvmName())).thenReturn(jvm);
        jvmServiceRest.setContext(mockContext);
        try {
            Response response = jvmServiceRest.uploadConfigTemplate(jvm.getJvmName(), authenticatedUser, "server.xml");
            assertNotNull(response.getEntity());
        } catch (Exception e) {
            assertEquals("Error receiving data", e.getMessage());
        }
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
        jvmServiceRest.setContext(msgContextMock);

        final SecurityContext securityContextMock = mock(SecurityContext.class);
        final AuthenticatedUser authenticatedUser = new AuthenticatedUser(securityContextMock);

        jvmServiceRest.uploadConfigTemplate(jvm.getJvmName(), authenticatedUser, "server.xml");
        verify(jvmService).uploadJvmTemplateXml(any(UploadJvmTemplateRequest.class), any(User.class));
    }


    @Test
    public void testGenerateAndDeployFile() throws CommandFailureException, IOException {
        ResourceTemplateMetaData mockResourceTemplateMetaData = mock(ResourceTemplateMetaData.class);
        CommandOutput mockExecData = mock(CommandOutput.class);
        when(mockExecData.getReturnCode()).thenReturn(new ExecReturnCode(0));
        when(mockResourceTemplateMetaData.getDeployFileName()).thenReturn("server.xml");
        when(mockResourceTemplateMetaData.getDeployPath()).thenReturn("/");
        when(jvmService.getJvm(jvm.getJvmName())).thenReturn(jvm);
        when(jvmService.generateConfigFile(anyString(), anyString())).thenReturn("<server>xml</server>");
        when(jvmService.getResourceTemplateMetaData(anyString())).thenReturn(mockResourceTemplateMetaData);
        when(jvmControlService.secureCopyFile(any(ControlJvmRequest.class), anyString(), anyString())).thenReturn(mockExecData);
        when(resourceService.generateResourceGroup()).thenReturn(new ResourceGroup());
        Response response = jvmServiceRest.generateAndDeployFile(jvm.getJvmName(), "server.xml", authenticatedUser);
        assertNotNull(response.hasEntity());

        when(mockExecData.getReturnCode()).thenReturn(new ExecReturnCode(1));
        when(mockExecData.getStandardError()).thenReturn("ERROR");
        when(jvmControlService.secureCopyFile(any(ControlJvmRequest.class), anyString(), anyString())).thenReturn(mockExecData);
        boolean exceptionThrown = false;
        try {
            jvmServiceRest.generateAndDeployFile(jvm.getJvmName(), "server.xml", authenticatedUser);
        } catch (Exception e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);

        exceptionThrown = false;
        try {
            jvmServiceRest.generateAndDeployFile(jvm.getJvmName(), "server.xml", authenticatedUser);
        } catch (InternalErrorException e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);

        when(jvmControlService.secureCopyFile(any(ControlJvmRequest.class), anyString(), anyString())).thenThrow(new CommandFailureException(new ExecCommand("fail for secure copy"), new Throwable("test fail")));
        exceptionThrown = false;
        try {
            jvmServiceRest.generateAndDeployFile(jvm.getJvmName(), "server.xml", authenticatedUser);
        } catch (InternalErrorException e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);

        exceptionThrown = false;
        try {
            jvmServiceRest.generateAndDeployFile(jvm.getJvmName(), "server.xml", authenticatedUser);
        } catch (InternalErrorException e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);

        FileUtils.deleteDirectory(new File("./" + jvm.getJvmName()));
    }

    @Test (expected = InternalErrorException.class)
    public void testGenerateAndDeployFileJvmStarted() {
        Jvm mockJvm = mock(Jvm.class);
        when(mockJvm.getState()).thenReturn(JvmState.JVM_STARTED);
        when(mockJvm.getId()).thenReturn(new Identifier<Jvm>(11111L));
        when(jvmService.getJvm(anyString())).thenReturn(mockJvm);
        jvmServiceRest.generateAndDeployFile("jvmName", "fileName", authenticatedUser);
    }

    @Test
    public void testUpdateResourceTemplate() {
        final String updateValue = "<server>update</server>";
        when(jvmService.updateResourceTemplate(anyString(), anyString(), anyString())).thenReturn(updateValue);
        Response response = jvmServiceRest.updateResourceTemplate(jvm.getJvmName(), "ServerXMLTemplate.tpl", updateValue);
        assertNotNull(response.getEntity());

        when(jvmService.updateResourceTemplate(anyString(), anyString(), anyString())).thenThrow(new ResourceTemplateUpdateException(jvm.getJvmName(), "server.xml"));
        response = jvmServiceRest.updateResourceTemplate(jvm.getJvmName(), "ServerXMLTemplate.tpl", updateValue);
        assertNotNull(response.getEntity());
    }

    @Test
    public void testPreviewResourceTemplate() {
        when(jvmService.previewResourceTemplate(anyString(), anyString(), anyString())).thenReturn("<server>preview</server>");
        Response response = jvmServiceRest.previewResourceTemplate(jvm.getJvmName(), "group1", "ServerXMLTemplate.tpl");
        assertNotNull(response.getEntity());

        when(jvmService.previewResourceTemplate(anyString(), anyString(), anyString())).thenThrow(new RuntimeException("Test failed preview"));
        response = jvmServiceRest.previewResourceTemplate(jvm.getJvmName(), "group1", "ServerXMLTemplate.tpl");
        assertNotNull(response.getEntity());
    }

    @Test
    public void testUploadConfigNoContent() throws IOException {

        verify(jvmService, never()).uploadJvmTemplateXml(any(UploadJvmTemplateRequest.class), any(User.class));

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
        when(jvmService.getJvm(jvm.getJvmName())).thenReturn(jvm);
        when(mockApp.getName()).thenReturn("NoContentTestApp");
        jvmServiceRest.setContext(msgContextMock);

        Response resp = jvmServiceRest.uploadConfigTemplate(jvm.getJvmName(), authenticatedUser, "ServerXMLTemplate.tpl");
        assertEquals(Response.Status.NO_CONTENT.getStatusCode(), resp.getStatus());
    }

    @Test
    public void testCreateConfigFile() {
        try {
            jvmServiceRest.createConfigFile("./src/test/resources/", "testConfigFile.bat", "REM BAT ME");
            FileUtils.forceDelete(new File("./src/test/resources/testConfigFile.bat"));
        } catch (IOException e) {
            e.printStackTrace();
            assertFalse(true);
        }
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
