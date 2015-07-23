package com.siemens.cto.aem.domain.model.rule.jvm;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.domain.model.rule.HostNameRule;
import org.junit.Test;

import static org.junit.Assert.*;

public class HostNameRuleTest {

    @Test
    public void testValidHostNames() {

        final String[] validNames = {"abc", "def", "AReallyLongNameGoesHere"};

        for (final String name : validNames) {
            final HostNameRule rule = new HostNameRule(name);
            assertTrue(rule.isValid());
            rule.validate();
        }
    }

    @Test
    public void testInvalidHostNames() {

        final String[] invalidNames = {"", "      ", null, "uri_with_underscore", "uri with spaces", "uriwith@#$%^&*"};

        for (final String name : invalidNames) {
            final HostNameRule rule = new HostNameRule(name);
            assertFalse(rule.isValid());
            try {
                rule.validate();
                fail("Rule should not have validated");
            } catch(final BadRequestException bre) {
                assertTrue(true);
            }
        }
    }
}
