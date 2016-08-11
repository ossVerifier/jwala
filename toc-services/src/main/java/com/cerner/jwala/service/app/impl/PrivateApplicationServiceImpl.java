package com.cerner.jwala.service.app.impl;

import com.cerner.jwala.common.domain.model.app.Application;
import com.cerner.jwala.common.domain.model.fault.AemFaultType;
import com.cerner.jwala.common.exception.BadRequestException;
import com.cerner.jwala.common.request.app.UploadWebArchiveRequest;
import com.cerner.jwala.files.RepositoryFileInformation;
import com.cerner.jwala.files.WebArchiveManager;
import com.cerner.jwala.persistence.service.ApplicationPersistenceService;
import com.cerner.jwala.service.app.PrivateApplicationService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

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
    public RepositoryFileInformation uploadWebArchiveData(final UploadWebArchiveRequest uploadWebArchiveRequest) {
        RepositoryFileInformation result = null;

        try {
            result = webArchiveManager.store(uploadWebArchiveRequest);
            LOGGER.info("Archive Upload: " + result);
        } catch (IOException e) {
            //This is logged here instead of included in the BadRequestException because it's a potential CSRF vector
            LOGGER.warn("IOException occurred while trying to upload Web Archive Data", e);
            throw new BadRequestException(AemFaultType.BAD_STREAM,
                                          "Error storing data");
        }

        Long bytes = result.getLength();

        if(uploadWebArchiveRequest == null || uploadWebArchiveRequest.getLength() != -1 && bytes != null && !bytes.equals(uploadWebArchiveRequest.getLength())) {
            throw new BadRequestException(AemFaultType.BAD_STREAM, "Post-condition file length check failed");
        }

        return result;
    }

    /**
     * Helper method - transactional update of database.
     */
    @Override
    @Transactional
    public Application uploadWebArchiveUpdateDB(final UploadWebArchiveRequest uploadWebArchiveRequest, final RepositoryFileInformation result) {

        return applicationPersistenceService.updateWARPath(uploadWebArchiveRequest, result.getPath().toAbsolutePath().toString());

    }

}
