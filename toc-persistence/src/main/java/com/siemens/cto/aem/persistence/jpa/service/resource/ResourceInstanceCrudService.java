package com.siemens.cto.aem.persistence.jpa.service.resource;

import com.siemens.cto.aem.common.domain.model.event.Event;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.resource.ResourceInstance;
import com.siemens.cto.aem.common.request.resource.ResourceInstanceRequest;
import com.siemens.cto.aem.persistence.jpa.domain.JpaResourceInstance;

import java.util.List;

/**
 * Created by z003e5zv on 3/25/2015.
 */
public interface ResourceInstanceCrudService {

    JpaResourceInstance createResourceInstance(final Event<ResourceInstanceRequest> createResourceInstanceCommandEvent);
    JpaResourceInstance updateResourceInstanceAttributes(final Identifier<ResourceInstance> resourceInstanceId, final Event<ResourceInstanceRequest> updateResourceInstanceCommandEvent);
    JpaResourceInstance updateResourceInstanceName(final Identifier<ResourceInstance> resourceInstanceId, final Event<ResourceInstanceRequest> updateResourceInstanceNameCommandEvent);
    JpaResourceInstance getResourceInstance(final Identifier<ResourceInstance> resourceInstanceId);
    List<JpaResourceInstance> getResourceInstancesByGroupId(final Long groupId);
    JpaResourceInstance getResourceInstanceByGroupIdAndName(final Long groupId, String name);
    List<JpaResourceInstance> getResourceInstancesByGroupIdAndResourceTypeName(final Long groupId, final String typeName);
    void deleteResourceInstance(final Identifier<ResourceInstance> resourceInstanceId);
}
