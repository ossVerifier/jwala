package com.siemens.cto.aem.common.exception;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class FaultCodeExceptionTest {

    @Test
    public void testGetMessageResponseStatus() {
        final MessageResponseStatus messageResponseStatus = new TestMessageResponseStatus();
        final FaultCodeException faultCodeException = new FaultCodeException(messageResponseStatus, "message");
        assertEquals(messageResponseStatus, faultCodeException.getMessageResponseStatus());
    }
}
