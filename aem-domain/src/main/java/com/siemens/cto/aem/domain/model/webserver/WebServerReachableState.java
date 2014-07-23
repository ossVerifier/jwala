package com.siemens.cto.aem.domain.model.webserver;

import java.util.HashMap;
import java.util.Map;

import com.siemens.cto.aem.domain.model.state.ExternalizableState;

public enum WebServerReachableState implements ExternalizableState {

    REACHABLE("REACHABLE"),
    UNREACHABLE("UNREACHABLE"),
    UNKNOWN("UNKNOWN"),
    START_REQUESTED("START_REQUESTED"),
    STOP_REQUESTED("STOP_REQUESTED");

    private static final Map<String, WebServerReachableState> LOOKUP_MAP = new HashMap<>(values().length);

    static {
        for (final WebServerReachableState state : values()) {
            LOOKUP_MAP.put(state.externalName, state);
        }
    }

    public static WebServerReachableState convertFrom(final String anExternalName) {
        if (LOOKUP_MAP.containsKey(anExternalName)) {
            return LOOKUP_MAP.get(anExternalName);
        }
        return UNKNOWN;
    }

    private final String externalName;

    private WebServerReachableState(final String theExternalName) {
        externalName = theExternalName;
    }

    @Override
    public String toStateString() {
        return externalName;
    }
}
