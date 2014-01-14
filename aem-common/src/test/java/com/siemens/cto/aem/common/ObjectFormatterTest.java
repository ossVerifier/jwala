package com.siemens.cto.aem.common;

import java.lang.reflect.Field;

import junit.framework.TestCase;

/**
 * Created by z002xuvs on 12/2/13.
 */
public class ObjectFormatterTest extends TestCase {

    public void testGetFieldValue() {
        final Field f = ToStringTestObject.class.getDeclaredFields()[0];
        final ObjectFormatter formatter = new ObjectFormatter(new ToStringTestObject());
        formatter.setDoPrintAll(false);
        final Object o = formatter.fieldValue(f);
        assertNull(o);
    }

    public void testGenerateToString() {
        final ToStringTestObject toStringTestObject = new ToStringTestObject();
        assertEquals("ToStringTestObject[testString=foo, testCalendar=02/01/13 00:00:00 AM, testInteger=2013]",
                new ObjectFormatter(toStringTestObject).format());
    }

    public void testGenerateToStringNull() {
        final ToStringTestObject toStringTestObject = null;
        assertEquals(null, new ObjectFormatter(toStringTestObject).format());
    }
}
