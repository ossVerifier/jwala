package com.cerner.jwala.ws.rest.v1.service.resource.impl;

import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.group.LiteGroup;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.resource.*;
import com.cerner.jwala.common.domain.model.user.User;
import com.cerner.jwala.common.exec.CommandOutput;
import com.cerner.jwala.common.exec.ExecReturnCode;
import com.cerner.jwala.common.request.resource.ResourceInstanceRequest;
import com.cerner.jwala.persistence.jpa.domain.resource.config.template.ConfigTemplate;
import com.cerner.jwala.persistence.jpa.service.exception.ResourceTemplateMetaDataUpdateException;
import com.cerner.jwala.service.group.GroupService;
import com.cerner.jwala.service.jvm.JvmService;
import com.cerner.jwala.service.resource.ResourceService;
import com.cerner.jwala.service.resource.impl.CreateResourceResponseWrapper;
import com.cerner.jwala.ws.rest.v1.provider.AuthenticatedUser;
import com.cerner.jwala.ws.rest.v1.response.ApplicationResponse;
import com.cerner.jwala.ws.rest.v1.service.resource.CreateResourceParam;
import com.cerner.jwala.ws.rest.v1.service.resource.ResourceHierarchyParam;
import junit.framework.Assert;
import org.apache.commons.io.IOUtils;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.activation.DataHandler;
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
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit test for {@link ResourceServiceRestImpl}.
 * <p/>
 * Created by z0033r5b on 9/29/2015.
 */
@RunWith(MockitoJUnitRunner.class)
public class ResourceServiceRestImplTest {
    @Mock
    private AuthenticatedUser authenticatedUser;
    @Mock
    private ResourceService impl;
    @Mock
    private GroupService groupService;
    @Mock
    private JvmService jvmService;
    @Mock
    private JsonResourceInstance jsonResourceInstance;
    private ResourceServiceRestImpl cut;
    private Group group;

    @Before
    public void setUp() {
        group = new Group(new Identifier<Group>(1L), "theGroup");
        cut = new ResourceServiceRestImpl(impl);
        when(authenticatedUser.getUser()).thenReturn(new User("Unused"));
        when(jsonResourceInstance.getCommand()).thenReturn(new ResourceInstanceRequest("resourceType", "resourceName", group.getName(), new HashMap<String, String>()));
    }

    @Test
    public void testCreateTemplate() throws IOException {
        List<Attachment> attachmentList = new ArrayList<>();
        Attachment json = mock(Attachment.class);
        Attachment tpl = mock(Attachment.class);
        attachmentList.add(json);
        attachmentList.add(tpl);
        DataHandler jsonDataHandler = mock(DataHandler.class);
        DataHandler tplDataHandler = mock(DataHandler.class);
        when(json.getDataHandler()).thenReturn(jsonDataHandler);
        when(tpl.getDataHandler()).thenReturn(tplDataHandler);
        when(jsonDataHandler.getName()).thenReturn("test-target.json");
        when(tplDataHandler.getName()).thenReturn("test-target.tpl");
        String jsonContent = "{}";
        when(jsonDataHandler.getInputStream()).thenReturn(new ByteArrayInputStream(jsonContent.getBytes()));
        String tplContent = "template content";
        when(tplDataHandler.getInputStream()).thenReturn(new ByteArrayInputStream(tplContent.getBytes()));

        when(impl.createTemplate(any(InputStream.class), any(InputStream.class), anyString(), any(User.class))).thenReturn(new CreateResourceResponseWrapper(new ConfigTemplate()));
        Response response = cut.createTemplate(attachmentList, "test-target-name", authenticatedUser);

        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
    }

    @Test
    public void testCreateTemplateThrowsIOException() throws IOException {
        List<Attachment> attachmentList = new ArrayList<>();
        Attachment json = mock(Attachment.class);
        Attachment tpl = mock(Attachment.class);
        attachmentList.add(json);
        attachmentList.add(tpl);
        DataHandler jsonDataHandler = mock(DataHandler.class);
        DataHandler tplDataHandler = mock(DataHandler.class);
        when(json.getDataHandler()).thenReturn(jsonDataHandler);
        when(tpl.getDataHandler()).thenReturn(tplDataHandler);
        when(jsonDataHandler.getName()).thenReturn("test-target.json");
        when(tplDataHandler.getName()).thenReturn("test-target.tpl");
        when(jsonDataHandler.getInputStream()).thenThrow(new IOException());
        when(tplDataHandler.getInputStream()).thenThrow(new IOException());

        when(impl.createTemplate(any(InputStream.class), any(InputStream.class), anyString(), any(User.class))).thenReturn(new CreateResourceResponseWrapper(new ConfigTemplate()));
        Response response = cut.createTemplate(attachmentList, "test-target-name", authenticatedUser);

        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
    }

    @Test
    public void testCreateTemplateHasIncorrectNumberOfAttachments() throws IOException {
        List<Attachment> attachmentList = new ArrayList<>();
        Attachment json = mock(Attachment.class);
        attachmentList.add(json);
        DataHandler jsonDataHandler = mock(DataHandler.class);
        when(json.getDataHandler()).thenReturn(jsonDataHandler);
        when(jsonDataHandler.getName()).thenReturn("test-target.json");

        when(impl.createTemplate(any(InputStream.class), any(InputStream.class), anyString(), any(User.class))).thenReturn(new CreateResourceResponseWrapper(new ConfigTemplate()));
        Response response = cut.createTemplate(attachmentList, "test-target-name", authenticatedUser);

        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
    }

    @Test
    public void testCreateResource() throws IOException {
        final CreateResourceParam createResourceParam = new CreateResourceParam();
        createResourceParam.setWebServer("someWebServer");

        // Deploy path
        final DataHandler deployPathDataHandler = mock(DataHandler.class);
        when(deployPathDataHandler.getName()).thenReturn("deployPath");
        when(deployPathDataHandler.getInputStream()).thenReturn(IOUtils.toInputStream("c:/tmp"));

        final Attachment deployPathAttachment = mock(Attachment.class);
        when(deployPathAttachment.getDataHandler()).thenReturn(deployPathDataHandler);

        // Content type
        final DataHandler contentTypeDataHandler = mock(DataHandler.class);
        when(contentTypeDataHandler.getName()).thenReturn("contentType");
        when(contentTypeDataHandler.getInputStream()).thenReturn(IOUtils.toInputStream("text/plain"));

        final Attachment contentTypeAttachment = mock(Attachment.class);
        when(contentTypeAttachment.getDataHandler()).thenReturn(contentTypeDataHandler);

        // File attachment
        final DataHandler fileAttachmentDataHandler = mock(DataHandler.class);
        when(fileAttachmentDataHandler.getName()).thenReturn("sample-resource.tpl");
        when(fileAttachmentDataHandler.getInputStream()).thenReturn(this.getClass().getClassLoader().getResourceAsStream("sample-resource.tpl"));

        final Attachment fileAttachment = mock(Attachment.class);
        when(fileAttachment.getDataHandler()).thenReturn(fileAttachmentDataHandler);
        when(fileAttachment.getHeader(eq("Content-Type"))).thenReturn("application/octet-stream");

        final List<Attachment> attachmentList = new ArrayList<>();
        attachmentList.add(fileAttachment);
        attachmentList.add(deployPathAttachment);
        attachmentList.add(contentTypeAttachment);

        final Response response = cut.createResource("httpd.conf", createResourceParam, attachmentList);
        assertEquals("0", ((ApplicationResponse) response.getEntity()).getMsgCode());
    }

    @Test
    public void testCreateResourceWithMissingAttachment() throws IOException {
        final CreateResourceParam createResourceParam = new CreateResourceParam();
        createResourceParam.setGroup("Group A");
        createResourceParam.setWebServer("Webserver 1");

        // File attachment
        final DataHandler fileAttachmentDataHandler = mock(DataHandler.class);
        when(fileAttachmentDataHandler.getName()).thenReturn("sample-resource.tpl");
        when(fileAttachmentDataHandler.getInputStream()).thenReturn(this.getClass().getClassLoader().getResourceAsStream("sample-resource.tpl"));

        final Attachment fileAttachment = mock(Attachment.class);
        when(fileAttachment.getDataHandler()).thenReturn(fileAttachmentDataHandler);
        when(fileAttachment.getHeader(eq("Content-Type"))).thenReturn("application/octet-stream");

        final List<Attachment> attachmentList = new ArrayList<>();
        attachmentList.add(fileAttachment);

        final Response response = cut.createResource("httpd.conf", createResourceParam, attachmentList);
        assertEquals("AEM61", ((ApplicationResponse) response.getEntity()).getMsgCode());
    }

    @Test
    public void testCreateResourceWithIOException() throws IOException {
        // Deploy path
        final DataHandler deployPathDataHandler = mock(DataHandler.class);
        when(deployPathDataHandler.getName()).thenReturn("deployPath");
        when(deployPathDataHandler.getInputStream()).thenReturn(IOUtils.toInputStream("c:/tmp"));

        final Attachment deployPathAttachment = mock(Attachment.class);
        when(deployPathAttachment.getDataHandler()).thenReturn(deployPathDataHandler);

        // Content type
        final DataHandler contentTypeDataHandler = mock(DataHandler.class);
        when(contentTypeDataHandler.getName()).thenReturn("contentType");
        when(contentTypeDataHandler.getInputStream()).thenReturn(IOUtils.toInputStream("text/plain"));

        final Attachment contentTypeAttachment = mock(Attachment.class);
        when(contentTypeAttachment.getDataHandler()).thenReturn(contentTypeDataHandler);

        // File attachment
        final DataHandler fileAttachmentDataHandler = mock(DataHandler.class);
        when(fileAttachmentDataHandler.getName()).thenReturn("sample-resource.tpl");
        when(fileAttachmentDataHandler.getInputStream()).thenReturn(this.getClass().getClassLoader().getResourceAsStream("sample-resource.tpl"));

        final Attachment fileAttachment = mock(Attachment.class);
        when(fileAttachment.getDataHandler()).thenReturn(fileAttachmentDataHandler);
        when(fileAttachment.getHeader(eq("Content-Type"))).thenReturn("application/octet-stream");

        final List<Attachment> attachmentList = new ArrayList<>();
        attachmentList.add(fileAttachment);
        attachmentList.add(deployPathAttachment);
        attachmentList.add(contentTypeAttachment);

        when(impl.getMetaData(anyString())).thenThrow(new IOException());

        final Response response = cut.createResource("httpd.conf", null, attachmentList);
        assertEquals("AEM60", ((ApplicationResponse) response.getEntity()).getMsgCode());
    }

    @Test
    public void testGetResourceAttributeData() {
        when(impl.generateResourceGroup()).thenReturn(new ResourceGroup());
        Response response = cut.getResourceAttrData();
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        response = cut.getResourceTopology();
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    public void testCheckFileExists() {
        when(impl.checkFileExists(anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(new HashMap<String, String>());
        Response response = cut.checkFileExists("test", "test", null, null, "test");
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    public void testDeleteGroupLevelAppResource() {
        final ResourceHierarchyParam resourceHierarchyParam = new ResourceHierarchyParam();
        resourceHierarchyParam.setGroup("someGroup");
        resourceHierarchyParam.setWebApp("someApp");
        cut.deleteResource("someResource", resourceHierarchyParam, authenticatedUser);
        verify(impl).deleteGroupLevelAppResource(anyString(), anyString());
    }

    @Test
    public void testDeleteAppResource() {
        final ResourceHierarchyParam resourceHierarchyParam = new ResourceHierarchyParam();
        resourceHierarchyParam.setJvm("someJvm");
        resourceHierarchyParam.setWebApp("someApp");
        cut.deleteResource("someResource", resourceHierarchyParam, authenticatedUser);
        verify(impl).deleteAppResource(eq("someResource"), eq("someApp"), eq("someJvm"));
    }

    @Test
    public void testDeleteGroupLevelWebServerResource() {
        final ResourceHierarchyParam resourceHierarchyParam = new ResourceHierarchyParam();
        resourceHierarchyParam.setGroup("someGroup");
        resourceHierarchyParam.setWebServer("*");
        cut.deleteResource("someResource", resourceHierarchyParam, authenticatedUser);
        verify(impl).deleteGroupLevelWebServerResource(eq("someResource"), eq("someGroup"));
    }

    @Test
    public void testDeleteWebServerResource() {
        final ResourceHierarchyParam resourceHierarchyParam = new ResourceHierarchyParam();
        resourceHierarchyParam.setWebServer("someWebServer");
        cut.deleteResource("someResource", resourceHierarchyParam, authenticatedUser);
        verify(impl).deleteWebServerResource(eq("someResource"), eq("someWebServer"));
    }

    @Test
    public void testDeleteGroupLevelJvmResource() {
        final ResourceHierarchyParam resourceHierarchyParam = new ResourceHierarchyParam();
        resourceHierarchyParam.setGroup("someGroup");
        resourceHierarchyParam.setJvm("*");
        cut.deleteResource("someResource", resourceHierarchyParam, authenticatedUser);
        verify(impl).deleteGroupLevelJvmResource(eq("someResource"), eq("someGroup"));
    }

    @Test
    public void testDeleteJvmResource() {
        final ResourceHierarchyParam resourceHierarchyParam = new ResourceHierarchyParam();
        resourceHierarchyParam.setJvm("someJvm");
        cut.deleteResource("someResource", resourceHierarchyParam, authenticatedUser);
        verify(impl).deleteJvmResource(eq("someResource"), eq("someJvm"));
    }

    @Test
    public void testDeleteJvmResourceNoParamsSpecified() {
        final ResourceHierarchyParam resourceHierarchyParam = new ResourceHierarchyParam();
        final Response response = cut.deleteResource("someResource", resourceHierarchyParam, authenticatedUser);
        assertEquals("AEM64", ((ApplicationResponse) response.getEntity()).getMsgCode());
    }

    @Test
    public void testGetResourceContent() {
        ResourceHierarchyParam param = new ResourceHierarchyParam();
        param.setGroup("test-group");
        param.setJvm("test-jvm");
        param.setWebApp("test-app");
        param.setWebServer("test-webserver");

        when(impl.getResourceContent(any(ResourceIdentifier.class))).thenReturn(new ResourceContent("{}", "key=value"));

        Response response = cut.getResourceContent("external.properties", param);
        assertEquals(200, response.getStatus());
    }

    @Test
    public void testGetResourceContentReturnsNull() {
        ResourceHierarchyParam param = new ResourceHierarchyParam();
        param.setGroup("test-group");
        param.setJvm("test-jvm");
        param.setWebApp("test-app");
        param.setWebServer("test-webserver");

        when(impl.getResourceContent(any(ResourceIdentifier.class))).thenReturn(null);

        Response response = cut.getResourceContent("external.properties", param);
        assertEquals(204, response.getStatus());
    }

    @Test
    public void testUpdateResourceContent() {
        ResourceHierarchyParam param = new ResourceHierarchyParam();
        param.setGroup("test-group");
        param.setJvm("test-jvm");
        param.setWebApp("test-app");
        param.setWebServer("test-webserver");

        when(impl.updateResourceContent(any(ResourceIdentifier.class), anyString())).thenReturn("newkey=newvalue");

        Response response = cut.updateResourceContent("external.properties", param, "newkey=newvalue");
        assertEquals(200, response.getStatus());
    }

    @Test
    public void testGetExternalProperties() {
        cut.getExternalProperties();
        verify(impl).getExternalProperties();
    }

    @Test
    public void testGetExternalPropertiesView() {
        when(impl.getExternalPropertiesAsString()).thenReturn("key=value");
        Response result = cut.getExternalPropertiesView();
        verify(impl).getExternalPropertiesAsString();
        assertEquals(200, result.getStatus());
        assertEquals("key=value", result.getEntity().toString());
    }

    @Test
    public void testPreviewResourceContent() {
        ResourceHierarchyParam param = new ResourceHierarchyParam();
        param.setGroup("test-group");
        param.setJvm("test-jvm");
        param.setWebApp("test-app");
        param.setWebServer("test-webserver");
        when(impl.previewResourceContent(any(ResourceIdentifier.class), anyString())).thenReturn("key=value");
        Response result = cut.previewResourceContent("myFile", param, "key=value");
        assertEquals(200, result.getStatus());
    }

    @Test
    public void testGetResourceFileNames() {
        // test file is uploaded
        final List<String> names = new ArrayList<>();
        names.add("external.properties");
        ResourceHierarchyParam param = new ResourceHierarchyParam();

        when(impl.getResourceNames(any(ResourceIdentifier.class))).thenReturn(names);

        Response result = cut.getResourcesFileNames(param);

        assertEquals(200, result.getStatus());
        verify(impl).getResourceNames(any(ResourceIdentifier.class));
        ApplicationResponse entity = (ApplicationResponse) result.getEntity();
        List<String> fileList = (List<String>) entity.getApplicationResponseContent();
        assertTrue(!fileList.isEmpty());

        // test file is not uploaded
        reset(impl);
        when(impl.getResourceNames(any(ResourceIdentifier.class))).thenReturn(new ArrayList<String>());

        result = cut.getResourcesFileNames(param);

        assertEquals(200, result.getStatus());
        verify(impl).getResourceNames(any(ResourceIdentifier.class));
        entity = (ApplicationResponse) result.getEntity();
        fileList = (List<String>) entity.getApplicationResponseContent();
        assertTrue(fileList.isEmpty());
    }

    @Test
    public void testDeleteExternalProperties() {
        String[] externalPropertiesArray = new String[]{"external.properties"};
        ResourceHierarchyParam param = new ResourceHierarchyParam();

        when(impl.deleteExternalProperties()).thenReturn(1);

        Response result = cut.deleteResources(externalPropertiesArray, param, authenticatedUser);
        Assert.assertEquals(200, result.getStatus());
        verify(impl).deleteExternalProperties();
        ApplicationResponse entity = (ApplicationResponse) result.getEntity();
        int recCount = (int) entity.getApplicationResponseContent();
        Assert.assertEquals(1, recCount);
    }

    @Test
    public void testExternalPropertiesSetAfterInitialization() throws Exception {
        final List<String> names = new ArrayList<>();
        names.add("external.properties");
        when(impl.getResourceNames(any(ResourceIdentifier.class))).thenReturn(names);
        when(impl.getResourceContent(any(ResourceIdentifier.class))).thenReturn(new ResourceContent("{}", "key=value"));

        cut.afterPropertiesSet();

        verify(impl).getResourceNames(any(ResourceIdentifier.class));
        verify(impl).getResourceContent(any(ResourceIdentifier.class));
    }

    @Test
    public void testDeployTemplate() {
        AuthenticatedUser mockAuthUser = mock(AuthenticatedUser.class);
        when(mockAuthUser.getUser()).thenReturn(new User("test-user"));

        ResourceHierarchyParam param = new ResourceHierarchyParam();

        when(impl.deployTemplateToHost(anyString(), anyString(), any(ResourceIdentifier.class))).thenReturn(new CommandOutput(new ExecReturnCode(0), "SUCCESS", ""));
        Response result = cut.deployTemplateToHost("external.properties", "test-host", param, mockAuthUser);
        assertEquals(200, result.getStatus());

        when(impl.deployTemplateToHost(anyString(), anyString(), any(ResourceIdentifier.class))).thenReturn(new CommandOutput(new ExecReturnCode(1), "", "FAILED"));
        result = cut.deployTemplateToHost("external.properties", "test-host", param, mockAuthUser);
        assertEquals(500, result.getStatus());
    }

    @Test
    public void testDeployTemplateToAllHosts() {
        AuthenticatedUser mockAuthUser = mock(AuthenticatedUser.class);
        ResourceHierarchyParam param = new ResourceHierarchyParam();

        when(mockAuthUser.getUser()).thenReturn(new User("test-user"));

        Response result = cut.deployTemplateToAllHosts("external.properties", param, mockAuthUser);
        assertEquals(200, result.getStatus());
    }

    @Test
    public void testGetExternalPropertiesAsFile() throws IOException {
        when(impl.getExternalPropertiesAsFile()).thenReturn(new File("./src/test/resources/vars.properties"));
        Response result = cut.getExternalPropertiesDownload();
        assertEquals(200, result.getStatus());
    }

    @Test
    public void testGetExternalPropertiesAsFileFails() throws IOException {
        when(impl.getExternalPropertiesAsFile()).thenThrow(new IOException("Fail this test"));
        Response result = cut.getExternalPropertiesDownload();
        assertEquals(500, result.getStatus());
    }

    @Test
    public void testUploadExternalProperties() throws IOException {
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
        cut.setMessageContext(msgContextMock);

        final SecurityContext securityContextMock = mock(SecurityContext.class);
        final AuthenticatedUser authenticatedUser = new AuthenticatedUser(securityContextMock);

        cut.uploadExternalProperties(authenticatedUser);
        verify(impl).createResource(any(ResourceIdentifier.class), any(ResourceTemplateMetaData.class), any(InputStream.class));
    }

    @Test
    public void testUpdateResourceMetaData() {
        final String updatedMetadata = "{\"updated\":\"meta-data\"}";
        ResourceHierarchyParam resourceHierarchyParam = new ResourceHierarchyParam();
        when(impl.updateResourceMetaData(any(ResourceIdentifier.class), anyString(), anyString())).thenReturn(updatedMetadata);

        Response response = cut.updateResourceMetaData("test-resource.txt", resourceHierarchyParam, updatedMetadata);

        assertEquals(200, response.getStatus());
        verify(impl).updateResourceMetaData(any(ResourceIdentifier.class), eq("test-resource.txt"), eq(updatedMetadata));
    }

    @Test
    public void testUpdateResourceMetaDataFails() {
        final String updatedMetadata = "{\"updated\":\"meta-data\"}";
        final String resourceName = "test-resource.txt";
        ResourceHierarchyParam resourceHierarchyParam = new ResourceHierarchyParam();
        when(impl.updateResourceMetaData(any(ResourceIdentifier.class), anyString(), anyString())).thenThrow(new ResourceTemplateMetaDataUpdateException("failed-entity", resourceName));

        Response response = cut.updateResourceMetaData(resourceName, resourceHierarchyParam, updatedMetadata);

        assertEquals(500, response.getStatus());
        assertEquals("test-resource.txt of failed-entity meta data update failed!", ((ApplicationResponse)response.getEntity()).getApplicationResponseContent());
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
