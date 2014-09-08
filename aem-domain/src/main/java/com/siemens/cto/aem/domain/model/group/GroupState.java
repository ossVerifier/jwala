package com.siemens.cto.aem.domain.model.group;

import java.util.HashMap;
import java.util.Map;

import com.siemens.cto.aem.domain.model.state.OperationalState;
import com.siemens.cto.aem.domain.model.state.Stability;
import com.siemens.cto.aem.domain.model.state.Transience;

import static com.siemens.cto.aem.domain.model.state.Stability.STABLE;
import static com.siemens.cto.aem.domain.model.state.Stability.UNSTABLE;
import static com.siemens.cto.aem.domain.model.state.Transience.PERMANENT;
import static com.siemens.cto.aem.domain.model.state.Transience.TRANSIENT;


public enum GroupState implements OperationalState {

    INITIALIZED("INITIALIZED", PERMANENT, UNSTABLE),
    PARTIAL("PARTIAL", PERMANENT, STABLE),
    ERROR("ERROR", PERMANENT, UNSTABLE),
    STARTED("STARTED", PERMANENT, STABLE),
    STOPPED("STOPPED", PERMANENT, STABLE),
    STARTING("STARTING", TRANSIENT, UNSTABLE),
    STOPPING("STOPPING", TRANSIENT, UNSTABLE),
    UNKNOWN("UNKNOWN", PERMANENT, UNSTABLE);

    private static final Map<String, GroupState> LOOKUP_MAP = new HashMap<>();

    static {
        for (final GroupState state : values()) {
            LOOKUP_MAP.put(state.toStateString(), state);
        }
    }

    public static GroupState convertFrom(final String aStateName) {
        if (LOOKUP_MAP.containsKey(aStateName)) {
            return LOOKUP_MAP.get(aStateName);
        }
        return UNKNOWN;
    }

    private final String stateName;
    private final Transience transientState;
    private final Stability stableState;

    private GroupState(final String theStateName,
                       final Transience theTransientState,
                       final Stability theStableState) {
        stateName = theStateName;
        transientState = theTransientState;
        stableState = theStableState;
    }

    public String getStateName() {
        return stateName;
    }

    @Override
    public String toStateString() {
        return stateName;
    }

    @Override
    public String toString() {
        return toStateString();
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
