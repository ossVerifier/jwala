package com.siemens.cto.aem.web.configuration.application;

import com.siemens.cto.aem.service.configuration.jms.AemJmsConfigReference;
import com.siemens.cto.aem.service.configuration.service.AemServiceConfigReference;
import com.siemens.cto.aem.web.security.SecurityConfig;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({AemServiceConfigReference.class,
        AemJmsConfigReference.class, SecurityConfig.class})
@ComponentScan("com.siemens.cto.toc.files.configuration")
public class ApacheEnterpriseManagerWebAppConfig {
}
