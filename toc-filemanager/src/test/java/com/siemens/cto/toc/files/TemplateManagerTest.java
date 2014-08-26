package com.siemens.cto.toc.files;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import com.siemens.cto.aem.domain.model.app.Application;
import com.siemens.cto.toc.files.impl.LocalFileSystemRepositoryImpl;
import com.siemens.cto.toc.files.impl.PropertyFilesConfigurationImpl;
import com.siemens.cto.toc.files.impl.TemplateManagerImpl;


@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = {
    TemplateManagerTest.CommonConfiguration.class})
@RunWith(SpringJUnit4ClassRunner.class)
public class TemplateManagerTest {
    
    static class CommonConfiguration { 
        
        @Bean FileSystem getPlatformFileSystem() {
            return FileSystems.getDefault();
        }

        @Bean TemplateManager getTemplateManager() {
            return new TemplateManagerImpl();
        }

        @Bean Repository getFileSystemStorage() throws IOException {
            return new LocalFileSystemRepositoryImpl();
        }
        
        @Bean FilesConfiguration getFilesConfiguration() throws IOException {
            Path storageFolder = Files.createTempDirectory("archives");
            
            Properties p = new Properties();
            p.put(TocPath.TEMPLATES.getProperty(), storageFolder.toString());

            return new PropertyFilesConfigurationImpl(p);
        }
    }
    
    @Autowired 
    TemplateManager templateManager;

    @Autowired 
    Repository fsRepository;
    
    @Autowired
    FilesConfiguration filesConfiguration;
    
    @Autowired
    FileSystem platformFileSystem;

    // Managed by setup/teardown
    ByteArrayInputStream uploadedFile;
    Application app;
        
    @Test
    public void testFindCurrent() throws IOException {
        RepositoryAction result = templateManager.locateTemplate(".");
        assertEquals(RepositoryAction.Type.FOUND, result.getType());
        assertEquals(filesConfiguration.getConfiguredPath(TocPath.TEMPLATES) + "\\.", result.getPath().toFile().getPath());
    }
    
}
