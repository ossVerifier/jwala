package com.siemens.cto.aem.web.configuration.application;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.siemens.cto.aem.configuration.AemAppConfigReference;

@Configuration
@Import(AemAppConfigReference.class)
public class ApacheEnterpriseManagerWebAppConfig {
}
