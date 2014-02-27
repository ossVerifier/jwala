package com.siemens.cto.aem.service.exception;

/**
 * Created by Z003BPEJ on 2/24/14.
 */
public class RecordNotFoundException extends RuntimeException {

    public RecordNotFoundException(Class theClass, Long id) {
        super(theClass.getSimpleName() + " with id = " + id + " was not found!");
    }

}
