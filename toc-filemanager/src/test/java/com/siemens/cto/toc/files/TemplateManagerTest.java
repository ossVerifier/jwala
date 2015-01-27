package com.siemens.cto.toc.files;

import static org.junit.Assert.assertEquals;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.Properties;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import com.siemens.cto.aem.domain.model.app.Application;
import com.siemens.cto.aem.domain.model.resource.ResourceType;
import com.siemens.cto.toc.files.impl.LocalFileSystemRepositoryImpl;
import com.siemens.cto.toc.files.impl.PropertyFilesConfigurationImpl;
import com.siemens.cto.toc.files.impl.TemplateManagerImpl;
import com.siemens.cto.toc.files.resources.ResourceTypeDeserializer;


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
            p.put(TocPath.RESOURCE_TYPES.getProperty(), storageFolder.toString());

            return new PropertyFilesConfigurationImpl(p);
        }

        @Bean
        public ResourceTypeDeserializer getResourceTypeDeserializer() { 
            return new ResourceTypeDeserializer();
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

    public void testGetResourceTypes() throws IOException { 
       
       Path storageFolder = filesConfiguration.getConfiguredPath(TocPath.WEB_ARCHIVE);
       try(BufferedWriter writer = Files.newBufferedWriter(storageFolder.resolve("ResourceProperties.json"), Charset.defaultCharset(), StandardOpenOption.CREATE)) {
           
        writer.write("{\"name\":\"MySql XA Database\",\"contentType\":\"application/xml\", \"properties\":[{\"name\":\"\u0032\", \"meta1\":\"meta1\"},{\"name\":\"name two\", \"meta2\" :\"meta two\"}]}");

        Collection<ResourceType> rtypes = templateManager.getResourceTypes();
        assertEquals(1, rtypes.size());
        assertEquals("MySql XA Database", rtypes.iterator().next().getName());
        
       }
       
    }
    
    @Test
    public void testFindCurrent() throws IOException {
        String  result = templateManager.getAbsoluteLocation(new TocFile()  {

            @Override
            public String getFileName() {
                return ".";
            }} );
        
        assertEquals(filesConfiguration.getConfiguredPath(TocPath.TEMPLATES) + "\\.", result);
    }
    
}
