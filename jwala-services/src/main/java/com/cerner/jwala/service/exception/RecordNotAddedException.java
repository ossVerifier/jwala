package com.cerner.jwala.service.exception;

/**
 * Created by Jedd Cuison on 2/26/14.
 */
public class RecordNotAddedException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public RecordNotAddedException(Class<?> theClass, String name, Throwable cause) {
        super("Failed to add " + theClass.getSimpleName() + " " + name + "!", cause);
    }

}
