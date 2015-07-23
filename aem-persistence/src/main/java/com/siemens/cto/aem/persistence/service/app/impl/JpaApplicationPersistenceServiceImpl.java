package com.siemens.cto.aem.persistence.service.app.impl;

import com.siemens.cto.aem.domain.model.app.*;
import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.persistence.jpa.domain.JpaApplication;
import com.siemens.cto.aem.persistence.jpa.domain.JpaGroup;
import com.siemens.cto.aem.persistence.jpa.domain.builder.JpaAppBuilder;
import com.siemens.cto.aem.persistence.jpa.service.app.ApplicationCrudService;
import com.siemens.cto.aem.persistence.jpa.service.group.GroupCrudService;
import com.siemens.cto.aem.persistence.service.app.ApplicationPersistenceService;

public class JpaApplicationPersistenceServiceImpl implements ApplicationPersistenceService {

    private final ApplicationCrudService applicationCrudService;
    private final GroupCrudService groupCrudService;

    public JpaApplicationPersistenceServiceImpl(final ApplicationCrudService theAppCrudService,
            final GroupCrudService theGroupCrudService) {
        applicationCrudService = theAppCrudService;
        groupCrudService = theGroupCrudService;
    }

    @Override
    public Application createApplication(final Event<CreateApplicationCommand> anAppToCreate) {
        JpaGroup jpaGroup = groupCrudService.getGroup(anAppToCreate.getCommand().getGroupId());
        final JpaApplication jpaApp = applicationCrudService.createApplication(anAppToCreate, jpaGroup);
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

}
