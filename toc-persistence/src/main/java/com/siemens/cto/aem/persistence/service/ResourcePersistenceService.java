package com.siemens.cto.aem.persistence.service;

import com.siemens.cto.aem.common.request.resource.ResourceInstanceRequest;
import com.siemens.cto.aem.common.domain.model.event.Event;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.resource.ResourceInstance;

import java.util.List;

/**
 * Created by z003e5zv on 3/25/2015.
 */
public interface ResourcePersistenceService {

    ResourceInstance createResourceInstance(final Event<ResourceInstanceRequest> resourceInstanceCreateEvent);
    List<ResourceInstance> getResourceInstancesByGroupId(final Long groupId);
    ResourceInstance getResourceInstanceByGroupIdAndName(final Long groupId, final String name);
    List<ResourceInstance> getResourceInstancesByGroupIdAndResourceTypeName(final Long groupId, final String typeName);
    ResourceInstance getResourceInstance(final Identifier<ResourceInstance> resourceInstanceId);
    ResourceInstance updateResourceInstance(ResourceInstance resourceInstance, final Event<ResourceInstanceRequest> resourceInstanceUpdateEvent);
    void deleteResourceInstance(final Identifier<ResourceInstance> resourceInstanceId);
    void deleteResources(final String groupName, final List<String> resourceNames);

}
