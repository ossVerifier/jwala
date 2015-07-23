package com.siemens.cto.aem.domain.model.rule;

import com.siemens.cto.aem.common.exception.BadRequestException;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public abstract class AbstractIdRuleTest {

    private Rule validRule;
    private Rule invalidRule;

    @Before
    public void setup() {
        validRule = createValidRule();
        invalidRule = createInvalidRule();
    }

    @Test
    public void testRuleIsValid() {
        assertTrue(validRule.isValid());
    }

    @Test
    public void testNoExceptionOnValidateWithValidRule() {
        validRule.validate();
    }

    @Test
    public void testRuleIsInvalid() {
        assertFalse(invalidRule.isValid());
    }

    @Test(expected = BadRequestException.class)
    public void testExceptionOnValidateWithInvalidRule() {
        invalidRule.validate();
    }

    protected abstract Rule createValidRule();

    protected abstract Rule createInvalidRule();
}
