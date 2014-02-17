package com.siemens.cto.aem.ws.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

@Configuration
@ImportResource({"classpath*:META-INF/cxf/cxf.xml",
                 "classpath*:META-INF/cxf/cxf-extension-soap.xml",
                 "classpath*:META-INF/cxf/cxf-servlet.xml",
                 "classpath:META-INF/ws-context.xml"})
public class RestConfig {

}
