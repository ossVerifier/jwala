package com.siemens.cto.toc.files;

import com.siemens.cto.aem.common.request.app.UploadWebArchiveRequest;
import com.siemens.cto.aem.common.domain.model.app.Application;
import com.siemens.cto.aem.common.request.app.RemoveWebArchiveRequest;
import com.siemens.cto.aem.common.domain.model.user.User;
import com.siemens.cto.toc.files.RepositoryFileInformation.Type;
import com.siemens.cto.toc.files.impl.LocalFileSystemRepositoryServiceImpl;
import com.siemens.cto.toc.files.impl.PropertyFilesConfigurationImpl;
import com.siemens.cto.toc.files.impl.WebArchiveManagerImpl;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.LinkedList;
import java.util.Properties;
import java.util.UUID;

import static org.junit.Assert.*;


@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = {
    WebArchiveManagerTest.CommonConfiguration.class})
@RunWith(SpringJUnit4ClassRunner.class)
public class WebArchiveManagerTest {
    
    static class MemoryNameSynthesizer implements NameSynthesizer {
        
        LinkedList<Path> appliedNames = new LinkedList<>();
        
        public Path unique(Path originalName) {
            Path uniquePath = Paths.get(originalName.toString() + UUID.randomUUID().toString());
            synchronized(appliedNames) { appliedNames.push(uniquePath); }
            return uniquePath;
        }
        
        public Path pop() { return appliedNames.pop(); }
        
    }
    
    static class CommonConfiguration { 
        
        @Bean FileSystem getPlatformFileSystem() {
            return FileSystems.getDefault();
        }

        @Bean NameSynthesizer getNameSynthesizer() {
            return new MemoryNameSynthesizer();
        }
        
        @Bean WebArchiveManager getWebArchiveManager() {
            return new WebArchiveManagerImpl();
        }
        
        @Bean
        RepositoryService getFileSystemStorage() throws IOException {
            return new LocalFileSystemRepositoryServiceImpl();
        }
        
        @Bean FilesConfiguration getFilesConfiguration() throws IOException {
            Path storageFolder = Files.createTempDirectory("archives");
            
            Properties p = new Properties();
            p.put(TocPath.WEB_ARCHIVE.getProperty(), storageFolder.toString());

            return new PropertyFilesConfigurationImpl(p);
        }
    }
    
    @Autowired 
    WebArchiveManager webArchiveManager;

    @Autowired
    RepositoryService fsRepositoryService;
    
    @Autowired
    FilesConfiguration filesConfiguration;
    
    @Autowired 
    NameSynthesizer nameSynthesizer;
    
    @Autowired
    FileSystem platformFileSystem;

    // Managed by setup/teardown
    ByteArrayInputStream uploadedFile;
    Application app;
        
    // static test constants
    private static final User TEST_USER = new User("test-user");
    private static Path cleanupPath;
    
    @AfterClass
    public static void tearDownClass() throws IOException {
        if(cleanupPath == null) return;
        
        Files.walkFileTree(cleanupPath, new FileVisitor<Path>() {

            @Override
            public FileVisitResult postVisitDirectory(Path arg0, IOException arg1) throws IOException {
                Files.delete(arg0);
                return FileVisitResult.CONTINUE;
           }

            @Override
            public FileVisitResult preVisitDirectory(Path arg0, BasicFileAttributes arg1) throws IOException {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path arg0, BasicFileAttributes arg1) throws IOException {
                Files.delete(arg0);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path arg0, IOException arg1) throws IOException {
                return FileVisitResult.TERMINATE;
            }

        });
    }
    @Before
    public void setup() throws IOException {
        filesConfiguration.getConfiguredPath(TocPath.WEB_ARCHIVE);

        ByteBuffer buf = java.nio.ByteBuffer.allocate(1*1024*1024); // 1 Mb file
        buf.asShortBuffer().put((short)0xc0de);

        uploadedFile = new ByteArrayInputStream(buf.array());
        
        app = new Application(null, null, null, null, null, false, false, false, null);
    }
    
    @After
    public void tearDown() throws IOException {
        cleanupPath = filesConfiguration.getConfiguredPath(TocPath.WEB_ARCHIVE);
    }
    
    private void testResults(long expectedSize, RepositoryFileInformation result) throws IOException {
        testResults(expectedSize, result, false);
    }
    private Path testResults(long expectedSize, RepositoryFileInformation result, boolean preserve) throws IOException {
        assertEquals("Size mismatch after store of file.", expectedSize, (long)result.getLength());
        
        Path storedPath = ((MemoryNameSynthesizer)nameSynthesizer).pop();
        
        FileChannel fc = FileChannel.open(filesConfiguration.getConfiguredPath(TocPath.WEB_ARCHIVE).resolve(storedPath), StandardOpenOption.READ, preserve==false?StandardOpenOption.DELETE_ON_CLOSE:StandardOpenOption.READ );
        
        assertNotNull(fc);
        
        ByteBuffer dst = ByteBuffer.allocate(2);
        
        fc.read(dst);
        dst.flip();
        
        assertTrue(dst.asShortBuffer().get(0) == (short)0xc0de);
        
        fc.close();       
        
        return result.getPath();
    }
    
    @Test
    public void testWriteArchive() throws IOException { 
                
        UploadWebArchiveRequest request = new UploadWebArchiveRequest(app, "filename.war", 1*1024*1024L, uploadedFile);
        request.validate();
        
        testResults(
                1*1024*1024L,
                webArchiveManager.store(request));
    }

    @Test
    public void testDeleteArchive() throws IOException {
        
        UploadWebArchiveRequest request = new UploadWebArchiveRequest(app, "filename.war", 1*1024*1024L, uploadedFile);
        request.validate();
        
        Path expectedPath = 
                testResults(
                    1*1024*1024L,
                    webArchiveManager.store(request),
                    true);  

        app.setWarPath(expectedPath.toString());
        
        RemoveWebArchiveRequest rwac = new RemoveWebArchiveRequest(app);
        rwac.validate();
        
        RepositoryFileInformation result = webArchiveManager.remove(rwac);
        
        assertEquals(RepositoryFileInformation.deleted(expectedPath, RepositoryFileInformation.found(expectedPath, RepositoryFileInformation.none())), result);
    }
}
