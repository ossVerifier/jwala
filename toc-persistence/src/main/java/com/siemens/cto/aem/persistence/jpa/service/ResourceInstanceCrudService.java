package com.siemens.cto.aem.persistence.jpa.service;

import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.resource.ResourceInstance;
import com.siemens.cto.aem.common.request.resource.ResourceInstanceRequest;
import com.siemens.cto.aem.persistence.jpa.domain.JpaResourceInstance;

import java.util.List;

/**
 * Created by z003e5zv on 3/25/2015.
 */
public interface ResourceInstanceCrudService extends CrudService<JpaResourceInstance> {

    JpaResourceInstance updateResourceInstanceAttributes(final Identifier<ResourceInstance> resourceInstanceId, final ResourceInstanceRequest resourceInstanceRequest);

    JpaResourceInstance updateResourceInstanceName(final Identifier<ResourceInstance> resourceInstanceId, final ResourceInstanceRequest resourceInstanceRequest);

    JpaResourceInstance getResourceInstance(final Identifier<ResourceInstance> resourceInstanceId);

    List<JpaResourceInstance> getResourceInstancesByGroupId(final Long groupId);

    JpaResourceInstance getResourceInstanceByGroupIdAndName(final Long groupId, String name);

    List<JpaResourceInstance> getResourceInstancesByGroupIdAndResourceTypeName(final Long groupId, final String typeName);
}
