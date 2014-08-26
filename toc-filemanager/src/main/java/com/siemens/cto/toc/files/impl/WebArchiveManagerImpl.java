package com.siemens.cto.toc.files.impl;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import com.siemens.cto.aem.domain.model.app.Application;
import com.siemens.cto.aem.domain.model.app.RemoveWebArchiveCommand;
import com.siemens.cto.aem.domain.model.app.UploadWebArchiveCommand;
import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.toc.files.NameSynthesizer;
import com.siemens.cto.toc.files.Repository;
import com.siemens.cto.toc.files.RepositoryAction;
import com.siemens.cto.toc.files.TocPath;
import com.siemens.cto.toc.files.WebArchiveManager;

public class WebArchiveManagerImpl implements WebArchiveManager {

    @Autowired
    private FileSystem platformFileSystem;
    
    @Autowired
    private NameSynthesizer synth;
    
    @Autowired
    private Repository fileSystemStorage;
    
    @Override
    public RepositoryAction store(Event<UploadWebArchiveCommand> event) throws IOException {
 
        UploadWebArchiveCommand cmd = event.getCommand();       
        Application app = event.getCommand().getApplication();
        String existing = app.getWarPath();
        
        Path place = synth.unique(platformFileSystem.getPath(cmd.getFilename()));
        
        RepositoryAction writeResult = fileSystemStorage.writeStream(TocPath.WEB_ARCHIVE, place, cmd.getTransientData());

        if( RepositoryAction.Type.STORED.equals(writeResult.getType())
            && existing != null 
            && existing.trim().length() > 0) {

            // attempt to delete since store succeeded and the previous file is no longer needed
            RepositoryAction deleted = fileSystemStorage.deleteIfExisting(TocPath.WEB_ARCHIVE, platformFileSystem.getPath(existing), writeResult);
            
            // Wrap the response, so that the primary action is a STORE
            return RepositoryAction.stored(writeResult.getPath(), writeResult.getLength(), deleted);
        }
        
        return writeResult;        
    }

    @Override
    public RepositoryAction remove(Event<RemoveWebArchiveCommand> event) throws IOException {
        RepositoryAction action = RepositoryAction.none();
        
        RemoveWebArchiveCommand cmd = event.getCommand();       
        Application app = cmd.getApplication();
                
        String existing = app.getWarPath();
        
        if(StringUtils.hasText(existing)) {
            action = fileSystemStorage.deleteIfExisting(TocPath.WEB_ARCHIVE, platformFileSystem.getPath(existing), action);
        }
        
        return action;
    }

}
