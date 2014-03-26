package com.siemens.cto.aem.persistence.configuration;

import javax.naming.NamingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.siemens.cto.aem.persistence.dao.group.GroupDao;
import com.siemens.cto.aem.persistence.dao.group.impl.springjdbc.SpringJdbcGroupDaoImpl;
import com.siemens.cto.aem.persistence.dao.jvm.JvmDao;
import com.siemens.cto.aem.persistence.dao.jvm.impl.jpa.JpaJvmDaoImpl;

@Configuration
public class AemDaoConfiguration {

    @Autowired
    private AemDataSourceConfiguration dataSourceConfiguration;

    @Bean
    public GroupDao getGroupDao() {
        return new SpringJdbcGroupDaoImpl(dataSourceConfiguration.getAemDataSource());
    }

    @Bean
    public JvmDao getJvmDao() {
        return new JpaJvmDaoImpl();
    }
}
