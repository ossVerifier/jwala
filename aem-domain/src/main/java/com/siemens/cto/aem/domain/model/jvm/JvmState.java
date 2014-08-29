package com.siemens.cto.aem.domain.model.jvm;

import java.util.HashMap;
import java.util.Map;

import com.siemens.cto.aem.domain.model.state.ExternalizableState;

public enum JvmState implements ExternalizableState {

    INITIALIZED("INITIALIZED", false),
    FAILED("FAILED", false),
    STARTED("STARTED", false),
    STOPPED("STOPPED", false),
    UNKNOWN("UNKNOWN", false),
    START_REQUESTED("STARTING", true),
    STOP_REQUESTED("STOPPING", true);

    private static final Map<String, JvmState> LOOKUP_MAP = new HashMap<>();
    private final boolean isTransientState;

    static {
        for (final JvmState state : values()) {
            LOOKUP_MAP.put(state.toStateString(), state);
        }
    }

    private final String stateName;

    private JvmState(final String theStateName, final boolean isTransientState) {
        stateName = theStateName;
        this.isTransientState = isTransientState;
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

    @Override
    public boolean isTransientState() {
        return isTransientState;
    }
}