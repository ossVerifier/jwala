package com.siemens.cto.aem.service.configuration.application;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * Load configurations found in the "Test" context
 */
@Configuration
@PropertySource("classpath:datasource-test.properties")
public class IntegrationTestPropertiesConfig {
}
