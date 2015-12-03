package com.siemens.cto.aem.service.exception;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class RecordNotDeletedExceptionTest {

    @Test
    public void testRecordNotDeletedException() {
        final RecordNotDeletedException exception =
                new RecordNotDeletedException(RecordNotDeletedExceptionTest.class, 1l, null);
        assertEquals("Failed to delete RecordNotDeletedExceptionTest 1!", exception.getMessage());
    }
}
