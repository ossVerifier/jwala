package com.siemens.cto.aem.service.webserver

import com.siemens.cto.aem.domain.model.jvm.Jvm
import com.siemens.cto.aem.service.webserver.exception.HttpdConfigTemplateNotFoundException
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
                                      final List<Application> apps) {
        final binding = [webServerName:webServerName,
                         apps:apps.collect {app:[mount: it.webAppContext + "/*", name: it.name]}]
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
                         apps:apps.collect {app:[name: it.name]}]
        return bindDataToTemplate(binding, templateFileName).toString()
    }

    private static bindDataToTemplate(final binding, final String templateFileName) {
        final resource = this.getResource(templateFileName)

        if (resource == null) {
            resource = new File(templateFileName)
        }

        final engine = new GStringTemplateEngine()

        try {
            return engine.createTemplate(resource.text).make(binding)
        } catch (FileNotFoundException e) {
            throw new HttpdConfigTemplateNotFoundException(templateFileName, e)
        }
    }

}