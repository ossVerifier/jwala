package com.siemens.cto.aem.persistence.service;

import com.siemens.cto.aem.common.request.resource.ResourceInstanceRequest;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.resource.ResourceInstance;

import java.util.List;

/**
 * Created by z003e5zv on 3/25/2015.
 */
public interface ResourcePersistenceService {

    ResourceInstance createResourceInstance(ResourceInstanceRequest resourceInstanceRequest);
    List<ResourceInstance> getResourceInstancesByGroupId(final Long groupId);
    ResourceInstance getResourceInstanceByGroupIdAndName(final Long groupId, final String name);
    List<ResourceInstance> getResourceInstancesByGroupIdAndResourceTypeName(final Long groupId, final String typeName);
    ResourceInstance getResourceInstance(final Identifier<ResourceInstance> resourceInstanceId);
    ResourceInstance updateResourceInstance(ResourceInstance resourceInstance, final ResourceInstanceRequest resourceInstanceRequest);
    void deleteResourceInstance(final Identifier<ResourceInstance> resourceInstanceId);
    void deleteResources(final String groupName, final List<String> resourceNames);

    /**
     * Get's an application's resource names.
     * @param groupName the group where the application belongs to
     * @param appName the application name
     * @return list of resource names
     */
    List<String> getApplicationResourceNames(String groupName, String appName);

    /**
     * Gets an application's resource template.
     * @param groupName the group the application belongs to
     * @param appName the application name
     * @param templateName the template name
     * @return the template
     */
    String getAppTemplate(String groupName, String appName, String templateName);
}
