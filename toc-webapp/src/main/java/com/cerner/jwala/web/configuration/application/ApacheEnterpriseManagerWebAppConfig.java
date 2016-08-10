package com.cerner.jwala.web.configuration.application;

import com.cerner.jwala.service.configuration.jms.AemJmsConfigReference;
import com.cerner.jwala.service.configuration.service.AemServiceConfigReference;
import com.cerner.jwala.web.security.SecurityConfig;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({AemServiceConfigReference.class,
        AemJmsConfigReference.class, SecurityConfig.class})
@ComponentScan("com.cerner.jwala.toc.files.configuration")
public class ApacheEnterpriseManagerWebAppConfig {
}
