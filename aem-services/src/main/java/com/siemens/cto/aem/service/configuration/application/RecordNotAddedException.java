package com.siemens.cto.aem.service.configuration.application;

/**
 * Created by Z003BPEJ on 2/26/14.
 */
public class RecordNotAddedException extends RuntimeException {

    public RecordNotAddedException(Class theClass, String name, Throwable cause) {
        super("Failed to add " + theClass.getSimpleName() + " " + name + "!", cause);
    }

}
