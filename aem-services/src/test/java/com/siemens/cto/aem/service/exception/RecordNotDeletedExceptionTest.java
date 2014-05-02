package com.siemens.cto.aem.service.exception;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class RecordNotDeletedExceptionTest {

    @Test
    public void testRecordNotDeletedException() {
        final RecordNotDeletedException exception =
                new RecordNotDeletedException(RecordNotDeletedExceptionTest.class, 1l, null);
        assertEquals("Failed to delete RecordNotDeletedExceptionTest 1!", exception.getMessage());
    }
}
