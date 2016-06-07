package com.siemens.cto.aem.service.resource;

import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.resource.EntityType;
import com.siemens.cto.aem.common.domain.model.resource.ResourceGroup;
import com.siemens.cto.aem.common.domain.model.resource.ResourceInstance;
import com.siemens.cto.aem.common.domain.model.resource.ResourceTemplateMetaData;
import com.siemens.cto.aem.common.domain.model.user.User;
import com.siemens.cto.aem.common.request.resource.ResourceInstanceRequest;
import com.siemens.cto.aem.service.resource.impl.CreateResourceTemplateApplicationResponseWrapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;


public interface ResourceService {

    ResourceInstance getResourceInstance(final Identifier<ResourceInstance> aResourceInstanceId);

    List<ResourceInstance> getResourceInstancesByGroupName(final String groupName);

    ResourceInstance getResourceInstanceByGroupNameAndName(final String groupName, final String name);

    List<ResourceInstance> getResourceInstancesByGroupNameAndResourceTypeName(final String groupName, final String resourceTypeName);

    ResourceInstance createResourceInstance(final ResourceInstanceRequest createResourceInstanceCommand, final User creatingUser);

    ResourceInstance updateResourceInstance(final String groupName, final String name, final ResourceInstanceRequest updateResourceInstanceAttributesCommand, final User updatingUser);

    void deleteResourceInstance(final String name, final String groupName);

    void deleteResources(final String groupName, final List<String> resourceNames);
    
    String  encryptUsingPlatformBean(String cleartext);

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
     * @param targetName
     * @param user
     */
    @Deprecated
    CreateResourceTemplateApplicationResponseWrapper createTemplate(InputStream metaDataInputStream, InputStream templateData, String targetName, User user);

    CreateResourceTemplateApplicationResponseWrapper createJvmResource(ResourceTemplateMetaData metaData,
                                                                       InputStream templateData,
                                                                       String jvmName);

    /**
     * Create the JVM template in the db and in the templates path for all the JVMs.
     *  @param metaData     the data that describes the template, please see {@link ResourceTemplateMetaData}
     * @param templateData the template content/data
     */
    CreateResourceTemplateApplicationResponseWrapper createGroupLevelJvmResource(ResourceTemplateMetaData metaData,
                                                                                 InputStream templateData, String groupName) throws IOException;

    /**
     * Create web server resource.
     * @param metaData the data that describes the resource, please see {@link ResourceTemplateMetaData}
     * @param templateData the template content/data
     * @param webServerName identifies the web server to which the template belongs to
     * @param user the user responsible for executing this service
     */
    CreateResourceTemplateApplicationResponseWrapper createWebServerResource(ResourceTemplateMetaData metaData,
                                                                             InputStream templateData,
                                                                             String webServerName,
                                                                             User user);

    /**
     * Create the web server template in the db and in the templates path for all the web servers.
     * @param metaData the data that describes the template, please see {@link ResourceTemplateMetaData}
     * @param templateData the template content/data
     * @param user the user
     */
    CreateResourceTemplateApplicationResponseWrapper createGroupLevelWebServersResource(ResourceTemplateMetaData metaData,
                                                                                        InputStream templateData,
                                                                                        String groupName,
                                                                                        User user) throws IOException;

    /**
     * Create application resource.
     *
     * @param metaData the data that describes the template, please see {@link ResourceTemplateMetaData}
     * @param templateData the template content/data
     * @param jvmName the name of the JVM to associate the resource to
     * @param appName the name of the application to associate the resource to
     */
    CreateResourceTemplateApplicationResponseWrapper createAppResource(ResourceTemplateMetaData metaData,
                                                                       InputStream templateData,
                                                                       String jvmName,
                                                                       String appName);

    /**
     * Create group level application resource.
     *
     * @param metaData the data that describes the template, please see {@link ResourceTemplateMetaData}
     * @param templateData the template content/data
     * @param targetAppName the name of the application to associate the resource to
     */
    // TODO: Specify the group as well!
    CreateResourceTemplateApplicationResponseWrapper createGroupedLevelAppResource(ResourceTemplateMetaData metaData,
                                                                                   InputStream templateData,
                                                                                   String targetAppName) throws IOException;

    /**
     * Deletes a resource template of a specific group and entity type (e.g. group = Group1, entity type = GROUPED_JVMS)
     * @param groupName the group name
     * @param entityType the entity type {@link EntityType}
     * @param templateNames comma separated names of templates to delete e.g. server.xml, context.xml (user can specify one template name as well)
     * @return the number of records deleted.
     */
    @Deprecated
    int removeTemplate(String groupName, EntityType entityType, String templateNames);

    /**
     * Generates the ResourceGroup class object, which contains all the jvms, webapps, webservers and groups information.
     * @return the ResourceGroup object
     */
    ResourceGroup generateResourceGroup();

    /**
     * Maps data to the template specified by the template parameter.
     * @param template the template parameter.
     * @param resourceGroup resourcegroup object
     * @param selectedValue the selectedvalue
     * @return the generated resource file string
     */
    <T> String generateResourceFile(final String template, final ResourceGroup resourceGroup, T selectedValue);

    /**
     * Get an application's resource names.
     * @param groupName the group where the app belongs to
     * @param appName the application name
     * @return List of resource names.
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

    String checkFileExists(String groupName, String jvmName, String webappName, String webserverName, String fileName);

    /**
     * Delete a web server resource.
     * @param templateName the template name
     * @param webServerName the web server name
     * @return the number of resources deleted
     */
    int deleteWebServerResource(String templateName, String webServerName);

    /**
     * Delete a group level web server resource.
     * @param templateName the template name
     * @param groupName the group name
     * @return the number of resources deleted
     */
    int deleteGroupLevelWebServerResource(String templateName, String groupName);

    /**
     * Delete a JVM resource.
     * @param templateName the template name
     * @param jvmName the JVM name
     * @return the number of resources deleted
     */
    int deleteJvmResource(String templateName, String jvmName);

    /**
     * Delete a group level JVM resource.
     * @param templateName the template name
     * @param groupName the group name
     * @return the number of resources deleted
     */
    int deleteGroupLevelJvmResource(String templateName, String groupName);

    /**
     * Delete an application resource
     * @param templateName the template name
     * @param appName the application name
     * @param jvmName the jvm name
     * @return the number of resources deleted
     */
    int deleteAppResource(String templateName, String appName, String jvmName);

    /**
     * Delete a group level application resource.
     * @param templateName the template name
     * @param groupName the application name
     * @return the number of resources deleted
     */
    int deleteGroupLevelAppResource(String templateName, String groupName);
}
