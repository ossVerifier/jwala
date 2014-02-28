package com.siemens.cto.aem.service.exception;

/**
 * Created by Z003BPEJ on 2/27/14.
 */
public class RecordNotUpdatedException extends RuntimeException {

    public RecordNotUpdatedException(Class aClass, String name, Throwable cause) {
        super("Failed to update " + aClass.getSimpleName() + " " + name + "!", cause);
    }

}