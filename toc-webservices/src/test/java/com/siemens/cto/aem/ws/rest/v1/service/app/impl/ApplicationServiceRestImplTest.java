package com.siemens.cto.aem.ws.rest.v1.service.app.impl;

import com.siemens.cto.aem.common.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.common.domain.model.jvm.JvmState;
import com.siemens.cto.aem.common.request.app.UpdateApplicationRequest;
import com.siemens.cto.aem.common.request.app.UploadAppTemplateRequest;
import com.siemens.cto.aem.common.request.app.UploadWebArchiveRequest;
import com.siemens.cto.aem.common.exception.InternalErrorException;
import com.siemens.cto.aem.common.request.app.CreateApplicationRequest;
import com.siemens.cto.aem.common.domain.model.app.Application;
import com.siemens.cto.aem.common.exec.CommandOutput;
import com.siemens.cto.aem.common.exec.ExecReturnCode;
import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.jvm.Jvm;
import com.siemens.cto.aem.common.domain.model.user.User;
import com.siemens.cto.aem.persistence.jpa.service.exception.ResourceTemplateUpdateException;
import com.siemens.cto.aem.service.app.ApplicationService;
import com.siemens.cto.aem.ws.rest.v1.provider.AuthenticatedUser;
import com.siemens.cto.aem.ws.rest.v1.response.ApplicationResponse;
import com.siemens.cto.aem.ws.rest.v1.service.app.ApplicationServiceRest;
import org.apache.commons.fileupload.FileUploadBase;
import org.apache.cxf.common.util.Base64Utility;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.util.Assert;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.siemens.cto.aem.common.domain.model.id.Identifier.id;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationServiceRestImplTest {

    @Mock
    private MessageContext mockMc;
    @Mock
    private HttpServletRequest mockHsr;
    @Mock
    private HttpHeaders mockHh;
    /*NoMock*/ private ApplicationService service;
    @Mock
    private AuthenticatedUser authenticatedUser;
    @InjectMocks
    @Spy
    private ApplicationServiceRestImpl applicationServiceRest = new ApplicationServiceRestImpl(service = Mockito.mock(ApplicationService.class));


    private ApplicationServiceRest cut;

    Group group1 = new Group(Identifier.id(0L, Group.class), "");
    Application application = new Application(Identifier.id(1L, Application.class), "", "", "", group1, true, true, false, "testWar.war");
    Application applicationWithWar = new Application(Identifier.id(1L, Application.class), "", "D:\\APACHE\\TOMCAT\\WEBAPPS\\toc-webapp-1.0-SNAPSHOT-b6349ade-d8f2-4a2f-bdc5-d92d644a1a67-.war", "", group1, true, true, false, "testWar.war");
    Application newlyCreatedApp = new Application(Identifier.id(2L, Application.class), "", "", "", group1, true, true, false, "testWar.war");

    List<Application> applications = new ArrayList<>(1);
    List<Application> applications2 = new ArrayList<>(2);
    List<Application> emptyList = new ArrayList<>(0);

    @Before
    public void setUp() {
        cut = applicationServiceRest;
        applications.add(application);

        applications2.add(application);
        applications2.add(newlyCreatedApp);

        List<MediaType> mtOk = new ArrayList<>();
        mtOk.add(MediaType.APPLICATION_JSON_TYPE);
        when(mockHh.getAcceptableMediaTypes()).thenReturn(mtOk);
        when(mockMc.getHttpHeaders()).thenReturn(mockHh);
        when(mockMc.getHttpServletRequest()).thenReturn(mockHsr);
        when(authenticatedUser.getUser()).thenReturn(new User("unusedUser"));
    }

    @Test
    public void testJsonSettersGetters() {
        JsonUpdateApplication testJua = new JsonUpdateApplication(2L, "name", "/ctx", 1L, true, true, false);
        JsonCreateApplication testJca = new JsonCreateApplication(2L, "name", "/ctx", true, true, false);
        assertEquals(testJca, testJca.clone());
        assertEquals(testJua, testJua.clone());
        assertEquals(testJca.hashCode(), testJca.clone().hashCode());
        assertEquals(testJua.hashCode(), testJua.clone().hashCode());
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

    private class IsValidUploadCommand extends ArgumentMatcher<UploadWebArchiveRequest> {

        @Override
        public boolean matches(Object arg) {
            UploadWebArchiveRequest uwac = (UploadWebArchiveRequest) arg;
            uwac.validate();
            return true;
        }

    }

    @Test
    public void testUploadWebArchive() throws IOException {

        when(service.uploadWebArchive(argThat(new IsValidUploadCommand()), any(User.class))).thenReturn(applicationWithWar);
        when(service.getApplication(any(Identifier.class))).thenReturn(application);

        // ISO8859-1
        String ls = System.lineSeparator();
        String boundary = "--WebKitFormBoundarywBZFyEeqG5xW80nx";

        @SuppressWarnings("unused")
        String http = "Accept:text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8" + ls +
                "Cache-Control:no-cache" + ls +
                "Content-Type:multipart/form-data; boundary=--" + boundary + ls +
                "Origin:null" + ls +
                "Pragma:no-cache" + ls +
                "Referer:" + ls +
                "User-Agent:Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/35.0.1916.114 Safari/537.36" + ls;

        String dataText = "abcdef";
        String contentText = "--" + boundary + ls +
                "Content-Disposition: form-data; name=\"files\"; filename=\"toc-webapp-1.0-SNAPSHOT.war\"" + ls +
                "Content-Type: text/plain" + ls + ls +
                dataText + ls +
                "--" + boundary + "--" + ls;

        String charsetText = "UTF-8";
        ByteBuffer bbBuffer = Charset.forName(charsetText).encode(contentText);
        when(mockHsr.getCharacterEncoding()).thenReturn(charsetText);
        when(mockHsr.getInputStream()).thenReturn(new MyIS(new ByteArrayInputStream(bbBuffer.array())));
        when(mockHsr.getContentType()).thenReturn(FileUploadBase.MULTIPART_FORM_DATA + ";boundary=" + boundary);


        Response resp = cut.uploadWebArchive(application.getId(), authenticatedUser);

        Application result = getApplicationFromResponse(resp);

        assertEquals(Status.CREATED.getStatusCode(), resp.getStatus());

        Assert.hasText(result.getWarPath());
    }

    @Test
    public void testUploadWebArchiveBinary() throws IOException {

        when(service.uploadWebArchive(argThat(new IsValidUploadCommand()), any(User.class))).thenReturn(applicationWithWar);
        when(service.getApplication(any(Identifier.class))).thenReturn(application);

        // ISO8859-1
        String ls = System.lineSeparator();
        String boundary = "--WebKitFormBoundarywBZFyEeqG5xW80nx";

        ByteBuffer file = ByteBuffer.allocate(4);
        file.asShortBuffer().put((short) 0xc0de);
        String data = Base64Utility.encode(file.array());

        @SuppressWarnings("unused")
        String http = "Accept:text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8" + ls +
                "Cache-Control:no-cache" + ls +
                "Content-Type:multipart/form-data; boundary=--" + boundary + ls +
                "Origin:null" + ls +
                "Pragma:no-cache" + ls +
                "Referer:" + ls +
                "User-Agent:Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/35.0.1916.114 Safari/537.36" + ls;

        String content = "--" + boundary + ls +
                "Content-Disposition: form-data; name=\"files\"; filename=\"toc-webapp-1.0-SNAPSHOT.war\"" + ls +
                "Content-Type: application/octet-stream" + ls + ls +
                data + ls +
                "--" + boundary + "--" + ls;

        String charsetBin = "ISO-8859-1";
        ByteBuffer bbBuffer = Charset.forName(charsetBin).encode(content);
        when(mockHsr.getCharacterEncoding()).thenReturn(charsetBin);
        when(mockHsr.getInputStream()).thenReturn(new MyIS(new ByteArrayInputStream(bbBuffer.array())));
        when(mockHsr.getContentType()).thenReturn(FileUploadBase.MULTIPART_FORM_DATA + ";boundary=" + boundary);


        Response resp = cut.uploadWebArchive(application.getId(), authenticatedUser);

        Application result = getApplicationFromResponse(resp);

        assertEquals(Status.CREATED.getStatusCode(), resp.getStatus());

        Assert.hasText(result.getWarPath());
    }

    @Test
    public void testUploadWebArchiveBinaryThrowsInternalErrorExceptionWhenCopyingToJvm() throws IOException {
        when(service.uploadWebArchive(argThat(new IsValidUploadCommand()), any(User.class))).thenThrow(new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE, "test failure"));
        when(service.getApplication(any(Identifier.class))).thenReturn(application);

        // ISO8859-1
        String ls = System.lineSeparator();
        String boundary = "--WebKitFormBoundarywBZFyEeqG5xW80nx";

        ByteBuffer file = ByteBuffer.allocate(4);
        file.asShortBuffer().put((short) 0xc0de);
        String data = Base64Utility.encode(file.array());

        @SuppressWarnings("unused")
        String http = "Accept:text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8" + ls +
                "Cache-Control:no-cache" + ls +
                "Content-Type:multipart/form-data; boundary=--" + boundary + ls +
                "Origin:null" + ls +
                "Pragma:no-cache" + ls +
                "Referer:" + ls +
                "User-Agent:Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/35.0.1916.114 Safari/537.36" + ls;

        String content = "--" + boundary + ls +
                "Content-Disposition: form-data; name=\"files\"; filename=\"toc-webapp-1.0-SNAPSHOT.war\"" + ls +
                "Content-Type: application/octet-stream" + ls + ls +
                data + ls +
                "--" + boundary + "--" + ls;

        String charsetBin = "ISO-8859-1";
        ByteBuffer bbBuffer = Charset.forName(charsetBin).encode(content);
        when(mockHsr.getCharacterEncoding()).thenReturn(charsetBin);
        when(mockHsr.getInputStream()).thenReturn(new MyIS(new ByteArrayInputStream(bbBuffer.array())));
        when(mockHsr.getContentType()).thenReturn(FileUploadBase.MULTIPART_FORM_DATA + ";boundary=" + boundary);

        Response resp = cut.uploadWebArchive(application.getId(), authenticatedUser);
        assertEquals(Status.INTERNAL_SERVER_ERROR.getStatusCode(), resp.getStatus());
    }

    @Test(expected = InternalErrorException.class)
    public void testUploadWebArchiveBadStream() throws IOException {

        when(service.uploadWebArchive(argThat(new IsValidUploadCommand()), any(User.class))).thenReturn(applicationWithWar);
        // ISO8859-1
        String ls = System.lineSeparator();
        String boundary = "--WebKitFormBoundarywBZFyEeqG5xW80nx";

        ByteBuffer file = ByteBuffer.allocate(4);
        file.asShortBuffer().put((short) 0xc0de);
        String data = Base64Utility.encode(file.array());

        @SuppressWarnings("unused")
        String http = "Accept:text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8" + ls +
                "Cache-Control:no-cache" + ls +
                "Content-Type:multipart/form-data; boundary=--" + boundary + ls +
                "Origin:null" + ls +
                "Pragma:no-cache" + ls +
                "Referer:" + ls +
                "User-Agent:Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/35.0.1916.114 Safari/537.36" + ls;

        String content = "--" + boundary + ls +
                "Content-Disposition: form-data; name=\"files\"; filename=\"toc-webapp-1.0-SNAPSHOT.war\"" + ls +
                "Content-Type: application/octet-stream" + ls + ls +
                data + ls +
                /*"--" + bad stream!*/ boundary + "--" + ls;

        String charsetBin = "ISO-8859-1";
        ByteBuffer bbBuffer = Charset.forName(charsetBin).encode(content);
        when(mockHsr.getCharacterEncoding()).thenReturn(charsetBin);
        when(mockHsr.getInputStream()).thenReturn(new MyIS(new ByteArrayInputStream(bbBuffer.array())));
        when(mockHsr.getContentType()).thenReturn(FileUploadBase.MULTIPART_FORM_DATA + ";boundary=" + boundary);

        cut.uploadWebArchive(application.getId(), authenticatedUser);
    }

    @Test
    public void testUploadWebArchiveNoContent() throws IOException {

        verify(service, never()).uploadWebArchive(argThat(new IsValidUploadCommand()), any(User.class));

        // ISO8859-1
        String boundary = "--WebKitFormBoundarywBZFyEeqG5xW80nx";

        String content = "";

        String charsetBin = "ISO-8859-1";
        ByteBuffer bbBuffer = Charset.forName(charsetBin).encode(content);
        Application mockApp = mock(Application.class);
        when(mockHsr.getCharacterEncoding()).thenReturn(charsetBin);
        when(mockHsr.getInputStream()).thenReturn(new MyIS(new ByteArrayInputStream(bbBuffer.array())));
        when(mockHsr.getContentType()).thenReturn(FileUploadBase.MULTIPART_FORM_DATA + ";boundary=" + boundary);
        when(service.getApplication(any(Identifier.class))).thenReturn(mockApp);
        when(mockApp.getName()).thenReturn("NoContentTestApp");


        Response resp = cut.uploadWebArchive(application.getId(), authenticatedUser);

        assertEquals(Status.NO_CONTENT.getStatusCode(), resp.getStatus());
    }

    @Test
    public void testUploadConfigNoContent() throws IOException {

        verify(service, never()).uploadWebArchive(argThat(new IsValidUploadCommand()), any(User.class));

        // ISO8859-1
        String boundary = "--WebKitFormBoundarywBZFyEeqG5xW80nx";

        String content = "";

        String charsetBin = "ISO-8859-1";
        ByteBuffer bbBuffer = Charset.forName(charsetBin).encode(content);
        Application mockApp = mock(Application.class);
        when(mockHsr.getCharacterEncoding()).thenReturn(charsetBin);
        when(mockHsr.getInputStream()).thenReturn(new MyIS(new ByteArrayInputStream(bbBuffer.array())));
        when(mockHsr.getContentType()).thenReturn(FileUploadBase.MULTIPART_FORM_DATA + ";boundary=" + boundary);
        when(service.getApplications()).thenReturn(applications);
        when(mockApp.getName()).thenReturn("NoContentTestApp");

        Response resp = cut.uploadConfigTemplate(application.getName(), authenticatedUser, "ApplicationContextXMLTemplate.tpl", "jvm-1Test");

        assertEquals(Status.NO_CONTENT.getStatusCode(), resp.getStatus());
    }

    @Test
    public void testDeleteWebArchive() throws IOException {
        when(service.getApplication(Matchers.eq(id(1L, Application.class)))).thenReturn(applicationWithWar);
        when(service.deleteWebArchive(Matchers.eq(id(1L, Application.class)), any(User.class))).thenReturn(application);

        Response currentResponse = cut.getApplication(id(1L, Application.class));
        Application current = getApplicationFromResponse(currentResponse);
        assertEquals(applicationWithWar, current);

        Response updatedResponse = cut.deleteWebArchive(id(1L, Application.class), authenticatedUser);
        Application updated = getApplicationFromResponse(updatedResponse);
        assertEquals(application, updated);
    }

    @Test
    public void testGetApplications() {
        when(service.getApplications()).thenReturn(applications);

        Response resp = cut.getApplications(null);
        List<Application> result = getApplicationsFromResponse(resp);

        assertEquals(applications, result);
    }

    @SuppressWarnings("unchecked")
    private List<Application> getApplicationsFromResponse(Response resp) {
        assertNotNull(resp.getEntity());
        ApplicationResponse appResponse = (ApplicationResponse) resp.getEntity();
        Object entity = appResponse.getApplicationResponseContent();
        assertTrue(entity instanceof List<?>);

        return (List<Application>) entity;
    }

    private Application getApplicationFromResponse(Response resp) {
        assertNotNull(resp.getEntity());
        ApplicationResponse appResponse = (ApplicationResponse) resp.getEntity();
        Object entity = appResponse.getApplicationResponseContent();
        assertNotNull(entity);
        assertTrue(entity instanceof Application);

        return (Application) entity;
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testGetApplicationById() {
        when(service.getApplication(any(Identifier.class))).thenReturn(application);
        Response resp = cut.getApplication(id(1L, Application.class));

        Application result = getApplicationFromResponse(resp);

        assertEquals(application, result);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testFindApplicationsByGroupIdNone() {
        when(service.findApplications(any(Identifier.class))).thenReturn(emptyList);
        Response resp = cut.getApplications(id(2L, Group.class));

        List<Application> result = getApplicationsFromResponse(resp);

        assertEquals(emptyList, result);
    }

    @Test
    public void testFindApplicationsByJvmId() {
        when(service.findApplicationsByJvmId(Matchers.eq(id(2L, Jvm.class)))).thenReturn(applications);
        Response resp = cut.findApplicationsByJvmId(id(2L, Jvm.class));
        List<Application> result = getApplicationsFromResponse(resp);

        assertEquals(applications, result);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testFindApplicationsByNullJvmId() {
        when(service.findApplicationsByJvmId(any(Identifier.class))).thenReturn(emptyList);
        Response resp = cut.findApplicationsByJvmId(null);
        List<Application> result = getApplicationsFromResponse(resp);

        assertEquals(emptyList, result);
    }

    /**
     * Testing: {@link com.siemens.cto.aem.ws.rest.v1.service.app.ApplicationServiceRest#createApplication(JsonCreateApplication, AuthenticatedUser)}
     */
    @Test
    public void testCreate() {
        when(service.createApplication(any(CreateApplicationRequest.class), any(User.class))).thenReturn(newlyCreatedApp);

        JsonCreateApplication jsonCreateAppRequest = new JsonCreateApplication();

        Response resp = cut.createApplication(jsonCreateAppRequest, authenticatedUser);
        assertNotNull(resp.getEntity());
        ApplicationResponse appResponse = (ApplicationResponse) resp.getEntity();
        Object entity = appResponse.getApplicationResponseContent();
        assertEquals(this.newlyCreatedApp, entity);
        assertEquals(Status.CREATED.getStatusCode(), resp.getStatus());
    }

    /**
     * Testing: {@link com.siemens.cto.aem.ws.rest.v1.service.app.ApplicationServiceRest#updateApplication(JsonUpdateApplication, AuthenticatedUser)}
     */
    @Test
    public void testUpdate() {
        when(service.updateApplication(any(UpdateApplicationRequest.class), any(User.class))).thenReturn(newlyCreatedApp);
        ArrayList<UpdateApplicationRequest> multiUpdate = new ArrayList<>();
        multiUpdate.add(new UpdateApplicationRequest(Identifier.id(0L, Application.class), Identifier.id(0L, Group.class), "", "", true, true, false));
        JsonUpdateApplication jsonUpdateAppRequest = new JsonUpdateApplication();
        Response resp = cut.updateApplication(jsonUpdateAppRequest, authenticatedUser);
        assertNotNull(resp.getEntity());
        ApplicationResponse appResponse = (ApplicationResponse) resp.getEntity();
        Object entity = appResponse.getApplicationResponseContent();
        assertEquals(this.newlyCreatedApp, entity);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testRemove() {
        Response resp = cut.removeApplication(application.getId(), authenticatedUser);
        Mockito.verify(service, Mockito.times(1)).removeApplication(any(Identifier.class), any(User.class));
        assertNull(resp.getEntity());
        assertEquals(Status.OK.getStatusCode(), resp.getStatus());
    }

    @Test
    public void testGetResourceNames() {
        when(service.getResourceTemplateNames(anyString())).thenReturn(new ArrayList());
        Response response = cut.getResourceNames(application.getName());
        assertNotNull(response.getEntity());
    }

    @Test
    public void testGetResourceTemplate() {
        when(service.getResourceTemplate(anyString(), anyString(), anyString(), anyString(), anyBoolean())).thenReturn("<server>template</server>");
        Response response = cut.getResourceTemplate(application.getName(), group1.getName(), "jvmName", "ServerXMLTemplate.tpl", true);
        assertNotNull(response.getEntity());
    }

    @Test
    public void testUpdateResourceTemplate() {
        final String updateContent = "<server>updatedContent</server>";
        when(service.updateResourceTemplate(anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(updateContent);
        Response response = cut.updateResourceTemplate(application.getName(), "ServerXMLTemplate.tpl", updateContent, "jvmName", "groupName");
        assertNotNull(response.getEntity());

        when(service.updateResourceTemplate(anyString(), anyString(), anyString(), anyString(), anyString())).thenThrow(new ResourceTemplateUpdateException("jvmName", "server"));
        response = cut.updateResourceTemplate(application.getName(), "ServerXMLTemplate.tpl", updateContent, "jvmName", "groupName");
        assertNotNull(response.getEntity());
    }

    @Test
    public void testDeployConf() {
        CommandOutput mockExecData = mock(CommandOutput.class);
        when(service.deployConf(anyString(), anyString(), anyString(), anyString(), anyBoolean(), any(User.class))).thenReturn(mockExecData);
        when(mockExecData.getReturnCode()).thenReturn(new ExecReturnCode(0));
        Response response = cut.deployConf(application.getName(), group1.getName(), "jvmName", "ServerXMLTemplate.tpl", authenticatedUser);
        assertNotNull(response.getEntity());

        when(mockExecData.getReturnCode()).thenReturn(new ExecReturnCode(1));
        response = cut.deployConf(application.getName(), group1.getName(), "jvmName", "ServerXMLTemplate.tpl", authenticatedUser);
        assertNotNull(response.getEntity());

        when(service.deployConf(anyString(), anyString(), anyString(), anyString(), anyBoolean(), any(User.class))).thenThrow(new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE, "Target JVM must be stopped"));
        boolean exceptionThrown = false;
        try {
            cut.deployConf(application.getName(), group1.getName(), "jvmName", "ServerXMLTemplate.tpl", authenticatedUser);
        } catch (InternalErrorException ie) {
            exceptionThrown = true;
            assertEquals("Target JVM must be stopped", ie.getMessage());
        }
        assertTrue(exceptionThrown);

    }

    @Test
    public void testPreviewResourceTemplate() {
        when(service.previewResourceTemplate(anyString(), anyString(), anyString(), anyString())).thenReturn("preview content");
        Response response = cut.previewResourceTemplate(application.getName(), group1.getName(), "jvmName", "ServerXMLTemplate.tpl");
        assertNotNull(response.getEntity());

        when(service.previewResourceTemplate(anyString(), anyString(), anyString(), anyString())).thenThrow(new RuntimeException("Test fail preview"));
        response = cut.previewResourceTemplate(application.getName(), group1.getName(), "jvmName", "ServerXMLTemplate.tpl");
        assertNotNull(response.getEntity());
    }

    @Test
    public void testUploadConfigAppTemplate() throws IOException {
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
        applicationServiceRest.setMessageContext(msgContextMock);
        List<Application> appList = new ArrayList<>();
        appList.add(application);
        when(service.getApplications()).thenReturn(appList);

        final SecurityContext securityContextMock = mock(SecurityContext.class);
        final AuthenticatedUser authenticatedUser = new AuthenticatedUser(securityContextMock);

        applicationServiceRest.uploadConfigTemplate(application.getName(), authenticatedUser, "hct.xml", "jvm-1Test");
        verify(service).uploadAppTemplate(any(UploadAppTemplateRequest.class), any(User.class));
    }

    @Test (expected = InternalErrorException.class)
    public void testUploadConfigAppTemplateThrowsNoAppFoundException() throws IOException {
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
        applicationServiceRest.setMessageContext(msgContextMock);

        final SecurityContext securityContextMock = mock(SecurityContext.class);
        final AuthenticatedUser authenticatedUser = new AuthenticatedUser(securityContextMock);

        applicationServiceRest.uploadConfigTemplate("testAppName", authenticatedUser, "hct.xml", "jvm-1Test");
        verify(service).uploadAppTemplate(any(UploadAppTemplateRequest.class), any(User.class));
    }

    @Test
    public void testUploadConfigAppTemplateThrowsBadStreamException() {
        MessageContext mockContext = mock(MessageContext.class);
        HttpHeaders mockHttpHeaders = mock(HttpHeaders.class);
        HttpServletRequest mockHttpServletReq = mock(HttpServletRequest.class);
        List<MediaType> mediaTypeList = new ArrayList<>();
        mediaTypeList.add(MediaType.APPLICATION_JSON_TYPE);
        when(mockHttpHeaders.getAcceptableMediaTypes()).thenReturn(mediaTypeList);
        when(mockContext.getHttpHeaders()).thenReturn(mockHttpHeaders);
        when(mockContext.getHttpServletRequest()).thenReturn(mockHttpServletReq);
        applicationServiceRest.setMessageContext(mockContext);
        List<Application> appList = new ArrayList<>();
        appList.add(application);
        when(service.getApplications()).thenReturn(appList);
        try {
            Response response = applicationServiceRest.uploadConfigTemplate(application.getName(), authenticatedUser, "hct.xml", "jvm-1Test");
            assertNotNull(response.getEntity());
        } catch (Exception e) {
            assertEquals("Error receiving data", e.getMessage());
        }

    }

    @Test
    public void testDeployWar() {
        Application mockApplication = mock(Application.class);
        Group mockGroup = mock(Group.class);
        Set<Jvm> jvmSet = new HashSet<>();
        Jvm mockJvm = mock(Jvm.class);
        jvmSet.add(mockJvm);
        when(mockApplication.getName()).thenReturn("appName");
        when(mockApplication.getGroup()).thenReturn(mockGroup);
        when(mockGroup.getJvms()).thenReturn(jvmSet);
        when(mockJvm.getJvmName()).thenReturn("jvmName");
        when(mockJvm.getState()).thenReturn(JvmState.JVM_STOPPED);
        when(service.getApplication(any(Identifier.class))).thenReturn(mockApplication);

        applicationServiceRest.deployWebArchive(new Identifier<Application>(111L), authenticatedUser);

        verify(service).copyApplicationWarToGroupHosts(any(Application.class));
        verify(service).copyApplicationConfigToGroupJvms(any(Group.class), anyString(), any(User.class));
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
}
