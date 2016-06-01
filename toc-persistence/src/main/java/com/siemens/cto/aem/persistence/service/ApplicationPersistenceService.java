package com.siemens.cto.aem.persistence.service;

import com.siemens.cto.aem.common.domain.model.app.Application;
import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.jvm.Jvm;
import com.siemens.cto.aem.common.request.app.*;
import com.siemens.cto.aem.persistence.jpa.domain.JpaApplicationConfigTemplate;
import com.siemens.cto.aem.persistence.jpa.domain.JpaJvm;

import java.util.List;

public interface ApplicationPersistenceService {

    Application createApplication(CreateApplicationRequest createApplicationRequest, String appContextTemplate,
                                  String roleMappingPropertiesTemplate, String appPropertiesTemplate);

    Application updateApplication(final UpdateApplicationRequest updateApplicationRequest);

    Application updateWARPath(UploadWebArchiveRequest uploadWebArchiveRequest, String warPath);

    Application removeWarPath(RemoveWebArchiveRequest removeWebArchiveRequest);

    void removeApplication(final Identifier<Application> anAppToRemove);

    List<String> getResourceTemplateNames(final String appName);

    String getResourceTemplate(final String appName, final String resourceTemplateName, final String jvmName, final String groupName);

    String updateResourceTemplate(final String appName, final String resourceTemplateName, final String template, final String jvmName, final String groupName);

    JpaApplicationConfigTemplate uploadAppTemplate(UploadAppTemplateRequest uploadAppTemplateRequest, JpaJvm jpaJvm);

    List<Application> getApplications();

    Application getApplication(Identifier<Application> aApplicationId);

    Application getApplication(String name);

    List<Application> findApplicationsBelongingTo(Identifier<Group> groupId);

    List<Application> findApplicationsBelongingTo(String groupName);

    List<Application> findApplicationsBelongingToJvm(Identifier<Jvm> jvmId);

    Application findApplication(String appName, String groupName, String jvmName);

    void createApplicationConfigTemplateForJvm(String jvmName, Application app, Identifier<Group> groupId, String metaData,
                                               String resourceTypeTemplate);

    int removeTemplate(String name);

    String getMetaData(String appName, String jvmName, String groupName, String templateName);

    /**
     * Check if the application contains the resource name.
     * @param groupName the name of the group, which contains the webapp
     * @param appName the name of the webapp, which contains the resource file
     * @param fileName the filename of the resource
     * @return true if the file already exists, else returns false
     */
    boolean checkAppResourceFileName(String groupName, String appName, String fileName);

}
