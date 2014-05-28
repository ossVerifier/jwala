package com.siemens.cto.aem.service.configuration.service;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.siemens.cto.toc.files.WebArchiveManger;
import com.siemens.cto.toc.files.impl.WebArchiveManagerImpl;

@Configuration
public class TocFileManagerConfiguration {
    @Bean WebArchiveManger getWebArchiveManager() {
        return new WebArchiveManagerImpl();
    }
}
