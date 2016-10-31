package com.cerner.jwala.files;

import com.cerner.jwala.common.domain.model.app.Application;
import com.cerner.jwala.common.properties.ApplicationProperties;
import com.cerner.jwala.files.impl.FileManagerImpl;
import com.cerner.jwala.files.impl.LocalFileSystemRepositoryServiceImpl;
import com.cerner.jwala.files.impl.PropertyFilesConfigurationImpl;
import com.cerner.jwala.files.resources.ResourceTypeDeserializer;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = {
    FileManagerTest.CommonConfiguration.class})
@RunWith(SpringJUnit4ClassRunner.class)
public class FileManagerTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileManagerTest.class);

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
            p.put(JwalaPath.TEMPLATES.getProperty(), storageFolder.toString());
            p.put(JwalaPath.RESOURCE_TEMPLATES.getProperty(), storageFolder.toString());

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
    public void testGetResourceTypeTemplate() throws IOException {
        Path storageFolder = filesConfiguration.getConfiguredPath(JwalaPath.RESOURCE_TEMPLATES);
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
        String  result = fileManager.getAbsoluteLocation(new JwalaFile()  {

            @Override
            public String getFileName() {
                return ".";
            }} );
        
        assertEquals(filesConfiguration.getConfiguredPath(JwalaPath.TEMPLATES) + "\\.", result);
    }
    @Test
    public void testUnZip() throws IOException{
//        System.setProperty(ApplicationProperties.PROPERTIES_ROOT_PATH, new File(".").getAbsolutePath() + "/src/test/resources/properties");
        System.setProperty(ApplicationProperties.PROPERTIES_ROOT_PATH, "build/resources/test");
        ApplicationProperties.getInstance();
        File tempDir = new File("build/resources/test/temp");
        tempDir.mkdir();
        String instanceTemplatePath = ApplicationProperties.get("jwala.instance-template");
        LOGGER.info("instanceTemplatePath: "+instanceTemplatePath);
        LOGGER.info("build/resources/test/");
        File zipFile = new File("build/resources/test/"+instanceTemplatePath);
        fileManager.unZipFile(zipFile,tempDir);
        File serverXml = new File(tempDir.getAbsolutePath()+"/conf/server.xml");
        assertTrue(serverXml.exists());

    }
}
