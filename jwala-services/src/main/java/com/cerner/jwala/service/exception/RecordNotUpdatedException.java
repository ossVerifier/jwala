package com.cerner.jwala.service.exception;

/**
 * Created by Jedd Cuison on 2/27/14.
 */
public class RecordNotUpdatedException extends RuntimeException {

	private static final long serialVersionUID = 1L;

    public RecordNotUpdatedException(Class<?> aClass, String name, Throwable cause) {
        super("Failed to update " + aClass.getSimpleName() + " " + name + "!", cause);
    }

}