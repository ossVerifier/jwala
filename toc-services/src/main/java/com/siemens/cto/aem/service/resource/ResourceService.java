package com.siemens.cto.aem.service.resource;

import com.siemens.cto.aem.common.request.resource.ResourceInstanceRequest;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.resource.ResourceInstance;
import com.siemens.cto.aem.common.domain.model.resource.ResourceType;
import com.siemens.cto.aem.common.domain.model.user.User;

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
     * @param metaDataFile the template meta data file written in JSON.
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
     * @param templateFile the file the contains the template
     * @param user the user calling this service
     */
    void createTemplate(String metaDataFile, String templateFile, User user);

    /**
     * Deletes a resource template.
     * @param name the template name (the actual name of the resource file when deployed e.g. context.xml)
     * @return the number of records deleted.
     */
    int removeTemplate(String name);
}
