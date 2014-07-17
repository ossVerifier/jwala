package com.siemens.cto.aem.domain.model.group;


public enum GroupState {

    INITIALIZED("INITIALIZED"),
    PARTIAL("PARTIAL"),
    ERROR("ERROR"),
    STARTED("STARTED"),
    STOPPED("STOPPED"),
    STARTING("STARTING"),
    STOPPING("STOPPING");

    private final String stateName;

    private GroupState(final String theStateName) {
        stateName = theStateName;
    }

    public String getStateName() {
        return stateName;
    }
}
