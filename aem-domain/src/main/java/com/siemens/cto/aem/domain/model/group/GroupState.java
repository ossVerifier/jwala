package com.siemens.cto.aem.domain.model.group;

import com.siemens.cto.aem.domain.model.state.ExternalizableState;


public enum GroupState implements ExternalizableState {

    INITIALIZED("INITIALIZED"),
    PARTIAL("PARTIAL"),
    ERROR("ERROR"),
    STARTED("STARTED"),
    STOPPED("STOPPED"),
    STARTING("STARTING"),
    STOPPING("STOPPING"),
    UNKNOWN("UNKNOWN");

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
}
