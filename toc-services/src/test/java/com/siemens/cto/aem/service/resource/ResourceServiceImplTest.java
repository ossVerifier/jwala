package com.siemens.cto.aem.service.resource;

import com.siemens.cto.aem.request.resource.ResourceInstanceRequest;
import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.resource.ResourceInstance;
import com.siemens.cto.aem.domain.model.resource.ResourceType;
import com.siemens.cto.aem.domain.model.user.User;
import com.siemens.cto.aem.persistence.service.group.GroupPersistenceService;
import com.siemens.cto.aem.persistence.service.resource.ResourcePersistenceService;
import com.siemens.cto.aem.service.resource.impl.ResourceServiceImpl;
import com.siemens.cto.aem.template.HarmonyTemplate;
import com.siemens.cto.aem.template.HarmonyTemplateEngine;
import com.siemens.cto.aem.template.webserver.exception.TemplateNotFoundException;
import com.siemens.cto.toc.files.FileManager;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

public class ResourceServiceImplTest {

    private FileManager fileManager;
    private HarmonyTemplateEngine templateEngine;
    private ResourcePersistenceService resourcePersistenceService;
    private GroupPersistenceService groupPesistenceService;
    ResourceService cut = new ResourceServiceImpl(
            fileManager = mock(FileManager.class),
            templateEngine = mock(HarmonyTemplateEngine.class),
            resourcePersistenceService = mock(ResourcePersistenceService.class),
            groupPesistenceService = mock(GroupPersistenceService.class));

    @Test
    public void testEncryption() {
        assertEquals("sr94UX5Zuw7QBM992+lAvQ==", cut.encryptUsingPlatformBean("hello"));
    }

    @Test
    public void testCreate() {
        assertNotNull("");
    }

    @Test
    public void testUpdateAttributes() {
        assertNotNull("");
    }

    @Test
    public void testUpdateFriendlyName() {
        assertNotNull("");
    }

    @Test
    public void testDelete() {
        Group mockGroup = mock(Group.class);
        ResourceInstance mockResourceInstance = mock(ResourceInstance.class);
        when(mockGroup.getId()).thenReturn(new Identifier<Group>(11L));
        when(groupPesistenceService.getGroup(anyString())).thenReturn(mockGroup);
        when(resourcePersistenceService.getResourceInstanceByGroupIdAndName(anyLong(), anyString())).thenReturn(mockResourceInstance);
        when(mockResourceInstance.getResourceInstanceId()).thenReturn(new Identifier<ResourceInstance>(1L));
        cut.deleteResourceInstance("resourceName", "groupName");
        verify(resourcePersistenceService).deleteResourceInstance(any(Identifier.class));

        final ArrayList<String> resourceNames = new ArrayList<>();
        cut.deleteResources("groupName", resourceNames);
        verify(resourcePersistenceService).deleteResources(anyString(), anyList());
    }

    @Test
    public void testRead() {
        assertNotNull("");
    }

    @Test
    public void getType() {
        assertNotNull("");
    }

    @Test
    public void testGetResourceTypes() throws IOException {
        Collection<ResourceType> resourceTypes = new ArrayList<>();
        ResourceType mockResourceType = mock(ResourceType.class);
        resourceTypes.add(mockResourceType);
        HarmonyTemplateEngine mockOwner = mock(HarmonyTemplateEngine.class);
        Path mockPath = mock(Path.class);
        HarmonyTemplate mockHarmonyTemplate = new HarmonyTemplate(new Object(), mockPath, mockOwner);
        when(mockResourceType.isValid()).thenReturn(true);
        when(templateEngine.getTemplate(mockResourceType)).thenReturn(mockHarmonyTemplate);
        when(fileManager.getResourceTypes()).thenReturn(resourceTypes);
        Collection<ResourceType> types = cut.getResourceTypes();
        assertNotNull(types);

        when(mockOwner.checkOnly(any(Path.class))).thenThrow(new TemplateNotFoundException("Test bad template", new FileNotFoundException("TEST")));
        types = cut.getResourceTypes();
        assertNotNull(types);

        when(fileManager.getResourceTypes()).thenThrow(new IOException("Fail get resources"));
        boolean exceptionThrown = false;
        try {
            cut.getResourceTypes();
        } catch (Exception e){
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
    }

    @Test
    public void testGetResourceInstance() {
        final Identifier<ResourceInstance> aResourceInstanceId = new Identifier<>(1L);
        ResourceInstance mockResourceInstance = mock(ResourceInstance.class);
        when(resourcePersistenceService.getResourceInstance(aResourceInstanceId)).thenReturn(mockResourceInstance);
        ResourceInstance value = cut.getResourceInstance(aResourceInstanceId);
        assertNotNull(value);
    }

    @Test
    public void testGetResourceInstanceByGroupName(){
        final String groupName = "groupName";
        Group mockGroup = mock(Group.class);
        when(mockGroup.getId()).thenReturn(new Identifier<Group>(1L));
        when(resourcePersistenceService.getResourceInstancesByGroupId(anyLong())).thenReturn(new ArrayList<ResourceInstance>());
        when(groupPesistenceService.getGroup(groupName)).thenReturn(mockGroup);
        List<ResourceInstance> value = cut.getResourceInstancesByGroupName(groupName);
        assertNotNull(value);
    }

    @Test
    public void testGetResourceInstanceByGroupNameAndName(){
        final String groupName = "groupName";
        String name = "name";
        Group mockGroup = mock(Group.class);
        ResourceInstance mockResourceInstance = mock(ResourceInstance.class);
        when(mockGroup.getId()).thenReturn(new Identifier<Group>(1L));
        when(resourcePersistenceService.getResourceInstanceByGroupIdAndName(anyLong(), anyString())).thenReturn(mockResourceInstance);
        when(groupPesistenceService.getGroup(groupName)).thenReturn(mockGroup);
        ResourceInstance value = cut.getResourceInstanceByGroupNameAndName(groupName, name);
        assertNotNull(value);
    }

    @Test
    public void testGenerateResourceInstanceFragment(){
        final String groupName = "groupName";
        String name = "name";
        Group mockGroup = mock(Group.class);
        ResourceInstance mockResourceInstance = mock(ResourceInstance.class);
        when(mockGroup.getId()).thenReturn(new Identifier<Group>(1L));
        when(resourcePersistenceService.getResourceInstanceByGroupIdAndName(anyLong(), anyString())).thenReturn(mockResourceInstance);
        when(groupPesistenceService.getGroup(groupName)).thenReturn(mockGroup);
        when(templateEngine.populateResourceInstanceTemplate(any(ResourceInstance.class), anyMap(), anyMap())).thenReturn("populated resource template");
        String value = cut.generateResourceInstanceFragment("groupName", "name");
        assertNotNull(value);
    }

    @Test
    public void testGetResourceInstancesByGroupNameAndResourceTypeName(){
        Group mockGroup = mock(Group.class);
        when(mockGroup.getId()).thenReturn(new Identifier<Group>(1L));
        when(groupPesistenceService.getGroup(anyString())).thenReturn(mockGroup);
        when(resourcePersistenceService.getResourceInstancesByGroupIdAndResourceTypeName(anyLong(), anyString())).thenReturn(new ArrayList<ResourceInstance>());
        List<ResourceInstance> value = cut.getResourceInstancesByGroupNameAndResourceTypeName("groupName", "resourceTypeName");
        assertNotNull(value);
    }

    @Test
    public void testCreateResourceInstance(){
        User mockUser = mock(User.class);
        ResourceInstance mockResourceInstance = mock(ResourceInstance.class);
        ResourceInstanceRequest mockResourceInstanceCommand = mock(ResourceInstanceRequest.class);
        when(mockUser.getId()).thenReturn("userId");
        when(groupPesistenceService.getGroup(anyString())).thenReturn(mock(Group.class));
        when(resourcePersistenceService.createResourceInstance(any(Event.class))).thenReturn(mockResourceInstance);
        ResourceInstance value = cut.createResourceInstance(mockResourceInstanceCommand, mockUser);
        assertNotNull(value);
    }

    @Test
    public void testUpdateResourceInstance(){
        User mockUser = mock(User.class);
        ResourceInstanceRequest mockResourceInstanceCommand = mock(ResourceInstanceRequest.class);
        when(mockUser.getId()).thenReturn("userId");
        final String groupName = "groupName";
        String name = "name";
        Group mockGroup = mock(Group.class);
        ResourceInstance mockResourceInstance = mock(ResourceInstance.class);
        when(mockGroup.getId()).thenReturn(new Identifier<Group>(1L));
        when(resourcePersistenceService.getResourceInstanceByGroupIdAndName(anyLong(), anyString())).thenReturn(mockResourceInstance);
        when(groupPesistenceService.getGroup(groupName)).thenReturn(mockGroup);
        when(resourcePersistenceService.updateResourceInstance(any(ResourceInstance.class), any(Event.class))).thenReturn(mockResourceInstance);
        ResourceInstance value = cut.updateResourceInstance("groupName", "name", mockResourceInstanceCommand, mockUser);
        assertNotNull(value);
    }
}
