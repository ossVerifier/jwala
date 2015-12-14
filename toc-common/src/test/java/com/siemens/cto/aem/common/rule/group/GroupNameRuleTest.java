package com.siemens.cto.aem.common.rule.group;

import com.siemens.cto.aem.common.exception.MessageResponseStatus;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class GroupNameRuleTest {
    private final GroupNameRule rule = new GroupNameRule("");

    @Test
    public void testGetMessageResponseStatus() {
        final MessageResponseStatus messageResponseStatus = rule.getMessageResponseStatus();
        assertEquals("InvalidGroupName", messageResponseStatus.getMessage());
    }

    @Test
    public void testGetMessage() {
        final String message = rule.getMessage();
        assertEquals("Invalid Group Name: \"\"", message);
    }
}
