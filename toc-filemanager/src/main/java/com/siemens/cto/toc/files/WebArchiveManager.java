package com.siemens.cto.toc.files;

import com.siemens.cto.aem.domain.command.app.RemoveWebArchiveCommand;
import com.siemens.cto.aem.domain.command.app.UploadWebArchiveCommand;
import com.siemens.cto.aem.domain.model.event.Event;

import java.io.IOException;

public interface WebArchiveManager {

    RepositoryFileInformation store(Event<UploadWebArchiveCommand> event) throws IOException;

    RepositoryFileInformation remove(Event<RemoveWebArchiveCommand> event) throws IOException;

}
