package com.cerner.jwala.service.app;

import com.cerner.jwala.common.domain.model.app.Application;
import com.cerner.jwala.common.request.app.UploadWebArchiveRequest;
import com.cerner.jwala.files.RepositoryFileInformation;

/**
 * Not to be used as entry points, these APIs are called indirectly from 
 * {@link com.cerner.jwala.service.app.impl.ApplicationServiceImpl}
 */
public interface PrivateApplicationService {

    RepositoryFileInformation uploadWebArchiveData(UploadWebArchiveRequest uploadWebArchiveRequest);

    Application uploadWebArchiveUpdateDB(UploadWebArchiveRequest uploadWebArchiveRequest, RepositoryFileInformation result);
}
