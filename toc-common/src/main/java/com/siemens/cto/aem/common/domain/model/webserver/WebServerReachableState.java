package com.siemens.cto.aem.common.domain.model.webserver;

import com.siemens.cto.aem.common.domain.model.state.OperationalState;

import java.util.HashMap;
import java.util.Map;

public enum WebServerReachableState implements OperationalState {

    WS_REACHABLE        ("STARTED"),
    WS_UNREACHABLE      ("STOPPED"),
    WS_UNKNOWN          ("UNKNOWN"),
    WS_START_SENT       ("START SENT"),
    WS_STOP_SENT        ("STOP SENT"),
    WS_FAILED           ("FAILED");

    private static final Map<String, WebServerReachableState> LOOKUP_MAP = new HashMap<>(values().length);

    static {
        for (final WebServerReachableState state : values()) {
            LOOKUP_MAP.put(state.toPersistentString(), state);
        }
    }

    public static WebServerReachableState convertFrom(final String anExternalName) {
        if (LOOKUP_MAP.containsKey(anExternalName)) {
            return LOOKUP_MAP.get(anExternalName);
        }
        return WS_UNKNOWN;
    }

    private final String externalName;

    private WebServerReachableState(final String theExternalName) {
        externalName = theExternalName;
    }

    @Override
    public String toStateString() {
        return externalName;
    }

    @Override
    public String toPersistentString() {
        return name();
    }
}
