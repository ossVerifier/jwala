package com.siemens.cto.aem.persistence.service.resource.impl;

import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.resource.ResourceInstance;
import com.siemens.cto.aem.domain.model.resource.command.ResourceInstanceCommand;
import com.siemens.cto.aem.domain.model.temporary.PaginationParameter;
import com.siemens.cto.aem.persistence.jpa.domain.JpaResourceInstance;
import com.siemens.cto.aem.persistence.jpa.domain.builder.JpaResourceInstanceBuilder;
import com.siemens.cto.aem.persistence.jpa.service.resource.ResourceInstanceCrudService;
import com.siemens.cto.aem.persistence.service.resource.ResourcePersistenceService;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by z003e5zv on 3/25/2015.
 */
public class JpaResourcePersistenceServiceImpl implements ResourcePersistenceService {

    private final ResourceInstanceCrudService resourceInstanceCrudService;

    public JpaResourcePersistenceServiceImpl(ResourceInstanceCrudService resourceInstanceCrudService) {
        this.resourceInstanceCrudService = resourceInstanceCrudService;
    }

    @Override
    public ResourceInstance createResourceInstance(final Event<ResourceInstanceCommand> resourceInstanceCreateEvent) {
        return parseFromJpa(this.resourceInstanceCrudService.createResourceInstance(resourceInstanceCreateEvent));
    }

    @Override
    public List<ResourceInstance> getResourceInstancesByGroupId(final Long groupId) {
        return this.parseFromJpa(this.resourceInstanceCrudService.getResourceInstancesByGroupId(groupId));
    }

    @Override
    public ResourceInstance getResourceInstanceByGroupIdAndName(final Long groupId, final String name) {
        return this.parseFromJpa(this.resourceInstanceCrudService.getResourceInstanceByGroupIdAndName(groupId, name));
    }

    @Override
    public List<ResourceInstance> getResourceInstancesByGroupIdAndResourceTypeName(final Long groupId, final String resourceTypeName) {
        return this.parseFromJpa(this.resourceInstanceCrudService.getResourceInstancesByGroupIdAndResourceTypeName(groupId, resourceTypeName));
    }

    @Override
    public ResourceInstance updateResourceInstance(ResourceInstance resourceInstance, final Event<ResourceInstanceCommand> resourceInstanceUpdateEvent) {
        if (resourceInstanceUpdateEvent.getCommand().getAttributes() != null) {
            this.resourceInstanceCrudService.updateResourceInstanceAttributes(resourceInstance.getResourceInstanceId(), resourceInstanceUpdateEvent);
        }
        else if (!resourceInstanceUpdateEvent.getCommand().getName().equals(resourceInstance.getName())){
            return parseFromJpa(this.resourceInstanceCrudService.updateResourceInstanceName(resourceInstance.getResourceInstanceId(), resourceInstanceUpdateEvent));
        }
        return parseFromJpa(this.resourceInstanceCrudService.getResourceInstance(resourceInstance.getResourceInstanceId()));
    }

    @Override
    public ResourceInstance getResourceInstance(final Identifier<ResourceInstance> resourceInstanceId) {
        return parseFromJpa(this.resourceInstanceCrudService.getResourceInstance(resourceInstanceId));
    }

    @Override
    public void deleteResourceInstance(final Identifier<ResourceInstance> resourceInstanceId) {
        this.resourceInstanceCrudService.deleteResourceInstance(resourceInstanceId);
    }

    private final List<ResourceInstance> parseFromJpa(List<JpaResourceInstance> jpaResourceInstances) {
        List<ResourceInstance> resourceInstances = new ArrayList<>();
        for (JpaResourceInstance resourceInstance: jpaResourceInstances) {
            JpaResourceInstanceBuilder builder = new JpaResourceInstanceBuilder(resourceInstance);
            resourceInstances.add(builder.build());
        }
        return resourceInstances;
    }
    private final ResourceInstance parseFromJpa(JpaResourceInstance jpaResourceInstance) {
        JpaResourceInstanceBuilder builder = new JpaResourceInstanceBuilder(jpaResourceInstance);
        return builder.build();
    }
}
