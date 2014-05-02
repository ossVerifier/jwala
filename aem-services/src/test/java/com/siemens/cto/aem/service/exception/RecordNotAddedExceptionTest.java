package com.siemens.cto.aem.service.exception;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class RecordNotAddedExceptionTest {

    @Test
    public void testRecordNotAddedException() {
        final RecordNotAddedException exception =
                new RecordNotAddedException(RecordNotAddedExceptionTest.class, "name", null);
        assertEquals("Failed to add RecordNotAddedExceptionTest name!", exception.getMessage());
    }
}
