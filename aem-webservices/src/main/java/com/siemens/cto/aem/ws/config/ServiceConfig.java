package com.siemens.cto.aem.ws.config;

import com.siemens.cto.aem.service.JvmInfoService;
import com.siemens.cto.aem.service.JvmInfoServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServiceConfig {

    @Bean
    public JvmInfoService jvmInfoService() {
        return new JvmInfoServiceImpl();
    }

}
