package com.siemens.cto.toc.files;

import com.siemens.cto.aem.request.app.RemoveWebArchiveRequest;
import com.siemens.cto.aem.request.app.UploadWebArchiveRequest;
import com.siemens.cto.aem.domain.model.event.Event;

import java.io.IOException;

public interface WebArchiveManager {

    RepositoryFileInformation store(Event<UploadWebArchiveRequest> event) throws IOException;

    RepositoryFileInformation remove(Event<RemoveWebArchiveRequest> event) throws IOException;

}
