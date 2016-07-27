package com.siemens.cto.aem.service.configuration.service;

import com.siemens.cto.aem.persistence.service.*;
import com.siemens.cto.aem.service.resource.ResourceContentGeneratorService;
import com.siemens.cto.aem.service.resource.impl.handler.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

/**
 * Resource handler wiring and configuration.
 * Note: The resource handlers implement the chain of responsibility pattern.
 *
 * Created by JC043760 on 7/21/2016
 */
@ComponentScan({"com.siemens.cto.aem.service.resource.impl"})
public class ResourceHandlerConfiguration {

    @Bean
    public WebServerResourceHandler getWebServerResourceHandler(final ResourceDao resourceDao,
                                                                final WebServerPersistenceService webServerPersistenceService,
                                                                final ResourceContentGeneratorService resourceContentGeneratorService,
                                                                final JvmResourceHandler jvmResourceHandler) {
        return new WebServerResourceHandler(resourceDao, webServerPersistenceService, resourceContentGeneratorService,
                                            jvmResourceHandler);
    }

    @Bean
    public JvmResourceHandler getJvmResourceHandler(final ResourceDao resourceDao,
                                                    final GroupPersistenceService groupPersistenceService,
                                                    final JvmPersistenceService jvmPersistenceService,
                                                    final AppResourceHandler appResourceHandler) {
        return new JvmResourceHandler(resourceDao, groupPersistenceService, jvmPersistenceService, appResourceHandler);
    }

    @Bean
    public AppResourceHandler getAppResourceHandler(final ResourceDao resourceDao,
                                                    final JvmPersistenceService jvmPersistenceService,
                                                    final ApplicationPersistenceService applicationPersistenceService,
                                                    final GroupLevelWebServerResourceHandler groupLevelWebServerResourceHandler) {
        return new AppResourceHandler(resourceDao, jvmPersistenceService, applicationPersistenceService,
                groupLevelWebServerResourceHandler);
    }

    @Bean
    public GroupLevelWebServerResourceHandler getGroupLevelWebServerResourceHandler(final ResourceDao resourceDao,
                                                                                    final GroupPersistenceService groupPersistenceService,
                                                                                    final WebServerPersistenceService webServerPersistenceService,
                                                                                    final ResourceContentGeneratorService resourceContentGeneratorService,
                                                                                    final GroupLevelJvmResourceHandler groupLevelJvmResourceHandler) {
        return new GroupLevelWebServerResourceHandler(resourceDao, groupPersistenceService, webServerPersistenceService,
                resourceContentGeneratorService, groupLevelJvmResourceHandler);
    }

    @Bean
    public GroupLevelJvmResourceHandler getDroupLevelJvmResourceHandler(final ResourceDao resourceDao,
                                                                        final GroupPersistenceService groupPersistenceService,
                                                                        final JvmPersistenceService jvmPersistenceService,
                                                                        final GroupLevelAppResourceHandler groupLevelAppResourceHandler) {
        return new GroupLevelJvmResourceHandler(resourceDao, groupPersistenceService, jvmPersistenceService,
                groupLevelAppResourceHandler);
    }

    @Bean
    public GroupLevelAppResourceHandler getGroupLevelAppResourceHandler(final ResourceDao resourceDao,
                                                                        final GroupPersistenceService groupPersistenceService,
                                                                        final JvmPersistenceService jvmPersistenceService,
                                                                        final ApplicationPersistenceService applicationPersistenceService,
                                                                        final ExternalPropertiesResourceHandler extPropertiesResourceHandler) {
        return new GroupLevelAppResourceHandler(resourceDao, groupPersistenceService, jvmPersistenceService,
                applicationPersistenceService, extPropertiesResourceHandler);
    }

    @Bean
    public ExternalPropertiesResourceHandler getExternalPropertiesResourceHandler(final ResourceDao resourceDao){
        return new ExternalPropertiesResourceHandler(resourceDao, null);
    }
}
