package com.cerner.jwala.ws.rest.v1.service.jvm.impl;

import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.domain.model.jvm.JvmState;
import com.cerner.jwala.common.domain.model.path.Path;
import com.cerner.jwala.common.domain.model.ssh.DecryptPassword;
import com.cerner.jwala.common.domain.model.user.User;
import com.cerner.jwala.common.exec.CommandOutput;
import com.cerner.jwala.common.exec.CommandOutputReturnCode;
import com.cerner.jwala.common.exec.ExecReturnCode;
import com.cerner.jwala.common.properties.ApplicationProperties;
import com.cerner.jwala.common.request.jvm.ControlJvmRequest;
import com.cerner.jwala.common.request.jvm.CreateJvmAndAddToGroupsRequest;
import com.cerner.jwala.common.request.jvm.UpdateJvmRequest;
import com.cerner.jwala.persistence.jpa.service.exception.ResourceTemplateUpdateException;
import com.cerner.jwala.service.jvm.JvmControlService;
import com.cerner.jwala.service.jvm.impl.JvmServiceImpl;
import com.cerner.jwala.service.jvm.state.JvmStateReceiverAdapter;
import com.cerner.jwala.service.resource.ResourceService;
import com.cerner.jwala.ws.rest.v1.provider.AuthenticatedUser;
import com.cerner.jwala.ws.rest.v1.response.ApplicationResponse;
import com.cerner.jwala.ws.rest.v1.service.jvm.impl.JsonControlJvm;
import com.cerner.jwala.ws.rest.v1.service.jvm.impl.JsonCreateJvm;
import com.cerner.jwala.ws.rest.v1.service.jvm.impl.JsonUpdateJvm;
import com.cerner.jwala.ws.rest.v1.service.jvm.impl.JvmServiceRestImpl;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.servlet.ServletInputStream;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
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
    private static final Jvm decryptedJvm = jvm.toDecrypted();
    private static final List<Jvm> decryptedJvmList = createDecryptedList(jvmList);

    // JVM ports
    private static final String httpPort = "80";
    private static final String httpsPort = "81";
    private static final String redirectPort = "82";
    private static final String shutdownPort = "83";
    private static final String ajpPort = "84";
    private static final Path statusPath = new Path("/statusPath");
    private static final String systemProperties = "EXAMPLE_OPTS=%someEnv%/someVal";
    private static final String userName = "JoeThePlumber";
    private static final String clearTextPassword = "The Quick Brown Fox";
    private static final String encryptedPassword = new DecryptPassword().encrypt(clearTextPassword);

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

    private JvmServiceRestImpl jvmServiceRest;
    private String generatedResourceDir;

    private static List<Jvm> createDecryptedList(List<Jvm> inList) {
        final List<Jvm> result = new ArrayList<>();
        for (Jvm jvm : inList) {
            result.add(jvm.toDecrypted());
        }
        return result;
    }

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
                null, null, null, userName, encryptedPassword);
        final List<Jvm> result = new ArrayList<>();
        result.add(ws);
        return result;
    }

    @Before
    public void setUp() {
        System.setProperty(ApplicationProperties.PROPERTIES_ROOT_PATH, "./src/test/resources");
        jvmServiceRest = new JvmServiceRestImpl(jvmService, jvmControlService, resourceService);
        when(authenticatedUser.getUser()).thenReturn(new User("Unused"));
        try {
            jvmServiceRest.afterPropertiesSet();
        } catch (Exception e) {
            assertTrue("This should not fail, but ... " + e.getMessage(), false);
        }

        generatedResourceDir = ApplicationProperties.get("paths.generated.resource.dir");
        assertTrue(new File(generatedResourceDir).mkdirs());
    }

    @After
    public void cleanUp() throws IOException {
        generatedResourceDir = ApplicationProperties.get("paths.generated.resource.dir");
        final File file = new File(generatedResourceDir);
        FileUtils.deleteDirectory(new File(generatedResourceDir));
        assertFalse(file.exists());
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
        assertEquals(decryptedJvm, received);
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
        assertEquals(decryptedJvm, received);
    }

    @Test
    public void testCreateJvm() {
        when(jvmService.createJvm(any(CreateJvmAndAddToGroupsRequest.class), any(User.class))).thenReturn(jvm);

        final JsonCreateJvm jsonCreateJvm = new JsonCreateJvm(name, hostName, httpPort, httpsPort, redirectPort,
                shutdownPort, ajpPort, statusPath.getUriPath(), systemProperties, userName, clearTextPassword);
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
        when(jvmService.createJvm(any(CreateJvmAndAddToGroupsRequest.class), any(User.class))).thenReturn(jvm);

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
                clearTextPassword);
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
                statusPath.getUriPath(), systemProperties, userName, clearTextPassword);
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
        verify(jvmService, atLeastOnce()).removeJvm(eq(jvm.getId()), any(User.class));
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        final ApplicationResponse applicationResponse = (ApplicationResponse) response.getEntity();
        assertNull(applicationResponse);

        // TODO move this test to the JvmServiceImpl tests
//        Jvm mockJvmStarted = mock(Jvm.class);
//        when(mockJvmStarted.getState()).thenReturn(JvmState.JVM_STARTED);
//        when(jvmService.getJvm(any(Identifier.class))).thenReturn(mockJvmStarted);
//        boolean exceptionThrown = false;
//        try {
//            response = jvmServiceRest.removeJvm(jvm.getId(), authenticatedUser);
//        } catch (Exception e) {
//            assertEquals("The target JVM must be stopped before attempting to delete it", e.getMessage());
//            exceptionThrown = true;
//        }
//        assertTrue(exceptionThrown);
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
