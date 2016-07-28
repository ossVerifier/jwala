package com.siemens.cto.aem.web.security;

import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.ldap.authentication.ad.ActiveDirectoryLdapAuthenticationProvider;

import com.siemens.cto.aem.common.properties.ApplicationProperties;

/**
 * @author AK048646
 *
 */
@Configuration
@ComponentScan("com.siemens.cto.aem.web.security")
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Autowired
    GrantedAuthoritiesMapperImpl  grantedAuthoritiesMapper;
    
    private static Logger LOGGER = Logger.getLogger(SecurityConfig.class);
    private static final String ACTIVE_DIRECTORY_DOMAIN = "active.directory.domain";
    private static final String ACTIVE_DIRECTORY_SERVER_NAME = "active.directory.server.name";
    private static final String ACTIVE_DIRECTORY_SERVER_PORT = "active.directory.server.port";
    
    private static final String LOGIN_PAGE ="/login";
    private static final String LOGIN_API = "/v1.0/user/login";
    private static final String LOGOUT_API = "/v1.0/user/logout";

    private static final String GEN_PUBLIC_RESOURCES = "/gen-public-resources/**";
    private static final String PUBLIC_RESOURCES = "/public-resources/**";
    private static final String PAGE_CONSTANTS = "/page-constants";
    
    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.security.config.annotation.web.configuration.
     * WebSecurityConfigurerAdapter#configure(org.springframework.security.
     * config.annotation.web.builders.HttpSecurity)
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                 .antMatchers(GEN_PUBLIC_RESOURCES, PUBLIC_RESOURCES,LOGIN_PAGE,LOGIN_API, LOGOUT_API).permitAll().and()
                 .formLogin().loginPage(LOGIN_PAGE).permitAll().and()
                 .authorizeRequests().anyRequest().authenticated();
        http.csrf().disable();
    }

    /* (non-Javadoc)
     * @see org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter#configure(org.springframework.security.config.annotation.web.builders.WebSecurity)
     */
    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers(PAGE_CONSTANTS);
    }

    /**
     * @param auth
     * @throws Exception
     */
    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        final String domain = ApplicationProperties.get(ACTIVE_DIRECTORY_DOMAIN);
        final String host = ApplicationProperties.get(ACTIVE_DIRECTORY_SERVER_NAME);
        final String port = ApplicationProperties.get(ACTIVE_DIRECTORY_SERVER_PORT);

        if (LOGGER.isDebugEnabled())
            LOGGER.debug("TOC AuthenticationManagerBuilder initialized");
        ActiveDirectoryLdapAuthenticationProvider provider = new ActiveDirectoryLdapAuthenticationProvider(domain,
                "ldap://" + host + ":" + port);
        provider.setConvertSubErrorCodesToExceptions(true);
        provider.setUseAuthenticationRequestCredentials(true);
        provider.setAuthoritiesMapper(grantedAuthoritiesMapper);
        auth.authenticationProvider(provider);
    }
}
