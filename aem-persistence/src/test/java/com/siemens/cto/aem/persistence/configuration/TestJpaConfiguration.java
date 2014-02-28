package com.siemens.cto.aem.persistence.configuration;

import javax.sql.DataSource;

import org.h2.Driver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.instrument.classloading.InstrumentationLoadTimeWeaver;
import org.springframework.instrument.classloading.LoadTimeWeaver;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
public class TestJpaConfiguration {

    @Bean
    public DataSource getDataSource() {
        return new SimpleDriverDataSource(new Driver(),
                                          "jdbc:h2:tcp://localhost/~/test",
                                          "sa",
                                          "");
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean getEntityManagerFactory() {
        final LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();

        factory.setPersistenceXmlLocation("classpath:META-INF/test-aem-persistence.xml");
        factory.setPersistenceUnitName("aem-unit");
        factory.setDataSource(getDataSource());
//        factory.setLoadTimeWeaver(getLoadTimeWeaver());

        return factory;
    }

    @Bean(name = "transactionManager")
    public PlatformTransactionManager getTransactionManager() {
        final PlatformTransactionManager manager = new JpaTransactionManager(getEntityManagerFactory().getObject());
        return manager;
    }

    @Bean(name = "loadTimeWeaver")
    public LoadTimeWeaver getLoadTimeWeaver() {
        return new InstrumentationLoadTimeWeaver();
    }
}
