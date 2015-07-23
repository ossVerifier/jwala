package com.siemens.cto.toc.files.impl;

import com.siemens.cto.aem.domain.model.app.Application;
import com.siemens.cto.aem.domain.model.app.RemoveWebArchiveCommand;
import com.siemens.cto.aem.domain.model.app.UploadWebArchiveCommand;
import com.siemens.cto.aem.domain.model.event.Event;
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
    public RepositoryFileInformation store(Event<UploadWebArchiveCommand> event) throws IOException {
 
        UploadWebArchiveCommand cmd = event.getCommand();       
        Application app = event.getCommand().getApplication();
        String existing = app.getWarPath();
        
        Path place = synth.unique(platformFileSystem.getPath(cmd.getFilename()));
        
        RepositoryFileInformation writeResult = fileSystemStorage.writeStream(TocPath.WEB_ARCHIVE, place, cmd.getTransientData());

        if( RepositoryFileInformation.Type.STORED.equals(writeResult.getType())
            && existing != null 
            && existing.trim().length() > 0) {

            // attempt to delete since store succeeded and the previous file is no longer needed
            RepositoryFileInformation deleted = fileSystemStorage.deleteIfExisting(TocPath.WEB_ARCHIVE, platformFileSystem.getPath(existing), writeResult);
            
            // Wrap the response, so that the primary action is a STORE
            return RepositoryFileInformation.stored(writeResult.getPath(), writeResult.getLength(), deleted);
        }
        
        return writeResult;        
    }

    @Override
    public RepositoryFileInformation remove(Event<RemoveWebArchiveCommand> event) throws IOException {
        RepositoryFileInformation action = RepositoryFileInformation.none();
        
        RemoveWebArchiveCommand cmd = event.getCommand();       
        Application app = cmd.getApplication();
                
        String existing = app.getWarPath();
        
        if(StringUtils.hasText(existing)) {
            action = fileSystemStorage.deleteIfExisting(TocPath.WEB_ARCHIVE, platformFileSystem.getPath(existing), action);
        }
        
        return action;
    }

}
