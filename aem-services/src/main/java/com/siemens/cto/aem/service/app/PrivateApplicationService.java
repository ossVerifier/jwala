package com.siemens.cto.aem.service.app;

import com.siemens.cto.aem.domain.model.app.Application;
import com.siemens.cto.aem.domain.model.app.UploadWebArchiveCommand;
import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.toc.files.RepositoryAction;

/**
 * Not to be used as entry points, these APIs are called indirectly from 
 * {@link com.siemens.cto.aem.service.app.impl.ApplicationServiceImpl}
 */
public interface PrivateApplicationService {

    RepositoryAction uploadWebArchiveData(Event<UploadWebArchiveCommand> event);

    Application uploadWebArchiveUpdateDB(Event<UploadWebArchiveCommand> event, RepositoryAction result);
}
