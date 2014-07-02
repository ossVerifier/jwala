package com.siemens.cto.aem.service.webserver

import com.siemens.cto.aem.service.webserver.exception.HttpdConfigTemplateNotFoundException
import groovy.text.GStringTemplateEngine
import com.siemens.cto.aem.domain.model.app.Application

/**
 * Wrapper that contains methods that generates configuration files for an Apache Web Server
 *
 * Created by Z003BPEJ on 6/20/14.
 */
public class ApacheWebServerConfigFileGenerator {

    /**
     * This private constructor was meant to prevent
     * instantiation of the class but it does not work
     * in groovy. There is actually a jira bug report
     * about it
     *
     * http://jira.codehaus.org/browse/GROOVY-3010
     */
    private ApacheWebServerConfigFileGenerator() {}

    def public static String getHttpdConf(String templateFileName, List<Application> apps) {
        def binding = [apps:apps.collect {app:[mount: it.webAppContext + "/*", name: it.name]}]

        def resource = this.getResource(templateFileName)

        if (resource == null) {
            resource = new File(templateFileName)
        }

        def engine = new GStringTemplateEngine()

        def template
        try {
            template = engine.createTemplate(resource.text).make(binding)
        } catch (FileNotFoundException e) {
            throw new HttpdConfigTemplateNotFoundException(templateFileName, e)
        }

        return template.toString()
    }

}