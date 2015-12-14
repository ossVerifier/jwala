package com.siemens.cto.aem.persistence.service.app.impl;

import com.siemens.cto.aem.common.request.app.*;
import com.siemens.cto.aem.common.domain.model.app.*;
import com.siemens.cto.aem.common.domain.model.event.Event;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.persistence.jpa.domain.JpaApplication;
import com.siemens.cto.aem.persistence.jpa.domain.JpaApplicationConfigTemplate;
import com.siemens.cto.aem.persistence.jpa.domain.JpaGroup;
import com.siemens.cto.aem.persistence.jpa.domain.builder.JpaAppBuilder;
import com.siemens.cto.aem.persistence.jpa.service.app.ApplicationCrudService;
import com.siemens.cto.aem.persistence.jpa.service.group.GroupCrudService;
import com.siemens.cto.aem.persistence.service.app.ApplicationPersistenceService;

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
        applicationCrudService.createConfigTemplate(jpaApp, resourceName + ".xml", appContextTemplate);
        applicationCrudService.createConfigTemplate(jpaApp, resourceName + "RoleMapping.properties", roleMappingPropertiesTemplate);
        applicationCrudService.createConfigTemplate(jpaApp, resourceName + ".properties", appPropertiesTemplate);
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
    public String getResourceTemplate(final String appName, final String resourceTemplateName) {
        return applicationCrudService.getResourceTemplate(appName, resourceTemplateName);
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
    public String updateResourceTemplate(final String appName, final String resourceTemplateName, final String template) {
        applicationCrudService.updateResourceTemplate(appName, resourceTemplateName, template);
        return applicationCrudService.getResourceTemplate(appName, resourceTemplateName);
    }

    @Override
    public JpaApplicationConfigTemplate uploadAppTemplate(Event<UploadAppTemplateRequest> event) {
        return applicationCrudService.uploadAppTemplate(event);
    }

}
