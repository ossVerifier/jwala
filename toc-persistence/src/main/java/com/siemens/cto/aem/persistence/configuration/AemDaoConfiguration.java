package com.siemens.cto.aem.persistence.configuration;

import com.siemens.cto.aem.persistence.jpa.service.HistoryCrudService;
import com.siemens.cto.aem.persistence.jpa.service.impl.HistoryCrudServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AemDaoConfiguration {

    @Bean
    public HistoryCrudService getHistoryDao() {
        return new HistoryCrudServiceImpl();
    }

}
