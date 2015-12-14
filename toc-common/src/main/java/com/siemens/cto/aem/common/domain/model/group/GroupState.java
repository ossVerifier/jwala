package com.siemens.cto.aem.common.domain.model.group;

import com.siemens.cto.aem.common.domain.model.state.OperationalState;

import java.util.HashMap;
import java.util.Map;


public enum GroupState implements OperationalState {

    GRP_INITIALIZED("INITIALIZED"),
    GRP_PARTIAL( "PARTIAL"),
    GRP_FAILURE( "FAILED"),
    GRP_STARTED( "STARTED"),
    GRP_STOPPED( "STOPPED"),
    GRP_STARTING("STARTING"),
    GRP_STOPPING("STOPPING"),
    GRP_UNKNOWN( "UNKNOWN");

    private static final Map<String, GroupState> LOOKUP_MAP = new HashMap<>();

    static {
        for (final GroupState state : values()) {
            LOOKUP_MAP.put(state.toPersistentString(), state);
        }
    }

    public static GroupState convertFrom(final String aStateName) {
        if (LOOKUP_MAP.containsKey(aStateName)) {
            return LOOKUP_MAP.get(aStateName);
        }
        return GRP_UNKNOWN;
    }

    private final String stateName;

    private GroupState(final String theStateName) {
        stateName = theStateName;
    }

    public String getStateName() {
        return stateName;
    }

    @Override
    public String toStateString() {
        return stateName;
    }

    @Override
    public String toPersistentString() {
        return name();
    }
}
