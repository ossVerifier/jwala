package com.siemens.cto.aem.service.app.impl;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.domain.model.app.Application;
import com.siemens.cto.aem.domain.model.app.UploadWebArchiveCommand;
import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.aem.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.persistence.service.app.ApplicationPersistenceService;
import com.siemens.cto.aem.service.app.PrivateApplicationService;
import com.siemens.cto.toc.files.RepositoryAction;
import com.siemens.cto.toc.files.WebArchiveManager;

public class PrivateApplicationServiceImpl implements PrivateApplicationService {

    private final static Logger LOGGER = LoggerFactory.getLogger(PrivateApplicationServiceImpl.class); 

    @Autowired
    private ApplicationPersistenceService applicationPersistenceService;
    
    @Autowired
    private WebArchiveManager webArchiveManager;

    /**
     * Helper method - non-transactional upload.
     */
    @Override
    public RepositoryAction uploadWebArchiveData(final Event<UploadWebArchiveCommand> event) {
        UploadWebArchiveCommand command = event.getCommand();
        RepositoryAction result = null;
        
        try {
            result = webArchiveManager.store(event);
            LOGGER.info("Archive Upload: " + result.toString());
        } catch (IOException e) {
            throw new BadRequestException(AemFaultType.BAD_STREAM, "Error storing data");
        }
        
        Long bytes = result.getLength();
        
        if(command.getLength() != -1 && bytes != null && bytes != command.getLength()) {
            throw new BadRequestException(AemFaultType.BAD_STREAM, "Post-condition file length check failed");
        }

        return result;        
    }

    /**
     * Helper method - transactional update of database.
     */
    @Override
    @Transactional
    public Application uploadWebArchiveUpdateDB(final Event<UploadWebArchiveCommand> event, final RepositoryAction result) {
        
        return applicationPersistenceService.updateWARPath(event, result.getPath().toAbsolutePath().toString());

    }

}
