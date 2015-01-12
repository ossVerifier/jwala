package com.siemens.cto.aem.service.exception;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class RecordNotFoundExceptionTest {

    @Test
    public void testRecordNotFoundExceptionLong() {
        final RecordNotFoundException exception = new RecordNotFoundException(RecordNotFoundExceptionTest.class, 1l);
        assertEquals("RecordNotFoundExceptionTest with id = 1 was not found!", exception.getMessage());
    }

    @Test
    public void testRecordNotFoundExceptionString() {
        final RecordNotFoundException exception =
                new RecordNotFoundException(RecordNotFoundExceptionTest.class, "name");
        assertEquals("RecordNotFoundExceptionTest with name = name was not found!", exception.getMessage());
    }
}
