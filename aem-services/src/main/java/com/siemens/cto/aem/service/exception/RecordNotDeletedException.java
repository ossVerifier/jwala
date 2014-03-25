package com.siemens.cto.aem.service.exception;

/**
 * Created by Z003BPEJ on 2/27/14.
 */
public class RecordNotDeletedException extends RuntimeException {

	private static final long serialVersionUID = 1L;

    public RecordNotDeletedException(Class<?> aClass, Long id, Exception e) {
        super("Failed to delete " + aClass.getSimpleName() + " " + id + "!", e);
    }

}
