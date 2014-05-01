package com.siemens.cto.aem.service.exception;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class RecordNotUpdatedExceptionTest {

    @Test
    public void testRecordNotUpdatedException() {
        final RecordNotUpdatedException exception =
                new RecordNotUpdatedException(RecordNotUpdatedExceptionTest.class, "name", null);
        assertEquals("Failed to update RecordNotUpdatedExceptionTest name!", exception.getMessage());
    }
}
