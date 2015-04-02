package com.siemens.cto.aem.persistence.service.resource.impl;

import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.resource.ResourceInstance;
import com.siemens.cto.aem.domain.model.resource.command.CreateResourceInstanceCommand;
import com.siemens.cto.aem.domain.model.resource.command.UpdateResourceInstanceAttributesCommand;
import com.siemens.cto.aem.domain.model.resource.command.UpdateResourceInstanceNameCommand;
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
    public ResourceInstance createResourceInstance(Event<CreateResourceInstanceCommand> resourceInstanceCreateEvent) {
        return parseFromJpa(this.resourceInstanceCrudService.createResourceInstance(resourceInstanceCreateEvent));
    }

    @Override
    public List<ResourceInstance> getResourceInstancesByGroupId(Long groupId) {
        return this.parseFromJpa(this.resourceInstanceCrudService.getResourceInstancesByGroupId(groupId));
    }

    @Override
    public ResourceInstance getResourceInstanceByGroupIdAndName(Long groupId, String name) {
        return this.parseFromJpa(this.resourceInstanceCrudService.getResourceInstanceByGroupIdAndName(groupId, name));
    }

    @Override
    public List<ResourceInstance> getResourceInstancesByGroupIdAndResourceTypeName(Long groupId, String resourceTypeName) {
        return this.parseFromJpa(this.resourceInstanceCrudService.getResourceInstancesByGroupIdAndResourceTypeName(groupId, resourceTypeName));
    }

    @Override
    public ResourceInstance updateResourceInstanceAttributes(Event<UpdateResourceInstanceAttributesCommand> resourceInstanceUpdateEvent) {
        return parseFromJpa(this.resourceInstanceCrudService.updateResourceInstanceAttributes(resourceInstanceUpdateEvent));
    }

    @Override
    public ResourceInstance updateResourceInstanceFriendlyName(Event<UpdateResourceInstanceNameCommand> resourceInstanceNameCommandEvent) {
        return parseFromJpa(this.resourceInstanceCrudService.updateResourceInstanceName(resourceInstanceNameCommandEvent));
    }

    @Override
    public ResourceInstance getResourceInstance(Identifier<ResourceInstance> resourceInstanceId) {
        return parseFromJpa(this.resourceInstanceCrudService.getResourceInstance(resourceInstanceId));
    }

    @Override
    public void deleteResourceInstance(Identifier<ResourceInstance> resourceInstanceId) {
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
