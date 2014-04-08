package com.siemens.cto.aem.service.configuration.application;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.siemens.cto.aem.persistence.configuration.AemDaoConfiguration;
import com.siemens.cto.aem.persistence.configuration.AemPersistenceConfigurationReference;
import com.siemens.cto.aem.service.configuration.service.AemServiceConfiguration;
import com.siemens.cto.aem.service.configuration.transaction.AemTransactionConfiguration;

@Configuration
@Import({AemServiceConfiguration.class,
         AemPersistenceConfigurationReference.class,
         AemDaoConfiguration.class,
         AemTransactionConfiguration.class})
public class AemServiceAppConfigReference {

}
