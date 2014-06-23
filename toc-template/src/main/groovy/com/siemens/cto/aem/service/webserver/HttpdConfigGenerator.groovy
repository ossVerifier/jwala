package com.siemens.cto.aem.service.webserver

import com.siemens.cto.aem.service.webserver.exception.HttpdConfigTemplateNotFoundException
import groovy.text.GStringTemplateEngine

/**
 * Wrapper that contains methods that generates configuration files for an Apache Web Server
 *
 * Created by Z003BPEJ on 6/20/14.
 */
public class HttpdConfigGenerator {

    /**
     * This private constructor was meant to prevent
     * instantiation of the class but it does not work
     * in groovy. There is actually a jira bug report
     * about it
     *
     * http://jira.codehaus.org/browse/GROOVY-3010
     */
    private HttpdConfigGenerator() {}

    def static getHttpdConf(templateFileName, binding) {
        def resource = this.getClass().getResource(templateFileName)
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