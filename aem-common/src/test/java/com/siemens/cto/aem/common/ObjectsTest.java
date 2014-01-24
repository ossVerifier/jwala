package com.siemens.cto.aem.common;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

/**
 * Created with IntelliJ IDEA. User: z002xuvs Date: 11/21/12 Time: 10:54 PM
 */
public class ObjectsTest extends TestCase {

    public void testIsEmptyNullArray() {
        final Integer[] integers = null;
        assertTrue(Objects.isEmpty(integers));
    }

    public void testIsEmptyArray() {
        final Integer[] integers = new Integer[] {3, 4};
        assertFalse(Objects.isEmpty(integers));
    }

    public void testIsEmptyEmptyArray() {
        final Integer[] integers = new Integer[] {};
        assertTrue(Objects.isEmpty(integers));
    }

    public void testIsEmptySizeNull() {
        assertEquals(0, Objects.size(null));
    }

    public void testIsEmptySizeEmptyCollection() {
        assertEquals(0, Objects.size(new ArrayList()));
    }

    public void testIsEmptySizeOneElement() {
        final List<Integer> l = new ArrayList<Integer>();
        l.add(1);
        assertEquals(1, Objects.size(l));
    }

    public void testIsEmptyNotEmptyList() {
        final List<Integer> l = new ArrayList<Integer>();
        l.add(1);
        assertFalse(Objects.isEmpty(l));
    }

    public void testIsEmptyNullList() {
        final List<Integer> l = null;
        assertTrue(Objects.isEmpty(l));
    }

    public void testIsEmptyEmptyList() {
        final List<Integer> l = new ArrayList<Integer>();
        assertTrue(Objects.isEmpty(l));
    }

    public void testIsEmptyStringNull() {
        final String s = null;
        assertTrue(Objects.isEmpty(s));
    }

    public void testIsEmptyStringEmptyString() {
        final String s = "";
        assertTrue(Objects.isEmpty(s));
    }

    public void testIsEmptyString() {
        final String s = "s";
        assertFalse(Objects.isEmpty(s));
    }

    public void testNotEmptyEmptyString() {
        final String s = "";
        assertFalse(Objects.notEmpty(s));
    }

    public void testNotEmptyStringNull() {
        final String s = null;
        assertFalse(Objects.notEmpty(s));
    }

    public void testNotEmptyString() {
        final String s = "s";
        assertTrue(Objects.notEmpty(s));
    }

    public void testNotEmptyNullCollection() {
        final List<Integer> l = null;
        assertFalse(Objects.notEmpty(l));
    }

    public void testNotEmptyCollection() {
        final List<Integer> l = new ArrayList<Integer>();
        assertFalse(Objects.notEmpty(l));
    }

    public void testNotCollection() {
        final List<Integer> l = new ArrayList<Integer>();
        l.add(1);
        assertTrue(Objects.notEmpty(l));
    }

    public void testSurround() {
        final String string = "s";
        final String surroundedString = Objects.surround(string);
        assertEquals("'s'", surroundedString);
    }

    public void testSurroundNull() {
        final String string = null;
        final String surroundedString = Objects.surround(string);
        assertEquals("'null'", surroundedString);
    }

    public void testConstructor() {
        constructorTest(Objects.class);
    }

    public static void constructorTest(final Class<?> cls) {
        final Constructor<?> c = cls.getDeclaredConstructors()[0];
        c.setAccessible(true);

        Throwable targetException = null;
        try {
            c.newInstance((Object[]) null);
        } catch (final Exception ite) {
            targetException = ite.getCause();
        }

        assertNotNull(targetException);
        assertEquals(targetException.getClass(), InstantiationException.class);
    }
}
