package com.siemens.cto.toc.files;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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
import com.siemens.cto.toc.files.impl.LocalFileSystemRepositoryServiceImpl;
import com.siemens.cto.toc.files.impl.PropertyFilesConfigurationImpl;
import com.siemens.cto.toc.files.impl.FileManagerImpl;
import com.siemens.cto.toc.files.resources.ResourceTypeDeserializer;


@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = {
    FileManagerTest.CommonConfiguration.class})
@RunWith(SpringJUnit4ClassRunner.class)
public class FileManagerTest {
    
    static class CommonConfiguration { 
        
        @Bean FileSystem getPlatformFileSystem() {
            return FileSystems.getDefault();
        }

        @Bean
        FileManager getTemplateManager() {
            return new FileManagerImpl();
        }

        @Bean
        RepositoryService getFileSystemStorage() throws IOException {
            return new LocalFileSystemRepositoryServiceImpl();
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
    FileManager fileManager;

    @Autowired
    RepositoryService fsRepositoryService;
    
    @Autowired
    FilesConfiguration filesConfiguration;
    
    @Autowired
    FileSystem platformFileSystem;

    // Managed by setup/teardown
    ByteArrayInputStream uploadedFile;
    Application app;

    @Test
    public void testGetResourceTypes() throws IOException { 
       
       Path storageFolder = filesConfiguration.getConfiguredPath(TocPath.RESOURCE_TYPES);
       try(BufferedWriter writer = Files.newBufferedWriter(storageFolder.resolve("MySql XA Database.json"), Charset.defaultCharset(), StandardOpenOption.CREATE)) {
           writer.write("{\"name\":\"MySql XA Database\",\"contentType\":\"application/xml\", \"properties\":[{\"name\":\"\u0032\", \"meta1\":\"meta1\"},{\"name\":\"name two\", \"meta2\" :\"meta two\"}]}");
           writer.flush();
           writer.close();
           Collection<ResourceType> rtypes = fileManager.getResourceTypes();
           assertEquals(1, rtypes.size());
           assertEquals("MySql XA Database", rtypes.iterator().next().getName());
       }
       
    }
    @Test
    public void testGetResourceTypeTemplate() throws IOException {
        Path storageFolder = filesConfiguration.getConfiguredPath(TocPath.RESOURCE_TYPES);
        try(BufferedWriter writer = Files.newBufferedWriter(storageFolder.resolve("ResourceInstanceTestTemplate.tpl"), Charset.defaultCharset(), StandardOpenOption.CREATE)) {

            writer.write("${replacementTest}");
            writer.flush();
            writer.close();

            String testTemplate = fileManager.getResourceTypeTemplate("ResourceInstanceTest");
            assertNotNull(testTemplate);
        }
    }
    @Test
    public void testFindCurrent() throws IOException {
        String  result = fileManager.getAbsoluteLocation(new TocFile()  {

            @Override
            public String getFileName() {
                return ".";
            }} );
        
        assertEquals(filesConfiguration.getConfiguredPath(TocPath.TEMPLATES) + "\\.", result);
    }
    
}
