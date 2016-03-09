package com.siemens.cto.aem.service.configuration.service;

import com.siemens.cto.aem.control.configuration.AemControlConfigReference;
import com.siemens.cto.aem.persistence.configuration.AemDaoConfiguration;
import com.siemens.cto.aem.persistence.configuration.AemPersistenceConfigurationReference;
import com.siemens.cto.aem.service.configuration.WebSocketConfig;
import com.siemens.cto.aem.service.configuration.transaction.AemTransactionConfiguration;
import com.siemens.cto.aem.template.configuration.TocTemplateConfigurationReference;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({AemServiceConfiguration.class,
         AemPersistenceConfigurationReference.class,
         AemDaoConfiguration.class,
         AemTransactionConfiguration.class,
         AemControlConfigReference.class,
         AemIntegrationConfig.class,
         TocTemplateConfigurationReference.class,
         WebSocketConfig.class
         })
public class AemServiceConfigReference {
}
