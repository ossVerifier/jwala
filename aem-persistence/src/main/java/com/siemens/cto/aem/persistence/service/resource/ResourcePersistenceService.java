package com.siemens.cto.aem.persistence.service.resource;

import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.resource.ResourceInstance;
import com.siemens.cto.aem.domain.model.resource.command.ResourceInstanceCommand;
import com.siemens.cto.aem.domain.model.temporary.PaginationParameter;

import java.util.List;

/**
 * Created by z003e5zv on 3/25/2015.
 */
public interface ResourcePersistenceService {

    ResourceInstance createResourceInstance(final Event<ResourceInstanceCommand> resourceInstanceCreateEvent);
    List<ResourceInstance> getResourceInstancesByGroupId(final Long groupId);
    ResourceInstance getResourceInstanceByGroupIdAndName(final Long groupId, final String name);
    List<ResourceInstance> getResourceInstancesByGroupIdAndResourceTypeName(final Long groupId, final String typeName);
    ResourceInstance getResourceInstance(final Identifier<ResourceInstance> resourceInstanceId);
    ResourceInstance updateResourceInstance(ResourceInstance resourceInstance, final Event<ResourceInstanceCommand> resourceInstanceUpdateEvent);
    void deleteResourceInstance(final Identifier<ResourceInstance> resourceInstanceId);

}
