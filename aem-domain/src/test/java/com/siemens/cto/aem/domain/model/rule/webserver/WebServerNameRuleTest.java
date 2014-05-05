package com.siemens.cto.aem.domain.model.rule.webserver;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.siemens.cto.aem.common.exception.BadRequestException;

public class WebServerNameRuleTest {

    @Test
    public void testValidNames() {
        final String[] validNames = {"abc", "def", "a really long name"};

        for (final String name : validNames) {
            final WebServerNameRule rule = new WebServerNameRule(name);
            assertTrue(rule.isValid());
            rule.validate();
        }
    }

    @Test
    public void testInvalidNames() {
        final String[] invalidNames = {"", "    ", null};

        for (final String name : invalidNames) {
            final WebServerNameRule rule = new WebServerNameRule(name);
            assertFalse(rule.isValid());
            try {
                rule.validate();
            } catch (final BadRequestException bre) {
                assertTrue(true);
            }
        }
    }
}
