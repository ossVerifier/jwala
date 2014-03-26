package com.siemens.cto.aem.persistence.configuration;

import javax.naming.InitialContext;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jndi.JndiLocatorDelegate;

@Configuration
public class AemDataSourceConfiguration {

    @Bean
    public DataSource getAemDataSource() {
        try {
            final InitialContext context = new InitialContext();
            final NamingEnumeration<NameClassPair> bound = context.list("java:comp/env/jdbc");
            while (bound.hasMoreElements()) {
                final NameClassPair pair = bound.nextElement();
                final int i = 0;
            }
            return JndiLocatorDelegate.createDefaultResourceRefLocator().lookup("jdbc/toc-xa",
                                                                                DataSource.class);
        } catch (final NamingException ne) {
            throw new RuntimeException(ne);
        }
    }
}
