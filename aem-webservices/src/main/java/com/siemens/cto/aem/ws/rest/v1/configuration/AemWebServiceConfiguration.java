package com.siemens.cto.aem.ws.rest.v1.configuration;

import java.util.ArrayList;
import java.util.List;

import org.apache.cxf.endpoint.Server;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.siemens.cto.aem.service.group.GroupService;
import com.siemens.cto.aem.service.jvm.JvmService;
import com.siemens.cto.aem.service.webserver.WebServerService;
import com.siemens.cto.aem.ws.rest.v1.exceptionmapper.BadRequestExceptionMapper;
import com.siemens.cto.aem.ws.rest.v1.exceptionmapper.NotFoundExceptionMapper;
import com.siemens.cto.aem.ws.rest.v1.service.group.GroupServiceRest;
import com.siemens.cto.aem.ws.rest.v1.service.group.impl.GroupServiceRestImpl;
import com.siemens.cto.aem.ws.rest.v1.service.jvm.JvmServiceRest;
import com.siemens.cto.aem.ws.rest.v1.service.jvm.impl.JvmServiceRestImpl;
import com.siemens.cto.aem.ws.rest.v1.service.webserver.WebServerServiceRest;
import com.siemens.cto.aem.ws.rest.v1.service.webserver.impl.WebServerServiceRestImpl;

@Configuration
public class AemWebServiceConfiguration {

    @Autowired
    private GroupService groupService;

    @Autowired
    private JvmService jvmService;

    @Autowired
    private WebServerService webServerService;

    @Bean
    public Server getV1JaxResServer() {

        final JAXRSServerFactoryBean factory = new JAXRSServerFactoryBean();
        factory.setAddress("/");
        factory.setServiceBeans(getV1ServiceBeans());
        factory.setProviders(getV1Providers());

        final Server server = factory.create();

        return server;
    }

    @Bean
    public List<Object> getV1ServiceBeans() {
        final List<Object> serviceBeans = new ArrayList<>();

        serviceBeans.add(getV1GroupServiceRest());
        serviceBeans.add(getV1JvmServiceRest());
        serviceBeans.add(getV1WebServerServiceRest());

        return serviceBeans;
    }

    @Bean
    public GroupServiceRest getV1GroupServiceRest() {
        return new GroupServiceRestImpl(groupService);
    }

    @Bean
    public JvmServiceRest getV1JvmServiceRest() {
        return new JvmServiceRestImpl(jvmService);
    }

    @Bean
    public WebServerServiceRest getV1WebServerServiceRest() {
        return new WebServerServiceRestImpl(webServerService);
    }

    @Bean
    public List<?> getV1Providers() {
        final List<? super Object> providers = new ArrayList<>();

        providers.add(getV1JsonProvider());
        providers.add(getV1NotFoundExceptionMapper());
        providers.add(getV1BadRequestExceptionMapper());

        return providers;
    }

    @Bean
    public JacksonJsonProvider getV1JsonProvider() {
        return new JacksonJsonProvider();
    }

    @Bean
    public NotFoundExceptionMapper getV1NotFoundExceptionMapper() {
        return new NotFoundExceptionMapper();
    }

    @Bean
    public BadRequestExceptionMapper getV1BadRequestExceptionMapper() {
        return new BadRequestExceptionMapper();
    }

}
