package com.siemens.cto.aem.persistence.service.app.impl;

import com.siemens.cto.aem.domain.model.app.*;
import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.aem.domain.model.id.Identifier;
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
    public Application createApplication(final Event<CreateApplicationCommand> anAppToCreate, final String appContextTemplate) {
        JpaGroup jpaGroup = groupCrudService.getGroup(anAppToCreate.getCommand().getGroupId());
        final JpaApplication jpaApp = applicationCrudService.createApplication(anAppToCreate, jpaGroup);
        final int idx = jpaApp.getWebAppContext().lastIndexOf('/');
        final String resourceName = idx == -1 ? jpaApp.getWebAppContext() : jpaApp.getWebAppContext().substring(idx + 1);
        applicationCrudService.createConfigTemplate(jpaApp, resourceName + ".xml", appContextTemplate);
        return JpaAppBuilder.appFrom(jpaApp);
    }

    @Override
    public Application updateApplication(Event<UpdateApplicationCommand> anAppToUpdate) {
        final JpaApplication jpaOriginal = applicationCrudService.getExisting(anAppToUpdate.getCommand().getId());
        final JpaGroup jpaGroup = groupCrudService.getGroup(anAppToUpdate.getCommand().getNewGroupId());
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
    public Application updateWARPath(final Event<UploadWebArchiveCommand> anAppToUpdate, String warPath) {
        final JpaApplication jpaOriginal = applicationCrudService.getExisting(anAppToUpdate.getCommand().getApplication().getId());
        jpaOriginal.setWarPath(warPath);
        return JpaAppBuilder.appFrom(jpaOriginal);
    }

    @Override
    public Application removeWARPath(final Event<RemoveWebArchiveCommand> anAppToUpdate) {
        final JpaApplication jpaOriginal = applicationCrudService.getExisting(anAppToUpdate.getCommand().getApplication().getId());
        jpaOriginal.setWarPath(null);
        return JpaAppBuilder.appFrom(jpaOriginal);
    }

    @Override
    public String updateResourceTemplate(final String appName, final String resourceTemplateName, final String template) {
        applicationCrudService.updateResourceTemplate(appName, resourceTemplateName, template);
        return applicationCrudService.getResourceTemplate(appName, resourceTemplateName);
    }

    @Override
    public JpaApplicationConfigTemplate uploadAppTemplate(Event<UploadAppTemplateCommand> event) {
        return applicationCrudService.uploadAppTemplate(event);
    }

}
