package com.cerner.jwala.service.bootstrap;

import com.cerner.jwala.common.properties.ApplicationProperties;
import com.cerner.jwala.persistence.jpa.domain.JpaMedia;
import com.cerner.jwala.persistence.jpa.type.MediaType;
import com.cerner.jwala.service.exception.ApplicationStartupException;
import com.cerner.jwala.service.media.MediaService;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created on 2/6/2017.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = {ApplicationContextListenerTest.Config.class})
public class ApplicationContextListenerTest {

    @Autowired
    private ApplicationContextListener applicationContextListener;

    @Before
    public void setup(){
        System.setProperty(ApplicationProperties.PROPERTIES_ROOT_PATH, new File(".").getAbsolutePath() + "/src/test/resources");
    }

    @Test
    public void testNoApplicationContext() {
        JpaMedia mockJdkMedia = mock(JpaMedia.class);
        when(mockJdkMedia.getType()).thenReturn(MediaType.JDK);
        List<JpaMedia> mediaList = Collections.singletonList(mockJdkMedia);

        when(Config.mediaServiceMock.findAll()).thenReturn(mediaList);
        ContextRefreshedEvent mockStartupEvent = mock(ContextRefreshedEvent.class);

        applicationContextListener.handleEvent(mockStartupEvent);
    }

    @Test
    public void testNoParentForApplicationContext() {
        JpaMedia mockJdkMedia = mock(JpaMedia.class);
        when(mockJdkMedia.getType()).thenReturn(MediaType.JDK);
        List<JpaMedia> mediaList = Collections.singletonList(mockJdkMedia);

        when(Config.mediaServiceMock.findAll()).thenReturn(mediaList);
        ContextRefreshedEvent mockStartupEvent = mock(ContextRefreshedEvent.class);

        ApplicationContext mockApplicationContext = mock(ApplicationContext.class);
        when(mockStartupEvent.getApplicationContext()).thenReturn(mockApplicationContext);

        applicationContextListener.handleEvent(mockStartupEvent);
    }

    @Test
    public void testFoundJdkMedia() {
        JpaMedia mockJdkMedia = mock(JpaMedia.class);
        when(mockJdkMedia.getType()).thenReturn(MediaType.JDK);
        List<JpaMedia> mediaList = Collections.singletonList(mockJdkMedia);

        when(Config.mediaServiceMock.findAll()).thenReturn(mediaList);
        ContextRefreshedEvent mockStartupEvent = mock(ContextRefreshedEvent.class);

        ApplicationContext mockApplicationContext = mock(ApplicationContext.class);
        ApplicationContext mockParent = mock(ApplicationContext.class);
        when(mockApplicationContext.getParent()).thenReturn(mockParent);
        when(mockStartupEvent.getApplicationContext()).thenReturn(mockApplicationContext);

        applicationContextListener.handleEvent(mockStartupEvent);
    }

    @Test
    public void testEmpyMedia() {
        List<JpaMedia> mediaList = new ArrayList<>();

        when(Config.mediaServiceMock.findAll()).thenReturn(mediaList);
        ContextRefreshedEvent mockStartupEvent = mock(ContextRefreshedEvent.class);

        ApplicationContext mockApplicationContext = mock(ApplicationContext.class);
        ApplicationContext mockParent = mock(ApplicationContext.class);
        when(mockApplicationContext.getParent()).thenReturn(mockParent);
        when(mockStartupEvent.getApplicationContext()).thenReturn(mockApplicationContext);

        applicationContextListener.handleEvent(mockStartupEvent);
    }

    @Test (expected = ApplicationStartupException.class)
    public void testEmpyMediaNoDefaultJDK() throws IOException {
        String propertiesRootPath = System.getProperty(ApplicationProperties.PROPERTIES_ROOT_PATH);
        try {
            // copy vars property that overrides data.binary location and reload the properties
            final String tempPropertiesRootPath = new File(".").getAbsolutePath() + "/build";
            FileUtils.copyFile(new File(propertiesRootPath + "/vars-applicationContextListener.properties"), new File(tempPropertiesRootPath + "/vars.properties"));
            System.setProperty(ApplicationProperties.PROPERTIES_ROOT_PATH, tempPropertiesRootPath);
            ApplicationProperties.reload();

            List<JpaMedia> mediaList = new ArrayList<>();

            when(Config.mediaServiceMock.findAll()).thenReturn(mediaList);
            ContextRefreshedEvent mockStartupEvent = mock(ContextRefreshedEvent.class);

            ApplicationContext mockApplicationContext = mock(ApplicationContext.class);
            ApplicationContext mockParent = mock(ApplicationContext.class);
            when(mockApplicationContext.getParent()).thenReturn(mockParent);
            when(mockStartupEvent.getApplicationContext()).thenReturn(mockApplicationContext);

            applicationContextListener.handleEvent(mockStartupEvent);
        } finally {
            System.setProperty(ApplicationProperties.PROPERTIES_ROOT_PATH, propertiesRootPath);
            ApplicationProperties.reload();
        }
    }

    @Test
    public void testNonEmptyMediaNoJDK() {
        JpaMedia mockJdkMedia = mock(JpaMedia.class);
        when(mockJdkMedia.getType()).thenReturn(MediaType.TOMCAT);
        List<JpaMedia> mediaList = Collections.singletonList(mockJdkMedia);

        when(Config.mediaServiceMock.findAll()).thenReturn(mediaList);
        ContextRefreshedEvent mockStartupEvent = mock(ContextRefreshedEvent.class);

        ApplicationContext mockApplicationContext = mock(ApplicationContext.class);
        ApplicationContext mockParent = mock(ApplicationContext.class);
        when(mockApplicationContext.getParent()).thenReturn(mockParent);
        when(mockStartupEvent.getApplicationContext()).thenReturn(mockApplicationContext);

        applicationContextListener.handleEvent(mockStartupEvent);
    }

    @Configuration
    static class Config {

        private static final MediaService mediaServiceMock = mock(MediaService.class);

        @Bean
        public MediaService getMediaService() {
            return mediaServiceMock;
        }

        @Bean
        public ApplicationContextListener getApplicationContextListener() {
            return new ApplicationContextListener();
        }

    }
}
