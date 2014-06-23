package com.siemens.cto.aem.service.webserver.exception

/**
 * Runtime exception wrapper for missing httpd.conf template file
 *
 * Created by Z003BPEJ on 6/23/14.
 */
public class HttpdConfigTemplateNotFoundException extends RuntimeException {

    public HttpdConfigTemplateNotFoundException(String file, FileNotFoundException e) {
        super("HTTPD Configuration File Template " + file + " was not found!", e)
    }

}
