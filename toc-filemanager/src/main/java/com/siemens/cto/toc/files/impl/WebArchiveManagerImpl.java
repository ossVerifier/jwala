package com.siemens.cto.toc.files.impl;

import com.siemens.cto.aem.common.request.app.RemoveWebArchiveRequest;
import com.siemens.cto.aem.common.request.app.UploadWebArchiveRequest;
import com.siemens.cto.aem.common.domain.model.app.Application;
import com.siemens.cto.toc.files.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;

public class WebArchiveManagerImpl implements WebArchiveManager {

    private FileSystem platformFileSystem = FileSystems.getDefault();
    
    @Autowired
    private NameSynthesizer synth;
    
    @Autowired
    private RepositoryService fileSystemStorage;
    
    @Override
    public RepositoryFileInformation store(UploadWebArchiveRequest uploadWebArchiveRequest) throws IOException {
        Path place = synth.unique(platformFileSystem.getPath(uploadWebArchiveRequest.getFilename()));
        RepositoryFileInformation writeResult = fileSystemStorage.writeStream(TocPath.WEB_ARCHIVE, place, uploadWebArchiveRequest.getTransientData());
        return writeResult;        
    }

    @Override
    public RepositoryFileInformation remove(RemoveWebArchiveRequest removeWebArchiveRequest) throws IOException {
        RepositoryFileInformation action = RepositoryFileInformation.none();
        
        Application app = removeWebArchiveRequest.getApplication();
                
        String existing = app.getWarPath();
        
        if(StringUtils.hasText(existing)) {
            action = fileSystemStorage.deleteIfExisting(TocPath.WEB_ARCHIVE, platformFileSystem.getPath(existing), action);
        }
        
        return action;
    }

}
