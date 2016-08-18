package com.cerner.jwala.service.exception;

import org.junit.Test;

import com.cerner.jwala.service.exception.RecordNotDeletedException;

import static org.junit.Assert.assertEquals;

public class RecordNotDeletedExceptionTest {

    @Test
    public void testRecordNotDeletedException() {
        final RecordNotDeletedException exception =
                new RecordNotDeletedException(RecordNotDeletedExceptionTest.class, 1l, null);
        assertEquals("Failed to delete RecordNotDeletedExceptionTest 1!", exception.getMessage());
    }
}
