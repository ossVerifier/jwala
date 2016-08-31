package com.cerner.jwala.service;

/**
 * {@link DbServerService} exception wrapper
 *
 * Created by JC043760 on 8/30/2016.
 */
public class DbServiceException extends RuntimeException {

    public DbServiceException(final Throwable t) {
        super(t);
    }
}
