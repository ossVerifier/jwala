package com.cerner.jwala.service.exception;

/**
 * Exception wrapper from {@link com.cerner.jwala.service.resource.ResourceService}.
 *
 * Created by JC043760 on 3/30/2016.
 */
public class ResourceServiceException extends RuntimeException {

    public ResourceServiceException(Throwable throwable) {
        super(throwable);
    }

    public ResourceServiceException(String s) {
        super(s);
    }

}
