package com.siemens.cto.aem.persistence.service.resource;

import com.siemens.cto.aem.common.domain.model.resource.ResourceInstance;
import com.siemens.cto.aem.common.domain.model.user.User;
import com.siemens.cto.aem.common.request.group.CreateGroupRequest;
import com.siemens.cto.aem.common.request.resource.ResourceInstanceRequest;
import com.siemens.cto.aem.persistence.jpa.domain.JpaGroup;
import com.siemens.cto.aem.persistence.jpa.service.GroupCrudService;
import com.siemens.cto.aem.persistence.service.ResourcePersistenceService;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.transaction.annotation.Transactional;

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

    @Before
    public void setUp() throws Exception {
        User user = new User("testUser");
        user.addToThread();
    }

    @After
    public void tearDown() {
        User.getThreadLocalUser().invalidate();
    }

    @Test
    public void testCreateResourceInstance() throws Exception {
        String testGroupName = "testCreateResourceInstance_Group";
        final JpaGroup jpaGroup = this.getGroupCrudService().createGroup(new CreateGroupRequest(testGroupName));

        Map<String, String> map = new HashMap<>();
        map.put("Attribute_key", "attribute_value");
        map.put("Attribute_key1", "attribute_value1");
        map.put("Attribute_key2", "attribute_value2");
        final ResourceInstanceRequest resourceInstanceRequest = ResourceInstanceEventsTestHelper.createEventWithResourceInstanceRequest("TestResourceTypeName", "TestFriendlyName", jpaGroup.getName(), map, userName);
        final ResourceInstance storedResourceInstance = getResourcePersistenceService().createResourceInstance(resourceInstanceRequest);

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
        ResourceInstance preCreateResourceInstance = getResourcePersistenceService().createResourceInstance(ResourceInstanceEventsTestHelper.createEventWithResourceInstanceRequest("ResourceTypeName", "FriendlyName", jpaGroup.getName(), map, userName));


        Map<String, String> updatedMap = new HashMap<>();
        updatedMap.put("Attribute_key", "attribute_value_updated");
        updatedMap.put("Attribute_key1", "attribute_value1_updated");
        updatedMap.put("Attribute_key2", "attribute_value2_updated");
        final ResourceInstanceRequest eventWithResourceInstanceRequest = ResourceInstanceEventsTestHelper.createEventWithResourceInstanceRequest("ResourceTypeName", "FriendlyName", jpaGroup.getName(), updatedMap, userName);
        final ResourceInstance storedResourceInstance = getResourcePersistenceService().updateResourceInstance(preCreateResourceInstance, eventWithResourceInstanceRequest);
        //Check to see if everything else remained the same
        Assert.assertEquals(preCreateResourceInstance.getGroup().getId(), storedResourceInstance.getGroup().getId());
        Assert.assertEquals(preCreateResourceInstance.getResourceTypeName(), storedResourceInstance.getResourceTypeName());
        Assert.assertEquals(preCreateResourceInstance.getResourceInstanceId(), storedResourceInstance.getResourceInstanceId());

        Assert.assertEquals(eventWithResourceInstanceRequest.getAttributes(), storedResourceInstance.getAttributes());
    }
    @Test
    public void testUpdateResourceInstanceName() throws Exception {
        String testGroupName = "testUpdateResourceInstanceName_Group";
        String oldName = "oldName";
        String newName = "new_name";
        JpaGroup jpaGroup = this.getGroupCrudService().createGroup(new CreateGroupRequest(testGroupName));

        ResourceInstance preCreateResourceInstance = getResourcePersistenceService().createResourceInstance(ResourceInstanceEventsTestHelper.createEventWithResourceInstanceRequest("ResourceTypeName", oldName, jpaGroup.getName(), null, userName));

        final ResourceInstanceRequest resourceInstanceRequest = ResourceInstanceEventsTestHelper.createEventWithResourceInstanceRequest("ResourceTypeName", newName, jpaGroup.getName(), null, userName);
        ResourceInstance resourceInstance = getResourcePersistenceService().updateResourceInstance(preCreateResourceInstance, resourceInstanceRequest);
        Assert.assertEquals(resourceInstance.getResourceInstanceId(), preCreateResourceInstance.getResourceInstanceId());
        Assert.assertEquals(resourceInstanceRequest.getName(), newName);
    }
    @Test
    public void testGetResourceInstance() {
        String testGroupName = "testGetResourceInstance_group";
        JpaGroup jpaGroup = this.getGroupCrudService().createGroup(new CreateGroupRequest(testGroupName));

        Map<String, String> map = new HashMap<>();
        map.put("Attribute_key", "attribute_value");
        map.put("Attribute_key1", "attribute_value1");
        map.put("Attribute_key2", "attribute_value2");
        final ResourceInstanceRequest resourceInstanceRequest = ResourceInstanceEventsTestHelper.createEventWithResourceInstanceRequest("TestResourceTypeName", "TestFriendlyName", jpaGroup.getName(), map, userName);
        final ResourceInstance storedResourceInstance = getResourcePersistenceService().createResourceInstance(resourceInstanceRequest);

        ResourceInstance jpaResourceInstance = this.getResourcePersistenceService().getResourceInstance(storedResourceInstance.getResourceInstanceId());
        Assert.assertNotNull(jpaResourceInstance);
    }

    @Test
    public void TestGetByGroupName() throws Exception {

        String testGroupName = "TestGetByGroupName_group";
        JpaGroup jpaGroup = this.getGroupCrudService().createGroup(new CreateGroupRequest(testGroupName));
        Map<String, String> map = new HashMap<>();
        map.put("Attribute_key", "attribute_value");
        map.put("Attribute_key1", "attribute_value1");
        map.put("Attribute_key2", "attribute_value2");
        this.getResourcePersistenceService().createResourceInstance(ResourceInstanceEventsTestHelper.createEventWithResourceInstanceRequest("TestResourceTypeName", "TestFriendlyName", jpaGroup.getName(), map, userName));
        this.getResourcePersistenceService().createResourceInstance(ResourceInstanceEventsTestHelper.createEventWithResourceInstanceRequest("TestResourceTypeName2", "TestFriendlyName2", jpaGroup.getName(), map, userName));
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
        this.getResourcePersistenceService().createResourceInstance(ResourceInstanceEventsTestHelper.createEventWithResourceInstanceRequest("TestResourceTypeName2", testName, jpaGroup.getName(), map, userName));
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
        this.getResourcePersistenceService().createResourceInstance(ResourceInstanceEventsTestHelper.createEventWithResourceInstanceRequest(testResourceName, "TestName1", jpaGroup.getName(), map, userName));
        this.getResourcePersistenceService().createResourceInstance(ResourceInstanceEventsTestHelper.createEventWithResourceInstanceRequest(testResourceName, "TestName2", jpaGroup.getName(), map, userName));
        List<ResourceInstance> results = this.getResourcePersistenceService().getResourceInstancesByGroupIdAndResourceTypeName(jpaGroup.getId(), testResourceName);
        Assert.assertEquals(results.size(), 2);
    }
}
