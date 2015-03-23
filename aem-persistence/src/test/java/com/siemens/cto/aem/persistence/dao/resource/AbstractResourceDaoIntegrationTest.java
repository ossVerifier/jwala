package com.siemens.cto.aem.persistence.dao.resource;

import com.siemens.cto.aem.common.exception.NotFoundException;
import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.resource.ResourceInstance;
import com.siemens.cto.aem.domain.model.resource.command.CreateResourceInstanceCommand;
import com.siemens.cto.aem.domain.model.resource.command.UpdateResourceInstanceCommand;
import com.siemens.cto.aem.persistence.dao.group.GroupEventsTestHelper;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by z003e5zv on 3/20/2015.
 */
@Transactional
public abstract class AbstractResourceDaoIntegrationTest {

    private String userName = "Test User Name";

    protected abstract ResourceInstanceDao getResourceInstanceDao();

    @Test
    public void testCreateResourceInstance() throws Exception {
        Map<String, String> map = new HashMap<>();
        map.put("Attribute_key", "attribute_value");
        map.put("Attribute_key1", "attribute_value1");
        map.put("Attribute_key2", "attribute_value2");
        final Event<CreateResourceInstanceCommand> resourceInstanceEvent = ResourceInstanceEventsTestHelper.createCreateResourceInstanceCommand("TestResourceTypeName", new Long("2000"), "JVM", map, userName);
        final ResourceInstance storedResourceInstance = getResourceInstanceDao().createResourceInstance(resourceInstanceEvent);

        Assert.assertEquals(resourceInstanceEvent.getCommand().getParentId(), storedResourceInstance.getParentId());
        Assert.assertEquals(resourceInstanceEvent.getCommand().getParentType(), storedResourceInstance.getParentType());
        Assert.assertEquals(resourceInstanceEvent.getCommand().getAttributes(), storedResourceInstance.getAttributes());
        Assert.assertEquals(resourceInstanceEvent.getCommand().getResourceTypeName(), storedResourceInstance.getResourceTypeName());
        Assert.assertNotNull(storedResourceInstance.getResourceInstanceId());
    }

    @Test
    public void testUpdateResourceInstance() throws Exception {
        Map<String, String> map = new HashMap<>();
        map.put("Attribute_key", "attribute_value");
        map.put("Attribute_key1", "attribute_value1");
        map.put("Attribute_key2", "attribute_value2");
        ResourceInstance preCreateResourceInstance = getResourceInstanceDao().createResourceInstance(ResourceInstanceEventsTestHelper.createCreateResourceInstanceCommand("ResourceTypeName", new Long("1000"), "JVM", map, userName));


        Map<String, String> upDatedMap = new HashMap<>();
        upDatedMap.put("Attribute_key", "attribute_value_updated");
        upDatedMap.put("Attribute_key1", "attribute_value1_updated");
        upDatedMap.put("Attribute_key2", "attribute_value2_updated");
        final Event<UpdateResourceInstanceCommand> resourceInstanceEvent = ResourceInstanceEventsTestHelper.createUpdateResourceInstanceCommand(preCreateResourceInstance.getResourceInstanceId(), upDatedMap, userName);
        final ResourceInstance storedResourceInstance = getResourceInstanceDao().updateResourceInstance(resourceInstanceEvent);

        Assert.assertEquals(preCreateResourceInstance.getParentId(), storedResourceInstance.getParentId());
        Assert.assertEquals(preCreateResourceInstance.getParentType(), storedResourceInstance.getParentType());
        Assert.assertEquals(resourceInstanceEvent.getCommand().getAttributes(), storedResourceInstance.getAttributes());
        Assert.assertEquals(preCreateResourceInstance.getResourceTypeName(), storedResourceInstance.getResourceTypeName());
        Assert.assertEquals(preCreateResourceInstance.getResourceInstanceId(), storedResourceInstance.getResourceInstanceId());
    }
    @Test(expected = NotFoundException.class)
    public void testUpdateNonExistent() {

        final Identifier<Group> nonExistentGroupId = new Identifier<>(-123456L);
        Map<String, String> map = new HashMap<>();
        map.put("Attribute_key", "attribute_value_updated");
        map.put("Attribute_key1", "attribute_value1_updated");
        map.put("Attribute_key2", "attribute_value2_updated");
        this.getResourceInstanceDao().updateResourceInstance(
                ResourceInstanceEventsTestHelper.createUpdateResourceInstanceCommand(new Identifier<ResourceInstance>(-12345678L), map, userName));
    }
    @Test
    public void testRemoveResourceInstance() throws Exception {
        Map<String, String> map = new HashMap<>();
        map.put("Attribute_key", "attribute_value");
        map.put("Attribute_key1", "attribute_value1");
        map.put("Attribute_key2", "attribute_value2");
        ResourceInstance preCreateResourceInstance = getResourceInstanceDao().createResourceInstance(ResourceInstanceEventsTestHelper.createCreateResourceInstanceCommand("ResourceTypeName", new Long("1000"), "JVM", map, userName));

        final Identifier<ResourceInstance> resourceInstanceId = preCreateResourceInstance.getResourceInstanceId();

        this.getResourceInstanceDao().removeResourceInstance(resourceInstanceId);

        try {
            this.getResourceInstanceDao().getResourceInstance(resourceInstanceId);
            throw new RuntimeException("expected error didn't happen");
        }
        catch (final NotFoundException nfd) {
            System.out.println("Resource that had been deleted as part of a test was not found");// The silliness of
        }
    }
}


