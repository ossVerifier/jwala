package com.siemens.cto.aem.common.exception;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class FaultCodeExceptionTest {

    @Test
    public void testGetMessageResponseStatus() {
        final MessageResponseStatus messageResponseStatus = new TestMessageResponseStatus();
        final FaultCodeException faultCodeException = new FaultCodeException(messageResponseStatus, "message");
        assertEquals(messageResponseStatus, faultCodeException.getMessageResponseStatus());
    }
}
