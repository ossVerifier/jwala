package com.siemens.cto.aem.ws.rest.v1.service.app.impl;

import com.siemens.cto.aem.request.app.UpdateApplicationRequest;
import com.siemens.cto.aem.request.app.UploadWebArchiveRequest;
import com.siemens.cto.aem.common.exception.InternalErrorException;
import com.siemens.cto.aem.request.app.CreateApplicationRequest;
import com.siemens.cto.aem.domain.model.app.Application;
import com.siemens.cto.aem.exec.CommandOutput;
import com.siemens.cto.aem.exec.ExecReturnCode;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.user.User;
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
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import static com.siemens.cto.aem.domain.model.id.Identifier.id;
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
    private ApplicationServiceRestImpl cutImpl = new ApplicationServiceRestImpl(service = Mockito.mock(ApplicationService.class));


    private ApplicationServiceRest cut;

    Group group1 = new Group(Identifier.id(0L, Group.class), "");
    Application application = new Application(Identifier.id(1L, Application.class), "", "", "", group1, true, true, "testWar.war");
    Application applicationWithWar = new Application(Identifier.id(1L, Application.class), "", "D:\\APACHE\\TOMCAT\\WEBAPPS\\aem-webapp-1.0-SNAPSHOT-b6349ade-d8f2-4a2f-bdc5-d92d644a1a67-.war", "", group1, true, true, "testWar.war");
    Application newlyCreatedApp = new Application(Identifier.id(2L, Application.class), "", "", "", group1, true, true, "testWar.war");

    List<Application> applications = new ArrayList<>(1);
    List<Application> applications2 = new ArrayList<>(2);
    List<Application> emptyList = new ArrayList<>(0);

    @Before
    public void setUp() {
        cut = cutImpl;
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
        JsonUpdateApplication testJua = new JsonUpdateApplication(2L, "name", "/ctx", 1L, true, true);
        JsonCreateApplication testJca = new JsonCreateApplication(2L, "name", "/ctx", true, true);
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
                "Content-Disposition: form-data; name=\"files\"; filename=\"aem-webapp-1.0-SNAPSHOT.war\"" + ls +
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
                "Content-Disposition: form-data; name=\"files\"; filename=\"aem-webapp-1.0-SNAPSHOT.war\"" + ls +
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
                "Content-Disposition: form-data; name=\"files\"; filename=\"aem-webapp-1.0-SNAPSHOT.war\"" + ls +
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
        when(mockHsr.getCharacterEncoding()).thenReturn(charsetBin);
        when(mockHsr.getInputStream()).thenReturn(new MyIS(new ByteArrayInputStream(bbBuffer.array())));
        when(mockHsr.getContentType()).thenReturn(FileUploadBase.MULTIPART_FORM_DATA + ";boundary=" + boundary);


        Response resp = cut.uploadWebArchive(application.getId(), authenticatedUser);

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
        multiUpdate.add(new UpdateApplicationRequest(Identifier.id(0L, Application.class), Identifier.id(0L, Group.class), "", "", true, true));
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
        when(service.updateResourceTemplate(anyString(), anyString(), anyString())).thenReturn(updateContent);
        Response response = cut.updateResourceTemplate(application.getName(), "ServerXMLTemplate.tpl", updateContent);
        assertNotNull(response.getEntity());

        when(service.updateResourceTemplate(anyString(), anyString(), anyString())).thenThrow(new ResourceTemplateUpdateException("jvmName", "server"));
        response = cut.updateResourceTemplate(application.getName(), "ServerXMLTemplate.tpl", updateContent);
        assertNotNull(response.getEntity());
    }

    @Test
    public void testDeployConf() {
        CommandOutput mockExecData = mock(CommandOutput.class);
        when(service.deployConf(anyString(), anyString(), anyString(), anyString(), any(User.class))).thenReturn(mockExecData);
        when(mockExecData.getReturnCode()).thenReturn(new ExecReturnCode(0));
        Response response = cut.deployConf(application.getName(), group1.getName(), "jvmName", "ServerXMLTemplate.tpl", authenticatedUser);
        assertNotNull(response.getEntity());

        when(mockExecData.getReturnCode()).thenReturn(new ExecReturnCode(1));
        response = cut.deployConf(application.getName(), group1.getName(), "jvmName", "ServerXMLTemplate.tpl", authenticatedUser);
        assertNotNull(response.getEntity());

        when(service.deployConf(anyString(), anyString(), anyString(), anyString(), any(User.class))).thenThrow(new RuntimeException("Test fail deploy conf"));
        response = cut.deployConf(application.getName(), group1.getName(), "jvmName", "ServerXMLTemplate.tpl", authenticatedUser);
        assertNotNull(response.getEntity());
    }

    @Test
    public void testPreviewResourceTemplate(){
        when(service.previewResourceTemplate(anyString(), anyString(), anyString(), anyString())).thenReturn("preview content");
        Response response = cut.previewResourceTemplate(application.getName(), group1.getName(), "jvmName", "ServerXMLTemplate.tpl");
        assertNotNull(response.getEntity());

        when(service.previewResourceTemplate(anyString(), anyString(), anyString(), anyString())).thenThrow(new RuntimeException("Test fail preview"));
        response = cut.previewResourceTemplate(application.getName(), group1.getName(), "jvmName", "ServerXMLTemplate.tpl");
        assertNotNull(response.getEntity());
    }

}
