package com.cerner.jwala.service.configuration.service;

import com.cerner.jwala.control.configuration.AemControlConfigReference;
import com.cerner.jwala.persistence.configuration.AemPersistenceConfigurationReference;
import com.cerner.jwala.service.configuration.WebSocketConfig;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({AemServiceConfiguration.class,
         AemPersistenceConfigurationReference.class,
         AemControlConfigReference.class,
         WebSocketConfig.class,
         ResourceHandlerConfiguration.class})
public class AemServiceConfigReference {
}
