package com.siemens.cto.aem.service.configuration.service;

import com.siemens.cto.aem.persistence.service.ResourceDao;
import com.siemens.cto.aem.service.resource.impl.handler.*;
import org.springframework.context.annotation.Bean;

/**
 * Resource handler wiring and configuration.
 * Note: The resource handlers implement the chain of responsibility pattern.
 *
 * Created by JC043760 on 7/21/2016
 */
public class ResourceHandlerConfiguration {

    @Bean
    public WebServerResourceHandler getWebServerResourceHandler(final ResourceDao resourceDao,
                                                                final JvmResourceHandler jvmResourceHandler) {
        return new WebServerResourceHandler(resourceDao, jvmResourceHandler);
    }

    @Bean
    public JvmResourceHandler getJvmResourceHandler(final ResourceDao resourceDao,
                                                    final AppResourceHandler appResourceHandler) {
        return new JvmResourceHandler(resourceDao, appResourceHandler);
    }

    @Bean
    public AppResourceHandler getAppResourceHandler(final ResourceDao resourceDao,
                                                    final GroupLevelWebServerResourceHandler groupLevelWebServerResourceHandler) {
        return new AppResourceHandler(resourceDao, groupLevelWebServerResourceHandler);
    }

    @Bean
    public GroupLevelWebServerResourceHandler getGroupLevelWebServerResourceHandler(final ResourceDao resourceDao,
                                                                                    final GroupLevelJvmResourceHandler groupLevelJvmResourceHandler) {
        return new GroupLevelWebServerResourceHandler(resourceDao, groupLevelJvmResourceHandler);
    }

    @Bean
    public GroupLevelJvmResourceHandler getDroupLevelJvmResourceHandler(final ResourceDao resourceDao,
                                                                        final GroupLevelAppResourceHandler groupLevelAppResourceHandler) {
        return new GroupLevelJvmResourceHandler(resourceDao, groupLevelAppResourceHandler);
    }

    @Bean
    public GroupLevelAppResourceHandler getGroupLevelAppResourceHandler(final ResourceDao resourceDao, final ExternalPropertiesResourceHandler extPropertiesResourceHandler) {
        return new GroupLevelAppResourceHandler(resourceDao, extPropertiesResourceHandler);
    }

    @Bean
    public ExternalPropertiesResourceHandler getExternalPropertiesResourceHandler(final ResourceDao resourceDao){
        return new ExternalPropertiesResourceHandler(resourceDao, null);
    }
}
