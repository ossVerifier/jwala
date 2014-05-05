package com.siemens.cto.aem.domain.model.rule;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

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
