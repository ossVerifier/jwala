package com.siemens.cto.aem.persistence.service;

import com.siemens.cto.aem.persistence.jpa.domain.JpaApplicationConfigTemplate;
import com.siemens.cto.aem.persistence.jpa.domain.resource.config.template.*;

import java.util.List;

/**
 * DAO Contract for resource related methods.
 *
 * Created by JC043760 on 6/3/2016.
 */
public interface ResourceDao {

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
     * @param appName the application name
     * @param groupName the group name
     * @param templateName the template name
     * @return the number of resources deleted
     */
    int deleteGroupLevelAppResource(String appName, String groupName, String templateName);

    /**
     * Delete web server resources.
     * @param templateNameList list of template names
     * @param webServerName the web server name
     * @return the number of resources deleted
     */
    int deleteWebServerResources(List<String> templateNameList, String webServerName);

    /**
     * Delete group level web server resources.
     * @param templateNameList the template name list
     * @param groupName the group name
     * @return the number of resources deleted
     */
    int deleteGroupLevelWebServerResources(List<String> templateNameList, String groupName);

    /**
     * Delete JVM resources.
     * @param templateNameList the template name list
     * @param jvmName the JVM name
     * @return the number of resources deleted
     */
    int deleteJvmResources(List<String> templateNameList, String jvmName);

    /**
     * Delete group level JVM resources.
     * @param templateNameList the template name list
     * @param groupName the group name
     * @return the number of resources deleted
     */
    int deleteGroupLevelJvmResources(List<String> templateNameList, String groupName);

    /**
     * Delete application resources.
     * @param templateNameList the template name list
     * @param appName the application name
     * @param jvmName the jvm name
     * @return the number of resources deleted
     */
    int deleteAppResources(List<String> templateNameList, String appName, String jvmName);

    /**
     * Delete group level application resources.
     * @param appName the application name
     * @param groupName the group name
     * @param templateNameList the template name list
     * @return the number of resources deleted
     */
    int deleteGroupLevelAppResources(String appName, String groupName, List<String> templateNameList);

    /**
     * Get web server resource
     * @param resourceName the resource name
     * @param webServerName the web server name
     * @return {@link JpaWebServerConfigTemplate}
     */
    JpaWebServerConfigTemplate getWebServerResource(String resourceName, String webServerName);

    /**
     * Get a JVM resource
     * @param resourceName the resource name
     * @param jvmName the JVM name
     * @return {@link JpaJvmConfigTemplate}
     */
    JpaJvmConfigTemplate getJvmResource(String resourceName, String jvmName);

    /**
     * Get an application resource
     * @param resourceName the resource name
     * @param appName the application name
     * @return {@link JpaApplicationConfigTemplate}
     */
    JpaApplicationConfigTemplate getAppResource(String resourceName, String appName, String jvmName);

    /**
     * Get a group level web server resource
     * @param resourceName the resource name
     * @param groupName the group name
     * @return {@link JpaGroupWebServerConfigTemplate}
     */
    JpaGroupWebServerConfigTemplate getGroupLevelWebServerResource(String resourceName, String groupName);

    /**
     * Get a group level JVM resource
     * @param resourceName resource name
     * @param groupName group name
     * @return {@link JpaGroupJvmConfigTemplate}
     */
    JpaGroupJvmConfigTemplate getGroupLevelJvmResource(String resourceName, String groupName);

    /**
     * Get a group level web application resource
     * @param resourceName resource name
     * @param appName the application name
     *@param groupName group name  @return {@link JpaGroupAppConfigTemplate}
     */
    JpaGroupAppConfigTemplate getGroupLevelAppResource(String resourceName, String appName, String groupName);

    JpaResourceConfigTemplate getExternalPropertiesResource(String resourceName);
}
