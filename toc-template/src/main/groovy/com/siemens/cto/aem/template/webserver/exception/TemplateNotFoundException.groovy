package com.siemens.cto.aem.template.webserver.exception

/**
 * Runtime exception wrapper for missing template file
 *
 * Created by Z003BPEJ on 6/23/14.
 */
public class TemplateNotFoundException extends RuntimeException {

    public TemplateNotFoundException(final String file, final FileNotFoundException e) {
        super("Template " + file + " was not found!", e)
    }

}
