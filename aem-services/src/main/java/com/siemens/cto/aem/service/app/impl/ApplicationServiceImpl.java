package com.siemens.cto.aem.service.app.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.siemens.cto.aem.domain.model.app.Application;
import com.siemens.cto.aem.domain.model.app.CreateApplicationCommand;
import com.siemens.cto.aem.domain.model.app.UpdateApplicationCommand;
import com.siemens.cto.aem.domain.model.audit.AuditEvent;
import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.temporary.PaginationParameter;
import com.siemens.cto.aem.domain.model.temporary.User;
import com.siemens.cto.aem.persistence.dao.app.ApplicationDao;
import com.siemens.cto.aem.persistence.service.app.ApplicationPersistenceService;
import com.siemens.cto.aem.service.app.ApplicationService;

public class ApplicationServiceImpl implements ApplicationService {

    @Autowired
    private ApplicationDao applicationDao;    
    
    @Autowired
    private ApplicationPersistenceService applicationPersistenceService;

    public ApplicationServiceImpl(ApplicationDao applicationDao) {
        this.applicationDao = applicationDao;
    }

    @Transactional(readOnly = true)
    @Override
    public Application getApplication(Identifier<Application> aApplicationId) {
        return applicationDao.getApplication(aApplicationId);
    }

    @Transactional(readOnly = true)
    @Override
    public List<Application> getApplications(PaginationParameter somePagination) {
        return applicationDao.getApplications(somePagination);
    }

    @Transactional(readOnly = true)
    @Override
    public List<Application> findApplications(Identifier<Group> groupId, PaginationParameter somePagination) {
        return applicationDao.findApplicationsBelongingTo(groupId, somePagination);
    }

    @Transactional(readOnly = true)
    @Override
    public List<Application> findApplicationsByJvmId(Identifier<Jvm> jvmId, PaginationParameter somePagination) {
        return applicationDao.findApplicationsBelongingToJvm(jvmId, somePagination);
    }

    @Transactional
    @Override
    public Application updateApplication(UpdateApplicationCommand anUpdateCommand, User anUpdatingUser) {
        anUpdateCommand.validateCommand();

        final Event<UpdateApplicationCommand> event = new Event<>(anUpdateCommand,
                                                          AuditEvent.now(anUpdatingUser));

        return applicationPersistenceService.updateApplication(event);
    }

    @Transactional
    @Override
    public Application createApplication(final CreateApplicationCommand aCreateAppCommand, 
                         final User aCreatingUser) {

        aCreateAppCommand.validateCommand();

        final Event<CreateApplicationCommand> event = new Event<>(aCreateAppCommand,
                                                          AuditEvent.now(aCreatingUser));

        return applicationPersistenceService.createApplication(event);
    }

    @Transactional( )
    @Override
    public void removeApplication(Identifier<Application> anAppIdToRemove, User user) {
        applicationPersistenceService.removeApplication(anAppIdToRemove);
    }
}
