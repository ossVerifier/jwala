package com.cerner.jwala.service.exception;

import org.junit.Test;

import com.cerner.jwala.service.exception.RecordNotAddedException;

import static org.junit.Assert.assertEquals;

public class RecordNotAddedExceptionTest {

    @Test
    public void testRecordNotAddedException() {
        final RecordNotAddedException exception =
                new RecordNotAddedException(RecordNotAddedExceptionTest.class, "name", null);
        assertEquals("Failed to add RecordNotAddedExceptionTest name!", exception.getMessage());
    }
}
