package com.cerner.jwala.web.security;

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

import com.cerner.jwala.common.properties.ApplicationProperties;

/**
 * @author AK048646
 *
 */
@Configuration
@ComponentScan("com.cerner.jwala.web.security")
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    
    @Autowired
    GrantedAuthoritiesMapperImpl  grantedAuthoritiesMapper;
    
    //for Test Injection
    /**
     * @param grantedAuthoritiesMapper
     */
    public SecurityConfig(GrantedAuthoritiesMapperImpl  grantedAuthoritiesMapper) {
        this.grantedAuthoritiesMapper = grantedAuthoritiesMapper;
    }
    
    private static Logger LOGGER = Logger.getLogger(SecurityConfig.class);
    private static final String ACTIVE_DIRECTORY_DOMAIN = "active.directory.fqdn";
    private static final String ACTIVE_DIRECTORY_SERVER_NAME = "active.directory.server.name";
    private static final String ACTIVE_DIRECTORY_SERVER_PORT = "active.directory.server.port";
    private static final String ACTIVE_DIRECTORY_PROTOCOL = "active.directory.server.protocol";
    private static final String TOC_AUTH_ENABLED = "toc.authorization";

    
    private static final String LOGIN_PAGE ="/login";
    private static final String LOGIN_API = "/**/user/login";
    private static final String LOGOUT_API = "/**/user/logout";

    private static final String GEN_PUBLIC_RESOURCES = "/gen-public-resources/**";
    private static final String PUBLIC_RESOURCES = "/public-resources/**";
    private static final String PAGE_CONSTANTS = "/page-constants";
    
    private static final String  WEBSERVER_CONF_URL = "/**/webservers/**/conf/deploy";
    private static final String  WEBSERVER_GENERATE_URL = "/toc/**/groups/**/webservers/conf/deploy";
    private static final String  JVM_CONF_URL = "/**/jvms/**/conf";
    private static final String  IDP_URL = "/idp";

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.security.config.annotation.web.configuration.
     * WebSecurityConfigurerAdapter#configure(org.springframework.security.
     * config.annotation.web.builders.HttpSecurity)
     */
     
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        final String ADMIN = ApplicationProperties.get("toc.role.admin");
        final String AUTH = ApplicationProperties.get(TOC_AUTH_ENABLED, "true");
        //ACL check
        if(!"false".equalsIgnoreCase(AUTH)){
            http.authorizeRequests().antMatchers(IDP_URL, 
                                                 WEBSERVER_GENERATE_URL, 
                                                 WEBSERVER_CONF_URL, 
                                                 JVM_CONF_URL).hasAnyAuthority(ADMIN);
        }
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
        final String protocol = ApplicationProperties.get(ACTIVE_DIRECTORY_PROTOCOL);
        if (LOGGER.isDebugEnabled())
            LOGGER.debug("TOC AuthenticationManagerBuilder initialized");
        ActiveDirectoryLdapAuthenticationProvider provider = new ActiveDirectoryLdapAuthenticationProvider(domain,
                protocol + "://" + host + ":" + port);
        provider.setConvertSubErrorCodesToExceptions(true);
        provider.setUseAuthenticationRequestCredentials(true);
        provider.setAuthoritiesMapper(grantedAuthoritiesMapper);
        auth.authenticationProvider(provider);
    }
}
