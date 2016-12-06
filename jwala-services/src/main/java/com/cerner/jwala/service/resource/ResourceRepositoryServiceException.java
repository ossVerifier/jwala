package com.cerner.jwala.service.resource;

/**
 * Exception thrown by {@link ResourceRepositoryService} as a result of failed operations
 *
 * Created by Jedd Anthony Cuison on 11/30/2016
 */
public class ResourceRepositoryServiceException extends RuntimeException {

    public ResourceRepositoryServiceException(final String msg) {
        super(msg);
    }

    public ResourceRepositoryServiceException(final String msg, final Throwable throwable) {
        super(msg, throwable);
    }
}
