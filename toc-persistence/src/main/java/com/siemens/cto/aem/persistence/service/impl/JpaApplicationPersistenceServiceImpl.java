package com.siemens.cto.aem.persistence.service.impl;

import com.siemens.cto.aem.common.domain.model.app.Application;
import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.jvm.Jvm;
import com.siemens.cto.aem.common.request.app.*;
import com.siemens.cto.aem.persistence.jpa.domain.JpaApplication;
import com.siemens.cto.aem.persistence.jpa.domain.JpaApplicationConfigTemplate;
import com.siemens.cto.aem.persistence.jpa.domain.JpaGroup;
import com.siemens.cto.aem.persistence.jpa.domain.JpaJvm;
import com.siemens.cto.aem.persistence.jpa.domain.builder.JpaAppBuilder;
import com.siemens.cto.aem.persistence.jpa.service.ApplicationCrudService;
import com.siemens.cto.aem.persistence.jpa.service.GroupCrudService;
import com.siemens.cto.aem.persistence.service.ApplicationPersistenceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class JpaApplicationPersistenceServiceImpl implements ApplicationPersistenceService {

    private final static Logger LOGGER = LoggerFactory.getLogger(ApplicationPersistenceService.class);

    private final ApplicationCrudService applicationCrudService;
    private final GroupCrudService groupCrudService;

    public JpaApplicationPersistenceServiceImpl(final ApplicationCrudService theAppCrudService,
                                                final GroupCrudService theGroupCrudService) {
        applicationCrudService = theAppCrudService;
        groupCrudService = theGroupCrudService;
    }

    @Override
    public Application createApplication(CreateApplicationRequest createApplicationRequest, final String appContextTemplate,
                                         final String roleMappingPropertiesTemplate, String appPropertiesTemplate) {
        JpaGroup jpaGroup = groupCrudService.getGroup(createApplicationRequest.getGroupId());
        final JpaApplication jpaApp = applicationCrudService.createApplication(createApplicationRequest, jpaGroup);

        //TODO do not propagate the default application templates since they are healthcheck specific
//        final int idx = jpaApp.getWebAppContext().lastIndexOf('/');
//        final String resourceName = idx == -1 ? jpaApp.getWebAppContext() : jpaApp.getWebAppContext().substring(idx + 1);
//
//        if (roleMappingPropertiesTemplate != null) {
//            applicationCrudService.createConfigTemplate(jpaApp, resourceName + "RoleMapping.properties",
//                    roleMappingPropertiesTemplate, null);
//        } else {
//            LOGGER.warn("Role mapping properties template is null!");
//        }
//
//        if (appPropertiesTemplate != null) {
//            applicationCrudService.createConfigTemplate(jpaApp, resourceName + ".properties", appPropertiesTemplate, null);
//        } else {
//            LOGGER.warn("Application properties template is null!");
//        }
//
//        if (appContextTemplate != null) {
//            if (jpaGroup.getJvms() != null) {
//                for (JpaJvm jvm : jpaGroup.getJvms()) {
//                    applicationCrudService.createConfigTemplate(jpaApp, resourceName + ".xml", appContextTemplate, jvm);
//                }
//            }
//        } else {
//            LOGGER.warn("Application context template is null!");
//        }

        return JpaAppBuilder.appFrom(jpaApp);
    }

    @Override
    public Application updateApplication(UpdateApplicationRequest updateApplicationRequest) {
        final JpaApplication jpaOriginal = applicationCrudService.getExisting(updateApplicationRequest.getId());
        final JpaGroup jpaGroup = groupCrudService.getGroup(updateApplicationRequest.getNewGroupId());
        final JpaApplication jpaApp = applicationCrudService.updateApplication(updateApplicationRequest, jpaOriginal, jpaGroup);
        return JpaAppBuilder.appFrom(jpaApp);
    }

    @Override
    public void removeApplication(Identifier<Application> anAppId) {
        applicationCrudService.removeApplication(anAppId);
    }

    @Override
    public List<String> getResourceTemplateNames(final String appName) {
        return applicationCrudService.getResourceTemplateNames(appName);
    }

    @Override
    public String getResourceTemplate(final String appName, final String resourceTemplateName, final String jvmName, final String groupName) {
        JpaJvm jvm = getJpaJvmForAppXml(resourceTemplateName, jvmName, groupName);
        if (resourceTemplateName.endsWith(".xml")) {
            return applicationCrudService.getResourceTemplate(appName, resourceTemplateName, jvmName, groupName);
        } else {
            return applicationCrudService.getResourceTemplate(appName, resourceTemplateName, null);
        }
    }

    @Override
    public Application updateWARPath(UploadWebArchiveRequest uploadWebArchiveRequest, String warPath) {
        final JpaApplication jpaOriginal = applicationCrudService.getExisting(uploadWebArchiveRequest.getApplication().getId());
        jpaOriginal.setWarPath(warPath);
        jpaOriginal.setWarName(uploadWebArchiveRequest.getFilename());
        return JpaAppBuilder.appFrom(jpaOriginal);
    }

    @Override
    public Application removeWarPath(RemoveWebArchiveRequest removeWebArchiveRequest) {
        final JpaApplication jpaOriginal = applicationCrudService.getExisting(removeWebArchiveRequest.getApplication().getId());
        jpaOriginal.setWarPath(null);
        return JpaAppBuilder.appFrom(jpaOriginal);
    }

    @Override
    public String updateResourceTemplate(final String appName, final String resourceTemplateName, final String template, final String jvmName, final String groupName) {
        JpaJvm jvm = getJpaJvmForAppXml(resourceTemplateName, jvmName, groupName);
        applicationCrudService.updateResourceTemplate(appName, resourceTemplateName, template, jvm);
        return applicationCrudService.getResourceTemplate(appName, resourceTemplateName, jvm);
    }

    @Override
    public JpaApplicationConfigTemplate uploadAppTemplate(UploadAppTemplateRequest uploadAppTemplateRequest, JpaJvm jpaJvm) {
        return applicationCrudService.uploadAppTemplate(uploadAppTemplateRequest, jpaJvm);
    }

    @Override
    public Application findApplication(String appName, String groupName, String jvmName) {
        return applicationCrudService.findApplication(appName, groupName, jvmName);
    }

    @Override
    public List<Application> findApplicationsBelongingToJvm(Identifier<Jvm> jvmId) {
        return applicationCrudService.findApplicationsBelongingToJvm(jvmId);
    }

    @Override
    public List<Application> findApplicationsBelongingTo(Identifier<Group> groupId) {
        return applicationCrudService.findApplicationsBelongingTo(groupId);
    }

    @Override
    public List<Application> findApplicationsBelongingTo(final String groupName) {
        return applicationCrudService.findApplicationsBelongingTo(groupName);
    }

    @Override
    public Application getApplication(Identifier<Application> aApplicationId) {
        return applicationCrudService.getApplication(aApplicationId);
    }

    @Override
    public Application getApplication(final String name) {
        return applicationCrudService.getApplication(name);
    }

    @Override
    public List<Application> getApplications() {
        return applicationCrudService.getApplications();
    }

    @Override
    public void createApplicationConfigTemplateForJvm(String jvmName, Application app, Identifier<Group> groupId, String appContextTemplate) {
        final String webAppContext = app.getWebAppContext();
        final int idx = webAppContext.lastIndexOf('/');
        final String resourceName = idx == -1 ? webAppContext : webAppContext.substring(idx + 1);
        LOGGER.info("Using resource name {} and file manager app content {}", resourceName, appContextTemplate != null);
        if (appContextTemplate != null) {
            JpaGroup jpaGroup = groupCrudService.getGroup(groupId);
            if (jpaGroup.getJvms() != null) {
                for (JpaJvm jvm : jpaGroup.getJvms()) {
                    if (jvm.getName().equals(jvmName)) {
                        applicationCrudService.createConfigTemplate(applicationCrudService.getExisting(app.getId()), resourceName + ".xml", appContextTemplate, jvm);
                        LOGGER.info("Creation of config template {} SUCCEEDED for {}", resourceName, jvm.getName());
                    }
                }
            }
        }
    }

    @Override
    public int removeTemplate(final String name) {
        return applicationCrudService.removeTemplate(name);
    }

    private JpaJvm getJpaJvmForAppXml(String resourceTemplateName, String jvmName, String groupName) {
        // the application context xml is created for each JVM, unlike the the properties and RoleMapping.properties files
        // so when we retrieve or update the template for the context xml make sure we send in a specific JVM
        JpaJvm jvm = null;
        if (resourceTemplateName.endsWith(".xml")) {
            jvm = getJpaJvmByName(groupCrudService.getGroup(groupName), jvmName);
        }
        return jvm;
    }

    private JpaJvm getJpaJvmByName(JpaGroup group, String jvmNameToFind) {
        List<JpaJvm> jvmList = group.getJvms();
        if (jvmList != null) {
            for (JpaJvm jvm : jvmList) {
                if (jvm.getName().equals(jvmNameToFind)) {
                    return jvm;
                }
            }
        }
        return null;
    }

}
