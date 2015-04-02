package com.siemens.cto.aem.persistence.service.resource;

import com.siemens.cto.aem.common.exception.NotFoundException;
import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.resource.ResourceInstance;
import com.siemens.cto.aem.domain.model.resource.command.CreateResourceInstanceCommand;
import com.siemens.cto.aem.domain.model.resource.command.UpdateResourceInstanceAttributesCommand;
import com.siemens.cto.aem.domain.model.resource.command.UpdateResourceInstanceNameCommand;
import com.siemens.cto.aem.persistence.dao.group.GroupEventsTestHelper;
import com.siemens.cto.aem.persistence.jpa.domain.JpaGroup;
import com.siemens.cto.aem.persistence.jpa.service.group.GroupCrudService;
import org.junit.Test;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        JpaGroup jpaGroup = this.getGroupCrudService().createGroup(GroupEventsTestHelper.createCreateGroupEvent(testGroupName, userName));

        Map<String, String> map = new HashMap<>();
        map.put("Attribute_key", "attribute_value");
        map.put("Attribute_key1", "attribute_value1");
        map.put("Attribute_key2", "attribute_value2");
        final Event<CreateResourceInstanceCommand> resourceInstanceEvent = ResourceInstanceEventsTestHelper.createCreateResourceInstanceCommand("TestResourceTypeName", "TestFriendlyName", jpaGroup.getId(), map, userName);
        final ResourceInstance storedResourceInstance = getResourcePersistenceService().createResourceInstance(resourceInstanceEvent);

       // Assert.assertEquals(resourceInstanceEvent.getCommand().getGroupId(), storedResourceInstance.getGroup().getId());
       // Assert.assertEquals(resourceInstanceEvent.getCommand().getAttributes(), storedResourceInstance.getAttributes());
        //Assert.assertEquals(resourceInstanceEvent.getCommand().getResourceTypeName(), storedResourceInstance.getResourceTypeName());
       // Assert.assertNotNull(storedResourceInstance.getResourceInstanceId());
    }

    @Test
    public void testUpdateResourceInstanceAttributes() throws Exception {
        String testGroupName = "testUpdateResourceInstanceAttributes_Group";
        JpaGroup jpaGroup = this.getGroupCrudService().createGroup(GroupEventsTestHelper.createCreateGroupEvent(testGroupName, userName));

        Map<String, String> map = new HashMap<>();
        map.put("Attribute_key", "attribute_value");
        map.put("Attribute_key1", "attribute_value1");
        map.put("Attribute_key2", "attribute_value2");
        ResourceInstance preCreateResourceInstance = getResourcePersistenceService().createResourceInstance(ResourceInstanceEventsTestHelper.createCreateResourceInstanceCommand("ResourceTypeName", "FriendlyName", jpaGroup.getId(), map, userName));


        Map<String, String> updatedMap = new HashMap<>();
        updatedMap.put("Attribute_key", "attribute_value_updated");
        updatedMap.put("Attribute_key1", "attribute_value1_updated");
        updatedMap.put("Attribute_key2", "attribute_value2_updated");
        final Event<UpdateResourceInstanceAttributesCommand> resourceInstanceEvent = ResourceInstanceEventsTestHelper.createUpdateResourceInstanceAttributesCommand(preCreateResourceInstance.getResourceInstanceId(), updatedMap, userName);
        final ResourceInstance storedResourceInstance = getResourcePersistenceService().updateResourceInstanceAttributes(resourceInstanceEvent);

       // Assert.assertEquals(preCreateResourceInstance.getGroup().getId(), storedResourceInstance.getGroup().getId());
       // Assert.assertEquals(resourceInstanceEvent.getCommand().getAttributes(), storedResourceInstance.getAttributes());
       // Assert.assertEquals(preCreateResourceInstance.getResourceTypeName(), storedResourceInstance.getResourceTypeName());
        //Assert.assertEquals(preCreateResourceInstance.getResourceInstanceId(), storedResourceInstance.getResourceInstanceId());
    }
    @Test
    public void testUpdateResourceInstanceName() throws Exception {
        String testGroupName = "testUpdateResourceInstanceName_Group";
        JpaGroup jpaGroup = this.getGroupCrudService().createGroup(GroupEventsTestHelper.createCreateGroupEvent(testGroupName, userName));

        ResourceInstance preCreateResourceInstance = getResourcePersistenceService().createResourceInstance(ResourceInstanceEventsTestHelper.createCreateResourceInstanceCommand("ResourceTypeName", "FriendlyName_old", jpaGroup.getId(), null, userName));

        final Event<UpdateResourceInstanceNameCommand> resourceInstanceEvent = ResourceInstanceEventsTestHelper.createUpdateResourceInstanceFriendlyNameCommand(preCreateResourceInstance.getResourceInstanceId(), "FriendlyName_new", userName);
        ResourceInstance resourceInstance = getResourcePersistenceService().getResourceInstance(preCreateResourceInstance.getResourceInstanceId());
       // Assert.assertEquals(resourceInstanceEvent.getCommand().getResourceInstanceIdentifier().getId(), resourceInstance.getResourceInstanceId());
       // Assert.assertEquals(resourceInstanceEvent.getCommand().getFriendlyName(), "FriendlyName_new");
    }
    @Test(expected = NotFoundException.class)
    public void testUpdateNonExistent() {
        String testGroupName = "testCreateResourceInstance_Group";
        JpaGroup jpaGroup = this.getGroupCrudService().createGroup(GroupEventsTestHelper.createCreateGroupEvent(testGroupName, userName));


        final Identifier<Group> nonExistentGroupId = new Identifier<>(-123456L);
        Map<String, String> map = new HashMap<>();
        map.put("Attribute_key", "attribute_value_updated");
        map.put("Attribute_key1", "attribute_value1_updated");
        map.put("Attribute_key2", "attribute_value2_updated");
        this.getResourcePersistenceService().updateResourceInstanceAttributes(
                ResourceInstanceEventsTestHelper.createUpdateResourceInstanceAttributesCommand(new Identifier<ResourceInstance>(-12345678L), map, userName));
    }
    @Test
    public void testGetResourceInstance() {
        String testGroupName = "testGetResourceInstance_group";
        JpaGroup jpaGroup = this.getGroupCrudService().createGroup(GroupEventsTestHelper.createCreateGroupEvent(testGroupName, userName));

        Map<String, String> map = new HashMap<>();
        map.put("Attribute_key", "attribute_value");
        map.put("Attribute_key1", "attribute_value1");
        map.put("Attribute_key2", "attribute_value2");
        final Event<CreateResourceInstanceCommand> resourceInstanceEvent = ResourceInstanceEventsTestHelper.createCreateResourceInstanceCommand("TestResourceTypeName", "TestFriendlyName", jpaGroup.getId(), map, userName);
        final ResourceInstance storedResourceInstance = getResourcePersistenceService().createResourceInstance(resourceInstanceEvent);

        ResourceInstance jpaResourceInstance = this.getResourcePersistenceService().getResourceInstance(storedResourceInstance.getResourceInstanceId());
       // Assert.assertNotNull(jpaResourceInstance);
    }
    @Test(expected = NotFoundException.class)
    public void testRemoveResourceInstance() throws Exception {
        String testGroupName = "testRemoveResourceInstance_Group";
        JpaGroup jpaGroup = this.getGroupCrudService().createGroup(GroupEventsTestHelper.createCreateGroupEvent(testGroupName, userName));

        Map<String, String> map = new HashMap<>();
        map.put("Attribute_key", "attribute_value");
        map.put("Attribute_key1", "attribute_value1");
        map.put("Attribute_key2", "attribute_value2");
        ResourceInstance preCreateResourceInstance = getResourcePersistenceService().createResourceInstance(ResourceInstanceEventsTestHelper.createCreateResourceInstanceCommand("ResourceTypeName", "friendlyName", jpaGroup.getId(), map, userName));

        final Identifier<ResourceInstance> resourceInstanceId = preCreateResourceInstance.getResourceInstanceId();

        this.getResourcePersistenceService().deleteResourceInstance(resourceInstanceId);

        this.getResourcePersistenceService().getResourceInstance(resourceInstanceId);
    }
    @Test
    public void TestGetByGroupName() throws Exception {

        String testGroupName = "TestGetByGroupName_group";
        JpaGroup jpaGroup = this.getGroupCrudService().createGroup(GroupEventsTestHelper.createCreateGroupEvent(testGroupName, userName));
        Map<String, String> map = new HashMap<>();
        map.put("Attribute_key", "attribute_value");
        map.put("Attribute_key1", "attribute_value1");
        map.put("Attribute_key2", "attribute_value2");
        this.getResourcePersistenceService().createResourceInstance(ResourceInstanceEventsTestHelper.createCreateResourceInstanceCommand("TestResourceTypeName", "TestFriendlyName", jpaGroup.getId(), map, userName));
        this.getResourcePersistenceService().createResourceInstance(ResourceInstanceEventsTestHelper.createCreateResourceInstanceCommand("TestResourceTypeName2", "TestFriendlyName2", jpaGroup.getId(), map, userName));
        List<ResourceInstance> results = this.getResourcePersistenceService().getResourceInstancesByGroupId(jpaGroup.getId());
       // Assert.assertEquals(results.size(), 2);
    }
    @Test
    public void TestGetByGroupNameAndName() throws Exception {
        String testGroupName = "TestGetByGroupNameAndName_group";
        String testName = "NameToTestWithGroup";
        JpaGroup jpaGroup = this.getGroupCrudService().createGroup(GroupEventsTestHelper.createCreateGroupEvent(testGroupName, userName));
        Map<String, String> map = new HashMap<>();
        map.put("Attribute_key", "attribute_value");
        map.put("Attribute_key1", "attribute_value1");
        map.put("Attribute_key2", "attribute_value2");
        this.getResourcePersistenceService().createResourceInstance(ResourceInstanceEventsTestHelper.createCreateResourceInstanceCommand("TestResourceTypeName2", testName, jpaGroup.getId(), map, userName));
        ResourceInstance result = this.getResourcePersistenceService().getResourceInstanceByGroupIdAndName(jpaGroup.getId(), testName);
       // Assert.assertNotNull(result);
    }
    @Test
    public void TestGetByGroupNameAndResourceTypeName() throws Exception {
        String testGroupName = "TestGetByGroupNameAndResourceTypeName_group";
        String testResourceName = "ResourceTypeNameTest";
        JpaGroup jpaGroup = this.getGroupCrudService().createGroup(GroupEventsTestHelper.createCreateGroupEvent(testGroupName, userName));
        Map<String, String> map = new HashMap<>();
        map.put("Attribute_key", "attribute_value");
        map.put("Attribute_key1", "attribute_value1");
        map.put("Attribute_key2", "attribute_value2");
        this.getResourcePersistenceService().createResourceInstance(ResourceInstanceEventsTestHelper.createCreateResourceInstanceCommand(testResourceName, "TestName1", jpaGroup.getId(), map, userName));
        this.getResourcePersistenceService().createResourceInstance(ResourceInstanceEventsTestHelper.createCreateResourceInstanceCommand(testResourceName, "TestName2", jpaGroup.getId(), map, userName));
        List<ResourceInstance> results = this.getResourcePersistenceService().getResourceInstancesByGroupIdAndResourceTypeName(jpaGroup.getId(), testResourceName);
       // Assert.assertEquals(results.size(), 2);
    }
}
