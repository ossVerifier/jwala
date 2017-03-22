package com.cerner.jwala.common;

import com.cerner.jwala.common.properties.PropertyKeys;
import junit.framework.TestCase;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cerner.jwala.common.exception.ApplicationException;
import com.cerner.jwala.common.properties.ApplicationProperties;
import sun.applet.AppletListener;

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
        assertEquals(ApplicationProperties.get(PropertyKeys.REMOTE_PATHS_APACHE_HTTPD_CONF), ApplicationProperties.get("remote.paths.httpd.conf"));
        assertEquals(ApplicationProperties.get(PropertyKeys.STRING_PROPERTY),ApplicationProperties.get("string.property"));
        assertEquals("string property",ApplicationProperties.get(PropertyKeys.STRING_PROPERTY));
        assertEquals(Integer.valueOf(5), ApplicationProperties.getAsInteger(PropertyKeys.INTEGER_PROPERTY.getPropertyName()));
        assertEquals(Boolean.TRUE, ApplicationProperties.getAsBoolean(PropertyKeys.BOOLEAN_PROPERTY.getPropertyName()));
    }


    public void testOverloadedMethod(){
        assertEquals(Boolean.TRUE, ApplicationProperties.getAsBoolean(PropertyKeys.BOOLEAN_PROPERTY));
        assertEquals(Integer.valueOf(5), ApplicationProperties.getAsInteger(PropertyKeys.INTEGER_PROPERTY));
        assertEquals("600",ApplicationProperties.get(PropertyKeys.REMOTE_JWALA_EXECUTION_TIMEOUT_SECONDS,"600"));

    }

    public void testReload() {
        System.setProperty(ApplicationProperties.PROPERTIES_ROOT_PATH, new File(".").getAbsolutePath() + "/src/test/resources/properties/reload");
        ApplicationProperties.reload();
        assertEquals("reloaded", ApplicationProperties.get("reload.property"));
        assertNull(ApplicationProperties.get("home team"));
    }
}
