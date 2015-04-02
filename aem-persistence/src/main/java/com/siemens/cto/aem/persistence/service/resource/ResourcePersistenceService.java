package com.siemens.cto.aem.persistence.service.resource;

import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.resource.ResourceInstance;
import com.siemens.cto.aem.domain.model.resource.command.CreateResourceInstanceCommand;
import com.siemens.cto.aem.domain.model.resource.command.UpdateResourceInstanceAttributesCommand;
import com.siemens.cto.aem.domain.model.resource.command.UpdateResourceInstanceNameCommand;

import java.util.List;

/**
 * Created by z003e5zv on 3/25/2015.
 */
public interface ResourcePersistenceService {

    ResourceInstance createResourceInstance(Event<CreateResourceInstanceCommand> resourceInstanceCreateEvent);
    List<ResourceInstance> getResourceInstancesByGroupId(final Long groupId);
    ResourceInstance getResourceInstanceByGroupIdAndName(final Long groupId, final String name);
    List<ResourceInstance> getResourceInstancesByGroupIdAndResourceTypeName(final Long groupId, final String typeName);
    ResourceInstance getResourceInstance(Identifier<ResourceInstance> resourceInstanceId);
    ResourceInstance updateResourceInstanceAttributes(Event<UpdateResourceInstanceAttributesCommand> resourceInstanceUpdateEvent);
    ResourceInstance updateResourceInstanceFriendlyName(Event<UpdateResourceInstanceNameCommand> resourceInstanceFriendlyNameCommandEvent);
    void deleteResourceInstance(Identifier<ResourceInstance> resourceInstanceId);

}
