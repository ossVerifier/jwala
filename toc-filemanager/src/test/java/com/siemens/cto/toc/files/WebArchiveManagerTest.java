package com.siemens.cto.toc.files;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.LinkedList;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import com.siemens.cto.aem.domain.model.app.Application;
import com.siemens.cto.aem.domain.model.app.UploadWebArchiveCommand;
import com.siemens.cto.aem.domain.model.audit.AuditEvent;
import com.siemens.cto.aem.domain.model.temporary.User;
import com.siemens.cto.toc.files.impl.LocalFileSystemRepositoryImpl;
import com.siemens.cto.toc.files.impl.PropertyFilesConfigurationImpl;
import com.siemens.cto.toc.files.impl.WebArchiveManagerImpl;


@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = {
    WebArchiveManagerTest.CommonConfiguration.class})
@RunWith(SpringJUnit4ClassRunner.class)
public class WebArchiveManagerTest {
    
    static class MemoryNameSynthesizer implements NameSynthesizer {
        
        LinkedList<Path> appliedNames = new LinkedList<>();
        
        Path synthesize(Path originalName) { 
            synchronized(appliedNames) { appliedNames.push(originalName); }
            return originalName;
        }
        
        Path pop() { return appliedNames.pop(); }
        
    }
    
    static class CommonConfiguration { 

        @Bean NameSynthesizer getNameSynthesizer() {
            return new MemoryNameSynthesizer();
        }
        
        @Bean WebArchiveManger getWebArchiveManager() {
            return new WebArchiveManagerImpl();
        }
        
        @Bean Repository getFileSystemStorage() throws IOException {
            Repository s = new LocalFileSystemRepositoryImpl();
            Path storageFolder = Files.createTempDirectory("archives");
            s.addCategory("archives", storageFolder);
            return s;
        }
        
        @Bean FilesConfiguration getFilesConfiguration() {
            FilesConfiguration f = new PropertyFilesConfigurationImpl();
            return f;
            
        }
    }
    
    @Autowired 
    WebArchiveManger webArchiveManager;

    @Autowired 
    Repository fsRepository;
    
    @Autowired
    FilesConfiguration filesConfiguration;
    
    @Autowired 
    NameSynthesizer nameSynthesizer;
    
    @Test
    public void testWriteArchive() throws IOException { 
                
        ByteBuffer buf = java.nio.ByteBuffer.allocate(64*1024*1024); // 64 Mb file
        buf.asShortBuffer().put((short)0xc0de);

        ByteArrayInputStream uploadedFile = new ByteArrayInputStream(buf.array());
        
        Application app = new Application(null, null, null, null, null);
        
        UploadWebArchiveCommand cmd = new UploadWebArchiveCommand(app, "filename.war", uploadedFile, AuditEvent.now(new User("test-user")));
        cmd.validateCommand(); // may trigger BadRequestException
        
        webArchiveManager.store(cmd);  
        
        Path storedPath = ((MemoryNameSynthesizer)nameSynthesizer).pop();
        
        FileChannel fc = FileChannel.open(storedPath, StandardOpenOption.READ, StandardOpenOption.DELETE_ON_CLOSE );
        
        assertNotNull(fc);
        
        ByteBuffer dst = ByteBuffer.allocate(2);
        
        fc.read(dst);
        
        assertTrue(dst.asShortBuffer().get(0) == 0xc0de);
        
        fc.close();
    }

}
