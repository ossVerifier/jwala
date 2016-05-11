package com.siemens.cto.aem.template;

import com.siemens.cto.aem.template.webserver.exception.TemplateNotFoundException
import groovy.text.GStringTemplateEngine

public class GeneratorUtils {

    public static String bindDataToTemplate(final binding, final String templateFileName) {
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

    @Deprecated
    public static String bindDataToTemplateText(final binding, final String templateText) {
        final engine = new GStringTemplateEngine()
        return engine.createTemplate(templateText).make(binding.withDefault{''})
    }

}
