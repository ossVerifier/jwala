package com.siemens.cto.aem.service.exception;

/**
 * Created by Z003BPEJ on 2/26/14.
 */
public class RecordNotAddedException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public RecordNotAddedException(Class<?> theClass, String name, Throwable cause) {
        super("Failed to add " + theClass.getSimpleName() + " " + name + "!", cause);
    }

}
