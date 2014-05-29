package com.siemens.cto.toc.files;

import java.io.IOException;

import com.siemens.cto.aem.domain.model.app.UploadWebArchiveCommand;
import com.siemens.cto.aem.domain.model.event.Event;

public interface WebArchiveManager {

    int store(Event<UploadWebArchiveCommand> event) throws IOException;

}
