package com.siemens.cto.toc.files;

import com.siemens.cto.aem.domain.model.app.UploadWebArchiveCommand;

public interface WebArchiveManger {

    void store(UploadWebArchiveCommand cmd);

}
