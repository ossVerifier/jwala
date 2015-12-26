package com.siemens.cto.aem.persistence.configuration;

import com.siemens.cto.aem.persistence.jpa.service.HistoryCrudService;
import com.siemens.cto.aem.persistence.jpa.service.impl.HistoryCrudServiceImpl;
import com.siemens.cto.aem.persistence.jpa.service.WebServerCrudService;
import com.siemens.cto.aem.persistence.jpa.service.impl.WebServerCrudServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AemDaoConfiguration {

    @Autowired
    private AemDataSourceConfiguration dataSourceConfiguration;

    @Deprecated // TODO (Peter) Needs replacing with a PersistenceService
    @Bean(name = "webServerDao")
    public WebServerCrudService getWebServerDao() {
        return new WebServerCrudServiceImpl();
    }

    @Bean
    public HistoryCrudService getHistoryDao() {
        return new HistoryCrudServiceImpl();
    }

}
