package com.siemens.cto.aem.persistence.configuration;

import javax.persistence.EntityManagerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.support.SharedEntityManagerBean;
import org.springframework.orm.jpa.vendor.OpenJpaVendorAdapter;

@Configuration
public class AemJpaConfiguration {

    @Autowired
    private AemDataSourceConfiguration dataSourceConfiguration;

    @Bean
    public JpaVendorAdapter getJpaVendorAdapter() {
        return new OpenJpaVendorAdapter();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean getEntityManagerFactoryBean() {

        final LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        factory.setDataSource(dataSourceConfiguration.getAemDataSource());
        factory.setJpaVendorAdapter(getJpaVendorAdapter());
        factory.setPersistenceUnitName("aem-unit");
        factory.setPersistenceXmlLocation("classpath:META-INF/persistence.xml");

        return factory;
    }

    @Bean
    public EntityManagerFactory getEntityManagerFactory() {
        return getEntityManagerFactoryBean().getObject();
    }

    @Bean
    public SharedEntityManagerBean getSharedEntityManagerBean() {

        final SharedEntityManagerBean shared = new SharedEntityManagerBean();

        shared.setEntityManagerFactory(getEntityManagerFactory());

        return shared;
    }
}
