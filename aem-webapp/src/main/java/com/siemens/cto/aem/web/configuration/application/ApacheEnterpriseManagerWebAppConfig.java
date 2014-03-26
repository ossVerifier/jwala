package com.siemens.cto.aem.web.configuration.application;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.siemens.cto.aem.service.configuration.application.ApacheEnterpriseManagerServiceAppConfigReference;

@Configuration
@Import(ApacheEnterpriseManagerServiceAppConfigReference.class)
public class ApacheEnterpriseManagerWebAppConfig {
}
