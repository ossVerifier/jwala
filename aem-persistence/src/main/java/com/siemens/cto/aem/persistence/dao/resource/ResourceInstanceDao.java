package com.siemens.cto.aem.persistence.dao.resource;

import com.siemens.cto.aem.common.exception.NotFoundException;
import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.aem.domain.model.group.CreateGroupCommand;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.group.UpdateGroupCommand;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.resource.ResourceInstance;
import com.siemens.cto.aem.domain.model.resource.command.CreateResourceInstanceCommand;
import com.siemens.cto.aem.domain.model.resource.command.UpdateResourceInstanceCommand;
import com.siemens.cto.aem.domain.model.temporary.PaginationParameter;

import java.util.List;

/**
 * Created by z003e5zv on 3/13/2015.
 */
public interface ResourceInstanceDao {

    ResourceInstance createResourceInstance(final Event<CreateResourceInstanceCommand> aResourceInstanceToCreate);

    ResourceInstance updateResourceInstance(final Event<UpdateResourceInstanceCommand> aResourceInstanceToUpdate);

    ResourceInstance getResourceInstance(final Identifier<ResourceInstance> aResourceInstanceId) throws NotFoundException;

    void removeResourceInstance(final Identifier<ResourceInstance> aResourceInstanceId);
}
