package com.siemens.cto.aem.domain.model.rule;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class HostNameRuleTest {
    HostNameRule hnrOne = new HostNameRule("Name");

    @Test
    public void testGetMessageResponseStatus() {
        assertEquals("InvalidHostName", hnrOne.getMessageResponseStatus().getMessage());
    }

    @Test
    public void testGetMessage() {
        assertEquals("Invalid Host Name : \"Name\"", hnrOne.getMessage());
    }
}
