package com.cerner.jwala.service.resource.impl.handler.exception;

/**
 * Exception wrapper for {@link com.cerner.jwala.service.resource.ResourceHandler} exceptions
 *
 * Created by JC043760 on 7/26/2016.
 */
public class ResourceHandlerException extends RuntimeException {

    public ResourceHandlerException(final String s, final Throwable throwable) {
        super(s, throwable);
    }
}
