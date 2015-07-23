package com.siemens.cto.aem.web.configuration.application;

import com.siemens.cto.aem.configuration.AemAppConfigReference;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(AemAppConfigReference.class)
public class ApacheEnterpriseManagerWebAppConfig {
}
