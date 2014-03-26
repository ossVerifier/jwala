package com.siemens.cto.aem.persistence.configuration;

import java.util.Properties;

import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.OpenJpaVendorAdapter;

@Configuration
public class AemJpaConfiguration {

    @Autowired
    private AemDataSourceConfiguration dataSourceConfiguration;

    @Bean
    public JpaVendorAdapter getJpaVendorAdapter() {
        return new OpenJpaVendorAdapter();
    }

    @Bean(name = "openJpaProperties")
    public Properties getJpaProperties() {
        final Properties properties = new Properties();
        properties.setProperty("openjpa.jdbc.DBDictionary", "org.apache.openjpa.jdbc.sql.H2Dictionary");
        return properties;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean getEntityManagerFactoryBean() {
        final LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();

        factory.setJpaVendorAdapter(getJpaVendorAdapter());
        factory.setPersistenceXmlLocation("classpath:META-INF/persistence.xml");
        factory.setDataSource(dataSourceConfiguration.getAemDataSource());
        factory.setJpaProperties(getJpaProperties());

        return factory;
    }

    @Bean
    public EntityManagerFactory getEntityManagerFactory() {
        return getEntityManagerFactoryBean().getObject();
    }
}
