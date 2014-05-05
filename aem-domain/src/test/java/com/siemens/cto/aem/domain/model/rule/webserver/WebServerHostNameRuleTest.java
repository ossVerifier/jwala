package com.siemens.cto.aem.domain.model.rule.webserver;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class WebServerHostNameRuleTest {
    WebServerHostNameRule hnrOne = new WebServerHostNameRule("Name");

    @Test
    public void testGetMessageResponseStatus() {
        assertEquals("InvalidWebServerHostName", hnrOne.getMessageResponseStatus().getMessage());
    }

    @Test
    public void testGetMessage() {
        assertEquals("Invalid Host Name : \"Name\"", hnrOne.getMessage());
    }
}
