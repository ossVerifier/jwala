package com.siemens.cto.aem.common.rule;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.common.domain.model.path.Path;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by Z003BPEJ on 12/8/14.
 *
 * Tests for {@link com.siemens.cto.aem.common.rule.StatusPathRule}
 */
public class StatusPathRuleTest {

    @Test
    public void testValidStatusPathValues() {
        final String[] validValues = {"/abc", "/def", "/AReallyLongStatusPathGoesHere"};

        for (final String val : validValues) {
            final StatusPathRule rule = new StatusPathRule(new Path(val));
            assertTrue(rule.isValid());
            rule.validate();
        }
    }

    @Test
    public void testInvalidHostNames() {
        final String[] invalidValues = {"", "      ", "uri_with_underscore", "uri with spaces", "uriwith@#$%^&*"};

        for (final String val : invalidValues) {
            final StatusPathRule rule = new StatusPathRule(new Path(val));
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