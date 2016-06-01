package com.siemens.cto.aem.ws.rest.v1.service.resource.impl;

import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.common.domain.model.group.LiteGroup;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.resource.ResourceGroup;
import com.siemens.cto.aem.common.domain.model.resource.ResourceInstance;
import com.siemens.cto.aem.common.domain.model.user.User;
import com.siemens.cto.aem.common.request.resource.ResourceInstanceRequest;
import com.siemens.cto.aem.persistence.jpa.domain.resource.config.template.ConfigTemplate;
import com.siemens.cto.aem.service.exception.ResourceServiceException;
import com.siemens.cto.aem.service.group.GroupService;
import com.siemens.cto.aem.service.jvm.JvmService;
import com.siemens.cto.aem.service.resource.ResourceService;
import com.siemens.cto.aem.service.resource.impl.CreateResourceTemplateApplicationResponseWrapper;
import com.siemens.cto.aem.ws.rest.v1.provider.AuthenticatedUser;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.activation.DataHandler;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
        Response response = cut.removeResourceInstance("resourceName", group.getName());
        assertNull(response.getEntity());
    }

    @Test
    public void testRemoveResources() {
        Response response = cut.removeResources(group.getName(), new ArrayList<String>());
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

        when(impl.createTemplate(any(InputStream.class), any(InputStream.class), anyString())).thenReturn(new CreateResourceTemplateApplicationResponseWrapper(new ConfigTemplate()));
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

        when(impl.createTemplate(any(InputStream.class), any(InputStream.class), anyString())).thenReturn(new CreateResourceTemplateApplicationResponseWrapper(new ConfigTemplate()));
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

        when(impl.createTemplate(any(InputStream.class), any(InputStream.class), anyString())).thenReturn(new CreateResourceTemplateApplicationResponseWrapper(new ConfigTemplate()));
        Response response = cut.createTemplate(attachmentList, "test-target-name", authenticatedUser);

        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
    }

    @Test
    public void testRemoveTemplate() {
        when(impl.removeTemplate(anyString())).thenReturn(1);
        Response response = cut.removeTemplate("template-name");
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        when(impl.removeTemplate(anyString())).thenThrow(new ResourceServiceException("FAIL"));
        response = cut.removeTemplate("template-name-fail");
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
        when(impl.checkFileExists(anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(new String());
        Response response = cut.checkFileExists("test", "test", null, null, "test");
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

}
