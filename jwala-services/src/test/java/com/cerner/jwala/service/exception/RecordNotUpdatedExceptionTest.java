package com.cerner.jwala.service.exception;

import org.junit.Test;

import com.cerner.jwala.service.exception.RecordNotUpdatedException;

import static org.junit.Assert.assertEquals;

public class RecordNotUpdatedExceptionTest {

    @Test
    public void testRecordNotUpdatedException() {
        final RecordNotUpdatedException exception =
                new RecordNotUpdatedException(RecordNotUpdatedExceptionTest.class, "name", null);
        assertEquals("Failed to update RecordNotUpdatedExceptionTest name!", exception.getMessage());
    }
}
