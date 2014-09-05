package com.siemens.cto.aem.service;

import com.siemens.cto.aem.service.webserver.exception.TemplateNotFoundException
import groovy.text.GStringTemplateEngine

public class GeneratorUtils {
    
    static String bindDataToTemplate(final binding, final String templateFileName) {
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
