package com.siemens.cto.aem.persistence.jpa.service.resource;

import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.resource.ResourceInstance;
import com.siemens.cto.aem.domain.model.resource.command.CreateResourceInstanceCommand;
import com.siemens.cto.aem.domain.model.resource.command.UpdateResourceInstanceAttributesCommand;
import com.siemens.cto.aem.domain.model.resource.command.UpdateResourceInstanceNameCommand;
import com.siemens.cto.aem.persistence.jpa.domain.JpaResourceInstance;

import java.util.List;

/**
 * Created by z003e5zv on 3/25/2015.
 */
public interface ResourceInstanceCrudService {

    JpaResourceInstance createResourceInstance(Event<CreateResourceInstanceCommand> createResourceInstanceCommandEvent);
    JpaResourceInstance updateResourceInstanceAttributes(Event<UpdateResourceInstanceAttributesCommand> updateResourceInstanceCommandEvent);
    JpaResourceInstance updateResourceInstanceName(Event<UpdateResourceInstanceNameCommand> updateResourceInstanceNameCommandEvent);
    JpaResourceInstance getResourceInstance(Identifier<ResourceInstance> resourceInstanceId);
    List<JpaResourceInstance> getResourceInstancesByGroupId(Long groupId);
    JpaResourceInstance getResourceInstanceByGroupIdAndName(Long groupId, String name);
    List<JpaResourceInstance> getResourceInstancesByGroupIdAndResourceTypeName(Long groupId, String typeName);
    void deleteResourceInstance(Identifier<ResourceInstance> resourceInstanceId);
}
