package com.siemens.cto.aem.service.webserver.exception;

/**
 * Created by JC043760 on 5/13/2016.
 */
public class WebServerServiceException extends RuntimeException {
    public WebServerServiceException(final String msg, final Throwable t) {
        super(msg, t);
    }
}
