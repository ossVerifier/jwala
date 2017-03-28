package com.cerner.jwala.common;

import junit.framework.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cerner.jwala.common.exception.ApplicationException;
import com.cerner.jwala.common.properties.ApplicationProperties;

import java.io.File;

public class ApplicationPropertiesTest extends TestCase {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationPropertiesTest.class);

    public void setUp() {
        System.setProperty(ApplicationProperties.PROPERTIES_ROOT_PATH, new File(".").getAbsolutePath() + "/src/test/resources/properties");
        LOGGER.debug("Loading properties from dir " + System.getProperty(ApplicationProperties.PROPERTIES_ROOT_PATH));
    }

    public void testBadPropertiesPath() {
        System.setProperty(ApplicationProperties.PROPERTIES_ROOT_PATH, new File(".").getAbsolutePath() + "/blah");
        try {
            ApplicationProperties.get("doesn't matter");
        } catch (ApplicationException e) {
            assertTrue(true);
            return;
        }

        assertFalse(false);
    }

    public void testLoadProperties() {
        assertTrue(ApplicationProperties.size() > 0);
    }

    public void testReadProperties() {
        assertEquals("string property", ApplicationProperties.get("string.property"));
        assertEquals(Integer.valueOf(5), ApplicationProperties.getAsInteger("integer.property"));
        assertEquals(Boolean.TRUE, ApplicationProperties.getAsBoolean("boolean.property"));
    }

    public void testReload() {
        System.setProperty(ApplicationProperties.PROPERTIES_ROOT_PATH, new File(".").getAbsolutePath() + "/src/test/resources/properties/reload");
        ApplicationProperties.reload();
        assertEquals("reloaded", ApplicationProperties.get("reload.property"));
        assertNull(ApplicationProperties.get("home team"));
    }

    public void testAsIntegerDefaultValue() {
        assertEquals(new Integer(1111), ApplicationProperties.getAsInteger("xxxxxx.not.an.integer.property.that.exists.xxxxxx", 1111));
        assertEquals(new Integer(5), ApplicationProperties.getAsInteger("net.stop.sleep.time.seconds", 1111));

        // test asInteger without default value
        boolean caughtApplicationException = false;
        try {
            ApplicationProperties.getAsInteger("remote.paths.instances");
        } catch (Exception e) {
            assertTrue(e instanceof ApplicationException);
            assertTrue(e.getMessage().contains("Expecting an integer"));
            caughtApplicationException = true;
        }
        assertTrue(caughtApplicationException);

        // test asInteger that takes a default value
        caughtApplicationException = false;
        try {
            ApplicationProperties.getAsInteger("remote.paths.instances", 1111);
        } catch (Exception e) {
            assertTrue(e instanceof ApplicationException);
            assertTrue(e.getMessage().contains("Expecting an integer"));
            caughtApplicationException = true;
        }
        assertTrue(caughtApplicationException);
    }

    public void testGetWithDefault() {
        assertEquals("d:/jwala/app/instances",ApplicationProperties.get("remote.paths.instances", "default-value"));
        assertEquals("default-value",ApplicationProperties.get("xxx.remote.paths.instances.xxx", "default-value"));
    }
}
