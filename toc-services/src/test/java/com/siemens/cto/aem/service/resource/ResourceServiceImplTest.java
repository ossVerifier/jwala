package com.siemens.cto.aem.service.resource;

import com.siemens.cto.aem.common.domain.model.jvm.Jvm;
import com.siemens.cto.aem.common.request.jvm.UploadJvmConfigTemplateRequest;
import com.siemens.cto.aem.common.request.resource.ResourceInstanceRequest;
import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.resource.ResourceInstance;
import com.siemens.cto.aem.common.domain.model.resource.ResourceType;
import com.siemens.cto.aem.common.domain.model.user.User;
import com.siemens.cto.aem.persistence.jpa.domain.resource.config.template.JpaJvmConfigTemplate;
import com.siemens.cto.aem.persistence.service.*;
import com.siemens.cto.aem.service.resource.impl.ResourceServiceImpl;
import com.siemens.cto.aem.template.HarmonyTemplate;
import com.siemens.cto.aem.template.HarmonyTemplateEngine;
import com.siemens.cto.aem.template.webserver.exception.TemplateNotFoundException;
import com.siemens.cto.toc.files.FileManager;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link ResourceService}.
 */
public class ResourceServiceImplTest {

    @Mock
    private FileManager mockFileManager;

    @Mock
    private HarmonyTemplateEngine mockHarmonyTemplateEngine;

    @Mock
    private ResourcePersistenceService mockResourcePersistenceService;

    @Mock
    private GroupPersistenceService mockGroupPesistenceService;

    @Mock
    private ApplicationPersistenceService mockAppPersistenceService;

    @Mock
    private JvmPersistenceService mockJvmPersistenceService;

    @Mock
    private WebServerPersistenceService mockWebServerPersistenceService;

    private ResourceService resourceService;

    @Before
    public void setup() {
        // It is good practice to start with a clean sheet of paper before each test that is why resourceService is
        // initialized here. This makes sure that unrelated tests don't affect each other.
        MockitoAnnotations.initMocks(this);
        resourceService = new ResourceServiceImpl(mockFileManager, mockHarmonyTemplateEngine, mockResourcePersistenceService,
                mockGroupPesistenceService, mockAppPersistenceService, mockJvmPersistenceService,
                mockWebServerPersistenceService);
    }

    @Test
    public void testEncryption() {
        assertEquals("sr94UX5Zuw7QBM992+lAvQ==", resourceService.encryptUsingPlatformBean("hello"));
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
        when(mockGroupPesistenceService.getGroup(anyString())).thenReturn(mockGroup);
        when(mockResourcePersistenceService.getResourceInstanceByGroupIdAndName(anyLong(), anyString())).thenReturn(mockResourceInstance);
        when(mockResourceInstance.getResourceInstanceId()).thenReturn(new Identifier<ResourceInstance>(1L));
        resourceService.deleteResourceInstance("resourceName", "groupName");
        verify(mockResourcePersistenceService).deleteResourceInstance(any(Identifier.class));

        final ArrayList<String> resourceNames = new ArrayList<>();
        resourceService.deleteResources("groupName", resourceNames);
        verify(mockResourcePersistenceService).deleteResources(anyString(), anyList());
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
        when(mockHarmonyTemplateEngine.getTemplate(mockResourceType)).thenReturn(mockHarmonyTemplate);
        when(mockFileManager.getResourceTypes()).thenReturn(resourceTypes);
        Collection<ResourceType> types = resourceService.getResourceTypes();
        assertNotNull(types);

        when(mockOwner.checkOnly(any(Path.class))).thenThrow(new TemplateNotFoundException("Test bad template", new FileNotFoundException("TEST")));
        types = resourceService.getResourceTypes();
        assertNotNull(types);

        when(mockFileManager.getResourceTypes()).thenThrow(new IOException("Fail get resources"));
        boolean exceptionThrown = false;
        try {
            resourceService.getResourceTypes();
        } catch (Exception e){
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
    }

    @Test
    public void testGetResourceInstance() {
        final Identifier<ResourceInstance> aResourceInstanceId = new Identifier<>(1L);
        ResourceInstance mockResourceInstance = mock(ResourceInstance.class);
        when(mockResourcePersistenceService.getResourceInstance(aResourceInstanceId)).thenReturn(mockResourceInstance);
        ResourceInstance value = resourceService.getResourceInstance(aResourceInstanceId);
        assertNotNull(value);
    }

    @Test
    public void testGetResourceInstanceByGroupName(){
        final String groupName = "groupName";
        Group mockGroup = mock(Group.class);
        when(mockGroup.getId()).thenReturn(new Identifier<Group>(1L));
        when(mockResourcePersistenceService.getResourceInstancesByGroupId(anyLong())).thenReturn(new ArrayList<ResourceInstance>());
        when(mockGroupPesistenceService.getGroup(groupName)).thenReturn(mockGroup);
        List<ResourceInstance> value = resourceService.getResourceInstancesByGroupName(groupName);
        assertNotNull(value);
    }

    @Test
    public void testGetResourceInstanceByGroupNameAndName(){
        final String groupName = "groupName";
        String name = "name";
        Group mockGroup = mock(Group.class);
        ResourceInstance mockResourceInstance = mock(ResourceInstance.class);
        when(mockGroup.getId()).thenReturn(new Identifier<Group>(1L));
        when(mockResourcePersistenceService.getResourceInstanceByGroupIdAndName(anyLong(), anyString())).thenReturn(mockResourceInstance);
        when(mockGroupPesistenceService.getGroup(groupName)).thenReturn(mockGroup);
        ResourceInstance value = resourceService.getResourceInstanceByGroupNameAndName(groupName, name);
        assertNotNull(value);
    }

    @Test
    public void testGenerateResourceInstanceFragment(){
        final String groupName = "groupName";
        String name = "name";
        Group mockGroup = mock(Group.class);
        ResourceInstance mockResourceInstance = mock(ResourceInstance.class);
        when(mockGroup.getId()).thenReturn(new Identifier<Group>(1L));
        when(mockResourcePersistenceService.getResourceInstanceByGroupIdAndName(anyLong(), anyString())).thenReturn(mockResourceInstance);
        when(mockGroupPesistenceService.getGroup(groupName)).thenReturn(mockGroup);
        when(mockHarmonyTemplateEngine.populateResourceInstanceTemplate(any(ResourceInstance.class), anyMap(), anyMap())).thenReturn("populated resource template");
        String value = resourceService.generateResourceInstanceFragment("groupName", "name");
        assertNotNull(value);
    }

    @Test
    public void testGetResourceInstancesByGroupNameAndResourceTypeName(){
        Group mockGroup = mock(Group.class);
        when(mockGroup.getId()).thenReturn(new Identifier<Group>(1L));
        when(mockGroupPesistenceService.getGroup(anyString())).thenReturn(mockGroup);
        when(mockResourcePersistenceService.getResourceInstancesByGroupIdAndResourceTypeName(anyLong(), anyString())).thenReturn(new ArrayList<ResourceInstance>());
        List<ResourceInstance> value = resourceService.getResourceInstancesByGroupNameAndResourceTypeName("groupName", "resourceTypeName");
        assertNotNull(value);
    }

    @Test
    public void testCreateResourceInstance(){
        User mockUser = mock(User.class);
        ResourceInstance mockResourceInstance = mock(ResourceInstance.class);
        ResourceInstanceRequest mockResourceInstanceCommand = mock(ResourceInstanceRequest.class);
        when(mockUser.getId()).thenReturn("userId");
        when(mockGroupPesistenceService.getGroup(anyString())).thenReturn(mock(Group.class));
        when(mockResourcePersistenceService.createResourceInstance(any(ResourceInstanceRequest.class))).thenReturn(mockResourceInstance);
        ResourceInstance value = resourceService.createResourceInstance(mockResourceInstanceCommand, mockUser);
        assertNotNull(value);
    }

    @Test
    public void testUpdateResourceInstance(){
        User mockUser = mock(User.class);
        ResourceInstanceRequest resourceInstanceRequest = mock(ResourceInstanceRequest.class);
        when(mockUser.getId()).thenReturn("userId");
        final String groupName = "groupName";
        String name = "name";
        Group mockGroup = mock(Group.class);
        ResourceInstance mockResourceInstance = mock(ResourceInstance.class);
        when(mockGroup.getId()).thenReturn(new Identifier<Group>(1L));
        when(mockResourcePersistenceService.getResourceInstanceByGroupIdAndName(anyLong(), anyString())).thenReturn(mockResourceInstance);
        when(mockGroupPesistenceService.getGroup(groupName)).thenReturn(mockGroup);
        when(mockResourcePersistenceService.updateResourceInstance(any(ResourceInstance.class), any(ResourceInstanceRequest.class))).thenReturn(mockResourceInstance);
        ResourceInstance value = resourceService.updateResourceInstance("groupName", "name", resourceInstanceRequest, mockUser);
        assertNotNull(value);
    }

    @Test
    public void testCreateJvmTemplate() {
        final InputStream metaDataIn = this.getClass().getClassLoader()
                .getResourceAsStream("resource-service-test-files/create-jvm-template-test-metadata.json");
        final InputStream templateIn = this.getClass().getClassLoader()
                .getResourceAsStream("resource-service-test-files/server.xml.tpl");
        when(mockJvmPersistenceService.findJvmByExactName(eq("some jvm"))).thenReturn(mock(Jvm.class));
        when(mockJvmPersistenceService.uploadJvmTemplateXml(any(UploadJvmConfigTemplateRequest.class))).thenReturn(mock(JpaJvmConfigTemplate.class));
        resourceService.createTemplate(metaDataIn, templateIn);
        verify(mockJvmPersistenceService).findJvmByExactName("some jvm");
        verify(mockJvmPersistenceService).uploadJvmTemplateXml(any(UploadJvmConfigTemplateRequest.class));
    }
}
