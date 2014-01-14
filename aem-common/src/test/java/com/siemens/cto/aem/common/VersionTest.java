package com.siemens.cto.aem.common;

import junit.framework.TestCase;

public class VersionTest extends TestCase {

    public void testConstructor() {
        ObjectsTest.constructorTest(Version.class);
    }

    public void testGetTitle() {
        final String title = Version.getTitle();
        assertEquals("aem-common", title);
    }

    public void testGetVersion() {
        final String version = Version.getVersion();
        assertEquals("1.0.0.1.100", version);
    }

    public void testGetBuildTime() {
        final String buildTime = Version.getBuildTime();
        assertEquals("2014-01-01T00:00:00-0500", buildTime);
    }
}
