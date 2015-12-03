package com.siemens.cto.aem.service.configuration.jms;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({AemJmsConfig.class,
         AemMessageListenerConfig.class})
public class AemJmsConfigReference {
}
