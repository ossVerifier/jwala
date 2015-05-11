package com.siemens.cto.aem.domain.model.webserver;

import java.util.HashMap;
import java.util.Map;

import com.siemens.cto.aem.domain.model.state.OperationalState;
import com.siemens.cto.aem.domain.model.state.Stability;
import com.siemens.cto.aem.domain.model.state.Transience;

import static com.siemens.cto.aem.domain.model.state.Stability.STABLE;
import static com.siemens.cto.aem.domain.model.state.Stability.UNSTABLE;
import static com.siemens.cto.aem.domain.model.state.Transience.PERMANENT;
import static com.siemens.cto.aem.domain.model.state.Transience.TRANSIENT;

public enum WebServerReachableState implements OperationalState {

    WS_REACHABLE        ("STARTED",  PERMANENT, STABLE),
    WS_UNREACHABLE      ("STOPPED",  PERMANENT, STABLE),
    WS_UNKNOWN          ("UNKNOWN",  PERMANENT, UNSTABLE),
    WS_STARTING         ("STARTING", TRANSIENT, UNSTABLE),
    WS_STOPPING         ("STOPPING", TRANSIENT, UNSTABLE),
    WS_FAILED           ("FAILED",   PERMANENT, UNSTABLE);

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
        return WS_UNKNOWN;
    }

    private final Transience transientState;
    private final Stability stableState;
    private final String externalName;

    private WebServerReachableState(final String theExternalName,
                                    final Transience theTransientState,
                                    final Stability theStableState) {
        externalName = theExternalName;
        transientState = theTransientState;
        stableState = theStableState;
    }

    @Override
    public String toStateString() {
        return externalName;
    }

    @Override
    public Transience getTransience() {
        return transientState;
    }

    @Override
    public Stability getStability() {
        return stableState;
    }
}
