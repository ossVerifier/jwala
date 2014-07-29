package com.siemens.cto.aem.domain.model.jvm;

import java.util.HashMap;
import java.util.Map;

import com.siemens.cto.aem.domain.model.state.ExternalizableState;

public enum JvmState implements ExternalizableState {

    INITIALIZED("INITIALIZED"),
    FAILED("FAILED"),
    STARTED("STARTED"),
    STOPPED("STOPPED"),
    UNKNOWN("UNKNOWN"),
    START_REQUESTED("STARTING"),
    STOP_REQUESTED("STOPPING");

    private static final Map<String, JvmState> LOOKUP_MAP = new HashMap<>();

    static {
        for (final JvmState state : values()) {
            LOOKUP_MAP.put(state.toStateString(), state);
        }
    }

    private final String stateName;

    private JvmState(final String theStateName) {
        stateName = theStateName;
    }

    @Override
    public String toStateString() {
        return stateName;
    }

    public static JvmState convertFrom(final String aStateName) {
        if (LOOKUP_MAP.containsKey(aStateName)) {
            return LOOKUP_MAP.get(aStateName);
        }
        return UNKNOWN;
    }

    @Override
    public String toString() {
        return stateName;
    }
}
