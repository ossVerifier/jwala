package com.siemens.cto.aem.service.configuration.service;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

@Configuration
@ImportResource({
    "classpath:META-INF/spring/integration.xml",
    "classpath:META-INF/spring/integration-state.xml"
})
public class AemIntegrationConfig {

}
