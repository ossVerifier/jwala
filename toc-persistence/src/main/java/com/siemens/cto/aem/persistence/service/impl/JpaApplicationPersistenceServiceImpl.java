package com.siemens.cto.aem.persistence.service.impl;

import com.siemens.cto.aem.common.domain.model.app.Application;
import com.siemens.cto.aem.common.domain.model.event.Event;
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

import java.util.List;

public class JpaApplicationPersistenceServiceImpl implements ApplicationPersistenceService {

    private final ApplicationCrudService applicationCrudService;
    private final GroupCrudService groupCrudService;

    public JpaApplicationPersistenceServiceImpl(final ApplicationCrudService theAppCrudService,
                                                final GroupCrudService theGroupCrudService) {
        applicationCrudService = theAppCrudService;
        groupCrudService = theGroupCrudService;
    }

    @Override
    public Application createApplication(final Event<CreateApplicationRequest> anAppToCreate, final String appContextTemplate,
                                         final String roleMappingPropertiesTemplate, String appPropertiesTemplate) {
        JpaGroup jpaGroup = groupCrudService.getGroup(anAppToCreate.getRequest().getGroupId());
        final JpaApplication jpaApp = applicationCrudService.createApplication(anAppToCreate, jpaGroup);
        final int idx = jpaApp.getWebAppContext().lastIndexOf('/');
        final String resourceName = idx == -1 ? jpaApp.getWebAppContext() : jpaApp.getWebAppContext().substring(idx + 1);
        applicationCrudService.createConfigTemplate(jpaApp, resourceName + "RoleMapping.properties", roleMappingPropertiesTemplate, null);
        applicationCrudService.createConfigTemplate(jpaApp, resourceName + ".properties", appPropertiesTemplate, null);
        if (jpaGroup.getJvms() != null) {
            for (JpaJvm jvm : jpaGroup.getJvms()) {
                applicationCrudService.createConfigTemplate(jpaApp, resourceName + ".xml", appContextTemplate, jvm);
            }
        }
        return JpaAppBuilder.appFrom(jpaApp);
    }

    @Override
    public Application updateApplication(Event<UpdateApplicationRequest> anAppToUpdate) {
        final JpaApplication jpaOriginal = applicationCrudService.getExisting(anAppToUpdate.getRequest().getId());
        final JpaGroup jpaGroup = groupCrudService.getGroup(anAppToUpdate.getRequest().getNewGroupId());
        final JpaApplication jpaApp = applicationCrudService.updateApplication(anAppToUpdate, jpaOriginal, jpaGroup);
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
        return applicationCrudService.getResourceTemplate(appName, resourceTemplateName, jvm);
    }

    @Override
    public Application updateWARPath(final Event<UploadWebArchiveRequest> anAppToUpdate, String warPath) {
        final UploadWebArchiveRequest command = anAppToUpdate.getRequest();
        final JpaApplication jpaOriginal = applicationCrudService.getExisting(command.getApplication().getId());
        jpaOriginal.setWarPath(warPath);
        jpaOriginal.setWarName(command.getFilename());
        return JpaAppBuilder.appFrom(jpaOriginal);
    }

    @Override
    public Application removeWARPath(final Event<RemoveWebArchiveRequest> anAppToUpdate) {
        final JpaApplication jpaOriginal = applicationCrudService.getExisting(anAppToUpdate.getRequest().getApplication().getId());
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
    public JpaApplicationConfigTemplate uploadAppTemplate(Event<UploadAppTemplateRequest> event) {
        return applicationCrudService.uploadAppTemplate(event);
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
    public Application getApplication(Identifier<Application> aApplicationId) {
        return applicationCrudService.getApplication(aApplicationId);
    }

    @Override
    public List<Application> getApplications() {
        return applicationCrudService.getApplications();
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
