package com.siemens.cto.aem.persistence.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.siemens.cto.aem.persistence.dao.app.ApplicationDao;
import com.siemens.cto.aem.persistence.dao.app.impl.jpa.JpaApplicationDaoImpl;
import com.siemens.cto.aem.persistence.dao.group.GroupDao;
import com.siemens.cto.aem.persistence.dao.group.impl.jpa.JpaGroupDaoImpl;
import com.siemens.cto.aem.persistence.dao.jvm.JvmDao;
import com.siemens.cto.aem.persistence.dao.jvm.impl.jpa.JpaJvmDaoImpl;
import com.siemens.cto.aem.persistence.dao.webserver.WebServerDao;
import com.siemens.cto.aem.persistence.dao.webserver.impl.jpa.JpaWebServerDaoImpl;

@Configuration
public class AemDaoConfiguration {

    @Autowired
    private AemDataSourceConfiguration dataSourceConfiguration;

    @Bean
    public GroupDao getGroupDao() {
        return new JpaGroupDaoImpl();
    }

    @Bean
    public JvmDao getJvmDao() {
        return new JpaJvmDaoImpl();
    }

    @Deprecated // TODO (Peter) Needs replacing with a PersistenceService
    @Bean(name="webServerDao")
    public WebServerDao getWebServerDao() {
        return new JpaWebServerDaoImpl();
    }

    @Bean
    public ApplicationDao getApplicationDao() {
        return new JpaApplicationDaoImpl();
    }
}
