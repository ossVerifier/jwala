package com.siemens.cto.toc.files.impl;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Autowired;

import com.siemens.cto.aem.domain.model.app.UploadWebArchiveCommand;
import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.toc.files.NameSynthesizer;
import com.siemens.cto.toc.files.Repository;
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
    public int store(Event<UploadWebArchiveCommand> event) throws IOException {
 
        UploadWebArchiveCommand cmd = event.getCommand();
        Path place = synth.unique(FileSystems.getDefault().getPath(cmd.getFilename()));
        int bytesWritten = fileSystemStorage.writeStream(TocPath.WEB_ARCHIVE, place, cmd.getTransientData());

        // TODO return object with size and location
        
        return bytesWritten;
        
    }

}
