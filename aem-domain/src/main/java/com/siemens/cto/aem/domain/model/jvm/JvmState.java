package com.siemens.cto.aem.domain.model.jvm;

import java.util.HashMap;
import java.util.Map;

import com.siemens.cto.aem.domain.model.state.OperationalState;
import com.siemens.cto.aem.domain.model.state.Stability;
import com.siemens.cto.aem.domain.model.state.Transience;

import static com.siemens.cto.aem.domain.model.state.Stability.STABLE;
import static com.siemens.cto.aem.domain.model.state.Stability.UNSTABLE;
import static com.siemens.cto.aem.domain.model.state.Transience.PERMANENT;
import static com.siemens.cto.aem.domain.model.state.Transience.TRANSIENT;

public enum JvmState implements OperationalState {

    INITIALIZED("INITIALIZED", PERMANENT, UNSTABLE),
    FAILED("FAILED", PERMANENT, UNSTABLE),
    STARTED("STARTED", PERMANENT, STABLE),
    STOPPED("STOPPED", PERMANENT, STABLE),
    UNKNOWN("UNKNOWN", PERMANENT, UNSTABLE),
    START_REQUESTED("STARTING", TRANSIENT, UNSTABLE),
    STOP_REQUESTED("STOPPING", TRANSIENT, UNSTABLE);

    private static final Map<String, JvmState> LOOKUP_MAP = new HashMap<>();

    static {
        for (final JvmState state : values()) {
            LOOKUP_MAP.put(state.toStateString(), state);
        }
    }

    public static JvmState convertFrom(final String aStateName) {
        if (LOOKUP_MAP.containsKey(aStateName)) {
            return LOOKUP_MAP.get(aStateName);
        }
        return UNKNOWN;
    }

    private final String stateName;
    private final Transience transientState;

    private final Stability stableState;

    private JvmState(final String theStateName,
                     final Transience theTransientState,
                     final Stability theStableState) {
        stateName = theStateName;
        transientState = theTransientState;
        stableState = theStableState;
    }

    @Override
    public String toStateString() {
        return stateName;
    }

    @Override
    public String toString() {
        return stateName;
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
