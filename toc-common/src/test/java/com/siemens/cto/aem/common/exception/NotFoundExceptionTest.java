package com.siemens.cto.aem.common.exception;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class NotFoundExceptionTest {

    @Test
    public void testGetMessageResponseStatus() {
        final MessageResponseStatus messageResponseStatus = new TestMessageResponseStatus();
        final NotFoundException notFoundException = new NotFoundException(messageResponseStatus, "message");
        assertEquals(messageResponseStatus, notFoundException.getMessageResponseStatus());
    }
}
