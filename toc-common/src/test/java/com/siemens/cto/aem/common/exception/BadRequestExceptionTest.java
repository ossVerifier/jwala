package com.siemens.cto.aem.common.exception;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BadRequestExceptionTest {

    @Test
    public void testGetMessageResponseStatus() {
        final MessageResponseStatus messageResponseStatus = new TestMessageResponseStatus();
        final BadRequestException badRequestException = new BadRequestException(messageResponseStatus, "message");
        assertEquals(messageResponseStatus, badRequestException.getMessageResponseStatus());
    }
}
