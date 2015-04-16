package com.siemens.cto.toc.files;

import com.siemens.cto.toc.files.impl.LocalFileSystemRepositoryServiceImpl;
import com.siemens.cto.toc.files.impl.PropertyFilesConfigurationImpl;
import com.siemens.cto.toc.files.resources.ResourceTypeDeserializer;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Properties;

/**
 * Created by z003e5zv on 4/15/2015.
 */
@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = {
        TemplateManagerTest.CommonConfiguration.class})
@RunWith(SpringJUnit4ClassRunner.class)
public class RepositoryServiceTest {
    static class CommonConfiguration {

        @Bean
        RepositoryService getRepositoryService() {
            return new LocalFileSystemRepositoryServiceImpl();
        }

        @Bean
        FileSystem getPlatformFileSystem() {
            return FileSystems.getDefault();
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
    private FilesConfiguration filesConfiguration;
    @Autowired
    private RepositoryService repositoryService;

    @Test
    public void testfindForFound() throws IOException {
        Path storageFolder = filesConfiguration.getConfiguredPath(TocPath.RESOURCE_TYPES);
        String nameOfFile = "ResourceInstanceFindFoundTestTemplate.tpl";
        try(BufferedWriter writer = Files.newBufferedWriter(storageFolder.resolve(nameOfFile), Charset.defaultCharset(), StandardOpenOption.CREATE)) {

            writer.write("${replacementTest}");
            writer.flush();
            writer.close();

            RepositoryFileInformation fileInformation = repositoryService.find(TocPath.RESOURCE_TYPES, Paths.get(nameOfFile));
            Assert.assertNotNull(fileInformation);
            Assert.assertEquals(RepositoryFileInformation.Type.FOUND, fileInformation.getType());
        }
    }

    @Test
    public void testfindForNone() throws IOException {
        String nameOfFile = "ResourceInstanceNotFoundBadValue";
        RepositoryFileInformation fileInformation = repositoryService.find(TocPath.RESOURCE_TYPES, Paths.get("ResourceInstanceTestBadValue"));
        Assert.assertNotNull(fileInformation);
        Assert.assertEquals(fileInformation.getType(), RepositoryFileInformation.Type.NONE);
    }

    @Test
    public void testfindForCreated() throws IOException {
        Path storageFolder = filesConfiguration.getConfiguredPath(TocPath.RESOURCE_TYPES);
        String nameOfFile = "ResourceInstanceCreateTest";
        String testString = "${replacementTest}";
        InputStream stream = new ByteArrayInputStream(testString.getBytes(StandardCharsets.UTF_8));
        RepositoryFileInformation fileInformation = repositoryService.writeStream(TocPath.RESOURCE_TYPES, Paths.get(nameOfFile + "Template.tpl"), stream);
        Assert.assertNotNull(fileInformation);
        Assert.assertEquals(fileInformation.getType(), RepositoryFileInformation.Type.STORED);
    }

    @Test
    public void testDelete() throws IOException {
        Path storageFolder = filesConfiguration.getConfiguredPath(TocPath.RESOURCE_TYPES);
        String nameOfFile = "ResourceInstanceDeletedTemplate.tpl";
        try(BufferedWriter writer = Files.newBufferedWriter(storageFolder.resolve(nameOfFile), Charset.defaultCharset(), StandardOpenOption.CREATE)) {

            writer.write("${replacementTest}");
            writer.flush();
            writer.close();

            RepositoryFileInformation fileInformation = repositoryService.deleteIfExisting(TocPath.RESOURCE_TYPES, Paths.get(nameOfFile));
            Assert.assertNotNull(fileInformation);
            Assert.assertEquals(fileInformation.getType(), RepositoryFileInformation.Type.DELETED);
        }
    }

    @Test
    public void testDeletedWhenNotFound() throws IOException {
        Path storageFolder = filesConfiguration.getConfiguredPath(TocPath.RESOURCE_TYPES);
        String nameOfFile = "ResourceInstanceDeletedBadValue";
        RepositoryFileInformation fileInformation = repositoryService.deleteIfExisting(TocPath.RESOURCE_TYPES, Paths.get(nameOfFile));
        Assert.assertNotNull(fileInformation);
        Assert.assertEquals(fileInformation.getType(), RepositoryFileInformation.Type.NONE);
    }
}
