package com.siemens.cto.aem.persistence.service;

import com.siemens.cto.aem.common.domain.model.resource.EntityType;
import com.siemens.cto.aem.persistence.jpa.domain.resource.config.template.JpaResourceConfigTemplate;

import java.io.InputStream;
import java.util.List;

/**
 * Created by z003e5zv on 3/25/2015.
 */
public interface ResourcePersistenceService {

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

    JpaResourceConfigTemplate createResource(Long entityId, Long groupId, Long appId, EntityType extProperties, String fileName, InputStream propertiesFileIn);
}
