package com.siemens.cto.aem.common.exception;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SuccessTest {

    @Test
    public void testGetMessageCode() {
        assertEquals("0", Success.SUCCESS.getMessageCode());
    }

    @Test
    public void testGetMessage() {
        assertEquals("SUCCESS", Success.SUCCESS.getMessage());
    }

}
