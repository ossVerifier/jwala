package com.cerner.jwala.dao.exception;

/**
 * Created by Jedd Cuison on 9/21/2017
 */
public class PersistenceHelperException extends RuntimeException {

    public PersistenceHelperException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
