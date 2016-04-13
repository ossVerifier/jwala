package com.siemens.cto.aem.service.resource;

import com.siemens.cto.aem.common.domain.model.resource.EntityType;
import com.siemens.cto.aem.common.request.resource.ResourceInstanceRequest;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.resource.ResourceInstance;
import com.siemens.cto.aem.common.domain.model.resource.ResourceType;
import com.siemens.cto.aem.common.domain.model.user.User;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;


public interface ResourceService {

    Collection<ResourceType> getResourceTypes();

    ResourceInstance getResourceInstance(final Identifier<ResourceInstance> aResourceInstanceId);

    List<ResourceInstance> getResourceInstancesByGroupName(final String groupName);

    ResourceInstance getResourceInstanceByGroupNameAndName(final String groupName, final String name);

    String generateResourceInstanceFragment(final String groupName, final String name);

    String generateResourceInstanceFragment(String groupName, String resourceInstanceName, Map<String, String> mockedValues);

    List<ResourceInstance> getResourceInstancesByGroupNameAndResourceTypeName(final String groupName, final String resourceTypeName);

    ResourceInstance createResourceInstance(final ResourceInstanceRequest createResourceInstanceCommand, final User creatingUser);

    ResourceInstance updateResourceInstance(final String groupName, final String name, final ResourceInstanceRequest updateResourceInstanceAttributesCommand, final User updatingUser);

    void deleteResourceInstance(final String name, final String groupName);

    void deleteResources(final String groupName, final List<String> resourceNames);
    
    String  encryptUsingPlatformBean(String cleartext);

    String getTemplate(final String resourceTypeName);

    /**
     * Creates a template file and it's corresponding JSON meta data file.
     * A template file is used when generating the actual resource file what will be deployed with the application.
     * @param metaDataInputStream the template meta data in JSON.
     *                             example:
     *                                      {
     *                                          "name": "My Context XML",
     *                                          "templateName": "my-context.tpl",
     *                                          "contentType": "application/xml",
     *                                          "configFileName":"mycontext.xml",
     *                                          "relativeDir":"/conf",
     *                                          "entity": {
     *                                              "type": "jvm",
     *                                              "group": "HEALTH CHECK 4.0",
     *                                              "target": "CTO-N9SF-LTST-HEALTH-CHECK-4.0-USMLVV1CTO4900-2"
     *                                          }
     *                                      }
     * @param templateData the template data
     */
    CreatedTemplateWrapper createTemplate(InputStream metaDataInputStream, InputStream templateData);

    /**
     * Deletes a resource template.
     * @param name the template name (the actual name of the resource file when deployed e.g. context.xml)
     * @return the number of records deleted.
     */
    int removeTemplate(String name);

    /**
     * Deletes a resource template of a specific group and entity type (e.g. group = Group1, entity type = JVMS)
     * @param groupName the group name
     * @param entityType the entity type {@link EntityType}
     * @param templateNames comma separated names of templates to delete e.g. server.xml, context.xml (user can specify one template name as well)
     * @return the number of records deleted.
     */
    int removeTemplate(String groupName, EntityType entityType, String templateNames);

    /**
     * Deletes a resource template of a specific entity type and name (e.g. entity type = jvms, entity name = jvm1).
     * @param entityType {@link EntityType}
     * @param entityName the name of the entity
     * @param templateNames comma separated names of templates to delete e.g. server.xml, context.xml (user can specify one template name as well)
     * @return the number records deleted.
     */
    int removeTemplate(EntityType entityType, String entityName, String templateNames);
}
