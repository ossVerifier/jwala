package com.siemens.cto.aem.service.exception;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class RecordNotAddedExceptionTest {

    @Test
    public void testRecordNotAddedException() {
        final RecordNotAddedException exception =
                new RecordNotAddedException(RecordNotAddedExceptionTest.class, "name", null);
        assertEquals("Failed to add RecordNotAddedExceptionTest name!", exception.getMessage());
    }
}
