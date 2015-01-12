package com.siemens.cto.aem.common.exception;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class NotFoundExceptionTest {

    @Test
    public void testGetMessageResponseStatus() {
        final MessageResponseStatus messageResponseStatus = new TestMessageResponseStatus();
        final NotFoundException notFoundException = new NotFoundException(messageResponseStatus, "message");
        assertEquals(messageResponseStatus, notFoundException.getMessageResponseStatus());
    }
}
