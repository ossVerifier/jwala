package com.siemens.cto.aem.web.configuration.application;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.siemens.cto.aem.service.configuration.application.AemServiceAppConfigReference;

@Configuration
@Import(AemServiceAppConfigReference.class)
public class ApacheEnterpriseManagerWebAppConfig {
}
