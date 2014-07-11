package com.siemens.cto.aem.domain.model.state.message;

public enum StateMessageKey {

    ID("ID");

    private final String key;

    private StateMessageKey(final String theKey) {
        key = theKey;
    }

    public String getKey() {
        return key;
    }
}
