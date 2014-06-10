package com.siemens.cto.aem.configuration;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.siemens.cto.aem.service.configuration.jms.AemJmsConfigReference;
import com.siemens.cto.aem.service.configuration.service.AemServiceConfigReference;

@Configuration
@Import({AemServiceConfigReference.class,
         AemJmsConfigReference.class})
@ComponentScan("com.siemens.cto.toc.files.configuration")
public class AemAppConfigReference {

}
