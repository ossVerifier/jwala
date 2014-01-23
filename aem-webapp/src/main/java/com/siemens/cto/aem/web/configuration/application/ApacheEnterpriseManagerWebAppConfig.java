package com.siemens.cto.aem.web.configuration.application;

import com.siemens.cto.aem.service.configuration.application.ApacheEnterpriseManagerServiceAppConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(ApacheEnterpriseManagerServiceAppConfig.class)
public class ApacheEnterpriseManagerWebAppConfig {
}
