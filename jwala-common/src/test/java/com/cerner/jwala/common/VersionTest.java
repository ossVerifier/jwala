package com.cerner.jwala.common;

import com.cerner.jwala.common.Version;

import junit.framework.TestCase;

public class VersionTest extends TestCase {

    public void testGetTitle() {
        final String title = Version.getTitle();
        assertEquals("jwala-common", title);
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
