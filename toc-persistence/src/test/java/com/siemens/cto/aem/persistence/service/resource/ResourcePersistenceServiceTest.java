package com.siemens.cto.aem.persistence.service.resource;

import com.siemens.cto.aem.common.request.group.CreateGroupRequest;
import com.siemens.cto.aem.common.request.resource.ResourceInstanceRequest;
import com.siemens.cto.aem.common.exception.NotFoundException;
import com.siemens.cto.aem.common.domain.model.event.Event;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.resource.ResourceInstance;
import com.siemens.cto.aem.persistence.dao.group.GroupEventsTestHelper;
import com.siemens.cto.aem.persistence.jpa.domain.JpaGroup;
import com.siemens.cto.aem.persistence.jpa.service.GroupCrudService;
import com.siemens.cto.aem.persistence.service.ResourcePersistenceService;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Created by z003e5zv on 3/25/2015.
 */
@Transactional
public abstract class ResourcePersistenceServiceTest {
    private String userName = "Test User Name";

    protected abstract ResourcePersistenceService getResourcePersistenceService();
    protected abstract GroupCrudService getGroupCrudService();

    @Test
    public void testCreateResourceInstance() throws Exception {
        String testGroupName = "testCreateResourceInstance_Group";
        final JpaGroup jpaGroup = this.getGroupCrudService().createGroup(new CreateGroupRequest(testGroupName));

        Map<String, String> map = new HashMap<>();
        map.put("Attribute_key", "attribute_value");
        map.put("Attribute_key1", "attribute_value1");
        map.put("Attribute_key2", "attribute_value2");
        final Event<ResourceInstanceRequest> resourceInstanceEvent = ResourceInstanceEventsTestHelper.createEventWithResourceInstanceCommand("TestResourceTypeName", "TestFriendlyName", jpaGroup.getName(), map, userName);
        final ResourceInstance storedResourceInstance = getResourcePersistenceService().createResourceInstance(resourceInstanceEvent);

        assertEquals(storedResourceInstance.getName(), "TestFriendlyName");
        assertEquals(3, storedResourceInstance.getAttributes().size());

        final ResourceInstance persistedResourceInstance = getResourcePersistenceService().getResourceInstanceByGroupIdAndName(jpaGroup.getId(), "TestFriendlyName");

        assertEquals(persistedResourceInstance.getName(), "TestFriendlyName");
        assertEquals(3, persistedResourceInstance.getAttributes().size());
    }

    @Test
    public void testUpdateResourceInstanceAttributes() throws Exception {
        String testGroupName = "testUpdateResourceInstanceAttributes_Group";
        JpaGroup jpaGroup = this.getGroupCrudService().createGroup(new CreateGroupRequest(testGroupName));

        Map<String, String> map = new HashMap<>();
        map.put("Attribute_key", "attribute_value");
        map.put("Attribute_key1", "attribute_value1");
        map.put("Attribute_key2", "attribute_value2");
        ResourceInstance preCreateResourceInstance = getResourcePersistenceService().createResourceInstance(ResourceInstanceEventsTestHelper.createEventWithResourceInstanceCommand("ResourceTypeName", "FriendlyName", jpaGroup.getName(), map, userName));


        Map<String, String> updatedMap = new HashMap<>();
        updatedMap.put("Attribute_key", "attribute_value_updated");
        updatedMap.put("Attribute_key1", "attribute_value1_updated");
        updatedMap.put("Attribute_key2", "attribute_value2_updated");
        final Event<ResourceInstanceRequest> resourceInstanceEvent = ResourceInstanceEventsTestHelper.createEventWithResourceInstanceCommand("ResourceTypeName", "FriendlyName", jpaGroup.getName(), updatedMap, userName);
        final ResourceInstance storedResourceInstance = getResourcePersistenceService().updateResourceInstance(preCreateResourceInstance, resourceInstanceEvent);
        //Check to see if everything else remained the same
        Assert.assertEquals(preCreateResourceInstance.getGroup().getId(), storedResourceInstance.getGroup().getId());
        Assert.assertEquals(preCreateResourceInstance.getResourceTypeName(), storedResourceInstance.getResourceTypeName());
        Assert.assertEquals(preCreateResourceInstance.getResourceInstanceId(), storedResourceInstance.getResourceInstanceId());

        Assert.assertEquals(resourceInstanceEvent.getRequest().getAttributes(), storedResourceInstance.getAttributes());
    }
    @Test
    public void testUpdateResourceInstanceName() throws Exception {
        String testGroupName = "testUpdateResourceInstanceName_Group";
        String oldName = "oldName";
        String newName = "new_name";
        JpaGroup jpaGroup = this.getGroupCrudService().createGroup(new CreateGroupRequest(testGroupName));

        ResourceInstance preCreateResourceInstance = getResourcePersistenceService().createResourceInstance(ResourceInstanceEventsTestHelper.createEventWithResourceInstanceCommand("ResourceTypeName", oldName, jpaGroup.getName(), null, userName));

        final Event<ResourceInstanceRequest> resourceInstanceUpdateEvent = ResourceInstanceEventsTestHelper.createEventWithResourceInstanceCommand("ResourceTypeName", newName, jpaGroup.getName(), null, userName);
        ResourceInstance resourceInstance = getResourcePersistenceService().updateResourceInstance(preCreateResourceInstance, resourceInstanceUpdateEvent);
        Assert.assertEquals(resourceInstance.getResourceInstanceId(), preCreateResourceInstance.getResourceInstanceId());
        Assert.assertEquals(resourceInstanceUpdateEvent.getRequest().getName(), newName);
    }
    @Test
    public void testGetResourceInstance() {
        String testGroupName = "testGetResourceInstance_group";
        JpaGroup jpaGroup = this.getGroupCrudService().createGroup(new CreateGroupRequest(testGroupName));

        Map<String, String> map = new HashMap<>();
        map.put("Attribute_key", "attribute_value");
        map.put("Attribute_key1", "attribute_value1");
        map.put("Attribute_key2", "attribute_value2");
        final Event<ResourceInstanceRequest> resourceInstanceEvent = ResourceInstanceEventsTestHelper.createEventWithResourceInstanceCommand("TestResourceTypeName", "TestFriendlyName", jpaGroup.getName(), map, userName);
        final ResourceInstance storedResourceInstance = getResourcePersistenceService().createResourceInstance(resourceInstanceEvent);

        ResourceInstance jpaResourceInstance = this.getResourcePersistenceService().getResourceInstance(storedResourceInstance.getResourceInstanceId());
        Assert.assertNotNull(jpaResourceInstance);
    }
    @Test(expected = NotFoundException.class)
    public void testRemoveResourceInstance() throws Exception {
        String testGroupName = "testRemoveResourceInstance_Group";
        JpaGroup jpaGroup = this.getGroupCrudService().createGroup(new CreateGroupRequest(testGroupName));

        Map<String, String> map = new HashMap<>();
        map.put("Attribute_key", "attribute_value");
        map.put("Attribute_key1", "attribute_value1");
        map.put("Attribute_key2", "attribute_value2");
        ResourceInstance preCreateResourceInstance = getResourcePersistenceService().createResourceInstance(ResourceInstanceEventsTestHelper.createEventWithResourceInstanceCommand("ResourceTypeName", "friendlyName", jpaGroup.getName(), map, userName));

        final Identifier<ResourceInstance> resourceInstanceId = preCreateResourceInstance.getResourceInstanceId();

        this.getResourcePersistenceService().deleteResourceInstance(resourceInstanceId);

        this.getResourcePersistenceService().getResourceInstance(resourceInstanceId);
    }

    @Test
    public void testRemoveResources() {
        final String RESOURCE1 = "resource1";
        final String RESOURCE2 = "resource2";

        final String testGroupName = "testRemoveResources_Group";
        JpaGroup jpaGroup = this.getGroupCrudService().createGroup(new CreateGroupRequest(testGroupName));

        Map<String, String> map = new HashMap<>();
        map.put("Attribute_key", "attribute_value");
        map.put("Attribute_key1", "attribute_value1");
        map.put("Attribute_key2", "attribute_value2");

        getResourcePersistenceService().createResourceInstance(ResourceInstanceEventsTestHelper.createEventWithResourceInstanceCommand("ResourceTypeName", RESOURCE1, jpaGroup.getName(), map, userName));
        getResourcePersistenceService().createResourceInstance(ResourceInstanceEventsTestHelper.createEventWithResourceInstanceCommand("ResourceTypeName", RESOURCE2, jpaGroup.getName(), map, userName));

        List<ResourceInstance> resources = getResourcePersistenceService().getResourceInstancesByGroupId(jpaGroup.getId());

        assertEquals(2, resources.size());

        final String [] resourceNames = {RESOURCE1, RESOURCE2};
        getResourcePersistenceService().deleteResources(testGroupName, Arrays.asList(resourceNames));
        resources = getResourcePersistenceService().getResourceInstancesByGroupId(jpaGroup.getId());

        assertEquals(0, resources.size());
    }


    @Test
    public void TestGetByGroupName() throws Exception {

        String testGroupName = "TestGetByGroupName_group";
        JpaGroup jpaGroup = this.getGroupCrudService().createGroup(new CreateGroupRequest(testGroupName));
        Map<String, String> map = new HashMap<>();
        map.put("Attribute_key", "attribute_value");
        map.put("Attribute_key1", "attribute_value1");
        map.put("Attribute_key2", "attribute_value2");
        this.getResourcePersistenceService().createResourceInstance(ResourceInstanceEventsTestHelper.createEventWithResourceInstanceCommand("TestResourceTypeName", "TestFriendlyName", jpaGroup.getName(), map, userName));
        this.getResourcePersistenceService().createResourceInstance(ResourceInstanceEventsTestHelper.createEventWithResourceInstanceCommand("TestResourceTypeName2", "TestFriendlyName2", jpaGroup.getName(), map, userName));
        List<ResourceInstance> results = this.getResourcePersistenceService().getResourceInstancesByGroupId(jpaGroup.getId());
        Assert.assertEquals(results.size(), 2);
    }
    @Test
    public void TestGetByGroupNameAndName() throws Exception {
        String testGroupName = "TestGetByGroupNameAndName_group";
        String testName = "NameToTestWithGroup";
        JpaGroup jpaGroup = this.getGroupCrudService().createGroup(new CreateGroupRequest(testGroupName));
        Map<String, String> map = new HashMap<>();
        map.put("Attribute_key", "attribute_value");
        map.put("Attribute_key1", "attribute_value1");
        map.put("Attribute_key2", "attribute_value2");
        this.getResourcePersistenceService().createResourceInstance(ResourceInstanceEventsTestHelper.createEventWithResourceInstanceCommand("TestResourceTypeName2", testName, jpaGroup.getName(), map, userName));
        ResourceInstance result = this.getResourcePersistenceService().getResourceInstanceByGroupIdAndName(jpaGroup.getId(), testName);
        Assert.assertNotNull(result);
    }
    @Test
    public void TestGetByGroupNameAndResourceTypeName() throws Exception {
        String testGroupName = "TestGetByGroupNameAndResourceTypeName_group";
        String testResourceName = "ResourceTypeNameTest";
        JpaGroup jpaGroup = this.getGroupCrudService().createGroup(new CreateGroupRequest(testGroupName));
        Map<String, String> map = new HashMap<>();
        map.put("Attribute_key", "attribute_value");
        map.put("Attribute_key1", "attribute_value1");
        map.put("Attribute_key2", "attribute_value2");
        this.getResourcePersistenceService().createResourceInstance(ResourceInstanceEventsTestHelper.createEventWithResourceInstanceCommand(testResourceName, "TestName1", jpaGroup.getName(), map, userName));
        this.getResourcePersistenceService().createResourceInstance(ResourceInstanceEventsTestHelper.createEventWithResourceInstanceCommand(testResourceName, "TestName2", jpaGroup.getName(), map, userName));
        List<ResourceInstance> results = this.getResourcePersistenceService().getResourceInstancesByGroupIdAndResourceTypeName(jpaGroup.getId(), testResourceName);
        Assert.assertEquals(results.size(), 2);
    }
}
