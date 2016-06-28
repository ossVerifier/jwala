package com.siemens.cto.aem.persistence.service;

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
}
