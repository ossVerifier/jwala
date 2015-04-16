package com.siemens.cto.aem.service.app.impl;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.domain.model.app.Application;
import com.siemens.cto.aem.domain.model.app.CreateApplicationCommand;
import com.siemens.cto.aem.domain.model.app.RemoveWebArchiveCommand;
import com.siemens.cto.aem.domain.model.app.UpdateApplicationCommand;
import com.siemens.cto.aem.domain.model.app.UploadWebArchiveCommand;
import com.siemens.cto.aem.domain.model.audit.AuditEvent;
import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.aem.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.temporary.PaginationParameter;
import com.siemens.cto.aem.domain.model.temporary.User;
import com.siemens.cto.aem.persistence.dao.app.ApplicationDao;
import com.siemens.cto.aem.persistence.service.app.ApplicationPersistenceService;
import com.siemens.cto.aem.service.app.ApplicationService;
import com.siemens.cto.aem.service.app.PrivateApplicationService;
import com.siemens.cto.toc.files.RepositoryFileInformation;
import com.siemens.cto.toc.files.RepositoryFileInformation.Type;
import com.siemens.cto.toc.files.WebArchiveManager;

public class ApplicationServiceImpl implements ApplicationService {

    private final static Logger LOGGER = LoggerFactory.getLogger(ApplicationServiceImpl.class); 

    @Autowired
    private ApplicationDao applicationDao;    
    
    @Autowired
    private ApplicationPersistenceService applicationPersistenceService;
    
    @Autowired
    private WebArchiveManager webArchiveManager;

    @Autowired
    private PrivateApplicationService privateApplicationService;
    
    public ApplicationServiceImpl(ApplicationDao applicationDao, ApplicationPersistenceService applicationPersistenceService) {
        this.applicationDao = applicationDao;
        this.applicationPersistenceService = applicationPersistenceService;
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

    @Transactional
    @Override
    public void removeApplication(Identifier<Application> anAppIdToRemove, User user) {
        applicationPersistenceService.removeApplication(anAppIdToRemove);
    }

    /** Non-transactional entry point, utilizes {@link PrivateApplicationServiceImpl}*/
    @Override
    public Application uploadWebArchive(UploadWebArchiveCommand command, User user) {
        command.validateCommand();
        
        Event<UploadWebArchiveCommand> event = Event.create(command, AuditEvent.now(user));
        
        return privateApplicationService.uploadWebArchiveUpdateDB(event, privateApplicationService.uploadWebArchiveData(event));
    }

    @Transactional    
    @Override
    public Application deleteWebArchive(Identifier<Application> appId, User user) {

        Application app = this.getApplication(appId);           
        RemoveWebArchiveCommand rwac = new RemoveWebArchiveCommand(app);
        Event<RemoveWebArchiveCommand> event = Event.create(rwac, AuditEvent.now(user));
        
        RepositoryFileInformation result = RepositoryFileInformation.none();
        
        try {
            result = webArchiveManager.remove(event);
            LOGGER.info("Archive Delete: " + result.toString());
        } catch (IOException e) {
            throw new BadRequestException(AemFaultType.INVALID_APPLICATION_WAR, "Error deleting archive.", e);
        }
        
        if(result.getType() == Type.DELETED) {
            return applicationPersistenceService.removeWARPath(event);
        }
        else {
            throw new BadRequestException(AemFaultType.INVALID_APPLICATION_WAR, "Archive not found to delete.");
        }
    }
}
