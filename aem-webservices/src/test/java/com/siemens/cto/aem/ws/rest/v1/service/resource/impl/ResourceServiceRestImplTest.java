package com.siemens.cto.aem.ws.rest.v1.service.resource.impl;

import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.group.LiteGroup;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.resource.ResourceInstance;
import com.siemens.cto.aem.domain.model.resource.ResourceType;
import com.siemens.cto.aem.domain.model.resource.command.ResourceInstanceCommand;
import com.siemens.cto.aem.domain.model.temporary.User;
import com.siemens.cto.aem.service.group.GroupService;
import com.siemens.cto.aem.service.jvm.JvmService;
import com.siemens.cto.aem.service.resource.ResourceService;
import com.siemens.cto.aem.ws.rest.v1.provider.AuthenticatedUser;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.OngoingStubbing;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

/**
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
        cut = new ResourceServiceRestImpl(impl, groupService, jvmService);
        when(authenticatedUser.getUser()).thenReturn(new User("Unused"));
        when(jsonResourceInstance.getCommand()).thenReturn(new ResourceInstanceCommand("resourceType", "resourceName", group.getName(), new HashMap<String, String>()));
    }

    @Test
    public void testGetTypes() {
        when(impl.getResourceTypes()).thenReturn(new ArrayList<ResourceType>());
        Response response = cut.getTypes();
        assertNotNull(response.getEntity());
    }

    @Test
    public void testFindResourceInstanceByGroup() {
        when(impl.getResourceInstancesByGroupName(group.getName())).thenReturn(new ArrayList<ResourceInstance>());
        Response response = cut.findResourceInstanceByGroup(group.getName());
        assertNotNull(response.getEntity());
    }

    @Test
    public void testGenerateInstanceByNameGroup() {
        final String testName = "testName";
        when(impl.generateResourceInstanceFragment(group.getName(), testName)).thenReturn("instance fragment");
        Response response = cut.generateResourceInstanceByNameGroup(testName, group.getName());
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
        when(impl.createResourceInstance(any(ResourceInstanceCommand.class), any(User.class))).thenReturn(resourceInstance);
        Response response = cut.createResourceInstance(jsonResourceInstance, authenticatedUser);
        assertNotNull(response.getEntity());
    }

    @Test
    public void testUpdateResourceInstanceAttributes(){
        when(impl.updateResourceInstance(anyString(), anyString(), any(ResourceInstanceCommand.class), any(User.class))).thenReturn(resourceInstance);
        Response response = cut.updateResourceInstanceAttributes("resourceName", group.getName(), jsonResourceInstance, authenticatedUser);
        assertNotNull(response.getEntity());
    }

    @Test
    public void testRemoveResourceInstance(){
        Response response = cut.removeResourceInstance("resourceName", group.getName());
        assertNull(response.getEntity());
    }

    @Test
    public void testRemoveResources() {
        Response response = cut.removeResources(group.getName(), new ArrayList<String>());
        assertNull(response.getEntity());
    }

    @Test
    public void testGetTemplate() {
        when(impl.getTemplate(anyString())).thenReturn("resourceTemplate");
        Response response = cut.getTemplate("resourceType");
        assertNotNull(response.getEntity());
    }

}
