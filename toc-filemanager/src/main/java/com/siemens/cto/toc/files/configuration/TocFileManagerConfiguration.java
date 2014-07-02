package com.siemens.cto.toc.files.configuration;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.context.event.ContextClosedEvent;

import com.siemens.cto.aem.common.properties.ApplicationProperties;
import com.siemens.cto.toc.files.FilesConfiguration;
import com.siemens.cto.toc.files.NameSynthesizer;
import com.siemens.cto.toc.files.Repository;
import com.siemens.cto.toc.files.WebArchiveManager;
import com.siemens.cto.toc.files.impl.DefaultNameSynthesizer;
import com.siemens.cto.toc.files.impl.LocalFileSystemRepositoryImpl;
import com.siemens.cto.toc.files.impl.PropertyFilesConfigurationImpl;
import com.siemens.cto.toc.files.impl.WebArchiveManagerImpl;
import com.siemens.med.hs.soarian.config.PropertiesStore;

@Configuration
public class TocFileManagerConfiguration implements ApplicationListener<ContextClosedEvent> {
    
    /**
     * Look up path properties from the application properties
     * @return A wrapper around Path properties
     */
    @Bean
    @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE, proxyMode = ScopedProxyMode.INTERFACES) 
    public FilesConfiguration getFilesConfiguration() {
        return new PropertyFilesConfigurationImpl(ApplicationProperties.getProperties());
    }

    @Bean(destroyMethod="") FileSystem getPlatformFileSystem() {
        return FileSystems.getDefault();
    }

    @Bean NameSynthesizer getNameSynthesizer() {
        return new DefaultNameSynthesizer();
    }
    
    @Bean WebArchiveManager getWebArchiveManager() {
        return new WebArchiveManagerImpl();
    }
    
    @Bean Repository getFileSystemStorage() throws IOException {
        return new LocalFileSystemRepositoryImpl();
    }

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        PropertiesStore.startPropertiesMonitor();        
    }
    
}
