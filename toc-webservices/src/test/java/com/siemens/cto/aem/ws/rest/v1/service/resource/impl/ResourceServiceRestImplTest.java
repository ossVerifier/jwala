package com.siemens.cto.aem.ws.rest.v1.service.resource.impl;

import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.common.domain.model.group.LiteGroup;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.resource.ResourceGroup;
import com.siemens.cto.aem.common.domain.model.resource.ResourceInstance;
import com.siemens.cto.aem.common.domain.model.resource.ResourceTemplateMetaData;
import com.siemens.cto.aem.common.domain.model.user.User;
import com.siemens.cto.aem.common.request.resource.ResourceInstanceRequest;
import com.siemens.cto.aem.persistence.jpa.domain.resource.config.template.ConfigTemplate;
import com.siemens.cto.aem.service.group.GroupService;
import com.siemens.cto.aem.service.jvm.JvmService;
import com.siemens.cto.aem.service.resource.ResourceService;
import com.siemens.cto.aem.service.resource.impl.CreateResourceTemplateApplicationResponseWrapper;
import com.siemens.cto.aem.ws.rest.v1.provider.AuthenticatedUser;
import com.siemens.cto.aem.ws.rest.v1.response.ApplicationResponse;
import com.siemens.cto.aem.ws.rest.v1.service.resource.CreateResourceParam;
import com.siemens.cto.aem.ws.rest.v1.service.resource.ResourceHierarchyParam;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.internal.verification.Times;
import org.mockito.runners.MockitoJUnitRunner;

import javax.activation.DataHandler;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.*;
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
    private ResourceInstance resourceInstance;

    @Before
    public void setUp() {
        group = new Group(new Identifier<Group>(1L), "theGroup");
        resourceInstance = new ResourceInstance(new Identifier<ResourceInstance>(1L), "resourceName", "resourceType", new LiteGroup(group.getId(), group.getName()), new HashMap<String, String>());
        cut = new ResourceServiceRestImpl(impl);
        when(authenticatedUser.getUser()).thenReturn(new User("Unused"));
        when(jsonResourceInstance.getCommand()).thenReturn(new ResourceInstanceRequest("resourceType", "resourceName", group.getName(), new HashMap<String, String>()));
    }

    @Test
    public void testFindResourceInstanceByGroup() {
        when(impl.getResourceInstancesByGroupName(group.getName())).thenReturn(new ArrayList<ResourceInstance>());
        Response response = cut.findResourceInstanceByGroup(group.getName());
        assertNotNull(response.getEntity());
    }

    @Test
    public void testFindResourceInstanceByNameGroup() {
        when(impl.getResourceInstancesByGroupName(group.getName())).thenReturn(new ArrayList<ResourceInstance>());
        Response response = cut.findResourceInstanceByNameGroup("testName", group.getName());
        assertNotNull(response.getEntity());
    }

    @Test
    public void testCreateResourceInstance() {
        when(impl.createResourceInstance(any(ResourceInstanceRequest.class), any(User.class))).thenReturn(resourceInstance);
        Response response = cut.createResourceInstance(jsonResourceInstance, authenticatedUser);
        assertNotNull(response.getEntity());
    }

    @Test
    public void testUpdateResourceInstanceAttributes() {
        when(impl.updateResourceInstance(anyString(), anyString(), any(ResourceInstanceRequest.class), any(User.class))).thenReturn(resourceInstance);
        Response response = cut.updateResourceInstanceAttributes("resourceName", group.getName(), jsonResourceInstance, authenticatedUser);
        assertNotNull(response.getEntity());
    }

    @Test
    public void testRemoveResourceInstance() {
        Response response = cut.removeResourceInstance("resourceName", group.getName(), authenticatedUser);
        assertNull(response.getEntity());
    }

    @Test
    public void testRemoveResources() {
        Response response = cut.removeResources(group.getName(), new ArrayList<String>(), authenticatedUser);
        assertNull(response.getEntity());
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

        when(impl.createTemplate(any(InputStream.class), any(InputStream.class), anyString(), any(User.class))).thenReturn(new CreateResourceTemplateApplicationResponseWrapper(new ConfigTemplate()));
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

        when(impl.createTemplate(any(InputStream.class), any(InputStream.class), anyString(), any(User.class))).thenReturn(new CreateResourceTemplateApplicationResponseWrapper(new ConfigTemplate()));
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

        when(impl.createTemplate(any(InputStream.class), any(InputStream.class), anyString(), any(User.class))).thenReturn(new CreateResourceTemplateApplicationResponseWrapper(new ConfigTemplate()));
        Response response = cut.createTemplate(attachmentList, "test-target-name", authenticatedUser);

        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
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
    public void testCreateGroupLevelWebAppResource() throws IOException {
        final DataHandler mockDataHandler1 = mock(DataHandler.class);
        when(mockDataHandler1.getName()).thenReturn("sample-resource.json");

        this.getClass().getClassLoader()
                .getResourceAsStream("sample-resource.json");

        when(mockDataHandler1.getInputStream()).thenReturn(this.getClass().getClassLoader()
                .getResourceAsStream("sample-resource.json"));
        final DataHandler mockDataHandler2 = mock(DataHandler.class);
        when(mockDataHandler2.getName()).thenReturn("sample-resource.tpl");
        when(mockDataHandler2.getInputStream()).thenReturn(this.getClass().getClassLoader()
                .getResourceAsStream("sample-resource.tpl"));

        final Attachment mockAttachment1 = mock(Attachment.class);
        when(mockAttachment1.getDataHandler()).thenReturn(mockDataHandler1);
        final Attachment mockAttachment2 = mock(Attachment.class);
        when(mockAttachment2.getDataHandler()).thenReturn(mockDataHandler2);
        final List<Attachment> attachmentList = new ArrayList<>();
        attachmentList.add(mockAttachment1);
        attachmentList.add(mockAttachment2);

        final CreateResourceParam createResourceParam = new CreateResourceParam();
        createResourceParam.setGroup("someGroup");
        createResourceParam.setWebApp("someWebApp");
        cut.createResource(attachmentList, createResourceParam, authenticatedUser);
        verify(impl).createGroupedLevelAppResource(any(ResourceTemplateMetaData.class), any(InputStream.class), anyString());
    }

    @Test
    public void testCreateWebAppResource() throws IOException {
        final DataHandler mockDataHandler1 = mock(DataHandler.class);
        when(mockDataHandler1.getName()).thenReturn("sample-resource.json");

        this.getClass().getClassLoader()
                .getResourceAsStream("sample-resource.json");

        when(mockDataHandler1.getInputStream()).thenReturn(this.getClass().getClassLoader()
                .getResourceAsStream("sample-resource.json"));
        final DataHandler mockDataHandler2 = mock(DataHandler.class);
        when(mockDataHandler2.getName()).thenReturn("sample-resource.tpl");
        when(mockDataHandler2.getInputStream()).thenReturn(this.getClass().getClassLoader()
                .getResourceAsStream("sample-resource.tpl"));

        final Attachment mockAttachment1 = mock(Attachment.class);
        when(mockAttachment1.getDataHandler()).thenReturn(mockDataHandler1);
        final Attachment mockAttachment2 = mock(Attachment.class);
        when(mockAttachment2.getDataHandler()).thenReturn(mockDataHandler2);
        final List<Attachment> attachmentList = new ArrayList<>();
        attachmentList.add(mockAttachment1);
        attachmentList.add(mockAttachment2);

        final CreateResourceParam createResourceParam = new CreateResourceParam();
        createResourceParam.setJvm("someJvm");
        createResourceParam.setWebApp("someWebApp");
        cut.createResource(attachmentList, createResourceParam, authenticatedUser);
        verify(impl).createAppResource(any(ResourceTemplateMetaData.class), any(InputStream.class), eq("someJvm"),
                eq("someWebApp"));
    }

    @Test
    public void testCreateGroupLevelWebServerResource() throws IOException {
        final DataHandler mockDataHandler1 = mock(DataHandler.class);
        when(mockDataHandler1.getName()).thenReturn("sample-resource.json");

        this.getClass().getClassLoader()
                .getResourceAsStream("sample-resource.json");

        when(mockDataHandler1.getInputStream()).thenReturn(this.getClass().getClassLoader()
                .getResourceAsStream("sample-resource.json"));
        final DataHandler mockDataHandler2 = mock(DataHandler.class);
        when(mockDataHandler2.getName()).thenReturn("sample-resource.tpl");
        when(mockDataHandler2.getInputStream()).thenReturn(this.getClass().getClassLoader()
                .getResourceAsStream("sample-resource.tpl"));

        final Attachment mockAttachment1 = mock(Attachment.class);
        when(mockAttachment1.getDataHandler()).thenReturn(mockDataHandler1);
        final Attachment mockAttachment2 = mock(Attachment.class);
        when(mockAttachment2.getDataHandler()).thenReturn(mockDataHandler2);
        final List<Attachment> attachmentList = new ArrayList<>();
        attachmentList.add(mockAttachment1);
        attachmentList.add(mockAttachment2);

        final CreateResourceParam createResourceParam = new CreateResourceParam();
        createResourceParam.setGroup("someGroup");
        createResourceParam.setWebServer("*");
        cut.createResource(attachmentList, createResourceParam, authenticatedUser);
        verify(impl).createGroupLevelWebServerResource(any(ResourceTemplateMetaData.class), any(InputStream.class),
                eq("someGroup"), any(User.class));
    }

    @Test
    public void testCreateWebServerResource() throws IOException {
        final DataHandler mockDataHandler1 = mock(DataHandler.class);
        when(mockDataHandler1.getName()).thenReturn("sample-resource.json");

        this.getClass().getClassLoader()
                .getResourceAsStream("sample-resource.json");

        when(mockDataHandler1.getInputStream()).thenReturn(this.getClass().getClassLoader()
                .getResourceAsStream("sample-resource.json"));
        final DataHandler mockDataHandler2 = mock(DataHandler.class);
        when(mockDataHandler2.getName()).thenReturn("sample-resource.tpl");
        when(mockDataHandler2.getInputStream()).thenReturn(this.getClass().getClassLoader()
                .getResourceAsStream("sample-resource.tpl"));

        final Attachment mockAttachment1 = mock(Attachment.class);
        when(mockAttachment1.getDataHandler()).thenReturn(mockDataHandler1);
        final Attachment mockAttachment2 = mock(Attachment.class);
        when(mockAttachment2.getDataHandler()).thenReturn(mockDataHandler2);
        final List<Attachment> attachmentList = new ArrayList<>();
        attachmentList.add(mockAttachment1);
        attachmentList.add(mockAttachment2);

        final CreateResourceParam createResourceParam = new CreateResourceParam();
        createResourceParam.setWebServer("someWebServer");
        cut.createResource(attachmentList, createResourceParam, authenticatedUser);
        verify(impl).createWebServerResource(any(ResourceTemplateMetaData.class), any(InputStream.class),
                eq("someWebServer"), any(User.class));
    }

    @Test
    public void testCreateGroupLevelJvmResource() throws IOException {
        final DataHandler mockDataHandler1 = mock(DataHandler.class);
        when(mockDataHandler1.getName()).thenReturn("sample-resource.json");

        this.getClass().getClassLoader()
                .getResourceAsStream("sample-resource.json");

        when(mockDataHandler1.getInputStream()).thenReturn(this.getClass().getClassLoader()
                .getResourceAsStream("sample-resource.json"));
        final DataHandler mockDataHandler2 = mock(DataHandler.class);
        when(mockDataHandler2.getName()).thenReturn("sample-resource.tpl");
        when(mockDataHandler2.getInputStream()).thenReturn(this.getClass().getClassLoader()
                .getResourceAsStream("sample-resource.tpl"));

        final Attachment mockAttachment1 = mock(Attachment.class);
        when(mockAttachment1.getDataHandler()).thenReturn(mockDataHandler1);
        final Attachment mockAttachment2 = mock(Attachment.class);
        when(mockAttachment2.getDataHandler()).thenReturn(mockDataHandler2);
        final List<Attachment> attachmentList = new ArrayList<>();
        attachmentList.add(mockAttachment1);
        attachmentList.add(mockAttachment2);

        final CreateResourceParam createResourceParam = new CreateResourceParam();
        createResourceParam.setGroup("someGroup");
        createResourceParam.setJvm("*");
        cut.createResource(attachmentList, createResourceParam, authenticatedUser);
        verify(impl).createGroupLevelJvmResource(any(ResourceTemplateMetaData.class), any(InputStream.class),
                eq("someGroup"));
    }

    @Test
    public void testCreateJvmResource() throws IOException {
        final DataHandler mockDataHandler1 = mock(DataHandler.class);
        when(mockDataHandler1.getName()).thenReturn("sample-resource.json");

        this.getClass().getClassLoader()
                .getResourceAsStream("sample-resource.json");

        when(mockDataHandler1.getInputStream()).thenReturn(this.getClass().getClassLoader()
                .getResourceAsStream("sample-resource.json"));
        final DataHandler mockDataHandler2 = mock(DataHandler.class);
        when(mockDataHandler2.getName()).thenReturn("sample-resource.tpl");
        when(mockDataHandler2.getInputStream()).thenReturn(this.getClass().getClassLoader()
                .getResourceAsStream("sample-resource.tpl"));

        final Attachment mockAttachment1 = mock(Attachment.class);
        when(mockAttachment1.getDataHandler()).thenReturn(mockDataHandler1);
        final Attachment mockAttachment2 = mock(Attachment.class);
        when(mockAttachment2.getDataHandler()).thenReturn(mockDataHandler2);
        final List<Attachment> attachmentList = new ArrayList<>();
        attachmentList.add(mockAttachment1);
        attachmentList.add(mockAttachment2);

        final CreateResourceParam createResourceParam = new CreateResourceParam();
        createResourceParam.setJvm("someJvm");
        cut.createResource(attachmentList, createResourceParam, authenticatedUser);
        verify(impl).createJvmResource(any(ResourceTemplateMetaData.class), any(InputStream.class),
                eq("someJvm"));
    }

    @Test
    public void testCreateResourceNoParamsSpecified() throws IOException {
        final DataHandler mockDataHandler1 = mock(DataHandler.class);
        when(mockDataHandler1.getName()).thenReturn("sample-resource.json");

        this.getClass().getClassLoader()
                .getResourceAsStream("sample-resource.json");

        when(mockDataHandler1.getInputStream()).thenReturn(this.getClass().getClassLoader()
                .getResourceAsStream("sample-resource.json"));
        final DataHandler mockDataHandler2 = mock(DataHandler.class);
        when(mockDataHandler2.getName()).thenReturn("sample-resource.tpl");
        when(mockDataHandler2.getInputStream()).thenReturn(this.getClass().getClassLoader()
                .getResourceAsStream("sample-resource.tpl"));

        final Attachment mockAttachment1 = mock(Attachment.class);
        when(mockAttachment1.getDataHandler()).thenReturn(mockDataHandler1);
        final Attachment mockAttachment2 = mock(Attachment.class);
        when(mockAttachment2.getDataHandler()).thenReturn(mockDataHandler2);
        final List<Attachment> attachmentList = new ArrayList<>();
        attachmentList.add(mockAttachment1);
        attachmentList.add(mockAttachment2);

        final CreateResourceParam createResourceParam = new CreateResourceParam();
        final Response response = cut.createResource(attachmentList, createResourceParam, authenticatedUser);
        assertEquals("AEM64", ((ApplicationResponse) response.getEntity()).getMsgCode());
    }

    @Test
    public void testCreateResourceWithMissingAttachment() throws IOException {
        final DataHandler mockDataHandler1 = mock(DataHandler.class);
        when(mockDataHandler1.getName()).thenReturn("sample-resource.json");

        this.getClass().getClassLoader()
                .getResourceAsStream("sample-resource.json");

        when(mockDataHandler1.getInputStream()).thenReturn(this.getClass().getClassLoader()
                .getResourceAsStream("sample-resource.json"));
        final DataHandler mockDataHandler2 = mock(DataHandler.class);
        when(mockDataHandler2.getName()).thenReturn("sample-resource.tpl");
        when(mockDataHandler2.getInputStream()).thenReturn(this.getClass().getClassLoader()
                .getResourceAsStream("sample-resource.tpl"));

        final Attachment mockAttachment1 = mock(Attachment.class);
        when(mockAttachment1.getDataHandler()).thenReturn(mockDataHandler1);
        final Attachment mockAttachment2 = mock(Attachment.class);
        when(mockAttachment2.getDataHandler()).thenReturn(mockDataHandler2);
        final List<Attachment> attachmentList = new ArrayList<>();
        attachmentList.add(mockAttachment1);

        final CreateResourceParam createResourceParam = new CreateResourceParam();
        final Response response = cut.createResource(attachmentList, createResourceParam, authenticatedUser);
        assertEquals("AEM61", ((ApplicationResponse) response.getEntity()).getMsgCode());
    }

    @Test
    public void testCreateResourceWithIoException() throws IOException {
        final DataHandler mockDataHandler1 = mock(DataHandler.class);
        when(mockDataHandler1.getName()).thenReturn("sample-resource.json");

        this.getClass().getClassLoader()
                .getResourceAsStream("sample-resource.json");

        when(mockDataHandler1.getInputStream()).thenReturn(this.getClass().getClassLoader()
                .getResourceAsStream("sample-resource.json"));
        final DataHandler mockDataHandler2 = mock(DataHandler.class);
        when(mockDataHandler2.getName()).thenReturn("sample-resource.tpl");
        doThrow(new IOException()).when(mockDataHandler2).getInputStream();

        final Attachment mockAttachment1 = mock(Attachment.class);
        when(mockAttachment1.getDataHandler()).thenReturn(mockDataHandler1);
        final Attachment mockAttachment2 = mock(Attachment.class);
        when(mockAttachment2.getDataHandler()).thenReturn(mockDataHandler2);
        final List<Attachment> attachmentList = new ArrayList<>();
        attachmentList.add(mockAttachment1);
        attachmentList.add(mockAttachment2);
        final CreateResourceParam createResourceParam = new CreateResourceParam();
        final Response response = cut.createResource(attachmentList, createResourceParam, authenticatedUser);
        assertEquals("AEM60", ((ApplicationResponse) response.getEntity()).getMsgCode());
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
    public void testUploadIncorrectFile() throws IOException {
        final DataHandler mockDataHandler1 = mock(DataHandler.class);
        when(mockDataHandler1.getName()).thenReturn("sample-resource.json");

        this.getClass().getClassLoader()
                .getResourceAsStream("sample-resource.json");

        when(mockDataHandler1.getInputStream()).thenReturn(this.getClass().getClassLoader()
                .getResourceAsStream("sample-resource.json"));
        final DataHandler mockDataHandler2 = mock(DataHandler.class);
        when(mockDataHandler2.getName()).thenReturn("sample-resource.war");
        when(mockDataHandler2.getInputStream()).thenReturn(this.getClass().getClassLoader()
                .getResourceAsStream("sample-resource.tpl"));

        final Attachment mockAttachment1 = mock(Attachment.class);
        when(mockAttachment1.getDataHandler()).thenReturn(mockDataHandler1);
        final Attachment mockAttachment2 = mock(Attachment.class);
        when(mockAttachment2.getDataHandler()).thenReturn(mockDataHandler2);

        final List<Attachment> attachmentList = new ArrayList<>();
        attachmentList.add(mockAttachment1);
        attachmentList.add(mockAttachment2);

        final CreateResourceParam createResourceParam = new CreateResourceParam();
        createResourceParam.setGroup("someGroup");
        createResourceParam.setJvm("*");

        final Response response = cut.createResource(attachmentList, createResourceParam, authenticatedUser);
        assertEquals("File being uploaded is invalid! The expected file type as indicated in the meta data is text based and should have a TPL extension.",
                ((ApplicationResponse) response.getEntity()).getApplicationResponseContent());
        verify(impl, new Times(0)).createGroupLevelJvmResource(any(ResourceTemplateMetaData.class), any(InputStream.class),
                eq("someGroup"));
    }

    @Test
    public void testUploadExternalProperties() throws IOException {
        AuthenticatedUser mockAuthenticatedUser = mock(AuthenticatedUser.class);
        User mockUser = mock(User.class);
        when(mockAuthenticatedUser.getUser()).thenReturn(mockUser);
        when(mockUser.getId()).thenReturn("mock-resources-user");

        Attachment mockAttachment = mock(Attachment.class);
        DataHandler mockDataHandler = mock(DataHandler.class);
        when(mockAttachment.getDataHandler()).thenReturn(mockDataHandler);
        when(mockDataHandler.getName()).thenReturn("external.properties");

        InputStream mockInputStream = mock(InputStream.class);
        when(mockDataHandler.getInputStream()).thenReturn(mockInputStream);

        File propertiesFile = new File("./src/test/resources/vars.properties");

        Response response = cut.uploadExternalProperties(mockAttachment, mockAuthenticatedUser);
        assertEquals(200, response.getStatusInfo().getStatusCode());
    }

    @Test
    public void testUploadExternalPropertiesThrowsIOException() throws IOException {
        AuthenticatedUser mockAuthenticatedUser = mock(AuthenticatedUser.class);
        User mockUser = mock(User.class);
        when(mockAuthenticatedUser.getUser()).thenReturn(mockUser);
        when(mockUser.getId()).thenReturn("mock-resources-user");

        Attachment mockAttachment = mock(Attachment.class);
        DataHandler mockDataHandler = mock(DataHandler.class);
        when(mockAttachment.getDataHandler()).thenReturn(mockDataHandler);
        when(mockDataHandler.getName()).thenReturn("external.properties");

        when(mockDataHandler.getInputStream()).thenThrow(new IOException("getInputStream is throwing an IO Exception"));

        File propertiesFile = new File("./src/test/resources/vars.properties");

        Response response = cut.uploadExternalProperties(mockAttachment, mockAuthenticatedUser);
        assertEquals(500, response.getStatusInfo().getStatusCode());
    }

    @Test
    public void testGetExternalProperties() {
        cut.getExternalProperties();
        verify(impl).getExternalProperties();
    }
}
