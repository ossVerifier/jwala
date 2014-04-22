package com.siemens.cto.aem.domain.model.rule.jvm;

import org.junit.Test;

import com.siemens.cto.aem.common.exception.BadRequestException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class JvmHostNameRuleTest {

    @Test
    public void testValidHostNames() {

        final String[] validNames = {"abc", "def", "a really long name goes here"};

        for (final String name : validNames) {
            final JvmHostNameRule rule = new JvmHostNameRule(name);
            assertTrue(rule.isValid());
            rule.validate();
        }
    }

    @Test
    public void testInvalidHostNames() {

        final String[] invalidNames = {"", "      ", null};

        for (final String name : invalidNames) {
            final JvmHostNameRule rule = new JvmHostNameRule(name);
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
