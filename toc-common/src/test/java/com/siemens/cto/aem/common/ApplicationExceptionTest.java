package com.siemens.cto.aem.common;

import com.siemens.cto.aem.common.exception.ApplicationException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ApplicationExceptionTest {

    private static final String MESSAGE = "message";

    @Test
    public void testApplicationException() {
        final ApplicationException applicationException = new ApplicationException();
        assertNull(applicationException.getMessage());
    }

    @Test
    public void testApplicationExceptionString() {
        final ApplicationException applicationException = new ApplicationException(MESSAGE);
        assertEquals(MESSAGE, applicationException.getMessage());
    }

    @Test
    public void testApplicationExceptionStringThrowable() {
        final Throwable throwable = new Throwable("throwable");
        final ApplicationException applicationException = new ApplicationException(MESSAGE, throwable);
        assertEquals(MESSAGE, applicationException.getMessage());
        assertEquals(throwable, applicationException.getCause());
    }

    @Test
    public void testApplicationExceptionThrowable() {
        final Throwable throwable = new Throwable("throwable");
        final ApplicationException applicationException = new ApplicationException(throwable);
        assertEquals(throwable, applicationException.getCause());
    }
}
