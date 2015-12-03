package com.siemens.cto.aem.persistence.configuration;

import com.siemens.cto.aem.common.ApplicationException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;
import org.springframework.jndi.JndiLocatorDelegate;

import javax.naming.NamingException;
import javax.sql.DataSource;

@Configuration
public class AemDataSourceConfiguration {

    @Bean
    public DataSource getAemDataSource() {
        try {
            return JndiLocatorDelegate.createDefaultResourceRefLocator().lookup("jdbc/toc-xa",
                                                                                DataSource.class);
        } catch (final NamingException ne) {
            throw new ApplicationException(ne);
        }
    }

    @Bean
    public DataSource getSpringManagedAemDataSource() {
        final TransactionAwareDataSourceProxy proxy = new TransactionAwareDataSourceProxy(getAemDataSource());

        return proxy;
    }
}
