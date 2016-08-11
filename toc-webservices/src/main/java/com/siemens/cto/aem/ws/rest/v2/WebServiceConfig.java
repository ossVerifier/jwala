package com.siemens.cto.aem.ws.rest.v2;

import com.siemens.cto.aem.service.jvm.JvmService;
import com.siemens.cto.aem.ws.rest.RestServiceErrorHandler;
import com.siemens.cto.aem.ws.rest.v2.service.group.GroupServiceRest;
import com.siemens.cto.aem.ws.rest.v2.service.group.impl.GroupServiceRestImpl;
import com.siemens.cto.aem.ws.rest.v2.service.jvm.JvmServiceRest;
import com.siemens.cto.aem.ws.rest.v2.service.jvm.JvmServiceRestImpl;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * Web Service Spring Configuration
 *
 * Created by JC043760 on 8/10/2016
 */
@Configuration
public class WebServiceConfig {

    @Bean
    public Server getJaxRestServer(final GroupServiceRest groupServiceRest, final JvmServiceRest jvmServiceRest) {
        final JAXRSServerFactoryBean factory = new JAXRSServerFactoryBean();

        factory.setAddress("/v2.0");

        final List<Object> serviceBeans = new ArrayList<>();
        serviceBeans.add(groupServiceRest);
        serviceBeans.add(jvmServiceRest);
        factory.setServiceBeans(serviceBeans);

        List<Object> providers = new ArrayList<>();
        providers.add(getJacksonJsonProvider());
        providers.add(getInternalServerErrorHandler());
        factory.setProviders(providers);
        return factory.create();
    }

    @Bean
    public GroupServiceRest getGroupServiceRest() {
        return new GroupServiceRestImpl();
    }

    @Bean
    public JvmServiceRest getJvmServiceRest(final JvmService jvmService) {
        return new JvmServiceRestImpl(jvmService);
    }

    @Bean
    public JacksonJsonProvider getJacksonJsonProvider() {
        return new JacksonJsonProvider();
    }

    @Bean
    public RestServiceErrorHandler getInternalServerErrorHandler() {
        return new RestServiceErrorHandler();
    }
}
