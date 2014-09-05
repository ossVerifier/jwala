package com.siemens.cto.aem.service.webserver

import com.siemens.cto.aem.domain.model.jvm.Jvm
import com.siemens.cto.aem.domain.model.webserver.WebServer
import com.siemens.cto.aem.service.webserver.exception.TemplateNotFoundException
import groovy.text.GStringTemplateEngine
import com.siemens.cto.aem.domain.model.app.Application

/**
 * Wrapper that contains methods that generates configuration files for an Apache Web Server
 *
 * Created by Z003BPEJ on 6/20/14.
 */
public class ApacheWebServerConfigFileGenerator {

    private ApacheWebServerConfigFileGenerator() {}

    /**
     * Generate httpd.conf content
     * @param templateFileName the template file name
     * @param apps the applications
     * @return generated httpd.conf content
     */
    public static String getHttpdConf(final String webServerName,
                                      final String templateFileName,
                                      final WebServer webServer,
                                      final List<Jvm> jvms,
                                      final List<Application> apps
                                      ) {
        final binding = [webServerName:webServerName,
                         webServer:webServer,
                         apps:apps.collect {app:[mount: it.webAppContext + "/*", name: it.name]},
                         jvms:jvms.collect {jvm:it},
                         comments:""]
        return bindDataToTemplate(binding, templateFileName).toString()
    }

    /**
     * Generate workers.properties content
     * @param templateFileName the template file name
     * @param apps the applications
     * @return generated worker.properties content
     */
    public static String getWorkersProperties(final String webServerName,
                                              final String templateFileName,
                                              final List<Jvm> jvms,
                                              final List<Application> apps) {
        final binding = [webServerName: webServerName,
                         jvms:jvms.collect {jvm:[jvmName: it.jvmName, hostName: it.hostName, ajpPort: it.ajpPort]},
                         apps:apps.collect {app:[name: it.name]},
                         comments:""]
        return bindDataToTemplate(binding, templateFileName).toString()
    }

    private static bindDataToTemplate(final binding, final String templateFileName) {
        def resource = new File(templateFileName)
        binding.comments = "Generated from " + templateFileName
        if (!resource.exists()) {
            resource = this.getResource(templateFileName)
            binding.comments += " classpath resource template"
        }

        final engine = new GStringTemplateEngine()

        if (resource == null) {
            throw new TemplateNotFoundException(templateFileName, null)
        }

        return engine.createTemplate(resource.text).make(binding)
    }

}