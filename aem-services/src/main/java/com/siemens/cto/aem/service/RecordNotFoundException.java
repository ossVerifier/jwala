package com.siemens.cto.aem.service;

/**
 * Created by Z003BPEJ on 2/24/14.
 */
public class RecordNotFoundException extends RuntimeException {

    public RecordNotFoundException(long id, String entity) {
        super(entity + " with id = " + id + " was not found!");
    }

}
