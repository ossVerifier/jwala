package com.siemens.cto.aem.domain.model.platform;

import java.util.HashMap;
import java.util.Map;

public enum Platform {

    WINDOWS("windows");
    //LINUX("linux"),
    //OSX("osx"),
    //etc.

    private static final Map<String, Platform> LOOKUP_MAP = new HashMap<>();

    static {
        for (final Platform p : values()) {
            LOOKUP_MAP.put(p.platformName, p);
        }
    }

    private final String platformName;

    private Platform(final String thePlatformName) {
        platformName = thePlatformName;
    }

    public String getPlatformName() {
        return platformName;
    }

    public static Platform lookup(final String aPlatformName) {
        return LOOKUP_MAP.get(aPlatformName);
    }
}
