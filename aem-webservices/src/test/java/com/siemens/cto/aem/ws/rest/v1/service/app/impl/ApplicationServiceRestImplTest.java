package com.siemens.cto.aem.ws.rest.v1.service.app.impl;

import static com.siemens.cto.aem.domain.model.id.Identifier.id;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.fileupload.FileUploadBase;
import org.apache.cxf.common.util.Base64Utility;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.util.Assert;

import com.siemens.cto.aem.common.exception.InternalErrorException;
import com.siemens.cto.aem.domain.model.app.Application;
import com.siemens.cto.aem.domain.model.app.UpdateApplicationCommand;
import com.siemens.cto.aem.domain.model.app.UploadWebArchiveCommand;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.temporary.PaginationParameter;
import com.siemens.cto.aem.domain.model.temporary.User;
import com.siemens.cto.aem.service.app.ApplicationService;
import com.siemens.cto.aem.ws.rest.v1.provider.PaginationParamProvider;
import com.siemens.cto.aem.ws.rest.v1.response.ApplicationResponse;
import com.siemens.cto.aem.ws.rest.v1.service.app.ApplicationServiceRest;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationServiceRestImplTest {
    
    @Mock private MessageContext mockMc;
    @Mock private HttpServletRequest mockHsr;    
    @Mock private HttpHeaders mockHh;
    /*NoMock*/ private ApplicationService service;
    @InjectMocks @Spy
    private ApplicationServiceRestImpl cutImpl = new ApplicationServiceRestImpl(service = Mockito.mock(ApplicationService.class));
    

    private ApplicationServiceRest cut;

    Group group1 = new Group(Identifier.id(0L, Group.class), "");
    Application application = new Application(Identifier.id(1L, Application.class), "","","", group1);
    Application applicationWithWar = new Application(Identifier.id(1L, Application.class), "","D:\\APACHE\\TOMCAT\\WEBAPPS\\aem-webapp-1.0-SNAPSHOT-b6349ade-d8f2-4a2f-bdc5-d92d644a1a67-.war","", group1); 
    Application newlyCreatedApp = new Application(Identifier.id(2L, Application.class), "","","", group1);
    
    List<Application> applications = new ArrayList<Application>(1);
    List<Application> applications2 = new ArrayList<Application>(2);
    List<Application> emptyList = new ArrayList<Application>(0);
    PaginationParamProvider allProvider = new PaginationParamProvider(null,null, "true");

    @Before
    public void setUp() {
        // cut = new ApplicationServiceRestImpl(service);
        cut = cutImpl;
        applications.add(application);

        applications2.add(application);
        applications2.add(newlyCreatedApp);

        List<MediaType> mtOk =new ArrayList<MediaType>();
        mtOk.add(MediaType.APPLICATION_JSON_TYPE);
        when(mockHh.getAcceptableMediaTypes()).thenReturn(mtOk);
        when(mockMc.getHttpHeaders()).thenReturn(mockHh);
        when(mockMc.getHttpServletRequest()).thenReturn(mockHsr);        
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

    private class IsValidUploadCommand extends ArgumentMatcher<UploadWebArchiveCommand> {

        @Override
        public boolean matches(Object arg) {
            UploadWebArchiveCommand uwac = (UploadWebArchiveCommand)arg;
            uwac.validateCommand();
            return true;
        } 
        
    }    
    
    @Test
    public void testUploadWebArchive() throws IOException {
                
        when(service.uploadWebArchive(argThat(new IsValidUploadCommand()), any(User.class))).thenReturn(applicationWithWar);
        // ISO8859-1
        String ls = System.lineSeparator();
        String boundary= "--WebKitFormBoundarywBZFyEeqG5xW80nx";
        
        @SuppressWarnings("unused")
        String http = "Accept:text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8" + ls +
                "Cache-Control:no-cache"+ ls +
                "Content-Type:multipart/form-data; boundary=--"+boundary+ ls +
                "Origin:null"+ ls +
                "Pragma:no-cache"+ ls +
                "Referer:"+ ls +
                "User-Agent:Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/35.0.1916.114 Safari/537.36" + ls;              

        String dataText = "abcdef";
        String contentText = "--" + boundary + ls + 
                "Content-Disposition: form-data; name=\"files\"; filename=\"aem-webapp-1.0-SNAPSHOT.war\"" + ls +
                "Content-Type: text/plain" + ls + ls +
                dataText + ls +
                "--" + boundary+"--" + ls;
       
        String charsetText = "UTF-8";
        ByteBuffer bbBuffer = Charset.forName(charsetText).encode(contentText);
        when(mockHsr.getCharacterEncoding()).thenReturn(charsetText);
        // ByteBuffer bbBoundary = Charset.forName("ISO-8859-1").encode(boundary);
        // when(mockHsr.getCharacterEncoding()).thenReturn(charsetBin);
        when(mockHsr.getInputStream()).thenReturn(new MyIS(new ByteArrayInputStream(bbBuffer.array())));
        when(mockHsr.getContentType()).thenReturn(FileUploadBase.MULTIPART_FORM_DATA+";boundary=" + boundary);
        
        
        Response resp = cut.uploadWebArchive(application.getId());
         
        Application result = getApplicationFromResponse(resp);
        
        assertEquals(Status.CREATED.getStatusCode(), resp.getStatus());

        Assert.hasText(result.getWarPath());
    }

    @Test
    public void testUploadWebArchiveBinary() throws IOException {
                
        when(service.uploadWebArchive(argThat(new IsValidUploadCommand()), any(User.class))).thenReturn(applicationWithWar);
        // ISO8859-1
        String ls = System.lineSeparator();
        String boundary= "--WebKitFormBoundarywBZFyEeqG5xW80nx";
        
        ByteBuffer file = ByteBuffer.allocate(4);
        file.asShortBuffer().put((short)0xc0de);        
        String data = Base64Utility.encode(file.array());

        @SuppressWarnings("unused")
        String http = "Accept:text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8" + ls +
                "Cache-Control:no-cache"+ ls +
                "Content-Type:multipart/form-data; boundary=--"+boundary+ ls +
                "Origin:null"+ ls +
                "Pragma:no-cache"+ ls +
                "Referer:"+ ls +
                "User-Agent:Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/35.0.1916.114 Safari/537.36" + ls;
                
        String content = "--" + boundary + ls + 
                "Content-Disposition: form-data; name=\"files\"; filename=\"aem-webapp-1.0-SNAPSHOT.war\"" + ls +
                "Content-Type: application/octet-stream" + ls + ls +
                data + ls +
                "--" + boundary+"--" + ls;
        
        String charsetBin = "ISO-8859-1";
        ByteBuffer bbBuffer = Charset.forName(charsetBin).encode(content);
        when(mockHsr.getCharacterEncoding()).thenReturn(charsetBin);
        when(mockHsr.getInputStream()).thenReturn(new MyIS(new ByteArrayInputStream(bbBuffer.array())));
        when(mockHsr.getContentType()).thenReturn(FileUploadBase.MULTIPART_FORM_DATA+";boundary=" + boundary);
        
        
        Response resp = cut.uploadWebArchive(application.getId());
         
        Application result = getApplicationFromResponse(resp);
        
        assertEquals(Status.CREATED.getStatusCode(), resp.getStatus());

        Assert.hasText(result.getWarPath());
    }

    @Test(expected = InternalErrorException.class)
    public void testUploadWebArchiveBadStream() throws IOException {
                
        when(service.uploadWebArchive(argThat(new IsValidUploadCommand()), any(User.class))).thenReturn(applicationWithWar);
        // ISO8859-1
        String ls = System.lineSeparator();
        String boundary= "--WebKitFormBoundarywBZFyEeqG5xW80nx";
        
        ByteBuffer file = ByteBuffer.allocate(4);
        file.asShortBuffer().put((short)0xc0de);        
        String data = Base64Utility.encode(file.array());

        @SuppressWarnings("unused")
        String http = "Accept:text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8" + ls +
                "Cache-Control:no-cache"+ ls +
                "Content-Type:multipart/form-data; boundary=--"+boundary+ ls +
                "Origin:null"+ ls +
                "Pragma:no-cache"+ ls +
                "Referer:"+ ls +
                "User-Agent:Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/35.0.1916.114 Safari/537.36" + ls;
                
        String content = "--" + boundary + ls + 
                "Content-Disposition: form-data; name=\"files\"; filename=\"aem-webapp-1.0-SNAPSHOT.war\"" + ls +
                "Content-Type: application/octet-stream" + ls + ls +
                data + ls +
                /*"--" + bad stream!*/ boundary+"--" + ls;
        
        String charsetBin = "ISO-8859-1";
        ByteBuffer bbBuffer = Charset.forName(charsetBin).encode(content);
        when(mockHsr.getCharacterEncoding()).thenReturn(charsetBin);
        when(mockHsr.getInputStream()).thenReturn(new MyIS(new ByteArrayInputStream(bbBuffer.array())));
        when(mockHsr.getContentType()).thenReturn(FileUploadBase.MULTIPART_FORM_DATA+";boundary=" + boundary);

        cut.uploadWebArchive(application.getId());
    }

    @Test
    public void testUploadWebArchiveNoContent() throws IOException {
                
        verify(service, never()).uploadWebArchive(argThat(new IsValidUploadCommand()), any(User.class));
        
        // ISO8859-1
        String boundary= "--WebKitFormBoundarywBZFyEeqG5xW80nx";
                
        String content = "";
        
        String charsetBin = "ISO-8859-1";
        ByteBuffer bbBuffer = Charset.forName(charsetBin).encode(content);
        when(mockHsr.getCharacterEncoding()).thenReturn(charsetBin);
        when(mockHsr.getInputStream()).thenReturn(new MyIS(new ByteArrayInputStream(bbBuffer.array())));
        when(mockHsr.getContentType()).thenReturn(FileUploadBase.MULTIPART_FORM_DATA+";boundary=" + boundary);
        
        
        Response resp = cut.uploadWebArchive(application.getId());  
        
        assertEquals(Status.NO_CONTENT.getStatusCode(), resp.getStatus());
    }

    @Test
    public void testDeleteWebArchive() throws IOException {
        when(service.getApplication(Matchers.eq(id(1L, Application.class)))).thenReturn(applicationWithWar);
        when(service.deleteWebArchive(Matchers.eq(id(1L, Application.class)), any(User.class))).thenReturn(application);
        
        Response currentResponse = cut.getApplication(id(1L, Application.class));
        Application current = getApplicationFromResponse(currentResponse);
        assertEquals(applicationWithWar, current);
        
        Response updatedResponse = cut.deleteWebArchive(id(1L, Application.class));
        Application updated = getApplicationFromResponse(updatedResponse);
        assertEquals(application, updated);
    }

    @Test
    public void testGetApplications() {
        when(service.getApplications(any(PaginationParameter.class))).thenReturn(applications);
        
        Response resp = cut.getApplications(null, allProvider);
        List<Application> result = getApplicationsFromResponse(resp);
        
        assertEquals(applications, result);
    }
    
    @SuppressWarnings("unchecked")
    private List<Application> getApplicationsFromResponse(Response resp) {
        assertNotNull(resp.getEntity());
        ApplicationResponse appResponse = (ApplicationResponse)resp.getEntity();
        Object entity = appResponse.getApplicationResponseContent();
        assertTrue(entity instanceof List<?>);
        
        return (List<Application>) entity;
    }

    private Application getApplicationFromResponse(Response resp) {
        assertNotNull(resp.getEntity());
        ApplicationResponse appResponse = (ApplicationResponse)resp.getEntity();
        Object entity = appResponse.getApplicationResponseContent();
        assertNotNull(entity);
        assertTrue(entity instanceof Application);
        
        return (Application)entity;
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
        when(service.findApplications(any(Identifier.class), any(PaginationParameter.class))).thenReturn(emptyList);
        Response resp = cut.getApplications(id(2L, Group.class), allProvider);
        
        List<Application> result = getApplicationsFromResponse(resp);
        
        assertEquals(emptyList, result);
    }

    @Test
    public void testFindApplicationsByJvmId() {
        when(service.findApplicationsByJvmId(Matchers.eq(id(2L, Jvm.class)), any(PaginationParameter.class))).thenReturn(applications);
        Response resp = cut.findApplicationsByJvmId(id(2L, Jvm.class), allProvider);
        List<Application> result = getApplicationsFromResponse(resp);
        
        assertEquals(applications, result);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testFindApplicationsByNullJvmId() {
        when(service.findApplicationsByJvmId(any(Identifier.class), any(PaginationParameter.class))).thenReturn(emptyList);
        Response resp = cut.findApplicationsByJvmId(null, allProvider);
        List<Application> result = getApplicationsFromResponse(resp);
        
        assertEquals(emptyList, result);        
    }
    
    /**
     *  Testing: {@link com.siemens.cto.aem.ws.rest.v1.service.app.impl.ApplicationServiceRestImpl#createApplication(JsonCreateApplication)}
     */
    @Test
    public void testCreate() {
        when(service.createApplication(any(com.siemens.cto.aem.domain.model.app.CreateApplicationCommand.class), any(User.class))).thenReturn(newlyCreatedApp);

        JsonCreateApplication jsonCreateAppRequest = new JsonCreateApplication();        

        Response resp = cut.createApplication(jsonCreateAppRequest);
        assertNotNull(resp.getEntity());
        ApplicationResponse appResponse = (ApplicationResponse)resp.getEntity();
        Object entity = appResponse.getApplicationResponseContent();
        assertEquals(this.newlyCreatedApp, entity);
        assertEquals(Status.CREATED.getStatusCode(), resp.getStatus());
    }
    
    /**
     *  Testing: {@link com.siemens.cto.aem.ws.rest.v1.service.app.impl.ApplicationServiceRestImpl#updateApplication(JsonUpdateApplication)}
     */
    @Test
    public void testUpdate() {
        when(service.updateApplication(any(com.siemens.cto.aem.domain.model.app.UpdateApplicationCommand.class), any(User.class))).thenReturn(newlyCreatedApp);
        ArrayList<UpdateApplicationCommand> multiUpdate = new ArrayList<>();
        multiUpdate.add(new UpdateApplicationCommand(Identifier.id(0L, Application.class), Identifier.id(0L, Group.class), "", ""));
        JsonUpdateApplication jsonUpdateAppRequest = new JsonUpdateApplication();
        Response resp = cut.updateApplication(jsonUpdateAppRequest);
        assertNotNull(resp.getEntity());
        ApplicationResponse appResponse = (ApplicationResponse)resp.getEntity();
        Object entity = appResponse.getApplicationResponseContent();
        assertEquals(this.newlyCreatedApp, entity);        
    }
   
    /**
     *  Testing: {@link com.siemens.cto.aem.ws.rest.v1.service.app.impl.ApplicationServiceRestImpl#removeApplication(Identifier)}
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testRemove() {
        Response resp = cut.removeApplication(application.getId());
        Mockito.verify(service, Mockito.times(1)).removeApplication(any(Identifier.class),  any(User.class));
        assertNull(resp.getEntity());
        assertEquals(Status.OK.getStatusCode(), resp.getStatus());
    }
}
