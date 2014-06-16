package com.siemens.cto.aem.domain.model.rule;

import org.junit.Test;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.domain.model.fault.AemFaultType;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PortNumberRuleTest {
    AemFaultType error = AemFaultType.INVALID_WEBSERVER_PORT;
    PortNumberRule pnrValid = new PortNumberRule(Integer.valueOf(1), error);
    PortNumberRule pnrNull = new PortNumberRule(null, error);
    PortNumberRule pnrOne = new PortNumberRule(Integer.valueOf(0), error);
    PortNumberRule pnrTwo = new PortNumberRule(Integer.valueOf(65536), error);

    PortNumberRule nullValueValid = new PortNumberRule(null, error, true);
    PortNumberRule nullValueInvalid1 = new PortNumberRule(null, error);
    PortNumberRule nullValueInvalid2 = new PortNumberRule(null, error, false);


    @Test
    public void testIsValid() {
        assertTrue(pnrValid.isValid());
        assertFalse(pnrNull.isValid());
        assertFalse(pnrOne.isValid());
        assertFalse(pnrTwo.isValid());
    }

    @Test
    public void testValidate() {
        pnrValid.validate();
    }

    @Test(expected = BadRequestException.class)
    public void testValidateNotValid() {
        pnrNull.validate();
    }

    @Test
    public void testGetMessageResponseStatus() {
        assertEquals("InvalidWebServerPortNumber", pnrOne.getMessageResponseStatus().getMessage());
    }

    @Test
    public void testGetMessage() {
        assertEquals("Port specified is invalid.", pnrNull.getMessage());
        assertEquals("Port specified is invalid (0).", pnrOne.getMessage());
    }

    @Test
    public void testPortValidationIfNullableIsTrue() {
        assertTrue(nullValueValid.isValid());
        assertEquals("Port specified is invalid.", nullValueInvalid1.getMessage());
        assertEquals("Port specified is invalid.", nullValueInvalid2.getMessage());
    }

    @Test
    public void testInvalidPortNumberWhileNullable() {
        final PortNumberRule rule = new PortNumberRule(-12,
                                                       AemFaultType.INVALID_WEBSERVER_PORT,
                                                       true);
        assertFalse(rule.isValid());
    }
}
