package com.siemens.cto.aem.domain.model.rule.jvm;

import com.siemens.cto.aem.common.exception.BadRequestException;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class JvmNameRuleTest {

    @Test
    public void testValidNames() {
        final String[] validNames = {"abc", "def", "a really long name"};

        for (final String name : validNames) {
            final JvmNameRule rule = new JvmNameRule(name);
            assertTrue(rule.isValid());
            rule.validate();
        }
    }

    @Test
    public void testInvalidNames() {
        final String[] invalidNames = {"", "    ", null};

        for (final String name : invalidNames) {
            final JvmNameRule rule = new JvmNameRule(name);
            assertFalse(rule.isValid());
            try {
                rule.validate();
            } catch (final BadRequestException bre) {
                assertTrue(true);
            }
        }
    }
}
