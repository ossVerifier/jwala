package com.cerner.jwala.service.exception;

/**
 * Created by Z003BPEJ on 2/24/14.
 */
public class RecordNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 1L;

    public RecordNotFoundException(Class<?> theClass, Long id) {
        super(theClass.getSimpleName() + " with id = " + id + " was not found!");
    }

    public RecordNotFoundException(Class<?> theClass, String name) {
        super(theClass.getSimpleName() + " with name = " + name + " was not found!");
    }

}
